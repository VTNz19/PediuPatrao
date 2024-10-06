package com.seuprojeto.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/meudb";
    private static final String USER = "root"; // Substitua pelo seu usuário
    private static final String PASSWORD = "root"; // Substitua pela sua senha

    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC não encontrado.");
        } catch (SQLException e) {
            System.err.println("Erro ao conectar: " + e.getMessage());
        }
        return connection;
    }

}
