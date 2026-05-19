package com.myorg.idcard.util;

import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.image.BufferedImage;

public class QRUtil {
    public static BufferedImage generateQR(String text, int size) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bm = writer.encode(text, BarcodeFormat.QR_CODE, size, size);
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        for (int x=0;x<size;x++){
            for (int y=0;y<size;y++){
                img.setRGB(x,y, bm.get(x,y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return img;
    }
}
