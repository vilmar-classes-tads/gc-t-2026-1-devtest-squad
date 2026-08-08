package br.edu.ifpe.sistemaeditais.model;

import java.util.ArrayList;
import java.util.List;

public class Membro {

    public static final int MAX_PLANOS_DE_TRABALHO = 4;

    private String nome;
    private String cpf;
    private FuncaoMembro funcao;
    private int cargaHoraria;
    private List<PlanoDeTrabalho> planosDeTrabalho = new ArrayList<>();

    public Membro(String nome, String cpf, FuncaoMembro funcao, int cargaHoraria) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do membro é obrigatório.");
        }
        if (cpf == null || !cpf.matches("\\d{11}")) {
            throw new IllegalArgumentException("CPF inválido. Informe exatamente 11 dígitos numéricos.");
        }
        if (funcao == null) {
            throw new IllegalArgumentException("Função do membro é obrigatória.");
        }
        if (cargaHoraria <= 0) {
            throw new IllegalArgumentException("Carga horária (CH) deve ser maior que zero.");
        }
        this.nome = nome;
        this.cpf = cpf;
        this.funcao = funcao;
        this.cargaHoraria = cargaHoraria;
    }

    public void adicionarPlanoDeTrabalho(PlanoDeTrabalho plano) {
        if (plano == null) {
            throw new IllegalArgumentException("Plano de trabalho não pode ser nulo.");
        }
        if (planosDeTrabalho.size() >= MAX_PLANOS_DE_TRABALHO) {
            throw new IllegalStateException(
                "Limite máximo de " + MAX_PLANOS_DE_TRABALHO + " planos de trabalho atingido.");
        }
        planosDeTrabalho.add(plano);
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }

    public FuncaoMembro getFuncao() { return funcao; }
    public void setFuncao(FuncaoMembro funcao) { this.funcao = funcao; }

    public int getCargaHoraria() { return cargaHoraria; }
    public void setCargaHoraria(int cargaHoraria) { this.cargaHoraria = cargaHoraria; }

    public List<PlanoDeTrabalho> getPlanosDeTrabalho() { return planosDeTrabalho; }
}
