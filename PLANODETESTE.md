# Documento de Testes – Sistema de Editais

## 1. Introdução

Este documento apresenta os testes realizados no sistema de editais acadêmicos, com o objetivo de validar o correto funcionamento das principais regras de negócio implementadas.

Os testes foram desenvolvidos utilizando o framework **JUnit 5**, garantindo a verificação automatizada das funcionalidades.

---

## 2. Objetivo

Os testes têm como objetivo:

- Verificar o funcionamento dos serviços do sistema
- Validar regras de negócio
- Garantir segurança (controle de acesso)
- Verificar criptografia de senha
- Prevenir falhas em cenários inválidos

---

## 3. Escopo

Foram testados os seguintes componentes:

- Cadastro de servidor
- Criação de projetos
- Criação de editais
- Regras de acesso por perfil
- Criptografia de senha
- Validação de datas

---

## 4. Casos de Teste

---

### 4.1 Cadastro de Servidor

**Classe:** `CadastroServidorTest`

#### Caso 1 – Criptografia de senha

**Descrição:**  
Verifica se a senha informada não é armazenada em texto puro.

**Resultado esperado:**
- Senha criptografada
- Senha diferente da original
- Perfis adicionados automaticamente:
  - `ROLE_COORDENADOR`
  - `ROLE_AVALIADOR`

---

### 4.2 Criação de Projeto

**Classe:** `ProjetoServiceTest`

#### Caso 2 – Criação com dados válidos

**Resultado esperado:**
- Projeto criado sem exceções

---

#### Caso 3 – Servidor nulo

**Resultado esperado:**
- Lançamento de `SecurityException`

---

#### Caso 4 – Servidor sem perfil de coordenador

**Resultado esperado:**
- Lançamento de `SecurityException`

---

### 4.3 Criação de Edital

**Classe:** `EditalServiceTest`

#### Caso 5 – Criação com datas válidas

**Descrição:**  
Verifica se um edital é criado corretamente quando todas as datas estão válidas.

**Resultado esperado:**
- Edital criado sem exceções

---

#### Caso 6 – Datas inválidas

**Descrição:**  
Verifica se o sistema impede criação de edital com datas inconsistentes.

**Exemplo:**
- Data final anterior à inicial

**Resultado esperado:**
- Lançamento de `IllegalArgumentException`

---

#### Caso 7 – Usuário sem permissão

**Descrição:**  
Verifica se administradores podem criar editais.

**Resultado esperado:**
- Lançamento de `SecurityException`

---

#### Caso 8 – Datas nulas

**Descrição:**  
Verifica se o sistema impede criação de edital com datas ausentes.

**Resultado esperado:**
- Lançamento de `IllegalArgumentException`

