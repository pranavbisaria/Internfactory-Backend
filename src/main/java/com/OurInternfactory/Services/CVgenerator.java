//package com.OurInternfactory.Services;
//
//import com.OurInternfactory.Payloads.CVGenerator;
//import com.lowagie.text.*;
//import com.lowagie.text.pdf.PdfWriter;
//import org.springframework.stereotype.Service;
//
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
//@Service
//public class CVgenerator {
//
//    private final CVGenerator cv;
//
//    public CVgenerator(CVGenerator cv) {
//        this.cv = cv;
//    }
//
//    public void export(HttpServletResponse response) throws IOException {
//        Document document = new Document(PageSize.A4);
//        PdfWriter.getInstance(document, response.getOutputStream());
//
//    document.open();
//        Font fontTitle= FontFactory.getFont(FontFactory.HELVETICA_BOLD);
//        fontTitle.setSize(18);
//
//        Paragraph paragraph=new Paragraph("CV",fontTitle);
//        paragraph.setAlignment(Paragraph.ALIGN_CENTER);
//
//        Font fontParagraph=FontFactory.getFont(FontFactory.HELVETICA);
//    fontParagraph.setSize(12);
//
//    Paragraph paragraph2=new Paragraph("This is a paragraph.",fontParagraph);
//    paragraph2.setAlignment(Paragraph.ALIGN_LEFT);
//
//  document.add(paragraph);
//  document.add(paragraph2);
//  document.close();
//
//
//    }
//
//
//}