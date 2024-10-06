package com.seuprojeto.dao;

import com.seuprojeto.model.Aluno;
import com.seuprojeto.utils.DatabaseConnection; 
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlunoDAO {
    private Connection connection;

    public AlunoDAO() {
        connection = DatabaseConnection.getConnection();
		if (connection == null) {
		    System.err.println("Falha ao conectar ao banco de dados.");
		}
    }

    public void inserirAluno(Aluno aluno) {
        String sql = "INSERT INTO alunos (nome, email, curso, anoDeIngresso) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, aluno.getNome());
            stmt.setString(2, aluno.getEmail());
            stmt.setString(3, aluno.getCurso());
            stmt.setInt(4, aluno.getAnoDeIngresso());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // Imprime o erro para os logs
            throw new RuntimeException("Erro ao inserir aluno: " + e.getMessage(), e);
        }
    }

    public List<Aluno> listarAlunos() {
        List<Aluno> alunos = new ArrayList<>();
        String sql = "SELECT * FROM alunos";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Aluno aluno = new Aluno(rs.getInt("id"), rs.getString("nome"), rs.getString("email"),
                                        rs.getString("curso"), rs.getInt("anoDeIngresso"));
                alunos.add(aluno);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alunos;
    }

    public Aluno buscarAluno(int id) {
        String sql = "SELECT * FROM alunos WHERE id = ?";
        Aluno aluno = null;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                aluno = new Aluno(rs.getInt("id"), rs.getString("nome"), rs.getString("email"),
                                  rs.getString("curso"), rs.getInt("anoDeIngresso"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return aluno;
    }

    public void excluirAluno(int id) {
        String sql = "DELETE FROM alunos WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void atualizarAluno(Aluno aluno) {
        String sql = "UPDATE alunos SET nome = ?, email = ?, curso = ?, anoDeIngresso = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, aluno.getNome());
            stmt.setString(2, aluno.getEmail());
            stmt.setString(3, aluno.getCurso());
            stmt.setInt(4, aluno.getAnoDeIngresso());
            stmt.setInt(5, aluno.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
