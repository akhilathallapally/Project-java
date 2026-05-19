package com.myorg.idcard.util;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class PDFUtil {
    public static void saveImageAsPdf(BufferedImage img, File outPdf) throws IOException {

        // Ensure .pdf extension
        if (!outPdf.getName().toLowerCase().endsWith(".pdf")) {
            outPdf = new File(outPdf.getAbsolutePath() + ".pdf");
        }

        try (PDDocument doc = new PDDocument()) {

            PDPage page = new PDPage(
                    new PDRectangle(img.getWidth(), img.getHeight())
            );
            doc.addPage(page);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "PNG", baos);

            PDImageXObject pdImage =
                    PDImageXObject.createFromByteArray(doc, baos.toByteArray(), "idcard");

            try (PDPageContentStream cs =
                         new PDPageContentStream(doc, page)) {

                cs.drawImage(pdImage, 0, 0,
                        img.getWidth(), img.getHeight());
            }

            doc.save(outPdf);
        }
    }
}
