package UI;

import API.PokeApiClient;
import Logic.BattleListener;
import Logic.Duel;
import Model.Card;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class Stadium implements BattleListener
{
    // ---- Componentes del Stadium.form ----
    private JPanel mainPanel;
    private JPanel player1;
    private JPanel player2;
    private JPanel logs;
    private JLabel titulo;
    private JLabel fotoplayerp1;
    private JLabel fotoplayer2;
    private JTextField nombrep1;
    private JTextField statsp1;
    private JButton batalla;
    private JButton RANDOMP1;
    private JButton LOADP1;
    private JProgressBar hpP1;
    private JTextField nombrep2;
    private JTextField statsp2;
    private JButton RANDOMP2;
    private JButton LOADP2;
    private JProgressBar hpP2;
    private JTextField BATTLELOG;
    private JTextArea logscombat;

    // ---- Datos ----
    private PokeApiClient api = new PokeApiClient();
    private Card pokemon1;
    private Card pokemon2;

    // ---- Colores ----
    private static final Color FONDO = new Color(25, 26, 28);
    private static final Color CAMPO = new Color(40, 42, 46);
    private static final Color AMARILLO = new Color(255, 203, 5);
    private static final Color AZUL = new Color(59, 76, 202);
    private static final Color ROJO = new Color(204, 0, 0);
    private static final Color GRIS = new Color(90, 93, 100);
    private static final Color VERDE = new Color(76, 175, 80);
    private static final Color NARANJA = new Color(255, 152, 0);

    public Stadium()
    {
        aplicarEstilos();

        //Fight! deshabilitado hasta que ambos esten cargados
        batalla.setEnabled(false);
        logscombat.setEditable(false);

        //ActionListener de cada boton
        RANDOMP1.addActionListener(e -> cargarPokemon(1, null));
        RANDOMP2.addActionListener(e -> cargarPokemon(2, null));
        LOADP1.addActionListener(e -> cargarPokemon(1, nombrep1.getText().trim().toLowerCase()));
        LOADP2.addActionListener(e -> cargarPokemon(2, nombrep2.getText().trim().toLowerCase()));
        batalla.addActionListener(e -> iniciarCombate());
    }

    public JPanel getMainPanel()
    {
        return mainPanel;
    }

    // ================= CARGA DE POKEMON =================

    //nombre == null significa aleatorio
    private void cargarPokemon(int jugador, String nombre)
    {
        if (nombre != null && nombre.isEmpty())
        {
            JOptionPane.showMessageDialog(null, "Escribe el nombre de un pokemon");
            return;
        }

        batalla.setEnabled(false);

        //Hilo aparte para no bloquear la UI mientras se consulta la API
        new Thread(() ->
        {
            try
            {
                Card card = (nombre == null) ? api.consultarAleatorio() : api.consultarPokemon(nombre);
                ImageIcon sprite = api.descargarImagen(card.getSpriteUrl());

                //La UI solo se toca desde el hilo de Swing
                SwingUtilities.invokeLater(() -> mostrarPokemon(jugador, card, sprite));
            }
            catch (Exception ex)
            {
                SwingUtilities.invokeLater(() ->
                {
                    logscombat.append("Error: " + ex.getMessage() + "\n");
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                    habilitarFight();
                });
            }
        }).start();
    }

    private void mostrarPokemon(int jugador, Card card, ImageIcon sprite)
    {
        String stats = card.getTipos() + " | HP:" + card.getHp() + " ATK:" + card.getAttack()
                + " DEF:" + card.getDefense() + " SPD:" + card.getSpeed();

        if (jugador == 1)
        {
            pokemon1 = card;
            nombrep1.setText(card.getNombre());
            statsp1.setText(stats);
            fotoplayerp1.setIcon(sprite);
            fotoplayerp1.setText(sprite == null ? "sin imagen" : "");
            reiniciarBarra(hpP1, card);
            estilizarPanel(player1, "PLAYER 1", colorPorTipo(card.getTipo()));
        }
        else
        {
            pokemon2 = card;
            nombrep2.setText(card.getNombre());
            statsp2.setText(stats);
            fotoplayer2.setIcon(sprite);
            fotoplayer2.setText(sprite == null ? "sin imagen" : "");
            reiniciarBarra(hpP2, card);
            estilizarPanel(player2, "PLAYER 2", colorPorTipo(card.getTipo()));
        }

        logscombat.append("Jugador " + jugador + " cargo a " + card.getNombre() + "\n");
        habilitarFight();
    }

    private void habilitarFight()
    {
        batalla.setEnabled(pokemon1 != null && pokemon2 != null);
    }

    // ================= COMBATE =================

    private void iniciarCombate()
    {
        //Restauramos el HP por si ya hubo un combate antes
        pokemon1.setHpActual(pokemon1.getHp());
        pokemon2.setHpActual(pokemon2.getHp());
        reiniciarBarra(hpP1, pokemon1);
        reiniciarBarra(hpP2, pokemon2);

        logscombat.setText("");
        logscombat.append("¡Comienza el combate: " + pokemon1.getNombre() + " VS " + pokemon2.getNombre() + "!\n");

        batalla.setEnabled(false);
        RANDOMP1.setEnabled(false);
        RANDOMP2.setEnabled(false);
        LOADP1.setEnabled(false);
        LOADP2.setEnabled(false);

        Duel duel = new Duel(pokemon1, pokemon2, this);
        duel.iniciar();
    }

    // ================= BattleListener =================
    // Duel corre en otro hilo, por eso todo va dentro de invokeLater

    @Override
    public void onTurn(String attacker, String defender, int damage, boolean critical, double modifier)
    {
        SwingUtilities.invokeLater(() ->
        {
            String texto = attacker + " ataca a " + defender + " -> daño: " + damage;

            if (critical)
            {
                texto += " ¡CRITICO!";
            }
            if (modifier > 1.0)
            {
                texto += " (super efectivo)";
            }
            if (modifier < 1.0)
            {
                texto += " (poco efectivo)";
            }

            logscombat.append(texto + "\n");
            logscombat.setCaretPosition(logscombat.getDocument().getLength());
        });
    }

    @Override
    public void onHpChanged(String pokemon, int hpActual)
    {
        SwingUtilities.invokeLater(() ->
        {
            if (pokemon.equals(pokemon1.getNombre()))
            {
                actualizarBarra(hpP1, hpActual);
            }
            else
            {
                actualizarBarra(hpP2, hpActual);
            }

            logscombat.append(pokemon + " HP restante: " + hpActual + "\n");
            logscombat.setCaretPosition(logscombat.getDocument().getLength());
        });
    }

    @Override
    public void onBattleEnded(String winner)
    {
        SwingUtilities.invokeLater(() ->
        {
            logscombat.append("\n*** GANADOR: " + winner.toUpperCase() + " ***\n");
            logscombat.setCaretPosition(logscombat.getDocument().getLength());

            RANDOMP1.setEnabled(true);
            RANDOMP2.setEnabled(true);
            LOADP1.setEnabled(true);
            LOADP2.setEnabled(true);
            habilitarFight();
        });
    }

    // ================= BARRAS DE HP =================

    private void reiniciarBarra(JProgressBar barra, Card card)
    {
        barra.setMinimum(0);
        barra.setMaximum(card.getHp());
        actualizarBarra(barra, card.getHpActual());
    }

    private void actualizarBarra(JProgressBar barra, int hpActual)
    {
        barra.setValue(hpActual);
        barra.setString("HP " + hpActual + " / " + barra.getMaximum());

        //Verde, naranja o rojo segun el porcentaje de vida
        double porcentaje = (double) hpActual / barra.getMaximum();
        if (porcentaje > 0.5)
        {
            barra.setForeground(VERDE);
        }
        else if (porcentaje > 0.2)
        {
            barra.setForeground(NARANJA);
        }
        else
        {
            barra.setForeground(ROJO);
        }
    }

    // ================= ESTILOS =================

    private void aplicarEstilos()
    {
        mainPanel.setBackground(FONDO);
        estilizarPanel(player1, "PLAYER 1", AMARILLO);
        estilizarPanel(player2, "PLAYER 2", AMARILLO);
        estilizarPanel(logs, null, AMARILLO);

        //Titulo (solo si le pusiste field name "titulo" en el form)
        if (titulo != null)
        {
            titulo.setForeground(AMARILLO);
            titulo.setFont(new Font("Monospaced", Font.BOLD, 26));
            titulo.setHorizontalAlignment(SwingConstants.CENTER);
        }

        for (JLabel foto : new JLabel[]{fotoplayerp1, fotoplayer2})
        {
            foto.setHorizontalAlignment(SwingConstants.CENTER);
            foto.setForeground(Color.GRAY);
            foto.setPreferredSize(new Dimension(160, 160));
        }

        estilizarCampo(nombrep1, true);
        estilizarCampo(nombrep2, true);
        estilizarCampo(statsp1, false);
        estilizarCampo(statsp2, false);
        nombrep1.setHorizontalAlignment(JTextField.CENTER);
        nombrep2.setHorizontalAlignment(JTextField.CENTER);
        statsp1.setHorizontalAlignment(JTextField.CENTER);
        statsp2.setHorizontalAlignment(JTextField.CENTER);

        //Titulo del log
        estilizarCampo(BATTLELOG, false);
        BATTLELOG.setHorizontalAlignment(JTextField.CENTER);
        BATTLELOG.setForeground(AMARILLO);

        estilizarBoton(RANDOMP1, AZUL);
        estilizarBoton(RANDOMP2, AZUL);
        estilizarBoton(LOADP1, GRIS);
        estilizarBoton(LOADP2, GRIS);
        estilizarBoton(batalla, ROJO);
        batalla.setFont(new Font("Monospaced", Font.BOLD, 18));

        for (JProgressBar barra : new JProgressBar[]{hpP1, hpP2})
        {
            barra.setStringPainted(true);
            barra.setString("HP");
            barra.setBackground(CAMPO);
            barra.setForeground(VERDE);
            barra.setBorder(BorderFactory.createLineBorder(AMARILLO));
        }

        logscombat.setBackground(FONDO);
        logscombat.setForeground(new Color(0, 230, 118));
        logscombat.setCaretColor(Color.WHITE);
        logscombat.setFont(new Font("Monospaced", Font.PLAIN, 13));
        logscombat.setLineWrap(true);
        logscombat.setWrapStyleWord(true);
    }

    private void estilizarPanel(JPanel panel, String titulo, Color color)
    {
        panel.setBackground(FONDO);

        if (titulo != null)
        {
            panel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(color, 2),
                    titulo,
                    TitledBorder.CENTER,
                    TitledBorder.TOP,
                    new Font("Monospaced", Font.BOLD, 14),
                    color));
        }
        else
        {
            panel.setBorder(BorderFactory.createLineBorder(color, 2));
        }
    }

    private void estilizarCampo(JTextField campo, boolean editable)
    {
        campo.setEditable(editable);
        campo.setBackground(CAMPO);
        campo.setForeground(Color.WHITE);
        campo.setCaretColor(Color.WHITE);
        campo.setFont(new Font("Monospaced", Font.BOLD, 13));
        campo.setBorder(BorderFactory.createLineBorder(AZUL));
    }

    private void estilizarBoton(JButton boton, Color fondo)
    {
        boton.setBackground(fondo);
        boton.setForeground(Color.WHITE);
        boton.setFont(new Font("Monospaced", Font.BOLD, 13));
        boton.setFocusPainted(false);
        boton.setOpaque(true);
        boton.setBorderPainted(false);
    }

    //Color del panel segun el primer tipo del pokemon
    private Color colorPorTipo(String tipo)
    {
        if (tipo.equals("fire")) { return new Color(240, 128, 48); }
        if (tipo.equals("water")) { return new Color(104, 144, 240); }
        if (tipo.equals("grass")) { return new Color(120, 200, 80); }
        if (tipo.equals("electric")) { return new Color(248, 208, 48); }
        if (tipo.equals("psychic")) { return new Color(248, 88, 136); }
        if (tipo.equals("ice")) { return new Color(152, 216, 216); }
        if (tipo.equals("fighting")) { return new Color(192, 48, 40); }
        if (tipo.equals("poison")) { return new Color(160, 64, 160); }
        if (tipo.equals("ground")) { return new Color(224, 192, 104); }
        if (tipo.equals("flying")) { return new Color(168, 144, 240); }
        if (tipo.equals("bug")) { return new Color(168, 184, 32); }
        if (tipo.equals("rock")) { return new Color(184, 160, 56); }
        if (tipo.equals("ghost")) { return new Color(112, 88, 152); }
        if (tipo.equals("dragon")) { return new Color(112, 56, 248); }
        if (tipo.equals("dark")) { return new Color(112, 88, 72); }
        if (tipo.equals("steel")) { return new Color(184, 184, 208); }
        if (tipo.equals("fairy")) { return new Color(238, 153, 172); }
        if (tipo.equals("normal")) { return new Color(168, 168, 120); }

        return AMARILLO;
    }
}