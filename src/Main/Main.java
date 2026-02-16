package Main;

import UI.UIMainMenu;
import UI.UITheme;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        UITheme.setup();
        SwingUtilities.invokeLater(() -> new UIMainMenu().setVisible(true));
    }
}
