-- Verifica se o banco atualmente selecionado contém o esquema esperado pelo Locare.
-- Selecione o banco correto no SSMS/HeidiSQL antes de executar este script.

IF DB_NAME() IS NULL OR DB_NAME() = N'master'
    THROW 50000, N'Selecione o banco de dados do projeto antes de executar este setup.', 1;
GO

DECLARE @Tabelas TABLE (nome sysname NOT NULL);
INSERT INTO @Tabelas (nome) VALUES
    (N'Cidade'), (N'Pessoa'), (N'Endereco'), (N'Telefone'),
    (N'Classificacao'), (N'Cliente'), (N'Funcionario'),
    (N'Fornecedor'), (N'Tipo'), (N'Equipamento'), (N'Locacao'),
    (N'Locacao_Equipamento'), (N'Pagamento');

IF EXISTS (
    SELECT 1 FROM @Tabelas t
    WHERE OBJECT_ID(N'dbo.' + QUOTENAME(t.nome), N'U') IS NULL
)
    THROW 50001, N'O esquema está incompleto. Uma ou mais tabelas esperadas não existem no banco selecionado.', 1;
GO

DECLARE @Colunas TABLE (tabela sysname NOT NULL, coluna sysname NOT NULL);
INSERT INTO @Colunas (tabela, coluna) VALUES
    (N'Cidade', N'id'), (N'Cidade', N'nome'), (N'Cidade', N'frete'),
    (N'Pessoa', N'codigo'), (N'Pessoa', N'nome'), (N'Pessoa', N'e_mail'), (N'Pessoa', N'cnpj_cpf'),
    (N'Endereco', N'id'), (N'Endereco', N'tipo'), (N'Endereco', N'logradouro'),
    (N'Endereco', N'numero'), (N'Endereco', N'bairro'), (N'Endereco', N'id_cidade'), (N'Endereco', N'codigo_pessoa'),
    (N'Telefone', N'codigo_pessoa'), (N'Telefone', N'numero'),
    (N'Classificacao', N'sigla'), (N'Classificacao', N'nome'),
    (N'Cliente', N'codigo'), (N'Cliente', N'sigla_classificacao'),
    (N'Funcionario', N'codigo'), (N'Funcionario', N'salario'),
    (N'Fornecedor', N'codigo'), (N'Fornecedor', N'canal'),
    (N'Tipo', N'codigo'), (N'Tipo', N'modelo'), (N'Tipo', N'marca'), (N'Tipo', N'status'),
    (N'Equipamento', N'codigo'), (N'Equipamento', N'nome'), (N'Equipamento', N'obs'),
    (N'Equipamento', N'status'), (N'Equipamento', N'codigo_tipo'), (N'Equipamento', N'codigo_fornecedor'),
    (N'Locacao', N'numero'), (N'Locacao', N'data_inicio'), (N'Locacao', N'data_termino'),
    (N'Locacao', N'status'), (N'Locacao', N'total'), (N'Locacao', N'codigo_cliente'),
    (N'Locacao', N'codigo_funcionario'), (N'Locacao', N'id_endereco'),
    (N'Locacao_Equipamento', N'numero_locacao'), (N'Locacao_Equipamento', N'codigo_equipamento'),
    (N'Pagamento', N'numero'), (N'Pagamento', N'valor'), (N'Pagamento', N'data_pagamento'),
    (N'Pagamento', N'data_vencimento'), (N'Pagamento', N'status'), (N'Pagamento', N'numero_locacao');

IF EXISTS (
    SELECT 1 FROM @Colunas c
    WHERE COL_LENGTH(N'dbo.' + QUOTENAME(c.tabela), c.coluna) IS NULL
)
    THROW 50002, N'Uma ou mais colunas esperadas estão ausentes no banco selecionado.', 1;
GO

PRINT N'Validação concluída: as 13 tabelas e as colunas esperadas foram encontradas em ' + DB_NAME() + N'.';
GO
