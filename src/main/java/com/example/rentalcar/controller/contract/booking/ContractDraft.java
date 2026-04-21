package com.example.rentalcar.controller.contract.booking;

import com.example.rentalcar.models.Customers;
import com.example.rentalcar.models.DepositType;
import com.example.rentalcar.models.Vehicles;
import com.example.rentalcar.models.Vouchers;

import java.time.LocalDateTime;

/**
 * ContractDraft – Object tạm thời lưu dữ liệu xuyên suốt 4 bước tạo hợp đồng.
 *
 * Chỉ tồn tại trong RAM, KHÔNG lưu xuống DB.
 * Khi Step 4 xác nhận → INSERT 1 lần duy nhất vào bảng contracts.
 * Nếu hủy giữa chừng → object bị GC, DB không bị ảnh hưởng.
 *
 * Đường dẫn: src/main/java/com/example/rentalcar/controller/contract/booking/ContractDraft.java
 */
public class ContractDraft {

    // =========================================================
    //  STEP 1 – Khách hàng
    // =========================================================
    private Customers selectedCustomer;

    // =========================================================
    //  STEP 2 – Xe thuê
    // =========================================================
    private Vehicles selectedVehicle;

    // =========================================================
    //  STEP 3 – Thời gian & Đặt cọc
    // =========================================================
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private DepositType   depositType;
    private double        depositAmount;
    private int           fuelStart   = 100; // % xăng lúc giao xe, mặc định 100%
    private int           kmStart     = 0;   // KM lúc giao xe

    // =========================================================
    //  STEP 3 – Voucher (tùy chọn)
    // =========================================================
    private Vouchers appliedVoucher;   // null nếu không dùng voucher
    private double   discountAmount = 0;

    // =========================================================
    //  TÍNH TOÁN GIÁ (cập nhật mỗi khi Step 3 thay đổi)
    // =========================================================
    private double basePrice  = 0;
    private double totalPrice = 0;

    // =========================================================
    //  STEP 4 – Ghi chú
    // =========================================================
    private String note = "";

    // =========================================================
    //  GETTERS & SETTERS
    // =========================================================

    public Customers getSelectedCustomer()              { return selectedCustomer; }
    public void setSelectedCustomer(Customers c)        { this.selectedCustomer = c; }

    public Vehicles getSelectedVehicle()                { return selectedVehicle; }
    public void setSelectedVehicle(Vehicles v)          { this.selectedVehicle = v; }

    public LocalDateTime getStartDatetime()             { return startDatetime; }
    public void setStartDatetime(LocalDateTime dt)      { this.startDatetime = dt; }

    public LocalDateTime getEndDatetime()               { return endDatetime; }
    public void setEndDatetime(LocalDateTime dt)        { this.endDatetime = dt; }

    public DepositType getDepositType()                 { return depositType; }
    public void setDepositType(DepositType t)           { this.depositType = t; }

    public double getDepositAmount()                    { return depositAmount; }
    public void setDepositAmount(double a)              { this.depositAmount = a; }

    public int getFuelStart()                           { return fuelStart; }
    public void setFuelStart(int f)                     { this.fuelStart = f; }

    public int getKmStart()                             { return kmStart; }
    public void setKmStart(int km)                      { this.kmStart = km; }

    public Vouchers getAppliedVoucher()                 { return appliedVoucher; }
    public void setAppliedVoucher(Vouchers v)           { this.appliedVoucher = v; }

    public double getDiscountAmount()                   { return discountAmount; }
    public void setDiscountAmount(double d)             { this.discountAmount = d; }

    public double getBasePrice()                        { return basePrice; }
    public void setBasePrice(double p)                  { this.basePrice = p; }

    public double getTotalPrice()                       { return totalPrice; }
    public void setTotalPrice(double p)                 { this.totalPrice = p; }

    public String getNote()                             { return note; }
    public void setNote(String n)                       { this.note = n; }

    // =========================================================
    //  HELPER – Kiểm tra từng bước đã đủ dữ liệu chưa
    // =========================================================

    /** Step 1 hợp lệ khi đã chọn khách hàng */
    public boolean isStep1Valid() {
        return selectedCustomer != null;
    }

    /** Step 2 hợp lệ khi đã chọn xe */
    public boolean isStep2Valid() {
        return selectedVehicle != null;
    }

    /** Step 3 hợp lệ khi đã chọn đủ thời gian và hình thức cọc */
    public boolean isStep3Valid() {
        return startDatetime != null
                && endDatetime != null
                && startDatetime.isBefore(endDatetime)
                && depositType != null;
    }

    /** Tạo mã hợp đồng tự động dạng HD-xxx dựa theo số lượng HĐ hiện tại */
    public static String generateContractCode(int currentCount) {
        return String.format("HD-%03d", currentCount + 1);
    }
}