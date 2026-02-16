package UI;

import Exceptions.DAOException;
import Exceptions.NotFoundException;
import Exceptions.ValidationException;
import Modelo.*;
import Service.*;

import com.toedter.calendar.JCalendar;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;

public class UITurno extends JFrame {

    private final TurnoService turnoService = new TurnoService();
    private final MedicoService medicoService = new MedicoService();
    private final PacienteService pacienteService = new PacienteService();
    private final ConsultorioService consultorioService = new ConsultorioService();

    // TAB GESTIÓN
    private JTable tabla;
    private DefaultTableModel modeloTabla;

    private JTextField txtId;
    private JComboBox<Medico> cmbMedico;
    private JComboBox<Paciente> cmbPaciente;
    private JComboBox<Consultorio> cmbConsultorio;

    private JDateChooser dcFecha;
    private JSpinner spHora;
    private JComboBox<EstadoTurno> cmbEstado;
    private JTextField txtObservacion;

    private JButton btnNuevo, btnGuardar, btnEliminar, btnRefrescar, btnCancelar;

    // Calendario + filtros de listado
    private JCalendar calendar;
    private JComboBox<Medico> cmbFiltroMedico;
    private JComboBox<EstadoTurno> cmbFiltroEstado;
    private JButton btnHoy;

    // TAB REPORTE
    private JTable tablaReporte;
    private DefaultTableModel modeloReporte;
    private JDateChooser dcDesde;
    private JDateChooser dcHasta;
    private JComboBox<Medico> cmbMedicoReporte;
    private JButton btnBuscarReporte, btnLimpiarReporte, btnRefrescarReporte;

    // Resumen reporte
    private JLabel lblConsultasAtendidas;
    private JLabel lblRecaudacionTotal;

    private final DecimalFormat df = new DecimalFormat("#0.00");

    public UITurno() {
        setTitle("Turnos");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1250, 760);
        setLocationRelativeTo(null);

