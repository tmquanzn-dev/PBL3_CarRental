package com.example.rentalcar.controller;

import com.example.rentalcar.utils.AppSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    // ── Layout ────────────────────────────────────────────────
    @FXML private VBox      sideBar;
    @FXML private StackPane contentArea;

    // ── Topbar ────────────────────────────────────────────────
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblAvatarInitial;

    // ── Menu buttons ─────────────────────────────────────────
    @FXML private Button btnDashboard;   // tất cả
    @FXML private Button btnContract;    // tất cả
    @FXML private Button btnVehicle;     // tất cả
    @FXML private Button btnCustomer;    // tất cả
    @FXML private Button btnVoucher;     // tất cả
    @FXML private Button btnEmployee;    // Admin only
    @FXML private Button btnRule;        // Admin only
    @FXML private Button btnPartPrice;   // Admin only
    @FXML private Button btnReport;      // Admin only
    @FXML private Button btnSettings;    // tất cả
    @FXML private Button btnStaffReport; //Staff

    // ─────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        loadUserInfoToTopbar();
        applyRoleMenu();

        changeView("DashboardView.fxml");
        sideBar.setVisible(false);
        if (btnDashboard != null) setActiveMenu(btnDashboard);
    }

    // Hiển thị tên + role lên topbar
    private void loadUserInfoToTopbar() {
        if (AppSession.getCurrentUser() == null) return;
        String name = AppSession.getCurrentUser().getFull_name();
        String role = AppSession.getRoleDisplayName();

        if (lblUserName     != null) lblUserName.setText(name != null ? name : "User");
        if (lblUserRole     != null) lblUserRole.setText(role);
        if (lblAvatarInitial != null && name != null && !name.isBlank())
            lblAvatarInitial.setText(name.substring(0, 1).toUpperCase());
    }

    // ── Ẩn/hiện menu theo role ────────────────────────────────
    private void applyRoleMenu() {
        boolean admin = AppSession.isAdmin();
        boolean staff = AppSession.isStaff();

        // Chỉ Admin thấy các mục này
        setVisible(btnEmployee, admin);
        setVisible(btnRule,     admin);
        setVisible(btnPartPrice,admin);
        setVisible(btnReport,   admin);   // Báo cáo tổng - Admin only

        // Staff thấy báo cáo cá nhân, Admin KHÔNG thấy (vì Admin đã có báo cáo tổng)
        setVisible(btnStaffReport, staff);
    }

    private void setVisible(Button btn, boolean show) {
        if (btn == null) return;
        btn.setVisible(show);
        btn.setManaged(show);   // khi ẩn không chiếm chỗ trong VBox
    }

    // ── Hamburger ─────────────────────────────────────────────
    @FXML
    void handleHamburgerMenu(ActionEvent event) {
        sideBar.setVisible(!sideBar.isVisible());
    }

    // ── Click menu ────────────────────────────────────────────
    @FXML
    void handleMenuClick(ActionEvent e) {
        Button src = (Button) e.getSource();
        setActiveMenu(src);
        sideBar.setVisible(false);

        String fxml = switch (src.getText().trim()) {
            case "Tổng quan"        -> "DashboardView.fxml";
            case "Quản lý hóa đơn" -> "ContractManagement.fxml";
            case "Quản lý xe"      -> "vehicle/VehicleManagement.fxml";
            case "Khách hàng"      -> "customer/CustomerManagement.fxml";
            case "Voucher"         -> "voucher/VoucherManagement.fxml";
            case "Nhân viên"       -> AppSession.isAdmin() ? "employee/EmployeeManagement.fxml"   : "";
            case "Luật tính giá"   -> AppSession.isAdmin() ? "rule/RuleManagement.fxml"           : "";
            case "Phụ tùng"        -> AppSession.isAdmin() ? "partprice/PartPriceManagement.fxml" : "";
            case "Báo cáo"         -> AppSession.isAdmin() ? "report/ReportView.fxml"             : "";
            case "Báo cáo của tôi"    -> "report/StaffReportView.fxml";
            case "Cài đặt"         -> "setting/SettingsMain.fxml";
            default                -> "";
        };

        if (!fxml.isEmpty()) changeView(fxml);
    }

    // ── Active highlight ──────────────────────────────────────
    private void setActiveMenu(Button active) {
        sideBar.getChildren().stream()
                .filter(n -> n instanceof Button)
                .forEach(n -> n.getStyleClass().remove("active_menu"));
        if (active != null) active.getStyleClass().add("active_menu");
    }

    // ── Load view ─────────────────────────────────────────────
    private void changeView(String fxmlFile) {
        try {
            Parent view = FXMLLoader.load(
                    getClass().getResource("/views/" + fxmlFile));
            contentArea.getChildren().setAll(view);
        } catch (IOException ex) {
            System.err.println("Không tìm thấy file: " + fxmlFile);
            ex.printStackTrace();
        }
    }

    // ── Logout ───────────────────────────────────────────────
    @FXML
    void handleLogout(ActionEvent e) {
        try {
            AppSession.clearSession();
            Parent root = FXMLLoader.load(
                    getClass().getResource("/views/LoginView.fxml"));
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}