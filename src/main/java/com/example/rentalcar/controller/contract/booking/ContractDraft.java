package com.example.rentalcar.controller.contract.booking;

import com.example.rentalcar.models.Customers;
import com.example.rentalcar.models.DepositType;
import com.example.rentalcar.models.Vehicles;
import com.example.rentalcar.models.Vouchers;

import java.time.LocalDateTime;

public class ContractDraft {

    private Customers selectedCustomer;
    private Vehicles selectedVehicle;

    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private DepositType depositType;
    private double depositAmount;
    private int fuelStart = 100;
    private int kmStart = 0;

    private Vouchers appliedVoucher;   // null nếu không dùng voucher
    private double   discountAmount = 0;

    // =========================================================
    //  TÍNH TOÁN GIÁ (cập nhật mỗi khi Step 3 thay đổi)
    // =========================================================
    private double basePrice  = 0;
    private double totalPrice = 0;

    private String note = "";

    public Customers getSelectedCustomer() {
        return selectedCustomer;
    }

    public void setSelectedCustomer(Customers selectedCustomer) {
        this.selectedCustomer = selectedCustomer;
    }

    public Vehicles getSelectedVehicle() {
        return selectedVehicle;
    }

    public void setSelectedVehicle(Vehicles selectedVehicle) {
        this.selectedVehicle = selectedVehicle;
    }

    public LocalDateTime getStartDatetime() {
        return startDatetime;
    }

    public void setStartDatetime(LocalDateTime startDatetime) {
        this.startDatetime = startDatetime;
    }

    public LocalDateTime getEndDatetime() {
        return endDatetime;
    }

    public void setEndDatetime(LocalDateTime endDatetime) {
        this.endDatetime = endDatetime;
    }

    public DepositType getDepositType() {
        return depositType;
    }

    public void setDepositType(DepositType depositType) {
        this.depositType = depositType;
    }

    public double getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(double depositAmount) {
        this.depositAmount = depositAmount;
    }

    public int getFuelStart() {
        return fuelStart;
    }

    public void setFuelStart(int fuelStart) {
        this.fuelStart = fuelStart;
    }

    public int getKmStart() {
        return kmStart;
    }

    public void setKmStart(int kmStart) {
        this.kmStart = kmStart;
    }

    public Vouchers getAppliedVoucher() {
        return appliedVoucher;
    }

    public void setAppliedVoucher(Vouchers appliedVoucher) {
        this.appliedVoucher = appliedVoucher;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }


    //  HELPER – Kiểm tra từng bước đã đủ dữ liệu chưa
    //Step 1 hợp lệ khi đã chọn khách hàng
    public boolean isStep1Valid() {
        return selectedCustomer != null;
    }

    //Step 2 hợp lệ khi đã chọn xe
    public boolean isStep2Valid() {
        return selectedVehicle != null;
    }

    //Step 3 hợp lệ khi đã chọn đủ thời gian và hình thức cọc
    public boolean isStep3Valid() {
        return startDatetime != null
                && endDatetime != null
                && startDatetime.isBefore(endDatetime)
                && depositType != null;
    }

    //Tạo mã hợp đồng tự động dạng HD-xxx dựa theo số lượng HĐ hiện tại
    public static String generateContractCode(int currentCount) {
        return String.format("HD-%03d", currentCount + 1);
    }
}