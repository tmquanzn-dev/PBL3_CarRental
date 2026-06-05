package com.example.rentalcar.bll;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalDate;

public class PriceBLL
{
    private final RuleBLL ruleBLL = new RuleBLL();
    private final SystemSettingBLL settingBLL = new SystemSettingBLL();

    // ==========================================================
    // 1. TÍNH TIỀN THUÊ CƠ BẢN (ĐÃ NÂNG CẤP: Áp hệ số cuối tuần từng ngày)
    // ==========================================================
    public double calculateBasePrice(LocalDateTime startDate, LocalDateTime endDate, double pricePerDay, double pricePerHour)
    {
        if (startDate == null || endDate == null || startDate.isAfter(endDate))
        {
            return 0.0;
        }

        // NẾU GIÁ GIỜ BẰNG 0, TỰ ĐỘNG QUY ĐỔI THEO GIÁ NGÀY ĐỂ TRÁNH BUG 0 ĐỒNG
        if (pricePerHour <= 0) {
            pricePerHour = pricePerDay / 24.0;
        }

        Duration duration = Duration.between(startDate, endDate);
        long totalHours = duration.toHours();

        // Nếu thuê chưa đầy 1 tiếng nhưng thực tế có sử dụng xe, làm tròn lên thành 1 tiếng để tính tiền
        if (totalHours == 0 && duration.toMinutes() > 0) {
            totalHours = 1;
        }

        // ─── TRƯỜNG HỢP A: THUÊ NGẮN HẠN DƯỚI 24 TIẾNG ───
        if (totalHours < 24)
        {
            double baseAmount = totalHours * pricePerHour;
            // Thuê ngắn hạn tính hệ số dựa theo đúng ngày nhận xe
            double multiplier = ruleBLL.getSmartMultiplierForDate(startDate.toLocalDate());
            return baseAmount * multiplier;
        }

        // ─── TRƯỜNG HỢP B: THUÊ DÀI HẠN TRÊN 24 TIẾNG ───
        long days = totalHours / 24;
        long remainingHours = totalHours % 24;

        double totalAmount = 0.0;
        LocalDate startLocalDate = startDate.toLocalDate();

        // Chạy vòng lặp duyệt quét qua từng ngày thuê lẻ để áp hệ số chính xác
        for (int i = 0; i < days; i++) {
            LocalDate currentDay = startLocalDate.plusDays(i);

            // Gọi hàm bóc tách lịch từ RuleBLL (T7/CN tự nhân hệ số, ngày thường hệ số = 1.0)
            double dayMultiplier = ruleBLL.getSmartMultiplierForDate(currentDay);

            // Cộng dồn doanh thu ngày đang xét
            totalAmount += (pricePerDay * dayMultiplier);
        }

        // Nếu có số giờ lẻ vượt ngày, tính tiền giờ lẻ áp theo luật của ngày trả xe
        if (remainingHours > 0) {
            LocalDate endLocalDate = endDate.toLocalDate();
            double hourMultiplier = ruleBLL.getSmartMultiplierForDate(endLocalDate);
            totalAmount += (remainingHours * pricePerHour * hourMultiplier);
        }

        return totalAmount;
    }

    // ==========================================================
    // 2. TÍNH TIỀN PHẠT TRỄ GIỜ (Giữ nguyên cấu trúc gốc)
    // ==========================================================
    public double calculateLatePenalty(LocalDateTime expectedReturnDate, LocalDateTime actualReturnDate)
    {
        if (actualReturnDate == null || !actualReturnDate.isAfter(expectedReturnDate))
        {
            return 0.0;
        }

        Duration delay = Duration.between(expectedReturnDate, actualReturnDate);
        long delayedMinutes = delay.toMinutes();

        long delayedHours = (delayedMinutes + 59) / 60;
        double latePenaltyPerHour = settingBLL.getDoubleSetting("Phi_Tre_Gio", 100000.0);

        return delayedHours * latePenaltyPerHour;
    }

    // ==========================================================
    // 3. TÍNH TIỀN PHẠT THIẾU XĂNG (Giữ nguyên cấu trúc gốc)
    // ==========================================================
    public double calculateFuelPenalty(int fuelPercentStart, int fuelPercentEnd, int fuelCapacity)
    {
        if (fuelPercentEnd >= fuelPercentStart)
        {
            return 0.0;
        }

        int lostPercent = fuelPercentStart - fuelPercentEnd;
        double lostLiters = (lostPercent * fuelCapacity) / 100.0;
        double fuelMarketPrice = settingBLL.getDoubleSetting("Gia_Xang_Litre", 25000.0);

        return lostLiters * fuelMarketPrice;
    }

    // ==========================================================
    // 4. TÍNH TIỀN KHI TRẢ XE TRƯỚC HẠN (Giữ nguyên cấu trúc gốc)
    // ==========================================================
    public double calculateEarlyReturnPrice(LocalDateTime startDate, LocalDateTime expectedEndDate, LocalDateTime actualReturnDate, double pricePerDay, double pricePerHour)
    {
        // Nếu trả đúng hạn hoặc trễ hạn thì tính theo mốc thời gian đăng ký ban đầu
        if (!actualReturnDate.isBefore(expectedEndDate))
        {
            return calculateBasePrice(startDate, expectedEndDate, pricePerDay, pricePerHour);
        }

        // Bắt lỗi thời gian trả xe nghịch lý
        if (actualReturnDate.isBefore(startDate))
        {
            throw new IllegalArgumentException("Thời gian trả xe không hợp lệ (nhỏ hơn cả thời gian bắt đầu)!");
        }

        // BẪY LỖI: Nếu giá giờ truyền vào bằng 0, ép quy đổi từ giá ngày ra để tính toán
        if (pricePerHour <= 0) {
            pricePerHour = pricePerDay / 24.0;
        }

        // 1. TÍNH SỐ TIỀN THỰC TẾ KHÁCH ĐÃ ĐI
        double actualUsedPrice = calculateBasePrice(startDate, actualReturnDate, pricePerDay, pricePerHour);

        // 2. TÍNH PHÍ PHẠT HỦY NGANG: Bằng 30% trên chính số tiền đã dùng đó
        double penaltyFee = actualUsedPrice * 0.30;

        // 3. TỔNG TIỀN THUÊ MỚI = Tiền thực tế đã đi + 30% Phí phạt bồi thường
        double totalEarlyReturnPrice = actualUsedPrice + penaltyFee;

        // 4. CHỐNG VƯỢT BIÊN: Đảm bảo tổng tiền sau khi phạt không bao giờ vượt quá số tiền của gói đặt gốc ban đầu
        double originalContractPrice = calculateBasePrice(startDate, expectedEndDate, pricePerDay, pricePerHour);
        if (totalEarlyReturnPrice > originalContractPrice) {
            return originalContractPrice;
        }

        return totalEarlyReturnPrice;
    }
}