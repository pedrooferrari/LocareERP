package br.com.locare.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class Cidade implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String nome;
    private BigDecimal frete;

    public Cidade(int id, String nome, BigDecimal frete) {
        if (nome == null || frete == null || frete.signum() < 0) {
            throw new IllegalArgumentException("Cidade e frete não negativo são obrigatórios.");
        }
        this.id = id;
        this.nome = nome;
        this.frete = frete;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) {
        if (nome == null) throw new IllegalArgumentException("O nome da cidade é obrigatório.");
        this.nome = nome;
    }
    public BigDecimal getFrete() { return frete; }
    public void setFrete(BigDecimal frete) {
        if (frete == null || frete.signum() < 0) {
            throw new IllegalArgumentException("O frete não pode ser negativo.");
        }
        this.frete = frete;
    }
}
