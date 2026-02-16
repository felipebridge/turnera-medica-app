package UI;

import Exceptions.DAOException;
import Exceptions.NotFoundException;
import Exceptions.ValidationException;
import Modelo.ObraSocial;
import Modelo.Paciente;
import Service.ObraSocialService;
import Service.PacienteService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import java.util.regex.Pattern;

public class UIPaciente extends JFrame {

    private final PacienteService pacienteService = new PacienteService();
    private final ObraSocialService obraSocialService = new ObraSocialService();

    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> sorter;

    // Barra de búsqueda
    private JTextField txtBuscar;

    private JTextField txtId;
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtDni;
    private JTextField txtTelefono;
    private JTextField txtEmail;
    private JComboBox<ObraSocial> cmbObraSocial;
    private JCheckBox chkActivo;

    private JButton btnNuevo, btnGuardar, btnEliminar, btnRefrescar;

    // Reglas de validación
    private static final int TEL_MIN_DIGITOS = 8;
    private static final int TEL_MAX_DIGITOS = 15;

    private static final Pattern SOLO_DIGITOS = Pattern.compile("^\\d+$");

    // Email razonable
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    public UIPaciente() {
        setTitle("Pacientes");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 560);
        setLocationRelativeTo(null);

        initUI();
        cargarComboObraSocial();
        cargarTabla();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // Búsqueda
        add(crearBarraBusqueda(), BorderLayout.NORTH);

