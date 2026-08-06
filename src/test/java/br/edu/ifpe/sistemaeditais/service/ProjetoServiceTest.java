package br.edu.ifpe.sistemaeditais.service;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.ifpe.sistemaeditais.model.AreaFormacao;
import br.edu.ifpe.sistemaeditais.model.AreaTematica;
import br.edu.ifpe.sistemaeditais.model.Campus;
import br.edu.ifpe.sistemaeditais.model.ODS;
import br.edu.ifpe.sistemaeditais.model.Perfil;
import br.edu.ifpe.sistemaeditais.model.Projeto;
import br.edu.ifpe.sistemaeditais.model.Servidor;
import br.edu.ifpe.sistemaeditais.model.StatusProjeto;
import br.edu.ifpe.sistemaeditais.model.Titulacao;
import br.edu.ifpe.sistemaeditais.repository.ProjetoRepository;

@ExtendWith(MockitoExtension.class)
public class ProjetoServiceTest {

    @Mock
    private ProjetoRepository projetoRepository;

    @InjectMocks
    private ProjetoService projetoService;

    private Servidor coordenador;

    @BeforeEach
    public void configurar() {
        coordenador = new Servidor(
                "Coordenador Teste",
                "44444444444",
                "coordenador.ct046@ifpe.edu.br",
                "senha123",
                Campus.RECIFE,
                AreaFormacao.ENGENHARIAS,
                Titulacao.MESTRADO
        );
        coordenador.adicionarPerfil(Perfil.ROLE_COORDENADOR);
    }

    // Monta um projeto já persistido (fora do fluxo de criarProjeto) com o
    // status informado, usado pelos testes de edição (CT-056 a CT-061).
    private Projeto projetoValido(Servidor autor, StatusProjeto status) {
        Projeto projeto = new Projeto(
                "Título Original",
                "Resumo original do projeto.",
                "extensão, comunidade",
                "Comunidade local",
                AreaTematica.CIENCIAS_SOCIAIS_APLICADAS,
                Campus.RECIFE,
                List.of(ODS.ODS_4_EDUCACAO_QUALIDADE),
                true,
                autor
        );
        projeto.setStatus(status);
        return projeto;
    }

    @Test
    public void ct046_criarProjetoComTituloVazio() {
        List<ODS> odsSelecionados = Collections.emptyList();

        Projeto projeto = assertDoesNotThrow(() -> projetoService.criarProjeto(
                "",
                "Resumo projeto de teste.",
                "robótica, educação, extensão, tecnologia",
                "Estudantes da rede pública",
                AreaTematica.ENGENHARIAS,
                Campus.RECIFE,
                odsSelecionados,
                true,
                coordenador
        ));

        Assertions.assertEquals("", projeto.getTitulo());
        verify(projetoRepository, times(1)).salvar(projeto);
    }

    // CT-049 — Aceitar valores do Enum Campus na submissão de projetos
    @Test
    public void ct049_aceitarValoresDoEnumCampusNaSubmissao() {
        Projeto projeto = projetoService.criarProjeto(
                "Projeto Educação a Distância",
                "Resumo do projeto de teste.",
                "ead, tecnologia",
                "Estudantes do ensino técnico",
                AreaTematica.MULTIDISCIPLINAR,
                Campus.EAD,
                List.of(ODS.ODS_4_EDUCACAO_QUALIDADE),
                true,
                coordenador
        );

        assertEquals(Campus.EAD, projeto.getCampus());
        verify(projetoRepository, times(1)).salvar(projeto);
    }

    // CT-050 — Aceitar e converter lista com múltiplos ODS selecionados
    @Test
    public void ct050_aceitarListaComMultiplosOdsSelecionados() {
        List<ODS> odsSelecionados = List.of(
                ODS.ODS_4_EDUCACAO_QUALIDADE,
                ODS.ODS_6_AGUA_SANEAMENTO,
                ODS.ODS_9_INDUSTRIA_INOVACAO
        );

        Projeto projeto = projetoService.criarProjeto(
                "Projeto com múltiplos ODS",
                "Resumo do projeto de teste.",
                "sustentabilidade, inovação",
                "Comunidade ribeirinha",
                AreaTematica.ENGENHARIAS,
                Campus.RECIFE,
                odsSelecionados,
                true,
                coordenador
        );

        assertEquals(3, projeto.getOdsSelecionados().size());
        assertTrue(projeto.getOdsSelecionados().containsAll(odsSelecionados));
        verify(projetoRepository, times(1)).salvar(projeto);
    }

