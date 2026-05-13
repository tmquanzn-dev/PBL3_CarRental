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

    public double getTodayRevenue()       { return contractDAO.getTodayRevenue(); }
    public double getMonthlyRevenue()     { return contractDAO.getMonthlyRevenue(); }
    public int    getActiveContractsCount(){ return contractDAO.getActiveContractsCount(); }

    public List<Contracts> getRecentContracts(int limit) { return contractDAO.findRecentContracts(limit); }
    public List<Contracts> getAllContracts()              { return contractDAO.findAll(); }
    public Contracts       getContractById(int id)       { return contractDAO.findById(id); }

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
    // TRẢ XE – tính đủ: trễ giờ + xăng + hư hỏng
    // =========================================================
    public boolean returnVehicle(Contracts contract) {
        if (contract.getReturn_datetime() == null)
            throw new IllegalArgumentException("Thời gian trả xe không được để trống!");

        // Phạt trễ giờ
        double latePenalty = priceBLL.calculateLatePenalty(
                contract.getEnd_datetime(),
                contract.getReturn_datetime());

        // Phạt thiếu xăng
        double fuelPenalty = priceBLL.calculateFuelPenalty(
                contract.getFuel_start(),
                contract.getFuel_end(),
                contract.getId_vehicle().getFuel_capacity());

        // Phạt hư hỏng: lấy từ bảng penalties đã được tạo trước
        double damagePenalty = getTotalDamagePenalty(contract.getId_contract());

        // Tổng tiền = tiền thuê - giảm giá + các khoản phạt
        double finalTotal = contract.getBase_price()
                - contract.getDiscount_amount()
                + latePenalty
                + fuelPenalty
                + damagePenalty;

        contract.setTotal_price(finalTotal);
        contract.setStatus(StatusContracts.HOAN_THANH);

        return contractDAO.update(contract);
    }

    /**
     * Lấy tổng tiền phạt hư hỏng (HU_HONG) của một hợp đồng.
     * Được gọi sau khi ReturnVehicleController đã tạo các Penalties.
     */
    private double getTotalDamagePenalty(int contractId) {
        try {
            return penaltyDAO.findByContractId(contractId).stream()
                    .filter(p -> p.getPenalty_type() != null
                            && p.getPenalty_type().name().equals("HU_HONG"))
                    .mapToDouble(Penalties::getAmount)
                    .sum();
        } catch (Exception e) {
            System.err.println("Lỗi lấy phạt hư hỏng: " + e.getMessage());
            return 0;
        }
    }

    // =========================================================
    // HỦY HỢP ĐỒNG
    // =========================================================
    public boolean cancelContract(int id) {
        if (!AppSession.isAdmin())
            throw new IllegalStateException("Chỉ Admin mới được phép hủy hợp đồng!");

        Contracts contract = contractDAO.findById(id);
        if (contract == null)
            throw new IllegalArgumentException("Không tìm thấy hợp đồng!");
        if (contract.getStatus() == StatusContracts.HOAN_THANH)
            throw new IllegalStateException("Không thể hủy hợp đồng đã hoàn thành!");

        return contractDAO.delete(id);
    }
}