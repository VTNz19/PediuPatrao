package com.seuprojeto.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Configurações da conexão com o banco de dados
    private static final String URL = "jdbc:mysql://localhost:3306/meuDB"; // Altere para o seu banco de dados
    private static final String USER = "root"; // Altere para o seu usuário do MySQL
    private static final String PASSWORD = "root"; // Altere para a sua senha

    // Método para obter uma conexão com o banco de dados
    public static Connection getConnection() throws SQLException {
        try {
            // Registrar o driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL não encontrado", e);
        }
        // Retorna uma conexão ao banco de dados
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