    // CT-051 — Aceitar submissão com seleção de ODS vazia ("0")
    @Test
    public void ct051_aceitarSubmissaoComSelecaoDeOdsVazia() {
        Projeto projeto = assertDoesNotThrow(() -> projetoService.criarProjeto(
                "Projeto sem ODS selecionado",
                "Resumo do projeto de teste.",
                "extensão",
                "Comunidade local",
                AreaTematica.CIENCIAS_HUMANAS,
                Campus.RECIFE,
                Collections.emptyList(),
                true,
                coordenador
        ));

        assertTrue(projeto.getOdsSelecionados().isEmpty());
        verify(projetoRepository, times(1)).salvar(projeto);
    }

    // CT-052 — Registrar e armazenar corretamente o aceite do Termo de Compromisso
    @Test
    public void ct052_registrarAceiteDoTermoDeCompromisso() {
        Projeto projeto = projetoService.criarProjeto(
                "Projeto com termo aceito",
                "Resumo do projeto de teste.",
                "extensão",
                "Comunidade local",
                AreaTematica.CIENCIAS_HUMANAS,
                Campus.RECIFE,
                List.of(ODS.ODS_4_EDUCACAO_QUALIDADE),
                true,
                coordenador
        );

        assertTrue(projeto.isAceitouTermoDeCompromisso());
    }

    // CT-053 — Permitir criação de projeto por usuário com perfil ROLE_COORDENADOR
    @Test
    public void ct053_permitirCriacaoDeProjetoPorCoordenador() {
        Projeto projeto = assertDoesNotThrow(() -> projetoService.criarProjeto(
                "Projeto do Coordenador",
                "Resumo do projeto de teste.",
                "extensão",
                "Comunidade local",
                AreaTematica.CIENCIAS_HUMANAS,
                Campus.RECIFE,
                List.of(ODS.ODS_4_EDUCACAO_QUALIDADE),
                true,
                coordenador
        ));

        verify(projetoRepository, times(1)).salvar(projeto);
    }

    // CT-054 — Rejeitar criação de projeto se o usuário não for coordenador
    @Test
    public void ct054_rejeitarCriacaoDeProjetoSeUsuarioNaoForCoordenador() {
        Servidor semPerfilDeCoordenador = new Servidor(
                "Servidor Sem Perfil",
                "55555555555",
                "servidor.ct054@ifpe.edu.br",
                "senha123",
                Campus.RECIFE,
                AreaFormacao.ENGENHARIAS,
                Titulacao.MESTRADO
        );

        Exception ex = assertThrows(SecurityException.class, () -> projetoService.criarProjeto(
                "Projeto Não Autorizado",
                "Resumo do projeto de teste.",
                "extensão",
                "Comunidade local",
                AreaTematica.CIENCIAS_HUMANAS,
                Campus.RECIFE,
                List.of(ODS.ODS_4_EDUCACAO_QUALIDADE),
                true,
                semPerfilDeCoordenador
        ));

        assertEquals("Acesso negado. Apenas coordenadores podem gerenciar projetos.", ex.getMessage());
        verifyNoInteractions(projetoRepository);
    }

    // CT-055 — Associar e salvar o projeto ao coordenador criador no repositório
    @Test
    public void ct055_associarESalvarProjetoAoCoordenadorCriador() {
        Projeto projeto = projetoService.criarProjeto(
                "Projeto do Coordenador",
                "Resumo do projeto de teste.",
                "extensão",
                "Comunidade local",
                AreaTematica.CIENCIAS_HUMANAS,
                Campus.RECIFE,
                List.of(ODS.ODS_4_EDUCACAO_QUALIDADE),
                true,
                coordenador
        );

        assertEquals(coordenador, projeto.getCoordenador());
        verify(projetoRepository, times(1)).salvar(projeto);
    }

