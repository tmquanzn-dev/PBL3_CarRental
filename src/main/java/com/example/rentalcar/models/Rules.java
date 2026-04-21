package com.example.rentalcar.models;

import java.sql.Date;

public class Rules {
    private int id_rule;
    private String rule_name;
    private double multi;
    private Date start_date;
    private Date end_date;
    private boolean is_active;

    public Rules() {};

    public Rules(int id_rule, String rule_name,
                 double multi, Date start_date, Date end_date, boolean is_active) {
        this.id_rule = id_rule;
        this.rule_name = rule_name;
        this.multi = multi;
        this.start_date = start_date;
        this.end_date = end_date;
        this.is_active = is_active;
    }

    public int getId_rule() {
        return id_rule;
    }

    public void setId_rule(int id_rule) {
        this.id_rule = id_rule;
    }

    public String getRule_name() {
        return rule_name;
    }

    public void setRule_name(String rule_name) {
        this.rule_name = rule_name;
    }

    public double getMulti() {
        return multi;
    }

    public void setMulti(double multi) {
        this.multi = multi;
    }

    public Date getStart_date() {
        return start_date;
    }

    public void setStart_date(Date start_date) {
        this.start_date = start_date;
    }

    public Date getEnd_date() {
        return end_date;
    }

    public void setEnd_date(Date end_date) {
        this.end_date = end_date;
    }

    public boolean isIs_active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }
}
