package com.myorg.idcard.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.myorg.idcard.model.IDCard;

public class IDCardDao {

    // ================= INSERT =================
    public void insert(IDCard c) throws Exception {

        String sql =
                "INSERT INTO id_cards (" +
                        "id_number, first_name, last_name, photo_path, template_name, " +
                        "department_class, blood_group, dob, years_of_study, emergency_contact, address" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = Database.getInstance(); // DO NOT close this

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.idNumber);
            ps.setString(2, c.firstName);
            ps.setString(3, c.lastName);
            ps.setString(4, c.photoPath);
            ps.setString(5, c.templateName);

            ps.setString(6, c.departmentClass);
            ps.setString(7, c.bloodGroup);
            ps.setString(8, c.dob);
            ps.setString(9, c.yearsOfStudy);
            ps.setString(10, c.emergencyContact);
            ps.setString(11, c.address);

            ps.executeUpdate();
        }
    }

    public void update(IDCard c) throws Exception {

        String sql =
                "UPDATE id_cards SET " +
                        "first_name = ?, " +
                        "last_name = ?, " +
                        "photo_path = ?, " +
                        "template_name = ?, " +
                        "department_class = ?, " +
                        "blood_group = ?, " +
                        "dob = ?, " +
                        "years_of_study = ?, " +
                        "emergency_contact = ?, " +
                        "address = ? " +
                        "WHERE id_number = ?";

        Connection conn = Database.getInstance();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.firstName);
            ps.setString(2, c.lastName);
            ps.setString(3, c.photoPath);
            ps.setString(4, c.templateName);
            ps.setString(5, c.departmentClass);
            ps.setString(6, c.bloodGroup);
            ps.setString(7, c.dob);
            ps.setString(8, c.yearsOfStudy);
            ps.setString(9, c.emergencyContact);
            ps.setString(10, c.address);
            ps.setString(11, c.idNumber);

            ps.executeUpdate();
        }
    }

    // ================= FIND ALL =================
    public List<IDCard> findAll() throws Exception {

        List<IDCard> list = new ArrayList<>();

        String sql = "SELECT * FROM id_cards ORDER BY id DESC";

        Connection conn = Database.getInstance(); // DO NOT close

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                IDCard c = new IDCard();

                c.id = rs.getLong("id");
                c.idNumber = rs.getString("id_number");
                c.firstName = rs.getString("first_name");
                c.lastName = rs.getString("last_name");
                c.photoPath = rs.getString("photo_path");
                c.templateName = rs.getString("template_name");

                // NEW FIELDS
                c.departmentClass = rs.getString("department_class");
                c.bloodGroup = rs.getString("blood_group");
                c.dob = rs.getString("dob");
                c.yearsOfStudy = rs.getString("years_of_study");
                c.emergencyContact = rs.getString("emergency_contact");
                c.address = rs.getString("address");

                list.add(c);
            }
        }

        return list;
    }

    // ================= DELETE =================
    public void delete(Long id) throws Exception {

        String sql = "DELETE FROM id_cards WHERE id = ?";

        Connection conn = Database.getInstance(); // DO NOT close

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
}
