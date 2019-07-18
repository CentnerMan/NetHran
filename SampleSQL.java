package ru.vlsv.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class SampleSQL {

    private static Connection connection;

    public static void main(String[] args) {
        final String database = "test.db";
        try {
            // Открываем соединение с базой данных
            connection = DriverManager.getConnection("jdbc:sqlite:" + database);

            // Создаём таблицы
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
                    + " id         INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " name       TEXT NOT NULL,"
                    + " last_visit INTEGER,"
                    + " visits     INTEGER"
                    + ")");

            System.out.print("Введите имя: ");
            String name = new Scanner(System.in).nextLine();

            // Ищем пользователя с этим именем
            PreparedStatement userExistStmt = connection.prepareStatement(
                    "SELECT COUNT(*) FROM users WHERE name = ?");
            userExistStmt.setString(1, name);
            if (userExistStmt.executeQuery().getInt(1) == 0) {
                // Пользователь не найден, добавляем
                addUser(name);
            } else {
                // Обновляем счетчик посещений
                PreparedStatement updateStmt = connection.prepareStatement(
                        "UPDATE users SET last_visit = CURRENT_TIMESTAMP, visits = visits + 1 WHERE name = ?");
                updateStmt.setString(1, name);
                updateStmt.executeUpdate();
            }

            // Выводим информацию обо всех пользователях
            showAllUsers();

            // Удаляем пользователей, которые не заходили 10 минут
            int affectedRows = statement.executeUpdate(
                    "DELETE FROM users WHERE last_visit < DATETIME('now', '-10 minutes')");
            System.out.format("Удалено %d записей%n", affectedRows);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            // Закрываем соединение
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                }
            }
        }
    }

    private static void addUser(String name) throws SQLException {
        PreparedStatement insertStmt = connection.prepareStatement(
                "INSERT INTO users(name, last_visit, visits) VALUES(?, CURRENT_TIMESTAMP, ?)");
        insertStmt.setString(1, name);
        insertStmt.setInt(2, 0);
        insertStmt.executeUpdate();
        ResultSet generatedKeys = insertStmt.getGeneratedKeys();
        if (generatedKeys.next()) {
            System.out.format("Пользователь %s добавлен. id: %d%n",
                    name, generatedKeys.getLong(1));
        }
    }

    private static void showAllUsers() throws SQLException {
        System.out.format("|%4s|%15s|%10s|%6s|%n", "id", "name", "last_visit", "visits");
        System.out.println("|----|---------------|----------|------|");
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(
                "SELECT *, time(last_visit) AS last FROM users ORDER BY last_visit DESC");
        while (rs.next()) {
            int id = rs.getInt(1); // можно обращаться по номеру столбца
            String username = rs.getString("name"); // а можно по имени
            String lastTime = rs.getString("last");
            int visits = rs.getInt("visits");
            System.out.format("|%4d|%15s|%10s|%6d|%n", id, username, lastTime, visits);
        }
    }

}
