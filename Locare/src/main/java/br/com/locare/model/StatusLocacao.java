package br.com.locare.model;

/**
 * Estados possíveis de uma locação.
 *
 * - ATIVA: locação em curso.
 * - FINALIZADA: contrato encerrado com sucesso.
 * - CANCELADA: contrato interrompido antes da conclusão.
 */
public enum StatusLocacao {
    ATIVA,
    FINALIZADA,
    CANCELADA
}
