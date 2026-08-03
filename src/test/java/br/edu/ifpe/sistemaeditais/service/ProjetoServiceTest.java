package br.edu.ifpe.sistemaeditais.service;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.ifpe.sistemaeditais.model.AreaFormacao;
import br.edu.ifpe.sistemaeditais.model.AreaTematica;
import br.edu.ifpe.sistemaeditais.model.Campus;
import br.edu.ifpe.sistemaeditais.model.ODS;
import br.edu.ifpe.sistemaeditais.model.Perfil;
import br.edu.ifpe.sistemaeditais.model.Projeto;
import br.edu.ifpe.sistemaeditais.model.Servidor;
import br.edu.ifpe.sistemaeditais.model.Titulacao;
import br.edu.ifpe.sistemaeditais.repository.ProjetoRepository;

@ExtendWith(MockitoExtension.class)
public class ProjetoServiceTest {

    @Mock
    private ProjetoRepository projetoRepository;

    @InjectMocks
    private ProjetoService projetoService;

    private Servidor coordenador;

    @BeforeEach
    public void configurar() {
        coordenador = new Servidor(
                "Coordenador Teste",
                "44444444444",
                "coordenador.ct046@ifpe.edu.br",
                "senha123",
                Campus.RECIFE,
                AreaFormacao.ENGENHARIAS,
                Titulacao.MESTRADO
        );
        coordenador.adicionarPerfil(Perfil.ROLE_COORDENADOR);
    }


    @Test
    public void ct046_criarProjetoComTituloVazio() {
        List<ODS> odsSelecionados = Collections.emptyList();

        Projeto projeto = assertDoesNotThrow(() -> projetoService.criarProjeto(
                "",
                "Resumo projeto de teste.",
                "robótica, educação, extensão, tecnologia",
                "Estudantes da rede pública",
                AreaTematica.ENGENHARIAS,
                Campus.RECIFE,
                odsSelecionados,
                true,
                coordenador
        ));

        Assertions.assertEquals("", projeto.getTitulo());
        verify(projetoRepository, times(1)).salvar(projeto);
    }
}
