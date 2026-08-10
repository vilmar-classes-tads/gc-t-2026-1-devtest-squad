package br.edu.ifpe.sistemaeditais.service;

import java.util.List;
import java.util.stream.Collectors;

import br.edu.ifpe.sistemaeditais.model.AreaTematica;
import br.edu.ifpe.sistemaeditais.model.Campus;
import br.edu.ifpe.sistemaeditais.model.Edital;
import br.edu.ifpe.sistemaeditais.model.Membro;
import br.edu.ifpe.sistemaeditais.model.ODS;
import br.edu.ifpe.sistemaeditais.model.Perfil;
import br.edu.ifpe.sistemaeditais.model.PlanoDeTrabalho;
import br.edu.ifpe.sistemaeditais.model.Projeto;
import br.edu.ifpe.sistemaeditais.model.Servidor;
import br.edu.ifpe.sistemaeditais.model.StatusProjeto;
import br.edu.ifpe.sistemaeditais.repository.ProjetoRepository;

public class ProjetoService {

    private static final List<StatusProjeto> STATUS_PERTINENTES_GESTOR =
            List.of(StatusProjeto.SUBMETIDO, StatusProjeto.EM_CORRECAO, StatusProjeto.EM_AVALIACAO);


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

    

    public void adicionarMembro(Projeto projeto, Membro membro, Servidor solicitante) {
        validarCoordenador(solicitante);
        validarDonoDoProjeto(projeto, solicitante);
        projeto.adicionarMembro(membro);
    }

    public void removerMembro(Projeto projeto, String cpfMembro, Servidor solicitante) {
        validarCoordenador(solicitante);
        validarDonoDoProjeto(projeto, solicitante);
        projeto.removerMembro(cpfMembro);
    }

    public List<Membro> listarEquipe(Projeto projeto, Servidor solicitante) {
        validarCoordenador(solicitante);
        validarDonoDoProjeto(projeto, solicitante);
        return projeto.getEquipe();
    }

    public void adicionarPlanoDeTrabalho(Projeto projeto, String cpfMembro,
                                          PlanoDeTrabalho plano, Servidor solicitante) {
        validarCoordenador(solicitante);
        validarDonoDoProjeto(projeto, solicitante);
        Membro membro = projeto.buscarMembroPorCpf(cpfMembro);
        if (membro == null) {
            throw new IllegalArgumentException("Membro não encontrado na equipe do projeto.");
        }
        membro.adicionarPlanoDeTrabalho(plano);
    }

    private void validarDonoDoProjeto(Projeto projeto, Servidor solicitante) {
        if (projeto == null) {
            throw new IllegalArgumentException("Projeto não informado.");
        }
        if (!projeto.getCoordenador().equals(solicitante)) {
            throw new SecurityException("Apenas o coordenador do projeto pode gerenciar a equipe.");
        }
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

    private void validarAdmin(Servidor servidor) {
        if (servidor == null || !servidor.getPerfis().contains(Perfil.ROLE_ADMIN)) {
            throw new SecurityException("Acesso negado. Apenas administradores podem acessar esta listagem.");
        }
    }

    private void validarGestor(Servidor servidor) {
        if (servidor == null
                || (!servidor.getPerfis().contains(Perfil.ROLE_GESTOR)
                    && !servidor.getPerfis().contains(Perfil.ROLE_ADMIN))) {
            throw new SecurityException("Acesso negado. Apenas gestores/diretores podem acessar esta listagem.");
        }
    }

    public List<Projeto> listarProjetosParaAdmin(Servidor admin, Edital editalFiltro,
                                                  Campus campusFiltro, AreaTematica areaFiltro,
                                                  StatusProjeto statusFiltro) {
        validarAdmin(admin);
        return projetoRepository.listarTodos().stream()
                .filter(p -> editalFiltro == null || editalFiltro.equals(p.getEdital()))
                .filter(p -> campusFiltro == null || campusFiltro.equals(p.getCampus()))
                .filter(p -> areaFiltro == null || areaFiltro.equals(p.getAreaTematica()))
                .filter(p -> statusFiltro == null || statusFiltro.equals(p.getStatus()))
                .collect(Collectors.toList());
    }

  
    public List<Projeto> listarProjetosParaGestor(Servidor gestor) {
        validarGestor(gestor);
        return projetoRepository.listarTodos().stream()
                .filter(p -> gestor.getCampus() != null && gestor.getCampus().equals(p.getCampus()))
                .filter(p -> STATUS_PERTINENTES_GESTOR.contains(p.getStatus()))
                .collect(Collectors.toList());
    }

    
    public boolean podeBaixarArquivos(Servidor solicitante, Projeto projeto) {
        if (solicitante == null || projeto == null) {
            return false;
        }

        if (solicitante.getPerfis().contains(Perfil.ROLE_ADMIN)) {
            return true;
        }

        if (solicitante.getPerfis().contains(Perfil.ROLE_GESTOR)) {
            return solicitante.getCampus() != null
                    && solicitante.getCampus().equals(projeto.getCampus());
        }

        return projeto.getCoordenador() != null
                && projeto.getCoordenador().equals(solicitante);
    }

    public byte[] baixarPlanoDeTrabalho(Long projetoId, String cpfMembro, Servidor solicitante) {
        Projeto projeto = projetoRepository.buscarPorId(projetoId);

        if (projeto == null) {
            throw new IllegalArgumentException("Projeto não encontrado.");
        }

        if (!podeBaixarArquivos(solicitante, projeto)) {
            throw new SecurityException("Acesso negado ao plano de trabalho.");
        }

        Membro membro = projeto.buscarMembroPorCpf(cpfMembro);

        if (membro == null) {
            throw new IllegalArgumentException("Membro não encontrado.");
        }

        List<PlanoDeTrabalho> planos = membro.getPlanosDeTrabalho();

        if (planos == null || planos.isEmpty()) {
            throw new IllegalStateException("Membro não possui plano de trabalho.");
        }

        PlanoDeTrabalho plano = planos.get(0);

        if (plano.getArquivo() == null) {
            throw new IllegalStateException("Plano de trabalho não possui arquivo.");
        }

        return plano.getArquivo();
    }

    public byte[] baixarAnexo(Long projetoId, Servidor solicitante) {
        if (projetoId == null) {
            throw new IllegalArgumentException("Projeto não informado.");
        }

        Projeto projeto = projetoRepository.buscarPorId(projetoId);

        if (projeto == null) {
            throw new IllegalArgumentException("Projeto não encontrado.");
        }

        if (!podeBaixarArquivos(solicitante, projeto)) {
            throw new SecurityException("Acesso negado ao arquivo.");
        }

        if (projeto.getAnexo() == null) {
            throw new IllegalStateException("Projeto não possui anexo.");
        }

        return projeto.getAnexo();
    }

    public void enviarProjetoParaSubmissao(Projeto projeto, Servidor solicitante) {
        validarCoordenador(solicitante);
        if (!projeto.getCoordenador().equals(solicitante)) {
            throw new SecurityException("Apenas o coordenador do projeto pode submetê-lo.");
        }
        if (projeto.getStatus() != StatusProjeto.RASCUNHO && projeto.getStatus() != StatusProjeto.EM_CORRECAO) {
            throw new IllegalStateException("Apenas projetos em RASCUNHO ou EM_CORRECAO podem ser submetidos.");
        }
        projeto.setStatus(StatusProjeto.SUBMETIDO);
    }
}