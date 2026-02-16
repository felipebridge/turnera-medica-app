package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class UIMainMenu extends JFrame {

    public UIMainMenu() {
        setTitle("Turnera Médica");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 620);
        setLocationRelativeTo(null);

        setContentPane(buildRoot());
    }

    private JComponent buildRoot() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        return root;
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 8));
        header.setBorder(new EmptyBorder(12, 14, 12, 14));
        header.setOpaque(true);
        header.setBackground(new Color(245, 246, 248));

        JLabel title = new JLabel("Turnera Médica");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));

        JLabel subtitle = new JLabel("Gestión completa: médicos, pacientes, turnos y reportes");
        subtitle.setForeground(new Color(90, 90, 90));

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 2));
        text.setOpaque(false);
        text.add(title);
        text.add(subtitle);

        header.add(text, BorderLayout.WEST);

        JPanel quick = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        quick.setOpaque(false);

        JButton btnSalir = new JButton("Salir");
        btnSalir.addActionListener(e -> dispose());

        quick.add(btnSalir);
        header.add(quick, BorderLayout.EAST);

        return header;
    }

    private JComponent buildCenter() {
        JPanel center = new JPanel(new BorderLayout(12, 12));

        // Panel izquierda
        JPanel nav = new JPanel(new GridBagLayout());
        nav.setBorder(BorderFactory.createTitledBorder("Menú"));
        nav.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JButton btnMedicos = bigButton("Médicos");
        JButton btnPacientes = bigButton("Pacientes");
        JButton btnTurnos = bigButton("Turnos");

        btnMedicos.addActionListener(e -> new UIMedico().setVisible(true));
        btnPacientes.addActionListener(e -> new UIPaciente().setVisible(true));
        btnTurnos.addActionListener(e -> new UITurno().setVisible(true));

        gbc.gridx = 0; gbc.gridy = 0; nav.add(btnMedicos, gbc);
        gbc.gridy = 1; nav.add(btnPacientes, gbc);
        gbc.gridy = 2; nav.add(btnTurnos, gbc);

        // Panel derecho: info
        JPanel info = new JPanel(new BorderLayout());
        info.setBorder(BorderFactory.createTitledBorder("Panel"));
        info.setOpaque(false);

        JTextArea txt = new JTextArea();
        txt.setEditable(false);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setOpaque(false);
        txt.setFont(txt.getFont().deriveFont(13f));
        txt.setText(
                "✅ Recomendación de uso:\n\n" +
                        "1) Cargá Médicos y Pacientes.\n" +
                        "2) Configurá Consultorios.\n" +
                        "3) Generá Turnos desde la Agenda (calendario).\n" +
                        "4) Usá el Reporte entre fechas para exportar CSV.\n\n" +
                        "Atajos:\n" +
                        "• En pantallas ABM: Enter = Guardar | Esc = Nuevo\n"
        );

        info.add(txt, BorderLayout.CENTER);

        // Layout centro
        center.add(nav, BorderLayout.WEST);

        // Ajuste de anchos
        nav.setPreferredSize(new Dimension(420, 10));

        return center;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(new EmptyBorder(6, 4, 0, 4));

        JLabel status = new JLabel("Listo. Seleccioná una opción del menú para comenzar.");
        status.setForeground(new Color(90, 90, 90));

        footer.add(status, BorderLayout.WEST);
        return footer;
    }

    private JButton bigButton(String title) {
        JButton b = new JButton("<html><div style='padding:6px 2px;'>" +
                "<div style='font-size:13px; font-weight:700;'>" + escape(title) + "</div>" +
                "</div></html>");

        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setPreferredSize(new Dimension(380, 64));
        b.setFocusPainted(false);
        return b;
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
