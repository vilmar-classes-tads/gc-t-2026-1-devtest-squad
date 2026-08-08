package br.edu.ifpe.sistemaeditais.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.edu.ifpe.sistemaeditais.Main;
import br.edu.ifpe.sistemaeditais.model.Servidor;
import br.edu.ifpe.sistemaeditais.repository.ServidorRepository;

/**
 * Testes de CLI (Main.cadastrarServidor) cobrindo os Casos de Teste 1 a 16
 * do Caderno de Testes - Issue 0 (Cadastro de Servidor):
 * <p>
 * 1. Nome Completo  -> CT-001 a CT-004
 * 2. CPF            -> CT-005 a CT-010
 * 3. E-mail          -> CT-011 a CT-016
 * <p>
 * OBSERVAÇÃO IMPORTANTE: a validação desses campos (não-vazio, formato,
 * duplicidade) está implementada quase inteiramente na camada de CLI
 * (Main.cadastrarServidor) e não em Servidor/CadastroServidor, que não
 * validam nada por conta própria. Por isso, os testes invocam o método
 * privado estático via reflection, simulando a entrada do usuário no
 * console (System.in) e capturando a saída (System.out).
 * <p>
 * GAP CONHECIDO: o campo Nome Completo é lido com uma única chamada a
 * scanner.nextLine(), sem laço de validação de campo vazio. O CT-002 abaixo
 * foi escrito para refletir o comportamento EXIGIDO pela Issue 0 e deve
 * falhar (REPROVADO) enquanto esse gap não for corrigido em Main.java.
 */
public class CadastroServidorTest {

    private static final AtomicInteger SEQ = new AtomicInteger();

    private Method cadastrarServidorMethod;
    private CadastroServidor cadastroServidor;
    private ServidorRepository servidorRepository;

    @BeforeEach
    public void configurar() throws Exception {
        // Reseta a lista estática compartilhada de servidores para isolar os testes
        // entre si (ServidorRepository.servidores é 'static final List').
        Field campoServidores = ServidorRepository.class.getDeclaredField("servidores");
        campoServidores.setAccessible(true);
        List<?> listaInterna = (List<?>) campoServidores.get(null);
        listaInterna.clear();

        cadastrarServidorMethod = Main.class.getDeclaredMethod(
                "cadastrarServidor", Scanner.class, CadastroServidor.class, ServidorRepository.class);
        cadastrarServidorMethod.setAccessible(true);

        cadastroServidor = new CadastroServidor();
        servidorRepository = new ServidorRepository();
    }

    // ---------------------------------------------------------------
    // Utilitários
    // ---------------------------------------------------------------

    private String cpfUnico() {
        return String.format("%011d", 10_000_000_000L + SEQ.incrementAndGet());
    }

    private String emailUnico() {
        return "usuario" + SEQ.incrementAndGet() + "@ifpe.edu.br";
    }

    /**
     * Monta a entrada completa do fluxo de cadastro de servidor (uma linha por
     * campo, na ordem exata lida por Main.cadastrarServidor), permitindo
     * customizar nome/cpf/email/senha/sexo para cada cenário de teste.
     * Campos opcionais (Nome Social, Lattes, Telefone) são enviados em branco.
     */
    private String entradaCadastro(String nome, String cpf, String email, String senha,
                                    String campus, String areaFormacao, String titulacao, String sexo) {
        return String.join("\n",
                nome,
                cpf,
                email,
                senha,
                campus,
                areaFormacao,
                titulacao,
                sexo,
                "",   // Nome Social (opcional)
                "",   // Link Lattes (opcional)
                ""    // Telefone (opcional)
        ) + "\n";
    }

    private String entradaCadastroValida(String cpf, String email) {
        return entradaCadastro("Maria da Silva Souza", cpf, email, "senha123",
                "RECIFE", "ENGENHARIAS", "MESTRADO", "FEMININO");
    }

