package br.com.locare.model;

import java.math.BigDecimal;

public class Funcionario extends Pessoa {
    private static final long serialVersionUID = 1L;
    private BigDecimal salario;

    public Funcionario(int codigo, String nome, String cnpjCpf, String email, BigDecimal salario) {
        super(codigo, nome, email, cnpjCpf);
        validarSalario(salario);
        this.salario = salario;
    }

    public String getCpf() { return getCnpjCpf(); }
    public BigDecimal getSalario() { return salario; }
    public void setSalario(BigDecimal salario) {
        validarSalario(salario);
        this.salario = salario;
    }

    private static void validarSalario(BigDecimal salario) {
        if (salario == null || salario.signum() < 0) {
            throw new IllegalArgumentException("O salário deve ser maior ou igual a zero.");
        }
    }
}
