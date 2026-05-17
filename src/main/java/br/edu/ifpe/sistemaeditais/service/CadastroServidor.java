package br.edu.ifpe.sistemaeditais.service;

import br.edu.ifpe.sistemaeditais.model.Servidor;
import br.edu.ifpe.sistemaeditais.repository.ServidorRepository;

public class CadastroServidor{

    private final ServidorRepository repository = new ServidorRepository();

    public void cadastrar(Servidor novoServidor){

        if(repository.buscaPorCpf(novoServidor.getCpf()) != null){
            throw new IllegalArgumentException("CPF já cadastrado.");
        }

        if(repository.buscaPorEmail(novoServidor.getEmailInstitucional()) != null){
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        repository.salvar(novoServidor);
    }
}