    private static final class Resultado {
        final String saidaConsole;

        Resultado(String saidaConsole) {
            this.saidaConsole = saidaConsole;
        }
    }

    private Resultado invocarCadastrarServidor(String entradaSimulada) throws Exception {
        Scanner scanner = new Scanner(new ByteArrayInputStream(
                entradaSimulada.getBytes(StandardCharsets.UTF_8)));

        ByteArrayOutputStream saidaCapturada = new ByteArrayOutputStream();
        PrintStream saidaOriginal = System.out;
        try {
            System.setOut(new PrintStream(saidaCapturada, true, StandardCharsets.UTF_8));
            cadastrarServidorMethod.invoke(null, scanner, cadastroServidor, servidorRepository);
        } finally {
            System.setOut(saidaOriginal);
        }
        return new Resultado(saidaCapturada.toString(StandardCharsets.UTF_8));
    }

    // =================================================================
    // 1. Nome Completo (CT-001 a CT-004)
    // =================================================================

    // CT-001 — Aceitar nome completo não vazio
    @Test
    public void ct001_aceitarNomeCompletoNaoVazio() throws Exception {
        String cpf = cpfUnico();
        String email = emailUnico();

        Resultado resultado = invocarCadastrarServidor(entradaCadastroValida(cpf, email));

        assertTrue(resultado.saidaConsole.contains("Servidor cadastrado com sucesso!"));
        Servidor servidor = servidorRepository.buscaPorCpf(cpf);
        assertNotNull(servidor);
        assertEquals("Maria da Silva Souza", servidor.getNomeCompleto());
    }

    // CT-002 — Rejeitar nome completo vazio e solicitar novo valor
    @Test
    public void ct002_rejeitarNomeCompletoVazio() throws Exception {
        String cpf = cpfUnico();
        String email = emailUnico();

        // Nome é lido com uma ÚNICA chamada a scanner.nextLine() em
        // Main.cadastrarServidor (sem laço de retry). Por isso, enviamos
        // apenas UMA linha vazia para o nome, seguida do restante do fluxo
        // válido, para não dessincronizar as próximas leituras do Scanner.
        String entrada = "" + "\n" +
                cpf + "\n" + email + "\n" +
                "senha123\nRECIFE\nENGENHARIAS\nMESTRADO\nFEMININO\n\n\n\n";

        Resultado resultado = invocarCadastrarServidor(entrada);

        boolean rejeitouOuNaoConcluiu = resultado.saidaConsole.toLowerCase().contains("obrigat")
                || !resultado.saidaConsole.contains("Servidor cadastrado com sucesso!");

        assertTrue(rejeitouOuNaoConcluiu,
                "GAP DE IMPLEMENTAÇÃO: Main.cadastrarServidor não valida nome vazio "
                        + "(campo lido com scanner.nextLine() único, sem laço de validação). "
                        + "A Issue 0 exige que o campo seja obrigatório - ver observações do "
                        + "Caderno de Testes v1. Este teste deve permanecer REPROVADO até a "
                        + "correção ser implementada em Main.java.");
    }

    // CT-003 — Aceitar nomes com espaços, acentuação e múltiplos sobrenomes
    @Test
    public void ct003_aceitarNomeComAcentuacaoEMultiplosSobrenomes() throws Exception {
        String cpf = cpfUnico();
        String email = emailUnico();
        String nomeComplexo = "José da Conceição Araújo Nascimento Filho";

        String entrada = entradaCadastro(nomeComplexo, cpf, email, "senha123",
                "RECIFE", "ENGENHARIAS", "MESTRADO", "MASCULINO");

        invocarCadastrarServidor(entrada);

        Servidor servidor = servidorRepository.buscaPorCpf(cpf);
        assertNotNull(servidor);
        assertEquals(nomeComplexo, servidor.getNomeCompleto());
    }

