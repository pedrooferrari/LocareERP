package br.com.locare.model;

import java.io.Serializable;

public class Equipamento implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int codigo;
    private String nome;
    private String obs;
    private String status;
    private Tipo tipo;
    private Fornecedor fornecedor;

    public Equipamento(int codigo, String nome, String obs, String status,
                       Tipo tipo, Fornecedor fornecedor) {
        if (nome == null || tipo == null || fornecedor == null) {
            throw new IllegalArgumentException("Nome, tipo e fornecedor são obrigatórios.");
        }
        this.codigo = codigo;
        this.nome = nome;
        this.obs = obs;
        this.status = status;
        this.tipo = tipo;
        this.fornecedor = fornecedor;
    }

    public int getId() { return codigo; }
    public int getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public void setNome(String nome) {
        if (nome == null) throw new IllegalArgumentException("O nome é obrigatório.");
        this.nome = nome;
    }
    public String getObs() { return obs; }
    public String getDescricao() { return obs; }
    public void setObs(String obs) { this.obs = obs; }
    public void setDescricao(String descricao) { this.obs = descricao; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Tipo getTipo() { return tipo; }
    public int getTipoId() { return tipo.getCodigo(); }
    public Fornecedor getFornecedor() { return fornecedor; }
    public void setFornecedor(Fornecedor fornecedor) {
        if (fornecedor == null) throw new IllegalArgumentException("O fornecedor é obrigatório.");
        this.fornecedor = fornecedor;
    }
}
