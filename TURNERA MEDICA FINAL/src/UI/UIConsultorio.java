package UI;

import Exceptions.DAOException;
import Exceptions.NotFoundException;
import Exceptions.ValidationException;
import Modelo.Consultorio;
import Service.ConsultorioService;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UIConsultorio extends JFrame {

    private final ConsultorioService service = new ConsultorioService();

    private JTable tabla;
    private DefaultTableModel modeloTabla;

    private JTextField txtId;
    private JTextField txtNumero;
    private JTextField txtPiso;
    private JTextField txtDescripcion;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnEliminar;
    private JButton btnRefrescar;

    public UIConsultorio() {
        setTitle("Consultorios");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(760, 460);
        setLocationRelativeTo(null);

        initUI();
        cargarTabla();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // Tabla
        modeloTabla = new DefaultTableModel(new Object[]{"ID", "Número", "Piso", "Descripción"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(this::onSeleccionTabla);

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Formulario
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Datos de Consultorio"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblId = new JLabel("ID:");
        JLabel lblNumero = new JLabel("Número:");
        JLabel lblPiso = new JLabel("Piso:");
        JLabel lblDesc = new JLabel("Descripción:");

        txtId = new JTextField(10);
        txtId.setEnabled(false);

        txtNumero = new JTextField(20);
        txtPiso = new JTextField(10);
        txtDescripcion = new JTextField(35);

        gbc.gridx = 0; gbc.gridy = 0; form.add(lblId, gbc);
        gbc.gridx = 1; gbc.gridy = 0; form.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy = 1; form.add(lblNumero, gbc);
        gbc.gridx = 1; gbc.gridy = 1; form.add(txtNumero, gbc);

        gbc.gridx = 0; gbc.gridy = 2; form.add(lblPiso, gbc);
        gbc.gridx = 1; gbc.gridy = 2; form.add(txtPiso, gbc);

        gbc.gridx = 0; gbc.gridy = 3; form.add(lblDesc, gbc);
        gbc.gridx = 1; gbc.gridy = 3; form.add(txtDescripcion, gbc);

        //Botonera
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
            List<Consultorio> lista = service.listar();
            modeloTabla.setRowCount(0);

            for (Consultorio c : lista) {
                modeloTabla.addRow(new Object[]{
                        c.getId(),
                        c.getNumero(),
                        c.getPiso(),
                        c.getDescripcion()
                });
            }
        } catch (DAOException e) {
            mostrarError("Error cargando consultorios", e);
        }
    }

    private void onSeleccionTabla(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int row = tabla.getSelectedRow();
        if (row < 0) return;

        txtId.setText(String.valueOf(modeloTabla.getValueAt(row, 0)));
        txtNumero.setText(String.valueOf(modeloTabla.getValueAt(row, 1)));
        txtPiso.setText(valueOrEmpty(modeloTabla.getValueAt(row, 2)));
        txtDescripcion.setText(valueOrEmpty(modeloTabla.getValueAt(row, 3)));
    }

    private String valueOrEmpty(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private void limpiarFormulario() {
        tabla.clearSelection();
        txtId.setText("");
        txtNumero.setText("");
        txtPiso.setText("");
        txtDescripcion.setText("");
        txtNumero.requestFocus();
    }

    private void guardar() {
        try {
            String numero = txtNumero.getText().trim();
            String piso = txtPiso.getText().trim();
            String descripcion = txtDescripcion.getText().trim();

            if (piso.isBlank()) piso = null;
            if (descripcion.isBlank()) descripcion = null;

            if (txtId.getText().isBlank()) {
                // CREATE
                Consultorio c = new Consultorio(numero, piso, descripcion);
                int id = service.crear(c);
                JOptionPane.showMessageDialog(this, "Creado con ID: " + id);
            } else {
                // UPDATE
                int id = Integer.parseInt(txtId.getText().trim());
                Consultorio c = new Consultorio(id, numero, piso, descripcion);
                service.actualizar(c);
                JOptionPane.showMessageDialog(this, "Actualizado correctamente.");
            }

            limpiarFormulario();
            cargarTabla();

        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (NotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No encontrado", JOptionPane.WARNING_MESSAGE);
            cargarTabla();
        } catch (DAOException ex) {
            mostrarError("Error guardando consultorio", ex);
        }
    }

    private void eliminar() {
        try {
            if (txtId.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Seleccioná un consultorio para eliminar.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id = Integer.parseInt(txtId.getText().trim());

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Eliminar el consultorio ID " + id + "?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) return;

            service.eliminar(id);
            JOptionPane.showMessageDialog(this, "Eliminado correctamente.");

            limpiarFormulario();
            cargarTabla();

        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (NotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No encontrado", JOptionPane.WARNING_MESSAGE);
            cargarTabla();
        } catch (DAOException ex) {
            mostrarError("Error eliminando consultorio", ex);
        }
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
