package com.example.rentalcar.controller.contract;

import com.example.rentalcar.dao.CustomerDAO;
import com.example.rentalcar.dao.PenaltyDAO;
import com.example.rentalcar.dao.VehicleDAO;
import com.example.rentalcar.models.*;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;
public class ContractDetailController {

    @FXML private Label label_contract_code;
    @FXML private Label label_customer_name;
    @FXML private Label label_customer_phone;
    @FXML private Label label_vehicle_info;
    @FXML private Label label_rental_period;
    @FXML private Label label_status;
    @FXML private Label label_total_price;
    @FXML private Label label_base_price;
    @FXML private Label label_discount;
    @FXML private Label label_payment_status;
    @FXML private Label label_deposit;

    // Penalty section
    @FXML private VBox  vboxPenalties;
    @FXML private Label lblNoPenalties;
    @FXML private Label lblTotalPenalty;

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final VehicleDAO  vehicleDAO  = new VehicleDAO();
    private final PenaltyDAO  penaltyDAO  = new PenaltyDAO();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void setContractData(Contracts contract) {
        // Mã HĐ
        setLabel(label_contract_code, "#" + contract.getCode_contract());

        // Khách hàng
        if (contract.getId_customer() != null) {
            Customers c = customerDAO.findById(contract.getId_customer().getId_customer());
            if (c != null) {
                setLabel(label_customer_name, c.getFull_name());
                setLabel(label_customer_phone, c.getPhone() != null ? c.getPhone() : "--");
            }
        }

        // Xe
        if (contract.getId_vehicle() != null) {
            Vehicles v = vehicleDAO.findById(contract.getId_vehicle().getId_vehicle());
            if (v != null) {
                setLabel(label_vehicle_info,
                        v.getBrand() + " " + v.getModel() + " (" + v.getCode_vehicle() + ")");
            }
        }

        // Thời gian
        if (contract.getStart_datetime() != null && contract.getEnd_datetime() != null) {
            setLabel(label_rental_period,
                    contract.getStart_datetime().format(FMT)
                            + " → " + contract.getEnd_datetime().format(FMT));
        }

        // Trạng thái HĐ
        if (contract.getStatus() != null) {
            String statusText = switch (contract.getStatus()) {
                case DANG_THUE  -> "Đang thuê";
                case QUA_HAN    -> "Quá hạn";
                case HOAN_THANH -> "Hoàn thành";
                case DA_HUY     -> "Đã hủy";
            };
            setLabel(label_status, statusText);
        }

        // Trạng thái thanh toán
        if (contract.getPayment_status() != null && label_payment_status != null) {
            String payText = switch (contract.getPayment_status()) {
                case CHUA_THANH_TOAN   -> "Chưa thanh toán";
                case THANH_TOAN_1_PHAN -> "Thanh toán một phần";
                case DA_THANH_TOAN     -> "Đã thanh toán đủ";
            };
            label_payment_status.setText(payText);
        }

        // Giá
        setLabel(label_base_price,
                fmt(contract.getBase_price()));
        setLabel(label_discount,
                contract.getDiscount_amount() > 0
                        ? "- " + fmt(contract.getDiscount_amount()) : "0 đ");
        setLabel(label_total_price,
                fmt(contract.getTotal_price()));

        // Đặt cọc
        if (label_deposit != null && contract.getDeposit_type() != null) {
            String depositText = switch (contract.getDeposit_type()) {
                case TIEN_MAT -> "Tiền mặt";
                case GIAY_TO  -> "Giấy tờ";
                default       -> "Khác";
            };
            label_deposit.setText(depositText
                    + (contract.getDeposit_amount() > 0
                    ? " – " + fmt(contract.getDeposit_amount()) : ""));
        }

        // Load penalties
        loadPenalties(contract.getId_contract());
    }

    private void loadPenalties(int contractId) {
        if (vboxPenalties == null) return;
        vboxPenalties.getChildren().clear();

        try {
            List<Penalties> penalties = penaltyDAO.findByContractId(contractId);

            if (penalties == null || penalties.isEmpty()) {
                if (lblNoPenalties != null) {
                    lblNoPenalties.setVisible(true);
                    lblNoPenalties.setManaged(true);
                }
                if (lblTotalPenalty != null)
                    lblTotalPenalty.setText("0 đ");
                return;
            }

            if (lblNoPenalties != null) {
                lblNoPenalties.setVisible(false);
                lblNoPenalties.setManaged(false);
            }

            double total = 0;

            for (Penalties p : penalties) {
                HBox row = new HBox();
                row.setAlignment(Pos.CENTER_LEFT);
                row.setMinHeight(38);
                row.setStyle("-fx-background-color: white; -fx-padding: 6 10;" +
                        "-fx-border-color: transparent transparent #f1f5f9 transparent;" +
                        "-fx-border-width: 1;");

                // Icon + Loại phạt
                String typeText = "--";
                String typeColor = "#64748b";
                String typeIcon = "⚠";
                if (p.getPenalty_type() != null) {
                    switch (p.getPenalty_type()) {
                        case QUA_GIO -> {
                            typeText = "Trả xe trễ giờ";
                            typeColor = "#b45309";
                            typeIcon = "⏰";
                        }
                        case XANG -> {
                            typeText = "Thiếu xăng";
                            typeColor = "#d97706";
                            typeIcon = "⛽";
                        }
                        case HU_HONG -> {
                            typeText = "Hư hỏng phụ tùng";
                            typeColor = "#7c3aed";
                            typeIcon = "🔧";
                        }
                        case VI_PHAM_GIAO_THONG -> {
                            typeText = "Vi phạm giao thông";
                            typeColor = "#dc2626";
                            typeIcon = "🚦";
                        }
                    }
                }

                Label icon = new Label(typeIcon);
                icon.setPrefWidth(28);
                icon.setStyle("-fx-font-size: 14px;");

                Label type = new Label(typeText);
                type.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + typeColor + ";");
                HBox.setHgrow(type, javafx.scene.layout.Priority.ALWAYS);
                type.setMaxWidth(Double.MAX_VALUE);

                Label amount = new Label(fmt(p.getAmount()));
                amount.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;" +
                        "-fx-text-fill: #dc2626;");

                row.getChildren().addAll(icon, type, amount);
                vboxPenalties.getChildren().add(row);
                total += p.getAmount();
            }

            if (lblTotalPenalty != null) {
                lblTotalPenalty.setText(fmt(total));
            }

        } catch (Exception e) {
            System.err.println("Lỗi load penalties: " + e.getMessage());
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) label_contract_code.getScene().getWindow();
        stage.close();
    }

    private void setLabel(Label lbl, String text) {
        if (lbl != null) lbl.setText(text);
    }

    private String fmt(double v) {
        return String.format("%,.0f đ", v).replace(",", ".");
    }
}