package com.myorg.idcard.gui;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import com.myorg.idcard.db.IDCardDao;
import com.myorg.idcard.model.IDCard;
import com.myorg.idcard.util.ImageUtil;
import com.myorg.idcard.util.PDFUtil;
import com.myorg.idcard.util.QRUtil;

public class MainFrame extends JFrame {

    // ================= BASIC FIELDS =================
    private JTextField tfFirst = new JTextField(15);
    private JTextField tfLast  = new JTextField(15);
    private JTextField tfId    = new JTextField(15);

    // ================= ADDITIONAL FIELDS =================
    private JTextField tfDepartmentClass = new JTextField(15);
    private JTextField tfBloodGroup = new JTextField(8);
    private JTextField tfDob = new JTextField(10);
    private JTextField tfYearsOfStudy = new JTextField(12);
    private JTextField tfEmergencyContact = new JTextField(15);
    private JTextArea taAddress = new JTextArea(3, 15);

    private JLabel previewLabel;
    private BufferedImage currentPhoto;

    private JTable table;
    private RecordTableModel tableModel = new RecordTableModel();

    private JComboBox<String> styleCombo =
            new JComboBox<>(new String[]{"Classic", "Modern", "Minimal"});

    private enum Theme { LIGHT, DARK }
    private Theme theme = Theme.LIGHT;

    private boolean isEditMode = false;

    private JButton btnNew = new JButton("New");
    private JButton btnEdit = new JButton("Edit");
    private JButton btnDelete = new JButton("Delete");
    private JButton btnRefresh = new JButton("Refresh");
    private JButton btnSave = new JButton("Save");

    // ================= CONSTRUCTOR =================
    public MainFrame() {
        setTitle("I-D Card Generator");
        setSize(1150, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initUI();
        refreshTable();
    }

    // ================= UI =================
    private void initUI() {

        previewLabel = new JLabel();
        previewLabel.setPreferredSize(new Dimension(260, 360));
        previewLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        previewLabel.setHorizontalAlignment(JLabel.CENTER);
        showEmptyPreview();

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(createToolbar(), BorderLayout.NORTH);

        // ===== BASIC DETAILS =====
        JPanel basicForm = new JPanel(new GridLayout(0, 2, 6, 6));
        basicForm.setBorder(BorderFactory.createTitledBorder("Basic Details"));
        basicForm.add(new JLabel("First Name:"));
        basicForm.add(tfFirst);
        basicForm.add(new JLabel("Last Name:"));
        basicForm.add(tfLast);
        basicForm.add(new JLabel("Registration Number:"));
        basicForm.add(tfId);
        basicForm.add(new JLabel("Style:"));
        basicForm.add(styleCombo);

        // ===== ADDITIONAL DETAILS =====
        JPanel additionalForm = new JPanel(new GridLayout(0, 2, 6, 6));
        additionalForm.setBorder(BorderFactory.createTitledBorder("Additional Details"));
        additionalForm.add(new JLabel("Department & Class:"));
        additionalForm.add(tfDepartmentClass);
        additionalForm.add(new JLabel("Blood Group:"));
        additionalForm.add(tfBloodGroup);
        additionalForm.add(new JLabel("Date of Birth:"));
        additionalForm.add(tfDob);
        additionalForm.add(new JLabel("Years of Study:"));
        additionalForm.add(tfYearsOfStudy);
        additionalForm.add(new JLabel("Emergency Contact:"));
        additionalForm.add(tfEmergencyContact);
        additionalForm.add(new JLabel("Address:"));
        additionalForm.add(new JScrollPane(taAddress));

        JButton btnLoad = new JButton("Load Photo");
        JButton btnPdf  = new JButton("Preview & Export PDF");

        btnLoad.addActionListener(e -> loadPhoto());
        btnSave.addActionListener(e -> {
            if (isEditMode) updateRecord();
            else saveRecord();
        });
        btnPdf.addActionListener(e -> previewAndExport());

        JPanel buttons = new JPanel();
        buttons.add(btnLoad);
        buttons.add(btnSave);
        buttons.add(btnPdf);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.add(basicForm);
        right.add(Box.createVerticalStrut(10));
        right.add(additionalForm);
        right.add(Box.createVerticalStrut(10));
        right.add(previewLabel);
        right.add(Box.createVerticalStrut(10));
        right.add(buttons);

        JScrollPane rightScroll = new JScrollPane(
                right,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        rightScroll.getVerticalScrollBar().setUnitIncrement(16);

        JSplitPane split =
                new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, rightScroll);
        split.setDividerLocation(420);

        add(split, BorderLayout.CENTER);
    }

    // ================= TOOLBAR =================
    private JToolBar createToolbar() {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);

        bar.add(btnNew);
        bar.add(btnEdit);
        bar.add(btnDelete);
        bar.add(btnRefresh);

        JToggleButton themeToggle = new JToggleButton("Dark Mode");
        bar.addSeparator();
        bar.add(themeToggle);

        btnNew.addActionListener(e -> clearForm());
        btnRefresh.addActionListener(e -> { refreshTable(); clearForm(); });
        btnEdit.addActionListener(e -> loadSelected());
        btnDelete.addActionListener(e -> deleteSelected());

        themeToggle.addActionListener(e ->
                theme = themeToggle.isSelected() ? Theme.DARK : Theme.LIGHT);

        return bar;
    }

