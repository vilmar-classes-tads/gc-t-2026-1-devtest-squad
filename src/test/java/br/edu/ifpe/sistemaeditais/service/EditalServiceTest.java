package br.edu.ifpe.sistemaeditais.service;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.edu.ifpe.sistemaeditais.model.AreaFormacao;
import br.edu.ifpe.sistemaeditais.model.Campus;
import br.edu.ifpe.sistemaeditais.model.Edital;
import br.edu.ifpe.sistemaeditais.model.Perfil;
import br.edu.ifpe.sistemaeditais.model.Servidor;
import br.edu.ifpe.sistemaeditais.model.Titulacao;

public class EditalServiceTest {

    private EditalService editalService;
    private Servidor admin;
    private Servidor coordenador;

    @BeforeEach
    public void configurar() {
        editalService = new EditalService();

        // com perfil ROLE_ADMIN 
        admin = new Servidor(
                "Admin Teste",
                "22222222222",
                "admin.ct033@ifpe.edu.br",
                "senha123",
                Campus.RECIFE,
                AreaFormacao.CIENCIAS_EXATAS_E_DA_TERRA,
                Titulacao.DOUTORADO
        );
        admin.adicionarPerfil(Perfil.ROLE_ADMIN);

        // sem perfil ROLE_ADMIN 
        coordenador = new Servidor(
                "Coordenador Teste",
                "33333333333",
                "coordenador.ct037@ifpe.edu.br",
                "senha123",
                Campus.RECIFE,
                AreaFormacao.ENGENHARIAS,
                Titulacao.MESTRADO
        );
        coordenador.adicionarPerfil(Perfil.ROLE_COORDENADOR);
    }

