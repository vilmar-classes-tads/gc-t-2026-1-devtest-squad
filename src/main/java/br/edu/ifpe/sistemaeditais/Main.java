package br.edu.ifpe.sistemaeditais;

import br.edu.ifpe.sistemaeditais.model.*;
import br.edu.ifpe.sistemaeditais.service.CadastroServidor;
import java.util.Scanner;

public class Main{
    public static void main(String[] args){

        CadastroServidor cadastroServidor = new CadastroServidor();

        Scanner scanner = new Scanner(System.in);
        int opcao = 0;

        do{
            System.out.println("   SISTEMAS DE EDITAIS -IFPE   ");
            System.out.println("================================");
            System.out.println("1. Cadastrar Novo Servidor");
            System.out.println("2. Sair");
            System.out.print("Escolha uma opção: ");


            try{
                opcao = Integer.parseInt(scanner.nextLine());
            }catch(NumberFormatException e){
                System.out.println("Digite apenas números.");
                continue;
            }

            switch(opcao){
                case 1:
                    try{
                        System.out.println("\n---Formulário de Cadastro---");

                        System.out.println("Nome Completo: ");
                        String nome = scanner.nextLine();

                        System.out.println("CPF (apenas números): ");
                        String cpf = scanner.nextLine();

                        System.out.println("E-mail Institucional: ");
                        String email = scanner.nextLine();

                        System.out.println("Senha: ");
                        String senha = scanner.nextLine();

                        System.out.println("\nCampi disponíveis: ");
                        for(Campus c : Campus.values()){
                            System.out.print("- " + c.name());
                        }
                        System.out.print("Digite o nome do Campus exatamente como listado: ");
                        Campus campus = Campus.valueOf(scanner.nextLine().toUpperCase().trim());
                        
                        System.out.println("\nÁreas de Formação disponíveis: ");
                        for(AreaFormacao af : AreaFormacao.values()){
                            System.out.print("- " + af.name());
                        }
                        System.out.print("Digite a área de formação exatamente como listado: ");
                        AreaFormacao areaFormacao = AreaFormacao.valueOf(scanner.nextLine().toUpperCase().trim());

                        System.out.println("\nTitulação disponíveis: ");
                        for(Titulacao t : Titulacao.values()){
                            System.out.print("- " + t.name());
                        }
                        System.out.print("Digite a titulação exatamente como listado: ");
                        Titulacao titulacao = Titulacao.valueOf(scanner.nextLine().toUpperCase().trim());

                        System.out.println("\nSexo: ");
                        for(Sexo s : Sexo.values()){
                            System.out.print("- " + s.name());
                        }
                        System.out.print("Digite o sexo exatamente como listado: ");
                        Sexo sexo = Sexo.valueOf(scanner.nextLine().toUpperCase().trim());

                        System.out.println("Nome Social: ");
                        String nomeSocial = scanner.nextLine();

                        System.out.println("Link Lattes: ");
                        String linkLattes = scanner.nextLine();

                        System.out.println("Telefone (apenas números): ");
                        String telefone = scanner.nextLine();


                        Servidor novoServidor = new Servidor(
                            nome, cpf, email, senha, campus, areaFormacao, titulacao
                        );

                        if(sexo != null){
                            novoServidor.setSexo(sexo);
                        }
                        
                        if(nomeSocial != null && !nomeSocial.trim().isEmpty()){
                            novoServidor.setNomeSocial(nomeSocial);
                        }

                        if(linkLattes != null && !linkLattes.trim().isEmpty()){
                            novoServidor.setLinkLattes(linkLattes);
                        }

                        if(telefone != null && !telefone.trim().isEmpty()){
                            novoServidor.setTelefone(telefone);
                        }


                        cadastroServidor.cadastrar(novoServidor);
                        System.out.println("Servidor cadastrado com sucesso");

                    }catch(IllegalArgumentException e){
                        System.out.println("Erro de validação: " + e.getMessage());
                    }catch(Exception e){
                        System.out.println("Ocorreu um erro: " + e.getMessage());
                    }
                    break;
                
                case 2:
                    System.out.println("\nEncerrando o sistema...");
                    break;
            }
        }while (opcao != 2);

        scanner.close();

    }
}
