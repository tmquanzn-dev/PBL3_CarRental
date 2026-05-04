package com.example.rentalcar.bll;

import com.example.rentalcar.dao.PartPriceDAO;
import com.example.rentalcar.models.PartPrices;
import com.example.rentalcar.utils.AppSession;

import java.util.List;

public class PartPriceBLL {

    private final PartPriceDAO partPriceDAO = new PartPriceDAO();

    // ==========================================================
    // 1. NHÓM HÀM TRUY VẤN (Tất cả mọi người đều được xem)
    // ==========================================================

    public List<PartPrices> getAllPartPrices() {
        return partPriceDAO.findAll();
    }

    public PartPrices getPartPriceById(int id) {
        return partPriceDAO.findById(id);
    }

    // ==========================================================
    // 2. NHÓM HÀM CẬP NHẬT (CHỈ ADMIN MỚI ĐƯỢC PHÉP)
    // ==========================================================

    public boolean addPartPrice(PartPrices partPrice) {
        // Ổ khóa phân quyền
        if (!AppSession.isAdmin()) {
            throw new IllegalStateException("Cảnh báo: Chỉ Quản trị viên mới được thêm giá phụ tùng!");
        }

        validatePartPrice(partPrice);
        return partPriceDAO.insert(partPrice);
    }

    public boolean updatePartPrice(PartPrices partPrice) {
        // Ổ khóa phân quyền
        if (!AppSession.isAdmin()) {
            throw new IllegalStateException("Cảnh báo: Chỉ Quản trị viên mới được sửa giá phụ tùng!");
        }

        validatePartPrice(partPrice);

        PartPrices existing = partPriceDAO.findById(partPrice.getId_part_price());
        if (existing == null) {
            throw new IllegalArgumentException("Không tìm thấy phụ tùng để cập nhật!");
        }

        return partPriceDAO.update(partPrice);
    }

    public boolean deletePartPrice(int id) {
        // Ổ khóa phân quyền
        if (!AppSession.isAdmin()) {
            throw new IllegalStateException("Cảnh báo: Chỉ Quản trị viên mới được xóa giá phụ tùng!");
        }

        PartPrices existing = partPriceDAO.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Không tìm thấy phụ tùng để xóa!");
        }

        return partPriceDAO.delete(id);
    }

    // ==========================================================
    // 3. HÀM KIỂM TRA LOGIC CHUNG
    // ==========================================================
    private void validatePartPrice(PartPrices partPrice) {
        if (partPrice.getPart_name() == null || partPrice.getPart_name().isBlank()) {
            throw new IllegalArgumentException("Tên phụ tùng không được để trống!");
        }
        if (partPrice.getVehicle() == null || partPrice.getVehicle().isBlank()) {
            throw new IllegalArgumentException("Phân loại xe (Vehicle) không được để trống!");
        }
        if (partPrice.getPrice() < 0) {
            throw new IllegalArgumentException("Giá phụ tùng không được là số âm!");
        }
    }
}