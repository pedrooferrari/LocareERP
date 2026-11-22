package br.com.locare.model;

import java.io.Serializable;

public class Endereco implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String tipo;
    private String logradouro;
    private String numero;
    private String bairro;
    private int codigoPessoa;
    private Cidade cidade;

    public Endereco(int id, String tipo, String logradouro, String numero,
                    String bairro, int codigoPessoa, Cidade cidade) {
        if (logradouro == null || cidade == null) {
            throw new IllegalArgumentException("Logradouro e cidade são obrigatórios.");
        }
        this.id = id;
        this.tipo = tipo;
        this.logradouro = logradouro;
        this.numero = numero;
        this.bairro = bairro;
        this.codigoPessoa = codigoPessoa;
        this.cidade = cidade;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) {
        if (logradouro == null) throw new IllegalArgumentException("O logradouro é obrigatório.");
        this.logradouro = logradouro;
    }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public int getCodigoPessoa() { return codigoPessoa; }
    public Cidade getCidade() { return cidade; }
    public void setCidade(Cidade cidade) {
        if (cidade == null) throw new IllegalArgumentException("A cidade é obrigatória.");
        this.cidade = cidade;
    }

    @Override
    public String toString() {
        return logradouro + (numero == null || numero.isBlank() ? "" : ", " + numero)
                + (bairro == null || bairro.isBlank() ? "" : " - " + bairro)
                + (cidade == null ? "" : " - " + cidade.getNome());
    }
}
