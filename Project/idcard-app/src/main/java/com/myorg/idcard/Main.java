package com.myorg.idcard;

import com.myorg.idcard.db.Database;
import com.myorg.idcard.gui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            Database.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Cannot initialize database:\n" + e.getMessage());
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
