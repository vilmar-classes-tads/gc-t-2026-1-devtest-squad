package br.edu.ifpe.sistemaeditais;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class MainTest {

    private Method lerCampoObrigatorioMethod;

    @BeforeEach
    public void configurar() throws Exception {
        lerCampoObrigatorioMethod = Main.class.getDeclaredMethod(
                "lerCampoObrigatorio", Scanner.class, String.class);
        lerCampoObrigatorioMethod.setAccessible(true);
    }

    private String invocarLerCampoObrigatorio(String entradaSimulada) throws Exception {
        Scanner scanner = new Scanner(new ByteArrayInputStream(
                entradaSimulada.getBytes(StandardCharsets.UTF_8)));
        return (String) lerCampoObrigatorioMethod.invoke(null, scanner, "Campo: ");
    }

    // CT-045 — Aceitar título não vazio
    @Test
    public void ct045_tituloNaoVazio() throws Exception {
        String titulo = invocarLerCampoObrigatorio("Robótica na Rede Pública\n");

        assertEquals("Robótica na Rede Pública", titulo);
    }

    // CT-046 — Rejeitar título vazio no CLI (lerCampoObrigatorio deve reexibir o erro e pedir de novo)
    @Test
    public void ct046_tituloVazio() throws Exception {
        ByteArrayOutputStream saidaCapturada = new ByteArrayOutputStream();
        PrintStream saidaOriginal = System.out;
        System.setOut(new PrintStream(saidaCapturada, true, StandardCharsets.UTF_8));

        String titulo;
        try {
            titulo = invocarLerCampoObrigatorio("\nRobótica na Rede Pública\n");
        } finally {
            System.setOut(saidaOriginal);
        }

        String saida = saidaCapturada.toString(StandardCharsets.UTF_8);

        assertTrue(saida.contains("[ERRO] Este campo é obrigatório."),
                "Deveria exibir a mensagem de erro ao receber campo vazio.");
        assertEquals("Robótica na Rede Pública", titulo,
                "Após o erro, o campo deveria aceitar o valor informado na segunda tentativa.");
    }

    // CT-047 — Aceitar resumo não vazio
    @Test
    public void ct047_resumoNaoVazio_deveSerAceitoDeImediato() throws Exception {
        String resumo = invocarLerCampoObrigatorio(
                "Projeto de extensão de robótica em escolas públicas.\n");

        assertEquals(
                "Projeto de extensão de robótica em escolas públicas.",
                resumo);
    }

    // CT-048 — Aceitar palavras-chave não vazias
    @Test
    public void ct048_palavrasChaveNaoVazias_devemSerAceitasDeImediato() throws Exception {
        String palavrasChave = invocarLerCampoObrigatorio(
                "robótica, educação, extensão, tecnologia\n");

        assertEquals("robótica, educação, extensão, tecnologia", palavrasChave);
    }
}
