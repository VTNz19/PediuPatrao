package com.seuprojeto.servlet;

import com.seuprojeto.service.AlunoService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/aluno/excluir")
public class AlunoDeleteServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private AlunoService alunoService = new AlunoService();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        alunoService.excluirAluno(id);
        response.sendRedirect("listarAlunos.jsp");
    }
}
