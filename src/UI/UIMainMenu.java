package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;
import java.io.File;

public class UIMainMenu extends JFrame {

    private static final String LOGO_RESOURCE = "/img/logo_turnera.png";
    private static final String LOGO_FILE_FALLBACK = "logo_turnera.png";

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

        // Panel izquierda (Menú)
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

        nav.setPreferredSize(new Dimension(420, 10));

        // Panel derecho
        JPanel right = buildLogoPanel();

        center.add(nav, BorderLayout.WEST);
        center.add(right, BorderLayout.CENTER);

        return center;
    }

    private JPanel buildLogoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setOpaque(false);

        JLabel logoLabel = new JLabel();
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        ImageIcon logo = loadScaledLogo(420, 420);
        if (logo != null) {
            logoLabel.setIcon(logo);
            setIconImage(logo.getImage());
        } else {
            logoLabel.setText("No se encontró el logo (revisá la ruta /img/logo_turnera.png)");
            logoLabel.setForeground(new Color(140, 60, 60));
        }

        JTextArea help = new JTextArea();
        help.setEditable(false);
        help.setLineWrap(true);
        help.setWrapStyleWord(true);
        help.setOpaque(false);
        help.setFont(help.getFont().deriveFont(12.5f));
        help.setForeground(new Color(80, 80, 80));
        help.setText(
                "Recomendación de uso:\n" +
                        "1) Cargá Médicos y Pacientes.\n" +
                        "2) Configurá Consultorios.\n" +
                        "3) Generá Turnos desde la Agenda.\n" +
                        "4) Usá Reportes entre fechas.\n"
        );

        JPanel helpWrap = new JPanel(new BorderLayout());
        helpWrap.setOpaque(false);
        helpWrap.setBorder(new EmptyBorder(12, 60, 0, 60));
        helpWrap.add(help, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.CENTER;

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(logoLabel, gbc);

        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 1; // empuja hacia arriba, mantiene estética
        panel.add(helpWrap, gbc);

        return panel;
    }

    private ImageIcon loadScaledLogo(int maxW, int maxH) {
        try {
            URL url = getClass().getResource(LOGO_RESOURCE);
            if (url != null) {
                return scaleIcon(new ImageIcon(url), maxW, maxH);
            }
        } catch (Exception ignored) {}

        try {
            File f = new File(LOGO_FILE_FALLBACK);
            if (f.exists()) {
                return scaleIcon(new ImageIcon(f.getAbsolutePath()), maxW, maxH);
            }
        } catch (Exception ignored) {}

        return null;
    }

    private ImageIcon scaleIcon(ImageIcon original, int maxW, int maxH) {
        int ow = original.getIconWidth();
        int oh = original.getIconHeight();
        if (ow <= 0 || oh <= 0) return null;

        double scale = Math.min((double) maxW / ow, (double) maxH / oh);
        int nw = (int) Math.round(ow * scale);
        int nh = (int) Math.round(oh * scale);

        Image scaled = original.getImage().getScaledInstance(nw, nh, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
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
