package com.seuprojeto.servlet;

import com.seuprojeto.model.Aluno;
import com.seuprojeto.service.AlunoService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class AlunoUpdateServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private AlunoService alunoService = new AlunoService();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idParam = request.getParameter("id"); // Obtém o parâmetro ID como String
        String nome = request.getParameter("nome");
        String email = request.getParameter("email");
        String curso = request.getParameter("curso");
        int anoDeIngresso;

        try {
            // Converte o ID para int
            int id = Integer.parseInt(idParam);
            anoDeIngresso = Integer.parseInt(request.getParameter("anoDeIngresso")); // Converte anoDeIngresso para int

            // Cria um objeto Aluno com o ID como int
            Aluno aluno = new Aluno(id, nome, email, curso, anoDeIngresso);
            alunoService.atualizarAluno(aluno); // Chama o método para atualizar o aluno
            response.sendRedirect("listarAlunos.jsp"); // Redireciona após a atualização
        } catch (NumberFormatException e) {
            // Lida com o erro se a conversão falhar
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID ou ano de ingresso inválido."); // Retorna erro 400
        } catch (Exception e) {
            // Lida com outras exceções
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao atualizar aluno."); // Retorna erro 500
        }
    }
}
