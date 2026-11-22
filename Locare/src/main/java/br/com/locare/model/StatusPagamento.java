package br.com.locare.model;

/**
 * Estados de um pagamento.
 *
 * - PENDENTE: ainda não foi quitado.
 * - PAGO: pagamento confirmado.
 * - ATRASADO: não foi pago no prazo estipulado.
 */
public enum StatusPagamento {
    PENDENTE,
    PAGO,
    ATRASADO
}
