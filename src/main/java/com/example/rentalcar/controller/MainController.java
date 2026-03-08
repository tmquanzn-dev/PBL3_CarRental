package com.example.rentalcar.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {
    @FXML
    private VBox sideBar;
    @FXML
    private StackPane contentArea;
    @FXML
    private Button btnDashboard;
    @FXML
    public void initialize() {
        changeView("DashboardView.fxml");
        sideBar.setVisible(false);
        if (btnDashboard != null)
            setActiveMenu(btnDashboard);
    }

    @FXML
    void handleHamburgerMenu(ActionEvent event) {
        sideBar.setVisible(!sideBar.isVisible());
    }
    @FXML
    void handleMenuClick(ActionEvent e) {
        Button btnClick = (Button) e.getSource();
        setActiveMenu(btnClick);
        String fxmlFile = "";
        switch (btnClick.getText()) {
            case "Tổng quan": fxmlFile = "DashboardView.fxml"; break;
            case "Quản lý xe": fxmlFile = "VehicleView.fxml"; break;
        }
        if (!fxmlFile.isEmpty()) {
            changeView(fxmlFile);
            sideBar.setVisible(false);
        }
    }

    private void setActiveMenu(Button activeBtn) {
        for (Node node : sideBar.getChildren()) {
            if (node instanceof Button)
                node.getStyleClass().remove("active_menu");
        }
        if (activeBtn != null)
            activeBtn.getStyleClass().add("active_menu");
    }

    private void changeView(String fxmlFile) {
        try {
            Parent fxml = FXMLLoader.load(getClass().getResource("/views/" + fxmlFile));
            contentArea.getChildren().setAll(fxml);
        } catch (IOException e) {
            System.err.println("Không tìm thấy file: " + fxmlFile);
            e.printStackTrace();
        }
    }

    @FXML
    void handleLogout(ActionEvent e) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/LoginView.fxml"));
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}