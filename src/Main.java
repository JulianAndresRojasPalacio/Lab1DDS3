import UI.Stadium;

import javax.swing.*;

public class Main
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() ->
        {
            JFrame frame = new JFrame("Pokémon Stadium Lite");
            Stadium stadium = new Stadium();

            frame.setContentPane(stadium.getMainPanel());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 650);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}