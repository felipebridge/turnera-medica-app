package UI;

import Exceptions.DAOException;
import Exceptions.NotFoundException;
import Exceptions.ValidationException;
import Modelo.Usuario;
import Modelo.UsuarioSimple;
import Service.UsuarioService;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UIUsuario extends JFrame {

    private final UsuarioService usuarioService = new UsuarioService();

    private JTable tabla;
    private DefaultTableModel modeloTabla;

    private JTextField txtId;
    private JComboBox<String> cmbTipo;
    private JCheckBox chkActivo;

    private JButton btnNuevo, btnGuardar, btnEliminar, btnRefrescar;

    public UIUsuario() {
        setTitle("Usuarios");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(720, 450);
        setLocationRelativeTo(null);

        initUI();
        cargarTabla();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // Tabla
        modeloTabla = new DefaultTableModel(new Object[]{"ID", "Tipo", "Activo"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(this::onSeleccionTabla);

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Formulario
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Datos del Usuario"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtId = new JTextField(8);
        txtId.setEnabled(false);

        cmbTipo = new JComboBox<>(new String[]{"MEDICO", "PACIENTE", "ADMIN"});
        chkActivo = new JCheckBox("Activo", true);

        int y = 0;

        // ID
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; form.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = y; gbc.weightx = 1; form.add(txtId, gbc);
        y++;

        // Tipo
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; form.add(new JLabel("Tipo:"), gbc);
        gbc.gridx = 1; gbc.gridy = y; gbc.weightx = 1; form.add(cmbTipo, gbc);
        y++;

        // Activo
        gbc.gridx = 1; gbc.gridy = y; gbc.weightx = 1; form.add(chkActivo, gbc);
        y++;

        // Botonera
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        btnNuevo = new JButton("Nuevo");
        btnGuardar = new JButton("Guardar");
        btnEliminar = new JButton("Eliminar");
        btnRefrescar = new JButton("Refrescar");

        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        btnRefrescar.addActionListener(e -> cargarTabla());

        botones.add(btnNuevo);
        botones.add(btnGuardar);
        botones.add(btnEliminar);
        botones.add(btnRefrescar);

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(botones, BorderLayout.SOUTH);

        add(south, BorderLayout.SOUTH);
    }

    private void cargarTabla() {
        try {
            List<Usuario> lista = usuarioService.listar();
            modeloTabla.setRowCount(0);

            for (Usuario u : lista) {
                modeloTabla.addRow(new Object[]{
                        u.getId(),
                        u.getTipoUsuario(),
                        u.isActivo() ? "Sí" : "No"
                });
            }
        } catch (DAOException e) {
            mostrarError("Error cargando usuarios", e);
        }
    }

    private void onSeleccionTabla(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int row = tabla.getSelectedRow();
        if (row < 0) return;

        int id = (int) modeloTabla.getValueAt(row, 0);

        try {
            Usuario u = usuarioService.buscarPorId(id);
            cargarFormulario(u);
        } catch (ValidationException | NotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Atención", JOptionPane.WARNING_MESSAGE);
        } catch (DAOException ex) {
            mostrarError("Error al cargar usuario", ex);
        }
    }

    private void cargarFormulario(Usuario u) {
        txtId.setText(String.valueOf(u.getId()));
        cmbTipo.setSelectedItem(u.getTipoUsuario());
        chkActivo.setSelected(u.isActivo());
    }

    private void limpiarFormulario() {
        tabla.clearSelection();
        txtId.setText("");
        cmbTipo.setSelectedIndex(0);
        chkActivo.setSelected(true);
        cmbTipo.requestFocus();
    }

    private void guardar() {
        try {
            String tipo = (String) cmbTipo.getSelectedItem();
            boolean activo = chkActivo.isSelected();

            if (tipo == null || tipo.trim().isEmpty()) {
                throw new ValidationException("Tipo de usuario obligatorio.");
            }

            if (txtId.getText().isBlank()) {
                // CREATE
                UsuarioSimple u = new UsuarioSimple(tipo, activo);
                int id = usuarioService.crear(u);
                JOptionPane.showMessageDialog(this, "Usuario creado con ID: " + id, "Información", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // UPDATE
                int id = Integer.parseInt(txtId.getText().trim());
                UsuarioSimple u = new UsuarioSimple(id, tipo, activo);
                usuarioService.actualizar(u);
                JOptionPane.showMessageDialog(this, "Usuario actualizado.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }

            limpiarFormulario();
            cargarTabla();

        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (NotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No encontrado", JOptionPane.WARNING_MESSAGE);
            cargarTabla();
        } catch (DAOException ex) {
            mostrarError("Error guardando usuario", ex);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID inválido.", "Validación", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminar() {
        try {
            if (txtId.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Seleccioná un usuario para eliminar.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id = Integer.parseInt(txtId.getText().trim());

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Eliminar el usuario ID " + id + "?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            usuarioService.eliminar(id);
            JOptionPane.showMessageDialog(this, "Usuario eliminado.", "Información", JOptionPane.INFORMATION_MESSAGE);

            limpiarFormulario();
            cargarTabla();

        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (NotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No encontrado", JOptionPane.WARNING_MESSAGE);
            cargarTabla();
        } catch (DAOException ex) {
            mostrarError("Error eliminando usuario", ex);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID inválido.", "Validación", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void mostrarError(String titulo, Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, titulo + ":\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
