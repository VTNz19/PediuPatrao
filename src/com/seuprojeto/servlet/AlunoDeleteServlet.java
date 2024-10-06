package com.seuprojeto.servlet;

import com.seuprojeto.service.AlunoService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class AlunoDeleteServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private AlunoService alunoService = new AlunoService();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idParam = request.getParameter("id"); // Obtendo o parâmetro como String
        try {
            int id = Integer.parseInt(idParam); // Convertendo o String para int
            alunoService.excluirAluno(id); // Chamando o método com o int
            response.sendRedirect("listarAlunos.jsp"); // Redirecionando após a exclusão
        } catch (NumberFormatException e) {
            // Lidar com o erro caso o ID não seja um número válido
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID inválido."); // Retorna erro 400
        } catch (Exception e) {
            // Tratar qualquer outra exceção que possa ocorrer
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao excluir aluno."); // Retorna erro 500
        }
    }
}
