package com.myorg.idcard.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class PhotoCropPanel extends JPanel {
    private BufferedImage image;
    private Rectangle selection = null;
    private Point startPoint = null;

    public PhotoCropPanel() {
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
                selection = new Rectangle(startPoint);
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point drag = e.getPoint();
                int x = Math.min(startPoint.x, drag.x);
                int y = Math.min(startPoint.y, drag.y);
                int w = Math.abs(startPoint.x - drag.x);
                int h = Math.abs(startPoint.y - drag.y);
                selection.setBounds(x, y, w, h);
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (selection != null && selection.width > 5 && selection.height > 5) {
                    repaint();
                }
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    public void setImage(BufferedImage img) {
        this.image = img;
        selection = null;
        revalidate();
        repaint();
    }

    public BufferedImage getCroppedImage() {
        if (image == null) return null;
        if (selection == null) {
            // return scaled center-crop if user didn't select
            int s = Math.min(image.getWidth(), image.getHeight());
            int x = (image.getWidth() - s) / 2;
            int y = (image.getHeight() - s) / 2;
            return image.getSubimage(x, y, s, s);
        }
        // clamp selection to image bounds
        Rectangle r = selection.intersection(new Rectangle(0,0,image.getWidth(), image.getHeight()));
        if (r.width <=0 || r.height <=0) return null;
        return image.getSubimage(r.x, r.y, r.width, r.height);
    }

    @Override
    public Dimension getPreferredSize() {
        if (image != null) {
            return new Dimension(Math.min(image.getWidth(), 800), Math.min(image.getHeight(), 600));
        }
        return new Dimension(400, 300);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if (image != null) {
            int panelW = getWidth();
            int panelH = getHeight();
            // draw scaled image to fit panel while preserving aspect
            double imgW = image.getWidth();
            double imgH = image.getHeight();
            double scale = Math.min(panelW / imgW, panelH / imgH);
            int drawW = (int)(imgW * scale);
            int drawH = (int)(imgH * scale);
            int offX = (panelW - drawW) / 2;
            int offY = (panelH - drawH) / 2;
            g.drawImage(image, offX, offY, drawW, drawH, null);

            // if selection exists, draw it relative to scaled coords
            if (selection != null) {
                // compute scaled selection
                double sx = scale;
                g.setColor(new Color(255,255,255,120));
                g.fillRect(offX, offY, drawW, drawH); // optional overlay
                // transform selection to panel coords
                int x = offX + (int)(selection.x * sx);
                int y = offY + (int)(selection.y * sx);
                int w = (int)(selection.width * sx);
                int h = (int)(selection.height * sx);
                g.setColor(Color.RED);
                g.setStroke(new BasicStroke(2));
                g.drawRect(x, y, w, h);
            }
        } else {
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0,0,getWidth(),getHeight());
        }
    }
}
