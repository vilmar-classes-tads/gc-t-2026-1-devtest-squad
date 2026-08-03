package br.edu.ifpe.sistemaeditais.service;

import br.edu.ifpe.sistemaeditais.model.Membro;
import br.edu.ifpe.sistemaeditais.model.Projeto;
import br.edu.ifpe.sistemaeditais.repository.ProjetoRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MembroService {

    private final ProjetoRepository projetoRepository;
    private final Map<Projeto, List<Membro>> membrosPorProjeto = new HashMap<>();

    public MembroService(ProjetoRepository projetoRepository) {
        this.projetoRepository = projetoRepository;
    }

    public void adicionarMembro(Projeto projeto, Membro membro) {
        validarProjeto(projeto);
        validarMembro(membro);

        List<Membro> membros = membrosPorProjeto.computeIfAbsent(projeto, key -> new ArrayList<>());

        boolean cpfJaCadastrado = membros.stream()
                .anyMatch(m -> membro.getCpf().equals(m.getCpf()));

        if (cpfJaCadastrado) {
            throw new IllegalArgumentException("CPF já cadastrado na equipe.");
        }

        membros.add(membro);
    }

    public void removerMembro(Projeto projeto, String cpf) {
        validarProjeto(projeto);
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF inválido");
        }

        List<Membro> membros = membrosPorProjeto.get(projeto);
        if (membros == null) {
            return;
        }

        membros.removeIf(m -> cpf.equals(m.getCpf()));
    }

    public List<Membro> listarMembros(Projeto projeto) {
        validarProjeto(projeto);
        List<Membro> membros = membrosPorProjeto.get(projeto);
        if (membros == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(membros);
    }

    private void validarProjeto(Projeto projeto) {
        if (projeto == null) {
            throw new IllegalArgumentException("Projeto não pode ser nulo");
        }
    }

    private void validarMembro(Membro membro) {
        if (membro == null) {
            throw new IllegalArgumentException("Membro não pode ser nulo");
        }

        String cpf = membro.getCpf();
        if (cpf == null || cpf.length() != 11 || !cpf.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("CPF inválido");
        }
    }
}
