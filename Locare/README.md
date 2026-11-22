# Locare — Sistema de locação (Java + SQL Server)

Aplicação acadêmica executada pelo terminal, desenvolvida em Java 17 ou superior, com persistência em Microsoft SQL Server por JDBC.

## 1. Requisitos

- JDK 17 ou superior.
- Apache Maven 3.8 ou superior.
- Microsoft SQL Server com TCP/IP habilitado e uma porta conhecida (ex.: 1433).
- Um banco já criado com as 13 tabelas e colunas esperadas pelo sistema.

## 2. Configurar a conexão

1. Abra a pasta raiz do projeto no VS Code (a pasta que contém `pom.xml`).
2. Na pasta `config`, copie `db.properties.example` para `db.properties`.
3. Edite `config/db.properties` com os dados da sua instalação. Exemplo para SQL Server local na porta 1433:

```properties
db.url=jdbc:sqlserver://localhost:1433;databaseName=SEU_BANCO;encrypt=true;trustServerCertificate=true
db.user=SEU_LOGIN_SQL
db.password=SUA_SENHA
db.integratedSecurity=false
```

Use o nome real do banco. Neste ambiente, o banco de teste utilizado foi `LOCAREDB_teste2`; em outra máquina, o nome pode ser diferente. O login deve ser um login de autenticação SQL Server com acesso ao banco. A autenticação integrada do Windows exige configuração adicional do driver e não é ativada apenas colocando o nome da conta Windows.

**Segurança:** `config/db.properties` contém credenciais e está listado no `.gitignore`. Não publique esse arquivo nem envie sua senha. O arquivo `db.properties.example` é o modelo seguro para compartilhar.

Se quiser guardar o arquivo de configuração fora do projeto, execute com a propriedade Java `-Dlocare.config=C:\caminho\db.properties`.

## 3. Verificar o banco

No HeidiSQL ou SSMS, conecte à instância correta e selecione o banco do projeto. Execute `sql/setup.sql`. O script apenas verifica se as 13 tabelas e as colunas esperadas existem; ele não cria nem apaga tabelas ou registros.

O programa também valida o esquema ao iniciar. Se o banco estiver incompleto ou se a conexão apontar para outro banco, a inicialização será interrompida com uma mensagem de diagnóstico.

## 4. Executar pelo VS Code

Abra o terminal integrado na pasta raiz do projeto e execute:

```powershell
mvn clean compile
mvn exec:java
```

Ou, no Windows, execute `executar.bat`. O menu principal será exibido no terminal. Para encerrar, escolha a opção `0`.

## 5. Diagnóstico rápido

- **Arquivo de configuração não encontrado:** confirme que abriu a pasta raiz do projeto e que existe `config/db.properties`.
- **Configure db.user e db.password:** preencha as credenciais SQL no arquivo local; não deixe os valores de exemplo.
- **Connection refused / conexão recusada:** confira serviço, TCP/IP, porta e firewall.
- **Login failed / falha de login:** confira usuário, senha e modo de autenticação.
- **Cannot open database / não foi possível abrir o banco:** confirme o nome do banco na URL e as permissões do login.
- **Esquema incompleto:** execute o script original de criação do banco ou restaure a estrutura correta; `setup.sql` apenas valida o esquema.
- **Maven não reconhecido:** instale o Apache Maven, configure o `PATH` e abra um terminal novo.

## 6. Estrutura do projeto

- `src/main/java/.../model`: entidades e estado do sistema.
- `src/main/java/.../app`: menus e interação pelo terminal.
- `src/main/java/.../infra`: persistência JDBC e validação do esquema.
- `sql/setup.sql`: validação das tabelas e colunas esperadas.
- `config/db.properties.example`: modelo de configuração sem credenciais reais.

O Locare utiliza as tabelas `Cidade`, `Pessoa`, `Endereco`, `Telefone`, `Classificacao`, `Cliente`, `Funcionario`, `Fornecedor`, `Tipo`, `Equipamento`, `Locacao`, `Locacao_Equipamento` e `Pagamento`.
