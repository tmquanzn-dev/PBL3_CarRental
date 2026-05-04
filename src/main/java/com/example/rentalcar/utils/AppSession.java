package com.example.rentalcar.utils;

import com.example.rentalcar.models.Users;

/**
 * AppSession – Lưu trạng thái đăng nhập toàn cục.
 * role_id = 1 → Admin  (toàn quyền)
 * role_id = 2 → Staff  (hạn chế theo nghiệp vụ)
 */
public class AppSession {

    private static Users currentUser;

    private AppSession() {}

    public static void setCurrentUser(Users user) {
        currentUser = user;
    }
    public static Users getCurrentUser() {
        return currentUser;
    }
    public static void clearSession() {
        currentUser = null;
    }
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /** Admin = role_id 1 */
    public static boolean isAdmin() {

        return currentUser != null && currentUser.getRole_id() == 1;
    }

    /** Staff = role_id 2 */
    public static boolean isStaff() {
        return currentUser != null && currentUser.getRole_id() == 2;
    }

    /** Tên role hiển thị lên topbar */
    public static String getRoleDisplayName() {
        if (currentUser == null) return "";
        String rn = currentUser.getRole_name();
        if (rn != null && !rn.isBlank()) return rn;
        return isAdmin() ? "Admin" : "Staff";
    }
}