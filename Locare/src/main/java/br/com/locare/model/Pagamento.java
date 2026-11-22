package br.com.locare.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Pagamento implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int numero;
    private final Locacao locacao;
    private BigDecimal valor;
    private LocalDate dataPagamento;
    private LocalDate dataVencimento;
    private String status;

    public Pagamento(int numero, Locacao locacao, BigDecimal valor,
                     LocalDate dataVencimento, LocalDate dataPagamento, String status) {
        if (locacao == null || valor == null || valor.signum() <= 0 || dataVencimento == null) {
            throw new IllegalArgumentException(
                    "Locação, vencimento e valor positivo são obrigatórios.");
        }
        this.numero = numero;
        this.locacao = locacao;
        this.valor = valor;
        this.dataVencimento = dataVencimento;
        this.dataPagamento = dataPagamento;
        this.status = status;
    }

    public int getId() { return numero; }
    public int getNumero() { return numero; }
    public Locacao getLocacao() { return locacao; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor do pagamento deve ser positivo.");
        }
        this.valor = valor;
    }
    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) {
        if (dataVencimento == null) {
            throw new IllegalArgumentException("A data de vencimento é obrigatória.");
        }
        this.dataVencimento = dataVencimento;
    }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public void confirmarPagamento() {
        dataPagamento = LocalDate.now();
        status = "PAGO";
    }

    public void marcarComoAtrasado() {
        if (!"PAGO".equalsIgnoreCase(status)) status = "ATRASADO";
    }
}
