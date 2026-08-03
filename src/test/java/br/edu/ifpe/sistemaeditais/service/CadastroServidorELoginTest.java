package br.edu.ifpe.sistemaeditais.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
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

    private Method cadastrarServidorMethod;
    private Method realizarLoginMethod;
    private CadastroServidor cadastroServidor;
    private ServidorRepository servidorRepository;

    @BeforeEach
    public void configurar() throws Exception {
        cadastrarServidorMethod = Main.class.getDeclaredMethod(
                "cadastrarServidor", Scanner.class, CadastroServidor.class, ServidorRepository.class);
        cadastrarServidorMethod.setAccessible(true);

        realizarLoginMethod = Main.class.getDeclaredMethod(
                "realizarLogin", Scanner.class, ServidorRepository.class);
        realizarLoginMethod.setAccessible(true);

        cadastroServidor = new CadastroServidor();
        servidorRepository = new ServidorRepository();
    }

    private String entradaCadastroBase(String nome, String cpf, String email, String senha,
                                        String campus, String areaFormacao, String titulacao,
                                        String sexo, String nomeSocial) {
        return nome + "\n"
                + cpf + "\n"
                + email + "\n"
                + senha + "\n"
                + campus + "\n"
                + areaFormacao + "\n"
                + titulacao + "\n"
                + sexo + "\n"
                + nomeSocial + "\n"
                + "\n"   // Link Lattes (opcional)
                + "\n";  // Telefone (opcional)
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
    @Test
    public void ct017_nomeSocialPreenchido_deveSerArmazenado() throws Exception {
        String entrada = entradaCadastroBase(
                "Maria de Souza Lima", "10000000017", "ct017@ifpe.edu.br", "senha123",
                "RECIFE", "CIENCIAS_EXATAS_E_DA_TERRA", "MESTRADO", "FEMININO", "Maria Souza");

        invocarCadastrarServidor(entrada);

        Servidor cadastrado = servidorRepository.buscaPorCpf("10000000017");
        assertNotNull(cadastrado, "O servidor deveria ter sido cadastrado.");
        assertEquals("Maria Souza", cadastrado.getNomeSocial(),
                "O nome social informado deveria ter sido armazenado no objeto Servidor.");
    }

    // CT-018 — Aceitar nome social vazio e prosseguir sem erro
    @Test
    public void ct018_nomeSocialVazio_deveProsseguirSemErro() throws Exception {
        String entrada = entradaCadastroBase(
                "Joana Pereira", "10000000018", "ct018@ifpe.edu.br", "senha123",
                "RECIFE", "CIENCIAS_BIOLOGICAS", "GRADUACAO", "FEMININO", "");

        String saida = invocarCadastrarServidor(entrada);

        Servidor cadastrado = servidorRepository.buscaPorCpf("10000000018");
        assertNotNull(cadastrado, "O servidor deveria ter sido cadastrado mesmo sem nome social.");
        assertNull(cadastrado.getNomeSocial(),
                "setNomeSocial() não deveria ter sido chamado, mantendo o campo nulo.");
        assertTrue(saida.contains("Servidor cadastrado com sucesso!"),
                "O cadastro deveria concluir sem erro mesmo com nome social vazio.");
    }

    // CT-019 — Cadastro completo com todos os campos obrigatórios válidos exibe mensagem de sucesso
    @Test
    public void ct019_cadastroCompletoValido_exibeMensagemDeSucesso() throws Exception {
        String entrada = entradaCadastroBase(
                "Pedro Henrique Alves", "10000000019", "ct019@ifpe.edu.br", "senha123",
                "CARUARU", "ENGENHARIAS", "DOUTORADO", "MASCULINO", "");

        String saida = invocarCadastrarServidor(entrada);

        assertTrue(saida.contains("Servidor cadastrado com sucesso!"),
                "Deveria exibir a mensagem de sucesso no console.");
    }

    // CT-020 — Atribuir os perfis ROLE_COORDENADOR e ROLE_AVALIADOR automaticamente
    @Test
    public void ct020_atribuiPerfisAutomaticamente() throws Exception {
        String entrada = entradaCadastroBase(
                "Ana Beatriz Costa", "10000000020", "ct020@ifpe.edu.br", "senha123",
                "OLINDA", "CIENCIAS_DA_SAUDE", "ESPECIALIZACAO", "FEMININO", "");

        invocarCadastrarServidor(entrada);

        Servidor cadastrado = servidorRepository.buscaPorCpf("10000000020");
        assertNotNull(cadastrado);
        assertTrue(cadastrado.getPerfis().contains(Perfil.ROLE_COORDENADOR),
                "O perfil ROLE_COORDENADOR deveria ser atribuído automaticamente.");
        assertTrue(cadastrado.getPerfis().contains(Perfil.ROLE_AVALIADOR),
                "O perfil ROLE_AVALIADOR deveria ser atribuído automaticamente.");
    }

    // CT-021 — CPF inválido não deve limpar o Nome Completo já informado
    @Test
    public void ct021_cpfInvalido_naoDeveLimparNomeCompleto() throws Exception {
        String entrada = "Carlos Eduardo Lima\n"      // Nome completo
                + "123\n"                              // CPF inválido (1ª tentativa)
                + "10000000021\n"                      // CPF válido (2ª tentativa)
                + "ct021@ifpe.edu.br\n"
                + "senha123\n"
                + "RECIFE\n"
                + "ENGENHARIAS\n"
                + "GRADUACAO\n"
                + "MASCULINO\n"
                + "\n"
                + "\n"
                + "\n";

        String saida = invocarCadastrarServidor(entrada);

        assertTrue(saida.contains("CPF inválido. Informe exatamente 11 dígitos numéricos."),
                "Deveria solicitar novamente o CPF ao receber um valor inválido.");

        Servidor cadastrado = servidorRepository.buscaPorCpf("10000000021");
        assertNotNull(cadastrado, "O cadastro deveria prosseguir após informar um CPF válido.");
        assertEquals("Carlos Eduardo Lima", cadastrado.getNomeCompleto(),
                "O Nome Completo informado antes do CPF inválido não deveria ter sido descartado.");
    }

    // CT-022 — Login com e-mail institucional e senha correta deve autenticar o usuário recém-cadastrado
    @Test
    public void ct022_loginComEmailESenhaCorretos_deveAutenticar() throws Exception {
        garantirServidorMariaSilvaCadastrado();

        String saida = invocarRealizarLogin("maria.silva@ifpe.edu.br\nsenha123\n");

        assertTrue(saida.contains("Autenticação realizada com sucesso!"),
                "Deveria exibir a mensagem de autenticação bem-sucedida.");

        Servidor usuarioLogado = obterUsuarioLogado();
        assertNotNull(usuarioLogado, "usuarioLogado deveria ter sido definido após o login.");
        assertEquals("maria.silva@ifpe.edu.br", usuarioLogado.getEmailInstitucional());
    }

    // CT-023 — Login com senha incorreta deve falhar
    @Test
    public void ct023_loginComSenhaIncorreta_deveFalhar() throws Exception {
        garantirServidorMariaSilvaCadastrado();

        String saida = invocarRealizarLogin("maria.silva@ifpe.edu.br\nsenhaErrada\n");

        assertTrue(saida.contains("[ERRO] Usuário ou senha inválidos."),
                "Deveria exibir mensagem de erro ao tentar login com senha incorreta.");
    }

    private void garantirServidorMariaSilvaCadastrado() throws Exception {
        if (servidorRepository.buscaPorEmail("maria.silva@ifpe.edu.br") != null) {
            return;
        }
        String entrada = entradaCadastroBase(
                "Maria Silva Santos", "10000000090", "maria.silva@ifpe.edu.br", "senha123",
                "RECIFE", "CIENCIAS_EXATAS_E_DA_TERRA", "MESTRADO", "FEMININO", "");
        invocarCadastrarServidor(entrada);
    }
}