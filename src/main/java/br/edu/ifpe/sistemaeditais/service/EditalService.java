package br.edu.ifpe.sistemaeditais.service;

import br.edu.ifpe.sistemaeditais.model.Edital;
import br.edu.ifpe.sistemaeditais.model.Perfil;
import br.edu.ifpe.sistemaeditais.model.Servidor;
import br.edu.ifpe.sistemaeditais.repository.EditalRepository;
import java.time.LocalDate;
import java.util.List;

public class EditalService {
    private final EditalRepository repository = new EditalRepository();

    public void criarEdital(Edital edital, Servidor executor) {
        validarAcessoAdmin(executor);
        
        if (repository.buscarPorNumero(edital.getNumero()) != null) {
            throw new IllegalArgumentException("Já existe um edital cadastrado com este número.");
        }
        
        validarPeriodos(edital.getDataInicioSubmissao(), edital.getDataFimSubmissao(),
                        edital.getDataInicioAvaliacao(), edital.getDataFimAvaliacao());
        
        repository.salvar(edital);
    }

    public void editarEdital(String numeroOriginal, Edital dadosAtualizados, Servidor executor) {
        validarAcessoAdmin(executor);
        
        Edital editalExistente = repository.buscarPorNumero(numeroOriginal);
        if (editalExistente == null) {
            throw new IllegalArgumentException("Edital não encontrado para edição.");
        }

        validarPeriodos(dadosAtualizados.getDataInicioSubmissao(), dadosAtualizados.getDataFimSubmissao(),
                        dadosAtualizados.getDataInicioAvaliacao(), dadosAtualizados.getDataFimAvaliacao());

        // Atualiza os campos mutáveis
        editalExistente.setTitulo(dadosAtualizados.getTitulo());
        editalExistente.setAno(dadosAtualizados.getAno());
        editalExistente.setDataInicioSubmissao(dadosAtualizados.getDataInicioSubmissao());
        editalExistente.setDataFimSubmissao(dadosAtualizados.getDataFimSubmissao());
        editalExistente.setDataInicioAvaliacao(dadosAtualizados.getDataInicioAvaliacao());
        editalExistente.setDataFimAvaliacao(dadosAtualizados.getDataFimAvaliacao());
    }

    public List<Edital> listarEditais(Servidor executor) {
        validarAcessoAdmin(executor);
        return repository.listarTodos();
    }

    // Regras de validação de datas (Início não pode ser maior que fim)
    private void validarPeriodos(LocalDate iniSub, LocalDate fimSub, LocalDate iniAv, LocalDate fimAv) {
        if (iniSub == null || fimSub == null || iniAv == null || fimAv == null) {
            throw new IllegalArgumentException("Todas as datas do edital são obrigatórias.");
        }
        if (iniSub.isAfter(fimSub)) {
            throw new IllegalArgumentException("A data de início da submissão não pode ser posterior ao fim.");
        }
        if (iniAv.isAfter(fimAv)) {
            throw new IllegalArgumentException("A data de início da avaliação não pode ser posterior ao fim.");
        }
        if (fimSub.isAfter(iniAv)) {
            throw new IllegalArgumentException("O período de avaliação só pode começar após o término das submissões.");
        }
    }

    private void validarAcessoAdmin(Servidor servidor) {
        if (servidor == null || !servidor.getPerfis().contains(Perfil.ROLE_ADMIN)) {
            throw new SecurityException("Acesso negado. Funcionalidade exclusiva para Administradores.");
        }
    }
}