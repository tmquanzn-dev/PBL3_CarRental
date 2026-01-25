package com.example.rentalcar.models;

import java.time.LocalDateTime;

public class SystemSettings {
    private int id_setting;
    private String setting_key;
    private String setting_value;
    private DataType data_type;
    private String description;
    private String category;
    private Users id_user;
    private LocalDateTime update_at;

    public SystemSettings(int id_setting, String setting_key, String setting_value, DataType data_type,
                          String description, String category, Users id_user, LocalDateTime update_at) {
        this.id_setting = id_setting;
        this.setting_key = setting_key;
        this.setting_value = setting_value;
        this.data_type = data_type;
        this.description = description;
        this.category = category;
        this.id_user = id_user;
        this.update_at = update_at;
    }

    public int getId_setting() {
        return id_setting;
    }

    public void setId_setting(int id_setting) {
        this.id_setting = id_setting;
    }

    public String getSetting_key() {
        return setting_key;
    }

    public void setSetting_key(String setting_key) {
        this.setting_key = setting_key;
    }

    public String getSetting_value() {
        return setting_value;
    }

    public void setSetting_value(String setting_value) {
        this.setting_value = setting_value;
    }

    public DataType getData_type() {
        return data_type;
    }

    public void setData_type(DataType data_type) {
        this.data_type = data_type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Users getId_user() {
        return id_user;
    }

    public void setId_user(Users id_user) {
        this.id_user = id_user;
    }

    public LocalDateTime getUpdate_at() {
        return update_at;
    }

    public void setUpdate_at(LocalDateTime update_at) {
        this.update_at = update_at;
    }
}
