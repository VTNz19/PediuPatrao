package com.seuprojeto.dao;

import com.seuprojeto.model.Aluno;

import java.util.ArrayList;
import java.util.List;

public class AlunoDAO {
    private List<Aluno> alunos = new ArrayList<>(); // Usando uma lista em memória para fins de exemplo

    public void inserirAluno(Aluno aluno) {
        alunos.add(aluno); // Adiciona o aluno à lista
    }

    public List<Aluno> listarAlunos() {
        return alunos; // Retorna a lista de alunos
    }

    public Aluno buscarAluno(String id) {
        // Aqui você deve implementar a lógica para buscar um aluno pela ID
        for (Aluno aluno : alunos) {
            if (aluno.getId().equals(id)) { // Supondo que Aluno tem um método getId()
                return aluno;
            }
        }
        return null; // Retorna null se não encontrar
    }

    public void excluirAluno(String id) {
        // Aqui você deve implementar a lógica para excluir um aluno pela ID
        alunos.removeIf(aluno -> aluno.getId().equals(id)); // Remove o aluno se a ID coincidir
    }

    public void atualizarAluno(Aluno aluno) {
        // Aqui você deve implementar a lógica para atualizar um aluno
        for (int i = 0; i < alunos.size(); i++) {
            if (alunos.get(i).getId().equals(aluno.getId())) {
                alunos.set(i, aluno); // Atualiza o aluno na lista
                break;
            }
        }
    }
}
