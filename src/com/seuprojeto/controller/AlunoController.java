package com.seuprojeto.controller;

import com.seuprojeto.model.Aluno;
import com.seuprojeto.service.AlunoService;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/aluno")
public class AlunoController extends HttpServlet {
    private static final long serialVersionUID = 1L; 
    private AlunoService alunoService = new AlunoService();

 // Método para adicionar um aluno
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String nome = request.getParameter("nome");
        String email = request.getParameter("email");
        String curso = request.getParameter("curso");
        int anoDeIngresso = Integer.parseInt(request.getParameter("anoDeIngresso"));

        // Cria um objeto Aluno com os dados recebidos
        Aluno aluno = new Aluno(null, nome, email, curso, anoDeIngresso);
        alunoService.adicionarAluno(aluno); // Adiciona o aluno através do serviço

        // Redireciona para a ação de listar
        response.sendRedirect("aluno?action=listar");
    }


    // Método para listar, editar e excluir alunos
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action"); // Obtém a ação desejada

        if ("listar".equals(action)) {
            // Lista todos os alunos
            List<Aluno> alunos = alunoService.listarAlunos();
            request.setAttribute("alunos", alunos); // Adiciona a lista de alunos à requisição
            RequestDispatcher dispatcher = request.getRequestDispatcher("jsp/listarAlunos.jsp"); // Ajuste o caminho aqui
            dispatcher.forward(request, response); // Encaminha para a página que lista os alunos
        } else if ("editar".equals(action)) {
            // Edita um aluno
            String id = request.getParameter("id");
            Aluno aluno = alunoService.buscarAluno(id); // Busca o aluno pelo ID
            request.setAttribute("aluno", aluno); // Adiciona o aluno à requisição
            RequestDispatcher dispatcher = request.getRequestDispatcher("jsp/editarAluno.jsp"); // Ajuste o caminho aqui
            dispatcher.forward(request, response); // Encaminha para a página de edição
        } else if ("excluir".equals(action)) {
            // Exclui um aluno
            String id = request.getParameter("id");
            alunoService.excluirAluno(id); // Exclui o aluno através do serviço
            response.sendRedirect("aluno?action=listar"); // Redireciona após a exclusão
        } else {
            // Se nenhuma ação correspondente, redireciona para a página de adicionar aluno
            RequestDispatcher dispatcher = request.getRequestDispatcher("jsp/adicionarAluno.jsp");
            dispatcher.forward(request, response);
        }
    }

}
