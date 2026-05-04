package com.example.rentalcar.bll;

import com.example.rentalcar.dao.ContractDAO;
import com.example.rentalcar.models.Contracts;
import com.example.rentalcar.models.StatusContracts;
import com.example.rentalcar.models.StatusVehicle;
import com.example.rentalcar.utils.AppSession;

import java.util.List;

public class ContractBLL {
    private final ContractDAO contractDAO = new ContractDAO();
    private final PriceBLL priceBLL = new PriceBLL();

    public double getTodayRevenue() {
        return contractDAO.getTodayRevenue();
    }

    public double getMonthlyRevenue() {
        return contractDAO.getMonthlyRevenue();
    }

    public int getActiveContractsCount() {
        return contractDAO.getActiveContractsCount();
    }

    public List<Contracts> getRecentContracts(int limit) {
        return contractDAO.findRecentContracts(limit);
    }

    public List<Contracts> getAllContracts() {
        return contractDAO.findAll();
    }

    public Contracts getContractById(int id) {
        return contractDAO.findById(id);
    }

    public boolean createContract(Contracts contract, String voucherCode) {
        // 1. Validate thời gian
        if (contract.getStart_datetime() == null || contract.getEnd_datetime() == null)
            throw new IllegalArgumentException("Thời gian thuê không hợp lệ!");
        if (contract.getStart_datetime().isAfter(contract.getEnd_datetime()))
            throw new IllegalArgumentException("Ngày bắt đầu phải trước ngày kết thúc!");

        // 2. Tạm thời bỏ qua việc kiểm tra lịch trùng (sẽ thêm vào sau nếu em muốn làm phức tạp)
        // Nếu muốn làm đơn giản, chỉ cần kiểm tra xem Status của Vehicle có phải là AVAILABLE không là đủ.
        if (contract.getId_vehicle().getStatus() != StatusVehicle.AVAILABLE) {
            throw new IllegalArgumentException("Chiếc xe này hiện không sẵn sàng để thuê!");
        }

        // 3. Tính tiền cơ bản
        double basePrice = priceBLL.calculateBasePrice(
                contract.getStart_datetime(),
                contract.getEnd_datetime(),
                contract.getId_vehicle().getPrice_day(),
                contract.getId_vehicle().getPrice_hour()
        );
        contract.setBase_price(basePrice);

        // 4. CHECK VOUCHER: Gọi VoucherBLL
        double discount = 0;
        if (voucherCode != null && !voucherCode.isBlank()) {
            VoucherBLL voucherBLL = new VoucherBLL();
            discount = voucherBLL.calculateDiscountAmount(voucherCode, basePrice);
        }

        contract.setDiscount_amount(discount);
        contract.setTotal_price(basePrice - discount);
        contract.setStatus(StatusContracts.DANG_THUE); // Cập nhật trạng thái hợp đồng

        // 5. Lưu xuống DB
        boolean isSuccess = contractDAO.insert(contract);

        // 6. Cập nhật lại trạng thái Xe và tăng số lần dùng Voucher nếu thành công
        if (isSuccess) {
            // Đổi trạng thái xe
            contract.getId_vehicle().setStatus(StatusVehicle.RENTED);
            VehicleBLL vehicleBLL = new VehicleBLL();
            vehicleBLL.updateVehicle(contract.getId_vehicle());

            // Tăng số lần dùng Voucher
            if (voucherCode != null && !voucherCode.isBlank()) {
                VoucherBLL voucherBLL = new VoucherBLL();
                voucherBLL.markVoucherAsUsed(voucherCode);
            }
        }

        return isSuccess;
    }

    public boolean returnVehicle(Contracts contract) {
        if (contract.getReturn_datetime() == null)
            throw new IllegalArgumentException("Thời gian trả xe không được để trống!");

        // Tính phạt trễ giờ
        double latePenalty = priceBLL.calculateLatePenalty(
                contract.getEnd_datetime(),
                contract.getReturn_datetime()
        );

        // Tính phạt thiếu xăng
        double fuelPenalty = priceBLL.calculateFuelPenalty(
                contract.getFuel_start(),
                contract.getFuel_end(),
                contract.getId_vehicle().getFuel_capacity()
        );

        // Cộng phạt vào tổng tiền
        double finalTotal = contract.getBase_price()
                - contract.getDiscount_amount()
                + latePenalty
                + fuelPenalty;

        contract.setTotal_price(finalTotal);
        contract.setStatus(StatusContracts.HOAN_THANH);

        return contractDAO.update(contract);
    }

    public boolean cancelContract(int id) {
        if (!AppSession.isAdmin()) throw new IllegalStateException("Cảnh báo bảo mật: Chỉ Admin mới được phép hủy hợp đồng!"); //
        Contracts contract = contractDAO.findById(id);
        if (contract == null)
            throw new IllegalArgumentException("Không tìm thấy hợp đồng!");
        if (contract.getStatus() == StatusContracts.HOAN_THANH)
            throw new IllegalStateException("Không thể hủy hợp đồng đã hoàn thành!");
        return contractDAO.delete(id);
    }
}