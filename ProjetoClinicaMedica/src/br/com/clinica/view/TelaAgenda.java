package br.com.clinica.view;

import br.com.clinica.dao.ConsultaDAO;
import br.com.clinica.model.Consulta;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional; 
import java.awt.event.ActionListener; // Importacao adicionada para ActionListener

/**
 * Tela de gestao da agenda de consultas com interface moderna.
 * Sistema completo para visualizacao e edicao de consultas medicas.
 * Layout otimizado com painel lateral para observacoes detalhadas.
 * Interage com ConsultaDAO.
 *
 * Estrutura da interface:
 * - Header: Contem o titulo principal da secao e um botao para voltar ao menu principal.
 * - Area Central: Divisao inteligente com:
 * - Tabela Principal: Lista completa de consultas.
 * - Painel Observacoes: Editor lateral para notas.
 * - Rodapec: Botoes de acao para gestao das consultas e navegacao.
 *
 * Funcionalidades principais:
 * - Visualizacao completa de todas as consultas.
 * - Edicao de observacoes em tempo real.
 * - Mudanca de status (Agendada -> Realizada/Cancelada/Nao Compareceu).
 * - Validacoes de negocio para alteracoes de status.
 * - Confirmacoes antes de acoes criticas.
 *
 * Sistema de observacoes:
 * - Selecao de consulta carrega observacoes automaticamente.
 * - Editor habilitado apenas com consulta selecionada.
 * - Salvamento direto no banco de dados.
 * - Atualizacao automatica da tabela apos modificacoes.
 *
 * Controles de status:
 * - Cancelamento: Apenas consultas "Agendadas".
 * - Realizacao: Apenas consultas "Agendadas" + captura de observacoes.
 * - Nao Compareceu: Apenas consultas "Agendadas" + captura de observacoes.
 * - Validacoes automaticas de regras de negocio.
 */
