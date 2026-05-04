package com.example.rentalcar.controller.voucher;

import com.example.rentalcar.bll.VoucherBLL;
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

public class VoucherFormController implements Initializable {

    @FXML private Label lblTitle, lblSubtitle, lblValueHint;
    @FXML private TextField txtCode, txtDiscountValue, txtUsageLimit;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> cbDiscountType, cbStatus;
    @FXML private DatePicker dpFrom, dpTo;
    @FXML private Label lblPreviewValue, lblMsg;
    @FXML private Button btnSave;

    // SỬA Ở ĐÂY: Dùng BLL thay vì DAO
    private final VoucherBLL voucherBLL = new VoucherBLL();
    private Vouchers editingVoucher = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbDiscountType.getItems().addAll("Cố định (VNĐ)", "Phần trăm (%)");
        cbDiscountType.setValue("Cố định (VNĐ)");

        cbStatus.getItems().addAll("Đang hoạt động", "Đã tắt");
        cbStatus.setValue("Đang hoạt động");

        cbDiscountType.valueProperty().addListener((obs, old, val) -> {
            updateHintLabel(val);
            updatePreview();
        });
        txtDiscountValue.textProperty().addListener((obs, old, val) -> updatePreview());

        dpFrom.setValue(LocalDate.now());
        dpTo.setValue(LocalDate.now().plusMonths(1));
    }

    public void setMode(Vouchers voucher) {
        this.editingVoucher = voucher;

        if (voucher == null) {
            lblTitle.setText("Tạo voucher mới");
            lblSubtitle.setText("Điền thông tin mã giảm giá bên dưới");
            btnSave.setText("💾  Lưu voucher");
        } else {
            lblTitle.setText("Chỉnh sửa voucher");
            lblSubtitle.setText("Cập nhật thông tin voucher: " + voucher.getCode_vouchers());
            btnSave.setText("💾  Cập nhật");

            txtCode.setText(voucher.getCode_vouchers());
            txtCode.setDisable(true);

            cbDiscountType.setValue(voucher.getDiscount_type() == DiscountType.CO_DINH ? "Cố định (VNĐ)" : "Phần trăm (%)");
            txtDiscountValue.setText(String.valueOf((long) voucher.getDiscount_value()));
            txtUsageLimit.setText(String.valueOf(voucher.getUsage_limit()));
            txtDescription.setText(voucher.getDescription() != null ? voucher.getDescription() : "");
            cbStatus.setValue(voucher.isIs_active() ? "Đang hoạt động" : "Đã tắt");

            if (voucher.getValid_from_date() != null) dpFrom.setValue(voucher.getValid_from_date().toLocalDate());
            if (voucher.getValid_to_date() != null) dpTo.setValue(voucher.getValid_to_date().toLocalDate());

            updatePreview();
        }
    }

    @FXML
    void handleSave() {
        try {
            // 1. Controller chỉ làm nhiệm vụ lấy dữ liệu thô từ Giao diện
            String code = txtCode.getText().trim().toUpperCase();
            double discountValue = Double.parseDouble(txtDiscountValue.getText().trim());
            int usageLimit = Integer.parseInt(txtUsageLimit.getText().trim());
            LocalDate fromDate = dpFrom.getValue();
            LocalDate toDate   = dpTo.getValue();
            boolean isActive = "Đang hoạt động".equals(cbStatus.getValue());
            DiscountType dt = cbDiscountType.getValue().contains("Cố định") ? DiscountType.CO_DINH : DiscountType.PHAN_TRAM;

            // Xử lý null ngày tháng trước khi parse sang SQL Date
            if (fromDate == null || toDate == null) {
                showMsg("❌  Vui lòng chọn đầy đủ ngày bắt đầu và kết thúc!", false);
                return;
            }

            // 2. Gom vào Object và quăng cho BLL xử lý phần còn lại
            if (editingVoucher == null) {
                Vouchers newV = new Vouchers(0, code, txtDescription.getText().trim(), dt, discountValue, usageLimit, 0, Date.valueOf(fromDate), Date.valueOf(toDate), isActive);

                voucherBLL.createVoucher(newV); // BLL sẽ tự check logic (âm/dương, ngày tháng)
                showMsg("✅  Tạo voucher thành công!", true);
                autoClose();
            } else {
                editingVoucher.setDescription(txtDescription.getText().trim());
                editingVoucher.setDiscount_type(dt);
                editingVoucher.setDiscount_value(discountValue);
                editingVoucher.setUsage_limit(usageLimit);
                editingVoucher.setValid_from_date(Date.valueOf(fromDate));
                editingVoucher.setValid_to_date(Date.valueOf(toDate));
                editingVoucher.setIs_active(isActive);

                voucherBLL.updateVoucher(editingVoucher); // BLL sẽ tự check logic
                showMsg("✅  Cập nhật voucher thành công!", true);
                autoClose();
            }

        } catch (NumberFormatException e) {
            // Bắt lỗi rỗng hoặc nhập chữ vào ô số
            showMsg("❌  Giá trị giảm giá và Giới hạn phải là số hợp lệ!", false);
        } catch (IllegalArgumentException e) {
            // Bắt lỗi nghiệp vụ từ BLL ném ra (VD: Vượt quá 100%, mã trùng...)
            showMsg("❌  " + e.getMessage(), false);
        }
    }

    @FXML void handleClose() { getStage().close(); }

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
            if (cbDiscountType.getValue().contains("Cố định")) {
                lblPreviewValue.setText(String.format("%,.0f đ", val).replace(",", "."));
            } else {
                lblPreviewValue.setText((int) val + " %");
            }
        } catch (NumberFormatException e) {
            lblPreviewValue.setText("❌ Sai định dạng");
        }
    }

    private void showMsg(String text, boolean success) {
        lblMsg.setText(text);
        lblMsg.setStyle(success ? "-fx-text-fill: #16a34a;" : "-fx-text-fill: #e11d48;");
        lblMsg.setVisible(true);
        Timeline hide = new Timeline(new KeyFrame(Duration.seconds(3), e -> lblMsg.setVisible(false)));
        hide.play();
    }

    private void autoClose() {
        Timeline tl = new Timeline(new KeyFrame(Duration.seconds(1.2), e -> getStage().close()));
        tl.play();
    }

    private Stage getStage() { return (Stage) btnSave.getScene().getWindow(); }
}