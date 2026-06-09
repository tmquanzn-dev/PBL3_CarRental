package com.example.rentalcar.controller.settings;

import com.example.rentalcar.utils.AppSession;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    @FXML private Button btnNavProfile, btnNavSecurity, btnNavGeneral, btnNavPricing, btnNavNotification, btnNavDatabase, btnNavAbout;

    @FXML private ScrollPane panelProfile;
    @FXML private ProfilePanelController panelProfileController;
    @FXML private ScrollPane panelSecurity;
    @FXML private SecurityPanelController panelSecurityController;
    @FXML private ScrollPane panelGeneral;
    @FXML private GeneralPanelController panelGeneralController;
    @FXML private ScrollPane panelPricing;
    @FXML private PricingPanelController panelPricingController;
    @FXML private ScrollPane panelNotification;
    @FXML private NotificationPanelController panelNotificationController;
    @FXML private ScrollPane panelDatabase;
    @FXML private DatabasePanelController panelDatabaseController;
    @FXML private ScrollPane panelAbout;
    @FXML private AboutPanelController panelAboutController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        applyRoleRestrictions();

        if (panelProfileController != null)
            panelProfileController.initData();
        if (panelSecurityController != null)
            panelSecurityController.initSecurity();
        if (panelGeneralController != null)
            panelGeneralController.initGeneral();
        if (panelPricingController != null)
            panelPricingController.initPricing();
        if (panelDatabaseController != null)
            panelDatabaseController.initDatabase();
        if (panelAboutController != null)
            panelAboutController.initAbout();

        showPanel(panelProfile);
    }

    private void applyRoleRestrictions() {
        boolean admin = AppSession.isAdmin();
        setNavVisible(btnNavGeneral, admin);
        setNavVisible(btnNavPricing, admin);
        setNavVisible(btnNavNotification, admin);
        setNavVisible(btnNavDatabase, admin);
        setNavVisible(btnNavAbout, admin);
    }

    private void setNavVisible(Button btn, boolean show) {
        if (btn != null) { btn.setVisible(show); btn.setManaged(show); }
    }

    @FXML void handleNavProfile() {
        switchNav(btnNavProfile);
        showPanel(panelProfile);
    }

    @FXML void handleNavSecurity() {
        switchNav(btnNavSecurity);
        showPanel(panelSecurity);
    }

    @FXML void handleNavGeneral() {
        if (AppSession.isAdmin()) {
            switchNav(btnNavGeneral);
            showPanel(panelGeneral); }
    }

    @FXML void handleNavNotification() {
        if (AppSession.isAdmin())
        {
            switchNav(btnNavNotification);
            showPanel(panelNotification);
        }

    }
    @FXML void handleNavDatabase() {
        if (AppSession.isAdmin())
        {
            switchNav(btnNavDatabase);
            showPanel(panelDatabase);
        }

    }
    @FXML void handleNavAbout() {
        if (AppSession.isAdmin()) {
            switchNav(btnNavAbout);
            showPanel(panelAbout); }
    }

    @FXML void handleNavPricing() {
        if (!AppSession.isAdmin()) return;
        switchNav(btnNavPricing);
        showPanel(panelPricing);
        panelPricingController.loadPricingFromDB();
    }

    private void showPanel(ScrollPane target) {
        ScrollPane[] all = {
                panelProfile, panelSecurity, panelGeneral, panelPricing, panelNotification, panelDatabase, panelAbout };
        for (ScrollPane p : all)
            if (p != null)
                p.setVisible(p == target);
    }

    private void switchNav(Button activeBtn) {
        Button[] navBtns = { btnNavProfile, btnNavSecurity, btnNavGeneral, btnNavPricing, btnNavNotification, btnNavDatabase, btnNavAbout };
        for (Button btn : navBtns) {
            if (btn == null) continue;
            btn.getStyleClass().removeAll("settings-nav-active");
            btn.getStyleClass().add("settings-nav-btn");
        }
        if (activeBtn != null) activeBtn.getStyleClass().add("settings-nav-active");
    }
}