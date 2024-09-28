package com.seuprojeto.service;

import com.seuprojeto.dao.AlunoDAO;
import com.seuprojeto.model.Aluno;

import java.util.List;

public class AlunoService {
    private AlunoDAO alunoDAO;

    public AlunoService() {
        alunoDAO = new AlunoDAO(); // Instância do AlunoDAO
    }

    // Método para adicionar um aluno
    public void adicionarAluno(Aluno aluno) {
        alunoDAO.inserirAluno(aluno);
    }

    // Método para listar todos os alunos
    public List<Aluno> listarAlunos() {
        return alunoDAO.listarAlunos();
    }

    // Método para buscar um aluno por ID
    public Aluno buscarAluno(String id) {
        return alunoDAO.buscarAluno(id); // Certifique-se de implementar o método buscarAluno na classe AlunoDAO
    }

    // Método para excluir um aluno
    public void excluirAluno(String id) {
        alunoDAO.excluirAluno(id);
    }

    // Método para atualizar um aluno
    public void atualizarAluno(Aluno aluno) {
        alunoDAO.atualizarAluno(aluno); // Certifique-se de que este método esteja implementado na AlunoDAO
    }
}
