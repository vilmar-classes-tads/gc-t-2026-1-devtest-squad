package br.edu.ifpe.sistemaeditais.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.edu.ifpe.sistemaeditais.Main;
import br.edu.ifpe.sistemaeditais.model.AreaFormacao;
import br.edu.ifpe.sistemaeditais.model.Campus;
import br.edu.ifpe.sistemaeditais.model.Edital;
import br.edu.ifpe.sistemaeditais.model.Perfil;
import br.edu.ifpe.sistemaeditais.model.Servidor;
import br.edu.ifpe.sistemaeditais.model.Titulacao;

public class CadastroEditalCliTest {

    private Method cadastrarEditalMethod;
    private Method lerDataMethod;
    private EditalService editalService;
    private Servidor admin;

    @BeforeEach
    public void configurar() throws Exception {
        cadastrarEditalMethod = Main.class.getDeclaredMethod(
                "cadastrarEdital", Scanner.class, EditalService.class);
        cadastrarEditalMethod.setAccessible(true);

        lerDataMethod = Main.class.getDeclaredMethod(
                "lerData", Scanner.class, String.class);
        lerDataMethod.setAccessible(true);

        editalService = new EditalService();

        admin = new Servidor(
                "Admin Editais Teste", "20000000000", "admin.editais.cli@ifpe.edu.br", "senha123",
                Campus.RECIFE, AreaFormacao.CIENCIAS_EXATAS_E_DA_TERRA, Titulacao.DOUTORADO);
        admin.adicionarPerfil(Perfil.ROLE_ADMIN);

        // O método cadastrarEdital() usa o campo estático usuarioLogado de Main,
        // por isso ele precisa ser definido via reflexão antes de cada teste.
        Field campoUsuarioLogado = Main.class.getDeclaredField("usuarioLogado");
        campoUsuarioLogado.setAccessible(true);
        campoUsuarioLogado.set(null, admin);
    }

    private String entradaEdital(String numero, String titulo, String ano,
                                  String iniSub, String fimSub, String iniAv, String fimAv) {
        return numero + "\n"
                + titulo + "\n"
                + ano + "\n"
                + iniSub + "\n"
                + fimSub + "\n"
                + iniAv + "\n"
                + fimAv + "\n";
    }

    private String invocarCadastrarEdital(String entradaSimulada) throws Exception {
        Scanner scanner = new Scanner(new ByteArrayInputStream(
                entradaSimulada.getBytes(StandardCharsets.UTF_8)));

        ByteArrayOutputStream saidaCapturada = new ByteArrayOutputStream();
        PrintStream saidaOriginal = System.out;
        System.setOut(new PrintStream(saidaCapturada, true, StandardCharsets.UTF_8));
        try {
            cadastrarEditalMethod.invoke(null, scanner, editalService);
        } finally {
            System.setOut(saidaOriginal);
        }
        return saidaCapturada.toString(StandardCharsets.UTF_8);
    }

    // CT-024 — Aceitar título não vazio
    @Test
    public void ct024_tituloNaoVazio_deveSerAceito() throws Exception {
        String entrada = entradaEdital("CT024/2026", "Edital de Fomento à Pesquisa 2026", "2026",
                "01/08/2026", "31/08/2026", "01/09/2026", "15/09/2026");

        String saida = invocarCadastrarEdital(entrada);

        assertFalse(saida.contains("[ERRO] O título do edital é obrigatório."),
                "Não deveria exibir erro de título obrigatório quando um valor válido é informado.");
        assertTrue(saida.contains("Edital publicado com sucesso!"),
                "O cadastro deveria prosseguir e concluir com sucesso.");
    }

    // CT-025 — Rejeitar título vazio e exibir erro
    @Test
    public void ct025_tituloVazio_deveExibirErroESolicitarNovamente() throws Exception {
        String entrada = "CT025/2026\n"
                + "\n"                                    // título vazio (1ª tentativa)
                + "Edital de Fomento à Pesquisa 2026\n"    // título válido (2ª tentativa)
                + "2026\n"
                + "01/08/2026\n31/08/2026\n01/09/2026\n15/09/2026\n";

        String saida = invocarCadastrarEdital(entrada);

        assertTrue(saida.contains("[ERRO] O título do edital é obrigatório."),
                "Deveria exibir a mensagem de erro ao receber título vazio.");
        assertTrue(saida.contains("Edital publicado com sucesso!"),
                "Após informar um título válido, o cadastro deveria prosseguir e concluir.");
    }

    // CT-026 — Aceitar número não vazio
    @Test
    public void ct026_numeroNaoVazio_deveSerAceito() throws Exception {
        String entrada = entradaEdital("001/2026-CT026", "Edital Teste CT026", "2026",
                "01/08/2026", "31/08/2026", "01/09/2026", "15/09/2026");

        String saida = invocarCadastrarEdital(entrada);

        assertFalse(saida.contains("[ERRO] O número do edital é obrigatório."),
                "Não deveria exibir erro de número obrigatório quando um valor válido é informado.");
        assertTrue(saida.contains("Edital publicado com sucesso!"));
    }

