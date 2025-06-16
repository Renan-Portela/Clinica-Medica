package br.com.clinica.view;

import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.model.Paciente;
import br.com.clinica.util.ValidadorCPF;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;
import java.text.ParseException;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TelaPacientes extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private JTable table;
    private DefaultTableModel tableModel;
    private PacienteDAO pacienteDAO;
    
    // Campos formulario
    private JTextField txtCpf;
    private JTextField txtNome;
    private JTextField txtDataNascimento;
    private JTextField txtEndereco;
    private JTextField txtTelefone;
    private JTextArea txtHistorico;
    
    private Paciente pacienteSelecionado = null;
    
    public TelaPacientes() {
        this.pacienteDAO = new PacienteDAO();
        initComponents();
        carregarPacientes();
    }
    
    private void initComponents() {
        setTitle("Gerenciamento de Pacientes");
        setSize(900, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        getContentPane().add(mainPanel);
        
        // Painel formulario
        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel, BorderLayout.NORTH);
        
        // Painel tabela
        JPanel tablePanel = createTablePanel();
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        
        // Painel botoes
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setPreferredSize(new Dimension(0, 200));
        panel.setBorder(BorderFactory.createTitledBorder("Dados do Paciente"));
        
        // CPF
        JLabel lblCpf = new JLabel("CPF:");
        lblCpf.setBounds(20, 30, 80, 25);
        panel.add(lblCpf);
        
        txtCpf = new JTextField();
        try {
            MaskFormatter cpfMask = new MaskFormatter("###.###.###-##");
            cpfMask.setPlaceholderCharacter('_');
            txtCpf = new JFormattedTextField(cpfMask);
        } catch (ParseException e) {
            txtCpf = new JTextField(); // fallback
        }
        txtCpf.setBounds(100, 30, 150, 25);
        panel.add(txtCpf);
        
        // Nome
        JLabel lblNome = new JLabel("Nome:");
        lblNome.setBounds(280, 30, 80, 25);
        panel.add(lblNome);
        
        txtNome = new JTextField();
        txtNome.setBounds(360, 30, 250, 25);
        panel.add(txtNome);
        
        // Data Nascimento
        JLabel lblData = new JLabel("Data Nasc:");
        lblData.setBounds(640, 30, 80, 25);
        panel.add(lblData);
        
        txtDataNascimento = new JTextField();
        try {
            MaskFormatter dataMask = new MaskFormatter("##/##/####");
            dataMask.setPlaceholderCharacter('_');
            txtDataNascimento = new JFormattedTextField(dataMask);
        } catch (ParseException e) {
            txtDataNascimento = new JTextField(); // fallback
        }
        txtDataNascimento.setBounds(720, 30, 100, 25);
        txtDataNascimento.setToolTipText("dd/MM/yyyy");
        panel.add(txtDataNascimento);
        
        // Endereco
        JLabel lblEndereco = new JLabel("Endereco:");
        lblEndereco.setBounds(20, 70, 80, 25);
        panel.add(lblEndereco);
        
        txtEndereco = new JTextField();
        txtEndereco.setBounds(100, 70, 300, 25);
        panel.add(txtEndereco);
        
        // Telefone
        JLabel lblTelefone = new JLabel("Telefone:");
        lblTelefone.setBounds(420, 70, 80, 25);
        panel.add(lblTelefone);
        
        txtTelefone = new JTextField();
        try {
            MaskFormatter telMask = new MaskFormatter("(##) #####-####");
            telMask.setPlaceholderCharacter('_');
            txtTelefone = new JFormattedTextField(telMask);
        } catch (ParseException e) {
            txtTelefone = new JTextField(); // fallback
        }

        txtTelefone.setBounds(500, 70, 150, 25);
        panel.add(txtTelefone);
        
        // Historico
        JLabel lblHistorico = new JLabel("Historico:");
        lblHistorico.setBounds(20, 110, 80, 25);
        panel.add(lblHistorico);
        
        txtHistorico = new JTextArea();
        txtHistorico.setLineWrap(true);
        JScrollPane scrollHistorico = new JScrollPane(txtHistorico);
        scrollHistorico.setBounds(100, 110, 720, 60);
        panel.add(scrollHistorico);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Lista de Pacientes"));
        
        String[] colunas = {"CPF", "Nome", "Idade", "Telefone", "Endereco"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(tableModel);
        table.setRowSelectionAllowed(false);
        table.setFont(new Font("Dialog", Font.PLAIN, 15));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                carregarPacienteSelecionado();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        
        JButton btnNovo = new JButton("Novo");
        btnNovo.addActionListener(e -> limparFormulario());
        panel.add(btnNovo);
        
        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.addActionListener(e -> salvarPaciente());
        panel.add(btnSalvar);
        
        JButton btnExcluir = new JButton("Excluir");
        btnExcluir.addActionListener(e -> excluirPaciente());
        panel.add(btnExcluir);
        
        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dispose());
        panel.add(btnFechar);
        
        return panel;
    }
    
    private void carregarPacientes() {
        try {
            List<Paciente> pacientes = pacienteDAO.findAll();
            tableModel.setRowCount(0); // Limpar tabela
            
            if (pacientes != null && !pacientes.isEmpty()) {
                for (Paciente paciente : pacientes) {
                    // Verificar se dados essenciais não são nulos
                    String cpfFormatado = paciente.getCpf() != null ? 
                        ValidadorCPF.formatar(paciente.getCpf()) : "N/A";
                    String nome = paciente.getNome() != null ? paciente.getNome() : "N/A";
                    String idade = paciente.getIdade() + " anos";
                    String telefone = paciente.getTelefone() != null ? paciente.getTelefone() : "N/A";
                    String endereco = paciente.getEndereco() != null ? paciente.getEndereco() : "N/A";
                    
                    Object[] row = {cpfFormatado, nome, idade, telefone, endereco};
                    tableModel.addRow(row);
                }
                System.out.println("Carregados " + pacientes.size() + " pacientes");
            } else {
                System.out.println("Nenhum paciente encontrado");
            }
            
        } catch (Exception e) {
            System.err.println("Erro detalhado ao carregar pacientes: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Erro ao carregar pacientes: " + e.getMessage() + 
                "\nVerifique se o banco de dados esta conectado.",
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void carregarPacienteSelecionado() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                String cpfFormatado = (String) tableModel.getValueAt(selectedRow, 0);
                String cpf = cpfFormatado.replaceAll("[^0-9]", "");
                
                pacienteSelecionado = pacienteDAO.findById(cpf);
                if (pacienteSelecionado != null) {
                    preencherFormulario(pacienteSelecionado);
                }
            } catch (Exception e) {
                System.err.println("Erro ao carregar paciente selecionado: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Erro ao carregar paciente: " + e.getMessage());
            }
        }
    }
    
    private void preencherFormulario(Paciente paciente) {
        txtCpf.setText(ValidadorCPF.formatar(paciente.getCpf()));
        txtNome.setText(paciente.getNome());
        txtDataNascimento.setText(paciente.getDataNascimento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtEndereco.setText(paciente.getEndereco() != null ? paciente.getEndereco() : "");
        txtTelefone.setText(paciente.getTelefone() != null ? paciente.getTelefone() : "");
        txtHistorico.setText(paciente.getHistoricoMedico() != null ? paciente.getHistoricoMedico() : "");
    }
    
    private void limparFormulario() {
        txtCpf.setText("");
        txtNome.setText("");
        txtDataNascimento.setText("");
        txtEndereco.setText("");
        txtTelefone.setText("");
        txtHistorico.setText("");
        
        pacienteSelecionado = null;
        table.clearSelection();
    }
    
    private void salvarPaciente() {
        try {
            if (txtCpf.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "CPF e obrigatorio!");
                return;
            }
            
            String cpfTexto = txtCpf.getText();
            String cpf = cpfTexto != null ? cpfTexto.replaceAll("[^0-9]", "") : "";
            if (!ValidadorCPF.validar(cpf)) {
                JOptionPane.showMessageDialog(this, "CPF invalido!");
                return;
            }
            
            if (txtNome.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nome e obrigatorio!");
                return;
            }
            
            if (txtDataNascimento.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Data nascimento e obrigatoria!");
                return;
            }
            
            // Parsear data
            LocalDate dataNascimento;
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                dataNascimento = LocalDate.parse(txtDataNascimento.getText().trim(), formatter);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Data invalida! Use formato dd/MM/yyyy");
                return;
            }
            
            // Criar paciente
            Paciente paciente = new Paciente();
            paciente.setCpf(cpf);
            paciente.setNome(txtNome.getText().trim());
            paciente.setDataNascimento(dataNascimento);
            paciente.setEndereco(txtEndereco.getText().trim());
            paciente.setTelefone(txtTelefone.getText().trim());
            paciente.setHistoricoMedico(txtHistorico.getText().trim());
            
            if (pacienteSelecionado == null) {
                pacienteDAO.save(paciente);
                JOptionPane.showMessageDialog(this, "Paciente cadastrado com sucesso!");
            } else {
                pacienteDAO.update(paciente);
                JOptionPane.showMessageDialog(this, "Paciente atualizado com sucesso!");
            }
            
            carregarPacientes();
            limparFormulario();
            
        } catch (Exception e) {
            System.err.println("Erro ao salvar paciente: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao salvar paciente: " + e.getMessage());
        }
    }
    
    private void excluirPaciente() {
        if (pacienteSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um paciente para excluir!");
            return;
        }
        
        int opcao = JOptionPane.showConfirmDialog(
            this,
            "Deseja realmente excluir o paciente " + pacienteSelecionado.getNome() + "?",
            "Confirmar Exclusao",
            JOptionPane.YES_NO_OPTION
        );
        
        if (opcao == JOptionPane.YES_OPTION) {
            try {
                pacienteDAO.delete(pacienteSelecionado.getCpf());
                JOptionPane.showMessageDialog(this, "Paciente excluido com sucesso!");
                carregarPacientes();
                limparFormulario();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir paciente: " + e.getMessage());
            }
        }
    }
}
