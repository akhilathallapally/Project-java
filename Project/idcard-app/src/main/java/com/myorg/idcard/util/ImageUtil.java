package com.myorg.idcard.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageUtil {

    public static BufferedImage resize(BufferedImage img, int w, int h) {
        BufferedImage out =
                new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, w, h, null);
        g.dispose();
        return out;
    }

    public static void savePng(BufferedImage img, File f) throws Exception {
        ImageIO.write(img, "png", f);
    }
}
