package com.example.rentalcar.models;


import java.sql.Date;

public class Vouchers {
    private int id_voucher;
    private String code_vouchers;
    private String description;
    private DiscountType discount_type;
    private double discount_value;
    private int usage_limit;
    private int usage_count;
    private Date valid_from_date;
    private Date valid_to_date;
    private boolean is_active;

    public Vouchers() {};

    public Vouchers(int id_voucher, String code_vouchers, String description, DiscountType discount_type, double discount_value, int usage_limit, int usage_count, Date valid_from_date, Date valid_to_date, boolean is_active) {
        this.id_voucher = id_voucher;
        this.code_vouchers = code_vouchers;
        this.description = description;
        this.discount_type = discount_type;
        this.discount_value = discount_value;
        this.usage_limit = usage_limit;
        this.usage_count = usage_count;
        this.valid_from_date = valid_from_date;
        this.valid_to_date = valid_to_date;
        this.is_active = is_active;
    }

    public int getId_voucher() {
        return id_voucher;
    }

    public void setId_voucher(int id_voucher) {
        this.id_voucher = id_voucher;
    }

    public String getCode_vouchers() {
        return code_vouchers;
    }

    public void setCode_vouchers(String code_vouchers) {
        this.code_vouchers = code_vouchers;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DiscountType getDiscount_type() {
        return discount_type;
    }

    public void setDiscount_type(DiscountType discount_type) {
        this.discount_type = discount_type;
    }

    public double getDiscount_value() {
        return discount_value;
    }

    public void setDiscount_value(double discount_value) {
        this.discount_value = discount_value;
    }

    public int getUsage_limit() {
        return usage_limit;
    }

    public void setUsage_limit(int usage_limit) {
        this.usage_limit = usage_limit;
    }

    public int getUsage_count() {
        return usage_count;
    }

    public void setUsage_count(int usage_count) {
        this.usage_count = usage_count;
    }

    public Date getValid_from_date() {
        return valid_from_date;
    }

    public void setValid_from_date(Date valid_from_date) {
        this.valid_from_date = valid_from_date;
    }

    public Date getValid_to_date() {
        return valid_to_date;
    }

    public void setValid_to_date(Date valid_to_date) {
        this.valid_to_date = valid_to_date;
    }

    public boolean isIs_active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }
}
