package br.com.locare.model;

import java.io.Serializable;

public class Classificacao implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String sigla;
    private String nome;

    public Classificacao(String sigla, String nome) {
        this.sigla = sigla;
        this.nome = nome;
    }

    public String getSigla() { return sigla; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
