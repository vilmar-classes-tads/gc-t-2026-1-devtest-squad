package br.edu.ifpe.sistemaeditais.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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
import br.edu.ifpe.sistemaeditais.model.Edital;
import br.edu.ifpe.sistemaeditais.model.FuncaoMembro;
import br.edu.ifpe.sistemaeditais.model.Membro;
import br.edu.ifpe.sistemaeditais.model.ODS;
import br.edu.ifpe.sistemaeditais.model.Perfil;
import br.edu.ifpe.sistemaeditais.model.PlanoDeTrabalho;
import br.edu.ifpe.sistemaeditais.model.Projeto;
import br.edu.ifpe.sistemaeditais.model.Servidor;
import br.edu.ifpe.sistemaeditais.model.Sexo;
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

    private Projeto projetoParaListagem(Servidor autor, Campus campus,
                                         AreaTematica area, StatusProjeto status, Edital edital) {
        Projeto projeto = new Projeto(
                "Projeto para Listagem",
                "Resumo do projeto de teste.",
                "palavras-chave",
                "Público-alvo",
                area,
                campus,
                List.of(ODS.ODS_4_EDUCACAO_QUALIDADE),
                true,
                autor
        );
        projeto.setStatus(status);
        projeto.setEdital(edital);
        return projeto;
    }
 
    private Edital editalDeTeste(String numero) {
        return new Edital(numero, "Edital de Teste", 2026,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31),
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 15));
    }
 
    private Servidor admin() {
        Servidor admin = new Servidor(
                "Admin Geral",
                "77777777777",
                "admin.listagem@ifpe.edu.br",
                "senha123",
                Campus.RECIFE,
                AreaFormacao.CIENCIAS_EXATAS_E_DA_TERRA,
                Titulacao.DOUTORADO
        );
        admin.adicionarPerfil(Perfil.ROLE_ADMIN);
        return admin;
    }
 
    private Servidor gestor(Campus campusDoGestor) {
        Servidor gestor = new Servidor(
                "Gestor de Campus",
                "88888888888",
                "gestor.listagem@ifpe.edu.br",
                "senha123",
                campusDoGestor,
                AreaFormacao.CIENCIAS_SOCIAIS_APLICADAS,
                Titulacao.MESTRADO
        );
        gestor.adicionarPerfil(Perfil.ROLE_GESTOR);
        return gestor;
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

    private Servidor servidorBaseEquipe() {
        Servidor servidor = new Servidor(
                "Maria da Silva Souza",
                "12345678901",
                "maria.silva@ifpe.edu.br",
                "senha123",
                Campus.RECIFE,
                AreaFormacao.ENGENHARIAS,
                Titulacao.MESTRADO
        );
        servidor.setSexo(Sexo.FEMININO);
        servidor.adicionarPerfil(Perfil.ROLE_COORDENADOR);
        return servidor;
    }

    private Projeto projetoBaseEquipe(Servidor coordenadorDoProjeto) {
        return new Projeto(
                "Robótica Educacional na Rede Pública",
                "Projeto de extensão para introduzir conceitos de robótica e programação em "
                        + "escolas públicas da região metropolitana do Recife.",
                "robótica, educação, extensão, tecnologia",
                "Estudantes do ensino médio da rede pública",
                AreaTematica.ENGENHARIAS,
                Campus.RECIFE,
                List.of(ODS.ODS_4_EDUCACAO_QUALIDADE, ODS.ODS_9_INDUSTRIA_INOVACAO),
                true,
                coordenadorDoProjeto
        );
    }

    private Membro membroValido(String cpf) {
        return new Membro("João Pedro Alves", cpf, FuncaoMembro.BOLSISTA, 20);
    }

    // CT-065 — Adicionar membro com dados válidos
    @Test
    public void ct065_adicionarMembroComDadosValidos() {
        Servidor coordenadorProjeto = servidorBaseEquipe();
        Projeto projeto = projetoBaseEquipe(coordenadorProjeto);
        Membro membro = membroValido("98765432100");

        assertDoesNotThrow(() -> projetoService.adicionarMembro(projeto, membro, coordenadorProjeto));

        assertEquals(1, projeto.getEquipe().size());
        assertTrue(projeto.getEquipe().contains(membro));
    }

    // CT-066 — Remover membro da equipe
    @Test
    public void ct066_removerMembroDaEquipe() {
        Servidor coordenadorProjeto = servidorBaseEquipe();
        Projeto projeto = projetoBaseEquipe(coordenadorProjeto);
        Membro membro = membroValido("98765432100");
        projetoService.adicionarMembro(projeto, membro, coordenadorProjeto);

        assertDoesNotThrow(() -> projetoService.removerMembro(projeto, "98765432100", coordenadorProjeto));

        assertTrue(projeto.getEquipe().isEmpty());
    }

    // CT-067 — Adicionar plano de trabalho dentro do limite permitido
    @Test
    public void ct067_adicionarPlanoDeTrabalhoDentroDoLimitePermitido() {
        Servidor coordenadorProjeto = servidorBaseEquipe();
        Projeto projeto = projetoBaseEquipe(coordenadorProjeto);
        Membro membro = membroValido("98765432100");
        projetoService.adicionarMembro(projeto, membro, coordenadorProjeto);

        PlanoDeTrabalho plano = new PlanoDeTrabalho(
                "Plano de Iniciação Científica",
                "Atividades de apoio à pesquisa em robótica educacional."
        );

        assertDoesNotThrow(() ->
                projetoService.adicionarPlanoDeTrabalho(projeto, "98765432100", plano, coordenadorProjeto));

        assertEquals(1, membro.getPlanosDeTrabalho().size());
    }

    // CT-068 — Permitir cadastro até o limite de 4 planos
    @Test
    public void ct068_permitirCadastroAteOLimiteDeQuatroPlanos() {
        Servidor coordenadorProjeto = servidorBaseEquipe();
        Projeto projeto = projetoBaseEquipe(coordenadorProjeto);
        Membro membro = membroValido("98765432100");
        projetoService.adicionarMembro(projeto, membro, coordenadorProjeto);

        for (int i = 1; i <= 4; i++) {
            PlanoDeTrabalho plano = new PlanoDeTrabalho("Plano " + i, "Atividades do plano " + i);
            assertDoesNotThrow(() ->
                    projetoService.adicionarPlanoDeTrabalho(projeto, "98765432100", plano, coordenadorProjeto));
        }

        assertEquals(4, membro.getPlanosDeTrabalho().size());
    }

    // CT-069 — Rejeitar adição acima do limite de planos
    @Test
    public void ct069_rejeitarAdicaoAcimaDoLimiteDePlanos() {
        Servidor coordenadorProjeto = servidorBaseEquipe();
        Projeto projeto = projetoBaseEquipe(coordenadorProjeto);
        Membro membro = membroValido("98765432100");
        projetoService.adicionarMembro(projeto, membro, coordenadorProjeto);

        for (int i = 1; i <= 4; i++) {
            projetoService.adicionarPlanoDeTrabalho(
                    projeto, "98765432100",
                    new PlanoDeTrabalho("Plano " + i, "Atividades do plano " + i),
                    coordenadorProjeto
            );
        }

        Exception ex = assertThrows(IllegalStateException.class, () ->
                projetoService.adicionarPlanoDeTrabalho(
                        projeto, "98765432100",
                        new PlanoDeTrabalho("Plano 5", "Atividades do plano 5"),
                        coordenadorProjeto
                ));

        assertTrue(ex.getMessage().contains("4"));
        assertEquals(4, membro.getPlanosDeTrabalho().size());
    }

    // CT-070 — Rejeitar membro com CPF inválido
    @Test
    public void ct070_rejeitarMembroComCpfInvalido() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new Membro("João Pedro Alves", "123", FuncaoMembro.BOLSISTA, 20));

        assertTrue(ex.getMessage().toLowerCase().contains("cpf"));
    }

    // CT-071 — Aceitar membro com CPF válido e único
    @Test
    public void ct071_aceitarMembroComCpfValidoEUnico() {
        Servidor coordenadorProjeto = servidorBaseEquipe();
        Projeto projeto = projetoBaseEquipe(coordenadorProjeto);
        Membro membro = membroValido("98765432100");

        assertDoesNotThrow(() -> projetoService.adicionarMembro(projeto, membro, coordenadorProjeto));

        assertEquals("98765432100", projeto.getEquipe().get(0).getCpf());
    }

    // CT-072 — Rejeitar adição de membro com CPF já cadastrado
    @Test
    public void ct072_rejeitarAdicaoDeMembroComCpfJaCadastrado() {
        Servidor coordenadorProjeto = servidorBaseEquipe();
        Projeto projeto = projetoBaseEquipe(coordenadorProjeto);
        projetoService.adicionarMembro(projeto, membroValido("98765432100"), coordenadorProjeto);

        Membro duplicado = new Membro("Outro Nome", "98765432100", FuncaoMembro.VOLUNTARIO, 10);

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                projetoService.adicionarMembro(projeto, duplicado, coordenadorProjeto));

        assertTrue(ex.getMessage().toLowerCase().contains("cpf"));
        assertEquals(1, projeto.getEquipe().size());
    }

    // CT-073 — Listar membros da equipe com sucesso
    @Test
    public void ct073_listarMembrosDaEquipeComSucesso() {
        Servidor coordenadorProjeto = servidorBaseEquipe();
        Projeto projeto = projetoBaseEquipe(coordenadorProjeto);
        Membro membro1 = membroValido("98765432100");
        Membro membro2 = new Membro("Carla Souza", "11223344556", FuncaoMembro.VOLUNTARIO, 10);
        projetoService.adicionarMembro(projeto, membro1, coordenadorProjeto);
        projetoService.adicionarMembro(projeto, membro2, coordenadorProjeto);

        List<Membro> equipe = assertDoesNotThrow(() ->
                projetoService.listarEquipe(projeto, coordenadorProjeto));

        assertEquals(2, equipe.size());
        assertTrue(equipe.contains(membro1));
        assertTrue(equipe.contains(membro2));
    }

    @Test
    public void ct074_adminVisualizaTodosProjetos() {
        Servidor admin = admin();

        Edital e1 = editalDeTeste("01/2026");
        Edital e2 = editalDeTeste("02/2026");

        Projeto a = projetoParaListagem(coordenador, Campus.RECIFE, AreaTematica.ENGENHARIAS, StatusProjeto.EM_AVALIACAO, e1);
        Projeto b = projetoParaListagem(coordenador, Campus.IPOJUCA, AreaTematica.ENGENHARIAS, StatusProjeto.RASCUNHO, e1);
        Projeto c = projetoParaListagem(coordenador, Campus.RECIFE, AreaTematica.CIENCIAS_DA_SAUDE, StatusProjeto.APROVADO, e2);
        Projeto d = projetoParaListagem(coordenador, Campus.IPOJUCA, AreaTematica.ENGENHARIAS, StatusProjeto.EM_CORRECAO, e2);

        when(projetoRepository.listarTodos()).thenReturn(List.of(a,b,c,d));

        List<Projeto> resultado = projetoService.listarProjetosParaAdmin(admin, null, null, null, null);

        assertEquals(4, resultado.size());
        assertTrue(resultado.containsAll(List.of(a,b,c,d)));
    }  

    @Test
    public void ct075_gestorVisualizaProjetosDoSeuCampus() {
        Servidor gestor = gestor(Campus.RECIFE);

        Edital e = editalDeTeste("01/2026");

        Projeto a = projetoParaListagem(coordenador, Campus.RECIFE, AreaTematica.ENGENHARIAS, StatusProjeto.SUBMETIDO, e);
        Projeto b = projetoParaListagem(coordenador, Campus.IPOJUCA, AreaTematica.ENGENHARIAS, StatusProjeto.SUBMETIDO, e);

        when(projetoRepository.listarTodos()).thenReturn(List.of(a,b));

        List<Projeto> resultado = projetoService.listarProjetosParaGestor(gestor);

        assertEquals(1, resultado.size());
        assertTrue(resultado.contains(a));
    }

    @Test
    public void ct076_gestorNaoVisualizaProjetosDeOutrosCampi() {
        Servidor gestor = gestor(Campus.RECIFE);

        Edital e = editalDeTeste("01/2026");

        Projeto b = projetoParaListagem(coordenador, Campus.IPOJUCA, AreaTematica.ENGENHARIAS, StatusProjeto.SUBMETIDO, e);

        when(projetoRepository.listarTodos()).thenReturn(List.of(b));

        List<Projeto> resultado = projetoService.listarProjetosParaGestor(gestor);

        assertTrue(resultado.isEmpty());
    }

    @Test
    public void ct077_gestorVisualizaApenasStatusPermitidos() {
        Servidor gestor = gestor(Campus.RECIFE);

        Edital e = editalDeTeste("01/2026");

        Projeto permitido1 = projetoParaListagem(coordenador, Campus.RECIFE, AreaTematica.ENGENHARIAS, StatusProjeto.SUBMETIDO, e);
        Projeto permitido2 = projetoParaListagem(coordenador, Campus.RECIFE, AreaTematica.ENGENHARIAS, StatusProjeto.EM_CORRECAO, e);
        Projeto bloqueado = projetoParaListagem(coordenador, Campus.RECIFE, AreaTematica.ENGENHARIAS, StatusProjeto.RASCUNHO, e);

        when(projetoRepository.listarTodos()).thenReturn(List.of(permitido1, permitido2, bloqueado));

        List<Projeto> resultado = projetoService.listarProjetosParaGestor(gestor);

        assertEquals(2, resultado.size());
        assertTrue(resultado.containsAll(List.of(permitido1, permitido2)));
        assertFalse(resultado.contains(bloqueado));
    }

    @Test
    public void ct079_usuarioSemPermissaoNaoAcessaAdmin() {
        Servidor coordenador = this.coordenador;

        assertThrows(SecurityException.class, () -> {
            projetoService.listarProjetosParaAdmin(coordenador, null, null, null, null);
        });

        verifyNoInteractions(projetoRepository);
    }

    @Test
    public void ct080_filtrarPorEdital() {
        Servidor admin = admin();

        Edital e1 = editalDeTeste("01/2026");
        Edital e2 = editalDeTeste("02/2026");

        Projeto a = projetoParaListagem(coordenador, Campus.RECIFE, AreaTematica.ENGENHARIAS, StatusProjeto.SUBMETIDO, e1);
        Projeto b = projetoParaListagem(coordenador, Campus.IPOJUCA, AreaTematica.ENGENHARIAS, StatusProjeto.SUBMETIDO, e1);
        Projeto c = projetoParaListagem(coordenador, Campus.RECIFE, AreaTematica.ENGENHARIAS, StatusProjeto.SUBMETIDO, e2);

        when(projetoRepository.listarTodos()).thenReturn(List.of(a,b,c));

        List<Projeto> resultado = projetoService.listarProjetosParaAdmin(admin, e1, null, null, null);

        assertEquals(2, resultado.size());
    }

    @Test
    public void ct081_filtrarPorCampus() {
        Servidor admin = admin();

        Edital e = editalDeTeste("01/2026");

        Projeto b = projetoParaListagem(coordenador, Campus.IPOJUCA, AreaTematica.ENGENHARIAS, StatusProjeto.SUBMETIDO, e);

        when(projetoRepository.listarTodos()).thenReturn(List.of(b));

        List<Projeto> resultado = projetoService.listarProjetosParaAdmin(admin, null, Campus.IPOJUCA, null, null);

        assertEquals(1, resultado.size());
        assertTrue(resultado.contains(b));
    }

    @Test
    public void ct082_filtrarPorArea() {
        Servidor admin = admin();

        Edital e = editalDeTeste("01/2026");

        Projeto c = projetoParaListagem(coordenador, Campus.RECIFE, AreaTematica.CIENCIAS_DA_SAUDE, StatusProjeto.SUBMETIDO, e);

        when(projetoRepository.listarTodos()).thenReturn(List.of(c));

        List<Projeto> resultado = projetoService.listarProjetosParaAdmin(admin, null, null, AreaTematica.CIENCIAS_DA_SAUDE, null);

        assertEquals(1, resultado.size());
    }

    @Test
    public void ct083_filtrarPorStatus() {
        Servidor admin = admin();

        Edital e = editalDeTeste("01/2026");

        Projeto b = projetoParaListagem(coordenador, Campus.IPOJUCA, AreaTematica.ENGENHARIAS, StatusProjeto.RASCUNHO, e);

        when(projetoRepository.listarTodos()).thenReturn(List.of(b));

        List<Projeto> resultado = projetoService.listarProjetosParaAdmin(admin, null, null, null, StatusProjeto.RASCUNHO);

        assertEquals(1, resultado.size());
    }

    @Test
    public void ct084_filtrarCombinandoMultiplosCriterios() {
        Servidor admin = admin();

        Edital e1 = editalDeTeste("01/2026");
        Edital e2 = editalDeTeste("02/2026");

        Projeto a = projetoParaListagem(coordenador, Campus.RECIFE,
                AreaTematica.ENGENHARIAS, StatusProjeto.SUBMETIDO, e1);

        Projeto b = projetoParaListagem(coordenador, Campus.IPOJUCA,
                AreaTematica.ENGENHARIAS, StatusProjeto.SUBMETIDO, e1);

        Projeto c = projetoParaListagem(coordenador, Campus.RECIFE,
                AreaTematica.EDUCACAO, StatusProjeto.SUBMETIDO, e2);

        //único que atende TODOS os critérios
        Projeto d = projetoParaListagem(coordenador, Campus.IPOJUCA,
                AreaTematica.EDUCACAO, StatusProjeto.EM_CORRECAO, e2);

        when(projetoRepository.listarTodos()).thenReturn(List.of(a,b,c,d));

        List<Projeto> resultado = projetoService.listarProjetosParaAdmin(
                admin,
                e2,
                Campus.IPOJUCA,
                AreaTematica.EDUCACAO,
                StatusProjeto.EM_CORRECAO
        );

        assertEquals(1, resultado.size());
        assertTrue(resultado.contains(d));
    }

    @Test
    public void ct085_filtrosSemResultadoRetornaListaVazia() {
        Servidor admin = admin();

        Edital e = editalDeTeste("01/2026");

        Projeto a = projetoParaListagem(coordenador, Campus.RECIFE,
                AreaTematica.ENGENHARIAS, StatusProjeto.SUBMETIDO, e);

        when(projetoRepository.listarTodos()).thenReturn(List.of(a));

        List<Projeto> resultado = projetoService.listarProjetosParaAdmin(
                admin,
                null,
                Campus.RECIFE,
                null,
                StatusProjeto.RASCUNHO // não existe
        );

        assertTrue(resultado.isEmpty());
    }

    @Test
    public void ct086_limparFiltrosRetornaTodos() {
        Servidor admin = admin();

        Edital e1 = editalDeTeste("01/2026");
        Edital e2 = editalDeTeste("02/2026");

        Projeto a = projetoParaListagem(coordenador, Campus.RECIFE, AreaTematica.ENGENHARIAS, StatusProjeto.SUBMETIDO, e1);
        Projeto b = projetoParaListagem(coordenador, Campus.IPOJUCA, AreaTematica.ENGENHARIAS, StatusProjeto.SUBMETIDO, e1);
        Projeto c = projetoParaListagem(coordenador, Campus.RECIFE, AreaTematica.EDUCACAO, StatusProjeto.SUBMETIDO, e2);
        Projeto d = projetoParaListagem(coordenador, Campus.IPOJUCA, AreaTematica.EDUCACAO, StatusProjeto.EM_CORRECAO, e2);

        when(projetoRepository.listarTodos()).thenReturn(List.of(a,b,c,d));

        List<Projeto> resultado = projetoService.listarProjetosParaAdmin(
                admin, null, null, null, null
        );

        assertEquals(4, resultado.size());
    }

    @Test
    public void ct087_gestorNaoFiltraOutroCampus() {
        Servidor gestor = gestor(Campus.RECIFE);

        Edital e = editalDeTeste("01/2026");

        Projeto recife = projetoParaListagem(coordenador, Campus.RECIFE,
                AreaTematica.ENGENHARIAS, StatusProjeto.SUBMETIDO, e);

        Projeto ipojuca = projetoParaListagem(coordenador, Campus.IPOJUCA,
                AreaTematica.ENGENHARIAS, StatusProjeto.SUBMETIDO, e);

        when(projetoRepository.listarTodos()).thenReturn(List.of(recife, ipojuca));

        List<Projeto> resultado = projetoService.listarProjetosParaGestor(gestor);

        assertEquals(1, resultado.size());
        assertTrue(resultado.contains(recife));
    }

    @Test
    public void ct088_statusGestorRestritoAoPermitido() {
        Servidor gestor = gestor(Campus.RECIFE);

        Edital e = editalDeTeste("01/2026");

        Projeto permitido = projetoParaListagem(coordenador, Campus.RECIFE,
                AreaTematica.ENGENHARIAS, StatusProjeto.EM_AVALIACAO, e);

        Projeto bloqueado = projetoParaListagem(coordenador, Campus.RECIFE,
                AreaTematica.ENGENHARIAS, StatusProjeto.RASCUNHO, e);

        when(projetoRepository.listarTodos()).thenReturn(List.of(permitido, bloqueado));

        List<Projeto> resultado = projetoService.listarProjetosParaGestor(gestor);

        assertEquals(1, resultado.size());
        assertTrue(resultado.contains(permitido));
        assertFalse(resultado.contains(bloqueado));
    }

    // CT-089 — Admin Geral baixa anexo de projeto do qual não é dono
    @Test
    public void ct089_adminGeralBaixaAnexoDeProjetoQueNaoEDono() {
        Servidor admin = admin();

        Projeto projeto = projetoParaListagem(
                coordenador,
                Campus.RECIFE,
                AreaTematica.ENGENHARIAS,
                StatusProjeto.SUBMETIDO,
                editalDeTeste("01/2026")
        );

        byte[] anexo = new byte[] {1, 2, 3, 4, 5};
        projeto.setAnexo(anexo);

        when(projetoRepository.buscarPorId(1L)).thenReturn(projeto);

        byte[] resultado = assertDoesNotThrow(() ->
                projetoService.baixarAnexo(1L, admin)
        );

        assertArrayEquals(anexo, resultado);
        assertEquals(coordenador, projeto.getCoordenador());

        verify(projetoRepository, times(1)).buscarPorId(1L);
    }

    // CT-090 — Gestor/Diretor baixa anexo e plano de projeto do seu Campus sem ser dono
    @Test
    public void ct090_gestorBaixaAnexoEPlanoDeProjetoDoSeuCampus() {
        Servidor gestor = gestor(Campus.RECIFE);

        Projeto projeto = projetoParaListagem(
                coordenador,
                Campus.RECIFE,
                AreaTematica.ENGENHARIAS,
                StatusProjeto.SUBMETIDO,
                editalDeTeste("01/2026")
        );

        byte[] anexo = new byte[] {1, 2, 3};
        projeto.setAnexo(anexo);

        Membro membro = membroValido("98765432100");

        PlanoDeTrabalho plano = new PlanoDeTrabalho(
                "Plano de Trabalho Teste",
                "Atividades do plano de trabalho."
        );

        byte[] arquivoPlano = new byte[] {10, 20, 30};

        plano.setArquivo(arquivoPlano);

        projeto.adicionarMembro(membro);
        membro.adicionarPlanoDeTrabalho(plano);

        when(projetoRepository.buscarPorId(1L)).thenReturn(projeto);

        // Gestor é do mesmo Campus, mas não é dono do projeto
        assertEquals(Campus.RECIFE, gestor.getCampus());
        assertEquals(Campus.RECIFE, projeto.getCampus());
        assertEquals(coordenador, projeto.getCoordenador());
        assertFalse(gestor.equals(projeto.getCoordenador()));

        byte[] resultadoAnexo = assertDoesNotThrow(() ->
                projetoService.baixarAnexo(1L, gestor)
        );

        byte[] resultadoPlano = assertDoesNotThrow(() ->
                projetoService.baixarPlanoDeTrabalho(
                        1L,
                        "98765432100",
                        gestor
                )
        );

        assertArrayEquals(anexo, resultadoAnexo);
        assertArrayEquals(arquivoPlano, resultadoPlano);

        verify(projetoRepository, times(2)).buscarPorId(1L);
    }

    // CT-091 — Rejeitar download de arquivo de projeto fora do escopo via URL/ID
    @Test
    public void ct091_rejeitarDownloadDeProjetoForaDoEscopo() {
        Servidor gestor = gestor(Campus.RECIFE);

        Projeto projetoIpojuca = projetoParaListagem(
                coordenador,
                Campus.IPOJUCA,
                AreaTematica.ENGENHARIAS,
                StatusProjeto.SUBMETIDO,
                editalDeTeste("01/2026")
        );

        byte[] anexo = new byte[] {1, 2, 3, 4};
        projetoIpojuca.setAnexo(anexo);

        when(projetoRepository.buscarPorId(2L)).thenReturn(projetoIpojuca);

        Exception ex = assertThrows(SecurityException.class, () ->
                projetoService.baixarAnexo(2L, gestor)
        );

        assertEquals("Acesso negado ao arquivo.", ex.getMessage());

        assertEquals(Campus.RECIFE, gestor.getCampus());
        assertEquals(Campus.IPOJUCA, projetoIpojuca.getCampus());

        verify(projetoRepository, times(1)).buscarPorId(2L);
    }

    // CT-092 — Desabilitar download quando projeto não possui arquivo
    @Test
    public void ct092_desabilitarDownloadQuandoProjetoNaoPossuiAnexo() {
        Servidor admin = admin();

        Projeto projetoSemAnexo = projetoParaListagem(
                coordenador,
                Campus.IPOJUCA,
                AreaTematica.ENGENHARIAS,
                StatusProjeto.SUBMETIDO,
                editalDeTeste("01/2026")
        );

        // Não cadastra anexo
        assertEquals(null, projetoSemAnexo.getAnexo());

        when(projetoRepository.buscarPorId(2L)).thenReturn(projetoSemAnexo);

        Exception ex = assertThrows(IllegalStateException.class, () ->
                projetoService.baixarAnexo(2L, admin)
        );

        assertEquals("Projeto não possui anexo.", ex.getMessage());

        verify(projetoRepository, times(1)).buscarPorId(2L);
    }

    @Test
    public void ct093_gestorSemProjetosRetornaListaVazia() {
        Servidor gestor = gestor(Campus.RECIFE);

        when(projetoRepository.listarTodos()).thenReturn(List.of());

        List<Projeto> resultado = projetoService.listarProjetosParaGestor(gestor);

        assertTrue(resultado.isEmpty());
    }

    
    

}
