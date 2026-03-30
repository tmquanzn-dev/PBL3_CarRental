package com.example.rentalcar.bll;

import java.time.Duration;
import java.time.LocalDateTime;

public class PriceBLL
{
    // ==========================================================
    // CÁC HẰNG SỐ CẤU HÌNH NGHIỆP VỤ (Luật chơi của Admin)
    // ==========================================================
    public static final double LATE_PENALTY_PER_HOUR = 100000.0; // Phạt 100k/giờ trễ
    public static final double FUEL_MARKET_PRICE = 25000.0;      // Giá xăng 25k/lít

    // ==========================================================
    // 1. TÍNH TIỀN THUÊ CƠ BẢN (Base Price)
    // ==========================================================
    public double calculateBasePrice(LocalDateTime startDate, LocalDateTime endDate, double pricePerDay, double pricePerHour)
    {
        if (startDate == null || endDate == null || startDate.isAfter(endDate))
        {
            return 0.0;
        }

        Duration duration = Duration.between(startDate, endDate);
        long totalHours = duration.toHours();

        // Thuê dưới 24h -> Tính thuần theo giờ
        if (totalHours < 24)
        {
            return totalHours * pricePerHour;
        }

        // Thuê trên 24h -> Tính theo ngày + số giờ lẻ
        long days = totalHours / 24;
        long remainingHours = totalHours % 24;

        return (days * pricePerDay) + (remainingHours * pricePerHour);
    }

    // ==========================================================
    // 2. TÍNH TIỀN PHẠT TRỄ GIỜ (Late Penalty)
    // ==========================================================
    public double calculateLatePenalty(LocalDateTime expectedReturnDate, LocalDateTime actualReturnDate)
    {
        if (actualReturnDate == null || !actualReturnDate.isAfter(expectedReturnDate))
        {
            return 0.0;
        }

        Duration delay = Duration.between(expectedReturnDate, actualReturnDate);
        long delayedMinutes = delay.toMinutes();

        // Làm tròn lên theo giờ (VD: trễ 65 phút -> tính là 2 giờ)
        long delayedHours = (delayedMinutes + 59) / 60;

        return delayedHours * LATE_PENALTY_PER_HOUR;
    }

    // ==========================================================
    // 3. TÍNH TIỀN PHẠT THIẾU XĂNG (Fuel Penalty)
    // ==========================================================
    public double calculateFuelPenalty(int fuelPercentStart, int fuelPercentEnd, int fuelCapacity)
    {
        if (fuelPercentEnd >= fuelPercentStart)
        {
            return 0.0;
        }

        int lostPercent = fuelPercentStart - fuelPercentEnd;
        double lostLiters = (lostPercent * fuelCapacity) / 100.0;

        return lostLiters * FUEL_MARKET_PRICE;
    }
}