package br.edu.ifpe.sistemaeditais.repository;

import br.edu.ifpe.sistemaeditais.model.Servidor;
import java.util.ArrayList;
import java.util.List;

public class ServidorRepository{
    
    private static final List<Servidor> servidores = new ArrayList<>();

    public void salvar(Servidor servidor){
        servidores.add(servidor);

    }

    public Servidor buscaPorCpf(String cpf){
        for (Servidor servidor : servidores){
            if(servidor.getCpf().equals(cpf)){
                return servidor;
            }
        }

        return null;
    }

    public Servidor buscaPorEmail(String email){
        for (Servidor servidor : servidores){
            if(servidor.getEmailInstitucional().equals(email)){
                return servidor;
            }
        }


        return null;
    }

    public List<Servidor> ListarServidores(){
        return servidores;
    }

}