        // Tabla
        modeloTabla = new DefaultTableModel(
                new Object[]{"ID", "Nombre", "Apellido", "DNI", "Teléfono", "Email", "Obra Social", "Activo"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(this::onSeleccionTabla);

        sorter = new TableRowSorter<>(modeloTabla);
        tabla.setRowSorter(sorter);

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Formulario
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Datos del Paciente"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtId = new JTextField(8);
        txtId.setEnabled(false);

        txtNombre = new JTextField(20);
        txtApellido = new JTextField(20);
        txtDni = new JTextField(20);
        txtTelefono = new JTextField(20);
        txtEmail = new JTextField(25);

        cmbObraSocial = new JComboBox<>();
        cmbObraSocial.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) setText("Particular (sin obra social)");
                else if (value instanceof ObraSocial os) setText(os.getId() + " - " + safe(os.getNombre()));
                return this;
            }
        });

        chkActivo = new JCheckBox("Activo", true);

        int y = 0;

        // ID
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; form.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = y; gbc.weightx = 1; form.add(txtId, gbc);
        y++;

        // Nombre (obligatorio)
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; form.add(new JLabel("Nombre (*):"), gbc);
        gbc.gridx = 1; gbc.gridy = y; gbc.weightx = 1; form.add(txtNombre, gbc);
        y++;

        // Apellido (obligatorio)
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; form.add(new JLabel("Apellido (*):"), gbc);
        gbc.gridx = 1; gbc.gridy = y; gbc.weightx = 1; form.add(txtApellido, gbc);
        y++;

        // DNI (obligatorio)
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; form.add(new JLabel("DNI (*):"), gbc);
        gbc.gridx = 1; gbc.gridy = y; gbc.weightx = 1; form.add(txtDni, gbc);
        y++;

        // Teléfono (obligatorio)
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; form.add(new JLabel("Teléfono (*):"), gbc);
        gbc.gridx = 1; gbc.gridy = y; gbc.weightx = 1; form.add(txtTelefono, gbc);
        y++;

        // Email (obligatorio)
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; form.add(new JLabel("Email (*):"), gbc);
        gbc.gridx = 1; gbc.gridy = y; gbc.weightx = 1; form.add(txtEmail, gbc);
        y++;

        // Obra Social
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; form.add(new JLabel("Obra Social:"), gbc);
        gbc.gridx = 1; gbc.gridy = y; gbc.weightx = 1; form.add(cmbObraSocial, gbc);
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
        btnRefrescar.addActionListener(e -> { cargarComboObraSocial(); cargarTabla(); });

        botones.add(btnNuevo);
        botones.add(btnGuardar);
        botones.add(btnEliminar);
        botones.add(btnRefrescar);

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(botones, BorderLayout.SOUTH);

        add(south, BorderLayout.SOUTH);
    }

    private JComponent crearBarraBusqueda() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createTitledBorder("Búsqueda"));

        txtBuscar = new JTextField();
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar por Nombre, Apellido o DNI...");
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { aplicarFiltroBusqueda(); }
            @Override public void removeUpdate(DocumentEvent e) { aplicarFiltroBusqueda(); }
            @Override public void changedUpdate(DocumentEvent e) { aplicarFiltroBusqueda(); }
        });

        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> txtBuscar.setText(""));

        p.add(txtBuscar, BorderLayout.CENTER);
        p.add(btnLimpiar, BorderLayout.EAST);
        return p;
    }

    private void aplicarFiltroBusqueda() {
        if (sorter == null) return;

        String q = txtBuscar.getText().trim();
        if (q.isBlank()) {
            sorter.setRowFilter(null);
            return;
        }

        final String needle = q.toLowerCase();


        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(RowFilter.Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String nombre = String.valueOf(entry.getValue(1)).toLowerCase();
                String apellido = String.valueOf(entry.getValue(2)).toLowerCase();
                String dni = String.valueOf(entry.getValue(3)).toLowerCase();

                return nombre.contains(needle)
                        || apellido.contains(needle)
                        || dni.contains(needle);
            }
        });

    }

    private void cargarComboObraSocial() {
        try {
            cmbObraSocial.removeAllItems();
            cmbObraSocial.addItem(null); // Particular

            List<ObraSocial> lista = obraSocialService.listar();
            for (ObraSocial os : lista) cmbObraSocial.addItem(os);

            cmbObraSocial.setSelectedItem(null);

        } catch (DAOException e) {
            mostrarError("Error cargando obras sociales", e);
        }
    }

    private void cargarTabla() {
        try {
            List<Paciente> lista = pacienteService.listar();
            modeloTabla.setRowCount(0);

            for (Paciente p : lista) {
                String osNombre = (p.getObraSocial() != null) ? safe(p.getObraSocial().getNombre()) : "Particular";

                modeloTabla.addRow(new Object[]{
                        p.getId(),
                        safe(p.getNombre()),
                        safe(p.getApellido()),
                        safe(p.getDni()),
                        safe(p.getTelefono()),
                        safe(p.getEmail()),
                        osNombre,
                        p.isActivo() ? "Sí" : "No"
                });
            }

            aplicarFiltroBusqueda();

        } catch (DAOException e) {
            mostrarError("Error cargando pacientes", e);
        }
    }

    private void onSeleccionTabla(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;

        int viewRow = tabla.getSelectedRow();
        if (viewRow < 0) return;

        int modelRow = tabla.convertRowIndexToModel(viewRow);
        int id = (int) modeloTabla.getValueAt(modelRow, 0);

        try {
            Paciente p = pacienteService.buscarPorId(id);
            cargarFormulario(p);
        } catch (ValidationException | NotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Atención", JOptionPane.WARNING_MESSAGE);
        } catch (DAOException ex) {
            mostrarError("Error al cargar paciente seleccionado", ex);
        }
    }

    private void cargarFormulario(Paciente p) {
        txtId.setText(String.valueOf(p.getId()));
        txtNombre.setText(safe(p.getNombre()));
        txtApellido.setText(safe(p.getApellido()));
        txtDni.setText(safe(p.getDni()));
        txtTelefono.setText(safe(p.getTelefono()));
        txtEmail.setText(safe(p.getEmail()));
        chkActivo.setSelected(p.isActivo());

        if (p.getObraSocial() == null) {
            cmbObraSocial.setSelectedItem(null);
        } else {
            seleccionarObraSocialPorId(p.getObraSocial().getId());
        }
    }

    private void seleccionarObraSocialPorId(int id) {
        for (int i = 0; i < cmbObraSocial.getItemCount(); i++) {
            ObraSocial os = cmbObraSocial.getItemAt(i);
            if (os != null && os.getId() == id) {
                cmbObraSocial.setSelectedIndex(i);
                return;
            }
        }
        cmbObraSocial.setSelectedItem(null);
    }

    private void limpiarFormulario() {
        tabla.clearSelection();
        txtId.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtDni.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");
        cmbObraSocial.setSelectedItem(null);
        chkActivo.setSelected(true);
        txtNombre.requestFocus();
    }

    private void guardar() {
        try {
            boolean activo = chkActivo.isSelected();

            String nombre = txtNombre.getText().trim();
            String apellido = txtApellido.getText().trim();
            String dni = txtDni.getText().trim();
            String telefono = txtTelefono.getText().trim();
            String email = txtEmail.getText().trim();

            // Validaciones obligatorias
            requireNoBlank(nombre, "El nombre es obligatorio.", txtNombre);
            requireNoBlank(apellido, "El apellido es obligatorio.", txtApellido);
            requireNoBlank(dni, "El DNI es obligatorio.", txtDni);
            requireNoBlank(telefono, "El teléfono es obligatorio.", txtTelefono);
            requireNoBlank(email, "El email es obligatorio.", txtEmail);

            // DNI numérico y longitud razonable
            validarDni(dni);

            // Teléfono: valida cantidad de dígitos
            validarTelefono(telefono);

            // Email formato
            validarEmail(email);

            ObraSocial os = (ObraSocial) cmbObraSocial.getSelectedItem();

            if (txtId.getText().isBlank()) {
                Paciente p = new Paciente(activo, nombre, apellido, dni, telefono, email, os);
                int id = pacienteService.crear(p);
                JOptionPane.showMessageDialog(this, "Paciente creado con ID: " + id);
                txtId.setText(String.valueOf(id));
            } else {
                int id = Integer.parseInt(txtId.getText().trim());
                Paciente p = new Paciente(id, activo, nombre, apellido, dni, telefono, email, os);
                pacienteService.actualizar(p);
                JOptionPane.showMessageDialog(this, "Paciente actualizado");
            }

            cargarTabla();
            limpiarFormulario();

        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (NotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No encontrado", JOptionPane.WARNING_MESSAGE);
            cargarTabla();
        } catch (DAOException ex) {
            mostrarError("Error guardando paciente", ex);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID inválido.", "Validación", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminar() {
        try {
            if (txtId.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Seleccioná un paciente para eliminar.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id = Integer.parseInt(txtId.getText().trim());

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Eliminar el paciente ID " + id + "?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) return;

            pacienteService.eliminar(id);
            JOptionPane.showMessageDialog(this, "Paciente eliminado.", "Información", JOptionPane.INFORMATION_MESSAGE);

            limpiarFormulario();
            cargarTabla();

        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (NotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No encontrado", JOptionPane.WARNING_MESSAGE);
            cargarTabla();
        } catch (DAOException ex) {
            mostrarError("Error eliminando paciente", ex);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID inválido.", "Validación", JOptionPane.WARNING_MESSAGE);
        }
    }

    // VALIDACIONES

    private void requireNoBlank(String s, String msg, JComponent focus) throws ValidationException {
        if (s == null || s.isBlank()) {
            if (focus != null) focus.requestFocus();
            throw new ValidationException(msg);
        }
    }

    private void validarDni(String dni) throws ValidationException {
        if (!SOLO_DIGITOS.matcher(dni).matches()) {
            txtDni.requestFocus();
            throw new ValidationException("El DNI debe contener solo números.");
        }
        if (dni.length() < 7 || dni.length() > 10) {
            txtDni.requestFocus();
            throw new ValidationException("El DNI debe tener entre 7 y 10 dígitos.");
        }
    }

    private void validarTelefono(String telefono) throws ValidationException {
        String digitos = telefono.replaceAll("\\D", "");
        if (digitos.isBlank()) {
            txtTelefono.requestFocus();
            throw new ValidationException("El teléfono debe contener números.");
        }
        if (digitos.length() < TEL_MIN_DIGITOS || digitos.length() > TEL_MAX_DIGITOS) {
            txtTelefono.requestFocus();
            throw new ValidationException("El teléfono debe tener entre " + TEL_MIN_DIGITOS + " y " + TEL_MAX_DIGITOS + " dígitos.");
        }
    }

    private void validarEmail(String email) throws ValidationException {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            txtEmail.requestFocus();
            throw new ValidationException("Email inválido. Ejemplo: nombre@dominio.com");
        }
    }

    private void mostrarError(String titulo, Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, titulo + ":\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
