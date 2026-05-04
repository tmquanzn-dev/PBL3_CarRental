package com.example.rentalcar.bll;

import com.example.rentalcar.dao.PenaltyDAO;
import com.example.rentalcar.models.Penalties;
import com.example.rentalcar.utils.AppSession;

import java.util.List;

public class PenaltyBLL {

    private final PenaltyDAO penaltyDAO = new PenaltyDAO();

    // ==========================================================
    // 1. NHÓM HÀM TRUY VẤN
    // ==========================================================
    public List<Penalties> getAllPenalties() {
        return penaltyDAO.findAll();
    }

    public Penalties getPenaltyById(int id) {
        return penaltyDAO.findById(id);
    }

    // ==========================================================
    // 2. NHÓM HÀM THÊM MỚI (Staff và Admin đều làm được)
    // ==========================================================
    public boolean createPenalty(Penalties penalty) {
        validatePenalty(penalty);
        return penaltyDAO.insert(penalty);
    }

    // ==========================================================
    // 3. NHÓM HÀM SỬA / XÓA (Nghiệp vụ tài chính - CHỈ ADMIN)
    // ==========================================================
    public boolean updatePenalty(Penalties penalty) {
        if (!AppSession.isAdmin()) {
            throw new IllegalStateException("Lỗi bảo mật: Chỉ Admin mới được sửa chi tiết khoản phạt!");
        }
        validatePenalty(penalty);

        Penalties existing = penaltyDAO.findById(penalty.getId_penalty());
        if (existing == null) throw new IllegalArgumentException("Không tìm thấy khoản phạt!");

        return penaltyDAO.update(penalty);
    }

    public boolean deletePenalty(int id) {
        if (!AppSession.isAdmin()) {
            throw new IllegalStateException("Lỗi bảo mật: Chỉ Admin mới được xóa khoản phạt để tránh gian lận!");
        }

        Penalties existing = penaltyDAO.findById(id);
        if (existing == null) throw new IllegalArgumentException("Không tìm thấy khoản phạt!");

        return penaltyDAO.delete(id);
    }

    // ==========================================================
    // 4. HÀM KIỂM TRA LOGIC
    // ==========================================================
    private void validatePenalty(Penalties penalty) {
        if (penalty.getId_contract() == null || penalty.getId_contract().getId_contract() <= 0) {
            throw new IllegalArgumentException("Khoản phạt phải được gắn với một hợp đồng!");
        }
        if (penalty.getPenalty_type() == null) {
            throw new IllegalArgumentException("Vui lòng chọn loại vi phạm (Trễ giờ, Hư hỏng, Khác...)!");
        }
        if (penalty.getAmount() < 0) {
            throw new IllegalArgumentException("Số tiền phạt không được là số âm!");
        }
    }
}