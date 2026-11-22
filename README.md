# Locare — Home Care

Sistema de gestão para empresas de **locação de camas e equipamentos hospitalares para uso domiciliar (home care)**. É um MPV (Mínimo Produto Viável) executado no **terminal**, desenvolvido em **Java 17** com persistência em **Microsoft SQL Server**.

> Projeto de Extensão Comunitária (Curricularização) — Fatec São José do Rio Preto, Curso Superior de Tecnologia em Informática para Negócios, semestre 2/2026.

## Funcionalidades

| Módulo | O que permite |
|---|---|
| **Cliente** | Cadastrar clientes (nome, CPF/CNPJ, telefone, e-mail e endereço com cidade), listar, atualizar nome e telefone, cadastrar classificações e atribuí-las aos clientes |
| **Pessoas** | Cadastrar clientes e funcionários e listar todas as pessoas, indicando se são cliente, funcionário ou fornecedor |
| **Equipamento** | Cadastrar equipamentos (nome, descrição, marca, modelo, status e fornecedor), listar e alterar o status |
| **Locação** | Registrar locação (cliente, equipamento disponível, dias, endereço de entrega, funcionário responsável e valor total), listar locações ativas, finalizadas e canceladas, finalizar e cancelar |
| **Pagamento** | Registrar pagamentos vinculados às locações (valor e vencimento), listar, confirmar e marcar como atrasado |
| **Dashboard** | Total de clientes, total de equipamentos, equipamentos alugados, locações ativas, total recebido, clientes com pagamentos em aberto e resumo geral |

Os dados são gravados automaticamente no banco após cada operação do menu.

## Tecnologias

- Java 17
- Apache Maven
- Microsoft SQL Server
- Driver JDBC `mssql-jdbc`

## Estrutura do projeto

```
Locare/
├── config/
│   └── db.properties.example     # modelo de configuração da conexão
├── sql/
│   ├── 01_estrutura.sql          # cria o banco LOCAREDB e as 13 tabelas
│   ├── 02_carga_dados.sql        # insere dados de exemplo
│   └── setup.sql                 # verifica se o esquema esperado existe
├── src/main/java/br/com/locare/
│   ├── app/                      # menus e interação pelo terminal (Main)
│   ├── infra/                    # persistência JDBC (PersistenciaSQLServer)
│   └── model/                    # entidades, enumerações e Dashboard
├── executar.bat                  # compila e executa no Windows
└── pom.xml
```

## Banco de dados

O banco `LOCAREDB` tem 13 tabelas: `CIDADE`, `PESSOA`, `ENDERECO`, `TELEFONE`, `CLASSIFICACAO`, `CLIENTE`, `FUNCIONARIO`, `FORNECEDOR`, `TIPO`, `EQUIPAMENTO`, `LOCACAO`, `LOCACAO_EQUIPAMENTO` e `PAGAMENTO`.

- `CLIENTE`, `FUNCIONARIO` e `FORNECEDOR` são especializações de `PESSOA`.
- `TELEFONE` é uma entidade fraca, identificada por `CODIGO_PESSOA` e `NUMERO`.
- `LOCACAO` e `EQUIPAMENTO` se relacionam por `LOCACAO_EQUIPAMENTO` (N:N).
- O endereço de uma locação deve pertencer ao cliente da própria locação (chave estrangeira composta).

## Como executar

### 1. Requisitos

- JDK 17 ou superior
- Apache Maven 3.8 ou superior
- SQL Server com TCP/IP habilitado (porta 1433, por padrão) e um login com autenticação SQL Server

### 2. Criar o banco

No SQL Server Management Studio (SSMS), execute os scripts nesta ordem:

1. `sql/01_estrutura.sql` — **atenção:** apaga o banco `LOCAREDB` anterior, se existir, e o recria.
2. `sql/02_carga_dados.sql` — insere os dados de exemplo (no mínimo 10 registros por tabela). Execute uma vez, em banco recém-criado.

### 3. Configurar a conexão

1. Copie `config/db.properties.example` para `config/db.properties`.
2. Edite com os dados da sua instalação:

```properties
db.url=jdbc:sqlserver://localhost:1433;databaseName=LOCAREDB;encrypt=true;trustServerCertificate=true
db.user=SEU_LOGIN_SQL
db.password=SUA_SENHA
db.integratedSecurity=false
```

O arquivo `config/db.properties` contém credenciais e está no `.gitignore`. **Não publique esse arquivo.**

Para guardar a configuração fora do projeto, execute com `-Dlocare.config=C:\caminho\db.properties`.

### 4. Executar

No Windows, dê dois cliques em `executar.bat`, ou rode no terminal, na pasta do projeto:

```bash
mvn clean compile
mvn exec:java
```

O menu principal será exibido no terminal. Para encerrar, escolha a opção `0`.

## Uso

```
============================
    LOCARE ERP
============================
1 - Cliente
2 - Equipamento
3 - Locação
4 - Pagamento
5 - Dashboard
6 - Pessoas
0 - Sair
============================
```

Fluxo básico: cadastre um cliente (1) e um equipamento (2), registre a locação (3), registre e confirme o pagamento (4) e consulte os indicadores no Dashboard (5).

## Problemas comuns

| Mensagem | O que verificar |
|---|---|
| Arquivo de configuração não encontrado | Abra a pasta raiz do projeto e confirme que existe `config/db.properties` |
| Connection refused | Serviço do SQL Server, TCP/IP, porta e firewall |
| Login failed | Usuário, senha e modo de autenticação (SQL Server e Windows) |
| Cannot open database | Nome do banco na URL e permissões do login |
| Esquema incompleto | Rode `sql/01_estrutura.sql` e confira com `sql/setup.sql` |
| `mvn` não reconhecido | Instale o Maven, configure o `PATH` e abra um novo terminal |

## Melhorias futuras

- Interface web, conforme os protótipos do projeto
- Edição e exclusão de registros
- Renovação de locações
- Locação com mais de um equipamento
- Cálculo automático do frete pela cidade de entrega
- Módulo de faturamento mensal
- Exportação de dados em CSV
- Contrato de locação gerado automaticamente

## Equipe

- Caio Kruth Nappi
- Guilherme Fernandes
- João Pedro Ferrari
- Joás Fernando de Lima Silva
- Junio Cesar de Lima
