package br.edu.ifpe.sistemaeditais.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import br.edu.ifpe.sistemaeditais.model.*;

public class CadastroServidorTest {

    @Test
    void senhaDeveSerCriptografada() {
        CadastroServidor service = new CadastroServidor();

        Servidor servidor = new Servidor(
            "Nome",
            "12345678900",
            "email@ifpe.edu",
            "senha123",
            Campus.RECIFE,
            AreaFormacao.CIENCIAS_DA_SAUDE,
            Titulacao.MESTRADO
        );

        service.cadastrar(servidor);

        assertNotEquals("senha123", servidor.getSenha());
        assertNotNull(servidor.getSenha());

        assertTrue(servidor.getPerfis().contains(Perfil.ROLE_COORDENADOR));
        assertTrue(servidor.getPerfis().contains(Perfil.ROLE_AVALIADOR));
    }
}