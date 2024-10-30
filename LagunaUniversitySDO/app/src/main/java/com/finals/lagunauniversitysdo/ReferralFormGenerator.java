package com.finals.lagunauniversitysdo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.widget.Toast;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ReferralFormGenerator {

    public static void generateReferralForm(Context context, String studentName, String studentId,
                                            String studentProgram, String studentContact,
                                            String studentYear, String studentBlock,
                                            String violations, String reporter, String date, String remarks) {
        try {
            // Define the path where the PDF will be saved
            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    + "/Referral_Form.pdf";
            File file = new File(filePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            // Initialize PdfWriter and PdfDocument
            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Adding a Header and Logo
            addHeaderWithLogo(document, context);

            // Add Date and Term Section
            addSection(document, "DATE OF FILING: " + date, "TERM: 1st Semester - A.Y. 2024-2025");

            // Add Referrer Information
            addSubsection(document, "REFERRER", new String[]{
                    "NAME: " + reporter,
                    "MOBILE NO: " + studentContact, // Assuming reporter contact is same as student's
                    "DEPARTMENT: CCS", // Replace with the actual department if available
                    "EMAIL ADDRESS: [email@example.com]" // Replace with actual email if available
            });

            // Add Student Details Section
            addSubsection(document, "STUDENT DETAILS", new String[]{
                    "NAME: " + studentName,
                    "PROGRAM: " + studentProgram,
                    "ID NUMBER: " + studentId,
                    "CONTACT DETAILS: " + studentContact,
                    "YEAR: " + studentYear,
                    "BLOCK: " + studentBlock,
            });

            // Add Violations Section
            addSubsection(document, "VIOLATIONS", violations.split("\n")); // Assuming violations are passed as a newline-separated string

            // Add Action Taken Section
            addSubsection(document, "ACTION TAKEN", new String[]{
                    "Processed by: Ms. Chrisna Fucio", " Position: Prefect of Discipline",
                    "Remarks: " + remarks
            });

            // Add Data Privacy Consent
            addPrivacyConsent(document);

            // Close document
            document.close();
            Toast.makeText(context, "PDF generated at " + filePath, Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private static void addHeaderWithLogo(Document document, Context context) throws IOException {
        // Add logo
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.lu_logo); // Use your logo here
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        Image logo = new Image(ImageDataFactory.create(stream.toByteArray())).scaleToFit(80, 80);
        logo.setMarginRight(10);

        // Add title next to logo
        Paragraph header = new Paragraph()
                .add(new Paragraph("LAGUNA UNIVERSITY").setBold().setFontSize(24))
                .add(new Paragraph("STUDENT DISCIPLINE OFFICE\nREFERRAL SLIP").setFontSize(18).setMarginTop(-5))
                .setTextAlignment(TextAlignment.CENTER);

        Table headerTable = new Table(2);
        headerTable.addCell(new Cell().add(logo).setBorder(Border.NO_BORDER));
        headerTable.addCell(new Cell().add(header).setBorder(Border.NO_BORDER));
        headerTable.setWidth(UnitValue.createPercentValue(100));

        document.add(headerTable);
    }

    private static void addSection(Document document, String text1, String text2) {
        document.add(new Paragraph(text1).setFontSize(12).setBold());
        document.add(new Paragraph(text2).setFontSize(12).setBold().setMarginBottom(10));
    }

    private static void addSubsection(Document document, String title, String[] details) {
        document.add(new Paragraph(title).setFontSize(14).setBold().setUnderline());
        for (String detail : details) {
            document.add(new Paragraph(detail).setFontSize(12).setMarginLeft(20));
        }
        document.add(new Paragraph("\n"));
    }

    private static void addPrivacyConsent(Document document) {
        document.add(new Paragraph("DATA PRIVACY CONSENT:").setBold().setFontSize(12).setUnderline());
        document.add(new Paragraph("I hereby affirm that all information supplied herein is complete and accurate...").setFontSize(10));
        document.add(new Paragraph("Further, I agree to the collection and processing of my data...").setFontSize(10));
    }
}