    // CT-004 — Manter valor de nome já informado quando outro campo subsequente for inválido
    @Test
    public void ct004_manterNomeQuandoCpfSubsequenteForInvalido() throws Exception {
        String cpfValido = cpfUnico();
        String email = emailUnico();
        String nome = "Ana Paula Ferreira";

        // CPF inválido (poucos dígitos) na 1ª tentativa, válido na 2ª.
        // O laço de validação de CPF existe de fato em Main.java, então este
        // retry não dessincroniza as leituras subsequentes.
        String entrada = nome + "\n"
                + "123\n" + cpfValido + "\n"
                + email + "\nsenha123\nRECIFE\nENGENHARIAS\nMESTRADO\nFEMININO\n\n\n\n";

        Resultado resultado = invocarCadastrarServidor(entrada);

        assertTrue(resultado.saidaConsole.contains(
                "[ERRO] CPF inválido. Informe exatamente 11 dígitos numéricos."));
        Servidor servidor = servidorRepository.buscaPorCpf(cpfValido);
        assertNotNull(servidor);
        assertEquals(nome, servidor.getNomeCompleto(),
                "O nome informado antes do erro de CPF deveria ter sido preservado.");
    }

    // =================================================================
    // 2. CPF (CT-005 a CT-010)
    // =================================================================

    // CT-005 — Aceitar CPF válido com exatamente 11 dígitos numéricos
    @Test
    public void ct005_aceitarCpfValidoComOnzeDigitos() throws Exception {
        String cpf = cpfUnico();
        String email = emailUnico();

        invocarCadastrarServidor(entradaCadastroValida(cpf, email));

        Servidor servidor = servidorRepository.buscaPorCpf(cpf);
        assertNotNull(servidor);
        assertEquals(cpf, servidor.getCpf());
    }

    // CT-006 — Rejeitar CPF com menos de 11 dígitos
    @Test
    public void ct006_rejeitarCpfComMenosDeOnzeDigitos() throws Exception {
        String cpfValido = cpfUnico();
        String email = emailUnico();

        String entrada = "Maria da Silva Souza\n"
                + "1234567890\n"          // 10 dígitos - inválido
                + cpfValido + "\n"
                + email + "\nsenha123\nRECIFE\nENGENHARIAS\nMESTRADO\nFEMININO\n\n\n\n";

        Resultado resultado = invocarCadastrarServidor(entrada);

        assertTrue(resultado.saidaConsole.contains(
                "[ERRO] CPF inválido. Informe exatamente 11 dígitos numéricos."));
        assertNotNull(servidorRepository.buscaPorCpf(cpfValido));
    }

    // CT-007 — Rejeitar CPF com mais de 11 dígitos
    @Test
    public void ct007_rejeitarCpfComMaisDeOnzeDigitos() throws Exception {
        String cpfValido = cpfUnico();
        String email = emailUnico();

        String entrada = "Maria da Silva Souza\n"
                + "123456789012\n"        // 12 dígitos - inválido
                + cpfValido + "\n"
                + email + "\nsenha123\nRECIFE\nENGENHARIAS\nMESTRADO\nFEMININO\n\n\n\n";

        Resultado resultado = invocarCadastrarServidor(entrada);

        assertTrue(resultado.saidaConsole.contains(
                "[ERRO] CPF inválido. Informe exatamente 11 dígitos numéricos."));
        assertNotNull(servidorRepository.buscaPorCpf(cpfValido));
    }

    // CT-008 — Rejeitar CPF contendo letras ou caracteres especiais
    @Test
    public void ct008_rejeitarCpfComCaracteresEspeciais() throws Exception {
        String cpfValido = cpfUnico();
        String email = emailUnico();

        String entrada = "Maria da Silva Souza\n"
                + "123.456.78A\n"         // caracteres inválidos
                + cpfValido + "\n"
                + email + "\nsenha123\nRECIFE\nENGENHARIAS\nMESTRADO\nFEMININO\n\n\n\n";

        Resultado resultado = invocarCadastrarServidor(entrada);

        assertTrue(resultado.saidaConsole.contains(
                "[ERRO] CPF inválido. Informe exatamente 11 dígitos numéricos."));
        assertNotNull(servidorRepository.buscaPorCpf(cpfValido));
    }