    // CT-033 — iniSub > fimSub deve lançar erro de início de submissão posterior ao fim
    @Test
    public void ct033_iniSubAposFimSub() {
        LocalDate iniSub = LocalDate.of(2026, 8, 31);
        LocalDate fimSub = LocalDate.of(2026, 8, 1);
        LocalDate iniAv  = LocalDate.of(2026, 9, 1);
        LocalDate fimAv  = LocalDate.of(2026, 9, 15);

        Edital edital = new Edital("CT033/2026", "Edital Teste CT033", 2026,
                iniSub, fimSub, iniAv, fimAv);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            editalService.criarEdital(edital, admin);
        });

        Assertions.assertEquals(
                "A data de início da submissão não pode ser posterior ao fim.",
                ex.getMessage());
    }

    // CT-034 — iniAv > fimAv deve lançar erro de início de avaliação posterior ao fim
    @Test
    public void ct034_iniAvAposFimAv() {
        LocalDate iniSub = LocalDate.of(2026, 8, 1);
        LocalDate fimSub = LocalDate.of(2026, 8, 31);
        LocalDate iniAv  = LocalDate.of(2026, 9, 15);
        LocalDate fimAv  = LocalDate.of(2026, 9, 1);

        Edital edital = new Edital("CT034/2026", "Edital Teste CT034", 2026,
                iniSub, fimSub, iniAv, fimAv);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            editalService.criarEdital(edital, admin);
        });

        Assertions.assertEquals(
                "A data de início da avaliação não pode ser posterior ao fim.",
                ex.getMessage());
    }

    // CT-035 — fimSub > iniAv deve lançar erro de período de avaliação antes do término das submissões
    @Test
    public void ct035_fimSubAposIniAv() {
        LocalDate iniSub = LocalDate.of(2026, 8, 1);
        LocalDate fimSub = LocalDate.of(2026, 9, 10);
        LocalDate iniAv  = LocalDate.of(2026, 9, 1);
        LocalDate fimAv  = LocalDate.of(2026, 9, 15);

        Edital edital = new Edital("CT035/2026", "Edital Teste CT035", 2026,
                iniSub, fimSub, iniAv, fimAv);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            editalService.criarEdital(edital, admin);
        });

        Assertions.assertEquals(
                "O período de avaliação só pode começar após o término das submissões.",
                ex.getMessage());
    }

    // CT-036 — Cadastrar novo edital com dados válidos
    @Test
    public void ct036_cadastrarEditalComDadosValidos() {
        LocalDate iniSub = LocalDate.of(2026, 8, 1);
        LocalDate fimSub = LocalDate.of(2026, 8, 31);
        LocalDate iniAv  = LocalDate.of(2026, 9, 1);
        LocalDate fimAv  = LocalDate.of(2026, 9, 15);

        Edital edital = new Edital("CT036/2026", "Edital Teste CT036 - Fomento à Pesquisa",
                2026, iniSub, fimSub, iniAv, fimAv);

        assertDoesNotThrow(() -> editalService.criarEdital(edital, admin));

        List<Edital> editais = editalService.listarEditais(admin);
        boolean persistido = editais.stream()
                .anyMatch(e -> e.getNumero().equals("CT036/2026"));

        assertTrue(persistido, "O edital deveria ter sido persistido no repositório.");
    }

    // CT-037 — Cadastro de edital sem permissão de administrador deve ser rejeitado
    @Test
    public void ct037_cadastroSemRoleAdmin_usuarioCoordenador() {
        LocalDate iniSub = LocalDate.of(2026, 8, 1);
        LocalDate fimSub = LocalDate.of(2026, 8, 31);
        LocalDate iniAv  = LocalDate.of(2026, 9, 1);
        LocalDate fimAv  = LocalDate.of(2026, 9, 15);

        Edital edital = new Edital("CT037/2026", "Edital Teste CT037", 2026,
                iniSub, fimSub, iniAv, fimAv);

        Exception ex = assertThrows(SecurityException.class, () -> {
            editalService.criarEdital(edital, coordenador);
        });

        assertEquals(
                "Acesso negado. Funcionalidade exclusiva para Administradores.",
                ex.getMessage());
    }

    // CT-037 — variação: nenhum usuário autenticado (executor nulo)
    @Test
    public void ct037_cadastroSemRoleAdmin_usuarioNulo() {
        LocalDate iniSub = LocalDate.of(2026, 8, 1);
        LocalDate fimSub = LocalDate.of(2026, 8, 31);
        LocalDate iniAv  = LocalDate.of(2026, 9, 1);
        LocalDate fimAv  = LocalDate.of(2026, 9, 15);

        Edital edital = new Edital("CT037B/2026", "Edital Teste CT037B", 2026,
                iniSub, fimSub, iniAv, fimAv);

        Exception ex = assertThrows(SecurityException.class, () -> {
            editalService.criarEdital(edital, null);
        });

        assertEquals(
                "Acesso negado. Funcionalidade exclusiva para Administradores.",
                ex.getMessage());
    }

    // CT-038 — Editar edital existente com dados válidos altera todos os campos mutáveis
    @Test
    public void ct038_editarEditalExistente() {
        LocalDate iniSubOriginal = LocalDate.of(2026, 8, 1);
        LocalDate fimSubOriginal = LocalDate.of(2026, 8, 31);
        LocalDate iniAvOriginal  = LocalDate.of(2026, 9, 1);
        LocalDate fimAvOriginal  = LocalDate.of(2026, 9, 15);

        Edital original = new Edital("001/2026", "Título Original", 2026,
                iniSubOriginal, fimSubOriginal, iniAvOriginal, fimAvOriginal);
        editalService.criarEdital(original, admin);

        LocalDate novoIniSub = LocalDate.of(2026, 10, 1);
        LocalDate novoFimSub = LocalDate.of(2026, 10, 31);
        LocalDate novoIniAv  = LocalDate.of(2026, 11, 1);
        LocalDate novoFimAv  = LocalDate.of(2026, 11, 15);

        Edital dadosAtualizados = new Edital("001/2026", "Título Atualizado", 2027,
                novoIniSub, novoFimSub, novoIniAv, novoFimAv);

        assertDoesNotThrow(() -> editalService.editarEdital("001/2026", dadosAtualizados, admin));

        Edital editalAtualizado = editalService.listarEditais(admin).stream()
                .filter(e -> e.getNumero().equals("001/2026"))
                .findFirst()
                .orElse(null);

        assertTrue(editalAtualizado != null, "O edital deveria continuar existindo com o número original.");
        assertEquals("001/2026", editalAtualizado.getNumero(), "O número original deve ser mantido como chave de busca.");
        assertEquals("Título Atualizado", editalAtualizado.getTitulo());
        assertEquals(2027, editalAtualizado.getAno());
        assertEquals(novoIniSub, editalAtualizado.getDataInicioSubmissao());
        assertEquals(novoFimSub, editalAtualizado.getDataFimSubmissao());
        assertEquals(novoIniAv, editalAtualizado.getDataInicioAvaliacao());
        assertEquals(novoFimAv, editalAtualizado.getDataFimAvaliacao());
    }

    // CT-039 — Tentar editar edital inexistente deve falhar
    @Test
    public void ct039_editarEditalInexistente() {
        LocalDate iniSub = LocalDate.of(2026, 8, 1);
        LocalDate fimSub = LocalDate.of(2026, 8, 31);
        LocalDate iniAv  = LocalDate.of(2026, 9, 1);
        LocalDate fimAv  = LocalDate.of(2026, 9, 15);

        Edital dadosAtualizados = new Edital("999/2099", "Edital Inexistente", 2099,
                iniSub, fimSub, iniAv, fimAv);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            editalService.editarEdital("999/2099", dadosAtualizados, admin);
        });

        assertEquals("Edital não encontrado para edição.", ex.getMessage());
    }

    // CT-040 — Edição de edital sem permissão de administrador deve ser rejeitada
    @Test
    public void ct040_editarEditalSemRoleAdmin() {
        LocalDate iniSub = LocalDate.of(2026, 8, 1);
        LocalDate fimSub = LocalDate.of(2026, 8, 31);
        LocalDate iniAv  = LocalDate.of(2026, 9, 1);
        LocalDate fimAv  = LocalDate.of(2026, 9, 15);

        Edital editalExistente = new Edital("CT040/2026", "Edital Teste CT040", 2026,
                iniSub, fimSub, iniAv, fimAv);
        editalService.criarEdital(editalExistente, admin);

        Edital dadosAtualizados = new Edital("CT040/2026", "Título Tentativa", 2026,
                iniSub, fimSub, iniAv, fimAv);

        Exception ex = assertThrows(SecurityException.class, () -> {
            editalService.editarEdital("CT040/2026", dadosAtualizados, coordenador);
        });

        assertEquals(
                "Acesso negado. Funcionalidade exclusiva para Administradores.",
                ex.getMessage());
    }

    // CT-041 — Listar editais com pelo menos um edital cadastrado
    @Test
    public void ct041_listarEditaisComEditalCadastrado() {
        LocalDate iniSub = LocalDate.of(2026, 8, 1);
        LocalDate fimSub = LocalDate.of(2026, 8, 31);
        LocalDate iniAv  = LocalDate.of(2026, 9, 1);
        LocalDate fimAv  = LocalDate.of(2026, 9, 15);

        Edital edital = new Edital("CT041/2026", "Edital Teste CT041", 2026,
                iniSub, fimSub, iniAv, fimAv);
        editalService.criarEdital(edital, admin);

        List<Edital> editais = editalService.listarEditais(admin);

        Edital encontrado = editais.stream()
                .filter(e -> e.getNumero().equals("CT041/2026"))
                .findFirst()
                .orElse(null);

        assertTrue(encontrado != null, "O edital cadastrado deveria estar na listagem.");
        assertEquals("Edital Teste CT041", encontrado.getTitulo());
        assertEquals(2026, encontrado.getAno());
        assertEquals(iniSub, encontrado.getDataInicioSubmissao());
        assertEquals(fimSub, encontrado.getDataFimSubmissao());
        assertEquals(iniAv, encontrado.getDataInicioAvaliacao());
        assertEquals(fimAv, encontrado.getDataFimAvaliacao());
    }

    // CT-042 — Listar editais quando não houver editais cadastrados
    @Test
    public void ct042_listarEditaisRepositorioVazio() throws Exception {

        java.lang.reflect.Field campoEditais = br.edu.ifpe.sistemaeditais.repository.EditalRepository.class
                .getDeclaredField("editais");
        campoEditais.setAccessible(true);
        List<?> listaInterna = (List<?>) campoEditais.get(null);
        listaInterna.clear();

        List<Edital> editais = editalService.listarEditais(admin);

        assertTrue(editais.isEmpty(), "A lista de editais deveria estar vazia.");
    }

    // CT-043 — Listagem em área administrativa sem perfil de administrador deve ser rejeitada
    @Test
    public void ct043_listarEditaisSemRoleAdmin() {
        Exception ex = assertThrows(SecurityException.class, () -> {
            editalService.listarEditais(coordenador);
        });

        assertEquals(
                "Acesso negado. Funcionalidade exclusiva para Administradores.",
                ex.getMessage());
    }

    // CT-044 — Listagem de editais não deve permitir modificação da lista retornada
    @Test
    public void ct044_listaDeEditaisRetornada() {
        LocalDate iniSub = LocalDate.of(2026, 8, 1);
        LocalDate fimSub = LocalDate.of(2026, 8, 31);
        LocalDate iniAv  = LocalDate.of(2026, 9, 1);
        LocalDate fimAv  = LocalDate.of(2026, 9, 15);

        Edital edital = new Edital("CT044/2026", "Edital Teste CT044", 2026,
                iniSub, fimSub, iniAv, fimAv);
        editalService.criarEdital(edital, admin);

        List<Edital> editais = editalService.listarEditais(admin);

        assertThrows(UnsupportedOperationException.class, () -> {
            editais.add(edital);
        });
    }
}
