package br.edu.ifpe.sistemaeditais.service;

import br.edu.ifpe.sistemaeditais.model.Membro;
import br.edu.ifpe.sistemaeditais.model.PlanoTrabalho;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanoTrabalhoService {

    private final Map<Membro, List<PlanoTrabalho>> planosPorMembro = new HashMap<>();

    public void adicionarPlano(Membro membro, PlanoTrabalho plano) {
        validarMembro(membro);
        validarPlano(plano);

        List<PlanoTrabalho> planos = planosPorMembro.computeIfAbsent(membro, key -> new ArrayList<>());
        if (planos.size() >= 4) {
            throw new IllegalStateException("Limite de planos excedido");
        }
        planos.add(plano);
    }

    public List<PlanoTrabalho> listarPlanosDoMembro(Membro membro) {
        validarMembro(membro);
        List<PlanoTrabalho> planos = planosPorMembro.get(membro);
        if (planos == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(planos);
    }

    private void validarMembro(Membro membro) {
        if (membro == null) {
            throw new IllegalArgumentException("Membro não pode ser nulo");
        }
    }

    private void validarPlano(PlanoTrabalho plano) {
        if (plano == null) {
            throw new IllegalArgumentException("Plano não pode ser nulo");
        }
    }
}