    // CT-009 — Rejeitar CPF já cadastrado e exibir mensagem clara
    @Test
    public void ct009_rejeitarCpfJaCadastrado() throws Exception {
        String cpfExistente = cpfUnico();
        String emailExistente = emailUnico();

        // Cadastra um primeiro servidor com o CPF que será reutilizado
        invocarCadastrarServidor(entradaCadastroValida(cpfExistente, emailExistente));
        assertNotNull(servidorRepository.buscaPorCpf(cpfExistente));

        // Tenta cadastrar outro servidor reutilizando o mesmo CPF
        String cpfNovoValido = cpfUnico();
        String emailNovo = emailUnico();
        String entrada = "Outro Servidor\n"
                + cpfExistente + "\n"      // CPF duplicado - deve ser rejeitado
                + cpfNovoValido + "\n"     // CPF válido e único - 2ª tentativa
                + emailNovo + "\nsenha123\nRECIFE\nENGENHARIAS\nMESTRADO\nMASCULINO\n\n\n\n";

        Resultado resultado = invocarCadastrarServidor(entrada);

        assertTrue(resultado.saidaConsole.contains("[ERRO] CPF já cadastrado."));
        assertNotNull(servidorRepository.buscaPorCpf(cpfNovoValido));
    }

    // CT-010 — Permitir novo CPF único após erro de CPF duplicado
    @Test
    public void ct010_permitirNovoCpfUnicoAposErroDeDuplicidade() throws Exception {
        String cpfExistente = cpfUnico();
        String emailExistente = emailUnico();
        invocarCadastrarServidor(entradaCadastroValida(cpfExistente, emailExistente));

        String cpfUnicoNovo = cpfUnico();
        String emailNovo = emailUnico();
        String entrada = "Outro Servidor\n"
                + cpfExistente + "\n"
                + cpfUnicoNovo + "\n"
                + emailNovo + "\nsenha123\nRECIFE\nENGENHARIAS\nMESTRADO\nMASCULINO\n\n\n\n";

        Resultado resultado = invocarCadastrarServidor(entrada);

        assertTrue(resultado.saidaConsole.contains("Servidor cadastrado com sucesso!"));
        Servidor novoServidor = servidorRepository.buscaPorCpf(cpfUnicoNovo);
        assertNotNull(novoServidor);
        assertEquals(cpfUnicoNovo, novoServidor.getCpf());
    }

    // =================================================================
    // 3. E-mail Institucional (CT-011 a CT-016)
    // =================================================================

    // CT-011 — Aceitar e-mail institucional válido no formato usuario@dominio.com
    @Test
    public void ct011_aceitarEmailValido() throws Exception {
        String cpf = cpfUnico();
        String email = emailUnico();

        invocarCadastrarServidor(entradaCadastroValida(cpf, email));

        Servidor servidor = servidorRepository.buscaPorEmail(email);
        assertNotNull(servidor);
        assertEquals(email, servidor.getEmailInstitucional());
    }

    // CT-012 — Rejeitar e-mail sem @
    @Test
    public void ct012_rejeitarEmailSemArroba() throws Exception {
        String cpf = cpfUnico();
        String emailValido = emailUnico();

        String entrada = "Maria da Silva Souza\n" + cpf + "\n"
                + "mariaifpe.edu.br\n"     // sem @
                + emailValido + "\n"
                + "senha123\nRECIFE\nENGENHARIAS\nMESTRADO\nFEMININO\n\n\n\n";

        Resultado resultado = invocarCadastrarServidor(entrada);

        assertTrue(resultado.saidaConsole.contains("[ERRO] E-mail institucional inválido."));
        assertNotNull(servidorRepository.buscaPorEmail(emailValido));
    }

