package br.edu.ifpe.sistemaeditais;

import br.edu.ifpe.sistemaeditais.model.*;
import br.edu.ifpe.sistemaeditais.repository.ServidorRepository;
import br.edu.ifpe.sistemaeditais.service.CadastroServidor;
import br.edu.ifpe.sistemaeditais.service.EditalService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class Main {

    private static Servidor usuarioLogado = null;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void main(String[] args) {
        CadastroServidor cadastroServidor = new CadastroServidor();
        ServidorRepository servidorRepository = new ServidorRepository();
        EditalService editalService = new EditalService();
        Scanner scanner = new Scanner(System.in);

        // Criando um Administrador padrão inicial para testes do menu restrito
        Servidor adminPadrao = new Servidor("Administrador Geral", "00000000000", "admin@ifpe.edu.br", "admin123", Campus.RECIFE, AreaFormacao.CIENCIAS_EXATAS_E_DA_TERRA, Titulacao.DOUTORADO);
        adminPadrao.adicionarPerfil(Perfil.ROLE_ADMIN);
        servidorRepository.salvar(adminPadrao);

        int opcao = 0;

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
                }
            }
            System.out.println("6. Sair");
            System.out.print("Escolha uma opção: ");

            try {
                opcao = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Digite apenas números.");
                continue;
            }

            switch (opcao) {
                case 1:
                    cadastrarServidor(scanner, cadastroServidor);
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
                    System.out.println("\nEncerrando o sistema...");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }

        } while (opcao != 6);

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
        if (servidor != null && servidor.getSenha() != null) {
            usuarioLogado = servidor;
            System.out.println("\nAutenticação realizada com sucesso!");
        } else {
            System.out.println("\n[ERRO] Usuário ou senha inválidos.");
        }
    }

    private static void cadastrarEdital(Scanner scanner, EditalService service) {
        System.out.println("\n--- Cadastro de Edital ---");
        System.out.print("Número do Edital (Ex: 01/2026): ");
        String numero = scanner.nextLine();
        System.out.print("Título do Edital: ");
        String titulo = scanner.nextLine();
        
        System.out.print("Ano: ");
        int ano = Integer.parseInt(scanner.nextLine());

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
        System.out.print("Informe o Número do Edital que deseja alterar: ");
        String numeroOriginal = scanner.nextLine();

        System.out.print("Novo Título: ");
        String titulo = scanner.nextLine();
        System.out.print("Novo Ano: ");
        int ano = Integer.parseInt(scanner.nextLine());

        LocalDate iniSub = lerData(scanner, "Nova Data de Início da Submissão (DD/MM/AAAA): ");
        LocalDate fimSub = lerData(scanner, "Nova Data de Fim da Submissão (DD/MM/AAAA): ");
        LocalDate iniAv = lerData(scanner, "Nova Data de Início da Avaliação (DD/MM/AAAA): ");
        LocalDate fimAv = lerData(scanner, "Nova Data de Fim da Avaliação (DD/MM/AAAA): ");

        try {
            Edital atualizado = new Edital(numeroOriginal, titulo, ano, iniSub, fimSub, iniAv, fimAv);
            service.editarEdital(numeroOriginal, atualizado, usuarioLogado);
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
            try {
                return LocalDate.parse(scanner.nextLine().trim(), DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("[ERRO] Formato inválido. Use o padrão DD/MM/AAAA.");
            }
        }
    }

    private static void cadastrarServidor(Scanner scanner, CadastroServidor cadastroServidor) {
        System.out.println("\n--- Formulário de Cadastro ---");

        System.out.print("Nome Completo: ");
        String nome = scanner.nextLine();

        System.out.print("CPF (apenas 11 dígitos numéricos): ");
        String cpf = scanner.nextLine();

        System.out.print("E-mail Institucional: ");
        String email = scanner.nextLine();

        System.out.print("Senha (mínimo 6 caracteres): ");
        String senha = scanner.nextLine();

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

        try {
            cadastroServidor.cadastrar(novoServidor);
            System.out.println("\nServidor cadastrado com sucesso!");
        } catch (IllegalArgumentException e) {
            System.out.println("\nErro de validação: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\nErro inesperado: " + e.getMessage());
        }
    }
}