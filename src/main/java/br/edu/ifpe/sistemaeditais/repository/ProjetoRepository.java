package br.edu.ifpe.sistemaeditais.repository;

import br.edu.ifpe.sistemaeditais.model.Projeto;
import br.edu.ifpe.sistemaeditais.model.Servidor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProjetoRepository {

    private final List<Projeto> projetos = new ArrayList<>();

    public void salvar(Projeto projeto) {
        projetos.add(projeto);
    }

    public List<Projeto> buscarPorCoordenador(Servidor coordenador) {
        return projetos.stream()
                .filter(p -> p.getCoordenador().equals(coordenador))
                .collect(Collectors.toList());
    }

    public Projeto buscarPorTitulo(String titulo) {
        return projetos.stream()
                .filter(p -> p.getTitulo().equalsIgnoreCase(titulo))
                .findFirst()
                .orElse(null);
    }

    public List<Projeto> listarTodos() {
        return new ArrayList<>(projetos);
    }
}