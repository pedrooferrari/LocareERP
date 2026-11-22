package br.com.locare.infra;

import br.com.locare.model.*;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public final class PersistenciaSQLServer {
    private static final String CONFIG = "config/db.properties";

    private PersistenciaSQLServer() {}

    private static Properties carregarConfiguracao() throws IOException {
        Properties propriedades = new Properties();
        String caminhoConfiguracao = System.getProperty("locare.config", CONFIG);
        Path caminho = Path.of(caminhoConfiguracao).toAbsolutePath().normalize();

        if (Files.isRegularFile(caminho)) {
            try (InputStream entrada = Files.newInputStream(caminho)) {
                propriedades.load(entrada);
            }
        } else {
            // Permite empacotar uma configuração não sensível dentro do classpath,
            // sem depender do diretório de trabalho usado para iniciar o programa.
            try (InputStream entrada = PersistenciaSQLServer.class
                    .getClassLoader().getResourceAsStream("db.properties")) {
                if (entrada == null) {
                    throw new IOException("Arquivo de configuração não encontrado: " + caminho
                            + ". Crie config/db.properties a partir de config/db.properties.example."
                            + " Também é possível informar outro caminho com -Dlocare.config=... ");
                }
                propriedades.load(entrada);
            }
        }

        String url = propriedades.getProperty("db.url", "").trim();
        if (url.isEmpty()) {
            throw new IOException("A propriedade db.url está vazia em " + caminho);
        }
        boolean autenticacaoIntegrada = Boolean.parseBoolean(
                propriedades.getProperty("db.integratedSecurity", "false").trim());
        if (!autenticacaoIntegrada) {
            String usuario = propriedades.getProperty("db.user", "").trim();
            String senha = propriedades.getProperty("db.password", "");
            if (usuario.isEmpty() || senha.isBlank() || senha.equals("SUA_SENHA_AQUI")) {
                throw new IOException("Configure db.user e db.password em " + caminho
                        + ". Não compartilhe esse arquivo com senhas reais.");
            }
        }
        return propriedades;
    }

    private static Connection conectar() throws Exception {
        Properties propriedades = carregarConfiguracao();
        String url = propriedades.getProperty("db.url");
        if (url == null || url.isBlank()) {
            throw new IOException("A propriedade db.url não foi configurada em " + CONFIG);
        }
        if (Boolean.parseBoolean(propriedades.getProperty("db.integratedSecurity", "false"))) {
            return DriverManager.getConnection(url);
        }
        return DriverManager.getConnection(url, propriedades.getProperty("db.user", ""),
                propriedades.getProperty("db.password", ""));
    }

    public static void prepararBanco() throws Exception {
        try (Connection c = conectar();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery("""
                     SELECT COUNT(*)
                     FROM (VALUES
                         (N'Cidade'), (N'Pessoa'), (N'Endereco'), (N'Telefone'),
                         (N'Classificacao'), (N'Cliente'), (N'Funcionario'),
                         (N'Fornecedor'), (N'Tipo'), (N'Equipamento'), (N'Locacao'),
                         (N'Locacao_Equipamento'), (N'Pagamento')
                     ) AS tabelas(nome)
                     WHERE OBJECT_ID(N'dbo.' + QUOTENAME(tabelas.nome), N'U') IS NULL
                     """)) {
            if (!r.next() || r.getInt(1) != 0) {
                throw new SQLException("O esquema original do LocareDB está incompleto. "
                        + "Confira sql/setup.sql e o banco conectado.");
            }
        }
        try (Connection c = conectar();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery("""
                     SELECT COUNT(*)
                     FROM (VALUES
                         (N'Cidade', N'id'), (N'Cidade', N'nome'), (N'Cidade', N'frete'),
                         (N'Pessoa', N'codigo'), (N'Pessoa', N'nome'),
                         (N'Pessoa', N'e_mail'), (N'Pessoa', N'cnpj_cpf'),
                         (N'Endereco', N'id'), (N'Endereco', N'tipo'),
                         (N'Endereco', N'logradouro'), (N'Endereco', N'numero'),
                         (N'Endereco', N'bairro'), (N'Endereco', N'id_cidade'),
                         (N'Endereco', N'codigo_pessoa'),
                         (N'Telefone', N'codigo_pessoa'), (N'Telefone', N'numero'),
                         (N'Classificacao', N'sigla'), (N'Classificacao', N'nome'),
                         (N'Cliente', N'codigo'), (N'Cliente', N'sigla_classificacao'),
                         (N'Funcionario', N'codigo'), (N'Funcionario', N'salario'),
                         (N'Fornecedor', N'codigo'), (N'Fornecedor', N'canal'),
                         (N'Tipo', N'codigo'), (N'Tipo', N'modelo'),
                         (N'Tipo', N'marca'), (N'Tipo', N'status'),
                         (N'Equipamento', N'codigo'), (N'Equipamento', N'nome'),
                         (N'Equipamento', N'obs'), (N'Equipamento', N'status'),
                         (N'Equipamento', N'codigo_tipo'),
                         (N'Equipamento', N'codigo_fornecedor'),
                         (N'Locacao', N'numero'), (N'Locacao', N'data_inicio'),
                         (N'Locacao', N'data_termino'), (N'Locacao', N'status'),
                         (N'Locacao', N'total'), (N'Locacao', N'codigo_cliente'),
                         (N'Locacao', N'codigo_funcionario'), (N'Locacao', N'id_endereco'),
                         (N'Locacao_Equipamento', N'numero_locacao'),
                         (N'Locacao_Equipamento', N'codigo_equipamento'),
                         (N'Pagamento', N'numero'), (N'Pagamento', N'valor'),
                         (N'Pagamento', N'data_pagamento'),
                         (N'Pagamento', N'data_vencimento'), (N'Pagamento', N'status'),
                         (N'Pagamento', N'numero_locacao')
                     ) AS colunas(tabela, nome)
                     WHERE COL_LENGTH(N'dbo.' + QUOTENAME(colunas.tabela), colunas.nome) IS NULL
                     """)) {
            if (!r.next() || r.getInt(1) != 0) {
                throw new SQLException("Faltam colunas do esquema original do LocareDB. "
                        + "Confira sql/setup.sql e o banco conectado.");
            }
        }
    }

    public static int proximoCodigoPessoa(EstadoSistema estado) throws Exception {
        int maior = maxCodigo("Pessoa");
        for (Cliente x : estado.clientes) maior = Math.max(maior, x.getId());
        for (Funcionario x : estado.funcionarios) maior = Math.max(maior, x.getId());
        for (Fornecedor x : estado.fornecedores) maior = Math.max(maior, x.getId());
        for (Pessoa x : estado.pessoas) maior = Math.max(maior, x.getId());
        return maior + 1;
    }

    public static int proximoCodigo(String tabela, int maiorEmMemoria) throws Exception {
        String sql;
        switch (tabela) {
            case "Cidade": sql = "SELECT ISNULL(MAX(id), 0) FROM dbo.Cidade"; break;
            case "Endereco": sql = "SELECT ISNULL(MAX(id), 0) FROM dbo.Endereco"; break;
            case "Tipo": sql = "SELECT ISNULL(MAX(codigo), 0) FROM dbo.Tipo"; break;
            case "Equipamento": sql = "SELECT ISNULL(MAX(codigo), 0) FROM dbo.Equipamento"; break;
            case "Locacao": sql = "SELECT ISNULL(MAX(numero), 0) FROM dbo.Locacao"; break;
            case "Pagamento": sql = "SELECT ISNULL(MAX(numero), 0) FROM dbo.Pagamento"; break;
            default: throw new IllegalArgumentException("Tabela de sequência não permitida: " + tabela);
        }
        try (Connection c = conectar(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {
            r.next();
            return Math.max(r.getInt(1), maiorEmMemoria) + 1;
        }
    }

    private static int maxCodigo(String tabela) throws Exception {
        try (Connection c = conectar(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT ISNULL(MAX(codigo), 0) FROM dbo." + tabela)) {
            r.next();
            return r.getInt(1);
        }
    }

    public static EstadoSistema carregar() throws Exception {
        prepararBanco();
        EstadoSistema estado = new EstadoSistema();
        Map<Integer, List<Pessoa>> pessoas = new HashMap<>();
        Map<Integer, Cliente> clientes = new HashMap<>();
        Map<Integer, Funcionario> funcionarios = new HashMap<>();
        Map<Integer, Fornecedor> fornecedores = new HashMap<>();
        Map<Integer, Cidade> cidades = new HashMap<>();
        Map<String, Classificacao> classificacoes = new HashMap<>();
        Map<Integer, Tipo> tipos = new HashMap<>();
        Map<Integer, Equipamento> equipamentos = new HashMap<>();
        Map<Integer, Endereco> enderecos = new HashMap<>();
        Map<Integer, Locacao> locacoes = new HashMap<>();

        try (Connection c = conectar()) {
            carregarCidades(c, estado, cidades);
            carregarClassificacoes(c, estado, classificacoes);
            carregarPessoas(c, estado, pessoas, clientes, funcionarios, fornecedores,
                    classificacoes);
            carregarContatos(c, pessoas, cidades, enderecos);
            carregarTipos(c, estado, tipos);
            carregarEquipamentos(c, estado, equipamentos, tipos, fornecedores);
            carregarLocacoes(c, estado, locacoes, clientes, funcionarios, equipamentos, enderecos);
            carregarPagamentos(c, estado, locacoes);
        }
        return estado;
    }

    private static void carregarCidades(Connection c, EstadoSistema estado,
                                         Map<Integer, Cidade> cidades) throws SQLException {
        try (Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT id, nome, frete FROM dbo.Cidade ORDER BY id")) {
            while (r.next()) {
                Cidade cidade = new Cidade(r.getInt("id"), r.getString("nome"),
                        r.getBigDecimal("frete"));
                cidades.put(cidade.getId(), cidade);
                estado.cidades.add(cidade);
            }
        }
    }

    private static void carregarClassificacoes(Connection c, EstadoSistema estado,
            Map<String, Classificacao> classificacoes) throws SQLException {
        try (Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT sigla, nome FROM dbo.Classificacao ORDER BY sigla")) {
            while (r.next()) {
                Classificacao classificacao = new Classificacao(
                        r.getString("sigla"), r.getString("nome"));
                classificacoes.put(classificacao.getSigla(), classificacao);
                estado.classificacoes.add(classificacao);
            }
        }
    }

    private static void carregarPessoas(Connection c, EstadoSistema estado,
            Map<Integer, List<Pessoa>> pessoas, Map<Integer, Cliente> clientes,
            Map<Integer, Funcionario> funcionarios, Map<Integer, Fornecedor> fornecedores,
            Map<String, Classificacao> classificacoes) throws SQLException {
        String sql = """
                SELECT p.codigo, p.nome, p.e_mail, p.cnpj_cpf, c.sigla_classificacao
                FROM dbo.Pessoa p JOIN dbo.Cliente c ON c.codigo = p.codigo
                ORDER BY p.codigo
                """;
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) {
                Classificacao classificacao = classificacoes.get(r.getString("sigla_classificacao"));
                Cliente cliente = new Cliente(r.getInt("codigo"), r.getString("nome"),
                        r.getString("cnpj_cpf"), r.getString("e_mail"), classificacao);
                estado.clientes.add(cliente);
                clientes.put(cliente.getId(), cliente);
                adicionarPessoa(pessoas, cliente);
            }
        }

        sql = """
                SELECT p.codigo, p.nome, p.e_mail, p.cnpj_cpf, f.salario
                FROM dbo.Pessoa p JOIN dbo.Funcionario f ON f.codigo = p.codigo
                ORDER BY p.codigo
                """;
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) {
                Funcionario funcionario = new Funcionario(r.getInt("codigo"), r.getString("nome"),
                        r.getString("cnpj_cpf"), r.getString("e_mail"), r.getBigDecimal("salario"));
                estado.funcionarios.add(funcionario);
                funcionarios.put(funcionario.getId(), funcionario);
                adicionarPessoa(pessoas, funcionario);
            }
        }

        sql = """
                SELECT p.codigo, p.nome, p.e_mail, p.cnpj_cpf, f.canal
                FROM dbo.Pessoa p JOIN dbo.Fornecedor f ON f.codigo = p.codigo
                ORDER BY p.codigo
                """;
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) {
                Fornecedor fornecedor = new Fornecedor(r.getInt("codigo"), r.getString("nome"),
                        r.getString("cnpj_cpf"), r.getString("e_mail"), r.getString("canal"));
                estado.fornecedores.add(fornecedor);
                fornecedores.put(fornecedor.getId(), fornecedor);
                adicionarPessoa(pessoas, fornecedor);
            }
        }

        sql = """
                SELECT p.codigo, p.nome, p.e_mail, p.cnpj_cpf
                FROM dbo.Pessoa p
                WHERE NOT EXISTS (SELECT 1 FROM dbo.Cliente x WHERE x.codigo = p.codigo)
                  AND NOT EXISTS (SELECT 1 FROM dbo.Funcionario x WHERE x.codigo = p.codigo)
                  AND NOT EXISTS (SELECT 1 FROM dbo.Fornecedor x WHERE x.codigo = p.codigo)
                ORDER BY p.codigo
                """;
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) {
                Pessoa pessoa = new Pessoa(r.getInt("codigo"), r.getString("nome"),
                        r.getString("e_mail"), r.getString("cnpj_cpf"));
                estado.pessoas.add(pessoa);
                adicionarPessoa(pessoas, pessoa);
            }
        }
    }

    private static void adicionarPessoa(Map<Integer, List<Pessoa>> pessoas, Pessoa pessoa) {
        pessoas.computeIfAbsent(pessoa.getCodigo(), chave -> new ArrayList<>()).add(pessoa);
    }

    private static void carregarContatos(Connection c, Map<Integer, List<Pessoa>> pessoas,
            Map<Integer, Cidade> cidades, Map<Integer, Endereco> enderecos) throws SQLException {
        try (Statement s = c.createStatement();
             ResultSet r = s.executeQuery("""
                     SELECT codigo_pessoa, numero FROM dbo.Telefone
                     ORDER BY codigo_pessoa, numero
                     """)) {
            while (r.next()) {
                List<Pessoa> titulares = pessoas.get(r.getInt("codigo_pessoa"));
                if (titulares == null) continue;
                for (Pessoa pessoa : titulares) pessoa.getTelefones().add(r.getString("numero"));
            }
        }

        try (Statement s = c.createStatement();
             ResultSet r = s.executeQuery("""
                     SELECT e.id, e.tipo, e.logradouro, e.numero, e.bairro,
                            e.codigo_pessoa, e.id_cidade
                     FROM dbo.Endereco e ORDER BY e.id
                     """)) {
            while (r.next()) {
                int cidadeId = r.getInt("id_cidade");
                Cidade cidade = cidades.get(cidadeId);
                if (cidade == null) throw new SQLException("Cidade não encontrada: " + cidadeId);
                int codigoPessoa = r.getInt("codigo_pessoa");
                Endereco endereco = new Endereco(r.getInt("id"), r.getString("tipo"),
                        r.getString("logradouro"), r.getString("numero"), r.getString("bairro"),
                        codigoPessoa, cidade);
                enderecos.put(endereco.getId(), endereco);
                List<Pessoa> titulares = pessoas.get(codigoPessoa);
                if (titulares != null) {
                    for (Pessoa pessoa : titulares) pessoa.getEnderecos().add(endereco);
                }
            }
        }
    }

    private static void carregarTipos(Connection c, EstadoSistema estado,
                                       Map<Integer, Tipo> tipos) throws SQLException {
        try (Statement s = c.createStatement();
             ResultSet r = s.executeQuery("""
                     SELECT codigo, modelo, marca, status FROM dbo.Tipo ORDER BY codigo
                     """)) {
            while (r.next()) {
                Tipo tipo = new Tipo(r.getInt("codigo"), r.getString("modelo"),
                        r.getString("marca"), r.getString("status"));
                tipos.put(tipo.getCodigo(), tipo);
                estado.tipos.add(tipo);
            }
        }
    }

    private static void carregarEquipamentos(Connection c, EstadoSistema estado,
            Map<Integer, Equipamento> equipamentos, Map<Integer, Tipo> tipos,
            Map<Integer, Fornecedor> fornecedores) throws SQLException {
        String sql = """
                SELECT e.codigo, e.nome, e.obs, e.status, e.codigo_tipo,
                       e.codigo_fornecedor
                FROM dbo.Equipamento e ORDER BY e.codigo
                """;
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) {
                int tipoId = r.getInt("codigo_tipo");
                int fornecedorId = r.getInt("codigo_fornecedor");
                Tipo tipo = tipos.get(tipoId);
                Fornecedor fornecedor = fornecedores.get(fornecedorId);
                if (tipo == null || fornecedor == null) {
                    throw new SQLException("Equipamento " + r.getInt("codigo")
                            + " aponta para tipo ou fornecedor inexistente.");
                }
                Equipamento equipamento = new Equipamento(r.getInt("codigo"), r.getString("nome"),
                        r.getString("obs"), r.getString("status"), tipo, fornecedor);
                equipamentos.put(equipamento.getId(), equipamento);
                estado.equipamentos.add(equipamento);
            }
        }
    }

    private static void carregarLocacoes(Connection c, EstadoSistema estado,
            Map<Integer, Locacao> locacoes, Map<Integer, Cliente> clientes,
            Map<Integer, Funcionario> funcionarios, Map<Integer, Equipamento> equipamentos,
            Map<Integer, Endereco> enderecos) throws SQLException {
        String sql = """
                SELECT l.numero, l.data_inicio, l.data_termino, l.status, l.total,
                       l.codigo_cliente, l.codigo_funcionario, l.id_endereco,
                       le.codigo_equipamento
                FROM dbo.Locacao l
                LEFT JOIN dbo.Locacao_Equipamento le
                  ON le.numero_locacao = l.numero
                ORDER BY l.numero, le.codigo_equipamento
                """;
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) {
                int id = r.getInt("numero");
                Locacao locacao = locacoes.get(id);
                if (locacao == null) {
                    int clienteId = r.getInt("codigo_cliente");
                    int funcionarioId = r.getInt("codigo_funcionario");
                    int enderecoId = r.getInt("id_endereco");
                    Cliente cliente = clientes.get(clienteId);
                    Funcionario funcionario = funcionarios.get(funcionarioId);
                    Endereco endereco = enderecos.get(enderecoId);
                    java.sql.Date fim = r.getDate("data_termino");
                    if (cliente == null || funcionario == null || endereco == null) {
                        throw new SQLException("Locação " + id
                                + " aponta para cliente, funcionário ou endereço inexistente.");
                    }
                    locacao = new Locacao(id, r.getDate("data_inicio").toLocalDate(),
                            fim == null ? null : fim.toLocalDate(), r.getString("status"),
                            r.getBigDecimal("total"), cliente, funcionario, endereco, List.of());
                    locacoes.put(id, locacao);
                    estado.locacoes.add(locacao);
                }
                int equipamentoId = r.getInt("codigo_equipamento");
                if (!r.wasNull()) {
                    Equipamento equipamento = equipamentos.get(equipamentoId);
                    if (equipamento == null) {
                        throw new SQLException("Equipamento não encontrado: " + equipamentoId);
                    }
                    locacao.getEquipamentos().add(equipamento);
                }
            }
        }
    }

    private static void carregarPagamentos(Connection c, EstadoSistema estado,
                                            Map<Integer, Locacao> locacoes) throws SQLException {
        try (Statement s = c.createStatement();
             ResultSet r = s.executeQuery("""
                     SELECT numero, valor, data_pagamento, data_vencimento, status,
                            numero_locacao
                     FROM dbo.Pagamento ORDER BY numero
                     """)) {
            while (r.next()) {
                Locacao locacao = locacoes.get(r.getInt("numero_locacao"));
                if (locacao == null) {
                    throw new SQLException("Pagamento ligado a locação inexistente: "
                            + r.getInt("numero_locacao"));
                }
                java.sql.Date dataPagamento = r.getDate("data_pagamento");
                estado.pagamentos.add(new Pagamento(r.getInt("numero"), locacao,
                        r.getBigDecimal("valor"), r.getDate("data_vencimento").toLocalDate(),
                        dataPagamento == null ? null : dataPagamento.toLocalDate(),
                        r.getString("status")));
            }
        }
    }

    public static void salvar(EstadoSistema estado) throws Exception {
        prepararBanco();
        try (Connection c = conectar()) {
            c.setAutoCommit(false);
            try {
                for (Classificacao x : estado.classificacoes) salvarClassificacao(c, x);
                for (Cidade x : estado.cidades) salvarCidade(c, x);
                for (Pessoa x : estado.pessoas) salvarPessoa(c, x);
                for (Cliente x : estado.clientes) salvarCliente(c, x);
                for (Funcionario x : estado.funcionarios) salvarFuncionario(c, x);
                for (Fornecedor x : estado.fornecedores) salvarFornecedor(c, x);
                for (Tipo x : estado.tipos) salvarTipo(c, x);
                for (Equipamento x : estado.equipamentos) salvarEquipamento(c, x);
                for (Locacao x : estado.locacoes) salvarLocacao(c, x);
                for (Pagamento x : estado.pagamentos) salvarPagamento(c, x);
                c.commit();
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    private static void salvarPessoa(Connection c, Pessoa pessoa) throws SQLException {
        String sql = "UPDATE dbo.Pessoa SET nome=?, e_mail=?, cnpj_cpf=? WHERE codigo=?";
        if (!update(c, sql, p -> {
            p.setString(1, pessoa.getNome());
            p.setString(2, pessoa.getEmail());
            p.setString(3, pessoa.getCnpjCpf());
            p.setInt(4, pessoa.getCodigo());
        })) {
            identityInsert(c, "dbo.Pessoa",
                    "INSERT dbo.Pessoa(codigo, nome, e_mail, cnpj_cpf) VALUES(?,?,?,?)", p -> {
                        p.setInt(1, pessoa.getCodigo());
                        p.setString(2, pessoa.getNome());
                        p.setString(3, pessoa.getEmail());
                        p.setString(4, pessoa.getCnpjCpf());
                    });
        }
        salvarTelefones(c, pessoa);
        for (Endereco endereco : pessoa.getEnderecos()) salvarEndereco(c, endereco);
    }

    private static void salvarCliente(Connection c, Cliente x) throws SQLException {
        salvarPessoa(c, x);
        String sigla = x.getClassificacao() == null ? null : x.getClassificacao().getSigla();
        if (!update(c, "UPDATE dbo.Cliente SET sigla_classificacao=? WHERE codigo=?",
                p -> { p.setString(1, sigla); p.setInt(2, x.getId()); })) {
            execute(c, "INSERT dbo.Cliente(codigo, sigla_classificacao) VALUES(?,?)",
                    p -> { p.setInt(1, x.getId()); p.setString(2, sigla); });
        }
    }

    private static void salvarFuncionario(Connection c, Funcionario x) throws SQLException {
        salvarPessoa(c, x);
        if (!update(c, "UPDATE dbo.Funcionario SET salario=? WHERE codigo=?",
                p -> { p.setBigDecimal(1, x.getSalario()); p.setInt(2, x.getId()); })) {
            execute(c, "INSERT dbo.Funcionario(codigo, salario) VALUES(?,?)",
                    p -> { p.setInt(1, x.getId()); p.setBigDecimal(2, x.getSalario()); });
        }
    }

    private static void salvarFornecedor(Connection c, Fornecedor x) throws SQLException {
        salvarPessoa(c, x);
        if (!update(c, "UPDATE dbo.Fornecedor SET canal=? WHERE codigo=?",
                p -> { p.setString(1, x.getCanal()); p.setInt(2, x.getId()); })) {
            execute(c, "INSERT dbo.Fornecedor(codigo, canal) VALUES(?,?)",
                    p -> { p.setInt(1, x.getId()); p.setString(2, x.getCanal()); });
        }
    }

    private static void salvarTelefones(Connection c, Pessoa pessoa) throws SQLException {
        execute(c, "DELETE FROM dbo.Telefone WHERE codigo_pessoa=?",
                p -> p.setInt(1, pessoa.getCodigo()));
        Set<String> numeros = new LinkedHashSet<>(pessoa.getTelefones());
        for (String numero : numeros) {
            if (numero == null || numero.isBlank()) continue;
            execute(c, "INSERT dbo.Telefone(codigo_pessoa, numero) VALUES(?,?)",
                    p -> { p.setInt(1, pessoa.getCodigo()); p.setString(2, numero); });
        }
    }

    private static void salvarClassificacao(Connection c, Classificacao x) throws SQLException {
        if (!update(c, "UPDATE dbo.Classificacao SET nome=? WHERE sigla=?",
                p -> { p.setString(1, x.getNome()); p.setString(2, x.getSigla()); })) {
            execute(c, "INSERT dbo.Classificacao(sigla, nome) VALUES(?,?)",
                    p -> { p.setString(1, x.getSigla()); p.setString(2, x.getNome()); });
        }
    }

    private static void salvarCidade(Connection c, Cidade x) throws SQLException {
        if (!update(c, "UPDATE dbo.Cidade SET nome=?, frete=? WHERE id=?",
                p -> { p.setString(1, x.getNome()); p.setBigDecimal(2, x.getFrete()); p.setInt(3, x.getId()); })) {
            identityInsert(c, "dbo.Cidade", "INSERT dbo.Cidade(id, nome, frete) VALUES(?,?,?)",
                    p -> { p.setInt(1, x.getId()); p.setString(2, x.getNome()); p.setBigDecimal(3, x.getFrete()); });
        }
    }

    private static void salvarEndereco(Connection c, Endereco x) throws SQLException {
        if (x.getCidade() == null) throw new SQLException("Endereço sem cidade.");
        salvarCidade(c, x.getCidade());
        String sql = "UPDATE dbo.Endereco SET tipo=?, logradouro=?, numero=?, bairro=?, "
                + "id_cidade=? WHERE id=? AND codigo_pessoa=?";
        Binder campos = p -> {
            p.setString(1, x.getTipo());
            p.setString(2, x.getLogradouro());
            p.setString(3, x.getNumero());
            p.setString(4, x.getBairro());
            p.setInt(5, x.getCidade().getId());
        };
        if (x.getId() > 0) {
            if (!update(c, sql, p -> {
                campos.bind(p);
                p.setInt(6, x.getId());
                p.setInt(7, x.getCodigoPessoa());
            })) {
                identityInsert(c, "dbo.Endereco",
                        "INSERT dbo.Endereco(id,tipo,logradouro,numero,bairro,id_cidade,codigo_pessoa) "
                                + "VALUES(?,?,?,?,?,?,?)", p -> {
                            p.setInt(1, x.getId());
                            p.setString(2, x.getTipo());
                            p.setString(3, x.getLogradouro());
                            p.setString(4, x.getNumero());
                            p.setString(5, x.getBairro());
                            p.setInt(6, x.getCidade().getId());
                            p.setInt(7, x.getCodigoPessoa());
                        });
            }
        } else {
            x.setId(insertGenerated(c,
                    "INSERT dbo.Endereco(tipo,logradouro,numero,bairro,id_cidade,codigo_pessoa) "
                            + "VALUES(?,?,?,?,?,?)", p -> {
                        p.setString(1, x.getTipo());
                        p.setString(2, x.getLogradouro());
                        p.setString(3, x.getNumero());
                        p.setString(4, x.getBairro());
                        p.setInt(5, x.getCidade().getId());
                        p.setInt(6, x.getCodigoPessoa());
                    }));
        }
    }

    private static void salvarTipo(Connection c, Tipo x) throws SQLException {
        if (!update(c, "UPDATE dbo.Tipo SET modelo=?, marca=?, status=? WHERE codigo=?",
                p -> { p.setString(1, x.getModelo()); p.setString(2, x.getMarca());
                    p.setString(3, x.getStatus()); p.setInt(4, x.getCodigo()); })) {
            identityInsert(c, "dbo.Tipo",
                    "INSERT dbo.Tipo(codigo, modelo, marca, status) VALUES(?,?,?,?)", p -> {
                        p.setInt(1, x.getCodigo()); p.setString(2, x.getModelo());
                        p.setString(3, x.getMarca()); p.setString(4, x.getStatus());
                    });
        }
    }

    private static void salvarEquipamento(Connection c, Equipamento x) throws SQLException {
        if (x.getTipo() == null || x.getFornecedor() == null) {
            throw new SQLException("O equipamento " + x.getId() + " precisa de tipo e fornecedor.");
        }
        salvarTipo(c, x.getTipo());
        salvarFornecedor(c, x.getFornecedor());
        String sql = "UPDATE dbo.Equipamento SET nome=?, obs=?, status=?, codigo_tipo=?, "
                + "codigo_fornecedor=? WHERE codigo=?";
        if (!update(c, sql, p -> {
            p.setString(1, x.getNome()); p.setString(2, x.getObs()); p.setString(3, x.getStatus());
            p.setInt(4, x.getTipo().getCodigo()); p.setInt(5, x.getFornecedor().getId());
            p.setInt(6, x.getId());
        })) {
            identityInsert(c, "dbo.Equipamento",
                    "INSERT dbo.Equipamento(codigo,nome,obs,status,codigo_tipo,codigo_fornecedor) "
                            + "VALUES(?,?,?,?,?,?)", p -> {
                        p.setInt(1, x.getId()); p.setString(2, x.getNome()); p.setString(3, x.getObs());
                        p.setString(4, x.getStatus()); p.setInt(5, x.getTipo().getCodigo());
                        p.setInt(6, x.getFornecedor().getId());
                    });
        }
    }

    private static void salvarLocacao(Connection c, Locacao x) throws SQLException {
        if (x.getEndereco() == null || x.getEndereco().getCodigoPessoa() != x.getCliente().getId()) {
            throw new SQLException("A locação " + x.getId()
                    + " deve usar um endereço pertencente ao cliente.");
        }
        salvarCliente(c, x.getCliente());
        salvarFuncionario(c, x.getFuncionario());
        for (Equipamento equipamento : x.getEquipamentos()) salvarEquipamento(c, equipamento);
        salvarEndereco(c, x.getEndereco());

        String sql = "UPDATE dbo.Locacao SET data_inicio=?, data_termino=?, status=?, total=?, "
                + "codigo_cliente=?, codigo_funcionario=?, id_endereco=? WHERE numero=?";
        if (!update(c, sql, p -> bindLocacao(p, x))) {
            identityInsert(c, "dbo.Locacao",
                    "INSERT dbo.Locacao(numero,data_inicio,data_termino,status,total,"
                            + "codigo_cliente,codigo_funcionario,id_endereco) VALUES(?,?,?,?,?,?,?,?)",
                    p -> {
                        p.setInt(1, x.getId());
                        bindLocacao(p, x, 2);
                    });
        }

        execute(c, "DELETE FROM dbo.Locacao_Equipamento WHERE numero_locacao=?",
                p -> p.setInt(1, x.getId()));
        Set<Integer> ids = new LinkedHashSet<>();
        for (Equipamento equipamento : x.getEquipamentos()) {
            if (ids.add(equipamento.getId())) {
                execute(c, "INSERT dbo.Locacao_Equipamento(numero_locacao,codigo_equipamento) "
                        + "VALUES(?,?)", p -> {
                    p.setInt(1, x.getId());
                    p.setInt(2, equipamento.getId());
                });
            }
        }
    }

    private static void bindLocacao(PreparedStatement p, Locacao x) throws SQLException {
        bindLocacao(p, x, 1);
    }

    private static void bindLocacao(PreparedStatement p, Locacao x, int offset) throws SQLException {
        p.setDate(offset, java.sql.Date.valueOf(x.getDataInicio()));
        setDate(p, offset + 1, x.getDataTermino());
        p.setString(offset + 2, x.getStatus());
        p.setBigDecimal(offset + 3, x.getTotal());
        p.setInt(offset + 4, x.getCliente().getId());
        p.setInt(offset + 5, x.getFuncionario().getId());
        p.setInt(offset + 6, x.getEndereco().getId());
        if (offset == 1) p.setInt(8, x.getId());
    }

    private static void salvarPagamento(Connection c, Pagamento x) throws SQLException {
        String sql = "UPDATE dbo.Pagamento SET valor=?, data_pagamento=?, data_vencimento=?, "
                + "status=?, numero_locacao=? WHERE numero=?";
        if (!update(c, sql, p -> {
            p.setBigDecimal(1, x.getValor());
            setDate(p, 2, x.getDataPagamento());
            p.setDate(3, java.sql.Date.valueOf(x.getDataVencimento()));
            p.setString(4, x.getStatus());
            p.setInt(5, x.getLocacao().getId());
            p.setInt(6, x.getId());
        })) {
            identityInsert(c, "dbo.Pagamento",
                    "INSERT dbo.Pagamento(numero,valor,data_pagamento,data_vencimento,status,"
                            + "numero_locacao) VALUES(?,?,?,?,?,?)", p -> {
                        p.setInt(1, x.getId());
                        p.setBigDecimal(2, x.getValor());
                        setDate(p, 3, x.getDataPagamento());
                        p.setDate(4, java.sql.Date.valueOf(x.getDataVencimento()));
                        p.setString(5, x.getStatus());
                        p.setInt(6, x.getLocacao().getId());
                    });
        }
    }

    private static void setDate(PreparedStatement p, int index, LocalDate value) throws SQLException {
        if (value == null) p.setNull(index, Types.DATE);
        else p.setDate(index, java.sql.Date.valueOf(value));
    }

    private static int insertGenerated(Connection c, String sql, Binder binder) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            binder.bind(p);
            p.executeUpdate();
            try (ResultSet keys = p.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("O SQL Server não retornou o código gerado para o registro.");
    }

    private static boolean update(Connection c, String sql, Binder binder) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(sql)) {
            binder.bind(p);
            return p.executeUpdate() > 0;
        }
    }

    private static void execute(Connection c, String sql, Binder binder) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(sql)) {
            binder.bind(p);
            p.executeUpdate();
        }
    }

    private static void identityInsert(Connection c, String table, String sql, Binder binder)
            throws SQLException {
        try (Statement s = c.createStatement()) {
            s.execute("SET IDENTITY_INSERT " + table + " ON");
        }
        try {
            execute(c, sql, binder);
        } finally {
            try (Statement s = c.createStatement()) {
                s.execute("SET IDENTITY_INSERT " + table + " OFF");
            }
        }
    }

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement p) throws SQLException;
    }
}
