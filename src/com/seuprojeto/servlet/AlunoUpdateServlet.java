package com.seuprojeto.servlet;

import com.seuprojeto.model.Aluno;
import com.seuprojeto.service.AlunoService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/aluno/atualizar")
public class AlunoUpdateServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private AlunoService alunoService = new AlunoService();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        String nome = request.getParameter("nome");
        String email = request.getParameter("email");
        String curso = request.getParameter("curso");
        int anoDeIngresso = Integer.parseInt(request.getParameter("anoDeIngresso"));

        Aluno aluno = new Aluno(id, nome, email, curso, anoDeIngresso);
        alunoService.atualizarAluno(aluno);
        response.sendRedirect("listarAlunos.jsp");
    }
}
