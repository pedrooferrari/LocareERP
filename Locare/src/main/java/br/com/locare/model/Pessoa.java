package br.com.locare.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Pessoa implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int codigo;
    private String nome;
    private String email;
    private String cnpjCpf;
    private final List<String> telefones = new ArrayList<>();
    private final List<Endereco> enderecos = new ArrayList<>();

    public Pessoa(int codigo, String nome, String email, String cnpjCpf) {
        if (nome == null || cnpjCpf == null) {
            throw new IllegalArgumentException("Nome e CPF/CNPJ são obrigatórios.");
        }
        this.codigo = codigo;
        this.nome = nome;
        this.email = email;
        this.cnpjCpf = cnpjCpf;
    }

    public int getId() { return codigo; }
    public int getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public void setNome(String nome) {
        if (nome == null) throw new IllegalArgumentException("O nome é obrigatório.");
        this.nome = nome;
    }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCnpjCpf() { return cnpjCpf; }
    public void setCnpjCpf(String cnpjCpf) {
        if (cnpjCpf == null) throw new IllegalArgumentException("CPF/CNPJ é obrigatório.");
        this.cnpjCpf = cnpjCpf;
    }
    public List<String> getTelefones() { return telefones; }
    public List<Endereco> getEnderecos() { return enderecos; }
}
