package br.edu.ifpe.sistemaeditais.model;

import java.util.ArrayList;
import java.util.List;

public class Servidor{
    private String nomeCompleto;
    private String cpf;
    private String emailInstitucional;
    private String senha;
    private Campus campus;
    private AreaFormacao areaFormacao;
    private Titulacao titulacao;
    private Sexo sexo;
    private String nomeSocial;
    private String linkLattes;
    private String telefone;

    private List<Perfil> perfis = new ArrayList<>();

    public Servidor(String nomeCompleto, String cpf, String emailInstitucional, String senha, Campus campus, AreaFormacao areaFormacao, Titulacao titulacao){
        this.nomeCompleto = nomeCompleto;
        this.cpf = cpf;
        this.emailInstitucional = emailInstitucional;
        this.senha = senha;
        this.campus = campus;
        this.areaFormacao = areaFormacao;
        this.titulacao = titulacao;
    }

    public String getNomeCompleto(){
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto){
        this.nomeCompleto = nomeCompleto;
    }

    public String getCpf(){
        return cpf;
    }

    public String getEmailInstitucional(){
        return emailInstitucional;
    }

    public String getSenha(){
        return senha;
    }

    public void setSenha(String senha){
        this.senha = senha;
    }

    public Campus getCampus(){
        return campus;
    }

    public void setCampus(Campus campus){
        this.campus = campus;
    }

    public AreaFormacao getAreaFormacao(){
        return areaFormacao;
    }

    public void setAreaFormacao(AreaFormacao areaFormacao){
        this.areaFormacao = areaFormacao;
    }

    public Titulacao getTitulacao(){
        return titulacao;
    }

    public void setTitulacao(Titulacao titulacao){
        this.titulacao = titulacao;
    }

    public Sexo getSexo(){
        return sexo;
    }

    public void setSexo(Sexo sexo){
        this.sexo = sexo;
    }

    public String getNomeSocial(){
        return nomeSocial;
    }

    public void setNomeSocial(String nomeSocial){
        this.nomeSocial = nomeSocial;
    }

    public String getLinkLattes(){
        return linkLattes;
    }

    public void setLinkLattes(String linkLattes){
        this.linkLattes = linkLattes;
    }

    public String getTelefone(){
        return telefone;
    }

    public void setTelefone(String telefone){
        this.telefone = telefone;
    }

    public List<Perfil> getPerfis() { return perfis; }
 
    public void adicionarPerfil(Perfil perfil) {
        if (!perfis.contains(perfil)) {
            perfis.add(perfil);
        }
    }
}