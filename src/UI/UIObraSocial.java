package UI;

import Exceptions.DAOException;
import Exceptions.NotFoundException;
import Exceptions.ValidationException;
import Modelo.ObraSocial;
import Service.ObraSocialService;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UIObraSocial extends JFrame {

    private final ObraSocialService service = new ObraSocialService();

    private JTable tabla;
    private DefaultTableModel modeloTabla;

    private JTextField txtId;
    private JTextField txtNombre;
    private JTextField txtDescuento;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnEliminar;
    private JButton btnRefrescar;

    public UIObraSocial() {
        setTitle("Obras Sociales");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 420);
        setLocationRelativeTo(null);

        initUI();
        cargarTabla();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // Tabla
        modeloTabla = new DefaultTableModel(new Object[]{"ID", "Nombre", "Descuento (%)"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(this::onSeleccionTabla);

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Formulario
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Datos de Obra Social"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblId = new JLabel("ID:");
        JLabel lblNombre = new JLabel("Nombre:");
        JLabel lblDescuento = new JLabel("Descuento (%):");

        txtId = new JTextField(10);
        txtId.setEnabled(false);

        txtNombre = new JTextField(25);
        txtDescuento = new JTextField(10);

        gbc.gridx = 0; gbc.gridy = 0; form.add(lblId, gbc);
        gbc.gridx = 1; gbc.gridy = 0; form.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy = 1; form.add(lblNombre, gbc);
        gbc.gridx = 1; gbc.gridy = 1; form.add(txtNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 2; form.add(lblDescuento, gbc);
        gbc.gridx = 1; gbc.gridy = 2; form.add(txtDescuento, gbc);

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
            List<ObraSocial> lista = service.listar();
            modeloTabla.setRowCount(0);

            for (ObraSocial os : lista) {
                modeloTabla.addRow(new Object[]{
                        os.getId(),
                        os.getNombre(),
                        os.getDescuento()
                });
            }

        } catch (DAOException e) {
            mostrarError("Error cargando Obras Sociales", e);
        }
    }

    private void onSeleccionTabla(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int row = tabla.getSelectedRow();
        if (row < 0) return;

        txtId.setText(String.valueOf(modeloTabla.getValueAt(row, 0)));
        txtNombre.setText(String.valueOf(modeloTabla.getValueAt(row, 1)));
        txtDescuento.setText(String.valueOf(modeloTabla.getValueAt(row, 2)));
    }

    private void limpiarFormulario() {
        tabla.clearSelection();
        txtId.setText("");
        txtNombre.setText("");
        txtDescuento.setText("");
        txtNombre.requestFocus();
    }

    private void guardar() {
        try {
            String nombre = txtNombre.getText().trim();
            double descuento = parseDescuento(txtDescuento.getText().trim());

            if (txtId.getText().isBlank()) {
                // CREATE
                ObraSocial os = new ObraSocial(nombre, descuento);
                int id = service.crear(os);
                JOptionPane.showMessageDialog(this, "Creada con ID: " + id);
            } else {
                // UPDATE
                int id = Integer.parseInt(txtId.getText().trim());
                ObraSocial os = new ObraSocial(id, nombre, descuento);
                service.actualizar(os);
                JOptionPane.showMessageDialog(this, "Actualizada correctamente.");
            }

            limpiarFormulario();
            cargarTabla();

        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (NotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No encontrado", JOptionPane.WARNING_MESSAGE);
            cargarTabla();
        } catch (DAOException ex) {
            mostrarError("Error guardando Obra Social", ex);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Descuento inválido. Ej: 40 o 40.5", "Validación", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminar() {
        try {
            if (txtId.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Seleccioná una obra social para eliminar.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id = Integer.parseInt(txtId.getText().trim());

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Eliminar la obra social ID " + id + "?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) return;

            service.eliminar(id);
            JOptionPane.showMessageDialog(this, "Eliminada correctamente.");

            limpiarFormulario();
            cargarTabla();

        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (NotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No encontrado", JOptionPane.WARNING_MESSAGE);
            cargarTabla();
        } catch (DAOException ex) {
            mostrarError("Error eliminando Obra Social", ex);
        }
    }

    private double parseDescuento(String value) {
        String normalizado = value.replace(",", ".");
        return Double.parseDouble(normalizado);
    }

    private void mostrarError(String titulo, Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(
                this,
                titulo + ":\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
