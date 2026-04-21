package com.example.rentalcar.controller.voucher;

import com.example.rentalcar.dao.VoucherDAO;
import com.example.rentalcar.models.DiscountType;
import com.example.rentalcar.models.Vouchers;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Controller form Thêm / Sửa Voucher.
 * Gọi setMode(null) để thêm mới, setMode(voucher) để sửa.
 */
public class VoucherFormController implements Initializable {

    @FXML private Label lblTitle, lblSubtitle, lblValueHint;
    @FXML private TextField txtCode, txtDiscountValue, txtUsageLimit;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> cbDiscountType, cbStatus;
    @FXML private DatePicker dpFrom, dpTo;
    @FXML private Label lblPreviewValue, lblMsg;
    @FXML private Button btnSave;

    private final VoucherDAO voucherDAO = new VoucherDAO();
    private Vouchers editingVoucher = null; // null = ADD mode

    // ======================================================
    //  INITIALIZE
    // ======================================================
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbDiscountType.getItems().addAll("Cố định (VNĐ)", "Phần trăm (%)");
        cbDiscountType.setValue("Cố định (VNĐ)");

        cbStatus.getItems().addAll("Đang hoạt động", "Đã tắt");
        cbStatus.setValue("Đang hoạt động");

        // Listener: cập nhật label gợi ý + preview khi thay đổi loại
        cbDiscountType.valueProperty().addListener((obs, old, val) -> {
            updateHintLabel(val);
            updatePreview();
        });
        txtDiscountValue.textProperty().addListener((obs, old, val) -> updatePreview());

