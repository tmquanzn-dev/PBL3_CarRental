package com.example.rentalcar.utils;

import com.example.rentalcar.bll.CustomerBLL;
import com.example.rentalcar.bll.PenaltyBLL;
import com.example.rentalcar.bll.VehicleBLL;
import com.example.rentalcar.dao.CustomerDAO;
import com.example.rentalcar.dao.PenaltyDAO;
import com.example.rentalcar.dao.VehicleDAO;
import com.example.rentalcar.models.*;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class ContractPrinter {

    private static final DeviceRgb COLOR_PRIMARY    = new DeviceRgb(20, 109, 255);
    private static final DeviceRgb COLOR_DARK       = new DeviceRgb(30, 41, 59);
    private static final DeviceRgb COLOR_GRAY       = new DeviceRgb(100, 116, 139);
    private static final DeviceRgb COLOR_LIGHT_BG   = new DeviceRgb(248, 250, 252);
    private static final DeviceRgb COLOR_GREEN      = new DeviceRgb(16, 185, 129);
    private static final DeviceRgb COLOR_RED        = new DeviceRgb(239, 68, 68);
    private static final DeviceRgb COLOR_YELLOW_BG  = new DeviceRgb(254, 243, 199);
    private static final DeviceRgb COLOR_YELLOW_FG  = new DeviceRgb(180, 83, 9);
    private static final DeviceRgb COLOR_BORDER     = new DeviceRgb(226, 232, 240);

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final CustomerBLL customerBLL = new CustomerBLL();
    private static final VehicleBLL vehicleBLL  = new VehicleBLL();
    private static final PenaltyBLL penaltyBLL  = new PenaltyBLL();

    public static String print(Contracts contract, Window owner) {
        if (contract == null) return null;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Lưu hợp đồng PDF");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files (*.pdf)", "*.pdf"));
        chooser.setInitialFileName("HopDong_" + contract.getCode_contract() + ".pdf");

        File file = chooser.showSaveDialog(owner);
        if (file == null) return null;

        try {
            generatePdf(contract, file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi tạo PDF: " + e.getMessage(), e);
        }
    }

    public static void printToPath(Contracts contract, String outputPath) throws Exception {
        if (contract == null || outputPath == null) return;
        generatePdf(contract, outputPath);
    }

    private static void generatePdf(Contracts contract, String outputPath) throws Exception {
        // Load dữ liệu liên quan
        Customers customer = contract.getId_customer() != null
                ? customerBLL.findById(contract.getId_customer().getId_customer()) : null;
        Vehicles vehicle = contract.getId_vehicle() != null
                ? vehicleBLL.getVehicleById(contract.getId_vehicle().getId_vehicle()) : null;
        List<Penalties> penalties = penaltyBLL.getByContractId(contract.getId_contract());

        // Khởi tạo document
        PdfWriter writer   = new PdfWriter(outputPath);
        PdfDocument pdf    = new PdfDocument(writer);
        Document   doc     = new Document(pdf, PageSize.A4);
        doc.setMargins(36, 36, 36, 36);

        PdfFont fontRegular = loadFont(false);
        PdfFont fontBold    = loadFont(true);

        addHeader(doc, contract, fontRegular, fontBold);

        addPartyInfo(doc, contract, customer, fontRegular, fontBold);

        addVehicleSection(doc, vehicle, contract, fontRegular, fontBold);

        addPricingSection(doc, contract, fontRegular, fontBold);

        if (penalties != null && !penalties.isEmpty()) {
            addPenaltySection(doc, penalties, fontRegular, fontBold);
        }

        addTotalSection(doc, contract, fontRegular, fontBold);

        addTermsSection(doc, fontRegular, fontBold);

        addSignatureSection(doc, contract, customer, fontRegular, fontBold);

        addFooter(doc, fontRegular);
        doc.close();
    }

    //  1. HEADER
    private static void addHeader(Document doc, Contracts contract,
                                  PdfFont regular, PdfFont bold) {
        Paragraph company = new Paragraph("VEHICLERENT PRO")
                .setFont(bold).setFontSize(22)
                .setFontColor(COLOR_PRIMARY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(2);
        doc.add(company);

        Paragraph tagline = new Paragraph("Hệ thống quản lý cho thuê xe máy chuyên nghiệp")
                .setFont(regular).setFontSize(10)
                .setFontColor(COLOR_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(16);
        doc.add(tagline);

        doc.add(new LineSeparator(
                new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(1.5f))
                .setStrokeColor(COLOR_PRIMARY).setMarginBottom(16));

        Paragraph title = new Paragraph("HỢP ĐỒNG THUÊ XE")
                .setFont(bold).setFontSize(18)
                .setFontColor(COLOR_DARK)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(4);
        doc.add(title);

        String createdDate = contract.getStart_datetime() != null
                ? contract.getStart_datetime().format(DATE_FMT)
                : "---";
        Paragraph meta = new Paragraph(
                "Mã hợp đồng: " + contract.getCode_contract()
                        + "     |     Ngày tạo: " + createdDate)
                .setFont(regular).setFontSize(10)
                .setFontColor(COLOR_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(4);
        doc.add(meta);

        String statusText = getStatusText(contract.getStatus());
        DeviceRgb[] statusColors = getStatusColors(contract.getStatus());
        Paragraph status = new Paragraph(statusText)
                .setFont(bold).setFontSize(10)
                .setFontColor(statusColors[1])
                .setBackgroundColor(statusColors[0])
                .setPadding(4).setBorderRadius(new com.itextpdf.layout.properties.BorderRadius(6))
                .setTextAlignment(TextAlignment.CENTER)
                .setMaxWidth(120)
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setMarginBottom(20);
        doc.add(status);
    }

    // =========================================================
    //  2. THÔNG TIN 2 BÊN
    // =========================================================
    private static void addPartyInfo(Document doc, Contracts contract,
                                     Customers customer, PdfFont regular, PdfFont bold) {
        addSectionTitle(doc, "I. THÔNG TIN CÁC BÊN", bold);

        Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(16);

        Cell cellA = new Cell().setPadding(12)
                .setBackgroundColor(COLOR_LIGHT_BG)
                .setBorder(new SolidBorder(COLOR_BORDER, 1));

        cellA.add(new Paragraph("BÊN A: CỬA HÀNG CHO THUÊ")
                .setFont(bold).setFontSize(10).setFontColor(COLOR_PRIMARY).setMarginBottom(6));
        cellA.add(infoRow("Tên cửa hàng:", "VehicleRent Pro", regular, bold));
        cellA.add(infoRow("Địa chỉ:", "Đà Nẵng, Việt Nam", regular, bold));
        cellA.add(infoRow("Điện thoại:", "1900 xxxx", regular, bold));

        // ✅ ĐÃ SỬA: Ưu tiên lấy đúng Nhân viên tạo hợp đồng gốc từ trước
        String staffName = "---";
        if (contract.getId_user() != null && contract.getId_user().getFull_name() != null) {
            staffName = contract.getId_user().getFull_name();
        } else if (AppSession.getCurrentUser() != null) {
            staffName = AppSession.getCurrentUser().getFull_name();
        }
        cellA.add(infoRow("Nhân viên lập:", staffName, regular, bold));

        Cell cellB = new Cell().setPadding(12)
                .setBorder(new SolidBorder(COLOR_BORDER, 1));

        cellB.add(new Paragraph("BÊN B: KHÁCH HÀNG THUÊ XE")
                .setFont(bold).setFontSize(10).setFontColor(COLOR_PRIMARY).setMarginBottom(6));

        if (customer != null) {
            cellB.add(infoRow("Họ và tên:", customer.getFull_name(), regular, bold));
            cellB.add(infoRow("Số CCCD:", customer.getCccd(), regular, bold));
            cellB.add(infoRow("Điện thoại:", nvl(customer.getPhone()), regular, bold));
            cellB.add(infoRow("Địa chỉ:", nvl(customer.getAddress()), regular, bold));
        } else {
            cellB.add(new Paragraph("(Không có thông tin khách hàng)")
                    .setFont(regular).setFontSize(9).setFontColor(COLOR_GRAY));
        }

        table.addCell(cellA);
        table.addCell(cellB);
        doc.add(table);
    }

    // =========================================================
    //  3. THÔNG TIN XE
    // =========================================================
    private static void addVehicleSection(Document doc, Vehicles vehicle,
                                          Contracts contract, PdfFont regular, PdfFont bold) {
        addSectionTitle(doc, "II. THÔNG TIN PHƯƠNG TIỆN", bold);

        if (vehicle == null) {
            doc.add(new Paragraph("(Không có thông tin xe)")
                    .setFont(regular).setFontColor(COLOR_GRAY).setMarginBottom(16));
            return;
        }

        Table table = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(16);

        String[] headers = {"Biển số xe", "Hãng xe / Model", "Loại xe", "Màu sắc"};
        for (String h : headers) {
            table.addHeaderCell(new Cell()
                    .setBackgroundColor(COLOR_PRIMARY)
                    .setBorder(new SolidBorder(COLOR_PRIMARY, 1))
                    .setPadding(8)
                    .add(new Paragraph(h).setFont(bold).setFontSize(9).setFontColor(ColorConstants.WHITE)));
        }

        addTableCell(table, vehicle.getCode_vehicle(), regular, true);
        addTableCell(table, vehicle.getBrand() + " " + vehicle.getModel(), regular, true);
        addTableCell(table, nvl(vehicle.getVehicle_type()), regular, false);
        addTableCell(table, nvl(vehicle.getColor()), regular, false);

        doc.add(table);

        Table table2 = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(16);

        String[] headers2 = {"KM lúc giao xe", "KM lúc trả xe", "Xăng lúc giao (%)", "Xăng lúc trả (%)"};
        for (String h : headers2) {
            table2.addHeaderCell(new Cell()
                    .setBackgroundColor(new DeviceRgb(71, 141, 255))
                    .setBorder(new SolidBorder(new DeviceRgb(71, 141, 255), 1))
                    .setPadding(8)
                    .add(new Paragraph(h).setFont(bold).setFontSize(9).setFontColor(ColorConstants.WHITE)));
        }

        addTableCell(table2, contract.getKm_start() + " km", regular, false);
        addTableCell(table2, contract.getKm_end() > 0 ? contract.getKm_end() + " km" : "---", regular, false);
        addTableCell(table2, contract.getFuel_start() + "%", regular, false);
        addTableCell(table2, contract.getFuel_end() > 0 ? contract.getFuel_end() + "%" : "---", regular, false);

        doc.add(table2);
    }

    // =========================================================
    //  4. THỜI GIAN & GIÁ
    // =========================================================
    private static void addPricingSection(Document doc, Contracts contract,
                                          PdfFont regular, PdfFont bold) {
        addSectionTitle(doc, "III. THỜI GIAN THUÊ & ĐƠN GIÁ", bold);

        Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(16);

        Cell cellLeft = new Cell().setPadding(12)
                .setBackgroundColor(COLOR_LIGHT_BG)
                .setBorder(new SolidBorder(COLOR_BORDER, 1));
        cellLeft.add(new Paragraph("Thời gian thuê").setFont(bold).setFontSize(10)
                .setFontColor(COLOR_DARK).setMarginBottom(8));

        String start = contract.getStart_datetime() != null ? contract.getStart_datetime().format(FMT) : "---";
        String end   = contract.getEnd_datetime()   != null ? contract.getEnd_datetime().format(FMT)   : "---";
        String ret   = contract.getReturn_datetime() != null ? contract.getReturn_datetime().format(FMT) : "Chưa trả";

        cellLeft.add(infoRow("Ngày nhận xe:", start, regular, bold));
        cellLeft.add(infoRow("Ngày trả dự kiến:", end, regular, bold));
        cellLeft.add(infoRow("Ngày trả thực tế:", ret, regular, bold));

        if (contract.getStart_datetime() != null && contract.getEnd_datetime() != null) {
            long hours = java.time.Duration.between(
                    contract.getStart_datetime(), contract.getEnd_datetime()).toHours();
            long days  = hours / 24;
            long remH  = hours % 24;
            String dur = days > 0 ? days + " ngày" + (remH > 0 ? " " + remH + " giờ" : "") : hours + " giờ";
            cellLeft.add(infoRow("Tổng thời gian:", dur, regular, bold));
        }

        Cell cellRight = new Cell().setPadding(12)
                .setBorder(new SolidBorder(COLOR_BORDER, 1));
        cellRight.add(new Paragraph("Đặt cọc").setFont(bold).setFontSize(10)
                .setFontColor(COLOR_DARK).setMarginBottom(8));

        String depositType = contract.getDeposit_type() != null
                ? switch (contract.getDeposit_type()) {
            case TIEN_MAT -> "Tiền mặt";
            case GIAY_TO  -> "Giấy tờ";
            default       -> "Khác";
        } : "---";
        cellRight.add(infoRow("Hình thức cọc:", depositType, regular, bold));
        cellRight.add(infoRow("Số tiền cọc:", formatMoney(contract.getDeposit_amount()), regular, bold));

        String payStatus = "---";
        if (contract.getPayment_status() != null) {
            payStatus = switch (contract.getPayment_status()) {
                case CHUA_THANH_TOAN   -> "Chưa thanh toán";
                case THANH_TOAN_1_PHAN -> "Thanh toán một phần";
                case DA_THANH_TOAN     -> "Đã thanh toán đủ";
            };
        }
        cellRight.add(infoRow("Trạng thái TT:", payStatus, regular, bold));

        if (contract.getId_voucher() != null) {
            cellRight.add(infoRow("Voucher:", contract.getId_voucher().getCode_vouchers(), regular, bold));
        }

        table.addCell(cellLeft);
        table.addCell(cellRight);
        doc.add(table);
    }

    // =========================================================
    //  5. PHẠT
    // =========================================================
    private static void addPenaltySection(Document doc, List<Penalties> penalties,
                                          PdfFont regular, PdfFont bold) {
        addSectionTitle(doc, "IV. CÁC KHOẢN PHẠT", bold);

        Table table = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(16);

        table.addHeaderCell(new Cell().setBackgroundColor(COLOR_YELLOW_BG)
                .setBorder(new SolidBorder(COLOR_BORDER, 1)).setPadding(8)
                .add(new Paragraph("Loại phạt").setFont(bold).setFontSize(9).setFontColor(COLOR_YELLOW_FG)));
        table.addHeaderCell(new Cell().setBackgroundColor(COLOR_YELLOW_BG)
                .setBorder(new SolidBorder(COLOR_BORDER, 1)).setPadding(8)
                .add(new Paragraph("Số tiền").setFont(bold).setFontSize(9).setFontColor(COLOR_YELLOW_FG)
                        .setTextAlignment(TextAlignment.RIGHT)));

        double totalPenalty = 0;
        for (Penalties p : penalties) {
            String type = p.getPenalty_type() != null
                    ? switch (p.getPenalty_type()) {
                case QUA_GIO            -> "Trả xe trễ giờ";
                case XANG               -> "Thiếu xăng";
                case HU_HONG            -> "Hư hỏng phương tiện";
                case VI_PHAM_GIAO_THONG -> "Vi phạm giao thông";
            } : "Khác";

            table.addCell(new Cell().setBorder(new SolidBorder(COLOR_BORDER, 0.5f)).setPadding(7)
                    .add(new Paragraph(type).setFont(regular).setFontSize(9)));
            table.addCell(new Cell().setBorder(new SolidBorder(COLOR_BORDER, 0.5f)).setPadding(7)
                    .add(new Paragraph(formatMoney(p.getAmount()))
                            .setFont(bold).setFontSize(9).setFontColor(COLOR_RED)
                            .setTextAlignment(TextAlignment.RIGHT)));
            totalPenalty += p.getAmount();
        }

        table.addCell(new Cell().setBackgroundColor(new DeviceRgb(254, 226, 226))
                .setBorder(new SolidBorder(COLOR_BORDER, 1)).setPadding(8)
                .add(new Paragraph("TỔNG PHẠT").setFont(bold).setFontSize(9).setFontColor(COLOR_RED)));
        table.addCell(new Cell().setBackgroundColor(new DeviceRgb(254, 226, 226))
                .setBorder(new SolidBorder(COLOR_BORDER, 1)).setPadding(8)
                .add(new Paragraph(formatMoney(totalPenalty))
                        .setFont(bold).setFontSize(10).setFontColor(COLOR_RED)
                        .setTextAlignment(TextAlignment.RIGHT)));

        doc.add(table);
    }

    // =========================================================
    //  6. TỔNG THANH TOÁN
    // =========================================================
    private static void addTotalSection(Document doc, Contracts contract,
                                        PdfFont regular, PdfFont bold) {
        addSectionTitle(doc, "V. TỔNG THANH TOÁN", bold);

        Table table = new Table(UnitValue.createPercentArray(new float[]{65, 35}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(16);

        addSummaryRow(table, "Tiền thuê cơ bản:", formatMoney(contract.getBase_price()),
                regular, bold, false);

        if (contract.getDiscount_amount() > 0) {
            addSummaryRow(table, "Giảm giá (voucher):", "- " + formatMoney(contract.getDiscount_amount()),
                    regular, bold, false);
        }

        List<Penalties> penalties = penaltyBLL.getByContractId(contract.getId_contract());
        if (penalties != null) {
            double totalPenalty = penalties.stream().mapToDouble(Penalties::getAmount).sum();
            if (totalPenalty > 0) {
                addSummaryRow(table, "Tổng tiền phạt:", formatMoney(totalPenalty),
                        regular, bold, false);
            }
        }

        Cell labelCell = new Cell().setBackgroundColor(COLOR_PRIMARY)
                .setBorder(new SolidBorder(COLOR_PRIMARY, 1)).setPadding(12)
                .add(new Paragraph("TỔNG CỘNG CẦN THANH TOÁN")
                        .setFont(bold).setFontSize(11).setFontColor(ColorConstants.WHITE));
        Cell valueCell = new Cell().setBackgroundColor(COLOR_PRIMARY)
                .setBorder(new SolidBorder(COLOR_PRIMARY, 1)).setPadding(12)
                .add(new Paragraph(formatMoney(contract.getTotal_price()))
                        .setFont(bold).setFontSize(14).setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.RIGHT));

        table.addCell(labelCell);
        table.addCell(valueCell);
        doc.add(table);
    }

    // =========================================================
    //  7. ĐIỀU KHOẢN
    // =========================================================
    private static void addTermsSection(Document doc, PdfFont regular, PdfFont bold) {
        addSectionTitle(doc, "VI. ĐIỀU KHOẢN CHUNG", bold);

        String[] terms = {
                "1. Bên B cam kết sử dụng xe đúng mục đích, không cho người khác thuê lại.",
                "2. Bên B chịu trách nhiệm bồi thường toàn bộ thiệt hại nếu xe bị hư hỏng do lỗi của Bên B.",
                "3. Bên B phải trả xe đúng thời hạn. Trễ mỗi giờ sẽ bị tính phạt theo quy định.",
                "4. Bên B phải trả xe với lượng xăng bằng hoặc nhiều hơn lúc nhận. Nếu thiếu sẽ bị tính phí.",
                "5. Mọi vi phạm giao thông trong thời gian thuê xe do Bên B hoàn toàn chịu trách nhiệm.",
                "6. Hợp đồng này có giá trị pháp lý khi có chữ ký của cả hai bên.",
        };

        Table table = new Table(1).setWidth(UnitValue.createPercentValue(100)).setMarginBottom(16);
        Cell cell = new Cell().setBackgroundColor(COLOR_LIGHT_BG)
                .setBorder(new SolidBorder(COLOR_BORDER, 1)).setPadding(12);
        for (String term : terms) {
            cell.add(new Paragraph(term).setFont(regular).setFontSize(9)
                    .setFontColor(COLOR_DARK).setMarginBottom(4));
        }
        table.addCell(cell);
        doc.add(table);
    }

    // =========================================================
    //  8. KÝ TÊN
    // =========================================================
    private static void addSignatureSection(Document doc, Contracts contract,
                                            Customers customer, PdfFont regular, PdfFont bold) {
        addSectionTitle(doc, "VII. XÁC NHẬN & CHỮ KÝ", bold);

        String location = "Đà Nẵng, ngày _____ tháng _____ năm _____";
        doc.add(new Paragraph(location).setFont(regular).setFontSize(9)
                .setFontColor(COLOR_GRAY).setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(20));

        Table sigTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(16);

        // Bên A
        Cell sigA = new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER).setPadding(12);
        sigA.add(new Paragraph("BÊN A (CỬA HÀNG)").setFont(bold).setFontSize(10)
                .setFontColor(COLOR_PRIMARY).setTextAlignment(TextAlignment.CENTER));
        sigA.add(new Paragraph("(Ký, ghi rõ họ tên)").setFont(regular).setFontSize(8)
                .setFontColor(COLOR_GRAY).setTextAlignment(TextAlignment.CENTER).setMarginBottom(50));

        // ✅ ĐÃ SỬA: Bộ ký tên ưu tiên lấy đúng nhân viên lập hợp đồng gốc
        String staffName = "_______________";
        if (contract.getId_user() != null && contract.getId_user().getFull_name() != null) {
            staffName = contract.getId_user().getFull_name();
        } else if (AppSession.getCurrentUser() != null) {
            staffName = AppSession.getCurrentUser().getFull_name();
        }
        sigA.add(new Paragraph(staffName).setFont(bold).setFontSize(10)
                .setFontColor(COLOR_DARK).setTextAlignment(TextAlignment.CENTER));

        // Bên B
        Cell sigB = new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER).setPadding(12);
        sigB.add(new Paragraph("BÊN B (KHÁCH HÀNG)").setFont(bold).setFontSize(10)
                .setFontColor(COLOR_PRIMARY).setTextAlignment(TextAlignment.CENTER));
        sigB.add(new Paragraph("(Ký, ghi rõ họ tên)").setFont(regular).setFontSize(8)
                .setFontColor(COLOR_GRAY).setTextAlignment(TextAlignment.CENTER).setMarginBottom(50));
        String custName = (customer != null && customer.getFull_name() != null)
                ? customer.getFull_name() : "_______________";
        sigB.add(new Paragraph(custName).setFont(bold).setFontSize(10)
                .setFontColor(COLOR_DARK).setTextAlignment(TextAlignment.CENTER));

        sigTable.addCell(sigA);
        sigTable.addCell(sigB);
        doc.add(sigTable);
    }

    // =========================================================
    //  9. FOOTER
    // =========================================================
    private static void addFooter(Document doc, PdfFont regular) {
        doc.add(new LineSeparator(
                new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(0.5f))
                .setStrokeColor(COLOR_BORDER).setMarginTop(10).setMarginBottom(8));

        String printedAt = "In lúc: " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        doc.add(new Paragraph("VehicleRent Pro – Hệ thống quản lý cho thuê xe     |     " + printedAt)
                .setFont(regular).setFontSize(8).setFontColor(COLOR_GRAY)
                .setTextAlignment(TextAlignment.CENTER));
    }

    // =========================================================
    //  HELPER METHODS
    // =========================================================

    private static PdfFont loadFont(boolean bold) throws IOException {
        String[] fontPaths = {
                "src/main/resources/fonts/NotoSans-Regular.ttf",
                "src/main/resources/fonts/NotoSans-Bold.ttf",
                "C:/Windows/Fonts/arial.ttf",
                "C:/Windows/Fonts/times.ttf",
                "/Library/Fonts/Arial.ttf",
                "/System/Library/Fonts/Helvetica.ttc",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf"
        };

        for (String path : fontPaths) {
            java.io.File f = new java.io.File(path);
            if (f.exists()) {
                try {
                    return PdfFontFactory.createFont(path, PdfEncodings.IDENTITY_H);
                } catch (Exception ignored) {}
            }
        }

        return bold
                ? PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD)
                : PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
    }

    private static void addSectionTitle(Document doc, String title, PdfFont bold) {
        String safeTitle = (title != null) ? title : "";
        doc.add(new Paragraph(safeTitle)
                .setFont(bold).setFontSize(11)
                .setFontColor(COLOR_PRIMARY)
                .setMarginTop(10).setMarginBottom(8)
                .setBackgroundColor(new DeviceRgb(239, 246, 255))
                .setPadding(6)
                .setBorderLeft(new SolidBorder(COLOR_PRIMARY, 3)));
    }

    private static Paragraph infoRow(String label, String value, PdfFont regular, PdfFont bold) {
        String safeLabel = (label != null) ? label : "";
        String safeValue = (value != null && !value.isBlank()) ? value : "---";

        Text labelText = new Text(safeLabel + " ").setFont(bold).setFontSize(9).setFontColor(COLOR_GRAY);
        Text valueText = new Text(safeValue).setFont(regular).setFontSize(9).setFontColor(COLOR_DARK);
        return new Paragraph().add(labelText).add(valueText).setMarginBottom(4);
    }

    private static void addTableCell(Table table, String text, PdfFont font, boolean highlight) {
        String safeText = (text != null) ? text : "---";
        Cell cell = new Cell().setBorder(new SolidBorder(COLOR_BORDER, 0.5f)).setPadding(7);
        if (highlight) cell.setBackgroundColor(new DeviceRgb(239, 246, 255));
        cell.add(new Paragraph(safeText).setFont(font).setFontSize(9).setFontColor(COLOR_DARK));
        table.addCell(cell);
    }

    private static void addSummaryRow(Table table, String label, String value,
                                      PdfFont regular, PdfFont bold, boolean isTotal) {
        String safeLabel = (label != null) ? label : "";
        String safeValue = (value != null) ? value : "0 VND";

        Cell c1 = new Cell().setBorder(new SolidBorder(COLOR_BORDER, 0.5f)).setPadding(8)
                .add(new Paragraph(safeLabel).setFont(isTotal ? bold : regular).setFontSize(9).setFontColor(COLOR_DARK));
        Cell c2 = new Cell().setBorder(new SolidBorder(COLOR_BORDER, 0.5f)).setPadding(8)
                .add(new Paragraph(safeValue).setFont(bold).setFontSize(10)
                        .setFontColor(isTotal ? COLOR_RED : COLOR_DARK)
                        .setTextAlignment(TextAlignment.RIGHT));
        table.addCell(c1);
        table.addCell(c2);
    }

    private static String formatMoney(double amount) {
        return String.format("%,.0f VND", amount).replace(",", ".");
    }

    private static String nvl(String s) {
        return (s != null && !s.isBlank()) ? s : "---";
    }

    private static String getStatusText(StatusContracts status) {
        if (status == null) return "---";
        return switch (status) {
            case DANG_THUE  -> "DANG THUE";
            case QUA_HAN    -> "QUA HAN";
            case HOAN_THANH -> "HOAN THANH";
            case DA_HUY     -> "DA HUY";
        };
    }

    private static DeviceRgb[] getStatusColors(StatusContracts status) {
        if (status == null) return new DeviceRgb[]{COLOR_LIGHT_BG, COLOR_GRAY};
        return switch (status) {
            case DANG_THUE  -> new DeviceRgb[]{new DeviceRgb(219, 234, 254), new DeviceRgb(29, 78, 216)};
            case QUA_HAN    -> new DeviceRgb[]{new DeviceRgb(254, 243, 199), COLOR_YELLOW_FG};
            case HOAN_THANH -> new DeviceRgb[]{new DeviceRgb(220, 252, 231), new DeviceRgb(21, 128, 61)};
            case DA_HUY     -> new DeviceRgb[]{COLOR_LIGHT_BG, COLOR_GRAY};
        };
    }
}