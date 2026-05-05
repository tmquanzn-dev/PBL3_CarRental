package com.example.rentalcar.bll;

import java.time.Duration;
import java.time.LocalDateTime;

public class PriceBLL
{
    // Gọi các BLL khác sang để hỗ trợ
    private final RuleBLL ruleBLL = new RuleBLL();
    private final SystemSettingBLL settingBLL = new SystemSettingBLL();

    // ==========================================================
    // 1. TÍNH TIỀN THUÊ CƠ BẢN (Đã tích hợp Hệ số Lễ/Tết)
    // ==========================================================
    public double calculateBasePrice(LocalDateTime startDate, LocalDateTime endDate, double pricePerDay, double pricePerHour)
    {
        if (startDate == null || endDate == null || startDate.isAfter(endDate))
        {
            return 0.0;
        }

        Duration duration = Duration.between(startDate, endDate);
        long totalHours = duration.toHours();

        double baseAmount = 0.0;

        // Thuê dưới 24h -> Tính thuần theo giờ
        if (totalHours < 24)
        {
            baseAmount = totalHours * pricePerHour;
        }
        else
        {
            // Thuê trên 24h -> Tính theo ngày + số giờ lẻ
            long days = totalHours / 24;
            long remainingHours = totalHours % 24;
            baseAmount = (days * pricePerDay) + (remainingHours * pricePerHour);
        }

        // TÍCH HỢP RULE: Lấy hệ số nhân từ RuleBLL
        double multiplier = ruleBLL.getHighestMultiplier(startDate, endDate);

        // Giá cuối cùng = Giá gốc * Hệ số nhân (VD: 500k * 1.5 = 750k)
        return baseAmount * multiplier;
    }

    // ==========================================================
    // 2. TÍNH TIỀN PHẠT TRỄ GIỜ (Lấy cấu hình từ Database)
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

        // Đọc mức phạt từ SystemSettings (Nếu DB không có thì mặc định lấy 100.000đ)
        double latePenaltyPerHour = settingBLL.getDoubleSetting("Phi_Tre_Gio", 100000.0);

        return delayedHours * latePenaltyPerHour;
    }

    // ==========================================================
    // 3. TÍNH TIỀN PHẠT THIẾU XĂNG (Lấy cấu hình từ Database)
    // ==========================================================
    public double calculateFuelPenalty(int fuelPercentStart, int fuelPercentEnd, int fuelCapacity)
    {
        if (fuelPercentEnd >= fuelPercentStart)
        {
            return 0.0;
        }

        int lostPercent = fuelPercentStart - fuelPercentEnd;
        double lostLiters = (lostPercent * fuelCapacity) / 100.0;

        // Đọc giá xăng thị trường từ SystemSettings (Nếu DB không có thì mặc định lấy 25.000đ)
        double fuelMarketPrice = settingBLL.getDoubleSetting("Gia_Xang_Litre", 25000.0);

        return lostLiters * fuelMarketPrice;
    }
}