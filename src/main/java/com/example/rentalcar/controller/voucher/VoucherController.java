package com.example.rentalcar.controller.voucher;

import com.example.rentalcar.bll.VoucherBLL;
import com.example.rentalcar.models.DiscountType;
import com.example.rentalcar.models.Vouchers;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class VoucherController implements Initializable {

    @FXML private Label lblTotalVouchers, lblActiveVouchers;
    @FXML private Label lblExpiringVouchers, lblExpiredVouchers;
    @FXML private Label lblCount;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbTypeFilter, cbStatusFilter;

    @FXML private TableView<Vouchers> tableVouchers;
    @FXML private TableColumn<Vouchers, String> colCode;
    @FXML private TableColumn<Vouchers, String> colDesc;
    @FXML private TableColumn<Vouchers, String> colType;
    @FXML private TableColumn<Vouchers, String> colValue;
    @FXML private TableColumn<Vouchers, String> colUsage;
    @FXML private TableColumn<Vouchers, String> colDate;
    @FXML private TableColumn<Vouchers, String> colStatus;
    @FXML private TableColumn<Vouchers, Void>   colAction;

    private final VoucherBLL voucherBLL = new VoucherBLL();
    private ObservableList<Vouchers> masterList;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupFilters();
        setupTableColumns();
        loadData();
    }


    private void setupFilters() {
        cbTypeFilter.getItems().addAll("Tất cả loại", "Cố định (VNĐ)", "Phần trăm (%)");
        cbTypeFilter.setValue("Tất cả loại");

        cbStatusFilter.getItems().addAll("Tất cả trạng thái", "Đang hoạt động", "Hết hạn / Tắt");
        cbStatusFilter.setValue("Tất cả trạng thái");
    }


    private void setupTableColumns() {

        // Mã voucher
        colCode.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getCode_vouchers()));
        colCode.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String code, boolean empty) {
                super.updateItem(code, empty);
                if (empty || code == null) { setGraphic(null); return; }
                Label lbl = new Label(code);
                lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #146dff; -fx-font-size: 14px;");
                setGraphic(lbl);
            }
        });

        // Mô tả
        colDesc.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getDescription() != null
                                ? cell.getValue().getDescription() : "--"));
        colDesc.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String desc, boolean empty) {
                super.updateItem(desc, empty);
                if (empty || desc == null) { setGraphic(null); return; }
                Label lbl = new Label(desc);
                lbl.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");
                lbl.setWrapText(true);
                setGraphic(lbl);
            }
        });

        // Loại giảm giá
        colType.setCellValueFactory(cell -> {
            DiscountType dt = cell.getValue().getDiscount_type();
            if (dt == null) return new SimpleStringProperty("--");
            return new SimpleStringProperty(dt == DiscountType.CO_DINH ? "CO_DINH" : "PHAN_TRAM");
        });
        colType.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null || type.equals("--")) { setGraphic(null); return; }
                Label badge = new Label(type.equals("CO_DINH") ? "Cố định" : "Phần trăm");
                badge.getStyleClass().add(type.equals("CO_DINH") ? "badge-fixed" : "badge-percent");
                HBox box = new HBox(badge);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });

        // Giá trị giảm
        colValue.setCellValueFactory(cell -> {
            Vouchers v = cell.getValue();
            if (v.getDiscount_type() == null) return new SimpleStringProperty("--");
            if (v.getDiscount_type() == DiscountType.CO_DINH) {
                return new SimpleStringProperty(
                        String.format("%,.0f đ", v.getDiscount_value()).replace(",", "."));
            } else {
                return new SimpleStringProperty((int) v.getDiscount_value() + " %");
            }
        });
        colValue.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); return; }
                Label lbl = new Label(val);
                lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #e11d48; -fx-font-size: 14px;");
                setGraphic(lbl);
            }
        });

        // Đã dùng / Giới hạn (dạng progress)
        colUsage.setCellValueFactory(cell -> {
            Vouchers v = cell.getValue();
            return new SimpleStringProperty(v.getUsage_count() + " / " + v.getUsage_limit());
        });
        colUsage.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String usage, boolean empty) {
                super.updateItem(usage, empty);
                if (empty || usage == null || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                Vouchers v = (Vouchers) getTableRow().getItem();
                int used  = v.getUsage_count();
                int limit = v.getUsage_limit();
                double ratio = limit > 0 ? (double) used / limit : 0;

                Label lblText = new Label(used + " / " + limit);
                lblText.setStyle("-fx-font-size: 13px; -fx-text-fill: #1e293b;");

                javafx.scene.layout.AnchorPane track = new javafx.scene.layout.AnchorPane();
                track.setPrefHeight(5);
                track.setPrefWidth(80);
                track.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 3;");

                javafx.scene.layout.AnchorPane fill = new javafx.scene.layout.AnchorPane();
                fill.setPrefHeight(5);
                fill.setPrefWidth(80 * Math.min(ratio, 1.0));
                String fillColor = ratio >= 1.0 ? "#ef4444" : ratio >= 0.8 ? "#f59e0b" : "#146dff";
                fill.setStyle("-fx-background-color: " + fillColor + "; -fx-background-radius: 3;");
                track.getChildren().add(fill);

                VBox box = new VBox(3, lblText, track);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });

        // Thời hạn
        colDate.setCellValueFactory(cell -> {
            Vouchers v = cell.getValue();
            String from = formatDate(v.getValid_from_date());
            String to   = formatDate(v.getValid_to_date());
            return new SimpleStringProperty(from + "\n→ " + to);
        });
        colDate.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String period, boolean empty) {
                super.updateItem(period, empty);
                if (empty || period == null) { setGraphic(null); return; }
                String[] parts = period.split("\n");
                Label from = new Label(parts.length > 0 ? parts[0] : "");
                from.setStyle("-fx-font-size: 12px; -fx-text-fill: #1e293b;");
                Label to = new Label(parts.length > 1 ? parts[1] : "");
                to.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
                VBox vb = new VBox(2, from, to);
                vb.setAlignment(Pos.CENTER_LEFT);
                setGraphic(vb);
            }
        });

        // Trạng thái
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                Vouchers v = (Vouchers) getTableRow().getItem();
                Label pill = new Label();
                pill.getStyleClass().add("status-pill");

                boolean expired = v.getValid_to_date() != null
                        && v.getValid_to_date().toLocalDate().isBefore(LocalDate.now());
                boolean outOfUse = v.getUsage_count() >= v.getUsage_limit();

                if (!v.isIs_active() || expired || outOfUse) {
                    pill.setText(expired ? "Hết hạn" : !v.isIs_active() ? "Đã tắt" : "Hết lượt");
                    pill.getStyleClass().add("status-expired");
                } else {
                    pill.setText("Hoạt động");
                    pill.getStyleClass().add("status-active");
                }
                HBox box = new HBox(pill);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });

        // Action buttons
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit   = new Button();
            private final Button btnToggle = new Button();
            private final HBox   pane      = new HBox(6, btnEdit, btnToggle);

            {
                pane.setAlignment(Pos.CENTER_LEFT);
                btnEdit.getStyleClass().add("btn-action");
                btnToggle.getStyleClass().add("btn-action");

                loadIcon(btnEdit, "/icon/dashboardform/edit.png");
                loadIcon(btnToggle, "/icon/dashboardform/delete.png");

                btnEdit.setOnAction(e -> {
                    Vouchers v = getTableView().getItems().get(getIndex());
                    openForm(v);
                });
                btnToggle.setOnAction(e -> {
                    Vouchers v = getTableView().getItems().get(getIndex());
                    confirmToggle(v);
                });
            }

            private void loadIcon(Button btn, String path) {
                try {
                    URL url = getClass().getResource(path);
                    if (url != null) {
                        ImageView iv = new ImageView(new Image(url.toExternalForm()));
                        iv.setFitHeight(17); iv.setFitWidth(17);
                        btn.setGraphic(iv);
                    }
                } catch (Exception ignored) {}
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Vouchers v = getTableView().getItems().get(getIndex());
                // Tooltip cho nút toggle
                btnToggle.setTooltip(new Tooltip(v.isIs_active() ? "Tắt voucher" : "Bật voucher"));
                setGraphic(pane);
            }
        });
    }


    private void loadData() {
        try {
            List<Vouchers> list = voucherBLL.getAllVouchers();
            masterList = FXCollections.observableArrayList(list);

            tableVouchers.setItems(masterList);
            tableVouchers.setFixedCellSize(70.0);
            lblCount.setText(masterList.size() + " voucher");

            updateStats(list);
        } catch (Exception e) {
            showError("Lỗi tải dữ liệu: " + e.getMessage());
        }
    }

    private void updateStats(List<Vouchers> list) {
        lblTotalVouchers.setText(String.valueOf(list.size()));

        LocalDate today = LocalDate.now();
        LocalDate soon  = today.plusDays(7);

        long active = list.stream().filter(v -> v.isIs_active()
                && v.getValid_to_date() != null
                && !v.getValid_to_date().toLocalDate().isBefore(today)
                && v.getUsage_count() < v.getUsage_limit()).count();

        long expiring = list.stream().filter(v -> v.isIs_active()
                && v.getValid_to_date() != null
                && !v.getValid_to_date().toLocalDate().isBefore(today)
                && !v.getValid_to_date().toLocalDate().isAfter(soon)).count();

        long expired  = list.stream().filter(v ->
                !v.isIs_active()
                        || (v.getValid_to_date() != null && v.getValid_to_date().toLocalDate().isBefore(today))
                        || v.getUsage_count() >= v.getUsage_limit()).count();

        lblActiveVouchers.setText(String.valueOf(active));
        lblExpiringVouchers.setText(String.valueOf(expiring));
        lblExpiredVouchers.setText(String.valueOf(expired));
    }

    //search
    @FXML
    void handleSearch(ActionEvent event) {
        if (masterList == null) return;
        String keyword       = txtSearch.getText().trim().toLowerCase();
        String typeVal       = cbTypeFilter.getValue();
        String statusVal     = cbStatusFilter.getValue();
        LocalDate today      = LocalDate.now();

        ObservableList<Vouchers> filtered = masterList.filtered(v -> {
            // Key
            boolean matchKey = keyword.isEmpty()
                    || v.getCode_vouchers().toLowerCase().contains(keyword)
                    || (v.getDescription() != null && v.getDescription().toLowerCase().contains(keyword));

            // Type
            boolean matchType = typeVal == null || typeVal.equals("Tất cả loại")
                    || (typeVal.equals("Cố định (VNĐ)") && v.getDiscount_type() == DiscountType.CO_DINH)
                    || (typeVal.equals("Phần trăm (%)") && v.getDiscount_type() == DiscountType.PHAN_TRAM);

            // Status
            boolean active = v.isIs_active()
                    && v.getValid_to_date() != null
                    && !v.getValid_to_date().toLocalDate().isBefore(today)
                    && v.getUsage_count() < v.getUsage_limit();
            boolean matchStatus = statusVal == null || statusVal.equals("Tất cả trạng thái")
                    || (statusVal.equals("Đang hoạt động") && active)
                    || (statusVal.equals("Hết hạn / Tắt") && !active);

            return matchKey && matchType && matchStatus;
        });

        tableVouchers.setItems(filtered);
        lblCount.setText(filtered.size() + " voucher");
    }

    @FXML
    void handleReload(ActionEvent event) {
        txtSearch.clear();
        cbTypeFilter.setValue("Tất cả loại");
        cbStatusFilter.setValue("Tất cả trạng thái");
        loadData();
    }

    //Thêm voucher
    @FXML
    void handleAddVoucher(ActionEvent event) {
        openForm(null);
    }

    private void openForm(Vouchers voucher) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/voucher/VoucherFormModal.fxml"));
            Parent root = loader.load();

            VoucherFormController ctrl = loader.getController();
            ctrl.setMode(voucher); // null = add, object = edit

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(voucher == null ? "Tạo voucher mới" : "Sửa: " + voucher.getCode_vouchers());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.setOnHidden(e -> loadData());
            stage.show();
        } catch (Exception e) {
            showError("Lỗi mở form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // bật hoặc tắt voucher
    private void confirmToggle(Vouchers v) {
        String action = v.isIs_active() ? "tắt" : "bật lại";
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có muốn " + action + " voucher \"" + v.getCode_vouchers() + "\" không?");

        confirm.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            try {
                // SỬA Ở ĐÂY: Nhờ BLL xử lý việc cập nhật trạng thái
                voucherBLL.toggleVoucherStatus(v.getId_voucher(), !v.isIs_active());
                loadData();
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            }
        });
    }

    // ======================================================
    //  HELPERS
    // ======================================================
    private String formatDate(Date date) {
        if (date == null) return "--";
        return date.toLocalDate().format(DATE_FMT);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}