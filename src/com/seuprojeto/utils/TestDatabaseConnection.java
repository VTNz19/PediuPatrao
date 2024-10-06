package com.seuprojeto.utils;

import java.sql.Connection;
import java.sql.SQLException;

public class TestDatabaseConnection {
    public static void main(String[] args) {
        // Tente obter uma conexão com o banco de dados
        try {
            Connection connection = DatabaseConnection.getConnection(); // Verifica a conexão
            if (connection != null) {
                System.out.println("Conexão com o banco de dados foi estabelecida com sucesso!");
                connection.close(); // Feche a conexão após o uso
            } else {
                System.out.println("Falha ao estabelecer a conexão.");
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Mostra a stack trace do erro
        }
    }
}

