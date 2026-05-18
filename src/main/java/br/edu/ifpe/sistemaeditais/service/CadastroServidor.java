package br.edu.ifpe.sistemaeditais.service;

import br.edu.ifpe.sistemaeditais.model.Perfil;
import br.edu.ifpe.sistemaeditais.model.Servidor;
import br.edu.ifpe.sistemaeditais.repository.ServidorRepository;
import br.edu.ifpe.sistemaeditais.util.SenhaUtil;

public class CadastroServidor {

    private final ServidorRepository repository = new ServidorRepository();

    public void cadastrar(Servidor novoServidor) {

        String cpf = novoServidor.getCpf();
        if (cpf == null || !cpf.matches("\\d{11}")) {
            throw new IllegalArgumentException("CPF inválido. Informe exatamente 11 dígitos numéricos.");
        }
        if (repository.buscaPorCpf(cpf) != null) {
            throw new IllegalArgumentException("CPF já cadastrado.");
        }

        String email = novoServidor.getEmailInstitucional();
        if (email == null || !email.matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("E-mail institucional inválido.");
        }
        if (repository.buscaPorEmail(email) != null) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        String senhaPlana = novoServidor.getSenha();
        if (senhaPlana == null || senhaPlana.length() < 6) {
            throw new IllegalArgumentException("A senha deve ter no mínimo 6 caracteres.");
        }
        novoServidor.setSenha(SenhaUtil.hash(senhaPlana));

        novoServidor.adicionarPerfil(Perfil.ROLE_COORDENADOR);
        novoServidor.adicionarPerfil(Perfil.ROLE_AVALIADOR);

        repository.salvar(novoServidor);
    }
}