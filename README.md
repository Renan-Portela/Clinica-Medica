# 🏥 ClinicSync - Sistema de Gestão de Clínica Médica

> **Sistema desktop desenvolvido em Java para gerenciamento completo de clínicas médicas**  
> *Projeto Acadêmico - 3º Período ADS | Engenharia de Software & Banco de Dados I*

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Supported-blue.svg)](https://www.docker.com/)
[![Swing](https://img.shields.io/badge/GUI-Java%20Swing-green.svg)](https://docs.oracle.com/javase/tutorial/uiswing/)
[![Linux](https://img.shields.io/badge/Tested%20on-Linux-yellow.svg)](https://www.linux.org/)
[![License](https://img.shields.io/badge/License-Academic-yellow.svg)](#)

---

## 📋 Sumário

- [🎯 Visão Geral](#-visão-geral)
- [✨ Funcionalidades Principais](#-funcionalidades-principais)
- [🏗️ Arquitetura do Sistema](#-arquitetura-do-sistema)
- [💻 Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [🚀 Como Executar](#-como-executar)
- [📊 Modelagem de Dados](#-modelagem-de-dados)
- [🎨 Interface do Usuario](#-interface-do-usuario)
- [📈 Relatórios Disponíveis](#-relatórios-disponíveis)
- [👥 Equipe de Desenvolvimento](#-equipe-de-desenvolvimento)
- [📚 Documentação Técnica](#-documentação-técnica)
- [📖 Referências](#-referências)

---

## 🎯 Visão Geral

O **ClinicSync** é um sistema desktop robusto desenvolvido para otimizar a gestão de clínicas médicas, oferecendo uma solução completa para agendamento de consultas, gerenciamento de pacientes e médicos, além de relatórios gerenciais abrangentes.

### 🌟 Principais Diferenciais

- **Interface Moderna**: Design clean e intuitivo usando componentes Swing customizados
- **Arquitetura Sólida**: Implementação do padrão MVC com camadas bem definidas
- **Validação Robusta**: Verificação de CPF, CRM e outros dados críticos
- **Relatórios Inteligentes**: Sistema completo de business intelligence para clínicas
- **Notificações Automáticas**: Envio de emails de confirmação para pacientes

---

## ✨ Funcionalidades Principais

### 👨‍⚕️ Gerenciamento de Médicos (CRUD)
- **Cadastro Completo**: CRM, nome, especialidade, horários e sala de atendimento
- **Validação de CRM**: Verificação automática do formato do registro médico
- **Gestão de Horários**: Configuração flexível de dias e horários de atendimento

### 👥 Gerenciamento de Pacientes (CRUD)
- **Dados Pessoais**: CPF, nome, data de nascimento, endereço e telefone
- **Histórico Médico**: Acompanhamento completo do histórico de cada paciente
- **Validação de CPF**: Algoritmo completo de validação de documentos

### 📅 Sistema de Agendamento
- **Agenda Visual**: Interface de calendário intuitiva com navegação semanal/mensal
- **Verificação de Disponibilidade**: Sistema automático de horários livres
- **Status de Consultas**: Controle de estados (Agendada, Realizada, Cancelada, Não Compareceu)
- **Cores Inteligentes**: Sistema visual de cores para identificação rápida de status

### 📧 Notificações Automáticas
- **Confirmação de Agendamento**: Emails automáticos para pacientes
- **Configuração SMTP**: Integração com Gmail para envio de notificações

---

## 🏗️ Arquitetura do Sistema

O projeto segue uma **arquitetura em camadas** bem estruturada:

```
📦 br.com.clinica
├── 🎯 model/          # Entidades de domínio
├── 🗄️ dao/            # Camada de acesso a dados
├── ⚙️ service/        # Lógica de negócio
├── 🎨 view/           # Interface gráfica (Swing)
├── 🔧 config/         # Configurações (Banco de dados)
├── 🛠️ util/           # Utilitários e validadores
└── ❌ exception/      # Tratamento de exceções
```

### 🔗 Padrões Implementados

- **MVC (Model-View-Controller)**: Separação clara de responsabilidades
- **DAO (Data Access Object)**: Abstração da camada de dados
- **Service Layer**: Centralização da lógica de negócio
- **Singleton**: Gerenciamento de conexão com banco de dados
- **Factory Method**: Criação de componentes de interface

---

## 💻 Tecnologias Utilizadas

### Core Technologies
- **Java 21**: Linguagem principal com recursos modernos
- **MySQL 8.0**: Sistema de gerenciamento de banco de dados
- **JDBC**: Conectividade com banco de dados
- **Docker**: Containerização do banco de dados MySQL

### Libraries & Frameworks
- **Java Swing**: Framework para interface gráfica desktop
- **JavaMail API**: Envio de emails automatizados
- **MySQL Connector/J**: Driver oficial MySQL para Java

### Development Tools
- **Eclipse IDE**: Ambiente de desenvolvimento integrado
- **WindowBuilder**: Plugin para design visual de interfaces Swing
- **Git**: Controle de versão distribuído

---

## 🚀 Como Executar

### 📋 Pré-requisitos

```bash
☑️ Java JDK 21 ou superior
☑️ Docker e Docker Compose
☑️ MySQL Connector JAR (incluído no projeto)
☑️ IDE Eclipse (recomendado)
☑️ Git para clone do repositório
```

> **📝 Nota**: O sistema foi testado e validado em ambiente **Linux** com MySQL rodando em **Docker**.

### 🔧 Configuração do Ambiente

1. **Clone o repositório**
   ```bash
   git clone https://github.com/Renan-Portela/Clinica-Medica.git
   cd ClinicSync
   ```

2. **Configuração do MySQL com Docker**
   ```bash
   # Executar MySQL 8.0 em container Docker
   docker run --name mysql-clinica \
     -e MYSQL_ROOT_PASSWORD=root \
     -e MYSQL_DATABASE=clinica_medica \
     -p 3306:3306 \
     -d mysql:8.0
   
   # Verificar se o container está rodando
   docker ps
   ```

3. **Dependências Necessárias**
   ```
   📁 lib/
   ├── 📦 mysql-connector-j-9.3.0.jar    # Driver MySQL (OBRIGATÓRIO)
   ├── 📦 javax.mail.jar                  # JavaMail API
   └── 📦 activation-1.1.1.jar            # Java Activation Framework
   ```

4. **Configurar Credenciais**
   ```java
   // Arquivo: src/br/com/clinica/config/DatabaseConnection.java
   private static final String URL = "jdbc:mysql://localhost:3306/clinica_medica";
   private static final String USERNAME = "root";
   private static final String PASSWORD = "root";
   ```

5. **Executar o Sistema**
   ```bash
   # Via Eclipse: Run -> Java Application -> Main.java
   # Via linha de comando:
   javac -cp "lib/*" src/br/com/clinica/Main.java
   java -cp "lib/*:src" br.com.clinica.Main
   ```

### 📊 Script SQL de Criação

```sql
-- Tabela de médicos
CREATE TABLE medicos (
    crm VARCHAR(10) PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    especialidade VARCHAR(50) NOT NULL,
    dias_atendimento TEXT,
    horario_inicio TIME,
    horario_fim TIME,
    sala_atendimento VARCHAR(20)
);

-- Tabela de pacientes
CREATE TABLE pacientes (
    cpf VARCHAR(11) PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    data_nascimento DATE NOT NULL,
    endereco VARCHAR(200),
    telefone VARCHAR(15),
    historico_medico TEXT
);

-- Tabela de consultas
CREATE TABLE consultas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medico_crm VARCHAR(10),
    paciente_cpf VARCHAR(11),
    data_horario DATETIME NOT NULL,
    observacoes TEXT,
    status ENUM('AGENDADA', 'REALIZADA', 'CANCELADA', 'NAO_COMPARECEU') DEFAULT 'AGENDADA',
    FOREIGN KEY (medico_crm) REFERENCES medicos(crm),
    FOREIGN KEY (paciente_cpf) REFERENCES pacientes(cpf)
);
```

---

## 📊 Modelagem de Dados

### 🎯 Diagrama Entidade-Relacionamento

![Diagrama ER - Sistema ClinicSync](/assets/diagrama%20entidade%20relacionamento%20-%20DER.png)

### 🔑 Relacionamentos

- **Médico → Consulta**: Um médico pode ter várias consultas (1:N)
- **Paciente → Consulta**: Um paciente pode ter várias consultas (1:N)
- **Consulta**: Entidade associativa entre Médico e Paciente

---

## 🎨 Interface do Usuario

### 🏠 Tela Principal
- **Design Moderno**: Layout responsivo com grid de botões funcionais
- **Navegação Intuitiva**: Acesso direto a todas as funcionalidades
- **Informações do Sistema**: Header com logo e informações da clínica

![Tela Principal - ClinicSync](/assets/Tela%20Principal%20com%20logo.png)

### 📅 Agenda Visual
- **Calendário Semanal**: Visualização clara da agenda médica
- **Cores por Status**: Sistema visual para identificação rápida
- **Mini-calendário**: Navegação rápida entre datas
- **Filtros Avançados**: Por médico, paciente ou período

![Agenda Visual - ClinicSync](/assets/Tela%20de%20Agenda.png)

### 📋 Formulários CRUD
- **Validação em Tempo Real**: Feedback imediato para o usuário
- **Campos Formatados**: Máscaras para CPF, telefone e CRM
- **Busca Inteligente**: Sistema de filtros e pesquisa avançada

#### Gerenciamento de Médicos
![CRUD Médicos - ClinicSync](/assets/CRUD%20Médicos.png)

#### Gerenciamento de Pacientes
![CRUD Pacientes - ClinicSync](/assets/CRUD%20Pacientes.png)

### 📊 Sistema de Relatórios
- **Interface Unificada**: Todos os relatórios em uma tela única
- **Filtros Dinâmicos**: Adaptação automática conforme o tipo de relatório
- **Exportação**: Possibilidade de exportar dados para análise externa

<!-- Inserir aqui a imagem do sistema de relatórios -->
![Sistema de Relatórios - ClinicSync](/assets/Tela%20Relatórios.png)

---

## 📈 Relatórios Disponíveis

### 📋 Relatórios Operacionais

1. **Consultas por Médico**
   - Filtros: Médico específico, período (mês/ano)
   - Dados: Lista completa de consultas com status e observações

2. **Consultas Canceladas**
   - Filtros: Período específico
   - Análise: Motivos de cancelamento e padrões identificados

3. **Histórico do Paciente**
   - Busca: Por nome ou CPF
   - Visualização: Timeline completo de consultas e observações

### 📊 Relatórios Gerenciais

4. **Pacientes Inativos**
   - Critério: Sem consultas no último ano
   - Uso: Estratégias de reativação e follow-up

5. **Distribuição de Consultas**
   - Análise: Por dia da semana e horário
   - Objetivo: Otimização de recursos e agenda

6. **Análise por Especialidade**
   - Métricas: Volume de atendimentos por área médica
   - Insights: Demanda e planejamento estratégico

---

## 👥 Equipe de Desenvolvimento

### 🎓 **JKLR² Development Team**

| 👨‍💻 Desenvolvedor | 🔗 GitHub | 💼 LinkedIn | 🚀 Especialização |
|-------------------|-----------|-------------|-------------------|
| **José Vitor** | [GitHub](https://github.com/JosVitorFerreiraDosSantosJV) | [LinkedIn](https://www.linkedin.com/in/josé-vitor-ferreira-dos-santos/) | DevOps & Infrastructure |
| **Karolina Zimmerman** | [GitHub](https://github.com/404) | [LinkedIn](https://www.linkedin.com/in/karolina-zimmermann-4491b5287/) | Frontend & UX/UI |
| **Lucas Alves** | [GitHub](https://github.com/Lucas-Alves-Paula) | [LinkedIn](https://www.linkedin.com/in/lucas-alves-a02514178/) | Full-Stack & Architecture |
| **Ryan Alves** | [GitHub](https://github.com/404) | [LinkedIn](https://www.linkedin.com/in/ryanaguilherme/) | Testing & QA |
| **Renan Portela** | [GitHub](https://github.com/Renan-Portela) | [LinkedIn](https://www.linkedin.com/in/portela-renan/) | Backend & Database |

---

## 📚 Documentação Técnica

### 🏛️ Padrões de Arquitetura

- **Clean Architecture**: Separação clara entre camadas de negócio e infraestrutura
- **SOLID Principles**: Código maintível e extensível
- **Design Patterns**: Factory, Singleton, Observer implementados

### 🔧 Convenções de Código

```java
// Exemplo de nomenclatura utilizada
public class ConsultaService {
    private final ConsultaDAO consultaDAO;
    
    public Consulta agendarNovaConsulta(Medico medico, Paciente paciente, 
                                       LocalDateTime dataHorario, String observacoes) {
        // Lógica de negócio centralizada
    }
}
```

### 🎨 Sistema de Cores (UITheme)

```java
interface UITheme {
    Color PRIMARY_BLUE = new Color(52, 144, 220);      // Cor principal
    Color SUCCESS_GREEN = new Color(40, 167, 69);      // Sucessos
    Color ACCENT_RED = new Color(220, 53, 69);         // Alertas
    Color COR_AGENDADA = new Color(52, 144, 220, 200); // Status: Agendada
    Color COR_REALIZADA = new Color(76, 175, 80, 200); // Status: Realizada
}
```

---

## 📖 Referências

### 📚 Bibliografia Acadêmica

- BARNES, David J. **Programação orientada a objetos com Java**. São Paulo: Pearson Prentice Hall, 2009.
- DEITEL, Paul J.; DEITEL, Harvey M. **Java: como programar**. 8. ed. São Paulo: Ed. Pearson Prentice Hall, 2010.
- MENDES, D. R. **Programação Java com ênfase a orientação a objetos**. São Paulo: Novatec, 2009.
- PRESSMAN, Roger S.; MAXIM, Bruce R. **Engenharia de software: uma abordagem profissional**. 8. ed. Porto Alegre: MacGrawHill, 2016.
- SILBERSCHATZ, Abraham; KORTH, Henry F.; SUDARSHAN, S. **Sistema de banco de dados**. 7. ed. Rio de Janeiro: Elsevier, 2020.
- SOMMERVILLE, Ian. **Engenharia de software**. 9. ed. São Paulo: Pearson, 2011.

### 🐳 Configuração Docker + MySQL

Para configuração completa do ambiente Docker com MySQL e conexão via Workbench, consulte:

**📖 [Configurando Docker com MySQL no Linux e conectando ao Workbench](https://dev.to/higorslva/configurando-docker-com-mysql-no-arch-linux-e-conectando-ao-workbench-420l)**

Este artigo contém instruções detalhadas para:
- Instalação e configuração do Docker
- Setup do MySQL 8.0 em container
- Configuração de conexão com MySQL Workbench
- Troubleshooting comum de conectividade

---

### 🏷️ Tags

`java` `swing` `mysql` `docker` `desktop-application` `healthcare` `crud` `mvc-pattern` `academic-project` `clinic-management` `appointment-system` `linux-tested`

---

<div align="center">

### 🏆 **Desenvolvido com 💙 pela Equipe JKLR²**

**Sistema ClinicSync** - *Inovando a gestão médica através da tecnologia*

[![GitHub](https://img.shields.io/badge/GitHub-Repository-black.svg)](https://github.com/Renan-Portela/Clinica-Medica.git)
[![Apresentação](https://img.shields.io/badge/Apresentação-25/06/2024-blue.svg)](#)
[![Faculdade](https://img.shields.io/badge/ADS-3º%20Período-green.svg)](#)

</div>