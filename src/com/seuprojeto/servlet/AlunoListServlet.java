package com.seuprojeto.servlet;

import com.seuprojeto.model.Aluno;
import com.seuprojeto.service.AlunoService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import java.io.IOException;
import java.util.List;


public class AlunoListServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private AlunoService alunoService = new AlunoService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Aluno> alunos = alunoService.listarAlunos();
        request.setAttribute("alunos", alunos);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/listarAlunos.jsp");
        dispatcher.forward(request, response);
    }
}
