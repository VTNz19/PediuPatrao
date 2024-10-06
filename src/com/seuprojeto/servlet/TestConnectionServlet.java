package com.seuprojeto.servlet;

import com.seuprojeto.utils.DatabaseConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;

@WebServlet("/testConnection")
public class TestConnectionServlet extends HttpServlet {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        try (Connection connection = DatabaseConnection.getConnection()) {
            if (connection != null) {
                response.getWriter().println("Conexão com o banco de dados foi estabelecida com sucesso!");
            } else {
                response.getWriter().println("Falha ao estabelecer a conexão.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Erro: " + e.getMessage());
        }
    }
}
