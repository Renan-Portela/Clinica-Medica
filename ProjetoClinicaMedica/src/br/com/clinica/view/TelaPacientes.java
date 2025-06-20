package br.com.clinica.view;

import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.model.Paciente;
import br.com.clinica.util.ValidadorCPF;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gerencia o Cadastro, Leitura, Atualização e Exclusão (CRUD) de pacientes.
 * A tela é iniciada com uma tabela vazia, exigindo que o usuário realize uma
 * busca ou liste todos os pacientes para visualizar os dados. A interface
 * segue o padrão visual moderno da aplicação.
 * Interage com as classes: PacienteDAO, Paciente, ValidadorCPF.
 */
public class TelaPacientes extends JFrame {

    private static final long serialVersionUID = 1L;

    // Paleta de cores padrão do sistema
    private static final Color PRIMARY_BLUE = new Color(52, 144, 220);
    private static final Color SUCCESS_GREEN = new Color(40, 167, 69);
    private static final Color ACCENT_RED = new Color(220, 53, 69);
    private static final Color CLEAN_WHITE = new Color(255, 255, 255);
    private static final Color LIGHT_GRAY = new Color(248, 249, 250);
    private static final Color DARK_TEXT = new Color(52, 58, 64);
    
    // Componentes da interface
    private JPanel contentPane;
    private JTable table;
    private DefaultTableModel tableModel;
    private PacienteDAO pacienteDAO;

    // Campos do formulário
    private JTextField txtCpf;
    private JTextField txtNome;
    private JTextField txtDataNascimento;
    private JTextField txtEndereco;
    private JTextField txtTelefone;
    private JTextArea txtHistorico;

    // Campos de busca
    private JTextField txtBusca;
    
    // Controle de estado
    private Paciente pacienteSelecionado = null;

    /**
     * Construtor da tela de pacientes. Inicializa o DAO e a interface gráfica.
     */
    public TelaPacientes() {
        this.pacienteDAO = new PacienteDAO();
        inicializarInterface();
    }

    /**
     * Ponto de entrada para a construção da interface gráfica da tela.
     */
    private void inicializarInterface() {
        configurarJanela();
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(criarHeader(), BorderLayout.NORTH);
        topPanel.add(criarFormulario(), BorderLayout.CENTER);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(criarPainelBusca(), BorderLayout.NORTH);
        centerPanel.add(criarTabelaPacientes(), BorderLayout.CENTER);

        contentPane.add(topPanel, BorderLayout.NORTH);
        contentPane.add(centerPanel, BorderLayout.CENTER);
        contentPane.add(criarPainelDeBotoes(), BorderLayout.SOUTH);
    }

    /**
     * Configura as propriedades principais da janela (JFrame).
     */
    private void configurarJanela() {
        setTitle("Gerenciamento de Pacientes - Sistema Clinica");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(LIGHT_GRAY);
        setContentPane(contentPane);
    }

