package com.seuprojeto.controller;

import com.seuprojeto.model.Aluno;
import com.seuprojeto.service.AlunoService;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class AlunoController extends HttpServlet {
    private static final long serialVersionUID = 1L; 
    private AlunoService alunoService = new AlunoService();

    // Método para adicionar um aluno
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String nome = request.getParameter("nome");
        String email = request.getParameter("email");
        String curso = request.getParameter("curso");
        int anoDeIngresso;

        // Tratamento de erro na conversão de anoDeIngresso
        try {
            anoDeIngresso = Integer.parseInt(request.getParameter("anoDeIngresso"));
        } catch (NumberFormatException e) {
            anoDeIngresso = 0; // ou redirecione para uma página de erro
        }

        // Cria um objeto Aluno com os dados recebidos
        Aluno aluno = new Aluno(0, nome, email, curso, anoDeIngresso);
        
        // Adiciona o aluno através do serviço e verifica o resultado
        boolean isAdded = alunoService.adicionarAluno(aluno);
        
        if (isAdded) {
            response.sendRedirect("aluno?action=listar"); // Redireciona para listar alunos em caso de sucesso
        } else {
            request.setAttribute("errorMessage", "Erro ao adicionar aluno.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("jsp/adicionarAluno.jsp"); // Retorna à página de adicionar aluno
            dispatcher.forward(request, response);
        }
    }


    // Método para listar, editar e excluir alunos
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	 System.out.println("Iniciando doGet do AlunoController");
    	    String action = request.getParameter("action"); 
    	    System.out.println("Ação recebida: " + action);

        if ("listar".equals(action)) {
            // Lista todos os alunos
            List<Aluno> alunos = alunoService.listarAlunos();
            request.setAttribute("alunos", alunos); // Adiciona a lista de alunos à requisição
            RequestDispatcher dispatcher = request.getRequestDispatcher("jsp/listarAlunos.jsp"); // Ajuste o caminho aqui
            dispatcher.forward(request, response); // Encaminha para a página que lista os alunos
        } else if ("editar".equals(action)) {
            // Edita um aluno
            String idStr = request.getParameter("id");
            try {
                int id = Integer.parseInt(idStr);
                Aluno aluno = alunoService.buscarAluno(id); // Busca o aluno pelo ID
                request.setAttribute("aluno", aluno); // Adiciona o aluno à requisição
                RequestDispatcher dispatcher = request.getRequestDispatcher("jsp/editarAluno.jsp"); // Ajuste o caminho aqui
                dispatcher.forward(request, response); // Encaminha para a página de edição
            } catch (NumberFormatException e) {
                // Lide com o erro, por exemplo, redirecionando para uma página de erro
                response.sendRedirect("jsp/erro.jsp"); // Exemplo de redirecionamento para uma página de erro
            }
        } else if ("excluir".equals(action)) {
            // Exclui um aluno
            String idStr = request.getParameter("id");
            try {
                int id = Integer.parseInt(idStr);
                alunoService.excluirAluno(id); // Exclui o aluno através do serviço
                response.sendRedirect("aluno?action=listar"); // Redireciona após a exclusão
            } catch (NumberFormatException e) {
                // Lide com o erro, por exemplo, redirecionando para uma página de erro
                response.sendRedirect("jsp/erro.jsp"); // Exemplo de redirecionamento para uma página de erro
            }
        } else {
            // Se nenhuma ação correspondente, redireciona para a página de adicionar aluno
            RequestDispatcher dispatcher = request.getRequestDispatcher("jsp/adicionarAluno.jsp");
            dispatcher.forward(request, response);
        }
    }
}
