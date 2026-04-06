package com.example.rentalcar.bll;

import com.example.rentalcar.dao.ContractDAO;
import com.example.rentalcar.models.Contracts;
import com.example.rentalcar.models.StatusContracts;
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

    public boolean createContract(Contracts contract) {
        // Validate nghiệp vụ
        if (contract.getStart_datetime() == null || contract.getEnd_datetime() == null)
            throw new IllegalArgumentException("Thời gian thuê không hợp lệ!");
        if (contract.getStart_datetime().isAfter(contract.getEnd_datetime()))
            throw new IllegalArgumentException("Ngày bắt đầu phải trước ngày kết thúc!");

        // Tính tiền tự động qua PriceBLL
        double basePrice = priceBLL.calculateBasePrice(
                contract.getStart_datetime(),
                contract.getEnd_datetime(),
                contract.getId_vehicle().getPrice_day(),
                contract.getId_vehicle().getPrice_hour()
        );
        contract.setBase_price(basePrice);
        contract.setTotal_price(basePrice - contract.getDiscount_amount());

        return contractDAO.insert(contract);
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
        Contracts contract = contractDAO.findById(id);
        if (contract == null)
            throw new IllegalArgumentException("Không tìm thấy hợp đồng!");
        if (contract.getStatus() == StatusContracts.HOAN_THANH)
            throw new IllegalStateException("Không thể hủy hợp đồng đã hoàn thành!");
        return contractDAO.delete(id);
    }
}