package br.com.locare.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Locacao implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int numero;
    private final LocalDate dataInicio;
    private LocalDate dataTermino;
    private String status;
    private BigDecimal total;
    private final Cliente cliente;
    private final Funcionario funcionario;
    private Endereco endereco;
    private final List<Equipamento> equipamentos = new ArrayList<>();

    public Locacao(int numero, LocalDate dataInicio, LocalDate dataTermino,
                   String status, BigDecimal total, Cliente cliente,
                   Funcionario funcionario, Endereco endereco,
                   List<Equipamento> equipamentos) {
        if (cliente == null || funcionario == null || dataInicio == null || endereco == null) {
            throw new IllegalArgumentException("Cliente, funcionário, endereço e início são obrigatórios.");
        }
        if (dataTermino != null && dataTermino.isBefore(dataInicio)) {
            throw new IllegalArgumentException("A data de término não pode anteceder o início.");
        }
        this.numero = numero;
        this.dataInicio = dataInicio;
        this.dataTermino = dataTermino;
        this.status = status;
        this.total = total;
        this.cliente = cliente;
        this.funcionario = funcionario;
        this.endereco = endereco;
        if (equipamentos != null) this.equipamentos.addAll(equipamentos);
    }

    public int getId() { return numero; }
    public int getNumero() { return numero; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataFim() { return dataTermino; }
    public LocalDate getDataTermino() { return dataTermino; }
    public void setDataTermino(LocalDate dataTermino) {
        if (dataTermino != null && dataTermino.isBefore(dataInicio)) {
            throw new IllegalArgumentException("A data de término não pode anteceder o início.");
        }
        this.dataTermino = dataTermino;
    }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getValorTotal() { return total; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public Cliente getCliente() { return cliente; }
    public Funcionario getFuncionario() { return funcionario; }
    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }
    public List<Equipamento> getEquipamentos() { return equipamentos; }
}
