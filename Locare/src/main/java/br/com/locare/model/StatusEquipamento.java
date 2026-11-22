package br.com.locare.model;

/**
 * Estados possíveis de um equipamento.
 *
 * - DISPONIVEL: pode ser locado.
 * - LOCADO: está vinculado a uma locação ativa.
 * - ALUGADO: estado legado equivalente a LOCADO.
 * - MANUTENCAO: não pode ser alugado até voltar ao normal.
 */
public enum StatusEquipamento {
    DISPONIVEL,
    LOCADO,
    ALUGADO,
    MANUTENCAO
}