    // CT-013 — Rejeitar e-mail sem domínio ou sem parte antes do @
    @Test
    public void ct013_rejeitarEmailSemDominioOuSemParteLocal() throws Exception {
        String cpf = cpfUnico();
        String emailValido = emailUnico();

        String entrada = "Maria da Silva Souza\n" + cpf + "\n"
                + "@ifpe.edu.br\n"         // sem parte local
                + emailValido + "\n"
                + "senha123\nRECIFE\nENGENHARIAS\nMESTRADO\nFEMININO\n\n\n\n";

        Resultado resultado = invocarCadastrarServidor(entrada);

        assertTrue(resultado.saidaConsole.contains("[ERRO] E-mail institucional inválido."));
        assertNotNull(servidorRepository.buscaPorEmail(emailValido));
    }

    // CT-014 — Rejeitar e-mail com domínio inválido
    @Test
    public void ct014_rejeitarEmailComDominioInvalido() throws Exception {
        String cpf = cpfUnico();
        String emailValido = emailUnico();

        String entrada = "Maria da Silva Souza\n" + cpf + "\n"
                + "maria.silva@ifpe\n"     // domínio sem TLD
                + emailValido + "\n"
                + "senha123\nRECIFE\nENGENHARIAS\nMESTRADO\nFEMININO\n\n\n\n";

        Resultado resultado = invocarCadastrarServidor(entrada);

        assertTrue(resultado.saidaConsole.contains("[ERRO] E-mail institucional inválido."));
        assertNotNull(servidorRepository.buscaPorEmail(emailValido));
    }

    // CT-015 — Rejeitar e-mail já cadastrado e exibir mensagem clara
    @Test
    public void ct015_rejeitarEmailJaCadastrado() throws Exception {
        String cpfExistente = cpfUnico();
        String emailExistente = emailUnico();
        invocarCadastrarServidor(entradaCadastroValida(cpfExistente, emailExistente));

        String cpfNovo = cpfUnico();
        String emailNovoValido = emailUnico();
        String entrada = "Outro Servidor\n" + cpfNovo + "\n"
                + emailExistente + "\n"     // e-mail duplicado
                + emailNovoValido + "\n"    // e-mail válido e único
                + "senha123\nRECIFE\nENGENHARIAS\nMESTRADO\nMASCULINO\n\n\n\n";

        Resultado resultado = invocarCadastrarServidor(entrada);

        assertTrue(resultado.saidaConsole.contains("[ERRO] E-mail já cadastrado."));
        assertNotNull(servidorRepository.buscaPorEmail(emailNovoValido));
    }

    // CT-016 — Manter o e-mail informado se outro campo inválido for corrigido em seguida
    @Test
    public void ct016_manterEmailQuandoSenhaSubsequenteForInvalida() throws Exception {
        String cpf = cpfUnico();
        String email = emailUnico();

        // Senha inválida (menos de 6 caracteres) na 1ª tentativa, válida na 2ª.
        String entrada = "Maria da Silva Souza\n" + cpf + "\n" + email + "\n"
                + "123\n"           // senha inválida
                + "senha123\n"      // senha válida
                + "RECIFE\nENGENHARIAS\nMESTRADO\nFEMININO\n\n\n\n";

        Resultado resultado = invocarCadastrarServidor(entrada);

        assertTrue(resultado.saidaConsole.contains(
                "[ERRO] A senha deve ter no mínimo 6 caracteres."));
        Servidor servidor = servidorRepository.buscaPorEmail(email);
        assertNotNull(servidor);
        assertEquals(email, servidor.getEmailInstitucional(),
                "O e-mail informado antes do erro de senha deveria ter sido preservado.");
    }
}