    // ================= HELPERS =================
    private void showEmptyPreview() {
        BufferedImage img = new BufferedImage(260, 360, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(230,230,230));
        g.fillRect(0,0,260,360);
        g.setColor(Color.GRAY);
        g.drawRect(0,0,259,359);
        g.setFont(new Font("Segoe UI", Font.ITALIC, 18));
        g.drawString("No Photo", 90, 180);
        g.dispose();
        previewLabel.setIcon(new ImageIcon(img));
    }

    private void clearForm() {
        tfFirst.setText("");
        tfLast.setText("");
        tfId.setText("");
        tfDepartmentClass.setText("");
        tfBloodGroup.setText("");
        tfDob.setText("");
        tfYearsOfStudy.setText("");
        tfEmergencyContact.setText("");
        taAddress.setText("");
        currentPhoto = null;
        showEmptyPreview();
        isEditMode = false;
        btnSave.setText("Save");
        tfId.setEditable(true);
    }

    private void loadPhoto() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                currentPhoto = ImageIO.read(fc.getSelectedFile());
                previewLabel.setIcon(new ImageIcon(
                        ImageUtil.resize(currentPhoto, 260, 360)));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // ================= SAVE =================
    private void saveRecord() {
        try {
            if (tfFirst.getText().isEmpty() || tfId.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "First Name and Registration Number are required");
                return;
            }

            if (currentPhoto == null) {
                JOptionPane.showMessageDialog(this,
                        "Please load a photo first");
                return;
            }

            File uploadsDir = new File("uploads");
            uploadsDir.mkdirs();

            File photoFile = new File(uploadsDir, tfId.getText().trim() + ".png");
            ImageIO.write(currentPhoto, "PNG", photoFile);

            IDCard c = buildCardFromForm(photoFile.getAbsolutePath());
            new IDCardDao().insert(c);

            JOptionPane.showMessageDialog(this, "Record saved successfully");
            refreshTable();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Save failed: " + ex.getMessage());
        }
    }

    // ================= UPDATE =================
    private void updateRecord() {
        try {
            File photoFile = new File("uploads/" + tfId.getText().trim() + ".png");
            if (currentPhoto != null)
                ImageIO.write(currentPhoto, "PNG", photoFile);

            IDCard c = buildCardFromForm(photoFile.getAbsolutePath());
            new IDCardDao().update(c);

            JOptionPane.showMessageDialog(this, "Record updated successfully");
            refreshTable();
            clearForm();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Update failed: " + ex.getMessage());
        }
    }

    private IDCard buildCardFromForm(String photoPath) {
        IDCard c = new IDCard();
        c.idNumber = tfId.getText().trim();
        c.firstName = tfFirst.getText().trim();
        c.lastName = tfLast.getText().trim();
        c.departmentClass = tfDepartmentClass.getText().trim();
        c.bloodGroup = tfBloodGroup.getText().trim();
        c.dob = tfDob.getText().trim();
        c.yearsOfStudy = tfYearsOfStudy.getText().trim();
        c.emergencyContact = tfEmergencyContact.getText().trim();
        c.address = taAddress.getText().trim();
        c.photoPath = photoPath;
        c.templateName = "idcard_template.png";
        return c;
    }

    // ================= EDIT / DELETE =================
    private void loadSelected() {
        int r = table.getSelectedRow();
        if (r < 0) return;

        IDCard c = tableModel.data.get(r);

        tfFirst.setText(c.firstName);
        tfLast.setText(c.lastName);
        tfId.setText(c.idNumber);
        tfDepartmentClass.setText(c.departmentClass);
        tfBloodGroup.setText(c.bloodGroup);
        tfDob.setText(c.dob);
        tfYearsOfStudy.setText(c.yearsOfStudy);
        tfEmergencyContact.setText(c.emergencyContact);
        taAddress.setText(c.address);

        try {
            currentPhoto = ImageIO.read(new File(c.photoPath));
            previewLabel.setIcon(new ImageIcon(
                    ImageUtil.resize(currentPhoto, 260, 360)));
        } catch (Exception e) {
            showEmptyPreview();
        }

        isEditMode = true;
        btnSave.setText("Update");
        tfId.setEditable(false);
    }

    private void deleteSelected() {
        int r = table.getSelectedRow();
        if (r < 0) return;

        if (JOptionPane.showConfirmDialog(
                this, "Delete record?", "Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        try {
            new IDCardDao().delete(tableModel.data.get(r).id);
            refreshTable();
            clearForm();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= PREVIEW & PDF =================
    private void previewAndExport() {
        try {
            int W = 1000, H = 600;
            BufferedImage canvas = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = canvas.createGraphics();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                    RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            g.setClip(new RoundRectangle2D.Float(0, 0, W, H, 40, 40));

            Color bg = theme == Theme.DARK ? new Color(30,30,30) : Color.WHITE;
            Color txt = theme == Theme.DARK ? Color.WHITE : Color.BLACK;

            g.setColor(bg);
            g.fillRect(0,0,W,H);

            g.setColor(new Color(33,150,243));
            g.fillRect(0,0,W,90);

            BufferedImage logo = ImageIO.read(
                    Objects.requireNonNull(getClass().getResource("/logo.png")));
            g.drawImage(logo, W-160, 15, 80, 60, null);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 36));
            g.drawString("IDENTITY CARD", 40, 60);

            int px=40, py=120, pw=300, ph=420;
            if (currentPhoto != null)
                g.drawImage(ImageUtil.resize(currentPhoto,pw,ph),px,py,null);

            int tx = px+pw+50;
            g.setColor(txt);
            g.setFont(new Font("Segoe UI", Font.BOLD, 36));
            g.drawString(tfFirst.getText()+" "+tfLast.getText(), tx, 180);

            g.setFont(new Font("Segoe UI", Font.PLAIN, 26));
            g.drawString("Reg No: "+tfId.getText(), tx, 220);
            g.drawString("Dept/Class: "+tfDepartmentClass.getText(), tx, 260);
            g.drawString("Blood: "+tfBloodGroup.getText(), tx, 300);
            g.drawString("DOB: "+tfDob.getText(), tx, 340);
            g.drawString("Study: "+tfYearsOfStudy.getText(), tx, 380);
            g.drawString("Emergency: "+tfEmergencyContact.getText(), tx, 420);

            BufferedImage qr = QRUtil.generateQR(tfId.getText(),160);
            g.drawImage(qr, W-200, H-200, null);

            g.dispose();

            JOptionPane.showMessageDialog(this,
                    new JLabel(new ImageIcon(
                            ImageUtil.resize(canvas,800,480))),
                    "Preview", JOptionPane.PLAIN_MESSAGE);

            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("id-card.pdf"));
            if (fc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION)
                PDFUtil.saveImageAsPdf(canvas, fc.getSelectedFile());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= TABLE =================
    private void refreshTable() {
        try {
            tableModel.setData(new IDCardDao().findAll());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class RecordTableModel extends AbstractTableModel {
        List<IDCard> data = new ArrayList<>();
        void setData(List<IDCard> d){ data=d; fireTableDataChanged(); }
        public int getRowCount(){ return data.size(); }
        public int getColumnCount(){ return 3; }
        public Object getValueAt(int r,int c){
            IDCard x=data.get(r);
            return c==0?x.idNumber:c==1?x.firstName+" "+x.lastName:x.templateName;
        }
        public String getColumnName(int c){
            return new String[]{"Reg No","Name","Template"}[c];
        }
    }
}
