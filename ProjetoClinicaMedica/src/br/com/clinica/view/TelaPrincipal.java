package br.com.clinica.view;

import br.com.clinica.config.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent; // Importacao especifica para ActionEvent
import java.awt.event.ActionListener; // Importacao especifica para ActionListener
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL; // Importacao adicionada

/**
 * Tela principal do sistema de gestao de clinica medica.
 * Apresenta um layout dividido com uma imagem informativa a esquerda e um grid de botoes a direita.
 * O design e moderno, com layout responsivo e feedback visual nos botoes.
 * Interage com DatabaseConnection para verificar o status do banco de dados.
 *
 * Estrutura da interface:
 * - Header: Contem o titulo do sistema e o status da conexao com o banco de dados.
 * - Centro: Dividido em duas areas (50% cada) por um JSplitPane:
 * - Esquerda: Exibe uma imagem medica.
 * - Direita: Contem um grid de botoes para navegacao as telas principais do sistema.
 * - Footer: Apresenta informacoes de copyright ou versao do sistema.
 *
 * Funcionalidades principais:
 * - Verificacao e exibicao do status da conexao com o banco de dados.
 * - Navegacao para as telas de Agendamento, Agenda (Calendario), Medicos, Pacientes e Relatorios.
 * - Botao de saida do sistema com confirmacao.
 */
public class TelaPrincipal extends JFrame {

    private static final long serialVersionUID = 1L;

    // Paleta de cores medica profissional
    private static final Color PRIMARY_BLUE = new Color(52, 144, 220);
    private static final Color MEDICAL_GREEN = new Color(76, 175, 80);
    private static final Color CLEAN_WHITE = new Color(255, 255, 255);
    private static final Color LIGHT_GRAY = new Color(248, 249, 250);
    private static final Color DARK_TEXT = new Color(52, 58, 64);
    private static final Color ACCENT_RED = new Color(220, 53, 69);
    private static final Color ORANGE_ACCENT = new Color(255, 152, 0);
    private static final Color PURPLE_ACCENT = new Color(156, 39, 176);
    private static final Color GRAY_ACCENT = new Color(96, 125, 139);

    // Componentes principais da interface
    private JPanel contentPane;
    private JPanel headerPanel;
    private JSplitPane centralPanel; // Alterado para JSplitPane para layout responsivo
    private JPanel imagemPanel;
    private JPanel botoesPanel;
    private JPanel footerPanel;

    // Labels informativos
    private JLabel lblTitulo;
    private JLabel lblSubtitulo;
    private JLabel lblStatus;
    private JLabel lblRodape;
    private JLabel lblImagem;

    // Botoes funcionais para navegacao entre telas
    private JButton btnMedicos;
    private JButton btnPacientes;
    private JButton btnConsultas;
    private JButton btnAgenda;
    private JButton btnRelatorios;
    private JButton btnSair;

    /**
     * Metodo principal que inicia a aplicacao Swing.
     * Cria e exibe a TelaPrincipal na Event Dispatch Thread (EDT).
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() { // Substituicao de lambda por classe anonima
            @Override
            public void run() {
                try {
                    TelaPrincipal frame = new TelaPrincipal();
                    frame.setVisible(true);
                } catch (Exception e) {
                    // Captura excecoes na inicializacao da interface
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Erro inesperado ao iniciar o sistema: " + e.getMessage(), "Erro de Inicializacao", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    /**
     * Construtor principal da TelaPrincipal.
     * Inicializa a interface com um layout dividido (imagem a esquerda, botoes a direita).
     * Configura a janela, cria os componentes, organiza o layout e aplica estilos visuais.
     */
    public TelaPrincipal() {
        configurarJanelaPrincipal();
        inicializarComponentes(); // Chama verificarConexaoBanco aqui dentro
        criarLayoutDividido();
        aplicarEstilosVisuais();
    }

    /**
     * Configura as propriedades basicas da janela principal (JFrame).
     * Define a operacao de fechamento, titulo, estado de tela maximizada e posicao inicial.
     */
    private void configurarJanelaPrincipal() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Sistema de Gestao Clinica Medica");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximiza a janela na tela
        setLocationRelativeTo(null); // Centraliza a janela

        contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(LIGHT_GRAY);
        setContentPane(contentPane);
    }

    /**
     * Inicializa todos os componentes visuais da interface principal.
     * Cria os paineis, labels informativos, carrega a imagem medica,
     * e cria os botoes funcionais, alem de verificar a conexao com o banco de dados.
     */
    private void inicializarComponentes() {
        criarPaineis();
        criarLabelsInformativos();
        carregarImagemMedica();
        criarBotoesFuncionais();
        verificarConexaoBanco(); // Verifica e atualiza o status da conexao
    }

    /**
     * Cria e configura os paineis principais que compoem a estrutura da interface.
     * Inclui o headerPanel, o centralPanel (como JSplitPane), imagemPanel, botoesPanel e footerPanel.
     */
    private void criarPaineis() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_BLUE);
        headerPanel.setPreferredSize(new Dimension(0, 100));
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        imagemPanel = new JPanel(new BorderLayout());
        imagemPanel.setBackground(CLEAN_WHITE);
        imagemPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // Borda interna

        botoesPanel = new JPanel(new GridLayout(3, 2, 25, 25)); // Grid 3x2 com espacamento
        botoesPanel.setBackground(LIGHT_GRAY);
        botoesPanel.setBorder(new EmptyBorder(40, 40, 40, 40)); // Padding ao redor do grid

        centralPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imagemPanel, botoesPanel);
        centralPanel.setResizeWeight(0.5); // Divide o peso de redimensionamento 50/50
        centralPanel.setOneTouchExpandable(false); // Nao mostra o botao de expandir/recolher
        centralPanel.setDividerSize(5); // Largura do divisor
        centralPanel.setBorder(null); // Remove a borda padrao do JSplitPane

        footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(DARK_TEXT);
        footerPanel.setPreferredSize(new Dimension(0, 50));
        footerPanel.setBorder(new EmptyBorder(15, 30, 15, 30));
    }

    /**
     * Cria e inicializa os labels informativos da interface.
     * Define o titulo principal, o subtitulo, o label de status da conexao e o rodape.
     */
    private void criarLabelsInformativos() {
        lblTitulo = new JLabel("SISTEMA DE GESTAO CLINICA MEDICA");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(CLEAN_WHITE);

        lblSubtitulo = new JLabel("Gerencie consultas, pacientes e medicos de forma eficiente");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblSubtitulo.setForeground(new Color(255, 255, 255, 200));

        lblStatus = new JLabel(""); // Inicialmente sem texto/cor, definido por verificarConexaoBanco()
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));

        lblRodape = new JLabel("Sistema desenvolvido para Projeto Academico - Versao 1.0");
        lblRodape.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblRodape.setForeground(new Color(255, 255, 255, 180));
        lblRodape.setHorizontalAlignment(SwingConstants.CENTER);
    }

    /**
     * Carrega e configura a imagem medica no painel esquerdo da interface.
     * Tenta carregar a imagem de recursos internos do JAR ou de um caminho relativo.
     * Se a imagem nao for encontrada, um placeholder informativo e exibido.
     * A imagem e redimensionada dinamicamente para se ajustar ao tamanho do painel.
     */
    private void carregarImagemMedica() {
        lblImagem = new JLabel();
        lblImagem.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagem.setVerticalAlignment(SwingConstants.CENTER);
        lblImagem.setBackground(new Color(245, 245, 245));
        lblImagem.setOpaque(true);
        lblImagem.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2));

        URL imageUrl = getClass().getResource("/imagens/medicos-clinica.jpg"); // Preferencia por recursos
        if (imageUrl == null) {
            // Tenta caminho relativo se nao encontrado em recursos
            imageUrl = getClass().getClassLoader().getResource("br/com/clinica/recursos/medicos-clinica.jpg");
        }

        if (imageUrl != null) {
            ImageIcon originalIcon = new ImageIcon(imageUrl);
            
            // Adiciona um ComponentListener para redimensionar a imagem dinamicamente
            imagemPanel.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    int panelWidth = imagemPanel.getWidth();
                    int panelHeight = imagemPanel.getHeight();
                    
                    if (panelWidth > 0 && panelHeight > 0 && originalIcon.getIconWidth() > 0) {
                        Image img = originalIcon.getImage();
                        double scaleX = (double) panelWidth / originalIcon.getIconWidth();
                        double scaleY = (double) panelHeight / originalIcon.getIconHeight();
                        double scale = Math.min(scaleX, scaleY); // Mantem a proporcao e cabe no painel

                        Image scaledImg = img.getScaledInstance((int) (originalIcon.getIconWidth() * scale), 
                                                                (int) (originalIcon.getIconHeight() * scale), 
                                                                Image.SCALE_SMOOTH);
                        lblImagem.setIcon(new ImageIcon(scaledImg));
                        lblImagem.setText(""); // Remove o texto do placeholder se a imagem for carregada
                    }
                }
            });
        } else {
            // Placeholder se a imagem nao for encontrada
            lblImagem.setText("<html><div style='text-align: center; color: #666; font-size: 16px;'>" +
                             "<br><br><br><br><br><br>" +
                             "IMAGEM MEDICA<br>" +
                             "NAO ENCONTRADA<br><br>" +
                             "Verifique:<br>" +
                             "/src/main/resources/imagens/medicos-clinica.jpg<br>ou<br>" +
                             "/src/br/com/clinica/recursos/medicos-clinica.jpg" +
                             "<br><br><br><br><br><br>" +
                             "</div></html>");
        }
        imagemPanel.add(lblImagem, BorderLayout.CENTER);
    }

    /**
     * Cria e configura os botoes funcionais para navegacao entre as telas principais.
     * Os botoes sao dispostos em um grid 3x2 no painel direito da interface.
     * Cada botao tem uma acao associada para abrir a tela correspondente.
     */
    private void criarBotoesFuncionais() {
        // Botao Agendar Consulta
        btnConsultas = criarBotaoModerno("Agendar Consulta", MEDICAL_GREEN, new ActionListener() { // Substituicao de lambda
            @Override
            public void actionPerformed(ActionEvent e) {
                new TelaAgendamento().setVisible(true);
            }
        });

        // Botao Visualizar Agenda (Calendario)
        btnAgenda = criarBotaoModerno("Visualizar Agenda", PRIMARY_BLUE, new ActionListener() { // Substituicao de lambda
            @Override
            public void actionPerformed(ActionEvent e) {
                new TelaAgendaCalendario().setVisible(true);
            }
        });

        // Botao Gerenciar Medicos
        btnMedicos = criarBotaoModerno("Gerenciar Medicos", PURPLE_ACCENT, new ActionListener() { // Substituicao de lambda
            @Override
            public void actionPerformed(ActionEvent e) {
                new TelaMedicos().setVisible(true);
            }
        });

        // Botao Gerenciar Pacientes
        btnPacientes = criarBotaoModerno("Gerenciar Pacientes", ORANGE_ACCENT, new ActionListener() { // Substituicao de lambda
            @Override
            public void actionPerformed(ActionEvent e) {
                new TelaPacientes().setVisible(true);
            }
        });

        // Botao Relatorios
        btnRelatorios = criarBotaoModerno("Relatorios", GRAY_ACCENT, new ActionListener() { // Substituicao de lambda
            @Override
            public void actionPerformed(ActionEvent e) {
                new TelaRelatorios().setVisible(true);
            }
        });

        // Botao Sair do Sistema
        btnSair = criarBotaoModerno("Sair do Sistema", ACCENT_RED, new ActionListener() { // Substituicao de lambda
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmarSaida(e);
            }
        });
        
        // Ordem de adicao define posicao no grid:
        botoesPanel.add(btnConsultas);
        botoesPanel.add(btnAgenda);
        botoesPanel.add(btnMedicos);
        botoesPanel.add(btnPacientes);
        botoesPanel.add(btnRelatorios);
        botoesPanel.add(btnSair);
    }

    /**
     * Cria um botao moderno com estilo visual padronizado.
     * Inclui configuracoes de fonte, cor, tamanho preferencial, borda e um efeito de hover.
     * @param texto O texto a ser exibido no botao.
     * @param cor A cor de fundo padrao do botao.
     * @param acao O ActionListener a ser executado quando o botao e clicado.
     * @return Um JButton estilizado.
     */
    private JButton criarBotaoModerno(String texto, Color cor, ActionListener acao) { // Adicionado ActionListener como parametro
        JButton botao = new JButton(texto);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 16));
        botao.setForeground(CLEAN_WHITE);
        botao.setBackground(cor);
        botao.setPreferredSize(new Dimension(200, 80)); // Tamanho otimizado para grid
        botao.setFocusPainted(false);
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Define a borda padrao
        botao.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0, 40), 1)); // Borda fina e escura

        // Hover effect para feedback visual
        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                botao.setBackground(cor.brighter());
                botao.setBorder(BorderFactory.createLineBorder(CLEAN_WHITE, 2)); // Borda branca e mais grossa no hover
            }

            @Override
            public void mouseExited(MouseEvent e) {
                botao.setBackground(cor);
                botao.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0, 40), 1)); // Retorna a borda padrao
            }
        });
        botao.addActionListener(acao); // Adicionado o ActionListener

        return botao;
    }

    /**
     * Organiza o layout da interface principal.
     * O headerPanel e posicionado ao norte, o centralPanel (JSplitPane) no centro,
     * e o footerPanel na parte inferior da janela.
     */
    private void criarLayoutDividido() {
        // Organizar header
        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);
        headerLeft.add(lblTitulo);
        headerLeft.add(Box.createVerticalStrut(5));
        headerLeft.add(lblSubtitulo);

        headerPanel.add(headerLeft, BorderLayout.WEST);
        headerPanel.add(lblStatus, BorderLayout.EAST); // Status de conexao

        // Adicionar paineis ao JSplitPane. A proporcao ja e configurada em criarPaineis()
        centralPanel.setLeftComponent(imagemPanel);
        centralPanel.setRightComponent(botoesPanel);
        
        // Definir a localizacao inicial do divisor
        centralPanel.setDividerLocation(0.5); // 50% da largura total do JSplitPane

        // Organizar footer
        footerPanel.add(lblRodape, BorderLayout.CENTER);

        // Adicionar paineis ao contentPane
        contentPane.add(headerPanel, BorderLayout.NORTH);
        contentPane.add(centralPanel, BorderLayout.CENTER); // JSplitPane agora no centro
        contentPane.add(footerPanel, BorderLayout.SOUTH);
    }

    /**
     * Aplica estilos visuais finais aos componentes da interface para um acabamento profissional.
     * Define bordas e efeitos de sombra sutis.
     */
    private void aplicarEstilosVisuais() {
        // Sombra sutil no header
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 0, 0, 30)),
                new EmptyBorder(20, 30, 20, 30)
        ));
        
        // A borda do imagemPanel e interna. A borda entre os dois lados e feita pelo JSplitPane.
        // As bordas dos botoes sao tratadas dentro de criarBotaoModerno.
    }

    /**
     * Exibe um dialogo de confirmacao antes de sair do sistema.
     * Se o usuario confirmar, a conexao com o banco de dados e fechada e a aplicacao e encerrada.
     * @param e O evento de acao que disparou este metodo.
     */
    private void confirmarSaida(java.awt.event.ActionEvent e) {
        int opcao = JOptionPane.showConfirmDialog(
                this,
                "Deseja realmente sair do sistema?",
                "Confirmar Saida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE // Adicionado icone de pergunta
        );

        if (opcao == JOptionPane.YES_OPTION) {
            DatabaseConnection.getInstance().closeConnection();
            System.exit(0);
        }
    }

    /**
     * Verifica a conexao com o banco de dados na inicializacao da tela
     * e atualiza o label de status no header para informar o usuario.
     * Interage com DatabaseConnection.
     */
    private void verificarConexaoBanco() {
        try {
            DatabaseConnection.getInstance().getConnection(); // Tenta obter a conexao
            lblStatus.setText("Conexao ao banco de dados: OK");
            lblStatus.setForeground(MEDICAL_GREEN);
        } catch (Exception ex) {
            lblStatus.setText("Conexao ao banco de dados: FALHOU! " + ex.getMessage());
            lblStatus.setForeground(ACCENT_RED);
            System.err.println("Erro ao conectar ao banco de dados: " + ex.getMessage());
            // Opcional: mostrar um dialogo mais detalhado ou logar para depuracao
        }
    }
}