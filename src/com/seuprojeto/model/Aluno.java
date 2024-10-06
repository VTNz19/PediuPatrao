package com.seuprojeto.model;

public class Aluno {
    private int id;            // ID do aluno
    private String nome;      // Nome do aluno
    private String email;     // Email do aluno
    private String curso;     // Curso do aluno
    private int anoDeIngresso; // Ano de ingresso do aluno

    // Construtor
    public Aluno(int id, String nome, String email, String curso, int anoDeIngresso) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.curso = curso;
        this.anoDeIngresso = anoDeIngresso;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public int getAnoDeIngresso() {
        return anoDeIngresso;
    }

    public void setAnoDeIngresso(int anoDeIngresso) {
        this.anoDeIngresso = anoDeIngresso;
    }
}