    // CT-027 — Rejeitar número vazio e exibir erro
    @Test
    public void ct027_numeroVazio_deveExibirErroESolicitarNovamente() throws Exception {
        String entrada = "\n"                  // número vazio (1ª tentativa)
                + "CT027/2026\n"                // número válido (2ª tentativa)
                + "Edital Teste CT027\n"
                + "2026\n"
                + "01/08/2026\n31/08/2026\n01/09/2026\n15/09/2026\n";

        String saida = invocarCadastrarEdital(entrada);

        assertTrue(saida.contains("[ERRO] O número do edital é obrigatório."),
                "Deveria exibir a mensagem de erro ao receber número vazio.");
        assertTrue(saida.contains("Edital publicado com sucesso!"),
                "Após informar um número válido, o cadastro deveria prosseguir e concluir.");
    }

    // CT-028 — Validar duplicidade de número no cadastro e rejeitar se já existir
    @Test
    public void ct028_numeroDuplicado_deveSerRejeitado() throws Exception {
        // Garante a pré-condição: já existe um edital cadastrado com número "001/2026"
        try {
            Edital preExistente = new Edital("001/2026", "Edital Pré-existente", 2026,
                    LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31),
                    LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 15));
            editalService.criarEdital(preExistente, admin);
        } catch (IllegalArgumentException jaExistente) {
            // já cadastrado por execução anterior — pré-condição já satisfeita
        }

        String entrada = entradaEdital("001/2026", "Edital Duplicado Teste", "2026",
                "01/08/2026", "31/08/2026", "01/09/2026", "15/09/2026");

        String saida = invocarCadastrarEdital(entrada);

        assertTrue(saida.contains("Erro ao salvar edital: Já existe um edital cadastrado com este número."),
                "Deveria rejeitar o cadastro de um edital com número já existente.");
    }

    // CT-029 — Aceitar ano numérico válido
    @Test
    public void ct029_anoNumericoValido_deveSerAceito() throws Exception {
        String entrada = entradaEdital("CT029/2026", "Edital Teste CT029", "2026",
                "01/08/2026", "31/08/2026", "01/09/2026", "15/09/2026");

        String saida = invocarCadastrarEdital(entrada);

        assertFalse(saida.contains("[ERRO] Digite um ano válido com caracteres numéricos."));
        assertTrue(saida.contains("Edital publicado com sucesso!"));
    }

    // CT-030 — Rejeitar entrada não numérica ("abcd") e exibir erro
    @Test
    public void ct030_anoNaoNumerico_deveExibirErroESolicitarNovamente() throws Exception {
        String entrada = "CT030/2026\n"
                + "Edital Teste CT030\n"
                + "abcd\n"    // ano inválido (1ª tentativa)
                + "2026\n"    // ano válido (2ª tentativa)
                + "01/08/2026\n31/08/2026\n01/09/2026\n15/09/2026\n";

        String saida = invocarCadastrarEdital(entrada);

        assertTrue(saida.contains("[ERRO] Digite um ano válido com caracteres numéricos."),
                "Deveria exibir a mensagem de erro ao receber um ano não numérico.");
        assertTrue(saida.contains("Edital publicado com sucesso!"),
                "Após informar um ano válido, o cadastro deveria prosseguir e concluir.");
    }

    // CT-031 — Rejeitar data vazia
    @Test
    public void ct031_dataVazia_deveExibirErroESolicitarNovamente() throws Exception {
        Scanner scanner = new Scanner(new ByteArrayInputStream(
                "\n01/08/2026\n".getBytes(StandardCharsets.UTF_8)));

        ByteArrayOutputStream saidaCapturada = new ByteArrayOutputStream();
        PrintStream saidaOriginal = System.out;
        System.setOut(new PrintStream(saidaCapturada, true, StandardCharsets.UTF_8));

        LocalDate data;
        try {
            data = (LocalDate) lerDataMethod.invoke(null, scanner, "Data Início Submissão (DD/MM/AAAA): ");
        } finally {
            System.setOut(saidaOriginal);
        }

        String saida = saidaCapturada.toString(StandardCharsets.UTF_8);

        assertTrue(saida.contains("[ERRO] Este campo de data é obrigatório e não pode ser vazio."),
                "Deveria exibir erro ao receber uma data vazia.");
        assertEquals(LocalDate.of(2026, 8, 1), data,
                "Após o erro, deveria aceitar a data válida informada na segunda tentativa.");
    }

    // CT-032 — Rejeitar formato inválido
    @Test
    public void ct032_formatoInvalido_deveExibirErroESolicitarNovamente() throws Exception {
        Scanner scanner = new Scanner(new ByteArrayInputStream(
                "2026-08-01\n01/08/2026\n".getBytes(StandardCharsets.UTF_8)));

        ByteArrayOutputStream saidaCapturada = new ByteArrayOutputStream();
        PrintStream saidaOriginal = System.out;
        System.setOut(new PrintStream(saidaCapturada, true, StandardCharsets.UTF_8));

        LocalDate data;
        try {
            data = (LocalDate) lerDataMethod.invoke(null, scanner, "Data Início Submissão (DD/MM/AAAA): ");
        } finally {
            System.setOut(saidaOriginal);
        }

        String saida = saidaCapturada.toString(StandardCharsets.UTF_8);

        assertTrue(saida.contains("[ERRO] Formato inválido. Use o padrão DD/MM/AAAA."),
                "Deveria exibir erro ao receber uma data em formato ISO (inválido para o sistema).");
        assertEquals(LocalDate.of(2026, 8, 1), data,
                "Após o erro, deveria aceitar a data válida informada no formato DD/MM/AAAA.");
    }
}