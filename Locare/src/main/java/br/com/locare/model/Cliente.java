package br.com.locare.model;

public class Cliente extends Pessoa {
    private static final long serialVersionUID = 1L;
    private Classificacao classificacao;

    public Cliente(int codigo, String nome, String cnpjCpf, String email,
                   Classificacao classificacao) {
        super(codigo, nome, email, cnpjCpf);
        this.classificacao = classificacao;
    }

    public String getCpf() { return getCnpjCpf(); }
    public Classificacao getClassificacao() { return classificacao; }
    public void setClassificacao(Classificacao classificacao) {
        this.classificacao = classificacao;
    }

    public String getEndereco() {
        return getEnderecos().isEmpty() ? "" : getEnderecos().get(0).toString();
    }
}
