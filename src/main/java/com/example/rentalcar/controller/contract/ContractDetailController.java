package com.example.rentalcar.controller.contract;
import com.example.rentalcar.dao.CustomerDAO;
import com.example.rentalcar.dao.VehicleDAO;
import com.example.rentalcar.models.Contracts;
import com.example.rentalcar.models.Customers;
import com.example.rentalcar.models.Vehicles;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class ContractDetailController {

    @FXML private Label label_contract_code, label_customer_name, label_customer_phone;
    @FXML private Label label_vehicle_info, label_rental_period, label_status, label_total_price;

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void setContractData(Contracts contract) {
        label_contract_code.setText("#" + contract.getCode_contract());

        if (contract.getId_customer() != null) {
            Customers c = customerDAO.findById(contract.getId_customer().getId_customer());
            if (c != null) {
                label_customer_name.setText(c.getFull_name());
                label_customer_phone.setText(c.getPhone());
            }
        }

        if (contract.getId_vehicle() != null) {
            Vehicles v = vehicleDAO.findById(contract.getId_vehicle().getId_vehicle());
            if (v != null) {
                label_vehicle_info.setText(v.getBrand() + " " + v.getModel() + " (" + v.getCode_vehicle() + ")");
            }
        }

        String start = contract.getStart_datetime().format(formatter);
        String end = contract.getEnd_datetime().format(formatter);
        label_rental_period.setText(start + " - " + end);

        label_status.setText(contract.getStatus().name().replace("_", " "));
        label_total_price.setText(String.format("%,.0f đ", contract.getTotal_price()).replace(",", "."));
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) label_contract_code.getScene().getWindow();
        stage.close();
    }
}