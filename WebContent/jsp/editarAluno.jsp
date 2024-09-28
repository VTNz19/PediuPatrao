<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Adicionar Aluno</title>
</head>
<body>
    <h1>Adicionar Aluno</h1>
    <form action="aluno" method="post">
        <label for="nome">Nome:</label>
        <input type="text" name="nome" required><br>
        <label for="email">Email:</label>
        <input type="email" name="email" required><br>
        <label for="curso">Curso:</label>
        <input type="text" name="curso" required><br>
        <label for="anoDeIngresso">Ano de Ingresso:</label>
        <input type="number" name="anoDeIngresso" required><br>
        <input type="submit" value="Adicionar Aluno">
    </form>
    <a href="listarAlunos.jsp">Voltar para a lista de alunos</a>
</body>
</html>
