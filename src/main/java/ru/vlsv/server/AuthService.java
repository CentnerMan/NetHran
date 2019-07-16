package ru.vlsv.server;


import java.sql.*;
import java.util.ArrayList;

public class AuthService {

    private static Connection connection;
    private static Statement stmt;

    public static void connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:netusers.db");
            stmt = connection.createStatement();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static String getPass(String login) {
        String sql = String.format("SELECT password FROM main\n" +
                "WHERE login = '%s'", login);

        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(sql);
            if (rs.next()) {

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static synchronized void addUser(String login, String password) {

        // Создаем таблицу, если ее нет

        String sql = String.format("CREATE TABLE IF NOT EXISTS main\n"
                + "(\n"
                + "  id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL,\n"
                + "  login TEXT UNIQUE NOT NULL,\n"
                + "  password TEXT NOT NULL,\n"
                + ")");
        try {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Добавляем пользователя

        sql = String.format("INSERT INTO main (login, password)" +
                "VALUES ('%s', '%s')", login, password);
        try {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
