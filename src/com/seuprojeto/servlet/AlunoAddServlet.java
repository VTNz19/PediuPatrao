package com.seuprojeto.servlet;

import com.seuprojeto.dao.AlunoDAO;
import com.seuprojeto.model.Aluno;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AlunoAddServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/adicionarAluno.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String nome = request.getParameter("nome");
        String email = request.getParameter("email");
        String curso = request.getParameter("curso");

        int anoDeIngresso;
        try {
            anoDeIngresso = Integer.parseInt(request.getParameter("anoDeIngresso"));
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "Ano de ingresso inválido.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/adicionarAluno.jsp");
            dispatcher.forward(request, response);
            return;
        }

        Aluno aluno = new Aluno(0, nome, email, curso, anoDeIngresso);
        AlunoDAO alunoDAO = new AlunoDAO();

        try {
            alunoDAO.inserirAluno(aluno);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Erro ao adicionar aluno.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/adicionarAluno.jsp");
            dispatcher.forward(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/aluno/listar");
    }
}
