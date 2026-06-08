package br.edu.ifpe.sistemaeditais;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

import br.edu.ifpe.sistemaeditais.model.AreaFormacao;
import br.edu.ifpe.sistemaeditais.model.Campus;
import br.edu.ifpe.sistemaeditais.model.Edital;
import br.edu.ifpe.sistemaeditais.model.Perfil;
import br.edu.ifpe.sistemaeditais.model.Servidor;
import br.edu.ifpe.sistemaeditais.model.Sexo;
import br.edu.ifpe.sistemaeditais.model.Titulacao;
import br.edu.ifpe.sistemaeditais.model.AreaTematica;
import br.edu.ifpe.sistemaeditais.model.ODS;
import br.edu.ifpe.sistemaeditais.model.Projeto;
import br.edu.ifpe.sistemaeditais.repository.ServidorRepository;
import br.edu.ifpe.sistemaeditais.repository.ProjetoRepository;
import br.edu.ifpe.sistemaeditais.service.CadastroServidor;
import br.edu.ifpe.sistemaeditais.service.EditalService;
import br.edu.ifpe.sistemaeditais.service.ProjetoService;

public class Main {

    private static Servidor usuarioLogado = null;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void main(String[] args) {
        CadastroServidor cadastroServidor = new CadastroServidor();
        ServidorRepository servidorRepository = new ServidorRepository();
        EditalService editalService = new EditalService();
        ProjetoRepository projetoRepository = new ProjetoRepository();
        ProjetoService projetoService = new ProjetoService(projetoRepository);
        Scanner scanner = new Scanner(System.in);

        // Criando um Administrador padrão inicial para testes do menu restrito
        Servidor adminPadrao = new Servidor("Administrador Geral", "00000000000", "admin@ifpe.edu.br", "admin123", Campus.RECIFE, AreaFormacao.CIENCIAS_EXATAS_E_DA_TERRA, Titulacao.DOUTORADO);
        adminPadrao.adicionarPerfil(Perfil.ROLE_ADMIN);
        servidorRepository.salvar(adminPadrao);

        int opcao = -1;

        // Coordenador padrão para testes de submissão de projetos
        Servidor coordPadrao = new Servidor("Coordenador Teste", "11111111111","coordenador@ifpe.edu.br", "coord123", Campus.RECIFE, AreaFormacao.ENGENHARIAS, Titulacao.MESTRADO);
        coordPadrao.adicionarPerfil(Perfil.ROLE_COORDENADOR);
        servidorRepository.salvar(coordPadrao);

        do {
            System.out.println("\n   SISTEMA DE EDITAIS - IFPE   ");
            System.out.println("================================");
            if (usuarioLogado == null) {
                System.out.println("Status: Não autenticado");
                System.out.println("1. Cadastrar Novo Servidor");
                System.out.println("2. Acessar o Sistema (Login)");
            } else {
                System.out.println("Logado como: " + usuarioLogado.getNomeCompleto() + " (" + usuarioLogado.getEmailInstitucional() + ")");
                System.out.println("1. Cadastrar Novo Servidor");
                System.out.println("2. Fazer Logout");
                
                // Exibição condicional do menu para ROLE_ADMIN
                if (usuarioLogado.getPerfis().contains(Perfil.ROLE_ADMIN)) {
                    System.out.println("--- ÁREA DO ADMINISTRADOR ---");
                    System.out.println("3. Cadastrar Novo Edital");
                    System.out.println("4. Editar Edital Existente");
                    System.out.println("5. Listar Todos os Editais");
                    System.out.println("6. Listar Todos os Projetos");
                }

                if (usuarioLogado.getPerfis().contains(Perfil.ROLE_COORDENADOR)) {
                    System.out.println("--- ÁREA DO COORDENADOR ---");
                    System.out.println("7. Submeter Novo Projeto");
                    System.out.println("8. Editar Projeto (Rascunho / Em Correção)");
                    System.out.println("9. Listar Meus Projetos");
                }
            }
            System.out.println("0. Sair");
            System.out.print("Escolha uma opção: ");

            try {
                String entradaOpcao = scanner.nextLine().trim();
                if (entradaOpcao.isEmpty()) {
                    System.out.println("Nenhuma opção foi digitada.");
                    continue;
                }
                opcao = Integer.parseInt(entradaOpcao);
            } catch (NumberFormatException e) {
                System.out.println("Digite apenas números.");
                continue;
            }

            switch (opcao) {
                case 1:
                    cadastrarServidor(scanner, cadastroServidor, servidorRepository);
                    break;
                case 2:
                    if (usuarioLogado == null) {
                        realizarLogin(scanner, servidorRepository);
                    } else {
                        usuarioLogado = null;
                        System.out.println("\nLogout efetuado com sucesso.");
                    }
                    break;
                case 3:
                    if (validarAdminVisual()) cadastrarEdital(scanner, editalService);
                    break;
                case 4:
                    if (validarAdminVisual()) editarEdital(scanner, editalService);
                    break;
                case 5:
                    if (validarAdminVisual()) listarEditais(editalService);
                    break;
                case 6:
                    if (validarAdminVisual()) listarTodosProjetos(projetoService);
                    break;
                case 7:
                    if (validarCoordenadorVisual()) submeterProjeto(scanner, projetoService);
                    break;
                case 8:
                    if (validarCoordenadorVisual()) editarProjeto(scanner, projetoService, projetoRepository);
                    break;
                case 9:
                    if (validarCoordenadorVisual()) listarMeusProjetos(projetoService);
                    break;
                case 0:
                    System.out.println("\nEncerrando o sistema...");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }

        } while (opcao != 0);

        scanner.close();
    }

