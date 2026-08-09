package br.edu.ifpe.sistemaeditais.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import br.edu.ifpe.sistemaeditais.model.Projeto;
import br.edu.ifpe.sistemaeditais.model.Servidor;

public class ProjetoRepository {

    private final List<Projeto> projetos = new ArrayList<>();

    private Long proximoId = 1L;

    public void salvar(Projeto projeto) {
        if (projeto == null) {
            throw new IllegalArgumentException("Projeto não pode ser nulo.");
        }
        projeto.setId(proximoId++);

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

    public Projeto buscarPorId(Long id) {
        return projetos.stream()
                .filter(p -> p.getId() != null && p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}