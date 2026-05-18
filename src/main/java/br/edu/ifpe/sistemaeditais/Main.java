package br.edu.ifpe.sistemaeditais;

import br.edu.ifpe.sistemaeditais.model.*;
import br.edu.ifpe.sistemaeditais.service.CadastroServidor;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        CadastroServidor cadastroServidor = new CadastroServidor();
        Scanner scanner = new Scanner(System.in);
        int opcao = 0;

        do {
            System.out.println("\n   SISTEMA DE EDITAIS - IFPE   ");
            System.out.println("================================");
            System.out.println("1. Cadastrar Novo Servidor");
            System.out.println("2. Sair");
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
                    System.out.println("\nEncerrando o sistema...");
                    break;

                default:
                    System.out.println("Opção inválida.");
            }

        } while (opcao != 2);

        scanner.close();
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
            System.out.println("Perfis atribuídos: ROLE_COORDENADOR, ROLE_AVALIADOR");
            System.out.println("Redirecionando para a tela de login...");
            System.out.println("Utilize seu e-mail e senha para acessar o sistema.\n");

        } catch (IllegalArgumentException e) {
            System.out.println("\nErro de validação: " + e.getMessage());
            System.out.println("Os dados informados foram mantidos. Corrija o campo indicado e tente novamente.\n");
        } catch (Exception e) {
            System.out.println("\nErro inesperado: " + e.getMessage());
        }
    }
}