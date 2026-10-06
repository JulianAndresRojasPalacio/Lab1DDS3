package Logic;

import Model.Card;

import java.util.Random;

public class Duel
{
    private Card pokemon1;
    private Card pokemon2;
    private BattleListener listener;
    private Random random = new Random();

    public Duel(Card pokemon1, Card pokemon2, BattleListener listener)
    {
        this.pokemon1 = pokemon1;
        this.pokemon2 = pokemon2;
        this.listener = listener;
    }

    //El combate corre en otro hilo para no congelar la UI
    public void iniciar()
    {
        new Thread(() -> pelear()).start();
    }

    private void pelear()
    {
        Card atacante;
        Card defensor;

        //Inicia el de mayor Speed; si empatan, es aleatorio
        if (pokemon1.getSpeed() > pokemon2.getSpeed())
        {
            atacante = pokemon1;
            defensor = pokemon2;
        }
        else if (pokemon1.getSpeed() < pokemon2.getSpeed())
        {
            atacante = pokemon2;
            defensor = pokemon1;
        }
        else if (random.nextBoolean())
        {
            atacante = pokemon1;
            defensor = pokemon2;
        }
        else
        {
            atacante = pokemon2;
            defensor = pokemon1;
        }

        boolean terminado = false;

        while (!terminado)
        {
            /*
             * Formula elegida: daño = ATK * random(0-1) - (DEF * 0.5) * random(0-1)
             * La defensa pesa la mitad para que los combates no se alarguen.
             * Minimo 1 de daño. Critico 10% (x1.5). Efectividad x1.3 / x0.7 / x1.0
             */
            double base = atacante.getAttack() * Math.random() - (defensor.getDefense() * 0.5) * Math.random();

            boolean critico = random.nextInt(100) < 10;
            double modificador = calcularEfectividad(atacante.getTipo(), defensor.getTipo());

            double total = base * modificador;
            if (critico)
            {
                total = total * 1.5;
            }

            int damage = (int) Math.round(total);
            if (damage < 1)
            {
                damage = 1;
            }

            //El HP no puede ser negativo
            int nuevoHp = Math.max(0, defensor.getHpActual() - damage);
            defensor.setHpActual(nuevoHp);

            listener.onTurn(atacante.getNombre(), defensor.getNombre(), damage, critico, modificador);
            listener.onHpChanged(defensor.getNombre(), nuevoHp);

            if (nuevoHp == 0)
            {
                listener.onBattleEnded(atacante.getNombre());
                terminado = true;
            }
            else
            {
                //intercambiamos los roles
                Card temporal = atacante;
                atacante = defensor;
                defensor = temporal;

                try
                {
                    Thread.sleep(900);
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    //Agua > Fuego, Fuego > Planta, Planta > Agua (x1.3); lo inverso x0.7; resto x1.0
    private double calcularEfectividad(String atacante, String defensor)
    {
        if (atacante.equals("water") && defensor.equals("fire"))
        {
            return 1.3;
        }
        if (atacante.equals("fire") && defensor.equals("grass"))
        {
            return 1.3;
        }
        if (atacante.equals("grass") && defensor.equals("water"))
        {
            return 1.3;
        }

        if (atacante.equals("fire") && defensor.equals("water"))
        {
            return 0.7;
        }
        if (atacante.equals("grass") && defensor.equals("fire"))
        {
            return 0.7;
        }
        if (atacante.equals("water") && defensor.equals("grass"))
        {
            return 0.7;
        }

        return 1.0;
    }
}