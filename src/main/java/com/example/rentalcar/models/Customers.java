package com.example.rentalcar.models;

public class Customers {
    private int id_customer;
    private String cccd;
    private String full_name;
    private boolean gender;
    private String phone;
    private int trust_score;
    private String email;
    private String address;
    private boolean is_blacklist;
    private String blacklist_reason;
    private int rental_count;
    private String cccd_images;

    public Customers() {}

    public Customers(int id_customer, String cccd, String full_name, boolean gender, String phone, int trust_score, String email, String address, boolean is_blacklist, String blacklist_reason, int rental_count, String cccd_images) {
        this.id_customer = id_customer;
        this.cccd = cccd;
        this.full_name = full_name;
        this.gender = gender;
        this.phone = phone;
        this.trust_score = trust_score;
        this.email = email;
        this.address = address;
        this.is_blacklist = is_blacklist;
        this.blacklist_reason = blacklist_reason;
        this.rental_count = rental_count;
        this.cccd_images = cccd_images;
    }

    public String getCccd_images() {
        return cccd_images;
    }

    public void setCccd_images(String cccd_images) {
        this.cccd_images = cccd_images;
    }

    public int getId_customer() {
        return id_customer;
    }

    public void setId_customer(int id_customer) {
        this.id_customer = id_customer;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getTrust_score() {
        return trust_score;
    }

    public void setTrust_score(int trust_score) {
        this.trust_score = trust_score;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isIs_blacklist() {
        return is_blacklist;
    }

    public void setIs_blacklist(boolean is_blacklist) {
        this.is_blacklist = is_blacklist;
    }

    public String getBlacklist_reason() {
        return blacklist_reason;
    }

    public void setBlacklist_reason(String blacklist_reason) {
        this.blacklist_reason = blacklist_reason;
    }

    public int getRental_count() {
        return rental_count;
    }

    public void setRental_count(int rental_count) {
        this.rental_count = rental_count;
    }
}
