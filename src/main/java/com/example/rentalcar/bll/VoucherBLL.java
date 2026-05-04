package com.example.rentalcar.bll;

import com.example.rentalcar.dao.VoucherDAO;
import com.example.rentalcar.models.DiscountType;
import com.example.rentalcar.models.Vouchers;
import com.example.rentalcar.utils.AppSession;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class VoucherBLL {

    private final VoucherDAO voucherDAO = new VoucherDAO();

    // ==========================================================
    // 1. NGHIỆP VỤ LÕI: TÍNH TIỀN GIẢM GIÁ (Dùng cho PriceBLL)
    // ==========================================================
    /**
     * Hàm này cực kỳ quan trọng, dùng lúc khách hàng nhập mã Voucher để chốt hợp đồng.
     * Trả về số tiền được giảm. Nếu mã lỗi, sẽ ném ra Exception để báo cho khách.
     */
    public double calculateDiscountAmount(String code, double totalContractAmount) {
        if (code == null || code.isBlank()) {
            return 0.0;
        }

        // 1. Tìm voucher trong DB (DAO đã check is_active = 1 và valid_to_date >= CURDATE)
        Vouchers voucher = voucherDAO.findByCode(code.trim().toUpperCase());
        if (voucher == null) {
            throw new IllegalArgumentException("Mã giảm giá không tồn tại hoặc đã hết hạn sử dụng!");
        }

        // 2. Kiểm tra ngày bắt đầu (Lỡ khách nhập mã của tháng sau)
        LocalDate today = LocalDate.now();
        if (voucher.getValid_from_date() != null && voucher.getValid_from_date().toLocalDate().isAfter(today)) {
            throw new IllegalArgumentException("Mã giảm giá này chưa đến thời gian áp dụng!");
        }

        // 3. Kiểm tra số lượt sử dụng
        if (voucher.getUsage_count() >= voucher.getUsage_limit()) {
            throw new IllegalArgumentException("Mã giảm giá này đã hết lượt sử dụng!");
        }

        // 4. Tính toán số tiền được giảm
        double discountAmount = 0;
        if (voucher.getDiscount_type() == DiscountType.CO_DINH) {
            discountAmount = voucher.getDiscount_value();
        } else if (voucher.getDiscount_type() == DiscountType.PHAN_TRAM) {
            discountAmount = totalContractAmount * (voucher.getDiscount_value() / 100.0);
        }

        // 5. Đảm bảo tiền giảm không vượt quá tổng tiền hợp đồng (không thối lại tiền thừa)
        return Math.min(discountAmount, totalContractAmount);
    }

    // ==========================================================
    // 2. NGHIỆP VỤ CẬP NHẬT: GHI NHẬN SỬ DỤNG VOUCHER
    // ==========================================================
    /**
     * Gọi hàm này khi Hợp đồng đã thanh toán xong để tăng lượt dùng (usage_count) lên 1.
     */
    public boolean markVoucherAsUsed(String code) {
        if (code == null || code.isBlank()) return false;

        Vouchers voucher = voucherDAO.findByCode(code.trim().toUpperCase());
        if (voucher != null && voucher.getUsage_count() < voucher.getUsage_limit()) {
            voucher.setUsage_count(voucher.getUsage_count() + 1);
            return voucherDAO.update(voucher);
        }
        return false;
    }

    // ==========================================================
    // 3. CÁC HÀM CRUD (Bọc lại DAO kèm Validate logic)
    // ==========================================================

    public List<Vouchers> getAllVouchers() {
        return voucherDAO.findAll();
    }

    // ==========================================================
    // NHÓM HÀM THAY ĐỔI DỮ LIỆU (CHỈ ADMIN)
    // ==========================================================

    public boolean createVoucher(Vouchers voucher) {
        if (!AppSession.isAdmin()) throw new IllegalStateException("Lỗi bảo mật: Chỉ Admin mới được tạo mã giảm giá!");
        validateVoucherData(voucher);

        // Kiểm tra trùng mã
        Vouchers existing = voucherDAO.findByCode(voucher.getCode_vouchers());
        if (existing != null) {
            throw new IllegalArgumentException("Mã voucher '" + voucher.getCode_vouchers() + "' đã tồn tại đang hoạt động!");
        }

        return voucherDAO.insert(voucher);
    }

    public boolean updateVoucher(Vouchers voucher) {
        if (!AppSession.isAdmin()) throw new IllegalStateException("Lỗi bảo mật: Chỉ Admin mới được sửa mã giảm giá!");
        validateVoucherData(voucher);
        return voucherDAO.update(voucher);
    }

    public boolean toggleVoucherStatus(int id, boolean newStatus) {
        if (!AppSession.isAdmin()) throw new IllegalStateException("Lỗi bảo mật: Bạn không có quyền bật/tắt voucher!");

        Vouchers v = voucherDAO.findById(id);
        if (v == null) {
            throw new IllegalArgumentException("Không tìm thấy Voucher!");
        }

        v.setIs_active(newStatus);
        return voucherDAO.update(v);
    }

    // ==========================================================
    // 4. HÀM KIỂM TRA LOGIC CHUNG
    // ==========================================================
    private void validateVoucherData(Vouchers v) {
        if (v.getCode_vouchers() == null || v.getCode_vouchers().isBlank()) {
            throw new IllegalArgumentException("Mã voucher không được để trống!");
        }
        if (v.getDiscount_value() <= 0) {
            throw new IllegalArgumentException("Giá trị giảm giá phải lớn hơn 0!");
        }
        if (v.getDiscount_type() == DiscountType.PHAN_TRAM && v.getDiscount_value() > 100) {
            throw new IllegalArgumentException("Giảm giá phần trăm không được vượt quá 100%!");
        }
        if (v.getUsage_limit() <= 0) {
            throw new IllegalArgumentException("Giới hạn sử dụng phải lớn hơn 0!");
        }
        if (v.getValid_from_date() == null || v.getValid_to_date() == null) {
            throw new IllegalArgumentException("Phải chọn đầy đủ ngày bắt đầu và kết thúc!");
        }
        if (v.getValid_to_date().before(v.getValid_from_date())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu!");
        }
    }
}