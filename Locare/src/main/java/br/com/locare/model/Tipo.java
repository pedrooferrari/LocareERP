package br.com.locare.model;

import java.io.Serializable;

public class Tipo implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int codigo;
    private String modelo;
    private String marca;
    private String status;

    public Tipo(int codigo, String modelo, String marca, String status) {
        if (modelo == null || marca == null) {
            throw new IllegalArgumentException("Modelo e marca são obrigatórios para o tipo.");
        }
        this.codigo = codigo;
        this.modelo = modelo;
        this.marca = marca;
        this.status = status;
    }

    public int getCodigo() { return codigo; }
    public int getId() { return codigo; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) {
        if (modelo == null) throw new IllegalArgumentException("O modelo é obrigatório.");
        this.modelo = modelo;
    }
    public String getMarca() { return marca; }
    public void setMarca(String marca) {
        if (marca == null) throw new IllegalArgumentException("A marca é obrigatória.");
        this.marca = marca;
    }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
