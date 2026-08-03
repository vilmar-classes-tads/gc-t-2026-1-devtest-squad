package br.edu.ifpe.sistemaeditais.service;

import br.edu.ifpe.sistemaeditais.model.Membro;
import br.edu.ifpe.sistemaeditais.model.PlanoTrabalho;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TDD - Issue 4: Gestão de Planos de Trabalho")
class PlanoTrabalhoServiceTest {

    private PlanoTrabalhoService planoService;
    private Membro bolsista;

    @BeforeEach
    void setUp() {
        planoService = new PlanoTrabalhoService();
        bolsista = new Membro("Ana Souza", "11122233344", "Bolsista", "20h");
    }

    @Nested
    @DisplayName("3. Limites de Planos de Trabalho (CT-067, CT-068, CT-069)")
    class LimitePlanosTests {

        @Test
        @DisplayName("CT-067: Deve adicionar plano de trabalho dentro do limite permitido (menos de 4 planos)")
        void deveAdicionarPlanoDentroDoLimite() {
            PlanoTrabalho plano = new PlanoTrabalho("Desenvolvimento de protótipo de robô");

            assertDoesNotThrow(() -> planoService.adicionarPlano(bolsista, plano));

            List<PlanoTrabalho> planos = planoService.listarPlanosDoMembro(bolsista);
            assertEquals(1, planos.size());
            assertEquals("Desenvolvimento de protótipo de robô", planos.get(0).getTitulo());
        }

        @Test
        @DisplayName("CT-068: Deve permitir cadastro exatamente até o limite de 4 planos por bolsista")
        void devePermitirAteLimiteMaximoDeQuatroPlanos() {
            planoService.adicionarPlano(bolsista, new PlanoTrabalho("Plano 1 - Estudo Bibliográfico"));
            planoService.adicionarPlano(bolsista, new PlanoTrabalho("Plano 2 - Modelagem do Circuito"));
            planoService.adicionarPlano(bolsista, new PlanoTrabalho("Plano 3 - Montagem do Hardware"));

            // Tenta adicionar o 4º plano (limite exatamente 4)
            PlanoTrabalho quartoPlano = new PlanoTrabalho("Plano 4 - Testes e Validação em Campo");
            assertDoesNotThrow(() -> planoService.adicionarPlano(bolsista, quartoPlano));

            List<PlanoTrabalho> planos = planoService.listarPlanosDoMembro(bolsista);
            assertEquals(4, planos.size());
        }

        @Test
        @DisplayName("CT-069: Deve rejeitar adição acima do limite de 4 planos e exibir mensagem 'Limite de planos excedido'")
        void deveRejeitarAdicaoAcimaDoLimite() {
            // Cadastra 4 planos válidos
            planoService.adicionarPlano(bolsista, new PlanoTrabalho("Plano 1"));
            planoService.adicionarPlano(bolsista, new PlanoTrabalho("Plano 2"));
            planoService.adicionarPlano(bolsista, new PlanoTrabalho("Plano 3"));
            planoService.adicionarPlano(bolsista, new PlanoTrabalho("Plano 4"));

            // Tentativa de adicionar o 5º plano
            PlanoTrabalho quintoPlano = new PlanoTrabalho("Plano 5 - Relatório Final");

            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> planoService.adicionarPlano(bolsista, quintoPlano)
            );

            assertEquals("Limite de planos excedido", exception.getMessage());
            assertEquals(4, planoService.listarPlanosDoMembro(bolsista).size());
        }
    }
}