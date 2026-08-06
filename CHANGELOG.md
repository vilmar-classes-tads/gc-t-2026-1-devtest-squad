# Changelog

Todos os marcos importantes, correções de bugs e novas funcionalidades deste projeto serão documentados neste arquivo.

## [0.1.0] - 2026-06-01

### Adicionado

- **Issue 0 (Cadastro de Servidores):** Criação das classes base do sistema, incluindo a entidade `Servidor` e os Enums de suporte (`Campus`, `AreaFormacao`, `Titulacao`, `Sexo`, `Perfil`).
- **Persistência Inicial:** Implementação do `ServidorRepository` simulando armazenamento em memória com coleções estáticas.
- **Mecanismo de Segurança:** Integração da classe utilitária `SenhaUtil` para aplicação de algoritmo de hash SHA-256 em senhas planas.
- **Regras de Atribuição Padrão:** Vinculação automática dos perfis `ROLE_COORDENADOR` e `ROLE_AVALIADOR` para novas contas de servidores.
- **Critérios de Unicidade:** Validação impeditiva para duplicação de registros com o mesmo CPF ou mesmo E-mail institucional através de expressões regulares (Regex).
- **Interface de Terminal:** Estruturação da `Main.java` com laço de controle operacional e formulário interativo de inputs.

- **Issue 1 (Gestão de Editais):** Implementação do modelo de domínio `Edital` contendo título, número, ano e períodos de submissão/avaliação.
- **Camada de Persistência:** Criação do `EditalRepository` para controle e isolamento do armazenamento de dados em memória (`List` estática).
- **Camada de Regras de Negócio:** Criação do `EditalService` centralizando a lógica de cadastro, listagem, edição e validação de consistência cronológica.
- **Segurança Adaptativa:** Implementação de barreira de controle de acesso por perfil (`ROLE_ADMIN`) na camada de serviço para operações de editais.
- **Fluxo de Autenticação:** Adicionado sistema simulado de Login na interface de console (`Main.java`) para capturar o usuário logado e suas respectivas permissões.

### Alterado
- **Menu Dinâmico:** Reestruturação do menu principal para exibir opções administrativas (CRUD de Editais) condicionalmente apenas se o usuário possuir a permissão `ROLE_ADMIN`.
- **Experiência do Usuário (UX):** Alteração da opção fixa do menu de encerramento de `6. Sair` para `0. Sair` (conforme sugestão de Code Review), evitando saltos numéricos quebrados para usuários comuns.

### Corrigido
- **Tratamento de Exceções em Enums:** Adicionados blocos `try-catch` e laços de repetição individuais na leitura de propriedades Enum (`Campus`, `AreaFormacao`, `Titulacao`, `Sexo`), impedindo a interrupção abrupta do sistema por entradas inválidas.
- **Validação de Inputs Vazios:** Correção do bug de quebra por Strings vazias (`""`) ou pressionamento acidental de Enter nos campos de texto e numéricos do formulário de Editais e no método geral de leitura de datas.
- **Arquitetura contra Efeitos Colaterais:** Ajuste na ordem de execução de `CadastroServidor.java` para garantir que as validações de dados e unicidade de chaves ocorram estritamente antes de qualquer modificação de estado no objeto (como o hash da senha).
