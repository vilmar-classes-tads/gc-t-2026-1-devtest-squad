package br.edu.ifpe.sistemaeditais.service;

import br.edu.ifpe.sistemaeditais.model.Perfil;
import br.edu.ifpe.sistemaeditais.model.Servidor;
import br.edu.ifpe.sistemaeditais.repository.ServidorRepository;
import br.edu.ifpe.sistemaeditais.util.SenhaUtil;

public class CadastroServidor {

    private final ServidorRepository repository = new ServidorRepository();

    public void cadastrar(Servidor novoServidor) {

        String senhaPlana = novoServidor.getSenha();
        novoServidor.setSenha(SenhaUtil.hash(senhaPlana));

        novoServidor.adicionarPerfil(Perfil.ROLE_COORDENADOR);
        novoServidor.adicionarPerfil(Perfil.ROLE_AVALIADOR);

        repository.salvar(novoServidor);
    }
}