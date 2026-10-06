import UI.Stadium;

import javax.swing.*;
import java.awt.Color;

public class Main
{
    public static void main(String[] args)
    {
        UIManager.put("ProgressBar.selectionForeground", Color.WHITE);
        UIManager.put("ProgressBar.selectionBackground", Color.WHITE);

        SwingUtilities.invokeLater(() ->
        {
            JFrame frame = new JFrame("Pokémon Stadium Lite");
            Stadium stadium = new Stadium();

            frame.setContentPane(stadium.getMainPanel());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(820, 700);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}