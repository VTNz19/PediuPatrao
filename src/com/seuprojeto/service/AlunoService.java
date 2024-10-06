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
    public boolean adicionarAluno(Aluno aluno) {
    	try {
            alunoDAO.inserirAluno(aluno); // Verifique se o método inserirAluno está implementado em AlunoDAO
            return true; // Retorna true se a adição foi bem-sucedida
        } catch (Exception e) {
            e.printStackTrace(); // Log do erro
            return false; // Retorna false em caso de erro
        }
    }

    // Método para listar todos os alunos
    public List<Aluno> listarAlunos() {
        return alunoDAO.listarAlunos(); // Verifique se o método listarAlunos está implementado em AlunoDAO
    }

    // Método para buscar um aluno por ID
    public Aluno buscarAluno(int id) { // Mudei para int
        return alunoDAO.buscarAluno(id); // Certifique-se de que este método está implementado na classe AlunoDAO
    }

    // Método para excluir um aluno
    public void excluirAluno(int id) { // Mudei para int
        alunoDAO.excluirAluno(id); // Certifique-se de que este método está implementado na classe AlunoDAO
    }

    // Método para atualizar um aluno
    public void atualizarAluno(Aluno aluno) {
        alunoDAO.atualizarAluno(aluno); // Certifique-se de que este método está implementado na classe AlunoDAO
    }
}