    // CT-056 — Permitir edição de todos os campos para projeto em status RASCUNHO
    @Test
    public void ct056_permitirEdicaoDeTodosOsCamposEmRascunho() {
        Projeto projeto = projetoValido(coordenador, StatusProjeto.RASCUNHO);
        List<ODS> novosOds = List.of(ODS.ODS_9_INDUSTRIA_INOVACAO);

        assertDoesNotThrow(() -> projetoService.editarProjeto(
                projeto,
                "Título Atualizado",
                "Resumo atualizado.",
                "inovação, tecnologia",
                "Novo público-alvo",
                AreaTematica.ENGENHARIAS,
                Campus.CARUARU,
                novosOds,
                false,
                coordenador
        ));

        assertEquals("Título Atualizado", projeto.getTitulo());
        assertEquals("Resumo atualizado.", projeto.getResumo());
        assertEquals("inovação, tecnologia", projeto.getPalavrasChave());
        assertEquals("Novo público-alvo", projeto.getPublicoAlvo());
        assertEquals(AreaTematica.ENGENHARIAS, projeto.getAreaTematica());
        assertEquals(Campus.CARUARU, projeto.getCampus());
        assertEquals(novosOds, projeto.getOdsSelecionados());
        assertFalse(projeto.isAceitouTermoDeCompromisso());
        assertEquals(StatusProjeto.RASCUNHO, projeto.getStatus());
    }

    // CT-057 — Permitir edição de projeto enquanto estiver em status EM_CORRECAO
    @Test
    public void ct057_permitirEdicaoDeProjetoEmCorrecao() {
        Projeto projeto = projetoValido(coordenador, StatusProjeto.EM_CORRECAO);

        assertDoesNotThrow(() -> projetoService.editarProjeto(
                projeto,
                "Título Corrigido",
                "Resumo corrigido conforme apontamentos da avaliação.",
                "correção, ajuste",
                "Público-alvo revisado",
                AreaTematica.CIENCIAS_SOCIAIS_APLICADAS,
                Campus.RECIFE,
                List.of(ODS.ODS_4_EDUCACAO_QUALIDADE),
                true,
                coordenador
        ));

        assertEquals("Título Corrigido", projeto.getTitulo());
        assertEquals(StatusProjeto.EM_CORRECAO, projeto.getStatus());
    }

    // CT-058 — Rejeitar tentativa de edição em projeto com status SUBMETIDO
    @Test
    public void ct058_rejeitarEdicaoDeProjetoSubmetido() {
        Projeto projeto = projetoValido(coordenador, StatusProjeto.SUBMETIDO);

        Exception ex = assertThrows(IllegalStateException.class, () -> projetoService.editarProjeto(
                projeto,
                "Tentativa de Edição",
                "Resumo alterado.",
                "palavras-chave",
                "Público-alvo",
                AreaTematica.ENGENHARIAS,
                Campus.RECIFE,
                List.of(ODS.ODS_4_EDUCACAO_QUALIDADE),
                true,
                coordenador
        ));

        assertTrue(ex.getMessage().contains("SUBMETIDO"));
        assertEquals("Título Original", projeto.getTitulo());
    }

    // CT-059 — Rejeitar tentativa de edição em projeto com status APROVADO
    @Test
    public void ct059_rejeitarEdicaoDeProjetoAprovado() {
        Projeto projeto = projetoValido(coordenador, StatusProjeto.APROVADO);

        Exception ex = assertThrows(IllegalStateException.class, () -> projetoService.editarProjeto(
                projeto,
                "Tentativa de Edição",
                "Resumo alterado.",
                "palavras-chave",
                "Público-alvo",
                AreaTematica.ENGENHARIAS,
                Campus.RECIFE,
                List.of(ODS.ODS_4_EDUCACAO_QUALIDADE),
                true,
                coordenador
        ));

        assertTrue(ex.getMessage().contains("APROVADO"));
        assertEquals("Título Original", projeto.getTitulo());
    }

