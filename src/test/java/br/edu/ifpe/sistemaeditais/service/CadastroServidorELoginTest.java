package br.edu.ifpe.sistemaeditais.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.edu.ifpe.sistemaeditais.Main;
import br.edu.ifpe.sistemaeditais.model.Perfil;
import br.edu.ifpe.sistemaeditais.model.Servidor;
import br.edu.ifpe.sistemaeditais.repository.ServidorRepository;

public class CadastroServidorELoginTest {

    private static final String NOME_BASE = "Maria da Silva Souza";
    private static final String CPF_BASE = "12345678901";
    private static final String EMAIL_BASE = "maria.silva@ifpe.edu.br";
    private static final String SENHA_BASE = "senha123";
    private static final String CAMPUS_BASE = "RECIFE";
    private static final String AREA_FORMACAO_BASE = "ENGENHARIAS";
    private static final String TITULACAO_BASE = "MESTRADO";
    private static final String SEXO_BASE = "FEMININO";
    private static final String NOME_SOCIAL_BASE = "";
    private static final String LINK_LATTES_BASE = "";
    private static final String TELEFONE_BASE = "";

    private Method cadastrarServidorMethod;
    private Method realizarLoginMethod;
    private CadastroServidor cadastroServidor;
    private ServidorRepository servidorRepository;

    @BeforeEach
    public void configurar() throws Exception {
        cadastrarServidorMethod = Main.class.getDeclaredMethod(
                "cadastrarServidor",
                Scanner.class,
                CadastroServidor.class,
                ServidorRepository.class
        );
        cadastrarServidorMethod.setAccessible(true);

        realizarLoginMethod = Main.class.getDeclaredMethod(
                "realizarLogin",
                Scanner.class,
                ServidorRepository.class
        );
        realizarLoginMethod.setAccessible(true);

        servidorRepository = new ServidorRepository();
        cadastroServidor = new CadastroServidor(servidorRepository);

        limparRepositorioDeServidores();
    }

    private void limparRepositorioDeServidores() throws Exception {
        Field campoServidores = ServidorRepository.class.getDeclaredField("servidores");
        campoServidores.setAccessible(true);
        List<?> lista = (List<?>) campoServidores.get(null);
        lista.clear();
    }

    private String entradaCadastro(String nome, String cpf, String email, String senha,
                                    String campus, String areaFormacao, String titulacao,
                                    String sexo, String nomeSocial, String linkLattes, String telefone) {
        return nome + "\n"
                + cpf + "\n"
                + email + "\n"
                + senha + "\n"
                + campus + "\n"
                + areaFormacao + "\n"
                + titulacao + "\n"
                + sexo + "\n"
                + nomeSocial + "\n"
                + linkLattes + "\n"
                + telefone + "\n";
    }

    private String entradaServidorBase() {
        return entradaCadastro(NOME_BASE, CPF_BASE, EMAIL_BASE, SENHA_BASE, CAMPUS_BASE,
                AREA_FORMACAO_BASE, TITULACAO_BASE, SEXO_BASE, NOME_SOCIAL_BASE, LINK_LATTES_BASE, TELEFONE_BASE);
    }

    private String invocarCadastrarServidor(String entradaSimulada) throws Exception {
        Scanner scanner = new Scanner(new ByteArrayInputStream(
                entradaSimulada.getBytes(StandardCharsets.UTF_8)));

        ByteArrayOutputStream saidaCapturada = new ByteArrayOutputStream();
        PrintStream saidaOriginal = System.out;
        System.setOut(new PrintStream(saidaCapturada, true, StandardCharsets.UTF_8));
        try {
            cadastrarServidorMethod.invoke(null, scanner, cadastroServidor, servidorRepository);
        } finally {
            System.setOut(saidaOriginal);
        }
        return saidaCapturada.toString(StandardCharsets.UTF_8);
    }

    private String invocarRealizarLogin(String entradaSimulada) throws Exception {
        Scanner scanner = new Scanner(new ByteArrayInputStream(
                entradaSimulada.getBytes(StandardCharsets.UTF_8)));

        ByteArrayOutputStream saidaCapturada = new ByteArrayOutputStream();
        PrintStream saidaOriginal = System.out;
        System.setOut(new PrintStream(saidaCapturada, true, StandardCharsets.UTF_8));
        try {
            realizarLoginMethod.invoke(null, scanner, servidorRepository);
        } finally {
            System.setOut(saidaOriginal);
        }
        return saidaCapturada.toString(StandardCharsets.UTF_8);
    }

    private Servidor obterUsuarioLogado() throws Exception {
        Field campo = Main.class.getDeclaredField("usuarioLogado");
        campo.setAccessible(true);
        return (Servidor) campo.get(null);
    }

    // CT-017 — Aceitar nome social preenchido e armazená-lo
    // Delta: Nome Social = "Maria Souza"
    @Test
    public void ct017_nomeSocialPreenchido_deveSerArmazenado() throws Exception {
        String entrada = entradaCadastro(NOME_BASE, CPF_BASE, EMAIL_BASE, SENHA_BASE, CAMPUS_BASE,
                AREA_FORMACAO_BASE, TITULACAO_BASE, SEXO_BASE, "Maria Souza", LINK_LATTES_BASE, TELEFONE_BASE);

        invocarCadastrarServidor(entrada);

        Servidor cadastrado = servidorRepository.buscaPorCpf(CPF_BASE);
        assertNotNull(cadastrado, "O servidor deveria ter sido cadastrado.");
        assertEquals("Maria Souza", cadastrado.getNomeSocial(),
                "O nome social informado deveria ter sido armazenado no objeto Servidor.");
    }

