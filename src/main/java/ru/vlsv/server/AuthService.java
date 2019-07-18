package ru.vlsv.server;


import java.sql.*;
import java.util.ArrayList;

public class AuthService {

    private static Connection connection;
    private static Statement stmt;

    public static void connectDB() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:netusers.db");
            stmt = connection.createStatement();
            createTableIfNotExist(); // Если нет таблицы пользователей - создаем.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String getPass(String login) {
        String sql = String.format("SELECT password FROM users\n" +
                "WHERE login = '%s'", login);

        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getString("password");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isExist(String login) {
        String sql = String.format("SELECT * FROM users\n" +
                "WHERE login = '%s'", login);

        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static synchronized void addUser(String login, String password) {

        // Добавляем пользователя

        String sql = String.format("INSERT INTO users (login, password)" +
                "VALUES ('%s', '%s')", login, password);
        try {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTableIfNotExist() {
        String sql = String.format("CREATE TABLE IF NOT EXISTS users\n"
                + "(\n"
                + "  id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL,\n"
                + "  login TEXT UNIQUE NOT NULL,\n"
                + "  password TEXT NOT NULL\n"
                + ")");
        try {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void disconnectDB() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
