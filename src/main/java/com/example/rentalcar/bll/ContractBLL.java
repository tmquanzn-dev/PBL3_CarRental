package com.example.rentalcar.bll;

import com.example.rentalcar.dao.ContractDAO;
import com.example.rentalcar.dao.PenaltyDAO;
import com.example.rentalcar.models.Contracts;
import com.example.rentalcar.models.Penalties;
import com.example.rentalcar.models.StatusContracts;
import com.example.rentalcar.models.StatusVehicle;
import com.example.rentalcar.utils.AppSession;

import java.util.List;

public class ContractBLL {
    private final ContractDAO contractDAO = new ContractDAO();
    private final PenaltyDAO  penaltyDAO  = new PenaltyDAO();
    private final PriceBLL    priceBLL    = new PriceBLL();

    public double getTodayRevenue()        { return contractDAO.getTodayRevenue(); }
    public double getMonthlyRevenue()      { return contractDAO.getMonthlyRevenue(); }
    public int    getActiveContractsCount(){ return contractDAO.getActiveContractsCount(); }

    public List<Contracts> getRecentContracts(int limit) { return contractDAO.findRecentContracts(limit); }
    public List<Contracts> getAllContracts()              { return contractDAO.findAll(); }
    public Contracts       getContractById(int id)       { return contractDAO.findById(id); }

    // =========================================================
    // FIX: Tự động cập nhật QUA_HAN khi mở app
    // Gọi từ DashboardController.initialize()
    // =========================================================
    public int markOverdueContracts() {
        return contractDAO.markOverdueContracts();
    }

    // =========================================================
    // TẠO HỢP ĐỒNG
    // =========================================================
    public boolean createContract(Contracts contract, String voucherCode) {
        if (contract.getStart_datetime() == null || contract.getEnd_datetime() == null)
            throw new IllegalArgumentException("Thời gian thuê không hợp lệ!");
        if (contract.getStart_datetime().isAfter(contract.getEnd_datetime()))
            throw new IllegalArgumentException("Ngày bắt đầu phải trước ngày kết thúc!");

        if (contract.getId_vehicle().getStatus() != StatusVehicle.AVAILABLE)
            throw new IllegalArgumentException("Chiếc xe này hiện không sẵn sàng để thuê!");

        double basePrice = priceBLL.calculateBasePrice(
                contract.getStart_datetime(), contract.getEnd_datetime(),
                contract.getId_vehicle().getPrice_day(),
                contract.getId_vehicle().getPrice_hour());
        contract.setBase_price(basePrice);

        double discount = 0;
        if (voucherCode != null && !voucherCode.isBlank()) {
            VoucherBLL voucherBLL = new VoucherBLL();
            discount = voucherBLL.calculateDiscountAmount(voucherCode, basePrice);
        }

        contract.setDiscount_amount(discount);
        contract.setTotal_price(basePrice - discount);
        contract.setStatus(StatusContracts.DANG_THUE);

        boolean isSuccess = contractDAO.insert(contract);

        if (isSuccess) {
            contract.getId_vehicle().setStatus(StatusVehicle.RENTED);
            VehicleBLL vehicleBLL = new VehicleBLL();
            vehicleBLL.updateVehicle(contract.getId_vehicle());

            if (voucherCode != null && !voucherCode.isBlank()) {
                VoucherBLL voucherBLL = new VoucherBLL();
                voucherBLL.markVoucherAsUsed(voucherCode);
            }
        }

        return isSuccess;
    }

    // =========================================================
    // TRẢ XE
    // =========================================================
    public boolean returnVehicle(Contracts contract) {
        if (contract.getReturn_datetime() == null)
            throw new IllegalArgumentException("Thời gian trả xe không được để trống!");

        // Tổng phạt = đọc từ bảng penalties (đã được insert trước khi gọi hàm này)
        double totalPenalty = getTotalPenalty(contract.getId_contract());

        double finalTotal = contract.getBase_price()
                - contract.getDiscount_amount()
                + totalPenalty;

        contract.setTotal_price(finalTotal);
        contract.setStatus(StatusContracts.HOAN_THANH);

        return contractDAO.update(contract);
    }

    /**
     * Lấy TỔNG TẤT CẢ các khoản phạt của hợp đồng (trễ giờ + xăng + hư hỏng).
     * Gọi sau khi ReturnVehicleController đã insert đủ vào bảng penalties.
     */
    private double getTotalPenalty(int contractId) {
        try {
            return penaltyDAO.findByContractId(contractId).stream()
                    .mapToDouble(Penalties::getAmount)
                    .sum();
        } catch (Exception e) {
            System.err.println("Lỗi lấy tổng phạt: " + e.getMessage());
            return 0;
        }
    }

    // =========================================================
    // HỦY HỢP ĐỒNG — FIX: giảm rental_count khách hàng
    // =========================================================
    public boolean cancelContract(int id) {
        if (!AppSession.isAdmin())
            throw new IllegalStateException("Chỉ Admin mới được phép hủy hợp đồng!");

        Contracts contract = contractDAO.findById(id);
        if (contract == null)
            throw new IllegalArgumentException("Không tìm thấy hợp đồng!");
        if (contract.getStatus() == StatusContracts.HOAN_THANH)
            throw new IllegalStateException("Không thể hủy hợp đồng đã hoàn thành!");

        // FIX: dùng method mới — vừa hủy HĐ vừa giảm rental_count
        boolean cancelled = contractDAO.deleteAndDecrementRentalCount(id);

        // Nếu xe đang RENTED thì chuyển về AVAILABLE
        if (cancelled && contract.getId_vehicle() != null) {
            try {
                VehicleBLL vehicleBLL = new VehicleBLL();
                var vehicle = vehicleBLL.getVehicleById(contract.getId_vehicle().getId_vehicle());
                if (vehicle != null && vehicle.getStatus() == StatusVehicle.RENTED) {
                    vehicle.setStatus(StatusVehicle.AVAILABLE);
                    vehicleBLL.updateVehicle(vehicle);
                }
            } catch (Exception e) {
                System.err.println("Lỗi cập nhật trạng thái xe khi hủy HĐ: " + e.getMessage());
            }
        }

        return cancelled;
    }
}