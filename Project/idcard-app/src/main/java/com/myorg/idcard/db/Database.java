package com.myorg.idcard.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Database {

    private static Connection conn;

    // 🔥 MUST match the DB you opened in DB Browser
    private static final String DB_URL =
            "jdbc:sqlite:./idcards.db";

    private Database() {
        // prevent instantiation
    }

    public static synchronized Connection getInstance() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(DB_URL);
                createTableIfNeeded();
                System.out.println("Connected to DB: " + DB_URL);
            }
            return conn;
        } catch (Exception e) {
            throw new RuntimeException("Database init failed", e);
        }
    }

    private static void createTableIfNeeded() throws Exception {

        // 🔥 COLUMN NAMES MATCH YOUR EXISTING TABLE
        String sql = """
            CREATE TABLE IF NOT EXISTS id_cards (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                id_number TEXT,
                first_name TEXT,
                last_name TEXT,
                department_class TEXT,
                blood_group TEXT,
                dob TEXT,
                years_of_study TEXT,
                emergency_contact TEXT,
                address TEXT,
                photo_path TEXT,
                template_name TEXT,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
            )
        """;

        try (Statement st = conn.createStatement()) {
            st.execute(sql);
        }
    }
}
