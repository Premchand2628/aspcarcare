package com.carwash.bookingservice.service;

import com.carwash.bookingservice.entity.Booking;
import com.carwash.bookingservice.entity.Invoice;
import com.carwash.bookingservice.repository.InvoiceRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.font.constants.StandardFonts;

import com.carwash.otplogin.entity.User;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class InvoicePdfService {

    private final InvoiceRepository invoiceRepository;

    public InvoicePdfService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(26, 35, 126);   // #1a237e
    private static final DeviceRgb LIGHT_BG = new DeviceRgb(245, 245, 250);      // #f5f5fa
    private static final DeviceRgb BORDER_COLOR = new DeviceRgb(200, 200, 210);   // #c8c8d2
    private static final DeviceRgb SUCCESS_COLOR = new DeviceRgb(46, 125, 50);    // #2e7d32
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public byte[] generateInvoice(Booking booking, User user) {
        // Reuse existing invoice or create new one
        Invoice invoice = invoiceRepository.findByBookingId(booking.getId())
                .orElseGet(() -> {
                    Invoice inv = Invoice.builder()
                            .bookingId(booking.getId())
                            .invoiceNumber(generateInvoiceNumber())
                            .firstName(user != null ? user.getFirstName() : null)
                            .lastName(user != null ? user.getLastName() : null)
                            .phone(booking.getPhone())
                            .email(user != null ? user.getEmail() : null)
                            .invoiceDate(booking.getCreatedAt() != null ? booking.getCreatedAt() : LocalDateTime.now())
                            .build();
                    return invoiceRepository.save(inv);
                });

        String invoiceNo = invoice.getInvoiceNumber();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf, PageSize.A4);
            doc.setMargins(30, 40, 30, 40);

            PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            addHeader(doc, bold, regular, booking, invoiceNo);

            // ── Customer & Booking Info ──
            addInfoSection(doc, bold, regular, booking, user);

            // ── Service Details ──
            addServiceDetails(doc, bold, regular, booking);

            // ── Price Breakdown Table ──
            addPriceBreakdown(doc, bold, regular, booking);

            // ── Footer ──
            addFooter(doc, regular);

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }

    private void addHeader(Document doc, PdfFont bold, PdfFont regular, Booking booking, String invoiceNo) {
        // Company Name
        doc.add(new Paragraph("ASP Care")
                .setFont(bold).setFontSize(26).setFontColor(PRIMARY_COLOR)
                .setMarginBottom(2));

        doc.add(new Paragraph("Professional Car Wash Services")
                .setFont(regular).setFontSize(10).setFontColor(ColorConstants.GRAY)
                .setMarginBottom(8));

        // INVOICE title + Invoice number row
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth().setMarginBottom(4);

        headerTable.addCell(new Cell().add(
                        new Paragraph("INVOICE")
                                .setFont(bold).setFontSize(20).setFontColor(PRIMARY_COLOR))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.LEFT));

        headerTable.addCell(new Cell().add(
                        new Paragraph("Invoice #: " + invoiceNo)
                                .setFont(regular).setFontSize(11).setFontColor(ColorConstants.DARK_GRAY))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT));

        doc.add(headerTable);

        // Date
        String invoiceDate = booking.getCreatedAt() != null
                ? booking.getCreatedAt().format(DATE_FMT)
                : "N/A";
        doc.add(new Paragraph("Date: " + invoiceDate)
                .setFont(regular).setFontSize(10).setFontColor(ColorConstants.DARK_GRAY)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(4));

        // Divider
        doc.add(new Div()
                .setHeight(2)
                .setBackgroundColor(PRIMARY_COLOR)
                .setMarginBottom(16));
    }

    private void addInfoSection(Document doc, PdfFont bold, PdfFont regular, Booking booking, User user) {
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth().setMarginBottom(16);

        // Left: Customer Info
        Cell customerCell = new Cell().setBorder(Border.NO_BORDER).setPaddingRight(20);
        customerCell.add(new Paragraph("CUSTOMER DETAILS")
                .setFont(bold).setFontSize(10).setFontColor(PRIMARY_COLOR).setMarginBottom(6));
        String fullName = buildFullName(user);
        customerCell.add(infoRow(regular, "Name", fullName));
        customerCell.add(infoRow(regular, "Email", user != null ? user.getEmail() : null));
        customerCell.add(infoRow(regular, "Phone", booking.getPhone()));
        customerCell.add(infoRow(regular, "Car Type", booking.getCarType()));
        customerCell.add(infoRow(regular, "Car Number", booking.getCarNumber()));
        infoTable.addCell(customerCell);

        // Right: Booking Info
        Cell bookingCell = new Cell().setBorder(Border.NO_BORDER).setPaddingLeft(20);
        bookingCell.add(new Paragraph("BOOKING DETAILS")
                .setFont(bold).setFontSize(10).setFontColor(PRIMARY_COLOR).setMarginBottom(6));

        String bookingDate = booking.getBookingDate() != null
                ? booking.getBookingDate().format(DATE_FMT) : "N/A";
        bookingCell.add(infoRow(regular, "Booking Date", bookingDate));
        bookingCell.add(infoRow(regular, "Time Slot", booking.getTimeSlot()));
        bookingCell.add(infoRow(regular, "Booking Code", booking.getBookingCode()));
        infoTable.addCell(bookingCell);

        doc.add(infoTable);
    }

    private Paragraph infoRow(PdfFont font, String label, String value) {
        String displayValue = (value != null && !value.isBlank()) ? value : "N/A";
        return new Paragraph(label + ":  " + displayValue)
                .setFont(font).setFontSize(10).setFontColor(ColorConstants.DARK_GRAY)
                .setMarginBottom(3);
    }

    private void addServiceDetails(Document doc, PdfFont bold, PdfFont regular, Booking booking) {
        doc.add(new Paragraph("SERVICE DETAILS")
                .setFont(bold).setFontSize(11).setFontColor(PRIMARY_COLOR).setMarginBottom(8));

        Table serviceTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .useAllAvailableWidth()
                .setMarginBottom(16);

        addServiceRow(serviceTable, bold, regular, "Service Type", formatServiceType(booking.getServiceType()));
        addServiceRow(serviceTable, bold, regular, "Wash Type", booking.getWashType());
        addServiceRow(serviceTable, bold, regular, "Centre", booking.getCentreName());

        if (booking.getAddress() != null && !booking.getAddress().isBlank()) {
            addServiceRow(serviceTable, bold, regular, "Address", booking.getAddress());
        }

        if (Boolean.TRUE.equals(booking.getSubscriptionRedeemed())) {
            addServiceRow(serviceTable, bold, regular, "Subscription", "Redeemed ✓");
        }

        if (Boolean.TRUE.equals(booking.getWaterProvided())) {
            addServiceRow(serviceTable, bold, regular, "Water", "Provided by customer");
        }

        doc.add(serviceTable);
    }

    private void addServiceRow(Table table, PdfFont bold, PdfFont regular, String label, String value) {
        table.addCell(new Cell()
                .add(new Paragraph(label).setFont(bold).setFontSize(10).setFontColor(ColorConstants.DARK_GRAY))
                .setBackgroundColor(LIGHT_BG)
                .setBorderBottom(new SolidBorder(BORDER_COLOR, 0.5f))
                .setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER)
                .setPadding(6));

        String displayValue = (value != null && !value.isBlank()) ? value : "N/A";
        table.addCell(new Cell()
                .add(new Paragraph(displayValue).setFont(regular).setFontSize(10))
                .setBorderBottom(new SolidBorder(BORDER_COLOR, 0.5f))
                .setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER)
                .setPadding(6));
    }

    private void addPriceBreakdown(Document doc, PdfFont bold, PdfFont regular, Booking booking) {
        doc.add(new Paragraph("PRICE BREAKDOWN")
                .setFont(bold).setFontSize(11).setFontColor(PRIMARY_COLOR).setMarginBottom(8));

        Table priceTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(16);

        BigDecimal originalAmount = safeAmount(booking.getOriginalAmount());
        BigDecimal payableAmount = safeAmount(booking.getPayableAmount());
        BigDecimal waterDiscount = safeAmount(booking.getWaterDiscountApplied());
        BigDecimal signupBonus = BigDecimal.ZERO; // Not directly on entity; computed from membershipDiscount if applicable

        // Sub Total
        addPriceRow(priceTable, regular, "Sub Total", formatCurrency(originalAmount), false);

        // Membership/Water discount
        BigDecimal totalDiscount = originalAmount.subtract(payableAmount);
        if (waterDiscount.compareTo(BigDecimal.ZERO) > 0) {
            addPriceRow(priceTable, regular, "Water Discount", "- " + formatCurrency(waterDiscount), false);
        }

        BigDecimal otherDiscount = totalDiscount.subtract(waterDiscount);
        if (otherDiscount.compareTo(BigDecimal.ZERO) > 0) {
            addPriceRow(priceTable, regular, "Membership Discount", "- " + formatCurrency(otherDiscount), false);
        }

        if (booking.getDiscountPercentApplied() != null && booking.getDiscountPercentApplied().compareTo(BigDecimal.ZERO) > 0) {
            addPriceRow(priceTable, regular, "Discount Applied",
                    booking.getDiscountPercentApplied().stripTrailingZeros().toPlainString() + "%", false);
        }

        if (Boolean.TRUE.equals(booking.getFreeApplied())) {
            addPriceRow(priceTable, regular, "Free Wash Applied", "Yes", false);
        }

        // Divider row
        priceTable.addCell(new Cell(1, 2)
                .setHeight(1).setBackgroundColor(PRIMARY_COLOR)
                .setBorder(Border.NO_BORDER)
                .setPaddingTop(4).setPaddingBottom(4));

        // Grand Total
        addPriceRow(priceTable, bold, "Grand Total", formatCurrency(payableAmount), true);

        doc.add(priceTable);
    }

    private void addPriceRow(Table table, PdfFont font, String label, String value, boolean isTotal) {
        DeviceRgb bg = isTotal ? LIGHT_BG : ColorConstants.WHITE instanceof DeviceRgb ? (DeviceRgb) ColorConstants.WHITE : new DeviceRgb(255, 255, 255);

        table.addCell(new Cell()
                .add(new Paragraph(label).setFont(font).setFontSize(isTotal ? 12 : 10)
                        .setFontColor(isTotal ? PRIMARY_COLOR : ColorConstants.DARK_GRAY))
                .setBackgroundColor(bg)
                .setBorder(Border.NO_BORDER)
                .setPadding(6));

        table.addCell(new Cell()
                .add(new Paragraph(value).setFont(font).setFontSize(isTotal ? 12 : 10)
                        .setFontColor(isTotal ? PRIMARY_COLOR : ColorConstants.DARK_GRAY))
                .setBackgroundColor(bg)
                .setBorder(Border.NO_BORDER)
                .setPadding(6)
                .setTextAlignment(TextAlignment.RIGHT));
    }

    private void addFooter(Document doc, PdfFont regular) {
        doc.add(new Div()
                .setHeight(1)
                .setBackgroundColor(BORDER_COLOR)
                .setMarginBottom(10));

        doc.add(new Paragraph("Thank you for choosing ASP Care!")
                .setFont(regular).setFontSize(10)
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(4));

        doc.add(new Paragraph("This is a computer-generated invoice and does not require a signature.")
                .setFont(regular).setFontSize(8)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));
    }

    private String formatServiceType(String serviceType) {
        if (serviceType == null) return "N/A";
        switch (serviceType.toUpperCase()) {
            case "HOME": return "Home Service";
            case "SELF_DRIVE": return "Self Drive";
            case "ASP_CARE": return "ASP Care Centre";
            default: return serviceType;
        }
    }

    private String formatCurrency(BigDecimal amount) {
        return "₹" + amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private BigDecimal safeAmount(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }

    private String buildFullName(User user) {
        if (user == null) return null;
        String first = user.getFirstName();
        String last = user.getLastName();
        if (first == null && last == null) return null;
        StringBuilder sb = new StringBuilder();
        if (first != null && !first.isBlank()) sb.append(first.trim());
        if (last != null && !last.isBlank()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(last.trim());
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    public String getInvoiceNumber(Long bookingId) {
        return invoiceRepository.findByBookingId(bookingId)
                .map(Invoice::getInvoiceNumber)
                .orElse(null);
    }

    private String generateInvoiceNumber() {
        String hex = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12).toUpperCase();
        return "INV-" + hex;
    }
}
