package br.edu.ifpe.sistemaeditais.model;

import java.util.ArrayList;
import java.util.List;

public class Projeto {

    private Long id;
    private String titulo;
    private String resumo;
    private String palavrasChave;
    private String publicoAlvo;
    private AreaTematica areaTematica;
    private Campus campus;
    private List<ODS> odsSelecionados;
    private boolean aceitouTermoDeCompromisso;
    private StatusProjeto status;
    private Servidor coordenador;
    private List<Membro> equipe = new ArrayList<>();
    private Edital edital;
    private byte[] anexo;

    public Projeto(String titulo, String resumo, String palavrasChave,
                   String publicoAlvo, AreaTematica areaTematica, Campus campus,
                   List<ODS> odsSelecionados, boolean aceitouTermoDeCompromisso,
                   Servidor coordenador) {
        this.titulo = titulo;
        this.resumo = resumo;
        this.palavrasChave = palavrasChave;
        this.publicoAlvo = publicoAlvo;
        this.areaTematica = areaTematica;
        this.campus = campus;
        this.odsSelecionados = odsSelecionados;
        this.aceitouTermoDeCompromisso = aceitouTermoDeCompromisso;
        this.coordenador = coordenador;
        this.status = StatusProjeto.RASCUNHO;
    }

    /**
     * Regra de negócio: apenas projetos em RASCUNHO ou EM_CORRECAO podem ser editados.
     */
    public void editarProjeto(String titulo, String resumo, String palavrasChave,
                               String publicoAlvo, AreaTematica areaTematica, Campus campus,
                               List<ODS> odsSelecionados, boolean aceitouTermoDeCompromisso) {
        if (this.status != StatusProjeto.RASCUNHO && this.status != StatusProjeto.EM_CORRECAO) {
            throw new IllegalStateException(
                "Projeto não pode ser editado. Status atual: \"" + this.status +
                "\". Somente projetos com status RASCUNHO ou EM_CORRECAO podem ser alterados.");
        }
        this.titulo = titulo;
        this.resumo = resumo;
        this.palavrasChave = palavrasChave;
        this.publicoAlvo = publicoAlvo;
        this.areaTematica = areaTematica;
        this.campus = campus;
        this.odsSelecionados = odsSelecionados;
        this.aceitouTermoDeCompromisso = aceitouTermoDeCompromisso;
    }


    public Long getId() {
        return id;
    }
    public void setId(Long id) {
    this.id = id;
}

    public byte[] getAnexo() {
        return anexo;
    }

    public void setAnexo(byte[] anexo) {
        this.anexo = anexo;
    }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getResumo() { return resumo; }
    public void setResumo(String resumo) { this.resumo = resumo; }

    public String getPalavrasChave() { return palavrasChave; }
    public void setPalavrasChave(String palavrasChave) { this.palavrasChave = palavrasChave; }

    public String getPublicoAlvo() { return publicoAlvo; }
    public void setPublicoAlvo(String publicoAlvo) { this.publicoAlvo = publicoAlvo; }

    public AreaTematica getAreaTematica() { return areaTematica; }
    public void setAreaTematica(AreaTematica areaTematica) { this.areaTematica = areaTematica; }

    public Campus getCampus() { return campus; }
    public void setCampus(Campus campus) { this.campus = campus; }

    public List<ODS> getOdsSelecionados() { return odsSelecionados; }
    public void setOdsSelecionados(List<ODS> odsSelecionados) { this.odsSelecionados = odsSelecionados; }

    public boolean isAceitouTermoDeCompromisso() { return aceitouTermoDeCompromisso; }
    public void setAceitouTermoDeCompromisso(boolean v) { this.aceitouTermoDeCompromisso = v; }

    public StatusProjeto getStatus() { return status; }
    public void setStatus(StatusProjeto status) { this.status = status; }

    public Servidor getCoordenador() { return coordenador; }
    public void setCoordenador(Servidor coordenador) { this.coordenador = coordenador; }

    public List<Membro> getEquipe() { return equipe; }

    public Edital getEdital() { return edital; }
    public void setEdital(Edital edital) { this.edital = edital; }

    public void adicionarMembro(Membro membro) {
        if (membro == null) {
            throw new IllegalArgumentException("Membro não pode ser nulo.");
        }
        boolean cpfJaCadastrado = equipe.stream()
                .anyMatch(m -> m.getCpf().equals(membro.getCpf()));
        if (cpfJaCadastrado) {
            throw new IllegalArgumentException(
                "Já existe um membro cadastrado com este CPF na equipe do projeto.");
        }
        equipe.add(membro);
    }

    public void removerMembro(String cpf) {
        boolean removido = equipe.removeIf(m -> m.getCpf().equals(cpf));
        if (!removido) {
            throw new IllegalArgumentException("Membro não encontrado na equipe do projeto.");
        }
    }

    public Membro buscarMembroPorCpf(String cpf) {
        return equipe.stream()
                .filter(m -> m.getCpf().equals(cpf))
                .findFirst()
                .orElse(null);
    }
}