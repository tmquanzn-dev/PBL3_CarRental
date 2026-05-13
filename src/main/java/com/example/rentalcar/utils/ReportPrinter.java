package com.example.rentalcar.utils;

import com.example.rentalcar.models.StaffReportRow;
import com.example.rentalcar.models.VehicleReportRow;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * ReportPrinter – Xuất báo cáo kinh doanh ra file PDF.
 */
public class ReportPrinter {

    // ── Màu ──────────────────────────────────────────────
    private static final DeviceRgb C_PRIMARY  = new DeviceRgb(59, 130, 246);   // #3b82f6
    private static final DeviceRgb C_DARK     = new DeviceRgb(30, 41, 59);     // #1e293b
    private static final DeviceRgb C_GRAY     = new DeviceRgb(100, 116, 139);  // #64748b
    private static final DeviceRgb C_LIGHT    = new DeviceRgb(248, 250, 252);  // #f8fafc
    private static final DeviceRgb C_GREEN    = new DeviceRgb(16, 185, 129);   // #10b981
    private static final DeviceRgb C_RED      = new DeviceRgb(220, 38, 38);    // #dc2626
    private static final DeviceRgb C_YELLOW   = new DeviceRgb(180, 83, 9);     // #b45309
    private static final DeviceRgb C_YELLOW_BG= new DeviceRgb(254, 243, 199);
    private static final DeviceRgb C_BORDER   = new DeviceRgb(226, 232, 240);  // #e2e8f0
    private static final DeviceRgb C_HEADER_BG= new DeviceRgb(239, 246, 255);  // #eff6ff

    private static final String[] MONTH_LABELS = {
            "T1","T2","T3","T4","T5","T6",
            "T7","T8","T9","T10","T11","T12"
    };
    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // ── Data container ────────────────────────────────────
    public static class PrintData {
        public int    year;
        public int    month;          // 0 = cả năm
        public double totalRev;
        public double growthPct;
        public int    totalContracts;
        public int    totalCustomers;
        public double avgMonthly;
        public double monthRev;
        public int    monthContracts;
        public Map<Integer, Double>     monthlyRevMap;
        public Map<String,  Integer>    vehStatus;
        public List<VehicleReportRow>   topVehicles;
        public List<StaffReportRow>     topStaff;
    }

