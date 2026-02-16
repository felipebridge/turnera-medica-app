package UI;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

public final class UITheme {

    private UITheme() {}

    public static void setup() {
        FlatLightLaf.setup();

        Font base = new Font("Segoe UI", Font.PLAIN, 13);
        UIManager.put("defaultFont", base);

        UIManager.put("Component.arc", 14);
        UIManager.put("Button.arc", 16);
        UIManager.put("TextComponent.arc", 14);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));

        UIManager.put("Table.rowHeight", 28);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.cellMargins", new Insets(0, 10, 0, 10));
        UIManager.put("TableHeader.height", 34);
        UIManager.put("TableHeader.font", base.deriveFont(Font.BOLD, 13f));

        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);

        UIManager.put("TitlePane.unifiedBackground", true);

        FlatLaf.updateUI();
    }
}
