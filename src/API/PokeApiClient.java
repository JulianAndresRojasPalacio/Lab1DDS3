package API;

import Model.Card;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;

public class PokeApiClient
{
    public Card consultarPokemon(String nombrePokemon) throws IOException, InterruptedException
    {
        //se crea un cliente http para realizar la peticion
        HttpClient client = HttpClient.newHttpClient();

        //se crea un objeto de tipo request para realizar la peticion
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://pokeapi.co/api/v2/pokemon/" + nombrePokemon))
                .build();

        //ejecutamos la solicitud
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        //200 = ejecucion exitosa
        if (response.statusCode() == 200)
        {
            JSONObject json = new JSONObject(response.body());
            Card card = new Card();

            card.setNombre(json.getString("name"));

            //Tipos: guardamos todos en un texto y el primero aparte
            StringBuilder tipos = new StringBuilder();
            json.getJSONArray("types").forEach(t ->
            {
                JSONObject tipoJson = (JSONObject) t;
                JSONObject nameJson = (JSONObject) tipoJson.get("type");

                if (tipos.length() > 0)
                {
                    tipos.append("/");
                }
                tipos.append(nameJson.getString("name"));
            });
            card.setTipos(tipos.toString());
            card.setTipo(tipos.toString().split("/")[0]);

            //Estadisticas: comparamos el nombre de cada una con if
            json.getJSONArray("stats").forEach(stat ->
            {
                JSONObject statJson = (JSONObject) stat;
                JSONObject nameJson = (JSONObject) statJson.get("stat");

                String nombreStat = nameJson.getString("name");
                int valor = statJson.getInt("base_stat");

                if (nombreStat.equals("hp"))
                {
                    card.setHp(valor);
                }
                if (nombreStat.equals("attack"))
                {
                    card.setAttack(valor);
                }
                if (nombreStat.equals("defense"))
                {
                    card.setDefense(valor);
                }
                if (nombreStat.equals("speed"))
                {
                    card.setSpeed(valor);
                }
            });

            //Imagen frontal (puede venir vacia en algunos pokemon)
            JSONObject imageJson = (JSONObject) json.get("sprites");
            card.setSpriteUrl(imageJson.optString("front_default", ""));

            return card;
        }
        else
        {
            throw new IOException("Pokemon no encontrado: " + nombrePokemon);
        }
    }

    //Pokemon aleatorio: la API tambien acepta el ID en lugar del nombre
    public Card consultarAleatorio() throws IOException, InterruptedException
    {
        int id = new Random().nextInt(1025) + 1;
        return consultarPokemon(String.valueOf(id));
    }

    //Descarga el sprite y lo deja listo para un JLabel
    public ImageIcon descargarImagen(String url) throws IOException
    {
        if (url == null || url.isEmpty())
        {
            return null;
        }

        BufferedImage imagen = ImageIO.read(URI.create(url).toURL());
        Image escalada = imagen.getScaledInstance(128, 128, Image.SCALE_SMOOTH);
        return new ImageIcon(escalada);
    }
}