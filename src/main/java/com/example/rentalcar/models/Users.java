package com.example.rentalcar.models;

import java.util.Date;

public class Users {
    private int id_user;
    private String username;
    private String password;
    private String role;
    private String full_name;
    private String phone;
    private String cccd;
    private boolean gender;
    private Date birth_date;
    private String email;
    private boolean is_active;
    private String address;

    public Users() {};

    public Users(int id_user, String username, String password, String role, String full_name, String phone, String cccd, boolean gender, Date birth_date, String email, boolean is_active, String address) {
        this.id_user = id_user;
        this.username = username;
        this.password = password;
        this.role = role;
        this.full_name = full_name;
        this.phone = phone;
        this.cccd = cccd;
        this.gender = gender;
        this.birth_date = birth_date;
        this.email = email;
        this.is_active = is_active;
        this.address = address;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public Date getBirth_date() {
        return birth_date;
    }

    public void setBirth_date(Date birth_date) {
        this.birth_date = birth_date;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isIs_active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
