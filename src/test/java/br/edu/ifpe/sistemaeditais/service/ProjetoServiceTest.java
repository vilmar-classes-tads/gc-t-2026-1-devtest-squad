package br.edu.ifpe.sistemaeditais.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.edu.ifpe.sistemaeditais.model.*;
import br.edu.ifpe.sistemaeditais.repository.ProjetoRepository;

public class ProjetoServiceTest {

    @Test
    void deveCriarProjetoComDadosValidos() {
        ProjetoRepository repository = new ProjetoRepository();
        ProjetoService service = new ProjetoService(repository);

        Servidor servidor = new Servidor(
            "Nome",
            "email@ifpe.edu",
            "12345678900",
            "senha",
            Campus.RECIFE,
            AreaFormacao.CIENCIAS_DA_SAUDE,
            Titulacao.MESTRADO
        );

        servidor.adicionarPerfil(Perfil.ROLE_COORDENADOR);

        assertDoesNotThrow(() -> {
            service.criarProjeto(
                "Titulo",
                "Resumo",
                "Objetivo",
                "Metodologia",
                AreaTematica.CIENCIAS_DA_SAUDE,
                Campus.RECIFE,
                List.of(ODS.ODS_4_EDUCACAO_QUALIDADE),
                true,
                servidor
            );
        });
    }

    @Test
    void naoDeveCriarProjetoComServidorNulo() {
        ProjetoRepository repository = new ProjetoRepository();
        ProjetoService service = new ProjetoService(repository);

        assertThrows(SecurityException.class, () -> {
            service.criarProjeto(
                "Titulo",
                "Resumo",
                "Objetivo",
                "Metodologia",
                AreaTematica.CIENCIAS_DA_SAUDE,
                Campus.RECIFE,
                List.of(ODS.ODS_4_EDUCACAO_QUALIDADE),
                true,
                null
            );
        });
    }
}