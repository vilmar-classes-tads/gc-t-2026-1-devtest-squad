package br.edu.ifpe.sistemaeditais.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import br.edu.ifpe.sistemaeditais.model.*;

public class EditalServiceTest {

    private Servidor criarAdmin() {
        Servidor admin = new Servidor(
            "Admin",
            "12345678900",
            "admin@ifpe.edu",
            "senha",
            Campus.RECIFE,
            AreaFormacao.CIENCIAS_DA_SAUDE,
            Titulacao.MESTRADO
        );

        admin.adicionarPerfil(Perfil.ROLE_ADMIN);

        return admin;
    }

    @Test
    void deveCriarEditalComDatasValidas() {
        EditalService service = new EditalService();

        Edital edital = new Edital(
            "001/2024",
            "Edital Teste",
            2024,
            LocalDate.now(),
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(15),
            LocalDate.now().plusDays(20)
        );

        assertDoesNotThrow(() -> {
            service.criarEdital(edital, criarAdmin());
        });
    }

    @Test
    void naoDeveCriarEditalComDatasInvalidas() {
        EditalService service = new EditalService();

        Edital edital = new Edital(
            "002/2024",
            "Edital Teste",
            2024,
            LocalDate.now(),
            LocalDate.now().minusDays(5),
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(15)
        );

        assertThrows(IllegalArgumentException.class, () -> {
            service.criarEdital(edital, criarAdmin());
        });
    }

    @Test
    void naoDevePermitirUsuarioSemPermissao() {
        EditalService service = new EditalService();

        Edital edital = new Edital(
            "003/2024",
            "Edital Teste",
            2024,
            LocalDate.now(),
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(15),
            LocalDate.now().plusDays(20)
        );

        Servidor comum = new Servidor(
            "User",
            "12345678900",
            "user@ifpe.edu",
            "senha",
            Campus.RECIFE,
            AreaFormacao.CIENCIAS_DA_SAUDE,
            Titulacao.MESTRADO
        );


        assertThrows(SecurityException.class, () -> {
            service.criarEdital(edital, comum);
        });
    }

    @Test
    void naoDeveCriarEditalComDatasNulas() {
        EditalService service = new EditalService();

        Edital edital = new Edital(
            "004/2024",
            "Edital Teste",
            2024,
            null,
            null,
            null,
            null
        );

        assertThrows(IllegalArgumentException.class, () -> {
            service.criarEdital(edital, criarAdmin());
        });
    }
}