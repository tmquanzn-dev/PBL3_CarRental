package com.example.rentalcar.bll;

import com.example.rentalcar.dao.ContractDAO;
import com.example.rentalcar.dao.PenaltyDAO;
import com.example.rentalcar.models.Contracts;
import com.example.rentalcar.models.Penalties;
import com.example.rentalcar.models.StatusContracts;
import com.example.rentalcar.models.StatusVehicle;
import com.example.rentalcar.models.Vehicles;
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
    // CODE CỦA BẠN EM: Tự động cập nhật QUA_HAN
    // =========================================================
    public int markOverdueContracts() {
        return contractDAO.markOverdueContracts();
    }

    // =========================================================
    // TẠO HỢP ĐỒNG (Giữ nguyên)
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
    // TRẢ XE: GỘP CODE CỦA EM (20%) VÀ CỦA BẠN (PenaltyDAO)
    // =========================================================
    public boolean returnVehicle(Contracts contract) {
        if (contract.getReturn_datetime() == null)
            throw new IllegalArgumentException("Thời gian trả xe không được để trống!");

        // KHÔNG TÍNH LẠI CHAY TẠI ĐÂY NỮA!
        // Vì bên ReturnVehicleController đã tính toán Live Preview cực chuẩn,
        // bẫy đầy đủ giá giờ/giá ngày và gán thẳng vào đối tượng contract rồi.

        double finalBasePrice = contract.getBase_price();
        double finalDiscount = contract.getDiscount_amount();

        // Giữ lại logic bảo mật chặn voucher vượt quá giá gốc
        if (finalDiscount > finalBasePrice) {
            finalDiscount = finalBasePrice;
            contract.setDiscount_amount(finalDiscount);
        }

        // Chuyển trạng thái hợp đồng sang hoàn thành
        contract.setStatus(StatusContracts.HOAN_THANH);

        // Lưu trực tiếp đối tượng đã có tiền chuẩn xuống MySQL
        return contractDAO.update(contract);
    }

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
    // HỦY HỢP ĐỒNG: GỘP XÓA MỀM (Em) VÀ GIẢM RENTAL COUNT (Bạn)
    // =========================================================
    public boolean cancelContract(int id) {
        if (!AppSession.isAdmin())
            throw new IllegalStateException("Cảnh báo bảo mật: Chỉ Admin mới được phép hủy hợp đồng!");

        Contracts contract = contractDAO.findById(id);
        if (contract == null)
            throw new IllegalArgumentException("Không tìm thấy hợp đồng!");
        if (contract.getStatus() == StatusContracts.HOAN_THANH)
            throw new IllegalStateException("Không thể hủy hợp đồng đã hoàn thành!");
        if (contract.getStatus() == StatusContracts.DA_HUY)
            throw new IllegalStateException("Hợp đồng này đã bị hủy từ trước!");

        // 1. CODE CỦA BẠN EM: Dùng hàm của DAO để update logic giảm rental_count
        // (Lưu ý: Anh gọi hàm của bạn em, nhưng em cần nhắc bạn kiểm tra lại xem hàm này trong DAO là XÓA CỨNG hay XÓA MỀM nhé. Nên là UPDATE status = DA_HUY)
        boolean cancelled = contractDAO.deleteAndDecrementRentalCount(id);

        // 2. CODE CỦA EM: Giải phóng xe
        if (cancelled && contract.getId_vehicle() != null) {
            try {
                VehicleBLL vehicleBLL = new VehicleBLL();
                Vehicles vehicle = vehicleBLL.getVehicleById(contract.getId_vehicle().getId_vehicle());
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