    private static boolean validarAdminVisual() {
        if (usuarioLogado == null || !usuarioLogado.getPerfis().contains(Perfil.ROLE_ADMIN)) {
            System.out.println("\n[ERRO] Acesso negado. Opção restrita a administradores.");
            return false;
        }
        return true;
    }

    private static void realizarLogin(Scanner scanner, ServidorRepository repository) {
        System.out.println("\n--- Login ---");
        System.out.print("E-mail Institucional: ");
        String email = scanner.nextLine().trim();
        System.out.print("Senha: ");
        String senha = scanner.nextLine();

        Servidor servidor = repository.buscaPorEmail(email);
        if (servidor != null && servidor.getSenha() != null && servidor.getSenha().equals(senha)) {
            usuarioLogado = servidor;
            System.out.println("\nAutenticação realizada com sucesso!");
        } else {
            System.out.println("\n[ERRO] Usuário ou senha inválidos.");
        }
    }

    private static void cadastrarEdital(Scanner scanner, EditalService service) {
        System.out.println("\n--- Cadastro de Edital ---");
        
        String numero = "";
        while (numero.isEmpty()) {
            System.out.print("Número do Edital (Ex: 01/2026): ");
            numero = scanner.nextLine().trim();
            if (numero.isEmpty()) {
                System.out.println("[ERRO] O número do edital é obrigatório.");
            }
        }

        String titulo = "";
        while (titulo.isEmpty()) {
            System.out.print("Título do Edital: ");
            titulo = scanner.nextLine().trim();
            if (titulo.isEmpty()) {
                System.out.println("[ERRO] O título do edital é obrigatório.");
            }
        }
        
        int ano = 0;
        while (ano == 0) {
            System.out.print("Ano: ");
            try {
                String entradaAno = scanner.nextLine().trim();
                if (entradaAno.isEmpty()) {
                    System.out.println("[ERRO] O ano não pode ser vazio.");
                    continue;
                }
                ano = Integer.parseInt(entradaAno);
            } catch (NumberFormatException e) {
                System.out.println("[ERRO] Digite um ano válido com caracteres numéricos.");
            }
        }

        LocalDate iniSub = lerData(scanner, "Data de Início da Submissão (DD/MM/AAAA): ");
        LocalDate fimSub = lerData(scanner, "Data de Fim da Submissão (DD/MM/AAAA): ");
        LocalDate iniAv = lerData(scanner, "Data de Início da Avaliação (DD/MM/AAAA): ");
        LocalDate fimAv = lerData(scanner, "Data de Fim da Avaliação (DD/MM/AAAA): ");

        try {
            Edital novo = new Edital(numero, titulo, ano, iniSub, fimSub, iniAv, fimAv);
            service.criarEdital(novo, usuarioLogado);
            System.out.println("\nEdital publicado com sucesso!");
        } catch (Exception e) {
            System.out.println("\nErro ao salvar edital: " + e.getMessage());
        }
    }

