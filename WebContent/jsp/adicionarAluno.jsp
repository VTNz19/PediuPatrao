<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Adicionar Aluno</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <script>
        function validateForm() {
            const nome = document.forms[0]["nome"].value;
            const email = document.forms[0]["email"].value;
            const curso = document.forms[0]["curso"].value;
            const anoDeIngresso = document.forms[0]["anoDeIngresso"].value;

            if (nome.length < 3) {
                alert("O nome deve ter pelo menos 3 caracteres.");
                return false;
            }
            if (!email.includes("@")) {
                alert("Por favor, insira um email válido.");
                return false;
            }
            if (curso.length < 3) {
                alert("O curso deve ter pelo menos 3 caracteres.");
                return false;
            }
            if (anoDeIngresso < 2000 || anoDeIngresso > new Date().getFullYear()) {
                alert("O ano de ingresso deve ser um ano válido.");
                return false;
            }
            return true;
        }
    </script>
</head>
<body>
    <h1>Adicionar Aluno</h1>
    <form action="${pageContext.request.contextPath}/aluno" method="post" onsubmit="return validateForm();">
    <label for="nome">Nome:</label>
    <input type="text" id="nome" name="nome" required>
    
    <label for="email">Email:</label>
    <input type="email" id="email" name="email" required>
    
    <label for="curso">Curso:</label>
    <input type="text" id="curso" name="curso" required>
    
    <label for="anoDeIngresso">Ano de Ingresso:</label>
    <input type="number" id="anoDeIngresso" name="anoDeIngresso" required>
    
    <input type="submit" value="Adicionar Aluno">
</form>
    
    
    <a href="listarAlunos.jsp" class="btn btn-secondary">Voltar para a lista de alunos</a>
</body>
</html>
