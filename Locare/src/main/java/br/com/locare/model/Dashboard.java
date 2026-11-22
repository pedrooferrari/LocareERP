package br.com.locare.model;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Centraliza consultas gerais do sistema para dashboard e relatórios.
 *
 * Ele não altera o estado dos objetos, apenas realiza leitura e agregação.
 */
public class Dashboard {
    private final List<Cliente> clientes;
    private final List<Equipamento> equipamentos;
    private final List<Locacao> locacoes;
    private final List<Pagamento> pagamentos;

    public Dashboard(List<Cliente> clientes, List<Equipamento> equipamentos,
                     List<Locacao> locacoes, List<Pagamento> pagamentos) {
        this.clientes = clientes;
        this.equipamentos = equipamentos;
        this.locacoes = locacoes;
        this.pagamentos = pagamentos;
    }

    public int getTotalClientes() {
        return clientes.size();
    }

    public long getEquipamentosAlugados() {
        return equipamentos.stream()
                .filter(e -> "ALUGADO".equalsIgnoreCase(e.getStatus()))
                .count();
    }

    public int getTotalEquipamentos() {
        return equipamentos.size();
    }

    public long getLocacoesAtivas() {
        return locacoes.stream()
                .filter(l -> "ATIVA".equalsIgnoreCase(l.getStatus())
                        || "ATIVO".equalsIgnoreCase(l.getStatus()))
                .count();
    }

    public BigDecimal getTotalRecebido() {
        return pagamentos.stream()
                .filter(p -> "PAGO".equalsIgnoreCase(p.getStatus())
                        || "QUITADO".equalsIgnoreCase(p.getStatus()))
                .map(Pagamento::getValor)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Retorna os clientes que possuem pagamentos pendentes ou atrasados
     * no mês atual, com base na data de vencimento do pagamento.
     */
    public Set<Cliente> getClientesEmAberto() {
        YearMonth mesAtual = YearMonth.now();

        return pagamentos.stream()
                .filter(p -> "PENDENTE".equalsIgnoreCase(p.getStatus())
                        || "ATRASADO".equalsIgnoreCase(p.getStatus())
                        || "VENCIDO".equalsIgnoreCase(p.getStatus()))
                .filter(p -> p.getDataVencimento() != null)
                .filter(p -> YearMonth.from(p.getDataVencimento()).equals(mesAtual))
                .map(p -> p.getLocacao().getCliente())
                .collect(Collectors.toSet());
    }
}
