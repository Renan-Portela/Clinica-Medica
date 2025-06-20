package br.com.clinica.view;

import br.com.clinica.config.DatabaseConnection;
import br.com.clinica.dao.ConsultaDAO;
import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.model.Consulta;
import br.com.clinica.model.Paciente;
import br.com.clinica.util.ValidadorCPF;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;

/**
 * Tela principal do sistema de gestão de clínica médica.
 * Apresenta um layout dividido com uma imagem informativa à esquerda e um grid de botões à direita.
 * O design é moderno, com layout responsivo e feedback visual nos botões.
 * Interage com DatabaseConnection para verificar o status do banco de dados e com os DAOs
 * para funcionalidades como a busca de histórico de paciente.
 *
 * Estrutura da interface:
 * - Header: Contém a logo, título do sistema, e botões de ação ("Sobre").
 * - Centro: Dividido em duas áreas por um JSplitPane:
 * - Esquerda: Exibe uma imagem médica.
 * - Direita: Contém um grid de botões para navegação às telas principais.
 * - Footer: Apresenta informações de copyright ou versão do sistema.
 */
public class TelaPrincipal extends JFrame {

    private static final long serialVersionUID = 1L;

    // Paleta de cores médica profissional
    private static final Color PRIMARY_BLUE = new Color(52, 144, 220);
    private static final Color CLEAN_WHITE = new Color(255, 255, 255);
    private static final Color LIGHT_GRAY = new Color(248, 249, 250);
    private static final Color DARK_TEXT = new Color(52, 58, 64);

    // Componentes principais da interface
    private JPanel contentPane;
    private JPanel botoesPanel;

    // DAOs para funcionalidades extras
    private PacienteDAO pacienteDAO;
    private ConsultaDAO consultaDAO;

    /**
     * Construtor principal da TelaPrincipal.
     * Inicializa a interface, os DAOs necessários e configura os componentes visuais.
     */
    public TelaPrincipal() {
        this.pacienteDAO = new PacienteDAO();
        this.consultaDAO = new ConsultaDAO();
        configurarJanelaPrincipal();
        inicializarComponentes();
        verificarConexaoBanco();
    }
    