        initUI();
        cargarCombos();
        refrescarListadoPorFecha(getFechaSeleccionadaCalendar(), true);
    }

    private void initUI() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Agenda", buildPanelGestionPro());
        tabs.addTab("Reporte", buildPanelReportePro());

        setContentPane(tabs);
    }

    // PANEL GESTIÓN

    private JPanel buildPanelGestionPro() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        root.add(buildHeader("Agenda de Turnos", "Calendario + listado diario + formulario"), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.30);
        split.setBorder(null);

        split.setLeftComponent(buildCard("Calendario", buildPanelCalendario()));
        split.setRightComponent(buildCard("Turnos del día", buildPanelListado()));

        root.add(split, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(10, 10));
        south.add(buildCard("Datos del turno", buildFormGestionPro()), BorderLayout.CENTER);
        south.add(buildBotoneraGestionPro(), BorderLayout.SOUTH);

        root.add(south, BorderLayout.SOUTH);
        return root;
    }

    private JComponent buildPanelCalendario() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        calendar = new JCalendar();
        calendar.setWeekOfYearVisible(false);

        calendar.addPropertyChangeListener("calendar", evt -> {
            LocalDate sel = getFechaSeleccionadaCalendar();
            setFechaForm(sel);
            refrescarListadoPorFecha(sel, true);
        });

        JPanel top = new JPanel(new BorderLayout(8, 8));
        JLabel hint = new JLabel("Tip: seleccioná un día para ver sus turnos.");
        hint.setForeground(new Color(90, 90, 90));

        btnHoy = new JButton("Hoy");
        btnHoy.addActionListener(e -> {
            Calendar c = Calendar.getInstance();
            calendar.setCalendar(c);
            LocalDate sel = getFechaSeleccionadaCalendar();
            setFechaForm(sel);
            refrescarListadoPorFecha(sel, true);
        });

        top.add(hint, BorderLayout.CENTER);
        top.add(btnHoy, BorderLayout.EAST);

        p.add(top, BorderLayout.NORTH);
        p.add(calendar, BorderLayout.CENTER);

        return p;
    }

    private JComponent buildPanelListado() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        cmbFiltroMedico = new JComboBox<>();
        cmbFiltroMedico.setRenderer(rendererMedicoConNull("Todos los médicos"));
        cmbFiltroMedico.addActionListener(e -> refrescarListadoPorFecha(getFechaSeleccionadaCalendar(), false));

        cmbFiltroEstado = new JComboBox<>();
        cmbFiltroEstado.addItem(null);
        for (EstadoTurno et : EstadoTurno.values()) cmbFiltroEstado.addItem(et);
        cmbFiltroEstado.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) setText("Todos los estados");
                else setText(((EstadoTurno) value).name());
                return this;
            }
        });
        cmbFiltroEstado.addActionListener(e -> refrescarListadoPorFecha(getFechaSeleccionadaCalendar(), false));

        JButton btnRef = new JButton("Refrescar");
        btnRef.addActionListener(e -> {
            cargarCombos();
            refrescarListadoPorFecha(getFechaSeleccionadaCalendar(), true);
        });

        filtros.add(new JLabel("Médico:"));
        filtros.add(cmbFiltroMedico);
        filtros.add(new JLabel("Estado:"));
        filtros.add(cmbFiltroEstado);
        filtros.add(btnRef);

        modeloTabla = new DefaultTableModel(
                new Object[]{"ID", "Fecha", "Hora", "Médico", "Paciente", "Consultorio", "Estado"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(28);
        tabla.getSelectionModel().addListSelectionListener(this::onSeleccionTabla);

        JScrollPane sp = new JScrollPane(tabla);

        p.add(filtros, BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);

        return p;
    }

    private JComponent buildFormGestionPro() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        txtId = new JTextField(10);
        txtId.setEnabled(false);

        cmbMedico = new JComboBox<>();
        cmbPaciente = new JComboBox<>();
        cmbConsultorio = new JComboBox<>();

        cmbMedico.setRenderer(rendererMedico());
        cmbPaciente.setRenderer(rendererPaciente());
        cmbConsultorio.setRenderer(rendererConsultorio());

        dcFecha = new JDateChooser();
        dcFecha.setDateFormatString("dd/MM/yyyy");

        SpinnerDateModel modelHora = new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE);
        spHora = new JSpinner(modelHora);
        spHora.setEditor(new JSpinner.DateEditor(spHora, "HH:mm"));

        cmbEstado = new JComboBox<>(EstadoTurno.values());
        txtObservacion = new JTextField(40);

        int y = 0;

        addLabel(form, gbc, 0, y, "ID:");
        addComp(form, gbc, 1, y, txtId);
        addLabel(form, gbc, 2, y, "Estado:");
        addComp(form, gbc, 3, y, cmbEstado);
        y++;

        addLabel(form, gbc, 0, y, "Médico:");
        addComp(form, gbc, 1, y, cmbMedico);
        addLabel(form, gbc, 2, y, "Consultorio:");
        addComp(form, gbc, 3, y, cmbConsultorio);
        y++;

        addLabel(form, gbc, 0, y, "Paciente:");
        gbc.gridx = 1; gbc.gridy = y; gbc.gridwidth = 3; gbc.weightx = 1;
        form.add(cmbPaciente, gbc);
        gbc.gridwidth = 1;
        y++;

        addLabel(form, gbc, 0, y, "Fecha:");
        addComp(form, gbc, 1, y, dcFecha);
        addLabel(form, gbc, 2, y, "Hora:");
        addComp(form, gbc, 3, y, spHora);
        y++;

        addLabel(form, gbc, 0, y, "Observación:");
        gbc.gridx = 1; gbc.gridy = y; gbc.gridwidth = 3; gbc.weightx = 1;
        form.add(txtObservacion, gbc);
        gbc.gridwidth = 1;

        setFechaForm(getFechaSeleccionadaCalendar());
        return form;
    }

    private JPanel buildBotoneraGestionPro() {
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setBorder(new EmptyBorder(0, 0, 0, 0));

        btnNuevo = new JButton("Nuevo");
        btnGuardar = new JButton("Guardar");
        btnCancelar = new JButton("Cancelar turno");
        btnEliminar = new JButton("Eliminar");
        btnRefrescar = new JButton("Refrescar");

        btnNuevo.addActionListener(e -> limpiarFormulario(true));
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        btnRefrescar.addActionListener(e -> {
            cargarCombos();
            refrescarListadoPorFecha(getFechaSeleccionadaCalendar(), true);
        });
        btnCancelar.addActionListener(e -> cancelar());

        botones.add(btnNuevo);
        botones.add(btnGuardar);
        botones.add(btnCancelar);
        botones.add(btnEliminar);
        botones.add(btnRefrescar);

        return botones;
    }

    // REPORTE

    private JPanel buildPanelReportePro() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel north = new JPanel(new BorderLayout(10, 10));
        north.add(buildHeader("Reporte de turnos", "Filtrá entre fechas y visualizá la recaudación"), BorderLayout.NORTH);
        north.add(buildCard("Filtros", buildFiltrosReporte()), BorderLayout.CENTER);

        root.add(north, BorderLayout.NORTH);

        modeloReporte = new DefaultTableModel(
                new Object[]{"ID", "Fecha", "Hora", "Médico", "Paciente", "Consultorio", "Estado"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaReporte = new JTable(modeloReporte);
        tablaReporte.setRowHeight(28);

        root.add(buildCard("Resultados", new JScrollPane(tablaReporte)), BorderLayout.CENTER);

        root.add(buildResumenReporte(), BorderLayout.SOUTH);

        if (dcDesde.getDate() == null || dcHasta.getDate() == null) {
            Date hoy = new Date();
            dcDesde.setDate(hoy);
            dcHasta.setDate(hoy);
        }

        actualizarResumenReporte(0, 0);

        return root;
    }


    private JComponent buildFiltrosReporte() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        dcDesde = new JDateChooser();
        dcDesde.setDateFormatString("dd/MM/yyyy");

        dcHasta = new JDateChooser();
        dcHasta.setDateFormatString("dd/MM/yyyy");

        cmbMedicoReporte = new JComboBox<>();
        cmbMedicoReporte.setRenderer(rendererMedicoConNull("Todos los médicos"));

        btnBuscarReporte = new JButton("Buscar");
        btnLimpiarReporte = new JButton("Limpiar");
        btnRefrescarReporte = new JButton("Refrescar médicos");

        btnBuscarReporte.addActionListener(e -> buscarReporte());
        btnLimpiarReporte.addActionListener(e -> limpiarReporte());
        btnRefrescarReporte.addActionListener(e -> cargarCombos());

        int y = 0;

        addLabel(p, gbc, 0, y, "Desde:");
        addComp(p, gbc, 1, y, dcDesde);
        addLabel(p, gbc, 2, y, "Hasta:");
        addComp(p, gbc, 3, y, dcHasta);
        y++;

        addLabel(p, gbc, 0, y, "Médico:");
        gbc.gridx = 1; gbc.gridy = y; gbc.gridwidth = 3; gbc.weightx = 1;
        p.add(cmbMedicoReporte, gbc);
        gbc.gridwidth = 1;
        y++;

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        acciones.add(btnBuscarReporte);
        acciones.add(btnLimpiarReporte);
        acciones.add(btnRefrescarReporte);

        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 4;
        p.add(acciones, gbc);

        return p;
    }

    private JComponent buildResumenReporte() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(6, 10, 6, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;

        lblConsultasAtendidas = new JLabel("Consultas atendidas: 0");
        lblRecaudacionTotal = new JLabel("Recaudación total: $ 0.00");

        lblConsultasAtendidas.setFont(lblConsultasAtendidas.getFont().deriveFont(Font.BOLD));
        lblRecaudacionTotal.setFont(lblRecaudacionTotal.getFont().deriveFont(Font.BOLD));

        gbc.gridx = 0; gbc.gridy = 0; p.add(lblConsultasAtendidas, gbc);
        gbc.gridx = 1; gbc.gridy = 0; p.add(lblRecaudacionTotal, gbc);

        // empuja a la izquierda
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 1;
        p.add(new JLabel(""), gbc);

        return p;
    }

    // LÓGICA UI

    private void cargarCombos() {
        try {
            List<Medico> medicos = medicoService.listar();
            List<Paciente> pacientes = pacienteService.listar();
            List<Consultorio> consultorios = consultorioService.listar();

            cmbMedico.removeAllItems();
            cmbPaciente.removeAllItems();
            cmbConsultorio.removeAllItems();

            for (Medico m : medicos) cmbMedico.addItem(m);
            for (Paciente p : pacientes) cmbPaciente.addItem(p);
            for (Consultorio c : consultorios) cmbConsultorio.addItem(c);

            cmbFiltroMedico.removeAllItems();
            cmbFiltroMedico.addItem(null);
            for (Medico m : medicos) cmbFiltroMedico.addItem(m);

            cmbMedicoReporte.removeAllItems();
            cmbMedicoReporte.addItem(null);
            for (Medico m : medicos) cmbMedicoReporte.addItem(m);

        } catch (DAOException e) {
            mostrarError("Error cargando combos", e);
        }
    }

    /** Refresca el listado derecho filtrando por fecha + filtros. */
    private void refrescarListadoPorFecha(LocalDate fecha, boolean limpiarSeleccion) {
        try {
            List<Turno> todos = turnoService.listar();

            Medico filtroMed = (Medico) cmbFiltroMedico.getSelectedItem();
            EstadoTurno filtroEst = (EstadoTurno) cmbFiltroEstado.getSelectedItem();

            modeloTabla.setRowCount(0);

            for (Turno t : todos) {
                if (t.getFecha() == null) continue;
                if (!t.getFecha().equals(fecha)) continue;

                if (filtroMed != null && (t.getMedico() == null || t.getMedico().getId() != filtroMed.getId()))
                    continue;

                if (filtroEst != null && (t.getEstado() == null || t.getEstado() != filtroEst))
                    continue;

                modeloTabla.addRow(new Object[]{
                        t.getId(),
                        t.getFecha(),
                        t.getHora(),
                        fmtMedico(t.getMedico()),
                        fmtPaciente(t.getPaciente()),
                        fmtConsultorio(t.getConsultorio()),
                        t.getEstado() != null ? t.getEstado().name() : ""
                });
            }

            if (limpiarSeleccion) tabla.clearSelection();

        } catch (DAOException e) {
            mostrarError("Error cargando turnos", e);
        }
    }

    private void onSeleccionTabla(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int row = tabla.getSelectedRow();
        if (row < 0) return;

        int id = (int) modeloTabla.getValueAt(row, 0);

        try {
            Turno t = turnoService.buscarPorId(id);
            cargarFormulario(t);
        } catch (ValidationException | NotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Atención", JOptionPane.WARNING_MESSAGE);
        } catch (DAOException ex) {
            mostrarError("Error al cargar turno", ex);
        }
    }

    private void cargarFormulario(Turno t) {
        txtId.setText(String.valueOf(t.getId()));

        seleccionarEnComboPorId(cmbMedico, t.getMedico() != null ? t.getMedico().getId() : -1);
        seleccionarEnComboPorId(cmbPaciente, t.getPaciente() != null ? t.getPaciente().getId() : -1);
        seleccionarEnComboPorId(cmbConsultorio, t.getConsultorio() != null ? t.getConsultorio().getId() : -1);

        setFechaForm(t.getFecha());

        if (t.getHora() != null) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, t.getHora().getHour());
            cal.set(Calendar.MINUTE, t.getHora().getMinute());
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            spHora.setValue(cal.getTime());
        }

        cmbEstado.setSelectedItem(t.getEstado() != null ? t.getEstado() : EstadoTurno.PENDIENTE);
        txtObservacion.setText(t.getObservacion() != null ? t.getObservacion() : "");
    }

    private <T> void seleccionarEnComboPorId(JComboBox<T> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object item = combo.getItemAt(i);
            if (item instanceof Medico m && m.getId() == id) { combo.setSelectedIndex(i); return; }
            if (item instanceof Paciente p && p.getId() == id) { combo.setSelectedIndex(i); return; }
            if (item instanceof Consultorio c && c.getId() == id) { combo.setSelectedIndex(i); return; }
        }
    }

    private void limpiarFormulario(boolean resetearFechaConCalendario) {
        tabla.clearSelection();
        txtId.setText("");

        if (cmbMedico.getItemCount() > 0) cmbMedico.setSelectedIndex(0);
        if (cmbPaciente.getItemCount() > 0) cmbPaciente.setSelectedIndex(0);
        if (cmbConsultorio.getItemCount() > 0) cmbConsultorio.setSelectedIndex(0);

        if (resetearFechaConCalendario) setFechaForm(getFechaSeleccionadaCalendar());

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        spHora.setValue(cal.getTime());

        cmbEstado.setSelectedItem(EstadoTurno.PENDIENTE);
        txtObservacion.setText("");
        cmbMedico.requestFocus();
    }

    private void guardar() {
        try {
            Medico medico = (Medico) cmbMedico.getSelectedItem();
            Paciente paciente = (Paciente) cmbPaciente.getSelectedItem();
            Consultorio consultorio = (Consultorio) cmbConsultorio.getSelectedItem();

            LocalDate fecha = getFechaForm();
            LocalTime hora = obtenerHoraSpinner();

            EstadoTurno estado = (EstadoTurno) cmbEstado.getSelectedItem();
            String obs = txtObservacion.getText().trim();
            if (obs.isBlank()) obs = null;

            if (txtId.getText().isBlank()) {
                Turno t = new Turno(medico, paciente, consultorio, fecha, hora, estado, obs);
                int id = turnoService.crear(t);
                JOptionPane.showMessageDialog(this, "Turno creado con ID: " + id, "Información", JOptionPane.INFORMATION_MESSAGE);
            } else {
                int id = Integer.parseInt(txtId.getText().trim());
                Turno t = new Turno(id, medico, paciente, consultorio, fecha, hora, estado, obs);
                turnoService.actualizar(t);
                JOptionPane.showMessageDialog(this, "Turno actualizado.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }

            limpiarFormulario(true);
            refrescarListadoPorFecha(getFechaSeleccionadaCalendar(), true);

        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (NotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No encontrado", JOptionPane.WARNING_MESSAGE);
            refrescarListadoPorFecha(getFechaSeleccionadaCalendar(), true);
        } catch (DAOException ex) {
            mostrarError("Error guardando turno", ex);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID inválido.", "Validación", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminar() {
        try {
            if (txtId.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Seleccioná un turno para eliminar.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id = Integer.parseInt(txtId.getText().trim());

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Eliminar el turno ID " + id + "?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            turnoService.eliminar(id);
            JOptionPane.showMessageDialog(this, "Turno eliminado.", "Información", JOptionPane.INFORMATION_MESSAGE);

            limpiarFormulario(true);
            refrescarListadoPorFecha(getFechaSeleccionadaCalendar(), true);

        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (NotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No encontrado", JOptionPane.WARNING_MESSAGE);
            refrescarListadoPorFecha(getFechaSeleccionadaCalendar(), true);
        } catch (DAOException ex) {
            mostrarError("Error eliminando turno", ex);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID inválido.", "Validación", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void cancelar() {
        try {
            if (txtId.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Seleccioná un turno para cancelar.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id = Integer.parseInt(txtId.getText().trim());

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Cancelar el turno ID " + id + "?",
                    "Confirmar cancelación",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            turnoService.cancelar(id);
            JOptionPane.showMessageDialog(this, "Turno cancelado.", "Información", JOptionPane.INFORMATION_MESSAGE);

            refrescarListadoPorFecha(getFechaSeleccionadaCalendar(), true);

        } catch (ValidationException | NotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Atención", JOptionPane.WARNING_MESSAGE);
            refrescarListadoPorFecha(getFechaSeleccionadaCalendar(), true);
        } catch (DAOException ex) {
            mostrarError("Error cancelando turno", ex);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID inválido.", "Validación", JOptionPane.WARNING_MESSAGE);
        }
    }

    // REPORTE

    private void buscarReporte() {
        try {
            LocalDate desde = getDateChooserLocalDate(dcDesde);
            LocalDate hasta = getDateChooserLocalDate(dcHasta);

            if (desde == null || hasta == null) throw new ValidationException("Seleccioná 'Desde' y 'Hasta'.");
            if (hasta.isBefore(desde)) throw new ValidationException("'Hasta' no puede ser menor que 'Desde'.");

            Medico medico = (Medico) cmbMedicoReporte.getSelectedItem();

            List<Turno> lista = (medico == null)
                    ? turnoService.reporteEntreFechas(desde, hasta)
                    : turnoService.reporteMedicoEntreFechas(medico.getId(), desde, hasta);

            System.out.println("Reporte: desde=" + desde + " hasta=" + hasta
                    + " medico=" + (medico == null ? "ALL" : medico.getId())
                    + " -> " + lista.size());

            modeloReporte.setRowCount(0);

            int atendidos = 0;
            double total = 0;

            for (Turno t : lista) {
                modeloReporte.addRow(new Object[]{
                        t.getId(),
                        t.getFecha(),
                        t.getHora(),
                        fmtMedico(t.getMedico()),
                        fmtPaciente(t.getPaciente()),
                        fmtConsultorio(t.getConsultorio()),
                        t.getEstado() != null ? t.getEstado().name() : ""
                });

                if (t.getEstado() == EstadoTurno.ATENDIDO) {
                    atendidos++;
                    if (t.getMedico() != null) total += t.getMedico().getHonorario();
                }
            }

            actualizarResumenReporte(atendidos, total);

        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (DAOException ex) {
            mostrarError("Error generando reporte", ex);
        }
    }


    private void limpiarReporte() {
        dcDesde.setDate(null);
        dcHasta.setDate(null);
        cmbMedicoReporte.setSelectedItem(null);
        modeloReporte.setRowCount(0);
        actualizarResumenReporte(0, 0);
    }


    private void actualizarResumenReporte(int atendidos, double total) {
        if (lblConsultasAtendidas != null) lblConsultasAtendidas.setText("Consultas atendidas: " + atendidos);
        if (lblRecaudacionTotal != null) lblRecaudacionTotal.setText("Recaudación total: $ " + df.format(total));
    }

    // FECHAS / HORA

    private LocalDate getFechaSeleccionadaCalendar() {
        Calendar c = calendar != null ? calendar.getCalendar() : Calendar.getInstance();
        Date d = c.getTime();
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void setFechaForm(LocalDate fecha) {
        if (dcFecha == null) return;
        if (fecha == null) {
            dcFecha.setDate(null);
        } else {
            Date d = Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant());
            dcFecha.setDate(d);
        }
    }

    private LocalDate getFechaForm() throws ValidationException {
        LocalDate d = getDateChooserLocalDate(dcFecha);
        if (d == null) throw new ValidationException("Seleccioná una fecha.");
        return d;
    }

    private LocalDate getDateChooserLocalDate(JDateChooser dc) {
        if (dc == null) return null;
        Date d = dc.getDate();
        if (d == null) return null;
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalTime obtenerHoraSpinner() throws ValidationException {
        Object v = spHora.getValue();
        if (!(v instanceof Date)) throw new ValidationException("Hora inválida.");
        Calendar cal = Calendar.getInstance();
        cal.setTime((Date) v);
        int h = cal.get(Calendar.HOUR_OF_DAY);
        int m = cal.get(Calendar.MINUTE);
        return LocalTime.of(h, m);
    }

    // FORMATEADORES

    private String fmtMedico(Medico m) {
        if (m == null) return "";
        String nombre = safe(m.getNombre());
        String apellido = safe(m.getApellido());
        String esp = (m.getEspecialidad() != null && !m.getEspecialidad().isBlank()) ? " (" + m.getEspecialidad() + ")" : "";
        return apellido + ", " + nombre + esp;
    }

    private String fmtPaciente(Paciente p) {
        if (p == null) return "";
        String dni = safe(p.getDni());
        String email = (p.getEmail() != null && !p.getEmail().isBlank()) ? " (" + p.getEmail() + ")" : "";
        return dni + email;
    }

    private String fmtConsultorio(Consultorio c) {
        if (c == null) return "";
        return safe(c.getNumero());
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    // RENDERERS

    private ListCellRenderer<Object> rendererMedico() {
        return new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Medico m) {
                    String nombre = safe(m.getNombre());
                    String apellido = safe(m.getApellido());
                    String esp = (m.getEspecialidad() != null && !m.getEspecialidad().isBlank())
                            ? " (" + m.getEspecialidad() + ")"
                            : "";
                    setText(m.getId() + " - " + apellido + ", " + nombre + esp);
                }
                return this;
            }
        };
    }

    private ListCellRenderer<Object> rendererMedicoConNull(String nullText) {
        return new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) setText(nullText);
                else if (value instanceof Medico m) {
                    String nombre = safe(m.getNombre());
                    String apellido = safe(m.getApellido());
                    String esp = (m.getEspecialidad() != null && !m.getEspecialidad().isBlank())
                            ? " (" + m.getEspecialidad() + ")"
                            : "";
                    setText(m.getId() + " - " + apellido + ", " + nombre + esp);
                }
                return this;
            }
        };
    }

    private ListCellRenderer<Object> rendererPaciente() {
        return new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Paciente p) {
                    String dni = safe(p.getDni());
                    String email = (p.getEmail() != null && !p.getEmail().isBlank()) ? " (" + p.getEmail() + ")" : "";
                    setText(p.getId() + " - " + dni + email);
                }
                return this;
            }
        };
    }

    private ListCellRenderer<Object> rendererConsultorio() {
        return new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Consultorio c) setText(c.getId() + " - " + safe(c.getNumero()));
                return this;
            }
        };
    }

    // UI BUILD HELPERS


    private JComponent buildHeader(String title, String subtitle) {
        JPanel header = new JPanel(new BorderLayout(10, 10));
        header.setBorder(new EmptyBorder(10, 12, 10, 12));
        header.setOpaque(true);
        header.setBackground(new Color(245, 246, 248));

        JLabel t = new JLabel(title);
        t.setFont(t.getFont().deriveFont(Font.BOLD, 18f));

        JLabel s = new JLabel(subtitle);
        s.setForeground(new Color(90, 90, 90));

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.add(t);
        left.add(s);

        header.add(left, BorderLayout.WEST);
        return header;
    }

    private JPanel buildCard(String title, JComponent content) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createTitledBorder(title));
        card.add(content, BorderLayout.CENTER);
        return card;
    }

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

    private void mostrarError(String titulo, Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, titulo + ":\n" + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
