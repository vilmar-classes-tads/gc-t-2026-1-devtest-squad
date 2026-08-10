package br.edu.ifpe.sistemaeditais.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
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
import br.edu.ifpe.sistemaeditais.repository.EditalRepository;

public class CadastroEditalCliTest {

    private static final String NUMERO_BASE = "001/2026";
    private static final String TITULO_BASE = "Edital de Fomento à Pesquisa 2026";
    private static final String ANO_BASE = "2026";
    private static final String INI_SUB_BASE = "01/08/2026";
    private static final String FIM_SUB_BASE = "31/08/2026";
    private static final String INI_AV_BASE = "01/09/2026";
    private static final String FIM_AV_BASE = "15/09/2026";

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

        Field campoUsuarioLogado = Main.class.getDeclaredField("usuarioLogado");
        campoUsuarioLogado.setAccessible(true);
        campoUsuarioLogado.set(null, admin);

        limparRepositorioDeEditais();
    }

    private void limparRepositorioDeEditais() throws Exception {
        Field campoEditais = EditalRepository.class.getDeclaredField("editais");
        campoEditais.setAccessible(true);
        List<?> lista = (List<?>) campoEditais.get(null);
        lista.clear();
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

    private String entradaEditalBase() {
        return entradaEdital(NUMERO_BASE, TITULO_BASE, ANO_BASE,
                INI_SUB_BASE, FIM_SUB_BASE, INI_AV_BASE, FIM_AV_BASE);
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
    // Sem delta: Título = base ("Edital de Fomento à Pesquisa 2026")
    @Test
    public void ct024_tituloNaoVazio_deveSerAceito() throws Exception {
        String saida = invocarCadastrarEdital(entradaEditalBase());

        assertFalse(saida.contains("[ERRO] O título do edital é obrigatório."),
                "Não deveria exibir erro de título obrigatório quando um valor válido é informado.");
        assertTrue(saida.contains("Edital publicado com sucesso!"),
                "O cadastro deveria prosseguir e concluir com sucesso.");
    }

    // CT-025 — Rejeitar título vazio e exibir erro
    // Delta: Título = "" (1ª tentativa), depois título base (2ª tentativa)
    @Test
    public void ct025_tituloVazio_deveExibirErroESolicitarNovamente() throws Exception {
        String entrada = NUMERO_BASE + "\n"
                + "\n"                    // título vazio (1ª tentativa)
                + TITULO_BASE + "\n"      // título válido (2ª tentativa)
                + ANO_BASE + "\n"
                + INI_SUB_BASE + "\n" + FIM_SUB_BASE + "\n" + INI_AV_BASE + "\n" + FIM_AV_BASE + "\n";

        String saida = invocarCadastrarEdital(entrada);

        assertTrue(saida.contains("[ERRO] O título do edital é obrigatório."),
                "Deveria exibir a mensagem de erro ao receber título vazio.");
        assertTrue(saida.contains("Edital publicado com sucesso!"),
                "Após informar um título válido, o cadastro deveria prosseguir e concluir.");
    }

    // CT-026 — Aceitar número não vazio
    // Sem delta: Número = base ("001/2026")
    @Test
    public void ct026_numeroNaoVazio_deveSerAceito() throws Exception {
        String saida = invocarCadastrarEdital(entradaEditalBase());

        assertFalse(saida.contains("[ERRO] O número do edital é obrigatório."),
                "Não deveria exibir erro de número obrigatório quando um valor válido é informado.");
        assertTrue(saida.contains("Edital publicado com sucesso!"));
    }

    // CT-027 — Rejeitar número vazio e exibir erro
    // Delta: Número = "" (1ª tentativa), depois número base (2ª tentativa)
    @Test
    public void ct027_numeroVazio_deveExibirErroESolicitarNovamente() throws Exception {
        String entrada = "\n"                 // número vazio (1ª tentativa)
                + NUMERO_BASE + "\n"           // número válido (2ª tentativa)
                + TITULO_BASE + "\n"
                + ANO_BASE + "\n"
                + INI_SUB_BASE + "\n" + FIM_SUB_BASE + "\n" + INI_AV_BASE + "\n" + FIM_AV_BASE + "\n";

        String saida = invocarCadastrarEdital(entrada);

        assertTrue(saida.contains("[ERRO] O número do edital é obrigatório."),
                "Deveria exibir a mensagem de erro ao receber número vazio.");
        assertTrue(saida.contains("Edital publicado com sucesso!"),
                "Após informar um número válido, o cadastro deveria prosseguir e concluir.");
    }

    // CT-028 — Validar duplicidade de número no cadastro e rejeitar se já existir
    // Pré-condição: já existe um edital cadastrado com o número base ("001/2026")
    @Test
    public void ct028_numeroDuplicado_deveSerRejeitado() throws Exception {
        Edital editalBase = new Edital(NUMERO_BASE, TITULO_BASE, Integer.parseInt(ANO_BASE),
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31),
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 15));
        editalService.criarEdital(editalBase, admin);

        String saida = invocarCadastrarEdital(entradaEditalBase());

        assertTrue(saida.contains("Erro ao salvar edital: Já existe um edital cadastrado com este número."),
                "Deveria rejeitar o cadastro de um edital com número já existente.");
    }

    // CT-029 — Aceitar ano numérico válido
    // Sem delta: Ano = base (2026)
    @Test
    public void ct029_anoNumericoValido_deveSerAceito() throws Exception {
        String saida = invocarCadastrarEdital(entradaEditalBase());

        assertFalse(saida.contains("[ERRO] Digite um ano válido com caracteres numéricos."));
        assertTrue(saida.contains("Edital publicado com sucesso!"));
    }

    // CT-030 — Rejeitar entrada não numérica ("abcd") e exibir erro
    // Delta: Ano = "abcd" (1ª tentativa), depois ano base (2ª tentativa)
    @Test
    public void ct030_anoNaoNumerico_deveExibirErroESolicitarNovamente() throws Exception {
        String entrada = NUMERO_BASE + "\n"
                + TITULO_BASE + "\n"
                + "abcd\n"          // ano inválido (1ª tentativa)
                + ANO_BASE + "\n"   // ano válido (2ª tentativa)
                + INI_SUB_BASE + "\n" + FIM_SUB_BASE + "\n" + INI_AV_BASE + "\n" + FIM_AV_BASE + "\n";

        String saida = invocarCadastrarEdital(entrada);

        assertTrue(saida.contains("[ERRO] Digite um ano válido com caracteres numéricos."),
                "Deveria exibir a mensagem de erro ao receber um ano não numérico.");
        assertTrue(saida.contains("Edital publicado com sucesso!"),
                "Após informar um ano válido, o cadastro deveria prosseguir e concluir.");
    }

    // CT-031 — Rejeitar data vazia
    // Delta: Data de Início da Submissão = "" (1ª tentativa), depois data base (2ª tentativa)
    @Test
    public void ct031_dataVazia_deveExibirErroESolicitarNovamente() throws Exception {
        Scanner scanner = new Scanner(new ByteArrayInputStream(
                ("\n" + INI_SUB_BASE + "\n").getBytes(StandardCharsets.UTF_8)));

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
                "Após o erro, deveria aceitar a data válida (base) informada na segunda tentativa.");
    }

    // CT-032 — Rejeitar formato inválido
    // Delta: Data de Início da Submissão = "2026-08-01" (formato ISO, 1ª tentativa), depois data base (2ª tentativa)
    @Test
    public void ct032_formatoInvalido_deveExibirErroESolicitarNovamente() throws Exception {
        Scanner scanner = new Scanner(new ByteArrayInputStream(
                ("2026-08-01\n" + INI_SUB_BASE + "\n").getBytes(StandardCharsets.UTF_8)));

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
                "Após o erro, deveria aceitar a data válida (base) informada no formato DD/MM/AAAA.");
    }
}