    /**
     * Cria o cabeçalho da tela.
     * @return O painel do cabeçalho.
     */
    private JPanel criarHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_BLUE);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        JLabel lblTitulo = new JLabel("GERENCIAMENTO DE PACIENTES");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(CLEAN_WHITE);
        
        headerPanel.add(lblTitulo, BorderLayout.WEST);
        return headerPanel;
    }

    /**
     * Cria o painel do formulário para entrada de dados do paciente.
     * @return O painel do formulário.
     */
    private JPanel criarFormulario() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CLEAN_WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_BLUE, 1), 
                "Dados do Paciente", 0, 0, new Font("Segoe UI", Font.BOLD, 16), PRIMARY_BLUE),
            new EmptyBorder(15, 25, 15, 25)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Linha 1: CPF, Nome, Data Nascimento
        gbc.gridy = 0;
        gbc.gridx = 0;
        formPanel.add(criarCampo("CPF:", txtCpf = criarCampoTextoFormatado("###.###.###-##")), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(criarCampo("Nome Completo:", txtNome = new JTextField(30)), gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        formPanel.add(criarCampo("Data Nascimento:", txtDataNascimento = criarCampoTextoFormatado("##/##/####")), gbc);
        
        // Linha 2: Endereço, Telefone
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        formPanel.add(criarCampo("Endereço:", txtEndereco = new JTextField(40)), gbc);
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        formPanel.add(criarCampo("Telefone:", txtTelefone = criarCampoTextoFormatado("(##) #####-####")), gbc);

        // Linha 3: Histórico Médico
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        formPanel.add(criarCampo("Histórico Médico:", txtHistorico = new JTextArea(4, 30)), gbc);
        
        return formPanel;
    }

    /**
     * Cria um painel de campo de formulário com um rótulo e um componente.
     * @param label O texto do rótulo.
     * @param component O componente de entrada de dados.
     * @return Um painel contendo o rótulo e o componente.
     */
    private JPanel criarCampo(String label, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        if (component instanceof JTextArea) {
            JScrollPane scrollPane = new JScrollPane(component);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            panel.add(lbl, BorderLayout.NORTH);
            panel.add(scrollPane, BorderLayout.CENTER);
        } else {
            component.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            panel.add(lbl, BorderLayout.NORTH);
            panel.add(component, BorderLayout.CENTER);
        }
        return panel;
    }

    /**
     * Cria um campo de texto formatado com uma máscara.
     * @param mask A máscara a ser aplicada.
     * @return Um JFormattedTextField configurado.
     */
    private JFormattedTextField criarCampoTextoFormatado(String mask) {
        try {
            MaskFormatter formatter = new MaskFormatter(mask);
            formatter.setPlaceholderCharacter('_');
            return new JFormattedTextField(formatter);
        } catch (ParseException e) {
            return new JFormattedTextField();
        }
    }

    /**
     * Cria o painel de busca acima da tabela.
     * @return O painel de busca.
     */
    private JPanel criarPainelBusca() {
        JPanel buscaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buscaPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        buscaPanel.add(new JLabel("Buscar por CPF:"));
        txtBusca = new JTextField(30);
        buscaPanel.add(txtBusca);
        
        JButton btnBuscar = criarBotaoAcao("Buscar", SUCCESS_GREEN, e -> buscarPacientes());
        btnBuscar.setToolTipText("Filtra a lista com base no texto digitado");
        buscaPanel.add(btnBuscar);
        
        JButton btnListarTodos = criarBotaoAcao("Listar Todos", PRIMARY_BLUE, e -> carregarTodosPacientes());
        btnListarTodos.setToolTipText("Mostra todos os pacientes cadastrados no sistema");
        buscaPanel.add(btnListarTodos);

        return buscaPanel;
    }

    /**
     * Cria o painel com a tabela de pacientes.
     * @return O painel contendo a tabela.
     */
    private JScrollPane criarTabelaPacientes() {
        String[] colunas = {"CPF", "Nome", "Idade", "Telefone", "Endereço"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                carregarPacienteSelecionado();
            }
        });
        
        return new JScrollPane(table);
    }
    
    /**
     * Cria o painel inferior com os botões de ação principais (Novo, Salvar, etc.).
     *
     * Este método utiliza Expressões Lambda (sintaxe 'e -> ...') para definir
     * as ações dos botões. Uma expressão lambda é uma forma compacta de
     * representar uma função anônima, substituindo a necessidade de criar uma
     * classe 'ActionListener' completa para cada botão.
     *
     * A sintaxe 'e -> dispose()' significa "ao receber um evento 'e', execute a
     * ação 'dispose()'", tornando o código mais conciso e legível.
     *
     * @return O painel de botões configurado.
     */
    private JPanel criarPainelDeBotoes() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setBackground(LIGHT_GRAY);

        buttonPanel.add(criarBotaoAcao("Novo Paciente", SUCCESS_GREEN, e -> limparFormulario()));
        buttonPanel.add(criarBotaoAcao("Salvar", PRIMARY_BLUE, e -> salvarPaciente()));
        buttonPanel.add(criarBotaoAcao("Excluir", ACCENT_RED, e -> excluirPaciente()));
        buttonPanel.add(criarBotaoAcao("Fechar", DARK_TEXT, e -> dispose()));
        
        return buttonPanel;
    }
    
    /**
     * Busca pacientes no banco de dados com base no termo digitado e popula a tabela.
     * Interage com a classe PacienteDAO.
     */
    private void buscarPacientes() {
        String termoBusca = txtBusca.getText().trim().toLowerCase();
        if (termoBusca.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite um termo para buscar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<Paciente> todosPacientes = pacienteDAO.findAll();
            List<Paciente> pacientesFiltrados = todosPacientes.stream()
                .filter(p -> p.getNome().toLowerCase().contains(termoBusca) || p.getCpf().contains(termoBusca.replaceAll("[^0-9]", "")))
                .collect(Collectors.toList());
            
            popularTabela(pacientesFiltrados);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao buscar pacientes: " + e.getMessage(), "Erro de Sistema", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Carrega todos os pacientes do banco de dados e popula a tabela.
     * Interage com a classe PacienteDAO.
     */
    private void carregarTodosPacientes() {
        try {
            List<Paciente> pacientes = pacienteDAO.findAll();
            popularTabela(pacientes);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar pacientes: " + e.getMessage(), "Erro de Carregamento", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Preenche a tabela com uma lista de pacientes.
     * @param pacientes A lista de pacientes a ser exibida.
     */
    private void popularTabela(List<Paciente> pacientes) {
        tableModel.setRowCount(0);
        
        if (pacientes != null && !pacientes.isEmpty()) {
            for (Paciente paciente : pacientes) {
                Object[] row = {
                    ValidadorCPF.formatar(paciente.getCpf()),
                    paciente.getNome(),
                    paciente.getIdade() + " anos",
                    paciente.getTelefone(),
                    paciente.getEndereco()
                };
                tableModel.addRow(row);
            }
        }
    }

    /**
     * Carrega os dados de um paciente selecionado na tabela para o formulário.
     * Interage com a classe PacienteDAO.
     */
    private void carregarPacienteSelecionado() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                String cpfFormatado = (String) tableModel.getValueAt(selectedRow, 0);
                String cpf = cpfFormatado.replaceAll("[^0-9]", "");
                
                pacienteSelecionado = pacienteDAO.findById(cpf);
                if (pacienteSelecionado != null) {
                    preencherFormularioComDados(pacienteSelecionado);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar paciente: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Preenche os campos do formulário com os dados de um objeto Paciente.
     * @param paciente O paciente cujos dados serão exibidos.
     */
    private void preencherFormularioComDados(Paciente paciente) {
        txtCpf.setText(paciente.getCpf());
        txtCpf.setEditable(false);
        txtNome.setText(paciente.getNome());
        txtDataNascimento.setText(paciente.getDataNascimento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtEndereco.setText(paciente.getEndereco());
        txtTelefone.setText(paciente.getTelefone());
        txtHistorico.setText(paciente.getHistoricoMedico());
    }

    /**
     * Limpa o formulário e a seleção da tabela, preparando para um novo cadastro.
     */
    private void limparFormulario() {
        txtCpf.setText("");
        txtCpf.setEditable(true);
        txtNome.setText("");
        txtDataNascimento.setText("");
        txtEndereco.setText("");
        txtTelefone.setText("");
        txtHistorico.setText("");
        
        pacienteSelecionado = null;
        table.clearSelection();
        txtCpf.requestFocus();
    }

    /**
     * Valida os dados do formulário e salva um novo paciente ou atualiza um existente.
     * Interage com as classes ValidadorCPF e PacienteDAO.
     */
    private void salvarPaciente() {
        try {
            String cpf = txtCpf.getText().replaceAll("[^0-9]", "");
            if (!ValidadorCPF.validar(cpf)) {
                JOptionPane.showMessageDialog(this, "CPF inválido!", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (txtNome.getText().trim().isEmpty() || txtDataNascimento.getText().replaceAll("[_/]", "").isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nome e Data de Nascimento são obrigatórios.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LocalDate dataNascimento;
            try {
                dataNascimento = LocalDate.parse(txtDataNascimento.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Data de Nascimento inválida! Use o formato dd/MM/yyyy.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Paciente paciente = new Paciente(
                cpf,
                txtNome.getText().trim(),
                dataNascimento,
                txtEndereco.getText().trim(),
                txtTelefone.getText().trim(),
                txtHistorico.getText().trim()
            );

            if (pacienteSelecionado == null) {
                pacienteDAO.save(paciente);
                JOptionPane.showMessageDialog(this, "Paciente cadastrado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                pacienteDAO.update(paciente);
                JOptionPane.showMessageDialog(this, "Paciente atualizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }
            
            limparFormulario();
            carregarTodosPacientes();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar paciente: " + e.getMessage(), "Erro de Sistema", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Exclui o paciente selecionado na tabela.
     * Interage com a classe PacienteDAO.
     */
    private void excluirPaciente() {
        if (pacienteSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um paciente para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Deseja realmente excluir o paciente " + pacienteSelecionado.getNome() + "?", "Confirmar Exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                pacienteDAO.delete(pacienteSelecionado.getCpf());
                JOptionPane.showMessageDialog(this, "Paciente excluído com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                limparFormulario();
                carregarTodosPacientes();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir paciente: " + e.getMessage(), "Erro de Sistema", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Fábrica de botões para criar instâncias de RoundedButton com estilo padrão.
     * @param texto O texto do botão.
     * @param cor A cor de fundo do botão.
     * @param acao O ActionListener para o clique.
     * @return Uma instância de RoundedButton.
     */
    private RoundedButton criarBotaoAcao(String texto, Color cor, ActionListener acao) {
        RoundedButton botao = new RoundedButton(texto);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 14));
        botao.setForeground(CLEAN_WHITE);
        botao.setBackground(cor);
        botao.setPreferredSize(new Dimension(140, 40));
        botao.addActionListener(acao);
        return botao;
    }

    /**
     * Classe interna para criar botões com cantos arredondados.
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
            if (bg != null) {
                hoverBackgroundColor = bg.brighter();
                pressedBackgroundColor = bg.darker();
            }
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