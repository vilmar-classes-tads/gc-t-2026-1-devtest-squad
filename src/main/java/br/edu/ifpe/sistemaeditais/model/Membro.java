package br.edu.ifpe.sistemaeditais.model;

public class Membro {

    private String nome;
    private String cpf;
    private String funcao;
    private String cargaHoraria;

    public Membro() {
    }

    public Membro(String nome, String cpf, String funcao, String cargaHoraria) {
        this.nome = nome;
        this.cpf = cpf;
        this.funcao = funcao;
        this.cargaHoraria = cargaHoraria;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getFuncao() {
        return funcao;
    }

    public void setFuncao(String funcao) {
        this.funcao = funcao;
    }

    public String getCargaHoraria() {
        return cargaHoraria;
    }

    public void setCargaHoraria(String cargaHoraria) {
        this.cargaHoraria = cargaHoraria;
    }
}
