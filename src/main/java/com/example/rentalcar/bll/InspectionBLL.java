package com.example.rentalcar.bll;

import com.example.rentalcar.dao.InspectionDAO;
import com.example.rentalcar.models.Inspections;
import com.example.rentalcar.utils.AppSession;

import java.util.List;

public class InspectionBLL {

    private final InspectionDAO inspectionDAO = new InspectionDAO();

    // ==========================================================
    // 1. NHÓM HÀM TRUY VẤN (Tất cả mọi người đều được xem)
    // ==========================================================
    public List<Inspections> getAllInspections() {
        return inspectionDAO.findAll();
    }

    public Inspections getInspectionById(int id) {
        return inspectionDAO.findById(id);
    }

    // ==========================================================
    // 2. NHÓM HÀM THÊM / SỬA (Nhân viên và Admin đều làm được)
    // ==========================================================
    public boolean createInspection(Inspections inspection) {
        validateInspection(inspection);

        // NGHIỆP VỤ: Tự động gán người lập biên bản là người đang đăng nhập hệ thống
        if (AppSession.getCurrentUser() != null) {
            inspection.setId_user(AppSession.getCurrentUser());
        } else {
            throw new IllegalStateException("Lỗi bảo mật: Không xác định được phiên đăng nhập!");
        }

        return inspectionDAO.insert(inspection);
    }

    public boolean updateInspection(Inspections inspection) {
        validateInspection(inspection);

        Inspections existing = inspectionDAO.findById(inspection.getId_inspection());
        if (existing == null) {
            throw new IllegalArgumentException("Không tìm thấy biên bản kiểm tra để cập nhật!");
        }

        // Cập nhật lại người sửa cuối cùng chính là người đang đăng nhập
        if (AppSession.getCurrentUser() != null) {
            inspection.setId_user(AppSession.getCurrentUser());
        }

        return inspectionDAO.update(inspection);
    }

    // ==========================================================
    // 3. XÓA BIÊN BẢN (Nghiệp vụ nhạy cảm - CHỈ ADMIN)
    // ==========================================================
    public boolean deleteInspection(int id) {
        // Ổ KHÓA BẢO MẬT: Chống nhân viên xóa biên bản để phi tang bằng chứng hỏng xe
        if (!AppSession.isAdmin()) {
            throw new IllegalStateException("Cảnh báo bảo mật: Chỉ Quản trị viên mới được phép xóa biên bản kiểm tra xe!");
        }

        Inspections existing = inspectionDAO.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Không tìm thấy biên bản kiểm tra để xóa!");
        }

        return inspectionDAO.delete(id);
    }

    // ==========================================================
    // 4. HÀM KIỂM TRA LOGIC CHUNG
    // ==========================================================
    private void validateInspection(Inspections inspection) {
        if (inspection.getId_contract() == null || inspection.getId_contract().getId_contract() <= 0) {
            throw new IllegalArgumentException("Biên bản phải được gắn với một hợp đồng hợp lệ!");
        }
        if (inspection.getInspection_type() == null) {
            throw new IllegalArgumentException("Phải xác định rõ đây là biên bản lúc GIAO_XE hay lúc TRA_XE!");
        }
    }
}