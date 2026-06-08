package br.edu.ifpe.sistemaeditais.service;

import br.edu.ifpe.sistemaeditais.model.AreaTematica;
import br.edu.ifpe.sistemaeditais.model.Campus;
import br.edu.ifpe.sistemaeditais.model.ODS;
import br.edu.ifpe.sistemaeditais.model.Perfil;
import br.edu.ifpe.sistemaeditais.model.Projeto;
import br.edu.ifpe.sistemaeditais.model.Servidor;
import br.edu.ifpe.sistemaeditais.repository.ProjetoRepository;

import java.util.List;

public class ProjetoService {

    private final ProjetoRepository projetoRepository;

    public ProjetoService(ProjetoRepository projetoRepository) {
        this.projetoRepository = projetoRepository;
    }

    public Projeto criarProjeto(String titulo, String resumo, String palavrasChave,
                                 String publicoAlvo, AreaTematica areaTematica, Campus campus,
                                 List<ODS> odsSelecionados, boolean aceitouTermo,
                                 Servidor coordenador) {
        validarCoordenador(coordenador);
        Projeto projeto = new Projeto(titulo, resumo, palavrasChave, publicoAlvo,
                areaTematica, campus, odsSelecionados, aceitouTermo, coordenador);
        projetoRepository.salvar(projeto);
        return projeto;
    }

    public void editarProjeto(Projeto projeto, String titulo, String resumo,
                               String palavrasChave, String publicoAlvo,
                               AreaTematica areaTematica, Campus campus,
                               List<ODS> odsSelecionados, boolean aceitouTermo,
                               Servidor solicitante) {
        validarCoordenador(solicitante);
        if (!projeto.getCoordenador().equals(solicitante)) {
            throw new SecurityException("Apenas o coordenador do projeto pode editá-lo.");
        }
        // Regra de status encapsulada no modelo (Projeto.editarProjeto)
        projeto.editarProjeto(titulo, resumo, palavrasChave, publicoAlvo,
                areaTematica, campus, odsSelecionados, aceitouTermo);
    }

    public List<Projeto> listarProjetosDoCoordenador(Servidor coordenador) {
        validarCoordenador(coordenador);
        return projetoRepository.buscarPorCoordenador(coordenador);
    }

    public List<Projeto> listarTodos(Servidor solicitante) {
        if (solicitante == null || !solicitante.getPerfis().contains(Perfil.ROLE_ADMIN)) {
            throw new SecurityException("Apenas administradores podem listar todos os projetos.");
        }
        return projetoRepository.listarTodos();
    }

    private void validarCoordenador(Servidor servidor) {
        if (servidor == null) {
            throw new SecurityException("Usuário não autenticado.");
        }
        if (!servidor.getPerfis().contains(Perfil.ROLE_COORDENADOR)
                && !servidor.getPerfis().contains(Perfil.ROLE_ADMIN)) {
            throw new SecurityException("Acesso negado. Apenas coordenadores podem gerenciar projetos.");
        }
    }
}