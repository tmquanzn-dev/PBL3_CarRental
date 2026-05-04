package com.example.rentalcar.bll;

import com.example.rentalcar.dao.CustomerDAO;
import com.example.rentalcar.models.Customers;
import com.example.rentalcar.utils.AppSession;

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
        if (customer.isIs_blacklist())
            throw new IllegalStateException("Khách hàng này đang trong danh sách đen!");
        if (customer.getFull_name() == null || customer.getFull_name().isBlank())
            throw new IllegalArgumentException("Tên khách hàng không được để trống!");
        return customerDAO.insert(customer);
    }

    public boolean updateCustomer(Customers customer) {
        return customerDAO.update(customer);
    }

    // ============================================================
    // BLACKLIST
    // ============================================================

    /**
     * Thêm khách hàng vào danh sách đen.
     * Bắt buộc phải có lý do (reason).
     */
    public boolean addToBlacklist(int customerId, String reason) {
        if (!AppSession.isAdmin()) throw new IllegalStateException("Cảnh báo bảo mật: Bạn không có quyền thực hiện thao tác Blacklist!"); //

        if (reason == null || reason.isBlank())
            throw new IllegalArgumentException("Phải nhập lý do đưa vào danh sách đen!");

        Customers customer = customerDAO.findById(customerId);
        if (customer == null)
            throw new IllegalArgumentException("Không tìm thấy khách hàng!");
        if (customer.isIs_blacklist())
            throw new IllegalStateException("Khách hàng này đã nằm trong danh sách đen rồi!");

        customer.setIs_blacklist(true);
        customer.setBlacklist_reason(reason.trim());
        return customerDAO.update(customer);
    }

    /**
     * Gỡ khách hàng khỏi danh sách đen.
     */
    public boolean removeFromBlacklist(int customerId) {
        Customers customer = customerDAO.findById(customerId);
        if (customer == null)
            throw new IllegalArgumentException("Không tìm thấy khách hàng!");
        if (!customer.isIs_blacklist())
            throw new IllegalStateException("Khách hàng này không nằm trong danh sách đen!");

        customer.setIs_blacklist(false);
        customer.setBlacklist_reason(null);
        return customerDAO.update(customer);
    }

    // KIỂM TRA ĐĂNG KÝ THUÊ
    /** Kiểm tra khách có đủ điều kiện thuê xe không */
    public boolean isEligibleToRent(String cccd) {
        Customers customer = customerDAO.findByCccd(cccd);
        if (customer == null) return true; // Khách mới
        if (customer.isIs_blacklist())
            throw new IllegalStateException("Khách hàng bị cấm thuê: " + customer.getBlacklist_reason());
        return true;
    }
}