public class TelaAgenda extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    // Paleta de cores medica profissional
    private static final Color PRIMARY_BLUE = new Color(52, 144, 220);
    private static final Color MEDICAL_GREEN = new Color(76, 175, 80);
    private static final Color CLEAN_WHITE = new Color(255, 255, 255);
    private static final Color LIGHT_GRAY = new Color(248, 249, 250);
    private static final Color DARK_TEXT = new Color(52, 58, 64);
    private static final Color ACCENT_RED = new Color(220, 53, 69);
    private static final Color SUCCESS_GREEN = new Color(40, 167, 69);
    
    // Componentes principais da interface
    private JPanel contentPane;
    private JPanel headerPanel;
    private JPanel centralPanel;
    private JPanel observacoesPanel;
    private JPanel botoesPanel;
    
    // Componentes da tabela de consultas
    private JTable table;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPaneTabela;
    
    // Componentes do painel de observacoes
    private JTextArea txtObservacoes;
    private JButton btnSalvarObs;
    private JLabel lblInfoObservacoes;
    
    // Botoes de acao principal e navegacao
    private JButton btnAtualizar;
    private JButton btnCancelar;
    private JButton btnRealizar;
    private JButton btnFechar; // Botao para fechar a tela e voltar ao menu principal
    private JButton btnNovoAgendamento; // Novo botao de navegacao
    private JButton btnVerCalendario;   // Novo botao de navegacao
    private JButton btnVoltarMenuPrincipalHeader; // Botao de navegacao no header
    
    // Dados e controle
    private ConsultaDAO consultaDAO;
    private Consulta consultaSelecionada;
    
    /**
     * Construtor principal da TelaAgenda.
     * Inicializa o DAO para interacao com o banco de dados.
     * Configura a interface visual da tela e carrega todas as consultas disponiveis.
     *
     * Comunicacao com Outras Classes:
     * - ConsultaDAO: Para buscar todas as consultas.
     */
    public TelaAgenda() {
        this.consultaDAO = new ConsultaDAO();
        this.consultaSelecionada = null;
        
        inicializarInterface();
        carregarConsultas();
    }

    /**
     * Construtor da TelaAgenda para inicializar a tela com uma consulta especifica selecionada.
     * Este construtor e utilizado quando se deseja abrir a TelaAgenda ja focada em uma consulta.
     * @param consultaId O ID da consulta a ser carregada e selecionada na tabela.
     *
     * Comunicacao com Outras Classes:
     * - ConsultaDAO: Para buscar a consulta pelo ID (indiretamente, atraves de findAll).
     */
    public TelaAgenda(Long consultaId) {
        this(); // Chama o construtor padrao para inicializar a interface e carregar todas as consultas
        
        // Tenta selecionar a consulta na tabela apos ela ser carregada
        SwingUtilities.invokeLater(() -> {
            try {
                // Percorre o modelo da tabela para encontrar o ID da consulta
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if (tableModel.getValueAt(i, 0).equals(consultaId)) {
                        table.setRowSelectionInterval(i, i); // Seleciona a linha
                        carregarConsultaSelecionada(); // Carrega os detalhes da consulta selecionada
                        break;
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Erro ao carregar consulta especifica: " + e.getMessage(),
                    "Erro de Sistema", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    /**
     * Configura a interface principal da tela com um layout moderno e otimizado.
     * A estrutura e dividida em Header, Area Central (tabela e observacoes) e Rodapec (botoes).
     */
    private void inicializarInterface() {
        configurarJanela();
        criarComponentesPrincipais();
        organizarLayout();
        aplicarEstilosVisuais();
    }
    
    /**
     * Configura as propriedades basicas da janela (JFrame) da TelaAgenda.
     * Define o titulo, estado de tela cheia, posicao inicial e operacao de fechamento.
     */
    private void configurarJanela() {
        setTitle("Agenda de Consultas - Sistema Clinica Medica");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximiza a janela
        setLocationRelativeTo(null); // Centraliza a janela na tela
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Fecha apenas esta janela
        
        contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(LIGHT_GRAY);
        setContentPane(contentPane);
    }
    
    /**
     * Cria e inicializa todos os principais componentes visuais da interface da TelaAgenda.
     * Inclui o painel de cabecalho, a tabela de consultas, o painel de observacoes e o painel de botoes de acao.
     */
    private void criarComponentesPrincipais() {
        criarHeader();
        criarTabelaConsultas();
        criarPainelObservacoes();
        criarBotoesAcao();
    }
    
    /**
     * Cria o painel de cabecalho da tela.
     * Exibe o titulo principal da secao e um botao para retornar ao menu principal.
     *
     * Comunicacao com Outras Classes:
     * - TelaPrincipal: A tela para a qual o usuario retornara.
     */
    private void criarHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_BLUE);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);
        
        JLabel lblTitulo = new JLabel("AGENDA DE CONSULTAS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(CLEAN_WHITE);
        
        JLabel lblSubtitulo = new JLabel("Gestao completa de consultas medicas e observacoes clinicas");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblSubtitulo.setForeground(new Color(255, 255, 255, 200));
        
        headerLeft.add(lblTitulo);
        headerLeft.add(Box.createVerticalStrut(5));
        headerLeft.add(lblSubtitulo);
        
        // Botao para voltar ao menu principal no header
        btnVoltarMenuPrincipalHeader = criarBotaoHeaderNav("Voltar ao Menu", CLEAN_WHITE, DARK_TEXT, e -> {
            dispose();
            trazerTelaPrincipalParaFrente();
        });

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        headerRight.setOpaque(false);
        headerRight.add(btnVoltarMenuPrincipalHeader);
        
        headerPanel.add(headerLeft, BorderLayout.WEST);
        headerPanel.add(headerRight, BorderLayout.EAST);
    }

    /**
     * Cria um botao padronizado para uso no cabecalho para navegacao.
     * @param texto O texto a ser exibido no botao.
     * @param corTexto A cor do texto do botao.
     * @param corFundo A cor de fundo padrao do botao.
     * @param acao O ActionListener a ser executado quando o botao e clicado.
     * @return Um JButton configurado com o estilo definido.
     */
    private JButton criarBotaoHeaderNav(String texto, Color corTexto, Color corFundo, ActionListener acao) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 14));
        botao.setForeground(corTexto);
        botao.setBackground(corFundo);
        botao.setPreferredSize(new Dimension(160, 35)); // Tamanho padrao para botoes de header
        botao.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        botao.setFocusPainted(false);
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botao.addActionListener(acao);
        
        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                botao.setBackground(corFundo.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                botao.setBackground(corFundo);
            }
        });
        return botao;
    }
    
    /**
     * Cria e configura a tabela de consultas.
     * Define o modelo da tabela, suas colunas, estilos de fonte e linha,
     * e um listener para carregar observacoes quando uma consulta e selecionada.
     */
    private void criarTabelaConsultas() {
        String[] colunas = {"ID", "Data/Hora", "Medico", "Paciente", "Status", "Observacoes"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabela somente leitura
            }
        };
        
        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Permite apenas uma selecao por vez
        table.setSelectionBackground(new Color(52, 144, 220, 30)); // Cor de selecao sutil
        table.setSelectionForeground(DARK_TEXT);
        table.setGridColor(new Color(220, 220, 220)); // Cor das linhas da grade
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        table.getTableHeader().setBackground(LIGHT_GRAY);
        table.getTableHeader().setForeground(DARK_TEXT);
        table.getTableHeader().setPreferredSize(new Dimension(0, 35));
        
        // Listener para carregar observacoes quando uma consulta e selecionada na tabela
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { // Evita multiplos eventos durante o ajuste da selecao
                carregarConsultaSelecionada();
            }
        });
        
        scrollPaneTabela = new JScrollPane(table);
        scrollPaneTabela.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_BLUE, 2),
                "Lista de Consultas Agendadas",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 18),
                PRIMARY_BLUE
            ),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        scrollPaneTabela.setBackground(CLEAN_WHITE);
    }
    
    /**
     * Cria e configura o painel lateral para edicao de observacoes de consulta.
     * Contem uma area de texto para observacoes e um botao para salva-las.
     * O editor e habilitado dinamicamente com base na selecao de uma consulta na tabela.
     */
    private void criarPainelObservacoes() {
        observacoesPanel = new JPanel(new BorderLayout());
        observacoesPanel.setBackground(CLEAN_WHITE);
        observacoesPanel.setPreferredSize(new Dimension(400, 0)); // Largura fixa para o painel de observacoes
        observacoesPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(23, 162, 184), 2),
                "Observacoes da Consulta",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 18),
                new Color(23, 162, 184)
            ),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        lblInfoObservacoes = new JLabel("Selecione uma consulta para editar observacoes");
        lblInfoObservacoes.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        lblInfoObservacoes.setForeground(new Color(108, 117, 125));
        lblInfoObservacoes.setHorizontalAlignment(SwingConstants.CENTER);
        lblInfoObservacoes.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        txtObservacoes = new JTextArea();
        txtObservacoes.setLineWrap(true);
        txtObservacoes.setWrapStyleWord(true);
        txtObservacoes.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtObservacoes.setEnabled(false); // Desabilitado ate selecionar consulta
        txtObservacoes.setBackground(new Color(248, 249, 250));
        txtObservacoes.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JScrollPane scrollObs = new JScrollPane(txtObservacoes);
        scrollObs.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        scrollObs.setPreferredSize(new Dimension(0, 200)); // Altura preferencial para o campo de texto
        scrollObs.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        JPanel painelBotaoObs = new JPanel(new FlowLayout(FlowLayout.CENTER));
        painelBotaoObs.setBackground(CLEAN_WHITE);
        
        btnSalvarObs = new JButton("Salvar Observacoes");
        btnSalvarObs.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnSalvarObs.setForeground(CLEAN_WHITE);
        btnSalvarObs.setBackground(SUCCESS_GREEN);
        btnSalvarObs.setPreferredSize(new Dimension(200, 35));
        btnSalvarObs.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnSalvarObs.setFocusPainted(false);
        btnSalvarObs.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSalvarObs.setEnabled(false); // Desabilitado ate selecionar consulta
        btnSalvarObs.addActionListener(e -> salvarObservacoes());
        
        btnSalvarObs.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btnSalvarObs.isEnabled()) {
                    btnSalvarObs.setBackground(SUCCESS_GREEN.brighter());
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (btnSalvarObs.isEnabled()) {
                    btnSalvarObs.setBackground(SUCCESS_GREEN);
                }
            }
        });
        
        painelBotaoObs.add(btnSalvarObs);
        
        observacoesPanel.add(lblInfoObservacoes, BorderLayout.NORTH);
        observacoesPanel.add(scrollObs, BorderLayout.CENTER);
        observacoesPanel.add(painelBotaoObs, BorderLayout.SOUTH);
    }
    
    /**
     * Cria e configura o painel de botoes de acao no rodape da tela.
     * Inclui botoes para atualizar a lista, cancelar/realizar consultas,
     * criar novo agendamento, ver o calendario e fechar a tela.
     *
     * Comunicacao com Outras Classes:
     * - TelaAgendamento: Aberta para criar novo agendamento.
     * - TelaAgendaCalendario: Aberta para ver o calendario semanal.
     */
    private void criarBotoesAcao() {
        botoesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        botoesPanel.setBackground(LIGHT_GRAY);
        botoesPanel.setPreferredSize(new Dimension(0, 80));
        
        // Botoes de navegacao adicionais
        btnNovoAgendamento = criarBotao("Novo Agendamento", SUCCESS_GREEN, e -> abrirTelaAgendamento());
        btnVerCalendario = criarBotao("Ver no Calendario", PRIMARY_BLUE, e -> abrirTelaAgendaCalendario());

        // Botoes de acao principal da tela
        btnAtualizar = criarBotao("Atualizar", PRIMARY_BLUE, e -> carregarConsultas());
        btnRealizar = criarBotao("Marcar Realizada", MEDICAL_GREEN, e -> marcarRealizada());
        btnCancelar = criarBotao("Cancelar Consulta", ACCENT_RED, e -> cancelarConsulta());
        
        // Botao para fechar a tela e retornar ao menu principal
        btnFechar = criarBotao("Fechar", DARK_TEXT, e -> {
            dispose();
            trazerTelaPrincipalParaFrente();
        });
        
        // Adicionar botoes ao painel em ordem logica
        botoesPanel.add(btnNovoAgendamento);
        botoesPanel.add(btnVerCalendario);
        botoesPanel.add(btnAtualizar);
        botoesPanel.add(btnRealizar);
        botoesPanel.add(btnCancelar);
        botoesPanel.add(btnFechar);
    }
    
    /**
     * Cria um botao padronizado com estilo moderno para uso nos paineis de acao.
     * Inclui configuracoes de fonte, cor, tamanho, borda e um efeito de hover visual.
     * @param texto O texto a ser exibido no botao.
     * @param cor A cor de fundo padrao do botao.
     * @param acao O ActionListener a ser executado quando o botao e clicado.
     * @return Um JButton configurado com o estilo definido.
     */
    private JButton criarBotao(String texto, Color cor, ActionListener acao) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 16));
        botao.setForeground(CLEAN_WHITE);
        botao.setBackground(cor);
        botao.setPreferredSize(new Dimension(200, 50)); // Tamanho padrao para botoes de acao
        botao.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        botao.setFocusPainted(false);
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botao.addActionListener(acao);
        
        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                botao.setBackground(cor.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                botao.setBackground(cor);
            }
        });
        
        return botao;
    }
    
    /**
     * Organiza o layout final dos paineis na janela principal (contentPane).
     * O headerPanel e posicionado ao norte, o centralPanel (dividido em tabela e observacoes)
     * ocupa o espaco central, e o botoesPanel fica na parte inferior.
     */
    private void organizarLayout() {
        contentPane.add(headerPanel, BorderLayout.NORTH);
        
        centralPanel = new JPanel(new BorderLayout());
        centralPanel.setBackground(LIGHT_GRAY);
        centralPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        centralPanel.add(scrollPaneTabela, BorderLayout.CENTER);
        centralPanel.add(observacoesPanel, BorderLayout.EAST);
        
        contentPane.add(centralPanel, BorderLayout.CENTER);
        contentPane.add(botoesPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Aplica estilos visuais finais aos paineis principais.
     * Adiciona bordas e efeitos para aprimorar a estetica da interface.
     */
    private void aplicarEstilosVisuais() {
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 0, 0, 30)),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
    }
    
    /**
     * Carrega a consulta selecionada na tabela no painel de observacoes.
     * Habilita a edicao das observacoes e o botao de salvar quando uma consulta e selecionada.
     * Se nenhuma consulta estiver selecionada, o painel de observacoes e resetado.
     *
     * Comunicacao com Outras Classes:
     * - ConsultaDAO: Para buscar os detalhes completos da consulta.
     * - Consulta: Para obter os dados da consulta, incluindo observacoes.
     */
    private void carregarConsultaSelecionada() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                Long id = (Long) tableModel.getValueAt(selectedRow, 0);
                
                // Busca a consulta completa usando streams para eficiencia
                Optional<Consulta> optConsulta = consultaDAO.findAll().stream()
                    .filter(c -> c.getId().equals(id))
                    .findFirst();
                
                if (optConsulta.isPresent()) {
                    consultaSelecionada = optConsulta.get();
                    
                    String obs = consultaSelecionada.getObservacoes();
                    txtObservacoes.setText(obs != null ? obs : "");
                    
                    txtObservacoes.setEnabled(true);
                    btnSalvarObs.setEnabled(true);
                    lblInfoObservacoes.setText("Editando observacoes da consulta selecionada");
                } else {
                    resetPainelObservacoes();
                }
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Erro ao carregar consulta: " + e.getMessage(),
                    "Erro de Sistema", 
                    JOptionPane.ERROR_MESSAGE);
                resetPainelObservacoes();
            }
        } else {
            // Nenhuma consulta selecionada
            resetPainelObservacoes();
        }
    }

    /**
     * Reseta o estado do painel de observacoes e seus componentes (area de texto, botao, label informativo).
     * Este metodo e chamado quando nenhuma consulta esta selecionada na tabela.
     */
    private void resetPainelObservacoes() {
        consultaSelecionada = null;
        txtObservacoes.setText("");
        txtObservacoes.setEnabled(false);
        btnSalvarObs.setEnabled(false);
        lblInfoObservacoes.setText("Selecione uma consulta para editar observacoes");
    }
    
    /**
     * Salva as observacoes editadas da consulta selecionada no banco de dados.
     * Atualiza o objeto Consulta em memoria e em seguida persiste a alteracao.
     * Apos o salvamento, recarrega a tabela de consultas para refletir a mudanca.
     *
     * Comunicacao com Outras Classes:
     * - Consulta: Modifica as observacoes.
     * - ConsultaDAO: Para atualizar a consulta no banco de dados.
     */
    private void salvarObservacoes() {
        if (consultaSelecionada == null) {
            JOptionPane.showMessageDialog(this, 
                "Nenhuma consulta selecionada!",
                "Aviso", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            consultaSelecionada.setObservacoes(txtObservacoes.getText().trim());
            consultaDAO.update(consultaSelecionada);
            
            JOptionPane.showMessageDialog(this, 
                "Observacoes salvas com sucesso!",
                "Sucesso", 
                JOptionPane.INFORMATION_MESSAGE);
            
            carregarConsultas(); // Atualiza a tabela
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao salvar observacoes: " + e.getMessage(),
                "Erro de Sistema", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Carrega todas as consultas do banco de dados e as exibe na tabela da agenda.
     * Limpa o modelo da tabela e o preenche com os dados mais recentes.
     *
     * Comunicacao com Outras Classes:
     * - ConsultaDAO: Para buscar todas as consultas.
     */
    private void carregarConsultas() {
        try {
            List<Consulta> consultas = consultaDAO.findAll();
            tableModel.setRowCount(0); // Limpa todas as linhas existentes
            
            for (Consulta consulta : consultas) {
                String observacoes = consulta.getObservacoes();
                // Limita a exibicao das observacoes na tabela para nao poluir
                if (observacoes != null && observacoes.length() > 50) {
                    observacoes = observacoes.substring(0, 47) + "...";
                }
                
                Object[] row = {
                    consulta.getId(),
                    consulta.getDataHorarioFormatado(),
                    consulta.getMedico().getNome(),
                    consulta.getPaciente().getNome(),
                    consulta.getStatus().getDescricao(),
                    observacoes != null ? observacoes : ""
                };
                tableModel.addRow(row);
            }
            resetPainelObservacoes(); // Garante que o painel de observacoes esteja limpo se nada estiver selecionado
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao carregar consultas: " + e.getMessage(),
                "Erro de Sistema", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Cancela a consulta selecionada na tabela.
     * Valida se a consulta pode ser cancelada (apenas status "Agendada")
     * e solicita confirmacao do usuario antes de atualizar o status no banco de dados.
     *
     * Comunicacao com Outras Classes:
     * - Consulta: Obtem o status e modifica-o.
     * - ConsultaDAO: Para atualizar o status da consulta.
     */
    private void cancelarConsulta() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Selecione uma consulta!",
                "Aviso", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            Long id = (Long) tableModel.getValueAt(selectedRow, 0);
            String status = (String) tableModel.getValueAt(selectedRow, 4);
            
            if (!"Agendada".equals(status)) {
                JOptionPane.showMessageDialog(this, 
                    "Apenas consultas agendadas podem ser canceladas!",
                    "Operacao Invalida", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int opcao = JOptionPane.showConfirmDialog(
                this,
                "Deseja realmente cancelar esta consulta?",
                "Confirmar Cancelamento",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (opcao == JOptionPane.YES_OPTION) {
                // Busca a consulta e atualiza status
                Optional<Consulta> optConsulta = consultaDAO.findAll().stream()
                    .filter(c -> c.getId().equals(id))
                    .findFirst();

                if (optConsulta.isPresent()) {
                    Consulta consulta = optConsulta.get();
                    consulta.setStatus(Consulta.StatusConsulta.CANCELADA);
                    consultaDAO.update(consulta);
                    
                    JOptionPane.showMessageDialog(this, 
                        "Consulta cancelada com sucesso!",
                        "Sucesso", 
                        JOptionPane.INFORMATION_MESSAGE);
                    carregarConsultas(); // Recarrega a lista para refletir a mudanca
                }
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao cancelar consulta: " + e.getMessage(),
                "Erro de Sistema", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Marca a consulta selecionada na tabela como "Realizada".
     * Valida se a consulta pode ser marcada (apenas status "Agendada")
     * e solicita observacoes adicionais antes de atualizar o status no banco de dados.
     *
     * Comunicacao com Outras Classes:
     * - Consulta: Obtem o status, modifica-o e define observacoes.
     * - ConsultaDAO: Para atualizar o status e observacoes da consulta.
     */
    private void marcarRealizada() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Selecione uma consulta!",
                "Aviso", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            Long id = (Long) tableModel.getValueAt(selectedRow, 0);
            String status = (String) tableModel.getValueAt(selectedRow, 4);
            
            if (!"Agendada".equals(status)) {
                JOptionPane.showMessageDialog(this, 
                    "Apenas consultas agendadas podem ser marcadas como realizadas!",
                    "Operacao Invalida", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String observacoes = JOptionPane.showInputDialog(
                this,
                "Digite as observacoes da consulta:",
                "Observacoes da Consulta",
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (observacoes != null) { // Continua somente se o usuario nao cancelar a entrada
                // Busca a consulta e atualiza
                Optional<Consulta> optConsulta = consultaDAO.findAll().stream()
                    .filter(c -> c.getId().equals(id))
                    .findFirst();
                
                if (optConsulta.isPresent()) {
                    Consulta consulta = optConsulta.get();
                    consulta.setStatus(Consulta.StatusConsulta.REALIZADA);
                    consulta.setObservacoes(observacoes);
                    consultaDAO.update(consulta);
                    
                    JOptionPane.showMessageDialog(this, 
                        "Consulta marcada como realizada!",
                        "Sucesso", 
                        JOptionPane.INFORMATION_MESSAGE);
                    carregarConsultas(); // Recarrega a lista para refletir a mudanca
                }
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao marcar consulta: " + e.getMessage(),
                "Erro de Sistema", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Abre a TelaAgendamento para criar uma nova consulta.
     * Fecha a tela atual se ja nao estiver visivel.
     *
     * Comunicacao com Outras Classes:
     * - TelaAgendamento: Abre uma nova instancia e a torna visivel.
     */
    private void abrirTelaAgendamento() {
        // Nao fecha a tela atual, apenas abre a nova. O usuario pode fechar esta manualmente.
        new TelaAgendamento().setVisible(true); 
    }

    /**
     * Abre a TelaAgendaCalendario para visualizar os agendamentos em formato de calendario.
     * Fecha a tela atual se ja nao estiver visivel.
     *
     * Comunicacao com Outras Classes:
     * - TelaAgendaCalendario: Abre uma nova instancia e a torna visivel.
     */
    private void abrirTelaAgendaCalendario() {
        // Nao fecha a tela atual, apenas abre a nova. O usuario pode fechar esta manualmente.
        new TelaAgendaCalendario().setVisible(true);
    }
    
    /**
     * Traz para a frente a instancia existente da TelaPrincipal,
     * ou cria uma nova se nenhuma for encontrada.
     * Garante que o usuario retorne ao menu principal sem abrir multiplas janelas.
     *
     * Comunicacao com Outras Classes:
     * - TelaPrincipal: Busca por uma instancia existente ou cria uma nova.
     */
    private void trazerTelaPrincipalParaFrente() {
        TelaPrincipal principal = null;
        // Percorre todas as janelas abertas para encontrar a TelaPrincipal
        for (Window window : Window.getWindows()) {
            if (window instanceof TelaPrincipal) {
                principal = (TelaPrincipal) window;
                break;
            }
        }

        if (principal != null) {
            principal.setVisible(true);
            principal.toFront();
            principal.requestFocus();
        } else {
            // Se por alguma razao a TelaPrincipal nao estiver aberta, cria uma nova
            // (Isso nao deveria acontecer se a TelaPrincipal e o ponto de entrada)
            new TelaPrincipal().setVisible(true);
        }
    }
}