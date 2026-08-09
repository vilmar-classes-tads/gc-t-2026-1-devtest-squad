package br.edu.ifpe.sistemaeditais.model;

public class PlanoDeTrabalho {

    private String titulo;
    private String descricaoAtividades;
    private byte[] arquivo;

    public byte[] getArquivo() {
        return arquivo;
    }

    public void setArquivo(byte[] arquivo) {
        this.arquivo = arquivo;
    }

    public PlanoDeTrabalho(String titulo, String descricaoAtividades) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("Título do plano de trabalho é obrigatório.");
        }
        if (descricaoAtividades == null || descricaoAtividades.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição das atividades é obrigatória.");
        }
        this.titulo = titulo;
        this.descricaoAtividades = descricaoAtividades;
    }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricaoAtividades() { return descricaoAtividades; }
    public void setDescricaoAtividades(String descricaoAtividades) { this.descricaoAtividades = descricaoAtividades; }
}
