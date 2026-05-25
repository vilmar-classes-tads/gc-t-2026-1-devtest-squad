package br.edu.ifpe.sistemaeditais.model;

import java.time.LocalDate;

public class Edital {
    private String numero;
    private String titulo;
    private int ano;
    private LocalDate dataInicioSubmissao;
    private LocalDate dataFimSubmissao;
    private LocalDate dataInicioAvaliacao;
    private LocalDate dataFimAvaliacao;

    public Edital(String numero, String titulo, int ano, LocalDate dataInicioSubmissao, LocalDate dataFimSubmissao, LocalDate dataInicioAvaliacao, LocalDate dataFimAvaliacao) {
        this.numero = numero;
        this.titulo = titulo;
        this.ano = ano;
        this.dataInicioSubmissao = dataInicioSubmissao;
        this.dataFimSubmissao = dataFimSubmissao;
        this.dataInicioAvaliacao = dataInicioAvaliacao;
        this.dataFimAvaliacao = dataFimAvaliacao;
    }

    // Getters e Setters
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public int getAno() { return ano; }
    public void setAno(int ano) { this.ano = ano; }

    public LocalDate getDataInicioSubmissao() { return dataInicioSubmissao; }
    public void setDataInicioSubmissao(LocalDate dataInicioSubmissao) { this.dataInicioSubmissao = dataInicioSubmissao; }

    public LocalDate getDataFimSubmissao() { return dataFimSubmissao; }
    public void setDataFimSubmissao(LocalDate dataFimSubmissao) { this.dataFimSubmissao = dataFimSubmissao; }

    public LocalDate getDataInicioAvaliacao() { return dataInicioAvaliacao; }
    public void setDataInicioAvaliacao(LocalDate dataInicioAvaliacao) { this.dataInicioAvaliacao = dataInicioAvaliacao; }

    public LocalDate getDataFimAvaliacao() { return dataFimAvaliacao; }
    public void setDataFimAvaliacao(LocalDate dataFimAvaliacao) { this.dataFimAvaliacao = dataFimAvaliacao; }
}