    // CT-060 — Rejeitar tentativa de edição em projeto com status REPROVADO
    @Test
    public void ct060_rejeitarEdicaoDeProjetoReprovado() {
        Projeto projeto = projetoValido(coordenador, StatusProjeto.REPROVADO);

        Exception ex = assertThrows(IllegalStateException.class, () -> projetoService.editarProjeto(
                projeto,
                "Tentativa de Edição",
                "Resumo alterado.",
                "palavras-chave",
                "Público-alvo",
                AreaTematica.ENGENHARIAS,
                Campus.RECIFE,
                List.of(ODS.ODS_4_EDUCACAO_QUALIDADE),
                true,
                coordenador
        ));

        assertTrue(ex.getMessage().contains("REPROVADO"));
        assertEquals("Título Original", projeto.getTitulo());
    }

    // CT-061 — Rejeitar edição quando o solicitante não for o coordenador autor do projeto
    @Test
    public void ct061_rejeitarEdicaoQuandoSolicitanteNaoForOAutor() {
        Projeto projeto = projetoValido(coordenador, StatusProjeto.RASCUNHO);

        Servidor outroCoordenador = new Servidor(
                "Outro Coordenador",
                "66666666666",
                "outro.ct061@ifpe.edu.br",
                "senha123",
                Campus.RECIFE,
                AreaFormacao.ENGENHARIAS,
                Titulacao.MESTRADO
        );
        outroCoordenador.adicionarPerfil(Perfil.ROLE_COORDENADOR);

        Exception ex = assertThrows(SecurityException.class, () -> projetoService.editarProjeto(
                projeto,
                "Tentativa de Edição Indevida",
                "Resumo alterado.",
                "palavras-chave",
                "Público-alvo",
                AreaTematica.ENGENHARIAS,
                Campus.RECIFE,
                List.of(ODS.ODS_4_EDUCACAO_QUALIDADE),
                true,
                outroCoordenador
        ));

        assertEquals("Apenas o coordenador do projeto pode editá-lo.", ex.getMessage());
        assertEquals("Título Original", projeto.getTitulo());
    }

    // CT-062 — Retornar apenas projetos pertencentes ao coordenador logado
    @Test
    public void ct062_retornarApenasProjetosDoCoordenadorLogado() {
        Projeto projetoDoCoordenador = projetoValido(coordenador, StatusProjeto.RASCUNHO);
        when(projetoRepository.buscarPorCoordenador(coordenador))
                .thenReturn(List.of(projetoDoCoordenador));

        List<Projeto> resultado = projetoService.listarProjetosDoCoordenador(coordenador);

        assertEquals(1, resultado.size());
        assertEquals(coordenador, resultado.get(0).getCoordenador());
        verify(projetoRepository, times(1)).buscarPorCoordenador(coordenador);
    }

    // CT-063 — Permitir ao Administrador (ROLE_ADMIN) listar todos os projetos do sistema
    @Test
    public void ct063_permitirAdminListarTodosOsProjetos() {
        Servidor admin = new Servidor(
                "Admin Teste",
                "77777777777",
                "admin.ct063@ifpe.edu.br",
                "senha123",
                Campus.RECIFE,
                AreaFormacao.CIENCIAS_EXATAS_E_DA_TERRA,
                Titulacao.DOUTORADO
        );
        admin.adicionarPerfil(Perfil.ROLE_ADMIN);

        Projeto projetoDeUmCoordenador = projetoValido(coordenador, StatusProjeto.SUBMETIDO);
        when(projetoRepository.listarTodos())
                .thenReturn(List.of(projetoDeUmCoordenador));

        List<Projeto> resultado = assertDoesNotThrow(() -> projetoService.listarTodos(admin));

        assertEquals(1, resultado.size());
        verify(projetoRepository, times(1)).listarTodos();
    }

    // CT-064 — Rejeitar acesso à listagem geral para coordenadores sem perfil de Admin
    @Test
    public void ct064_rejeitarListagemGeralParaCoordenadorSemPerfilAdmin() {
        Exception ex = assertThrows(SecurityException.class, () -> projetoService.listarTodos(coordenador));

        assertEquals("Apenas administradores podem listar todos os projetos.", ex.getMessage());
        verifyNoInteractions(projetoRepository);
    }
}
