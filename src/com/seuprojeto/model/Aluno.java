package com.seuprojeto.model;

public class Aluno {
    private String id; // Deve ser único
    private String nome;
    private String email;
    private String curso;
    private int anoDeIngresso;

    // Construtor, getters e setters
    public Aluno(String id, String nome, String email, String curso, int anoDeIngresso) {
        this.id = id; // Atribua um ID único
        this.nome = nome;
        this.email = email;
        this.curso = curso;
        this.anoDeIngresso = anoDeIngresso;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getCurso() {
        return curso;
    }

    public int getAnoDeIngresso() {
        return anoDeIngresso;
    }
}