    /**
     * Configura as propriedades básicas da janela principal (JFrame).
     */
    private void configurarJanelaPrincipal() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Sistema de Gestão Clínica Médica");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(LIGHT_GRAY);
        setContentPane(contentPane);
    }

    /**
     * Inicializa todos os componentes visuais da interface principal.
     */
    private void inicializarComponentes() {
        criarLayoutDividido();
    }

    /**
     * Cria e organiza o layout principal da interface, incluindo cabeçalho,
     * painel central dividido e rodapé.
     */
    private void criarLayoutDividido() {
        contentPane.add(criarHeader(), BorderLayout.NORTH);
        contentPane.add(criarPainelCentral(), BorderLayout.CENTER);
        contentPane.add(criarFooter(), BorderLayout.SOUTH);
    }
    
    /**
     * Cria o painel de cabeçalho com logo, título, subtítulo e botões de ação.
     * @return O JPanel do cabeçalho configurado.
     */
    private JPanel criarHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_BLUE);
        headerPanel.setPreferredSize(new Dimension(0, 100));
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 30));

        JPanel leftContentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        leftContentPanel.setOpaque(false);

        JLabel lblLogo = new JLabel("");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblLogo.setForeground(DARK_TEXT);
        lblLogo.setPreferredSize(new Dimension(80, 80));
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogo.setVerticalAlignment(SwingConstants.CENTER);
        lblLogo.setBorder(BorderFactory.createDashedBorder(CLEAN_WHITE, 2, 5, 2, false));
        lblLogo.setOpaque(true);
        lblLogo.setBackground(LIGHT_GRAY);
        
        leftContentPanel.add(lblLogo);

        JPanel titulosPanel = new JPanel();
        titulosPanel.setLayout(new BoxLayout(titulosPanel, BoxLayout.Y_AXIS));
        titulosPanel.setOpaque(false);

        JLabel lblTitulo = new JLabel("ClinicSync - Sistema de Gestão de Clínica médica.");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(CLEAN_WHITE);

        JLabel lblSubtitulo = new JLabel("Gerencie consultas, pacientes e médicos de forma eficiente");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblSubtitulo.setForeground(new Color(255, 255, 255, 200));
        
        titulosPanel.add(Box.createVerticalGlue());
        titulosPanel.add(lblTitulo);
        titulosPanel.add(Box.createVerticalStrut(5));
        titulosPanel.add(lblSubtitulo);
        titulosPanel.add(Box.createVerticalGlue());
        
        leftContentPanel.add(titulosPanel);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        headerRight.setOpaque(false);
        
        JButton btnSobre = criarBotaoHeader("Sobre", e -> abrirDialogoSobre());
        headerRight.add(btnSobre);

        headerPanel.add(leftContentPanel, BorderLayout.WEST);
        headerPanel.add(headerRight, BorderLayout.EAST);
        
        return headerPanel;
    }

    /**
     * Cria o painel central dividido, com imagem à esquerda e botões à direita.
     * @return O JSplitPane configurado.
     */
    private JSplitPane criarPainelCentral() {
        botoesPanel = new JPanel(new GridLayout(3, 2, 25, 25));
        botoesPanel.setBackground(LIGHT_GRAY);
        botoesPanel.setBorder(new EmptyBorder(40, 40, 40, 40));
        
        criarBotoesFuncionais();

        JSplitPane centralPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, criarPainelImagem(), botoesPanel);
        centralPanel.setResizeWeight(0.5);
        centralPanel.setDividerSize(5);
        centralPanel.setBorder(null);

        return centralPanel;
    }

    /**
     * Cria o painel que exibe a imagem principal da aplicação.
     * @return O JPanel da imagem.
     */
    private JPanel criarPainelImagem() {
        JPanel imagemPanel = new JPanel(new BorderLayout());
        imagemPanel.setBackground(CLEAN_WHITE);
        imagemPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel lblImagem = new JLabel();
        lblImagem.setHorizontalAlignment(SwingConstants.CENTER);
        
        URL imageUrl = getClass().getResource("/br/com/clinica/recursos/medicos-clinica.jpg");
        if (imageUrl != null) {
            lblImagem.setIcon(new ImageIcon(imageUrl));
        } else {
            lblImagem.setText("Imagem não encontrada");
        }
        
        imagemPanel.add(lblImagem, BorderLayout.CENTER);
        return imagemPanel;
    }

    /**
     * Cria o painel de rodapé da aplicação.
     * @return O JPanel do rodapé.
     */
    private JPanel criarFooter() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(DARK_TEXT);
        footerPanel.setPreferredSize(new Dimension(0, 50));
        footerPanel.setBorder(new EmptyBorder(15, 30, 15, 30));
        
        JLabel lblRodape = new JLabel("Sistema desenvolvido por JKLR² - Versão 1.1");
        lblRodape.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblRodape.setForeground(new Color(255, 255, 255, 180));
        lblRodape.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblStatus = new JLabel("");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        footerPanel.add(lblRodape, BorderLayout.CENTER);
        footerPanel.add(lblStatus, BorderLayout.EAST);
        
        return footerPanel;
    }

    /**
     * Cria e adiciona os botões funcionais ao painel principal.
     *
     * Este método utiliza Expressões Lambda (sintaxe 'e -> ...') para definir
     * as ações dos botões. Uma expressão lambda é uma forma compacta de
     * representar uma função anônima.
     *
     * No contexto deste método, cada lambda implementa a interface ActionListener,
     * que possui um único método, o 'actionPerformed(ActionEvent e)'. O 'e'
     * representa o ActionEvent, e o código à direita da seta '->' é a ação
     * a ser executada quando o botão é clicado.
     *
     * Esta abordagem é usada por ser mais concisa e legível do que a alternativa
     * tradicional com classes anônimas, que seria muito mais longa para cada botão.
     */
    private void criarBotoesFuncionais() {
        botoesPanel.add(criarBotaoModerno("Agendar Consulta", e -> new TelaNovoAgendamento().setVisible(true)));
        botoesPanel.add(criarBotaoModerno("Visualizar Agenda", e -> new TelaAgendaCalendario().setVisible(true)));
        botoesPanel.add(criarBotaoModerno("Gerenciar Médicos", e -> new TelaMedicos().setVisible(true)));
        botoesPanel.add(criarBotaoModerno("Gerenciar Pacientes", e -> new TelaPacientes().setVisible(true)));
        botoesPanel.add(criarBotaoModerno("Relatórios", e -> new TelaRelatorios().setVisible(true)));
        botoesPanel.add(criarBotaoModerno("Histórico do Paciente", e -> abrirDialogoBuscaHistorico()));
    }

    /**
     * Abre o diálogo "Sobre" com informações dos desenvolvedores e links.
     */
    private void abrirDialogoSobre() {
        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane.setOpaque(false);

        String htmlContent = "<html>"
            + "<body style='font-family: Segoe UI; font-size: 12pt;'>"
            + "<h2>Desenvolvedores</h2>"
            
            + "<p><b>José Vitor</b></p>"
            + "<ul>"
            + "<li><a href='https://github.com/JosVitorFerreiraDosSantosJV/JosVitorFerreiraDosSantosJV'>GitHub</a></li>"
            + "<li><a href='https://www.linkedin.com/in/jos%C3%A9-vitor-ferreira-dos-santos/'>LinkedIn</a></li>"
            + "</ul>"
            + "<hr>"

            + "<p><b>Karolina Zimmerman</b></p>"
            + "<ul>"
            + "<li><a href='https://github.com/404'>GitHub</a></li>"
            + "<li><a href='https://www.linkedin.com/in/karolina-zimmermann-4491b5287/'>LinkedIn</a></li>"
            + "</ul>"
            + "<hr>"

            + "<p><b>Lucas Alves</b></p>"
            + "<ul>"
            + "<li><a href='https://github.com/Lucas-Alves-Paula'>GitHub</a></li>"
            + "<li><a href='https://www.linkedin.com/in/lucas-alves-a02514178/'>LinkedIn</a></li>"
            + "</ul>"
            + "<hr>"

            + "<p><b>Ryan Alves</b></p>"
            + "<ul>"
            + "<li><a href='https://github.com/404'>GitHub</a></li>"
            + "<li><a href='https://linkedin.com/in/404'>LinkedIn</a></li>"
            + "</ul>"
            + "<hr>"

            + "<p><b>Renan Portela</b></p>"
            + "<ul>"
            + "<li><a href='https://github.com/Renan-Portela'>GitHub</a></li>"
            + "<li><a href='https://www.linkedin.com/in/portela-renan/'>LinkedIn</a></li>"
            + "</ul>"

            + "</body></html>";
        
        editorPane.setText(htmlContent);

        editorPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (IOException | URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        JOptionPane.showMessageDialog(this, editorPane, "Sobre o Sistema", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Abre um diálogo customizado para buscar o histórico de um paciente pelo CPF.
     */
    private void abrirDialogoBuscaHistorico() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel label = new JLabel("Digite o CPF do Paciente:");
        panel.add(label, BorderLayout.NORTH);

        JFormattedTextField cpfField = null;
        try {
            MaskFormatter cpfMask = new MaskFormatter("###.###.###-##");
            cpfMask.setPlaceholderCharacter('_');
            cpfField = new JFormattedTextField(cpfMask);
            cpfField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        } catch (ParseException e) {
            e.printStackTrace();
            cpfField = new JFormattedTextField();
        }
        panel.add(cpfField, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panel, "Buscar Histórico", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String cpf = cpfField.getText().replaceAll("[^0-9]", "");
            if (ValidadorCPF.validar(cpf)) {
                try {
                    Paciente paciente = pacienteDAO.findById(cpf);
                    if (paciente != null) {
                        List<Consulta> historico = consultaDAO.findByPaciente(cpf);
                        mostrarHistoricoPopup(paciente, historico);
                    } else {
                        JOptionPane.showMessageDialog(this, "Paciente com o CPF informado não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao buscar histórico: " + ex.getMessage(), "Erro de Sistema", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "CPF inválido. Por favor, tente novamente.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Exibe uma nova janela (JDialog) com o histórico de consultas de um paciente em uma tabela.
     * @param paciente O paciente cujo histórico será exibido.
     * @param consultas A lista de consultas do paciente.
     */
    private void mostrarHistoricoPopup(Paciente paciente, List<Consulta> consultas) {
        if (consultas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhuma consulta encontrada para: " + paciente.getNome(), "Histórico Vazio", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Histórico - " + paciente.getNome(), true);
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        String[] colunas = {"Data/Hora", "Médico", "Status", "Observações"};
        DefaultTableModel modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Consulta consulta : consultas) {
            modeloTabela.addRow(new Object[]{
                consulta.getDataHorarioFormatado(),
                consulta.getMedico().getNome(),
                consulta.getStatus().getDescricao(),
                consulta.getObservacoes()
            });
        }
        
        JTable tabela = new JTable(modeloTabela);
        tabela.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabela.setRowHeight(28);
        
        dialog.add(new JScrollPane(tabela), BorderLayout.CENTER);
        dialog.setVisible(true);
    }
    
    /**
     * Verifica a conexão com o banco de dados.
     */
    private void verificarConexaoBanco() {
        try {
            DatabaseConnection.getInstance().getConnection();
            System.out.println("Conexão ao banco de dados: OK");
        } catch (Exception ex) {
            System.err.println("Conexão ao banco de dados: FALHOU!");
        }
    }
    
    /**
     * Cria um botão de navegação para o cabeçalho.
     * @param texto O texto do botão.
     * @param acao O ActionListener para o clique.
     * @return Um JButton configurado.
     */
    private JButton criarBotaoHeader(String texto, ActionListener acao) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 14));
        botao.setForeground(PRIMARY_BLUE);
        botao.setBackground(CLEAN_WHITE);
        botao.setFocusPainted(false);
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botao.addActionListener(acao);
        return botao;
    }

    /**
     * Cria um botão moderno com bordas arredondadas para o grid principal.
     * @param texto O texto do botão.
     * @param acao O ActionListener para o clique.
     * @return Uma instância de RoundedButton.
     */
    private RoundedButton criarBotaoModerno(String texto, ActionListener acao) {
        RoundedButton botao = new RoundedButton(texto);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 16));
        botao.setForeground(CLEAN_WHITE);
        botao.setBackground(PRIMARY_BLUE);
        botao.setPreferredSize(new Dimension(200, 80));
        botao.addActionListener(acao);
        return botao;
    }
    
    /**
     * Classe interna para criar botões com cantos arredondados.
     * Sobrescreve o método de pintura para desenhar uma forma customizada.
     */
    private class RoundedButton extends JButton {
        private static final long serialVersionUID = 1L;
        private Color hoverBackgroundColor;
        private Color pressedBackgroundColor;

        public RoundedButton(String text) {
            super(text);
            super.setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        public void setBackground(Color bg) {
            super.setBackground(bg);
            hoverBackgroundColor = bg.brighter();
            pressedBackgroundColor = bg.darker();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (getModel().isPressed()) {
                g2.setColor(pressedBackgroundColor);
            } else if (getModel().isRollover()) {
                g2.setColor(hoverBackgroundColor);
            } else {
                g2.setColor(getBackground());
            }

            int arc = 25;
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), arc, arc));

            super.paintComponent(g2);
            g2.dispose();
        }
    }
}