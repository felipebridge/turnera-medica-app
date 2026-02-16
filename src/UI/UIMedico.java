package UI;

import Exceptions.DAOException;
import Exceptions.NotFoundException;
import Exceptions.ValidationException;
import Modelo.Medico;
import Service.MedicoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class UIMedico extends JFrame {

    private final MedicoService medicoService = new MedicoService();

    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> sorter;

    // Filtros
    private JTextField txtBuscar;
    private JComboBox<String> cmbFiltroEspecialidad;
    private JComboBox<String> cmbFiltroActivo;
    private JLabel lblResultados;

    // Formulario
    private JTextField txtId;
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtMatricula;
    private JTextField txtHonorario;
    private JComboBox<String> cmbEspecialidad;
    private JCheckBox chkActivo;

    // Botones
    private JButton btnNuevo, btnGuardar, btnEliminar, btnRefrescar;

    // Estado interno
    private volatile boolean cargandoTabla = false;

    private final DecimalFormat df;

    public UIMedico() {
        setTitle("Médicos");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1180, 700);
        setLocationRelativeTo(null);

        DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.US);
        sym.setDecimalSeparator('.');
        df = new DecimalFormat("#0.00", sym);

        initUI();
        cargarTablaAsync();
    }

    private void initUI() {
        setLayout(new BorderLayout(12, 12));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));

        add(crearHeaderPro(), BorderLayout.NORTH);
        add(crearCentroPro(), BorderLayout.CENTER);
        add(crearFormPro(), BorderLayout.SOUTH);

        setJMenuBar(crearMenu());
        configurarAtajos((JComponent) getContentPane());
    }

    // HEADER

    private JComponent crearHeaderPro() {
        JPanel header = new JPanel(new BorderLayout(12, 10));
        header.setBorder(new EmptyBorder(12, 14, 12, 14));
        header.setOpaque(true);
        header.setBackground(new Color(245, 246, 248));

        JLabel titulo = new JLabel("Gestión de Médicos");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));

        JLabel subtitulo = new JLabel("Alta, baja, modificación y consulta — con filtros y búsqueda");
        subtitulo.setForeground(new Color(90, 90, 90));

        JPanel textos = new JPanel(new GridLayout(2, 1, 0, 2));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(subtitulo);

        header.add(textos, BorderLayout.WEST);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acciones.setOpaque(false);

        btnNuevo = new JButton("Nuevo");
        btnGuardar = new JButton("Guardar");
        btnEliminar = new JButton("Eliminar");
        btnRefrescar = new JButton("Refrescar");

        btnGuardar.putClientProperty("JButton.buttonType", "default");
        btnEliminar.putClientProperty("JButton.buttonType", "danger");

        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardarAsync());
        btnEliminar.addActionListener(e -> eliminarAsync());
        btnRefrescar.addActionListener(e -> cargarTablaAsync());

        acciones.add(btnNuevo);
        acciones.add(btnGuardar);
        acciones.add(btnEliminar);
        acciones.add(btnRefrescar);

        header.add(acciones, BorderLayout.EAST);

        return header;
    }

    // CENTRO: FILTROS + TABLA

    private JComponent crearCentroPro() {
        JPanel cont = new JPanel(new BorderLayout(10, 10));
        cont.add(crearBarraFiltros(), BorderLayout.NORTH);
        cont.add(crearTablaPro(), BorderLayout.CENTER);
        cont.add(crearBarraEstado(), BorderLayout.SOUTH);
        return cont;
    }

    private JComponent crearBarraFiltros() {
        JPanel filtros = new JPanel(new GridBagLayout());
        filtros.setBorder(BorderFactory.createTitledBorder("Búsqueda y filtros"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtBuscar = new JTextField(26);
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar por nombre, apellido o matrícula");
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { aplicarFiltros(); }
            @Override public void removeUpdate(DocumentEvent e) { aplicarFiltros(); }
            @Override public void changedUpdate(DocumentEvent e) { aplicarFiltros(); }
        });

        cmbFiltroEspecialidad = new JComboBox<>(new String[]{
                "Todas", "Cardiólogo", "Neurocirujano", "Dermatólogo"
        });
        cmbFiltroEspecialidad.addActionListener(e -> aplicarFiltros());

        cmbFiltroActivo = new JComboBox<>(new String[]{
                "Todos", "Solo activos", "Solo inactivos"
        });
        cmbFiltroActivo.addActionListener(e -> aplicarFiltros());

        JButton btnLimpiar = new JButton("Limpiar filtros");
        btnLimpiar.addActionListener(e -> {
            txtBuscar.setText("");
            cmbFiltroEspecialidad.setSelectedIndex(0);
            cmbFiltroActivo.setSelectedIndex(0);
            aplicarFiltros();
        });

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; filtros.add(new JLabel("Buscar:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1; filtros.add(txtBuscar, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0; filtros.add(new JLabel("Especialidad:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0; filtros.add(cmbFiltroEspecialidad, gbc);

        gbc.gridx = 4; gbc.gridy = 0; gbc.weightx = 0; filtros.add(new JLabel("Estado:"), gbc);
        gbc.gridx = 5; gbc.gridy = 0; gbc.weightx = 0; filtros.add(cmbFiltroActivo, gbc);

        gbc.gridx = 6; gbc.gridy = 0; gbc.weightx = 0; filtros.add(btnLimpiar, gbc);

        return filtros;
    }

    private JComponent crearTablaPro() {
        modeloTabla = new DefaultTableModel(
                new Object[]{"ID", "Nombre", "Apellido", "Matrícula", "Especialidad", "Honorario", "Activo"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(28);

        tabla.getSelectionModel().addListSelectionListener(this::onSeleccionTabla);

        sorter = new TableRowSorter<>(modeloTabla);
        tabla.setRowSorter(sorter);

        JScrollPane sp = new JScrollPane(tabla);
        sp.setBorder(BorderFactory.createTitledBorder("Listado"));

        return sp;
    }

    private JComponent crearBarraEstado() {
        JPanel status = new JPanel(new BorderLayout());
        status.setBorder(new EmptyBorder(4, 2, 0, 2));
        lblResultados = new JLabel(" ");
        lblResultados.setForeground(new Color(90, 90, 90));
        status.add(lblResultados, BorderLayout.WEST);
        return status;
    }

    // FORM

    private JComponent crearFormPro() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createTitledBorder("Datos del médico"));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtId = new JTextField(10);
        txtId.setEnabled(false);

        txtNombre = new JTextField(20);
        txtApellido = new JTextField(20);
        txtMatricula = new JTextField(20);
        txtHonorario = new JTextField(12);

        cmbEspecialidad = new JComboBox<>(new String[]{
                "Cardiólogo", "Neurocirujano", "Dermatólogo"
        });
        cmbEspecialidad.setSelectedIndex(-1);

        chkActivo = new JCheckBox("Activo", true);

        int y = 0;

        addLabel(form, gbc, 0, y, "ID:");
        addComp(form, gbc, 1, y, txtId);
        gbc.gridx = 2; gbc.gridy = y; gbc.weightx = 0; form.add(new JLabel(""), gbc);
        gbc.gridx = 3; gbc.gridy = y; gbc.weightx = 1; form.add(chkActivo, gbc);
        y++;

        addLabel(form, gbc, 0, y, "Nombre:");
        addComp(form, gbc, 1, y, txtNombre);
        addLabel(form, gbc, 2, y, "Apellido:");
        addComp(form, gbc, 3, y, txtApellido);
        y++;

        addLabel(form, gbc, 0, y, "Matrícula:");
        addComp(form, gbc, 1, y, txtMatricula);
        addLabel(form, gbc, 2, y, "Especialidad:");
        addComp(form, gbc, 3, y, cmbEspecialidad);
        y++;

        addLabel(form, gbc, 0, y, "Honorario ($):");
        addComp(form, gbc, 1, y, txtHonorario);
        gbc.gridx = 2; gbc.gridy = y; gbc.weightx = 0; form.add(new JLabel(""), gbc);
        gbc.gridx = 3; gbc.gridy = y; gbc.weightx = 1; form.add(new JLabel(""), gbc);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JMenuBar crearMenu() {
        JMenuBar bar = new JMenuBar();
        JMenu archivo = new JMenu("Archivo");
        archivo.setMnemonic(KeyEvent.VK_A);

        JMenuItem mNuevo = new JMenuItem("Nuevo");
        JMenuItem mRefrescar = new JMenuItem("Refrescar");
        JMenuItem mSalir = new JMenuItem("Cerrar");

        mNuevo.addActionListener(e -> limpiarFormulario());
        mRefrescar.addActionListener(e -> cargarTablaAsync());
        mSalir.addActionListener(e -> dispose());

        archivo.add(mNuevo);
        archivo.add(mRefrescar);
        archivo.addSeparator();
        archivo.add(mSalir);

        bar.add(archivo);
        return bar;
    }

    private void configurarAtajos(JComponent root) {
        InputMap im = root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = root.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "guardar");
        am.put("guardar", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { guardarAsync(); }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "limpiar");
        am.put("limpiar", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { limpiarFormulario(); }
        });
    }

    // ASYNC HELPERS

    private void setBusy(boolean busy) {
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        btnNuevo.setEnabled(!busy);
        btnGuardar.setEnabled(!busy);
        btnEliminar.setEnabled(!busy);
        btnRefrescar.setEnabled(!busy);
        tabla.setEnabled(!busy);
    }

    private void mostrarError(String titulo, Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, titulo + ":\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    // DATA

    private void cargarTablaAsync() {
        if (cargandoTabla) return;

        cargandoTabla = true;
        setBusy(true);

        new SwingWorker<List<Medico>, Void>() {
            @Override
            protected List<Medico> doInBackground() throws Exception {
                return medicoService.listar();
            }

            @Override
            protected void done() {
                try {
                    List<Medico> lista = get();

                    modeloTabla.setRowCount(0);
                    for (Medico m : lista) {
                        modeloTabla.addRow(new Object[]{
                                m.getId(),
                                safe(m.getNombre()),
                                safe(m.getApellido()),
                                safe(m.getMatricula()),
                                safe(m.getEspecialidad()),
                                df.format(m.getHonorario()),
                                m.isActivo() ? "Sí" : "No"
                        });
                    }

                    aplicarFiltros();
                    lblResultados.setText("Registros: " + tabla.getRowCount());

                } catch (Exception ex) {
                    mostrarError("Error cargando médicos", (ex instanceof Exception) ? (Exception) ex : new Exception(ex));
                } finally {
                    cargandoTabla = false;
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void aplicarFiltros() {
        if (sorter == null) return;

        String q = txtBuscar != null ? txtBuscar.getText().trim() : "";
        String esp = cmbFiltroEspecialidad != null ? (String) cmbFiltroEspecialidad.getSelectedItem() : "Todas";
        String act = cmbFiltroActivo != null ? (String) cmbFiltroActivo.getSelectedItem() : "Todos";

        RowFilter<DefaultTableModel, Object> rf = new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                String nombre = String.valueOf(entry.getValue(1));
                String apellido = String.valueOf(entry.getValue(2));
                String matricula = String.valueOf(entry.getValue(3));
                String especialidad = String.valueOf(entry.getValue(4));
                String activoTxt = String.valueOf(entry.getValue(6)); // "Sí"/"No"

                if (!q.isBlank()) {
                    String haystack = (nombre + " " + apellido + " " + matricula).toLowerCase();
                    if (!haystack.contains(q.toLowerCase())) return false;
                }

                if (esp != null && !"Todas".equals(esp)) {
                    if (!esp.equalsIgnoreCase(especialidad)) return false;
                }

                if ("Solo activos".equals(act) && !"Sí".equalsIgnoreCase(activoTxt)) return false;
                if ("Solo inactivos".equals(act) && !"No".equalsIgnoreCase(activoTxt)) return false;

                return true;
            }
        };

        sorter.setRowFilter(rf);

        if (lblResultados != null) {
            lblResultados.setText("Registros: " + tabla.getRowCount());
        }
    }

    private void onSeleccionTabla(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        if (cargandoTabla) return;

        int viewRow = tabla.getSelectedRow();
        if (viewRow < 0) return;

        int modelRow = tabla.convertRowIndexToModel(viewRow);
        Object idObj = modeloTabla.getValueAt(modelRow, 0);
        if (!(idObj instanceof Integer)) return;

        int id = (Integer) idObj;

        setBusy(true);
        new SwingWorker<Medico, Void>() {
            @Override
            protected Medico doInBackground() throws Exception {
                return medicoService.buscarPorId(id);
            }

            @Override
            protected void done() {
                try {
                    Medico m = get();
                    cargarFormulario(m);
                } catch (Exception ex) {
                    if (ex.getCause() instanceof ValidationException ve) {
                        JOptionPane.showMessageDialog(UIMedico.this, ve.getMessage(), "Atención", JOptionPane.WARNING_MESSAGE);
                    } else if (ex.getCause() instanceof NotFoundException ne) {
                        JOptionPane.showMessageDialog(UIMedico.this, ne.getMessage(), "No encontrado", JOptionPane.WARNING_MESSAGE);
                        cargarTablaAsync();
                    } else {
                        mostrarError("Error al cargar médico seleccionado", (Exception) ex);
                    }
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void cargarFormulario(Medico m) {
        txtId.setText(String.valueOf(m.getId()));
        txtNombre.setText(safe(m.getNombre()));
        txtApellido.setText(safe(m.getApellido()));
        txtMatricula.setText(safe(m.getMatricula()));
        chkActivo.setSelected(m.isActivo());

        String esp = safe(m.getEspecialidad());
        cmbEspecialidad.setSelectedItem(esp.isBlank() ? null : esp);

        txtHonorario.setText(df.format(m.getHonorario()));
    }

    private void limpiarFormulario() {
        tabla.clearSelection();
        txtId.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtMatricula.setText("");
        txtHonorario.setText("");
        cmbEspecialidad.setSelectedIndex(-1);
        chkActivo.setSelected(true);
        txtNombre.requestFocus();
    }

    private void guardarAsync() {
        // Validación + construcción del objeto
        Medico medico;
        try {
            medico = construirDesdeFormulario();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        setBusy(true);

        new SwingWorker<Integer, Void>() {
            private boolean esAlta;

            @Override
            protected Integer doInBackground() throws Exception {
                if (medico.getId() <= 0) {
                    esAlta = true;
                    return medicoService.crear(medico);
                } else {
                    esAlta = false;
                    medicoService.actualizar(medico);
                    return medico.getId();
                }
            }

            @Override
            protected void done() {
                try {
                    int id = get();
                    JOptionPane.showMessageDialog(
                            UIMedico.this,
                            esAlta ? ("Médico creado con ID: " + id) : "Médico actualizado",
                            "OK",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    cargarTablaAsync();
                    limpiarFormulario();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;

                    if (cause instanceof ValidationException ve) {
                        JOptionPane.showMessageDialog(UIMedico.this, ve.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
                    } else if (cause instanceof NotFoundException ne) {
                        JOptionPane.showMessageDialog(UIMedico.this, ne.getMessage(), "No encontrado", JOptionPane.WARNING_MESSAGE);
                        cargarTablaAsync();
                    } else if (cause instanceof DAOException de) {
                        mostrarError("Error guardando médico", de);
                    } else {
                        mostrarError("Error guardando médico", new Exception(cause));
                    }
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    private Medico construirDesdeFormulario() throws ValidationException {
        boolean activo = chkActivo.isSelected();

        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String matricula = txtMatricula.getText().trim();

        Object espSel = cmbEspecialidad.getSelectedItem();
        String especialidad = (espSel == null) ? "" : espSel.toString().trim();

        if (nombre.isBlank()) {
            txtNombre.requestFocus();
            throw new ValidationException("El nombre es obligatorio.");
        }
        if (apellido.isBlank()) {
            txtApellido.requestFocus();
            throw new ValidationException("El apellido es obligatorio.");
        }
        if (matricula.isBlank()) {
            txtMatricula.requestFocus();
            throw new ValidationException("La matrícula es obligatoria.");
        }
        if (especialidad.isBlank()) {
            cmbEspecialidad.requestFocus();
            throw new ValidationException("Seleccioná una especialidad.");
        }

        double honorario = parseHonorarioRobusto(txtHonorario.getText());
        if (honorario <= 0) {
            txtHonorario.requestFocus();
            throw new ValidationException("El honorario debe ser mayor a 0.");
        }

        int id = 0;
        if (!txtId.getText().isBlank()) {
            try {
                id = Integer.parseInt(txtId.getText().trim());
            } catch (NumberFormatException ex) {
                throw new ValidationException("ID inválido.");
            }
        }

        if (id <= 0) {
            return new Medico(activo, nombre, apellido, matricula, especialidad, honorario);
        }
        return new Medico(id, activo, nombre, apellido, matricula, especialidad, honorario);
    }

    private double parseHonorarioRobusto(String txt) throws ValidationException {
        String s = (txt == null) ? "" : txt.trim();
        if (s.isBlank()) return -1;

        s = s.replace("$", "").replace(" ", "");
        s = s.replaceAll("[^0-9,\\.]", "");

        if (s.contains(",") && s.contains(".")) {
            s = s.replace(".", "");
        }
        s = s.replace(",", ".");

        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Honorario inválido.");
        }
    }

    private void eliminarAsync() {
        if (txtId.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Seleccioná un médico para eliminar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id;
        try {
            id = Integer.parseInt(txtId.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID inválido.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Eliminar el médico ID " + id + "?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        setBusy(true);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                medicoService.eliminar(id);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(UIMedico.this, "Médico eliminado.", "Información", JOptionPane.INFORMATION_MESSAGE);
                    limpiarFormulario();
                    cargarTablaAsync();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;

                    if (cause instanceof ValidationException ve) {
                        JOptionPane.showMessageDialog(UIMedico.this, ve.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
                    } else if (cause instanceof NotFoundException ne) {
                        JOptionPane.showMessageDialog(UIMedico.this, ne.getMessage(), "No encontrado", JOptionPane.WARNING_MESSAGE);
                        cargarTablaAsync();
                    } else if (cause instanceof DAOException de) {
                        mostrarError("Error eliminando médico", de);
                    } else {
                        mostrarError("Error eliminando médico", new Exception(cause));
                    }
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    // UTILS

    private String safe(String s) { return s == null ? "" : s; }

    private void addLabel(JPanel p, GridBagConstraints gbc, int x, int y, String text) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        p.add(new JLabel(text), gbc);
    }

    private void addComp(JPanel p, GridBagConstraints gbc, int x, int y, JComponent c) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        p.add(c, gbc);
    }
}
