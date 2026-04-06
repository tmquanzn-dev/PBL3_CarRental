package com.example.rentalcar.bll;

import com.example.rentalcar.dao.CustomerDAO;
import com.example.rentalcar.models.Customers;
import java.util.List;

public class CustomerBLL {
    private final CustomerDAO customerDAO = new CustomerDAO();

    public Customers findByCccd(String cccd) {
        if (cccd == null || cccd.isBlank())
            throw new IllegalArgumentException("CCCD không được để trống!");
        return customerDAO.findByCccd(cccd);
    }

    public Customers findById(int id) {
        return customerDAO.findById(id);
    }

    public List<Customers> getAllCustomers() {
        return customerDAO.findAll();
    }

    public boolean addCustomer(Customers customer) {
        // Kiểm tra blacklist trước khi cho thuê
        if (customer.isIs_blacklist())
            throw new IllegalStateException("Khách hàng này đang trong danh sách đen!");
        if (customer.getFull_name() == null || customer.getFull_name().isBlank())
            throw new IllegalArgumentException("Tên khách hàng không được để trống!");
        return customerDAO.insert(customer);
    }

    public boolean updateCustomer(Customers customer) {
        return customerDAO.update(customer);
    }

    // Kiểm tra khách có thể thuê xe không
    public boolean isEligibleToRent(String cccd) {
        Customers customer = customerDAO.findByCccd(cccd);
        if (customer == null) return true; // Khách mới, cho phép
        if (customer.isIs_blacklist())
            throw new IllegalStateException("Khách hàng bị cấm thuê: " + customer.getBlacklist_reason());
        return true;
    }
}