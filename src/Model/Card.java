package Model;

public class Card
{
    private String nombre;
    private String tipo;      // primer tipo (para la efectividad)
    private String tipos;     // todos los tipos, ej: "fire/flying"
    private int hp;
    private int hpActual;
    private int attack;
    private int defense;
    private int speed;
    private String spriteUrl;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getTipos() { return tipos; }
    public void setTipos(String tipos) { this.tipos = tipos; }

    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; this.hpActual = hp; }

    public int getHpActual() { return hpActual; }
    public void setHpActual(int hpActual) { this.hpActual = hpActual; }

    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }

    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }

    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }

    public String getSpriteUrl() { return spriteUrl; }
    public void setSpriteUrl(String spriteUrl) { this.spriteUrl = spriteUrl; }
}