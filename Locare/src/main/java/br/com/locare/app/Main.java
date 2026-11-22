package br.com.locare.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import br.com.locare.model.Cliente;
import br.com.locare.model.Dashboard;
import br.com.locare.model.Equipamento;
import br.com.locare.model.Locacao;
import br.com.locare.model.Pagamento;
import br.com.locare.model.StatusLocacao;
import br.com.locare.model.EstadoSistema;
import br.com.locare.infra.PersistenciaSQLServer;
import br.com.locare.model.Funcionario;
import br.com.locare.model.Fornecedor;
import br.com.locare.model.Cidade;
import br.com.locare.model.Endereco;
import br.com.locare.model.Tipo;
import br.com.locare.model.Classificacao;
import br.com.locare.model.Pessoa;
import br.com.locare.model.StatusEquipamento;

/**
 * Menu principal do sistema Locare ERP.
 *
 * Este menu permite acessar as operações principais de cada entidade do modelo:
 * pessoas, clientes, equipamento, locação, pagamento e dashboard.
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        EstadoSistema estado;
        try {
            estado = PersistenciaSQLServer.carregar();
            System.out.println("Conexão com SQL Server estabelecida.");
        } catch (Exception e) {
            System.err.println("Não foi possível iniciar o Locare.");
            System.err.println("Verifique a configuração em config/db.properties, o serviço SQL Server, "
                    + "o banco selecionado e as permissões do login.");
            System.err.println("Detalhe: " + e.getMessage());
            Throwable causa = e.getCause();
            while (causa != null) {
                System.err.println("Causa: " + causa.getMessage());
                causa = causa.getCause();
            }
            return;
        }

        List<Cliente> clientes = estado.clientes;
        List<Equipamento> equipamentos = estado.equipamentos;
        List<Locacao> locacoes = estado.locacoes;
        List<Pagamento> pagamentos = estado.pagamentos;

        int opcao;
        do {
            exibirMenuPrincipal();
            opcao = lerInteiro("Escolha uma opção: ");

            switch (opcao) {
                case 1:
                    menuCliente(estado);
                    break;
                case 2:
                    menuEquipamento(estado);
                    break;
                case 3:
                    menuLocacao(estado);
                    break;
                case 4:
                    menuPagamento(locacoes, pagamentos);
                    break;
                case 5:
                    menuDashboard(clientes, equipamentos, locacoes, pagamentos);
                    break;
                case 6:
                    menuPessoas(estado);
                    break;
                case 0:
                    System.out.println("Encerrando o sistema...");
                    break;
                default:
                    System.out.println("Opção inválida! Tente novamente.");
            }

            salvarEstado(estado);
            if (opcao != 0) {
                System.out.println("\nPressione Enter para continuar...");
                scanner.nextLine();
            }
        } while (opcao != 0);
        salvarEstado(estado);
    }

    private static void exibirMenuPrincipal() {
        System.out.println("\n============================");
        System.out.println("    LOCARE ERP");
        System.out.println("============================");
        System.out.println("1 - Cliente");
        System.out.println("2 - Equipamento");
        System.out.println("3 - Locação");
        System.out.println("4 - Pagamento");
        System.out.println("5 - Dashboard");
        System.out.println("6 - Pessoas");
        System.out.println("0 - Sair");
        System.out.println("============================");
    }

    private static void menuPessoas(EstadoSistema estado) {
        int opcao;
        do {
            System.out.println("\n--- PESSOAS ---");
            System.out.println("1 - Cadastrar pessoa");
            System.out.println("2 - Listar pessoas");
            System.out.println("0 - Voltar");

            opcao = lerInteiro("Escolha uma opção: ");
            switch (opcao) {
                case 1:
                    cadastrarPessoa(estado);
                    break;
                case 2:
                    listarPessoas(estado);
                    break;
                case 0:
                    System.out.println("Voltando ao menu principal...");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        } while (opcao != 0);
    }

    private static void cadastrarPessoa(EstadoSistema estado) {
        System.out.println("\nTipo de pessoa:");
        System.out.println("1 - Cliente");
        System.out.println("2 - Funcionário");
        System.out.println("0 - Cancelar");
        int tipo = lerInteiro("Escolha uma opção: ");
        switch (tipo) {
            case 1:
                cadastrarCliente(estado);
                break;
            case 2:
                cadastrarFuncionario(estado);
                break;
            case 0:
                System.out.println("Cadastro cancelado.");
                break;
            default:
                System.out.println("Tipo de pessoa inválido.");
        }
    }

    private static void listarPessoas(EstadoSistema estado) {
        if (estado.clientes.isEmpty() && estado.funcionarios.isEmpty()
                && estado.fornecedores.isEmpty() && estado.pessoas.isEmpty()) {
            System.out.println("Nenhuma pessoa cadastrada.");
            return;
        }

        System.out.println("\nPessoas cadastradas:");
        for (Cliente pessoa : estado.clientes) {
            System.out.println("- " + pessoa.getId() + " | Cliente | " + pessoa.getNome()
                    + " | " + pessoa.getCpf());
        }
        for (Funcionario pessoa : estado.funcionarios) {
            System.out.println("- " + pessoa.getId() + " | Funcionário | " + pessoa.getNome()
                    + " | " + pessoa.getCpf());
        }
        for (Fornecedor pessoa : estado.fornecedores) {
            System.out.println("- " + pessoa.getId() + " | Fornecedor | " + pessoa.getNome()
                    + " | " + pessoa.getCnpjCpf());
        }
        for (Pessoa pessoa : estado.pessoas) {
            System.out.println("- " + pessoa.getId() + " | Pessoa | " + pessoa.getNome()
                    + " | " + pessoa.getCnpjCpf());
        }
    }

    private static void menuCliente(EstadoSistema estado) {
        List<Cliente> clientes = estado.clientes;
        int opcao;
        do {
            System.out.println("\n--- CLIENTE ---");
            System.out.println("1 - Cadastrar cliente");
            System.out.println("2 - Listar clientes");
            System.out.println("3 - Atualizar nome");
            System.out.println("4 - Atualizar telefone");
            System.out.println("5 - Atribuir classificação");
            System.out.println("6 - Cadastrar classificação");
            System.out.println("0 - Voltar");

            opcao = lerInteiro("Escolha uma opção: ");

            switch (opcao) {
                case 1:
                    cadastrarCliente(estado);
                    break;
                case 2:
                    listarClientes(clientes);
                    break;
                case 3:
                    atualizarNomeCliente(clientes);
                    break;
                case 4:
                    atualizarTelefoneCliente(clientes);
                    break;
                case 5:
                    atribuirClassificacaoCliente(estado);
                    break;
                case 6:
                    cadastrarClassificacao(estado);
                    break;
                case 0:
                    System.out.println("Voltando ao menu principal...");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        } while (opcao != 0);
    }

    private static void menuEquipamento(EstadoSistema estado) {
        List<Equipamento> equipamentos = estado.equipamentos;
        int opcao;
        do {
            System.out.println("\n--- EQUIPAMENTO ---");
            System.out.println("1 - Cadastrar equipamento");
            System.out.println("2 - Listar equipamentos");
            System.out.println("3 - Alterar status");
            System.out.println("0 - Voltar");

            opcao = lerInteiro("Escolha uma opção: ");

            switch (opcao) {
                case 1:
                    cadastrarEquipamento(estado);
                    break;
                case 2:
                    listarEquipamentos(equipamentos);
                    break;
                case 3:
                    alterarStatusEquipamento(equipamentos);
                    break;
                case 0:
                    System.out.println("Voltando ao menu principal...");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        } while (opcao != 0);
    }

    private static void menuLocacao(EstadoSistema estado) {
        List<Cliente> clientes = estado.clientes;
        List<Equipamento> equipamentos = estado.equipamentos;
        List<Locacao> locacoes = estado.locacoes;
        int opcao;
        do {
            System.out.println("\n--- LOCAÇÃO ---");
            System.out.println("1 - Nova locação");
            System.out.println("2 - Listar locações ativas");
            System.out.println("3 - Finalizar locação");
            System.out.println("4 - Cancelar locação");
            System.out.println("5 - Listar locações finalizadas");
            System.out.println("6 - Listar locações canceladas");
            System.out.println("0 - Voltar");

            opcao = lerInteiro("Escolha uma opção: ");

            switch (opcao) {
                case 1:
                    novaLocacao(estado);
                    break;
                case 2:
                    listarLocacoesPorStatus(locacoes, StatusLocacao.ATIVA);
                    break;
                case 3:
                    finalizarLocacao(locacoes);
                    break;
                case 4:
                    cancelarLocacao(locacoes);
                    break;
                case 5:
                    listarLocacoesPorStatus(locacoes, StatusLocacao.FINALIZADA);
                    break;
                case 6:
                    listarLocacoesPorStatus(locacoes, StatusLocacao.CANCELADA);
                    break;
                case 0:
                    System.out.println("Voltando ao menu principal...");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        } while (opcao != 0);
    }

    private static void menuPagamento(List<Locacao> locacoes, List<Pagamento> pagamentos) {
        int opcao;
        do {
            System.out.println("\n--- PAGAMENTO ---");
            System.out.println("1 - Registrar pagamento");
            System.out.println("2 - Listar pagamentos");
            System.out.println("3 - Confirmar pagamento");
            System.out.println("4 - Marcar como atrasado");
            System.out.println("0 - Voltar");

            opcao = lerInteiro("Escolha uma opção: ");

            switch (opcao) {
                case 1:
                    registrarPagamento(locacoes, pagamentos);
                    break;
                case 2:
                    listarPagamentos(pagamentos);
                    break;
                case 3:
                    confirmarPagamento(pagamentos);
                    break;
                case 4:
                    atrasarPagamento(pagamentos);
                    break;
                case 0:
                    System.out.println("Voltando ao menu principal...");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        } while (opcao != 0);
    }

    private static void menuDashboard(List<Cliente> clientes,
                                     List<Equipamento> equipamentos,
                                     List<Locacao> locacoes,
                                     List<Pagamento> pagamentos) {
        int opcao;
        do {
            System.out.println("\n--- DASHBOARD ---");
            System.out.println("1 - Total de clientes");
            System.out.println("2 - Total de equipamentos");
            System.out.println("3 - Equipamentos alugados");
            System.out.println("4 - Locações ativas");
            System.out.println("5 - Total recebido");
            System.out.println("6 - Clientes em aberto");
            System.out.println("7 - Resumo geral");
            System.out.println("0 - Voltar");

            opcao = lerInteiro("Escolha uma opção: ");

            Dashboard dashboard = new Dashboard(clientes, equipamentos, locacoes, pagamentos);

            switch (opcao) {
                case 1:
                    System.out.println("Total de clientes: " + dashboard.getTotalClientes());
                    break;
                case 2:
                    System.out.println("Total de equipamentos: " + dashboard.getTotalEquipamentos());
                    break;
                case 3:
                    System.out.println("Equipamentos alugados: " + dashboard.getEquipamentosAlugados());
                    break;
                case 4:
                    System.out.println("Locações ativas: " + dashboard.getLocacoesAtivas());
                    break;
                case 5:
                    System.out.println("Total recebido: R$ " + dashboard.getTotalRecebido());
                    break;
                case 6:
                    System.out.println("Clientes em aberto: " + dashboard.getClientesEmAberto());
                    break;
                case 7:
                    System.out.println("========== RESUMO GERAL ==========");
                    System.out.println("Clientes: " + dashboard.getTotalClientes());
                    System.out.println("Equipamentos: " + dashboard.getTotalEquipamentos());
                    System.out.println("Alugados: " + dashboard.getEquipamentosAlugados());
                    System.out.println("Locações ativas: " + dashboard.getLocacoesAtivas());
                    System.out.println("Total recebido: R$ " + dashboard.getTotalRecebido());
                    System.out.println("==================================");
                    break;
                case 0:
                    System.out.println("Voltando ao menu principal...");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        } while (opcao != 0);
    }

    private static void cadastrarCliente(EstadoSistema estado) {
        List<Cliente> clientes = estado.clientes;
        int id;
        try {
            id = PersistenciaSQLServer.proximoCodigoPessoa(estado);
        } catch (Exception e) {
            System.err.println("Não foi possível gerar o código do cliente: " + e.getMessage());
            return;
        }
        String nome = lerTextoObrigatorio("Nome: ");
        String cpf = lerTextoObrigatorio("CPF: ");
        if (cpfJaCadastrado(estado, cpf)) {
            System.out.println("Já existe uma pessoa cadastrada com esse CPF/CNPJ.");
            return;
        }
        String telefone = lerTexto("Telefone: ");
        String email = lerTexto("Email: ");
        String logradouro = lerTextoObrigatorio("Logradouro: ");
        String numero = lerTexto("Número: ");
        String bairro = lerTexto("Bairro: ");
        String cidade = lerTextoObrigatorio("Cidade: ");

        try {
            Cidade cidadeObj = estado.cidades.stream()
                    .filter(c -> c.getNome().equalsIgnoreCase(cidade))
                    .findFirst().orElse(null);
            if (cidadeObj == null) {
                int cidadeId = PersistenciaSQLServer.proximoCodigo("Cidade",
                        estado.cidades.stream().mapToInt(Cidade::getId).max().orElse(0));
                cidadeObj = new Cidade(cidadeId, cidade, BigDecimal.ZERO);
                estado.cidades.add(cidadeObj);
            }
            Cliente cliente = new Cliente(id, nome, cpf, email, null);
            if (!telefone.isBlank()) cliente.getTelefones().add(telefone);
            int enderecoId = PersistenciaSQLServer.proximoCodigo("Endereco",
                    estado.clientes.stream().flatMap(c -> c.getEnderecos().stream())
                            .mapToInt(Endereco::getId).max().orElse(0));
            cliente.getEnderecos().add(new Endereco(enderecoId, "Residencial",
                    logradouro, numero, bairro, id, cidadeObj));
            clientes.add(cliente);
        } catch (Exception e) {
            System.err.println("Não foi possível gerar os códigos do endereço/cidade: " + e.getMessage());
            return;
        }
        System.out.println("Cliente cadastrado com sucesso!");
    }

    private static void cadastrarFuncionario(EstadoSistema estado) {
        int id;
        try {
            id = PersistenciaSQLServer.proximoCodigoPessoa(estado);
        } catch (Exception e) {
            System.err.println("Não foi possível gerar o código do funcionário: " + e.getMessage());
            return;
        }

        String nome = lerTextoObrigatorio("Nome: ");
        String cpf = lerTextoObrigatorio("CPF: ");
        if (cpfJaCadastrado(estado, cpf)) {
            System.out.println("Já existe uma pessoa cadastrada com esse CPF/CNPJ.");
            return;
        }
        String telefone = lerTexto("Telefone: ");
        String email = lerTexto("Email: ");
        BigDecimal salario = lerBigDecimal("Salário: ");
        if (salario.signum() < 0) {
            System.out.println("O salário deve ser maior ou igual a zero.");
            return;
        }
        Funcionario funcionario = new Funcionario(id, nome, cpf,
                email.isBlank() ? null : email, salario);
        if (!telefone.isBlank()) funcionario.getTelefones().add(telefone);
        estado.funcionarios.add(funcionario);
        System.out.println("Funcionário cadastrado com sucesso!");
    }

    private static boolean cpfJaCadastrado(EstadoSistema estado, String cpf) {
        return estado.clientes.stream().anyMatch(p -> p.getCnpjCpf().equalsIgnoreCase(cpf))
                || estado.funcionarios.stream().anyMatch(p -> p.getCnpjCpf().equalsIgnoreCase(cpf))
                || estado.fornecedores.stream().anyMatch(p -> p.getCnpjCpf().equalsIgnoreCase(cpf))
                || estado.pessoas.stream().anyMatch(p -> p.getCnpjCpf().equalsIgnoreCase(cpf));
    }

    private static void cadastrarClassificacao(EstadoSistema estado) {
        String sigla = lerTextoObrigatorio("Sigla (até 10 caracteres): ").trim();
        String nome = lerTextoObrigatorio("Nome (até 50 caracteres): ").trim();
        if (sigla.length() > 10 || nome.length() > 50) {
            System.out.println("A sigla deve ter até 10 caracteres e o nome até 50.");
            return;
        }
        boolean existe = estado.classificacoes.stream()
                .anyMatch(c -> c.getSigla().equalsIgnoreCase(sigla));
        if (existe) {
            System.out.println("Já existe uma classificação com essa sigla.");
            return;
        }
        estado.classificacoes.add(new Classificacao(sigla, nome));
        System.out.println("Classificação cadastrada com sucesso.");
    }

    private static void atribuirClassificacaoCliente(EstadoSistema estado) {
        if (estado.clientes.isEmpty()) {
            System.out.println("Nenhum cliente cadastrado.");
            return;
        }
        if (estado.classificacoes.isEmpty()) {
            System.out.println("Nenhuma classificação cadastrada. Cadastre uma antes de atribuí-la.");
            return;
        }

        listarClientes(estado.clientes);
        int clienteId = lerInteiro("Digite o ID do cliente: ");
        Cliente cliente = estado.clientes.stream()
                .filter(c -> c.getId() == clienteId)
                .findFirst().orElse(null);
        if (cliente == null) {
            System.out.println("Cliente não encontrado.");
            return;
        }

        System.out.println("Classificações disponíveis (0 - sem classificação):");
        for (int i = 0; i < estado.classificacoes.size(); i++) {
            Classificacao classificacao = estado.classificacoes.get(i);
            System.out.println((i + 1) + " - " + classificacao.getSigla()
                    + " | " + classificacao.getNome());
        }
        int escolha = lerInteiro("Selecione a classificação: ");
        if (escolha == 0) {
            cliente.setClassificacao(null);
            System.out.println("Classificação removida do cliente.");
        } else if (escolha > 0 && escolha <= estado.classificacoes.size()) {
            cliente.setClassificacao(estado.classificacoes.get(escolha - 1));
            System.out.println("Classificação atribuída ao cliente.");
        } else {
            System.out.println("Classificação inválida.");
        }
    }

    private static void listarClientes(List<Cliente> clientes) {
        if (clientes.isEmpty()) {
            System.out.println("Nenhum cliente cadastrado.");
            return;
        }

        System.out.println("\nClientes cadastrados:");
        for (Cliente cliente : clientes) {
            String classificacao = cliente.getClassificacao() == null
                    ? "Sem classificação" : cliente.getClassificacao().getNome();
            System.out.println("- " + cliente.getId() + " | " + cliente.getNome() + " | "
                    + cliente.getCpf() + " | " + classificacao);
        }
    }

    private static void atualizarNomeCliente(List<Cliente> clientes) {
        if (clientes.isEmpty()) {
            System.out.println("Nenhum cliente cadastrado.");
            return;
        }

        listarClientes(clientes);
        int id = lerInteiro("Digite o ID do cliente: ");

        for (Cliente cliente : clientes) {
            if (cliente.getId() == id) {
                cliente.setNome(lerTexto("Novo nome: "));
                System.out.println("Nome atualizado.");
                return;
            }
        }

        System.out.println("Cliente não encontrado.");
    }

    private static void atualizarTelefoneCliente(List<Cliente> clientes) {
        if (clientes.isEmpty()) {
            System.out.println("Nenhum cliente cadastrado.");
            return;
        }

        listarClientes(clientes);
        int id = lerInteiro("Digite o ID do cliente: ");

        for (Cliente cliente : clientes) {
            if (cliente.getId() == id) {
                cliente.getTelefones().clear();
                String telefone = lerTexto("Novo telefone (vazio para remover): ");
                if (!telefone.isBlank()) cliente.getTelefones().add(telefone);
                System.out.println("Telefone atualizado.");
                return;
            }
        }

        System.out.println("Cliente não encontrado.");
    }

    private static void cadastrarEquipamento(EstadoSistema estado) {
        List<Equipamento> equipamentos = estado.equipamentos;
        int id;
        int tipoId;
        try {
            id = PersistenciaSQLServer.proximoCodigo("Equipamento",
                    equipamentos.stream().mapToInt(Equipamento::getId).max().orElse(0));
            tipoId = PersistenciaSQLServer.proximoCodigo("Tipo",
                    estado.tipos.stream().mapToInt(Tipo::getId).max().orElse(0));
        } catch (Exception e) {
            System.err.println("Não foi possível gerar o código do equipamento: " + e.getMessage());
            return;
        }
        String nome = lerTextoObrigatorio("Nome: ");
        String descricao = lerTexto("Descrição: ");
        String marca = lerTextoObrigatorio("Marca: ");
        String modelo = lerTextoObrigatorio("Modelo: ");
        String statusTipo = lerTexto("Status do tipo (opcional): ");
        String statusEquipamento = lerTexto("Status do equipamento [DISPONIVEL]: ");
        if (statusEquipamento.isBlank()) statusEquipamento = "DISPONIVEL";

        String cpfFornecedor = lerTextoObrigatorio("CPF/CNPJ do fornecedor: ");
        Fornecedor fornecedor = estado.fornecedores.stream()
                .filter(f -> f.getCnpjCpf().equalsIgnoreCase(cpfFornecedor))
                .findFirst().orElse(null);
        if (fornecedor == null) {
            int fornecedorId;
            try {
                fornecedorId = PersistenciaSQLServer.proximoCodigoPessoa(estado);
            } catch (Exception e) {
                System.err.println("Não foi possível gerar o código do fornecedor: " + e.getMessage());
                return;
            }
            fornecedor = new Fornecedor(fornecedorId,
                    lerTextoObrigatorio("Nome do fornecedor: "), cpfFornecedor,
                    lerTexto("Email do fornecedor (opcional): "),
                    lerTexto("Canal/contato do fornecedor: "));
            estado.fornecedores.add(fornecedor);
        }
        Tipo tipo = new Tipo(tipoId, modelo, marca, statusTipo);
        estado.tipos.add(tipo);
        Equipamento equipamento = new Equipamento(
                id, nome, descricao, statusEquipamento, tipo, fornecedor);
        equipamentos.add(equipamento);
        System.out.println("Equipamento cadastrado com sucesso!");
    }

    private static void listarEquipamentos(List<Equipamento> equipamentos) {
        if (equipamentos.isEmpty()) {
            System.out.println("Nenhum equipamento cadastrado.");
            return;
        }

        System.out.println("\nEquipamentos cadastrados:");
        for (Equipamento equipamento : equipamentos) {
            System.out.println("- " + equipamento.getId() + " | " + equipamento.getNome() + " | " + equipamento.getStatus());
        }
    }

    private static void alterarStatusEquipamento(List<Equipamento> equipamentos) {
        if (equipamentos.isEmpty()) {
            System.out.println("Nenhum equipamento cadastrado.");
            return;
        }

        listarEquipamentos(equipamentos);
        int id = lerInteiro("Digite o ID do equipamento: ");

        for (Equipamento equipamento : equipamentos) {
            if (equipamento.getId() == id) {
                equipamento.setStatus(lerTextoObrigatorio("Novo status: ").toUpperCase());

                System.out.println("Status atualizado para: " + equipamento.getStatus());
                return;
            }
        }

        System.out.println("Equipamento não encontrado.");
    }

    private static void novaLocacao(EstadoSistema estado) {
        List<Cliente> clientes = estado.clientes;
        List<Equipamento> equipamentos = estado.equipamentos;
        List<Locacao> locacoes = estado.locacoes;
        if (clientes.isEmpty() || equipamentos.isEmpty()) {
            System.out.println("Cadastre pelo menos um cliente e um equipamento antes de criar uma locação.");
            return;
        }

        System.out.println("Clientes disponíveis:");
        for (int i = 0; i < clientes.size(); i++) {
            System.out.println((i + 1) + " - " + clientes.get(i).getNome());
        }

        int indiceCliente = lerInteiro("Selecione o cliente: ") - 1;
        if (indiceCliente < 0 || indiceCliente >= clientes.size()) {
            System.out.println("Cliente inválido.");
            return;
        }

        System.out.println("Equipamentos disponíveis:");
        for (int i = 0; i < equipamentos.size(); i++) {
            Equipamento e = equipamentos.get(i);
            System.out.println((i + 1) + " - " + e.getNome() + " | Status: " + e.getStatus());
        }

        int indiceEquipamento = lerInteiro("Selecione o equipamento: ") - 1;
        if (indiceEquipamento < 0 || indiceEquipamento >= equipamentos.size()) {
            System.out.println("Equipamento inválido.");
            return;
        }

        Equipamento equipamento = equipamentos.get(indiceEquipamento);
        if (!"DISPONIVEL".equalsIgnoreCase(equipamento.getStatus())
                && !"DISPONÍVEL".equalsIgnoreCase(equipamento.getStatus())) {
            System.out.println("Este equipamento não está disponível para locação.");
            return;
        }

        LocalDate dataInicio = LocalDate.now();
        int dias = lerInteiro("Quantos dias de locação? ");
        if (dias < 1) {
            System.out.println("A locação deve ter pelo menos um dia.");
            return;
        }
        LocalDate dataFim = dataInicio.plusDays(dias);
        BigDecimal total = lerBigDecimal("Total da locação: ");
        Cliente cliente = clientes.get(indiceCliente);
        if (cliente.getEnderecos().isEmpty()) {
            System.out.println("O cliente selecionado não possui endereço cadastrado.");
            return;
        }
        System.out.println("Endereços do cliente:");
        for (int i = 0; i < cliente.getEnderecos().size(); i++) {
            System.out.println((i + 1) + " - " + cliente.getEnderecos().get(i));
        }
        int indiceEndereco = lerInteiro("Selecione o endereço da locação: ") - 1;
        if (indiceEndereco < 0 || indiceEndereco >= cliente.getEnderecos().size()) {
            System.out.println("Endereço inválido.");
            return;
        }

        String cpfFuncionario = lerTextoObrigatorio("CPF do funcionário responsável: ");
        Funcionario funcionario = estado.funcionarios.stream()
                .filter(f -> f.getCpf().equalsIgnoreCase(cpfFuncionario))
                .findFirst().orElse(null);
        if (funcionario == null) {
            int funcionarioId;
            try {
                funcionarioId = PersistenciaSQLServer.proximoCodigoPessoa(estado);
            } catch (Exception e) {
                System.err.println("Não foi possível gerar o código do funcionário: " + e.getMessage());
                return;
            }
            funcionario = new Funcionario(funcionarioId,
                    lerTextoObrigatorio("Nome do funcionário: "), cpfFuncionario,
                    lerTexto("Email do funcionário (opcional): "),
                    lerBigDecimal("Salário do funcionário: "));
            estado.funcionarios.add(funcionario);
        }
        int locacaoId;
        try {
            locacaoId = PersistenciaSQLServer.proximoCodigo("Locacao",
                    locacoes.stream().mapToInt(Locacao::getId).max().orElse(0));
        } catch (Exception e) {
            System.err.println("Não foi possível gerar o código da locação: " + e.getMessage());
            return;
        }
        Locacao locacao = new Locacao(locacaoId, dataInicio, dataFim, "ATIVA", total,
                cliente, funcionario, cliente.getEnderecos().get(indiceEndereco),
                List.of(equipamento));

        locacoes.add(locacao);
        equipamento.setStatus(StatusEquipamento.LOCADO.name());
        System.out.println("Locação registrada com sucesso!");
    }

    private static void listarLocacoes(List<Locacao> locacoes) {
        if (locacoes.isEmpty()) {
            System.out.println("Nenhuma locação cadastrada.");
            return;
        }

        System.out.println("\nLocações cadastradas:");
        for (Locacao locacao : locacoes) {
            exibirLocacao(locacao);
        }
    }

    private static void listarLocacoesPorStatus(List<Locacao> locacoes, StatusLocacao status) {
        List<Locacao> filtradas = locacoes.stream()
                .filter(locacao -> status.name().equalsIgnoreCase(locacao.getStatus()))
                .toList();
        if (filtradas.isEmpty()) {
            System.out.println("Nenhuma locação " + status.name().toLowerCase() + " encontrada.");
            return;
        }

        System.out.println("\nLocações " + status.name().toLowerCase() + ":");
        for (Locacao locacao : filtradas) {
            exibirLocacao(locacao);
        }
    }

    private static void exibirLocacao(Locacao locacao) {
        System.out.println("- ID: " + locacao.getId()
                + " | Cliente: " + locacao.getCliente().getNome()
                + " | Equipamentos: " + locacao.getEquipamentos().stream()
                        .map(Equipamento::getNome)
                        .collect(java.util.stream.Collectors.joining(", "))
                + " | Status: " + locacao.getStatus());
    }

    private static void finalizarLocacao(List<Locacao> locacoes) {
        List<Locacao> ativas = locacoes.stream()
                .filter(locacao -> StatusLocacao.ATIVA.name().equalsIgnoreCase(locacao.getStatus()))
                .toList();
        if (ativas.isEmpty()) {
            System.out.println("Nenhuma locação ativa para finalizar.");
            return;
        }

        listarLocacoesPorStatus(locacoes, StatusLocacao.ATIVA);
        int id = lerInteiro("Digite o ID da locação: ");

        for (Locacao locacao : ativas) {
            if (locacao.getId() == id) {
                locacao.setStatus(StatusLocacao.FINALIZADA.name());
                System.out.println("Locação finalizada.");
                return;
            }
        }

        System.out.println("Locação não encontrada.");
    }

    private static void cancelarLocacao(List<Locacao> locacoes) {
        List<Locacao> ativas = locacoes.stream()
                .filter(locacao -> StatusLocacao.ATIVA.name().equalsIgnoreCase(locacao.getStatus()))
                .toList();
        if (ativas.isEmpty()) {
            System.out.println("Nenhuma locação ativa para cancelar.");
            return;
        }

        listarLocacoesPorStatus(locacoes, StatusLocacao.ATIVA);
        int id = lerInteiro("Digite o ID da locação: ");

        for (Locacao locacao : ativas) {
            if (locacao.getId() == id) {
                locacao.setStatus(StatusLocacao.CANCELADA.name());
                System.out.println("Locação cancelada.");
                return;
            }
        }

        System.out.println("Locação não encontrada.");
    }

    private static void registrarPagamento(List<Locacao> locacoes, List<Pagamento> pagamentos) {
        if (locacoes.isEmpty()) {
            System.out.println("Nenhuma locação cadastrada.");
            return;
        }

        listarLocacoes(locacoes);
        int idLocacao = lerInteiro("Digite o ID da locação para pagar: ");

        Locacao locacaoSelecionada = null;
        for (Locacao locacao : locacoes) {
            if (locacao.getId() == idLocacao) {
                locacaoSelecionada = locacao;
                break;
            }
        }

        if (locacaoSelecionada == null) {
            System.out.println("Locação não encontrada.");
            return;
        }

        int id;
        try {
            id = PersistenciaSQLServer.proximoCodigo("Pagamento",
                    pagamentos.stream().mapToInt(Pagamento::getId).max().orElse(0));
        } catch (Exception e) {
            System.err.println("Não foi possível gerar o código do pagamento: " + e.getMessage());
            return;
        }
        BigDecimal valor = lerBigDecimal("Valor do pagamento: ");
        LocalDate vencimento = lerData("Data de vencimento (AAAA-MM-DD): ");

        Pagamento pagamento = new Pagamento(id, locacaoSelecionada, valor,
                vencimento, null, "PENDENTE");
        pagamentos.add(pagamento);
        System.out.println("Pagamento registrado com sucesso.");
    }

    private static void listarPagamentos(List<Pagamento> pagamentos) {
        if (pagamentos.isEmpty()) {
            System.out.println("Nenhum pagamento registrado.");
            return;
        }

        System.out.println("\nPagamentos registrados:");
        for (Pagamento pagamento : pagamentos) {
            System.out.println("- ID: " + pagamento.getId()
                    + " | Cliente: " + pagamento.getLocacao().getCliente().getNome()
                    + " | Valor: R$ " + pagamento.getValor()
                    + " | Status: " + pagamento.getStatus());
        }
    }

    private static void confirmarPagamento(List<Pagamento> pagamentos) {
        if (pagamentos.isEmpty()) {
            System.out.println("Nenhum pagamento registrado.");
            return;
        }

        listarPagamentos(pagamentos);
        int id = lerInteiro("Digite o ID do pagamento: ");

        for (Pagamento pagamento : pagamentos) {
            if (pagamento.getId() == id) {
                pagamento.confirmarPagamento();
                System.out.println("Pagamento confirmado.");
                return;
            }
        }

        System.out.println("Pagamento não encontrado.");
    }

    private static void atrasarPagamento(List<Pagamento> pagamentos) {
        if (pagamentos.isEmpty()) {
            System.out.println("Nenhum pagamento registrado.");
            return;
        }

        listarPagamentos(pagamentos);
        int id = lerInteiro("Digite o ID do pagamento: ");

        for (Pagamento pagamento : pagamentos) {
            if (pagamento.getId() == id) {
                pagamento.marcarComoAtrasado();
                System.out.println("Pagamento marcado como atrasado.");
                return;
            }
        }

        System.out.println("Pagamento não encontrado.");
    }

    private static int lerInteiro(String mensagem) {
        while (true) {
            try {
                System.out.print(mensagem);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Digite um número válido.");
            }
        }
    }

    private static String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return scanner.nextLine();
    }

    private static String lerTextoObrigatorio(String mensagem) {
        while (true) {
            String valor = lerTexto(mensagem).trim();
            if (!valor.isEmpty()) return valor;
            System.out.println("Este campo é obrigatório.");
        }
    }

    private static BigDecimal lerBigDecimal(String mensagem) {
        while (true) {
            try {
                System.out.print(mensagem);
                return new BigDecimal(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Digite um valor decimal válido.");
            }
        }
    }

    private static LocalDate lerData(String mensagem) {
        while (true) {
            try {
                System.out.print(mensagem);
                return LocalDate.parse(scanner.nextLine());
            } catch (java.time.format.DateTimeParseException e) {
                System.out.println("Digite uma data válida no formato AAAA-MM-DD.");
            }
        }
    }

    private static void salvarEstado(EstadoSistema estado) {
        try {
            PersistenciaSQLServer.salvar(estado);
        } catch (Exception e) {
            System.err.println("ERRO ao salvar no banco: " + e.getMessage());
        }
    }

}
