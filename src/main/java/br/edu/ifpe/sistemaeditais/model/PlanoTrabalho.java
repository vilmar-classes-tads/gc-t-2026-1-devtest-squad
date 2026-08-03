package br.edu.ifpe.sistemaeditais.model;

public class PlanoTrabalho {

    private String titulo;

    public PlanoTrabalho() {
    }

    public PlanoTrabalho(String titulo) {
        this.titulo = titulo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
}
