package br.com.locare.model;

public class Fornecedor extends Pessoa {
    private static final long serialVersionUID = 1L;
    private String canal;

    public Fornecedor(int codigo, String nome, String cnpjCpf, String email, String canal) {
        super(codigo, nome, email, cnpjCpf);
        this.canal = canal;
    }

    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }
}
