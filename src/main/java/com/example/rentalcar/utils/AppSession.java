package com.example.rentalcar.utils;

import com.example.rentalcar.models.Users;

public class AppSession
{
    private static Users currentUser;

    // Private constructor để ngăn việc tạo instance mới (new AppSession())
    private AppSession() {}

    public static void setCurrentUser(Users user)
    {
        currentUser = user;
    }

    public static Users getCurrentUser()
    {
        return currentUser;
    }

    // Hàm gọi khi nhấn nút Đăng xuất
    public static void clearSession()
    {
        currentUser = null;
    }

    // Hàm tiện ích kiểm tra xem đã có ai đăng nhập chưa
    public static boolean isLoggedIn()
    {
        return currentUser != null;
    }
}
