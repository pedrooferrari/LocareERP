package br.com.locare.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EstadoSistema implements Serializable {
    private static final long serialVersionUID = 1L;
    public List<Pessoa> pessoas = new ArrayList<>();
    public List<Cliente> clientes = new ArrayList<>();
    public List<Equipamento> equipamentos = new ArrayList<>();
    public List<Locacao> locacoes = new ArrayList<>();
    public List<Pagamento> pagamentos = new ArrayList<>();
    public List<Funcionario> funcionarios = new ArrayList<>();
    public List<Fornecedor> fornecedores = new ArrayList<>();
    public List<Cidade> cidades = new ArrayList<>();
    public List<Classificacao> classificacoes = new ArrayList<>();
    public List<Tipo> tipos = new ArrayList<>();
}