    // ─────────────────────────────────────────────────────
    //  PUBLIC API
    // ─────────────────────────────────────────────────────
    public static String print(PrintData data, Window owner) {
        if (data == null) return null;

        String defaultName = data.month > 0
                ? "BaoCao_T" + data.month + "_" + data.year + ".pdf"
                : "BaoCao_Nam" + data.year + ".pdf";

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Lưu báo cáo PDF");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files (*.pdf)", "*.pdf"));
        chooser.setInitialFileName(defaultName);
        File file = chooser.showSaveDialog(owner);
        if (file == null) return null;

        try {
            generatePdf(data, file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Loi tao PDF: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────
    //  CORE
    // ─────────────────────────────────────────────────────
    private static void generatePdf(PrintData data, String path) throws Exception {
        PdfWriter   writer = new PdfWriter(path);
        PdfDocument pdf    = new PdfDocument(writer);
        Document    doc    = new Document(pdf, PageSize.A4);
        doc.setMargins(36, 36, 36, 36);

        PdfFont regular = loadFont(false);
        PdfFont bold    = loadFont(true);

        addHeader(doc, data, regular, bold);
        addKpiSection(doc, data, regular, bold);
        addRevenueTable(doc, data, regular, bold);
        addTopVehicles(doc, data, regular, bold);
        addTopStaff(doc, data, regular, bold);
        addVehicleStatus(doc, data, regular, bold);
        addFooter(doc, regular);

        doc.close();
    }

    // ─────────────────────────────────────────────────────
    //  1. HEADER
    // ─────────────────────────────────────────────────────
    private static void addHeader(Document doc, PrintData data,
                                  PdfFont regular, PdfFont bold) {
        doc.add(new Paragraph("VEHICLERENT PRO")
                .setFont(bold).setFontSize(22).setFontColor(C_PRIMARY)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(2));

        doc.add(new Paragraph("He thong quan ly cho thue xe may chuyen nghiep")
                .setFont(regular).setFontSize(10).setFontColor(C_GRAY)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(12));

        doc.add(new LineSeparator(
                new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(1.5f))
                .setStrokeColor(C_PRIMARY).setMarginBottom(12));

        String title = data.month > 0
                ? "BAO CAO KINH DOANH THANG " + data.month + "/" + data.year
                : "BAO CAO KINH DOANH NAM " + data.year;

        doc.add(new Paragraph(title)
                .setFont(bold).setFontSize(17).setFontColor(C_DARK)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(4));

        doc.add(new Paragraph("In luc: " + LocalDateTime.now().format(DT_FMT))
                .setFont(regular).setFontSize(9).setFontColor(C_GRAY)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(18));
    }

    // ─────────────────────────────────────────────────────
    //  2. KPI TỔNG QUAN
    // ─────────────────────────────────────────────────────
    private static void addKpiSection(Document doc, PrintData data,
                                      PdfFont regular, PdfFont bold) {
        addSectionTitle(doc, "I. TONG QUAN", bold);

        Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(16);

        Cell left = kpiCell(bold, regular,
                "Tong doanh thu nam " + data.year,
                fmt(data.totalRev),
                data.growthPct != 0
                        ? (data.growthPct > 0 ? "Tang " : "Giam ") + String.format("%.1f%%", Math.abs(data.growthPct)) + " so voi nam truoc"
                        : "Khong co du lieu nam truoc");

        Cell right = kpiCell(bold, regular,
                "Hop dong hoan thanh",
                String.valueOf(data.totalContracts),
                "Khach hang: " + data.totalCustomers
                        + "  |  DT TB thang: " + fmtSmart(data.avgMonthly));

        table.addCell(left);
        table.addCell(right);
        doc.add(table);

        // Tháng cụ thể (nếu có)
        if (data.month > 0) {
            Table mt = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(16);

            Cell ml = kpiCell(bold, regular,
                    "Doanh thu thang " + data.month,
                    fmtSmart(data.monthRev), "");
            Cell mr = kpiCell(bold, regular,
                    "Hop dong thang " + data.month,
                    String.valueOf(data.monthContracts), "");

            mt.addCell(ml);
            mt.addCell(mr);
            doc.add(mt);
        }
    }

    // ─────────────────────────────────────────────────────
    //  3. BẢNG DOANH THU 12 THÁNG
    // ─────────────────────────────────────────────────────
    private static void addRevenueTable(Document doc, PrintData data,
                                        PdfFont regular, PdfFont bold) {
        if (data.monthlyRevMap == null) return;
        addSectionTitle(doc, "II. DOANH THU THEO THANG (NAM " + data.year + ")", bold);

        Table table = new Table(UnitValue.createPercentArray(new float[]{16,28,28,28}))
                .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(16);

        // Header
        String[] hdrs = {"Thang", "Doanh thu (VND)", "Ty le nam", "So voi thang truoc"};
        for (String h : hdrs) {
            table.addHeaderCell(new Cell().setBackgroundColor(C_PRIMARY)
                    .setBorder(new SolidBorder(C_PRIMARY, 1)).setPadding(7)
                    .add(new Paragraph(h).setFont(bold).setFontSize(9)
                            .setFontColor(ColorConstants.WHITE)
                            .setTextAlignment(TextAlignment.CENTER)));
        }

        double totalRev = data.monthlyRevMap.values().stream().mapToDouble(Double::doubleValue).sum();
        double prevRev  = 0;

        for (int m = 1; m <= 12; m++) {
            double rev = data.monthlyRevMap.getOrDefault(m, 0.0);
            double pct = totalRev > 0 ? (rev / totalRev * 100) : 0;
            String vsLast = prevRev > 0
                    ? String.format("%+.1f%%", (rev - prevRev) / prevRev * 100)
                    : (rev > 0 ? "Dau ky" : "--");

            boolean isHighlight = data.month > 0 && m == data.month;
            DeviceRgb rowBg = isHighlight ? C_HEADER_BG : (m % 2 == 0 ? C_LIGHT : null);

            addRevRow(table, regular, bold,
                    MONTH_LABELS[m-1], fmt(rev),
                    pct > 0 ? String.format("%.1f%%", pct) : "--",
                    vsLast, rowBg, isHighlight);
            prevRev = rev;
        }

        // Total row
        Cell tc1 = new Cell().setBackgroundColor(C_PRIMARY).setBorder(new SolidBorder(C_PRIMARY,1)).setPadding(8)
                .add(new Paragraph("TONG CONG").setFont(bold).setFontSize(9).setFontColor(ColorConstants.WHITE));
        Cell tc2 = new Cell().setBackgroundColor(C_PRIMARY).setBorder(new SolidBorder(C_PRIMARY,1)).setPadding(8)
                .add(new Paragraph(fmt(totalRev)).setFont(bold).setFontSize(10).setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.RIGHT));
        Cell tc3 = new Cell().setBackgroundColor(C_PRIMARY).setBorder(new SolidBorder(C_PRIMARY,1)).setPadding(8)
                .add(new Paragraph("100%").setFont(bold).setFontSize(9).setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER));
        Cell tc4 = new Cell().setBackgroundColor(C_PRIMARY).setBorder(new SolidBorder(C_PRIMARY,1)).setPadding(8)
                .add(new Paragraph("").setFont(bold).setFontSize(9).setFontColor(ColorConstants.WHITE));
        table.addCell(tc1); table.addCell(tc2); table.addCell(tc3); table.addCell(tc4);

        doc.add(table);
    }

    private static void addRevRow(Table table, PdfFont regular, PdfFont bold,
                                  String month, String rev, String pct, String vs,
                                  DeviceRgb bg, boolean highlight) {
        String[] vals = {month, rev, pct, vs};
        for (int i = 0; i < vals.length; i++) {
            Cell c = new Cell().setBorder(new SolidBorder(C_BORDER, 0.5f)).setPadding(6);
            if (bg != null) c.setBackgroundColor(bg);
            Paragraph p = new Paragraph(vals[i]).setFont(highlight ? bold : regular).setFontSize(9);
            if (i == 1) p.setTextAlignment(TextAlignment.RIGHT).setFontColor(C_DARK);
            else if (i == 2) p.setTextAlignment(TextAlignment.CENTER).setFontColor(C_GRAY);
            else if (i == 3) {
                if (vals[i].startsWith("+")) p.setFontColor(C_GREEN);
                else if (vals[i].startsWith("-")) p.setFontColor(C_RED);
                p.setTextAlignment(TextAlignment.CENTER);
            } else {
                p.setFontColor(C_DARK).setFont(bold);
            }
            c.add(p);
            table.addCell(c);
        }
    }

    // ─────────────────────────────────────────────────────
    //  4. TOP 5 XE
    // ─────────────────────────────────────────────────────
    private static void addTopVehicles(Document doc, PrintData data,
                                       PdfFont regular, PdfFont bold) {
        if (data.topVehicles == null || data.topVehicles.isEmpty()) return;
        String period = data.month > 0 ? "Thang " + data.month + "/" + data.year : "Nam " + data.year;
        addSectionTitle(doc, "III. TOP 5 XE DUOC THUE NHIEU NHAT (" + period + ")", bold);

        Table table = new Table(UnitValue.createPercentArray(new float[]{8, 30, 22, 15, 25}))
                .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(16);

        String[] hdrs = {"#", "Ten xe", "Bien so", "Luot thue", "Doanh thu"};
        for (String h : hdrs) {
            table.addHeaderCell(new Cell().setBackgroundColor(C_GREEN)
                    .setBorder(new SolidBorder(C_GREEN, 1)).setPadding(7)
                    .add(new Paragraph(h).setFont(bold).setFontSize(9)
                            .setFontColor(ColorConstants.WHITE)
                            .setTextAlignment(TextAlignment.CENTER)));
        }

        for (VehicleReportRow row : data.topVehicles) {
            addRankCell(table, row.getRank(), bold);
            addDataCell(table, regular, bold, row.getVehicleName(), true, TextAlignment.LEFT, null);
            addDataCell(table, regular, bold, row.getPlateNumber(), false, TextAlignment.CENTER, C_GRAY);
            addDataCell(table, regular, bold, row.getRentalCount() + " luot", false, TextAlignment.CENTER, C_PRIMARY);
            addDataCell(table, regular, bold, fmt(row.getRevenue()), true, TextAlignment.RIGHT, C_RED);
        }
        doc.add(table);
    }

    // ─────────────────────────────────────────────────────
    //  5. TOP 5 NHÂN VIÊN
    // ─────────────────────────────────────────────────────
    private static void addTopStaff(Document doc, PrintData data,
                                    PdfFont regular, PdfFont bold) {
        if (data.topStaff == null || data.topStaff.isEmpty()) return;
        String period = data.month > 0 ? "Thang " + data.month + "/" + data.year : "Nam " + data.year;
        addSectionTitle(doc, "IV. TOP 5 NHAN VIEN XUAT SAC (" + period + ")", bold);

        Table table = new Table(UnitValue.createPercentArray(new float[]{8, 37, 20, 35}))
                .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(16);

        String[] hdrs = {"#", "Ho va ten", "So hop dong", "Doanh thu"};
        for (String h : hdrs) {
            table.addHeaderCell(new Cell().setBackgroundColor(new DeviceRgb(109,40,217))
                    .setBorder(new SolidBorder(new DeviceRgb(109,40,217), 1)).setPadding(7)
                    .add(new Paragraph(h).setFont(bold).setFontSize(9)
                            .setFontColor(ColorConstants.WHITE)
                            .setTextAlignment(TextAlignment.CENTER)));
        }

        for (StaffReportRow row : data.topStaff) {
            addRankCell(table, row.getRank(), bold);
            addDataCell(table, regular, bold, row.getFullName(), true, TextAlignment.LEFT, null);
            addDataCell(table, regular, bold, row.getContractCount() + " HD", false, TextAlignment.CENTER, C_PRIMARY);
            addDataCell(table, regular, bold, fmt(row.getRevenue()), true, TextAlignment.RIGHT, C_RED);
        }
        doc.add(table);
    }

    // ─────────────────────────────────────────────────────
    //  6. TRẠNG THÁI XE
    // ─────────────────────────────────────────────────────
    private static void addVehicleStatus(Document doc, PrintData data,
                                         PdfFont regular, PdfFont bold) {
        if (data.vehStatus == null) return;
        addSectionTitle(doc, "V. TRANG THAI DOI XE (HIEN TAI)", bold);

        Table table = new Table(UnitValue.createPercentArray(new float[]{40, 30, 30}))
                .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(16);

        String[] hdrs = {"Trang thai", "So luong", "Ty le"};
        for (String h : hdrs) {
            table.addHeaderCell(new Cell().setBackgroundColor(new DeviceRgb(71,85,105))
                    .setBorder(new SolidBorder(new DeviceRgb(71,85,105), 1)).setPadding(7)
                    .add(new Paragraph(h).setFont(bold).setFontSize(9)
                            .setFontColor(ColorConstants.WHITE)
                            .setTextAlignment(TextAlignment.CENTER)));
        }

        int total = data.vehStatus.getOrDefault("TOTAL", 1);
        if (total == 0) total = 1;

        String[][] rows = {
                {"San sang (Available)", String.valueOf(data.vehStatus.getOrDefault("AVAILABLE",0))},
                {"Dang thue (Rented)",   String.valueOf(data.vehStatus.getOrDefault("RENTED",0))},
                {"Bao duong (Maintenance)", String.valueOf(data.vehStatus.getOrDefault("MAINTENANCE",0))},
                {"Tong xe hoat dong",    String.valueOf(data.vehStatus.getOrDefault("TOTAL",0))},
        };

        for (String[] r : rows) {
            int count = Integer.parseInt(r[1]);
            double pct = (double) count / total * 100;
            boolean isTotal = r[0].startsWith("Tong");
            DeviceRgb bg = isTotal ? C_LIGHT : null;

            Cell c1 = new Cell().setBorder(new SolidBorder(C_BORDER, 0.5f)).setPadding(7);
            if (bg != null) c1.setBackgroundColor(bg);
            c1.add(new Paragraph(r[0]).setFont(isTotal ? bold : regular).setFontSize(9).setFontColor(C_DARK));

            Cell c2 = new Cell().setBorder(new SolidBorder(C_BORDER, 0.5f)).setPadding(7);
            if (bg != null) c2.setBackgroundColor(bg);
            c2.add(new Paragraph(r[1]).setFont(isTotal ? bold : regular).setFontSize(9)
                    .setFontColor(C_DARK).setTextAlignment(TextAlignment.CENTER));

            Cell c3 = new Cell().setBorder(new SolidBorder(C_BORDER, 0.5f)).setPadding(7);
            if (bg != null) c3.setBackgroundColor(bg);
            c3.add(new Paragraph(isTotal ? "100%" : String.format("%.1f%%", pct))
                    .setFont(regular).setFontSize(9)
                    .setFontColor(C_GRAY).setTextAlignment(TextAlignment.CENTER));

            table.addCell(c1); table.addCell(c2); table.addCell(c3);
        }
        doc.add(table);
    }

    // ─────────────────────────────────────────────────────
    //  7. FOOTER
    // ─────────────────────────────────────────────────────
    private static void addFooter(Document doc, PdfFont regular) {
        doc.add(new LineSeparator(
                new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(0.5f))
                .setStrokeColor(C_BORDER).setMarginTop(10).setMarginBottom(8));
        doc.add(new Paragraph("VehicleRent Pro – He thong quan ly cho thue xe     |     "
                + "Bao cao duoc tao tu dong luc: " + LocalDateTime.now().format(DT_FMT))
                .setFont(regular).setFontSize(8).setFontColor(C_GRAY)
                .setTextAlignment(TextAlignment.CENTER));
    }

    // ─────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────
    private static void addSectionTitle(Document doc, String title, PdfFont bold) {
        doc.add(new Paragraph(title)
                .setFont(bold).setFontSize(11).setFontColor(C_PRIMARY)
                .setMarginTop(10).setMarginBottom(8)
                .setBackgroundColor(C_HEADER_BG).setPadding(6)
                .setBorderLeft(new SolidBorder(C_PRIMARY, 3)));
    }

    private static Cell kpiCell(PdfFont bold, PdfFont regular,
                                String label, String value, String sub) {
        Cell cell = new Cell().setPadding(14)
                .setBackgroundColor(C_LIGHT)
                .setBorder(new SolidBorder(C_BORDER, 1));
        cell.add(new Paragraph(label).setFont(regular).setFontSize(10).setFontColor(C_GRAY).setMarginBottom(4));
        cell.add(new Paragraph(value).setFont(bold).setFontSize(18).setFontColor(C_PRIMARY).setMarginBottom(4));
        if (!sub.isEmpty())
            cell.add(new Paragraph(sub).setFont(regular).setFontSize(9).setFontColor(C_GRAY));
        return cell;
    }

    private static void addRankCell(Table table, int rank, PdfFont bold) {
        String badge = switch (rank) {
            case 1 -> "🥇";
            case 2 -> "🥈";
            case 3 -> "🥉";
            default -> String.valueOf(rank);
        };
        table.addCell(new Cell().setBorder(new SolidBorder(C_BORDER, 0.5f)).setPadding(7)
                .add(new Paragraph(rank <= 3 ? rank + "" : badge)
                        .setFont(bold).setFontSize(11).setFontColor(C_PRIMARY)
                        .setTextAlignment(TextAlignment.CENTER)));
    }

    private static void addDataCell(Table table, PdfFont regular, PdfFont bold,
                                    String text, boolean useBold,
                                    TextAlignment align, DeviceRgb color) {
        Paragraph p = new Paragraph(text)
                .setFont(useBold ? bold : regular).setFontSize(9)
                .setTextAlignment(align)
                .setFontColor(color != null ? color : C_DARK);
        table.addCell(new Cell().setBorder(new SolidBorder(C_BORDER, 0.5f)).setPadding(7).add(p));
    }

    private static String fmt(double amount) {
        return String.format("%,.0f d", amount).replace(",", ".");
    }

    private static String fmtSmart(double amount) {
        if (amount >= 1_000_000_000)
            return String.format("%.1f ty", amount / 1_000_000_000);
        if (amount >= 1_000_000)
            return String.format("%.1f tr", amount / 1_000_000);
        return fmt(amount);
    }

    private static PdfFont loadFont(boolean bold) throws IOException {
        String[] paths = {
                "src/main/resources/fonts/NotoSans-Regular.ttf",
                "src/main/resources/fonts/NotoSans-Bold.ttf",
                "src/main/resources/fonts/arial.ttf",
                "C:/Windows/Fonts/arial.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
        };
        for (String p : paths) {
            File f = new File(p);
            if (f.exists()) {
                try {
                    return PdfFontFactory.createFont(p, PdfEncodings.IDENTITY_H);
                } catch (Exception ignored) {}
            }
        }
        return bold
                ? PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD)
                : PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
    }
}