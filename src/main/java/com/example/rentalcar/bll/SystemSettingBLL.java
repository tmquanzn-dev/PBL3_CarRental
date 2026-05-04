package com.example.rentalcar.bll;

import com.example.rentalcar.dao.SystemSettingDAO;
import com.example.rentalcar.models.SystemSettings;
import com.example.rentalcar.utils.AppSession;

import java.util.List;

public class SystemSettingBLL {

    private final SystemSettingDAO settingDAO = new SystemSettingDAO();

    // ==========================================================
    // 1. NHÓM HÀM LẤY GIÁ TRỊ (Dùng để tính toán tiền)
    // ==========================================================

    public double getDoubleSetting(String key, double defaultValue) {
        String val = settingDAO.getValueByKey(key);
        if (val == null || val.isBlank()) return defaultValue;
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            System.err.println("Cảnh báo: Cấu hình '" + key + "' bị sai định dạng số thực. Dùng mặc định: " + defaultValue);
            return defaultValue;
        }
    }

    public int getIntSetting(String key, int defaultValue) {
        String val = settingDAO.getValueByKey(key);
        if (val == null || val.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            System.err.println("Cảnh báo: Cấu hình '" + key + "' bị sai định dạng số nguyên. Dùng mặc định: " + defaultValue);
            return defaultValue;
        }
    }

    public String getStringSetting(String key, String defaultValue) {
        String val = settingDAO.getValueByKey(key);
        return (val == null || val.isBlank()) ? defaultValue : val;
    }

    // ==========================================================
    // 2. NHÓM HÀM QUẢN LÝ (Dành cho màn hình Admin Settings)
    // ==========================================================

    public List<SystemSettings> getAllSettings() {
        return settingDAO.findAll();
    }

    // ==========================================================
    // CHỈ ADMIN ĐƯỢC SỬA CÀI ĐẶT
    // ==========================================================

    public boolean updateSettingValue(String key, String newValue, int editorUserId) {
        if (!AppSession.isAdmin()) {
            throw new IllegalStateException("Lỗi bảo mật: Chỉ Admin mới được thay đổi cấu hình hệ thống (giá xăng, tiền phạt...)!");
        }

        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Khóa cài đặt không được để trống!");
        }
        if (newValue == null || newValue.isBlank()) {
            throw new IllegalArgumentException("Giá trị cài đặt không được để trống!");
        }

        // Lấy setting hiện tại lên để kiểm tra Data Type
        // (Giả sử trong DAO em có hàm findByKey, nếu chưa có thì viết thêm nhé)
        SystemSettings existing = settingDAO.findByKey(key);
        if (existing == null) {
            throw new IllegalArgumentException("Không tìm thấy cấu hình với mã: " + key);
        }

        // Kiểm tra xem dữ liệu nhập vào có khớp với DataType yêu cầu không
        validateDataType(newValue, existing.getData_type().name());

        existing.setSetting_value(newValue.trim());
        existing.getId_user().setId_user(editorUserId); // Lưu lại ID người vừa sửa

        return settingDAO.update(existing);
    }

    private void validateDataType(String value, String expectedType) {
        try {
            if ("NUMBER".equalsIgnoreCase(expectedType)) {
                Double.parseDouble(value); // Ép kiểu thử, nếu lỗi sẽ văng xuống catch
            } else if ("BOOLEAN".equalsIgnoreCase(expectedType)) {
                if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false") && !value.equals("0") && !value.equals("1")) {
                    throw new IllegalArgumentException("Kiểu Boolean chỉ nhận giá trị true/false hoặc 1/0.");
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Dữ liệu nhập vào phải là số!");
        }
    }
}