        // Ngày mặc định
        dpFrom.setValue(LocalDate.now());
        dpTo.setValue(LocalDate.now().plusMonths(1));
    }

    // ======================================================
    //  SET MODE (ADD or EDIT)
    // ======================================================
    public void setMode(Vouchers voucher) {
        this.editingVoucher = voucher;

        if (voucher == null) {
            // ADD mode
            lblTitle.setText("Tạo voucher mới");
            lblSubtitle.setText("Điền thông tin mã giảm giá bên dưới");
            btnSave.setText("💾  Lưu voucher");
        } else {
            // EDIT mode
            lblTitle.setText("Chỉnh sửa voucher");
            lblSubtitle.setText("Cập nhật thông tin voucher: " + voucher.getCode_vouchers());
            btnSave.setText("💾  Cập nhật");

            // Fill data
            txtCode.setText(voucher.getCode_vouchers());
            txtCode.setDisable(true); // Không cho đổi mã

            if (voucher.getDiscount_type() == DiscountType.CO_DINH) {
                cbDiscountType.setValue("Cố định (VNĐ)");
            } else {
                cbDiscountType.setValue("Phần trăm (%)");
            }

            txtDiscountValue.setText(String.valueOf((long) voucher.getDiscount_value()));
            txtUsageLimit.setText(String.valueOf(voucher.getUsage_limit()));
            txtDescription.setText(voucher.getDescription() != null ? voucher.getDescription() : "");
            cbStatus.setValue(voucher.isIs_active() ? "Đang hoạt động" : "Đã tắt");

            if (voucher.getValid_from_date() != null)
                dpFrom.setValue(voucher.getValid_from_date().toLocalDate());
            if (voucher.getValid_to_date() != null)
                dpTo.setValue(voucher.getValid_to_date().toLocalDate());

            updatePreview();
        }
    }

    // ======================================================
    //  SAVE
    // ======================================================
    @FXML
    void handleSave() {
        // ===== VALIDATION =====
        String code = txtCode.getText().trim().toUpperCase();
        if (code.isEmpty()) {
            showMsg("❌  Vui lòng nhập mã voucher!", false);
            return;
        }
        if (cbDiscountType.getValue() == null) {
            showMsg("❌  Vui lòng chọn loại giảm giá!", false);
            return;
        }

        double discountValue;
        try {
            discountValue = Double.parseDouble(txtDiscountValue.getText().trim());
            if (discountValue <= 0) throw new NumberFormatException();
            if (cbDiscountType.getValue().contains("%") && discountValue > 100) {
                showMsg("❌  Giảm giá theo % không được vượt quá 100%!", false);
                return;
            }
        } catch (NumberFormatException e) {
            showMsg("❌  Giá trị giảm giá không hợp lệ (phải là số dương)!", false);
            return;
        }

        int usageLimit;
        try {
            usageLimit = Integer.parseInt(txtUsageLimit.getText().trim());
            if (usageLimit <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showMsg("❌  Giới hạn lượt dùng không hợp lệ (phải là số nguyên dương)!", false);
            return;
        }

        LocalDate fromDate = dpFrom.getValue();
        LocalDate toDate   = dpTo.getValue();
        if (fromDate == null || toDate == null) {
            showMsg("❌  Vui lòng chọn đầy đủ ngày bắt đầu và kết thúc!", false);
            return;
        }
        if (!toDate.isAfter(fromDate)) {
            showMsg("❌  Ngày kết thúc phải sau ngày bắt đầu!", false);
            return;
        }

        // ===== BUILD OBJECT =====
        boolean isActive = "Đang hoạt động".equals(cbStatus.getValue());
        DiscountType dt = cbDiscountType.getValue().contains("Cố định")
                ? DiscountType.CO_DINH : DiscountType.PHAN_TRAM;

        if (editingVoucher == null) {
            // === ADD ===
            Vouchers newV = new Vouchers(
                    0, code,
                    txtDescription.getText().trim(),
                    dt, discountValue,
                    usageLimit, 0,
                    Date.valueOf(fromDate),
                    Date.valueOf(toDate),
                    isActive
            );
            boolean ok = voucherDAO.insert(newV);
            if (ok) {
                showMsg("✅  Tạo voucher thành công!", true);
                autoClose();
            } else {
                showMsg("❌  Lưu thất bại! Mã voucher có thể đã tồn tại.", false);
            }

        } else {
            // === EDIT ===
            editingVoucher.setDescription(txtDescription.getText().trim());
            editingVoucher.setDiscount_type(dt);
            editingVoucher.setDiscount_value(discountValue);
            editingVoucher.setUsage_limit(usageLimit);
            editingVoucher.setValid_from_date(Date.valueOf(fromDate));
            editingVoucher.setValid_to_date(Date.valueOf(toDate));
            editingVoucher.setIs_active(isActive);

            boolean ok = voucherDAO.update(editingVoucher);
            if (ok) {
                showMsg("✅  Cập nhật voucher thành công!", true);
                autoClose();
            } else {
                showMsg("❌  Cập nhật thất bại, vui lòng thử lại!", false);
            }
        }
    }

    // ======================================================
    //  CLOSE
    // ======================================================
    @FXML
    void handleClose() {
        getStage().close();
    }

    // ======================================================
    //  HELPERS
    // ======================================================
    private void updateHintLabel(String typeVal) {
        if (typeVal == null) return;
        if (typeVal.contains("Cố định")) {
            lblValueHint.setText("Số tiền giảm (VNĐ) *");
            txtDiscountValue.setPromptText("VD: 50000");
        } else {
            lblValueHint.setText("Phần trăm giảm (%) *");
            txtDiscountValue.setPromptText("VD: 10 (tối đa 100)");
        }
    }

    private void updatePreview() {
        String text = txtDiscountValue.getText().trim();
        if (text.isEmpty()) {
            lblPreviewValue.setText("--");
            return;
        }
        try {
            double val = Double.parseDouble(text.replace(".", "").replace(",", ""));
            String typeVal = cbDiscountType.getValue();

            if (typeVal.contains("Cố định")) {
                lblPreviewValue.setText(String.format("%,.0f đ", val).replace(",", "."));
            } else {
                lblPreviewValue.setText((int) val + " %");
            }
        } catch (NumberFormatException e) {
            lblPreviewValue.setText("❌ Sai định dạng");
        }
    }

    /** Hiển thị thông báo inline, tự ẩn sau 3s nếu thành công */
    private void showMsg(String text, boolean success) {
        lblMsg.setText(text);
        lblMsg.setStyle(success ? "-fx-text-fill: #16a34a;" : "-fx-text-fill: #e11d48;");
        lblMsg.setVisible(true);

        // Tự động ẩn sau 3 giây nếu là lỗi, 1.2s nếu thành công (để kịp đóng form)
        Timeline hide = new Timeline(new KeyFrame(Duration.seconds(3), e -> lblMsg.setVisible(false)));
        hide.play();
    }

    /** Đóng stage sau 1.2 giây (cho user thấy thông báo thành công) */
    private void autoClose() {
        Timeline tl = new Timeline(new KeyFrame(Duration.seconds(1.2), e -> getStage().close()));
        tl.play();
    }

    private Stage getStage() {
        return (Stage) btnSave.getScene().getWindow();
    }
}