package br.edu.ifpe.sistemaeditais.service;

import br.edu.ifpe.sistemaeditais.model.Membro;
import br.edu.ifpe.sistemaeditais.model.Projeto;
import br.edu.ifpe.sistemaeditais.repository.ProjetoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TDD - Issue 4: Gestão de Membros da Equipe")
class MembroServiceTest {

    private MembroService membroService;
    private ProjetoRepository projetoRepository;
    private Projeto projeto;

    @BeforeEach
    void setUp() {
        projetoRepository = new ProjetoRepository();
        membroService = new MembroService(projetoRepository);
        
        // Projeto base para associar a equipe
        projeto = new Projeto();
        projeto.setTitulo("Robótica Educacional");
        projetoRepository.salvar(projeto);
    }

    @Nested
    @DisplayName("1. Adição de Membro (CT-065, CT-070, CT-071, CT-072)")
    class AdicaoMembroTests {

        @Test
        @DisplayName("CT-065 / CT-071: Deve adicionar membro com dados válidos e CPF único de 11 dígitos")
        void deveAdicionarMembroComDadosValidos() {
            Membro novoMembro = new Membro("João Silva", "12345678901", "Bolsista", "20h");

            assertDoesNotThrow(() -> membroService.adicionarMembro(projeto, novoMembro));

            List<Membro> membros = membroService.listarMembros(projeto);
            assertEquals(1, membros.size());
            assertEquals("João Silva", membros.get(0).getNome());
            assertEquals("12345678901", membros.get(0).getCpf());
        }

        @Test
        @DisplayName("CT-070: Deve rejeitar adição de membro com CPF inválido (menos de 11 dígitos)")
        void deveRejeitarMembroComCpfInvalido() {
            Membro membroInvalido = new Membro("Maria Santos", "123", "Voluntário", "10h");

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> membroService.adicionarMembro(projeto, membroInvalido)
            );

            assertTrue(exception.getMessage().contains("CPF inválido"));
            assertTrue(membroService.listarMembros(projeto).isEmpty());
        }

        @Test
        @DisplayName("CT-072: Deve rejeitar adição de membro com CPF já cadastrado na equipe")
        void deveRejeitarMembroComCpfDuplicado() {
            Membro m1 = new Membro("João Silva", "12345678901", "Bolsista", "20h");
            Membro m2 = new Membro("Carlos Lima", "12345678901", "Voluntário", "10h");

            membroService.adicionarMembro(projeto, m1);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> membroService.adicionarMembro(projeto, m2)
            );

            assertEquals("CPF já cadastrado na equipe.", exception.getMessage());
            assertEquals(1, membroService.listarMembros(projeto).size());
        }
    }

    @Nested
    @DisplayName("2. Remoção de Membro (CT-066)")
    class RemocaoMembroTests {

        @Test
        @DisplayName("CT-066: Deve remover membro cadastrado da equipe com sucesso")
        void deveRemoverMembroDaEquipe() {
            Membro m1 = new Membro("João Silva", "12345678901", "Bolsista", "20h");
            membroService.adicionarMembro(projeto, m1);

            assertDoesNotThrow(() -> membroService.removerMembro(projeto, "12345678901"));

            List<Membro> membros = membroService.listarMembros(projeto);
            assertTrue(membros.isEmpty());
        }
    }

    @Nested
    @DisplayName("4. Listagem de Membros (CT-073)")
    class ListagemMembrosTests {

        @Test
        @DisplayName("CT-073: Deve listar todos os membros da equipe cadastrada com sucesso")
        void deveListarMembrosComSucesso() {
            membroService.adicionarMembro(projeto, new Membro("João Silva", "12345678901", "Bolsista", "20h"));
            membroService.adicionarMembro(projeto, new Membro("Maria Santos", "98765432100", "Voluntário", "12h"));

            List<Membro> membros = membroService.listarMembros(projeto);

            assertEquals(2, membros.size());
            assertEquals("João Silva", membros.get(0).getNome());
            assertEquals("Maria Santos", membros.get(1).getNome());
        }
    }
}