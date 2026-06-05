package com.example.rentalcar.bll;

import com.example.rentalcar.dao.PaymentDAO;
import com.example.rentalcar.models.Payments;
import com.example.rentalcar.utils.AppSession;

import java.util.List;

public class PaymentBLL {

    private final PaymentDAO paymentDAO = new PaymentDAO();

    public List<Payments> getAllPayments() {
        return paymentDAO.findAll();
    }

    public Payments getPaymentById(int id) {
        return paymentDAO.findById(id);
    }

    // ✅ BỔ SUNG: Trung gian gọi từ Controller lấy danh sách lịch sử dòng tiền
    public List<Payments> getPaymentsByContractId(int contractId) {
        return paymentDAO.findByContractId(contractId);
    }

    // ==========================================================
    // THÊM MỚI GIAO DỊCH (Staff thu tiền)
    // ==========================================================
    public boolean createPayment(Payments payment) {
        validatePayment(payment);

        // NGHIỆP VỤ: Tự động gán người thu tiền là người đang đăng nhập hệ thống thông qua AppSession
        if (AppSession.getCurrentUser() != null) {
            payment.setId_user(AppSession.getCurrentUser());
        } else {
            throw new IllegalStateException("Lỗi bảo mật: Không xác định được phiên đăng nhập!");
        }

        return paymentDAO.insert(payment);
    }

    // ==========================================================
    // SỬA / XÓA LỊCH SỬ DÒNG TIỀN (CHỈ ADMIN)
    // ==========================================================
    public boolean updatePayment(Payments payment) {
        if (!AppSession.isAdmin()) {
            throw new IllegalStateException("Lỗi bảo mật: Chỉ Admin mới được can thiệp sửa lịch sử thanh toán!");
        }
        validatePayment(payment);
        return paymentDAO.update(payment);
    }

    public boolean deletePayment(int id) {
        if (!AppSession.isAdmin()) {
            throw new IllegalStateException("Lỗi bảo mật: Chỉ Admin mới được xóa lịch sử giao dịch!");
        }
        return paymentDAO.delete(id);
    }

    // ==========================================================
    // LOGIC VALIDATE
    // ==========================================================
    private void validatePayment(Payments payment) {
        if (payment.getId_contract() == null || payment.getId_contract().getId_contract() <= 0) {
            throw new IllegalArgumentException("Giao dịch thanh toán phải thuộc về một hợp đồng!");
        }
        if (payment.getAmount() <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0!");
        }
        if (payment.getPayment_method() == null) {
            throw new IllegalArgumentException("Vui lòng chọn phương thức thanh toán (Tiền mặt, Chuyển khoản...)!");
        }
        if (payment.getPayment_type() == null) {
            throw new IllegalArgumentException("Vui lòng chọn loại thanh toán (Đặt cọc, Thanh toán cuối...)!");
        }
    }
}