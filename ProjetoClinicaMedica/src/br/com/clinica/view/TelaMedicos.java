package br.com.clinica.view;

import br.com.clinica.dao.MedicoDAO;
import br.com.clinica.model.Medico;
import br.com.clinica.util.ValidadorCRM;

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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gerencia o Cadastro, Leitura, Atualização e Exclusão (CRUD) de médicos.
 * A tela é iniciada com uma tabela vazia, exigindo que o usuário realize uma
 * busca ou liste todos os médicos para visualizar os dados. A interface
 * segue o padrão visual moderno da aplicação.
 * Interage com as classes: MedicoDAO, Medico, ValidadorCRM.
 */
public class TelaMedicos extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    // Paleta de cores padrão do sistema
    private static final Color PRIMARY_BLUE = new Color(52, 144, 220);
    private static final Color SUCCESS_GREEN = new Color(40, 167, 69);
    private static final Color ACCENT_RED = new Color(220, 53, 69);
    private static final Color CLEAN_WHITE = new Color(255, 255, 255);
    private static final Color LIGHT_GRAY = new Color(248, 249, 250);
    private static final Color DARK_TEXT = new Color(52, 58, 64);
    
    // Componentes da interface
    private JTable table;
    private DefaultTableModel tableModel;
    
    // Campos do formulário
    private JTextField txtCrm;
    private JTextField txtNome;
    private JComboBox<String> cbEspecialidade;
    private JTextField txtSala;
    private JSpinner spinnerInicio;
    private JSpinner spinnerFim;
    private JCheckBox[] checkDias;
    
    // Campos de busca
    private JTextField txtBusca;

    // Controle de estado e persistência
    private Medico medicoSelecionado = null;
    private MedicoDAO medicoDAO;
    
    /**
     * Construtor da tela de médicos. Inicializa o DAO e a interface gráfica.
     */
    public TelaMedicos() {
        this.medicoDAO = new MedicoDAO();
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
        centerPanel.add(criarTabelaMedicos(), BorderLayout.CENTER);

        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(centerPanel, BorderLayout.CENTER);
        getContentPane().add(criarPainelDeBotoes(), BorderLayout.SOUTH);
    }
    
    /**
     * Configura as propriedades principais da janela (JFrame).
     */
    private void configurarJanela() {
        setTitle("Gerenciamento de Médicos - Sistema Clínica");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel contentPane = new JPanel(new BorderLayout());
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
        
        JLabel lblTitulo = new JLabel("GERENCIAMENTO DE MÉDICOS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(CLEAN_WHITE);
        
        headerPanel.add(lblTitulo, BorderLayout.WEST);
        return headerPanel;
    }

    /**
     * Cria o painel do formulário para entrada de dados do médico.
     * @return O painel do formulário.
     */
    private JPanel criarFormulario() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CLEAN_WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_BLUE, 1), 
                "Dados do Médico", 0, 0, new Font("Segoe UI", Font.BOLD, 16), PRIMARY_BLUE),
            new EmptyBorder(15, 25, 15, 25)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Linha 1
        gbc.gridy = 0;
        gbc.gridx = 0;
        formPanel.add(criarCampo("CRM:", txtCrm = criarCampoTextoFormatado("CRM######")), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(criarCampo("Nome Completo:", txtNome = new JTextField(30)), gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        String[] especialidades = {"Cardiologia", "Pediatria", "Ortopedia", "Dermatologia", "Ginecologia", "Neurologia", "Oftalmologia", "Psiquiatria", "Urologia", "Endocrinologia"};
        formPanel.add(criarCampo("Especialidade:", cbEspecialidade = new JComboBox<>(especialidades)), gbc);
        cbEspecialidade.setEditable(true);
        
        // Linha 2
        gbc.gridy = 1;
        gbc.gridx = 0;
        formPanel.add(criarCampo("Sala:", txtSala = new JTextField(10)), gbc);
        gbc.gridx = 1;
        formPanel.add(criarCampoHorario(), gbc);
        gbc.gridx = 2;
        formPanel.add(criarCampoDiasAtendimento(), gbc);
        
        return formPanel;
    }
    
    /**
     * Cria um painel de campo de formulário com um rótulo e um componente.
     * @param label O texto do rótulo.
     * @param component O componente de entrada de dados.
     * @return Um painel contendo o rótulo e o componente.
     */
    private JPanel criarCampo(String label, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(lbl, BorderLayout.NORTH);
        component.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Cria um painel composto para os campos de horário de início e fim.
     * @return O painel de horários.
     */
    private JPanel criarCampoHorario() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setOpaque(false);

        SpinnerDateModel modelInicio = new SpinnerDateModel();
        spinnerInicio = new JSpinner(modelInicio);
        spinnerInicio.setEditor(new JSpinner.DateEditor(spinnerInicio, "HH:mm"));
        spinnerInicio.setValue(java.sql.Time.valueOf(LocalTime.of(8, 0)));
        
        SpinnerDateModel modelFim = new SpinnerDateModel();
        spinnerFim = new JSpinner(modelFim);
        spinnerFim.setEditor(new JSpinner.DateEditor(spinnerFim, "HH:mm"));
        spinnerFim.setValue(java.sql.Time.valueOf(LocalTime.of(17, 0)));

        panel.add(criarCampo("Início:", spinnerInicio));
        panel.add(criarCampo("Fim:", spinnerFim));
        return panel;
    }
    
    /**
     * Cria um painel com checkboxes para os dias de atendimento.
     * @return O painel dos dias de atendimento.
     */
    private JPanel criarCampoDiasAtendimento() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);
        JLabel lbl = new JLabel("Dias de Atendimento:");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(lbl, BorderLayout.NORTH);

        JPanel diasPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        diasPanel.setOpaque(false);
        String[] dias = {"Seg", "Ter", "Qua", "Qui", "Sex", "Sab", "Dom"};
        checkDias = new JCheckBox[7];
        for (int i = 0; i < dias.length; i++) {
            checkDias[i] = new JCheckBox(dias[i]);
            checkDias[i].setOpaque(false);
            if (i < 5) checkDias[i].setSelected(true);
            diasPanel.add(checkDias[i]);
        }
        panel.add(diasPanel, BorderLayout.CENTER);
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
        
        buscaPanel.add(new JLabel("Buscar por Nome ou CRM:"));
        txtBusca = new JTextField(30);
        buscaPanel.add(txtBusca);
        
        JButton btnBuscar = criarBotaoAcao("Buscar", SUCCESS_GREEN, e -> buscarMedicos());
        buscaPanel.add(btnBuscar);
        
        JButton btnListarTodos = criarBotaoAcao("Listar Todos", PRIMARY_BLUE, e -> carregarTodosMedicos());
        buscaPanel.add(btnListarTodos);

        return buscaPanel;
    }

    /**
     * Cria o painel com a tabela de médicos.
     * @return O painel contendo a tabela.
     */
    private JScrollPane criarTabelaMedicos() {
        String[] colunas = {"CRM", "Nome", "Especialidade", "Horário", "Sala"};
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
                carregarMedicoSelecionado();
            }
        });
        
        return new JScrollPane(table);
    }
    
    /**
     * Cria o painel inferior com os botões de ação principais (Novo, Salvar, etc.).
     * @return O painel de botões.
     */
    private JPanel criarPainelDeBotoes() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setBackground(LIGHT_GRAY);

        buttonPanel.add(criarBotaoAcao("Novo Médico", SUCCESS_GREEN, e -> limparFormulario()));
        buttonPanel.add(criarBotaoAcao("Salvar", PRIMARY_BLUE, e -> salvarMedico()));
        buttonPanel.add(criarBotaoAcao("Excluir", ACCENT_RED, e -> excluirMedico()));
        buttonPanel.add(criarBotaoAcao("Fechar", DARK_TEXT, e -> dispose()));
        
        return buttonPanel;
    }
    
    /**
     * Busca médicos no banco de dados com base no termo digitado e popula a tabela.
     */
    private void buscarMedicos() {
        String termoBusca = txtBusca.getText().trim().toLowerCase();
        if (termoBusca.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite um termo para buscar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<Medico> todosMedicos = medicoDAO.findAll();
            List<Medico> medicosFiltrados = todosMedicos.stream()
                .filter(m -> m.getNome().toLowerCase().contains(termoBusca) || m.getCrm().toLowerCase().contains(termoBusca))
                .collect(Collectors.toList());
            
            popularTabela(medicosFiltrados);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao buscar médicos: " + e.getMessage(), "Erro de Sistema", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Carrega todos os médicos do banco de dados e popula a tabela.
     */
    private void carregarTodosMedicos() {
        try {
            List<Medico> medicos = medicoDAO.findAll();
            popularTabela(medicos);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar médicos: " + e.getMessage(), "Erro de Carregamento", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Preenche a tabela com uma lista de médicos.
     * @param medicos A lista de médicos a ser exibida.
     */
    private void popularTabela(List<Medico> medicos) {
        tableModel.setRowCount(0);
        
        if (medicos != null && !medicos.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            for (Medico medico : medicos) {
                String horario = medico.getHorarioInicio().format(formatter) + " às " + medico.getHorarioFim().format(formatter);
                tableModel.addRow(new Object[]{
                    medico.getCrm(),
                    medico.getNome(),
                    medico.getEspecialidade(),
                    horario,
                    medico.getSalaAtendimento()
                });
            }
        }
    }

    /**
     * Carrega os dados de um médico selecionado na tabela para o formulário.
     */
    private void carregarMedicoSelecionado() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                String crm = (String) tableModel.getValueAt(selectedRow, 0);
                medicoSelecionado = medicoDAO.findById(crm);
                if (medicoSelecionado != null) {
                    preencherFormularioComDados(medicoSelecionado);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar médico: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Preenche os campos do formulário com os dados de um objeto Medico.
     * @param medico O médico cujos dados serão exibidos.
     */
    private void preencherFormularioComDados(Medico medico) {
        txtCrm.setText(medico.getCrm());
        txtCrm.setEditable(false);
        txtNome.setText(medico.getNome());
        cbEspecialidade.setSelectedItem(medico.getEspecialidade());
        txtSala.setText(medico.getSalaAtendimento());
        spinnerInicio.setValue(java.sql.Time.valueOf(medico.getHorarioInicio()));
        spinnerFim.setValue(java.sql.Time.valueOf(medico.getHorarioFim()));
        
        for (JCheckBox check : checkDias) check.setSelected(false);
        String[] diasCodigo = {"seg", "ter", "qua", "qui", "sex", "sab", "dom"};
        for (int i = 0; i < diasCodigo.length; i++) {
            if (medico.getDiasAtendimento().contains(diasCodigo[i])) {
                checkDias[i].setSelected(true);
            }
        }
    }

    /**
     * Limpa o formulário e a seleção da tabela, preparando para um novo cadastro.
     */
    private void limparFormulario() {
        txtCrm.setText("");
        txtCrm.setEditable(true);
        txtNome.setText("");
        cbEspecialidade.setSelectedIndex(0);
        txtSala.setText("");
        spinnerInicio.setValue(java.sql.Time.valueOf(LocalTime.of(8, 0)));
        spinnerFim.setValue(java.sql.Time.valueOf(LocalTime.of(17, 0)));
        for (int i = 0; i < checkDias.length; i++) {
            checkDias[i].setSelected(i < 5);
        }
        medicoSelecionado = null;
        table.clearSelection();
        txtCrm.requestFocus();
    }

    /**
     * Valida os dados do formulário e salva um novo médico ou atualiza um existente.
     */
    private void salvarMedico() {
        try {
            if (!ValidadorCRM.validar(txtCrm.getText().trim())) {
                JOptionPane.showMessageDialog(this, "CRM inválido! Formato esperado: CRM######", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (txtNome.getText().trim().isEmpty() || txtSala.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nome e Sala são obrigatórios.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            List<String> diasSelecionados = new ArrayList<>();
            String[] diasCodigo = {"seg", "ter", "qua", "qui", "sex", "sab", "dom"};
            for (int i = 0; i < checkDias.length; i++) {
                if (checkDias[i].isSelected()) diasSelecionados.add(diasCodigo[i]);
            }
            if (diasSelecionados.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Selecione ao menos um dia de atendimento.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Medico medico = new Medico();
            medico.setCrm(txtCrm.getText().trim().toUpperCase());
            medico.setNome(txtNome.getText().trim());
            medico.setEspecialidade(cbEspecialidade.getSelectedItem().toString());
            medico.setSalaAtendimento(txtSala.getText().trim());
            medico.setHorarioInicio(LocalTime.of(((java.util.Date) spinnerInicio.getValue()).getHours(), ((java.util.Date) spinnerInicio.getValue()).getMinutes()));
            medico.setHorarioFim(LocalTime.of(((java.util.Date) spinnerFim.getValue()).getHours(), ((java.util.Date) spinnerFim.getValue()).getMinutes()));
            medico.setDiasAtendimento(diasSelecionados);

            if (medicoSelecionado == null) {
                medicoDAO.save(medico);
                JOptionPane.showMessageDialog(this, "Médico cadastrado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                medicoDAO.update(medico);
                JOptionPane.showMessageDialog(this, "Médico atualizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }
            
            limparFormulario();
            carregarTodosMedicos();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar médico: " + e.getMessage(), "Erro de Sistema", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Exclui o médico selecionado na tabela.
     */
    private void excluirMedico() {
        if (medicoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um médico para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Deseja realmente excluir o médico " + medicoSelecionado.getNome() + "?", "Confirmar Exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                medicoDAO.delete(medicoSelecionado.getCrm());
                JOptionPane.showMessageDialog(this, "Médico excluído com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                limparFormulario();
                carregarTodosMedicos();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir médico: " + e.getMessage(), "Erro de Sistema", JOptionPane.ERROR_MESSAGE);
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