    // CT-018 — Aceitar nome social vazio e prosseguir sem erro
    // Sem delta: Nome Social = "" (igual ao Servidor Base)
    @Test
    public void ct018_nomeSocialVazio_deveProsseguirSemErro() throws Exception {
        String saida = invocarCadastrarServidor(entradaServidorBase());

        Servidor cadastrado = servidorRepository.buscaPorCpf(CPF_BASE);
        assertNotNull(cadastrado, "O servidor deveria ter sido cadastrado mesmo sem nome social.");
        assertNull(cadastrado.getNomeSocial(),
                "setNomeSocial() não deveria ter sido chamado, mantendo o campo nulo.");
        assertTrue(saida.contains("Servidor cadastrado com sucesso!"),
                "O cadastro deveria concluir sem erro mesmo com nome social vazio.");
    }

    // CT-019 — Cadastro completo com todos os campos obrigatórios válidos exibe mensagem de sucesso
    // Sem delta: todos os campos do Servidor Base
    @Test
    public void ct019_cadastroCompletoValido_exibeMensagemDeSucesso() throws Exception {
        String saida = invocarCadastrarServidor(entradaServidorBase());

        assertTrue(saida.contains("Servidor cadastrado com sucesso!"),
                "Deveria exibir a mensagem de sucesso no console.");
    }

    // CT-020 — Atribuir os perfis ROLE_COORDENADOR e ROLE_AVALIADOR automaticamente
    // Sem delta: Servidor Base
    @Test
    public void ct020_atribuiPerfisAutomaticamente() throws Exception {
        invocarCadastrarServidor(entradaServidorBase());

        Servidor cadastrado = servidorRepository.buscaPorCpf(CPF_BASE);
        assertNotNull(cadastrado);
        assertTrue(cadastrado.getPerfis().contains(Perfil.ROLE_COORDENADOR),
                "O perfil ROLE_COORDENADOR deveria ser atribuído automaticamente.");
        assertTrue(cadastrado.getPerfis().contains(Perfil.ROLE_AVALIADOR),
                "O perfil ROLE_AVALIADOR deveria ser atribuído automaticamente.");
    }

    // CT-021 — CPF inválido não deve limpar o Nome Completo já informado
    // Delta: Nome Completo válido (base); CPF 1ª tentativa inválido ("123"), 2ª tentativa válida (base)
    @Test
    public void ct021_cpfInvalido_naoDeveLimparNomeCompleto() throws Exception {
        String entrada = NOME_BASE + "\n"
                + "123\n"                 // CPF inválido (1ª tentativa)
                + CPF_BASE + "\n"          // CPF válido (2ª tentativa)
                + EMAIL_BASE + "\n"
                + SENHA_BASE + "\n"
                + CAMPUS_BASE + "\n"
                + AREA_FORMACAO_BASE + "\n"
                + TITULACAO_BASE + "\n"
                + SEXO_BASE + "\n"
                + NOME_SOCIAL_BASE + "\n"
                + LINK_LATTES_BASE + "\n"
                + TELEFONE_BASE + "\n";

        String saida = invocarCadastrarServidor(entrada);

        assertTrue(saida.contains("CPF inválido. Informe exatamente 11 dígitos numéricos."),
                "Deveria solicitar novamente o CPF ao receber um valor inválido.");

        Servidor cadastrado = servidorRepository.buscaPorCpf(CPF_BASE);
        assertNotNull(cadastrado, "O cadastro deveria prosseguir após informar um CPF válido.");
        assertEquals(NOME_BASE, cadastrado.getNomeCompleto(),
                "O Nome Completo informado antes do CPF inválido não deveria ter sido descartado.");
    }

    // CT-022 — Login com e-mail institucional e senha correta deve autenticar o usuário recém-cadastrado
    // Pré-condição: servidor base recém-cadastrado com e-mail "maria.silva@ifpe.edu.br" e senha "senha123"
    @Test
    public void ct022_loginComEmailESenhaCorretos_deveAutenticar() throws Exception {
        invocarCadastrarServidor(entradaServidorBase());

        String saida = invocarRealizarLogin(EMAIL_BASE + "\n" + SENHA_BASE + "\n");

        assertTrue(saida.contains("Autenticação realizada com sucesso!"),
                "Deveria exibir a mensagem de autenticação bem-sucedida.");

        Servidor usuarioLogado = obterUsuarioLogado();
        assertNotNull(usuarioLogado, "usuarioLogado deveria ter sido definido após o login.");
        assertEquals(EMAIL_BASE, usuarioLogado.getEmailInstitucional());
    }

    // CT-023 — Login com senha incorreta deve falhar
    // Delta: Senha = "senhaErrada" (incorreta)
    @Test
    public void ct023_loginComSenhaIncorreta_deveFalhar() throws Exception {
        invocarCadastrarServidor(entradaServidorBase());

        String saida = invocarRealizarLogin(EMAIL_BASE + "\nsenhaErrada\n");

        assertTrue(saida.contains("[ERRO] Usuário ou senha inválidos."),
                "Deveria exibir mensagem de erro ao tentar login com senha incorreta.");
    }
}