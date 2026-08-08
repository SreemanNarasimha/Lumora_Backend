package com.example.demo.service;

import com.example.demo.entity.Address;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.User;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class InvoiceService {

    public byte[] generateInvoice(Order order) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();
            
            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, Color.BLACK);
            Font companyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);

            // Title
            Paragraph title = new Paragraph("INVOICE", titleFont);
            title.setAlignment(Element.ALIGN_RIGHT);
            document.add(title);
            
            // Company Info
            Paragraph companyName = new Paragraph("Lumora", companyFont);
            document.add(companyName);
            document.add(new Paragraph("123 Fashion Street", normalFont));
            document.add(new Paragraph("Mumbai, Maharashtra, 400001", normalFont));
            document.add(new Paragraph("Email: support@lumora.com", normalFont));
            document.add(Chunk.NEWLINE);

            // Invoice details
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            String orderDate = order.getCreatedAt() != null ? order.getCreatedAt().format(formatter) : "";
            
            infoTable.addCell(new Phrase("Invoice Number: LMR-" + order.getOrderId(), boldFont));
            
            PdfPCell dateCell = new PdfPCell(new Phrase("Date: " + orderDate, normalFont));
            dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            dateCell.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(dateCell);
            
            infoTable.addCell(new Phrase("Payment Status: " + order.getPaymentStatus(), normalFont));
            
            PdfPCell statusCell = new PdfPCell(new Phrase("Order Status: " + order.getStatus(), normalFont));
            statusCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            statusCell.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(statusCell);
            
            document.add(infoTable);
            document.add(Chunk.NEWLINE);

            // Billing/Shipping Address
            document.add(new Paragraph("Bill To / Ship To:", headerFont));
            User user = order.getUser();
            Address address = order.getAddress();
            if (user != null) {
                document.add(new Paragraph(user.getFullName() != null ? user.getFullName() : user.getUsername(), normalFont));
                document.add(new Paragraph("Email: " + user.getEmail(), normalFont));
            }
            if (address != null) {
                document.add(new Paragraph(address.getLine1() + (address.getLine2() != null && !address.getLine2().isEmpty() ? ", " + address.getLine2() : ""), normalFont));
                document.add(new Paragraph(address.getCity() + ", " + address.getState() + " - " + address.getPostalCode(), normalFont));
                document.add(new Paragraph(address.getCountry(), normalFont));
            }
            document.add(Chunk.NEWLINE);

            // Items Table
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4f, 1f, 2f, 2f});
            
            // Table Header
            String[] headers = {"Item", "Qty", "Price per Unit (INR)", "Total (INR)"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                cell.setPadding(5);
                table.addCell(cell);
            }
            
            // Table Body
            if (order.getOrderItems() != null) {
                for (OrderItem item : order.getOrderItems()) {
                    PdfPCell nameCell = new PdfPCell(new Phrase(item.getProductNameSnapshot(), normalFont));
                    nameCell.setPadding(5);
                    table.addCell(nameCell);
                    
                    PdfPCell qtyCell = new PdfPCell(new Phrase(String.valueOf(item.getQuantity()), normalFont));
                    qtyCell.setPadding(5);
                    table.addCell(qtyCell);
                    
                    PdfPCell priceCell = new PdfPCell(new Phrase(String.valueOf(item.getPricePerUnit()), normalFont));
                    priceCell.setPadding(5);
                    table.addCell(priceCell);
                    
                    PdfPCell itemTotalCell = new PdfPCell(new Phrase(String.valueOf(item.getTotalPrice()), normalFont));
                    itemTotalCell.setPadding(5);
                    table.addCell(itemTotalCell);
                }
            }
            
            // Table Footer (Total)
            PdfPCell totalLabelCell = new PdfPCell(new Phrase("Grand Total", boldFont));
            totalLabelCell.setColspan(3);
            totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalLabelCell.setPadding(5);
            table.addCell(totalLabelCell);
            
            PdfPCell totalValCell = new PdfPCell(new Phrase(String.valueOf(order.getTotalAmount()), boldFont));
            totalValCell.setPadding(5);
            table.addCell(totalValCell);
            
            document.add(table);
            
            // Footer text
            document.add(Chunk.NEWLINE);
            Paragraph footer = new Paragraph("Thank you for your business!", normalFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            
        } catch (DocumentException e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating invoice PDF", e);
        }
        
        return baos.toByteArray();
    }
}
