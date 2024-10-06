<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Adicionar Aluno</title>
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
            background-color: rgba(0, 0, 0, 0.7); /* Fundo semitransparente para destacar o conteúdo */
            padding: 20px;
            border-radius: 10px;
            max-width: 600px;
            margin: auto;
            margin-top: 50px;
        }
        h1 {
            text-align: center;
            margin-bottom: 30px;
        }
    </style>
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
    <div class="container">
        <h1>Adicionar Aluno</h1>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger" role="alert">
                ${errorMessage}
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/aluno/adicionar" method="post" onsubmit="return validateForm();">
            <div class="form-group">
                <label for="nome">Nome:</label>
                <input type="text" class="form-control" id="nome" name="nome" required>
            </div>

            <div class="form-group">
                <label for="email">Email:</label>
                <input type="email" class="form-control" id="email" name="email" required>
            </div>

            <div class="form-group">
                <label for="curso">Curso:</label>
                <input type="text" class="form-control" id="curso" name="curso" required>
            </div>

            <div class="form-group">
                <label for="anoDeIngresso">Ano de Ingresso:</label>
                <input type="number" class="form-control" id="anoDeIngresso" name="anoDeIngresso" required>
            </div>

            <button type="submit" class="btn btn-primary btn-block">Adicionar Aluno</button>
        </form>

        <a href="${pageContext.request.contextPath}/jsp/listarAlunos.jsp" class="btn btn-secondary btn-block mt-3">Voltar para a lista de alunos</a>

    </div>
</body>
</html>
