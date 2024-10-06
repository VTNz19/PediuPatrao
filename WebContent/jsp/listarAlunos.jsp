<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.seuprojeto.model.Aluno" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Lista de Alunos</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <style>
        body {
            background-image: url('${pageContext.request.contextPath}/images/gestao.jpg');
            background-size: cover;
            background-position: center;
            background-repeat: no-repeat;
            height: 100vh;
            color: white;
        }
        .container {
            background-color: rgba(0, 0, 0, 0.7); /* Fundo semitransparente */
            padding: 20px;
            border-radius: 10px;
            max-width: 900px;
            margin: auto;
            margin-top: 50px;
        }
        h1 {
            text-align: center;
            margin-bottom: 30px;
        }
        table {
            color: white;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Lista de Alunos</h1>
        <table class="table table-striped table-dark">
            <thead>
                <tr>
                    <th>Nome</th>
                    <th>Email</th>
                    <th>Curso</th>
                    <th>Ano de Ingresso</th>
                </tr>
            </thead>
            <tbody>
                <% 
                List<?> alunos = (List<?>) request.getAttribute("alunos");
                if (alunos != null && !alunos.isEmpty()) { 
                    for (Object obj : alunos) { 
                        Aluno aluno = (Aluno) obj; 
                %>
                <tr>
                    <td><%= aluno.getNome() %></td>
                    <td><%= aluno.getEmail() %></td>
                    <td><%= aluno.getCurso() %></td>
                    <td><%= aluno.getAnoDeIngresso() %></td>
                </tr>
                <%
                    }
                } else {
                %>
                <tr>
                    <td colspan="4">Nenhum aluno cadastrado.</td>
                </tr>
                <% 
                } 
                %>
            </tbody>
        </table>
        
        <!-- Botão para Adicionar Mais Alunos -->
        <a href="${pageContext.request.contextPath}/aluno/adicionar" class="btn btn-primary btn-block mt-3">Adicionar Mais Alunos</a>
        
        <!-- Botão para Voltar para a Página Inicial -->
        <a href="${pageContext.request.contextPath}/index.jsp" class="btn btn-secondary btn-block mt-3">Voltar para a Página Inicial</a>
    </div>
</body>
</html>
