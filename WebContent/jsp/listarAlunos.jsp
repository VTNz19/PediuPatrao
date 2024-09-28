<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.seuprojeto.model.Aluno" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Lista de Alunos</title>
</head>
<body>
    <h1>Lista de Alunos</h1>
    <table border="1">
        <tr>
            <th>Nome</th>
            <th>Email</th>
            <th>Curso</th>
            <th>Ano de Ingresso</th>
        </tr>
        <% 
        List<?> alunos = (List<?>) request.getAttribute("alunos"); // Mudando para List<?> para evitar o erro de type safety
        if (alunos != null && !alunos.isEmpty()) { // Verifica se a lista não está vazia
            for (Object obj : alunos) { 
                Aluno aluno = (Aluno) obj; // Faz o cast aqui
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
    </table>
    <a href="${pageContext.request.contextPath}/jsp/adicionarAluno.jsp">Adicionar Aluno</a>
    
</body>
</html>