    private static void editarEdital(Scanner scanner, EditalService service) {
        System.out.println("\n--- Edição de Edital ---");
        
        String numeroOriginal = "";
        while (numeroOriginal.isEmpty()) {
            System.out.print("Informe o Número do Edital que deseja alterar: ");
            numeroOriginal = scanner.nextLine().trim();
            if (numeroOriginal.isEmpty()) {
                System.out.println("[ERRO] Você precisa especificar o número do edital.");
            }
        }

        String titulo = "";
        while (titulo.isEmpty()) {
            System.out.print("Novo Título: ");
            titulo = scanner.nextLine().trim();
            if (titulo.isEmpty()) {
                System.out.println("[ERRO] O título não pode ser vazio.");
            }
        }

        int ano = 0;
        while (ano == 0) {
            System.out.print("Novo Ano: ");
            try {
                String entradaAno = scanner.nextLine().trim();
                if (entradaAno.isEmpty()) {
                    System.out.println("[ERRO] O ano não pode ser vazio.");
                    continue;
                }
                ano = Integer.parseInt(entradaAno);
            } catch (NumberFormatException e) {
                System.out.println("[ERRO] Digite um ano válido.");
            }
        }

        LocalDate iniSub = lerData(scanner, "Nova Data de Início da Submissão (DD/MM/AAAA): ");
        LocalDate fimSub = lerData(scanner, "Nova Data de Fim da Submissão (DD/MM/AAAA): ");
        LocalDate iniAv = lerData(scanner, "Nova Data de Início da Avaliação (DD/MM/AAAA): ");
        LocalDate fimAv = lerData(scanner, "Nova Data de Fim da Avaliação (DD/MM/AAAA): ");

        try {
            Edital updated = new Edital(numeroOriginal, titulo, ano, iniSub, fimSub, iniAv, fimAv);
            service.editarEdital(numeroOriginal, updated, usuarioLogado);
            System.out.println("\nEdital atualizado com sucesso!");
        } catch (Exception e) {
            System.out.println("\nErro ao editar edital: " + e.getMessage());
        }
    }

