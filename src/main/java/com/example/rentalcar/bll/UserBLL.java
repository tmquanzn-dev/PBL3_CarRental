package com.example.rentalcar.bll;

import com.example.rentalcar.dao.UserDAO;
import com.example.rentalcar.models.Users;
import com.example.rentalcar.utils.AppSession;

import java.util.List;

public class UserBLL {
    private final UserDAO userDAO = new UserDAO();

    // Đăng nhập
    public Users login(String username, String password){
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Tên đăng nhập không được để trống!");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Mật khẩu không được để trống!");

        Users user = userDAO.findByUsername(username);
        if (user == null)
            throw new IllegalArgumentException("Tên đăng nhập không tồn tại!");
        if (!user.getPassword().equals(password))
            throw new IllegalArgumentException("Mật khẩu không đúng");
        if (!user.isIs_active())
            throw new IllegalArgumentException("Tài khoản đã bị khoá. Vui lòng liên hệ Admin để mở khóa!");

        return user;
    }

    // ==========================================================
    // QUẢN LÝ NHÂN SỰ (CHỈ ADMIN)
    // ==========================================================

    public boolean createUser(Users user) {
        if (!AppSession.isAdmin()) throw new IllegalStateException("Cảnh báo bảo mật: Chỉ Admin mới được tạo tài khoản!");

        if (user.getUsername() == null || user.getUsername().isBlank())
            throw new IllegalArgumentException("Username không được để trống");
        if (user.getPassword() == null || user.getPassword().isBlank())
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 8 kí tự");
        if (user.getFull_name() == null || user.getFull_name().isBlank())
            throw new IllegalArgumentException("Họ tên không được để trống");

        Users check = userDAO.findByUsername(user.getUsername());
        if (check != null)
            throw new IllegalArgumentException("Username "+ user.getUsername()+ " đã tồn tại. Vui lòng nhập lại!");

        return userDAO.insert(user);
    }

    //Khóa tk
    public boolean lockUser(int id) {
        if (!AppSession.isAdmin()) throw new IllegalStateException("Cảnh báo bảo mật: Chỉ Admin mới có quyền khóa tài khoản!");

        Users user = userDAO.findById(id);
        if (user == null)
            throw new IllegalArgumentException("Khoông tìm thấy nhân viên!");
        if (!user.isIs_active())
            throw new IllegalArgumentException("Tài khoản này đã bị khóa rồi!");

        user.setIs_active(false);
        return userDAO.update(user);
    }

    //Mở khóa tk
    public boolean unlockUser(int id)
    {
        if (!AppSession.isAdmin()) throw new IllegalStateException("Cảnh báo bảo mật: Chỉ Admin mới có quyền mở khóa tài khoản!");

        Users user = userDAO.findById(id);
        if (user == null)
            throw new IllegalArgumentException("Không tìm thấy nhân viên!");
        if (user.isIs_active())
            throw new IllegalArgumentException("Tài khoản này hiện đang không bị khóa!");

        user.setIs_active(true);
        return userDAO.update(user);
    }

    public boolean changePassword(int id, String oldPass, String newPass)
    {
        // Chỉ cho phép nếu là Admin HOẶC đang đổi pass của chính tài khoản đang đăng nhập
        if (!AppSession.isAdmin() && AppSession.getCurrentUser().getId_user() != id) {
            throw new IllegalStateException("Cảnh báo bảo mật: Bạn chỉ được đổi mật khẩu của chính mình!");
        }

        if (oldPass == null || newPass.length() < 8)
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 8 kí tự");

        Users user = userDAO.findById(id);
        if (user == null)
            throw new IllegalArgumentException("Không tìm thấy nhân viên");
        if (!user.getPassword().equals(oldPass))
            throw new IllegalArgumentException("Mật khẩu cũ không đúng");
        if (oldPass.equals(newPass))
            throw new IllegalArgumentException("Mật khẩu mới không được trùng mật khẩu cũ");
        user.setPassword(newPass);
        return userDAO.update(user);
    }

    //Admin(reset mật khẩu, không cần pass cũ)
    public boolean resetPassword(int id, String newPass)
    {
        if (!AppSession.isAdmin()) throw new IllegalStateException("Cảnh báo bảo mật: Chỉ Admin mới được reset mật khẩu!");

        if (newPass == null || newPass.length() < 8)
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 8 kí tự");
        Users user = userDAO.findById(id);
        if (user == null)
            throw new IllegalArgumentException("Không tìm thấy nhân viên");
        user.setPassword(newPass);
        return userDAO.update(user);
    }

    public boolean updateUser(Users user)
    {
        if (!AppSession.isAdmin()) throw new IllegalStateException("Cảnh báo bảo mật: Chỉ Admin mới được cập nhật thông tin nhân viên!");

        if (user.getFull_name() == null || user.getFull_name().isBlank())
            throw new IllegalArgumentException("Họ tên không được để trống");
        Users check = userDAO.findById(user.getId_user());
        if (check == null)
            throw new IllegalArgumentException("Không tìm thấy nhân viên");
        return userDAO.update(user);
    }

    public List<Users> getAllUsers()
    {
        return userDAO.findAll();
    }
    public Users getUserById(int id)
    {
        Users users = userDAO.findById(id);
        if(users == null)
            throw new IllegalArgumentException("Không tìm thấy nhân viên với ID: "+ id);
        return users;
    }
    public Users getUserByUsername(String username)
    {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username không được để trống");
        return userDAO.findByUsername(username);
    }
}