    private static void listarEditais(EditalService service) {
        System.out.println("\n--- Listagem Administrativa de Editais ---");
        try {
            var lista = service.listarEditais(usuarioLogado);
            if (lista.isEmpty()) {
                System.out.println("Nenhum edital cadastrado até o momento.");
                return;
            }
            for (Edital e : lista) {
                System.out.printf("Edital Nº: %s | %s (%d)%n", e.getNumero(), e.getTitulo(), e.getAno());
                System.out.printf("  > Submissões: %s até %s%n", e.getDataInicioSubmissao().format(DATE_FORMATTER), e.getDataFimSubmissao().format(DATE_FORMATTER));
                System.out.printf("  > Avaliações: %s até %s%n%n", e.getDataInicioAvaliacao().format(DATE_FORMATTER), e.getDataFimAvaliacao().format(DATE_FORMATTER));
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static LocalDate lerData(Scanner scanner, String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String entrada = scanner.nextLine().trim();
            if (entrada.isEmpty()) {
                System.out.println("[ERRO] Este campo de data é obrigatório e não pode ser vazio.");
                continue;
            }
            try {
                return LocalDate.parse(entrada, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("[ERRO] Formato inválido. Use o padrão DD/MM/AAAA.");
            }
        }
    }

    private static void cadastrarServidor(Scanner scanner, CadastroServidor cadastroServidor, ServidorRepository repository) {
        System.out.println("\n--- Formulário de Cadastro ---");

        System.out.print("Nome Completo: ");
        String nome = scanner.nextLine();

        String cpf = null;
        while (cpf == null) {
            System.out.print("CPF (apenas 11 dígitos numéricos): ");
            String entrada = scanner.nextLine().trim();
            try {
                if (!entrada.matches("\\d{11}"))
                    throw new IllegalArgumentException("CPF inválido. Informe exatamente 11 dígitos numéricos.");
                if (repository.buscaPorCpf(entrada) != null)
                    throw new IllegalArgumentException("CPF já cadastrado.");
                cpf = entrada;
            } catch (IllegalArgumentException e) {
                System.out.println("\n[ERRO] " + e.getMessage());
            }
        }

        String email = null;
        while (email == null) {
            System.out.print("E-mail Institucional: ");
            String entrada = scanner.nextLine().trim();
            try {
                if (!entrada.matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))
                    throw new IllegalArgumentException("E-mail institucional inválido.");
                if (repository.buscaPorEmail(entrada) != null) 
                throw new IllegalArgumentException("E-mail já cadastrado.");
                email = entrada;
            } catch (IllegalArgumentException e) {
                System.out.println("\n[ERRO] " + e.getMessage());
            }
        }

        String senha = null;
        while (senha == null) {
            System.out.print("Senha (mínimo 6 caracteres): ");
            String entrada = scanner.nextLine();
            try {
                if (entrada.length() < 6)
                    throw new IllegalArgumentException("A senha deve ter no mínimo 6 caracteres.");
                senha = entrada;
            } catch (IllegalArgumentException e) {
                System.out.println("\n[ERRO] " + e.getMessage());
            }
        }

        Campus campus = null;
        while (campus == null) {
            System.out.println("\nCampi disponíveis:");
            for (Campus c : Campus.values()) {
                System.out.println("   - " + c.name());
            }
            System.out.print("Digite o Campus exatamente como listado: ");
            try {
                campus = Campus.valueOf(scanner.nextLine().toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                System.out.println("\n[ERRO] Campus inválido! Digite exatamente uma das opções da lista.");
            }
        }

        AreaFormacao areaFormacao = null;
        while (areaFormacao == null) {
            System.out.println("\nÁreas de Formação disponíveis:");
            for (AreaFormacao af : AreaFormacao.values()) {
                System.out.println("   - " + af.name());
            }
            System.out.print("Digite a Área de Formação exatamente como listado: ");
            try {
                areaFormacao = AreaFormacao.valueOf(scanner.nextLine().toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                System.out.println("\n[ERRO] Área de Formação inválida! Digite exatamente uma das opções da lista.");
            }
        }

        Titulacao titulacao = null;
        while (titulacao == null) {
            System.out.println("\nTitulações disponíveis:");
            for (Titulacao t : Titulacao.values()) {
                System.out.println("   - " + t.name());
            }
            System.out.print("Digite a titulação exatamente como listado: ");
            try {
                titulacao = Titulacao.valueOf(scanner.nextLine().toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                System.out.println("\n[ERRO] Titulação inválida! Digite exatamente uma das opções da lista.");
            }
        }
        
        Sexo sexo = null;
        while (sexo == null) {
            System.out.println("\nSexo:");
            for (Sexo s : Sexo.values()) {
                System.out.println("   - " + s.name());
            }
            System.out.print("Digite o sexo exatamente como listado: ");
            try {
                sexo = Sexo.valueOf(scanner.nextLine().toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                System.out.println("\n[ERRO] Opção de sexo inválida! Digite exatamente uma das opções da lista.");
            }
        }

        System.out.print("Nome Social (opcional — pressione Enter para pular): ");
        String nomeSocial = scanner.nextLine();

        System.out.print("Link Lattes (opcional — pressione Enter para pular): ");
        String linkLattes = scanner.nextLine();

        System.out.print("Telefone (opcional — apenas números, pressione Enter para pular): ");
        String telefone = scanner.nextLine();

        Servidor novoServidor = new Servidor(nome, cpf, email, senha, campus, areaFormacao, titulacao);

        if (sexo != null){
            novoServidor.setSexo(sexo);
        }

        if (!nomeSocial.trim().isEmpty()){
            novoServidor.setNomeSocial(nomeSocial);
        }

        if (!linkLattes.trim().isEmpty()){
            novoServidor.setLinkLattes(linkLattes);
        }

        if (!telefone.trim().isEmpty()){
            novoServidor.setTelefone(telefone);
        }

       
        cadastroServidor.cadastrar(novoServidor);
        System.out.println("\nServidor cadastrado com sucesso!");
    }

    private static boolean validarCoordenadorVisual() {
        if (usuarioLogado == null
                || (!usuarioLogado.getPerfis().contains(Perfil.ROLE_COORDENADOR)
                        && !usuarioLogado.getPerfis().contains(Perfil.ROLE_ADMIN))) {
            System.out.println("\n[ERRO] Acesso negado. Opção restrita a coordenadores.");
            return false;
        }
        return true;
    }

    private static void submeterProjeto(Scanner scanner, ProjetoService service) {
        System.out.println("\n--- Submissão de Novo Projeto ---");

        String titulo   = lerCampoObrigatorio(scanner, "Título do Projeto: ");
        String resumo   = lerCampoObrigatorio(scanner, "Resumo: ");
        String palavras = lerCampoObrigatorio(scanner, "Palavras-chave (separe por vírgula): ");
        String publico  = lerCampoObrigatorio(scanner, "Público-alvo: ");
        AreaTematica area = lerAreaTematica(scanner);
        Campus campus     = lerCampusProjeto(scanner);
        List<ODS> ods     = lerODS(scanner);
        boolean aceite    = lerAceiteTermoCompromisso(scanner);

        try {
            Projeto projeto = service.criarProjeto(titulo, resumo, palavras, publico,
                    area, campus, ods, aceite, usuarioLogado);
            System.out.println("\nProjeto submetido com sucesso! Status: " + projeto.getStatus());
        } catch (Exception e) {
            System.out.println("\n[ERRO] " + e.getMessage());
        }
    }

    private static void editarProjeto(Scanner scanner, ProjetoService service, ProjetoRepository repository) {
        System.out.println("\n--- Edição de Projeto ---");

        listarMeusProjetos(service);

        String tituloBusca = lerCampoObrigatorio(scanner, "\nDigite o título exato do projeto que deseja editar: ");
        Projeto projeto = repository.buscarPorTitulo(tituloBusca);

        if (projeto == null) {
            System.out.println("[ERRO] Projeto não encontrado.");
            return;
        }

        System.out.println("Status atual: " + projeto.getStatus());

        String titulo   = lerCampoObrigatorio(scanner, "Novo Título: ");
        String resumo   = lerCampoObrigatorio(scanner, "Novo Resumo: ");
        String palavras = lerCampoObrigatorio(scanner, "Novas Palavras-chave: ");
        String publico  = lerCampoObrigatorio(scanner, "Novo Público-alvo: ");
        AreaTematica area = lerAreaTematica(scanner);
        Campus campus     = lerCampusProjeto(scanner);
        List<ODS> ods     = lerODS(scanner);
        boolean aceite    = lerAceiteTermoCompromisso(scanner);

        try {
            service.editarProjeto(projeto, titulo, resumo, palavras, publico,
                    area, campus, ods, aceite, usuarioLogado);
            System.out.println("\nProjeto atualizado com sucesso!");
        } catch (IllegalStateException e) {
            System.out.println("\n[ERRO] " + e.getMessage());
        } catch (SecurityException e) {
            System.out.println("\n[ACESSO NEGADO] " + e.getMessage());
        }
    }

    private static void listarMeusProjetos(ProjetoService service) {
        System.out.println("\n--- Meus Projetos ---");
        try {
            List<Projeto> projetos = service.listarProjetosDoCoordenador(usuarioLogado);
            if (projetos.isEmpty()) {
                System.out.println("Nenhum projeto encontrado.");
                return;
            }
            for (Projeto p : projetos) {
                System.out.printf("Título: %s | Status: %s | Área: %s | Campus: %s%n",
                        p.getTitulo(), p.getStatus(), p.getAreaTematica(), p.getCampus());
                System.out.printf("  > Palavras-chave: %s%n", p.getPalavrasChave());
                System.out.printf("  > Público-alvo: %s%n", p.getPublicoAlvo());
                System.out.printf("  > Aceite do Termo: %s%n%n",
                        p.isAceitouTermoDeCompromisso() ? "Sim" : "Não");
            }
        } catch (Exception e) {
            System.out.println("[ERRO] " + e.getMessage());
        }
    }

    private static void listarTodosProjetos(ProjetoService service) {
        System.out.println("\n--- Listagem Geral de Projetos ---");
        try {
            List<Projeto> projetos = service.listarTodos(usuarioLogado);
            if (projetos.isEmpty()) {
                System.out.println("Nenhum projeto cadastrado.");
                return;
            }
            for (Projeto p : projetos) {
                System.out.printf("Título: %s | Status: %s | Coordenador: %s | Campus: %s%n",
                        p.getTitulo(), p.getStatus(),
                        p.getCoordenador().getNomeCompleto(), p.getCampus());
            }
        } catch (Exception e) {
            System.out.println("[ERRO] " + e.getMessage());
        }
    }

    private static String lerCampoObrigatorio(Scanner scanner, String mensagem) {
        String valor = "";
        while (valor.isEmpty()) {
            System.out.print(mensagem);
            valor = scanner.nextLine().trim();
            if (valor.isEmpty()) System.out.println("[ERRO] Este campo é obrigatório.");
        }
        return valor;
    }

    private static AreaTematica lerAreaTematica(Scanner scanner) {
        AreaTematica area = null;
        while (area == null) {
            System.out.println("\nÁreas Temáticas disponíveis:");
            for (AreaTematica at : AreaTematica.values()) {
                System.out.println("  - " + at.name());
            }
            System.out.print("Digite a Área Temática exatamente como listado: ");
            try {
                area = AreaTematica.valueOf(scanner.nextLine().toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                System.out.println("[ERRO] Área Temática inválida.");
            }
        }
        return area;
    }

    private static Campus lerCampusProjeto(Scanner scanner) {
        Campus campus = null;
        while (campus == null) {
            System.out.println("\nCampi disponíveis:");
            for (Campus c : Campus.values()) {
                System.out.println("  - " + c.name());
            }
            System.out.print("Digite o Campus: ");
            try {
                campus = Campus.valueOf(scanner.nextLine().toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                System.out.println("[ERRO] Campus inválido.");
            }
        }
        return campus;
    }

    private static List<ODS> lerODS(Scanner scanner) {
        List<ODS> selecionados = new ArrayList<>();
        System.out.println("\nODS disponíveis:");
        ODS[] todos = ODS.values();
        for (int i = 0; i < todos.length; i++) {
            System.out.printf("  %d. %s%n", i + 1, todos[i].getDescricao());
        }
        System.out.println("Digite os números dos ODS separados por vírgula (ex: 1,4,8) ou 0 para nenhum:");
        System.out.print("> ");
        String entrada = scanner.nextLine().trim();
        if (!entrada.equals("0") && !entrada.isEmpty()) {
            for (String parte : entrada.split(",")) {
                try {
                    int idx = Integer.parseInt(parte.trim()) - 1;
                    if (idx >= 0 && idx < todos.length) selecionados.add(todos[idx]);
                    else System.out.println("[AVISO] Número fora do intervalo ignorado: " + (idx + 1));
                } catch (NumberFormatException e) {
                    System.out.println("[AVISO] Entrada ignorada: " + parte.trim());
                }
            }
        }
        return selecionados;
    }

    private static boolean lerAceiteTermoCompromisso(Scanner scanner) {
        while (true) {
            System.out.print("\nAceite o Termo de Compromisso? (S/N): ");
            String resp = scanner.nextLine().trim().toUpperCase();
            if (resp.equals("S")) return true;
            if (resp.equals("N")) return false;
            System.out.println("[ERRO] Digite S para Sim ou N para Não.");
        }
    }
}