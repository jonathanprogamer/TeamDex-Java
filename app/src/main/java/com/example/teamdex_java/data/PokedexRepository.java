
package com.example.teamdex_java.data;

import com.example.teamdex_java.model.Pokemon;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class PokedexRepository {

    private static final String API_URL =
            "https://pokeapi.co/api/v2/pokemon?limit=2000";

    public static ArrayList<Pokemon> obtenerPokemon() throws Exception {

        ArrayList<Pokemon> listaPokemon = new ArrayList<>();

        HttpURLConnection conexion = null;

        try {

            URL url = new URL(API_URL);

            conexion = (HttpURLConnection) url.openConnection();

            conexion.setRequestMethod("GET");
            conexion.setConnectTimeout(15000);
            conexion.setReadTimeout(30000);
            conexion.setRequestProperty("Accept", "application/json");

            int codigoRespuesta = conexion.getResponseCode();

            if (codigoRespuesta != HttpURLConnection.HTTP_OK) {
                throw new IOException(
                        "Error al consultar PokéAPI: " + codigoRespuesta
                );
            }

            try (
                    InputStream entrada = conexion.getInputStream();
                    BufferedReader lector = new BufferedReader(
                            new InputStreamReader(
                                    entrada,
                                    StandardCharsets.UTF_8
                            )
                    )
            ) {

                StringBuilder respuesta = new StringBuilder();

                String linea;

                while ((linea = lector.readLine()) != null) {
                    respuesta.append(linea);
                }

                JSONObject objeto = new JSONObject(respuesta.toString());
                JSONArray resultados = objeto.getJSONArray("results");

                // Primero guardamos los números de los Pokémon originales.
                Map<String, Integer> numerosNacionales = new HashMap<>();

                for (int i = 0; i < resultados.length(); i++) {

                    JSONObject pokemonJson = resultados.getJSONObject(i);

                    String nombre = pokemonJson.getString("name");
                    String pokemonUrl = pokemonJson.getString("url");

                    int id = obtenerIdDesdeUrl(pokemonUrl);

                    if (id >= 1 && id <= 1025) {
                        numerosNacionales.put(nombre, id);
                    }
                }

                // Después agregamos Pokémon originales y formas regionales.
                for (int i = 0; i < resultados.length(); i++) {

                    JSONObject pokemonJson = resultados.getJSONObject(i);

                    String nombre = pokemonJson.getString("name");
                    String pokemonUrl = pokemonJson.getString("url");

                    int id = obtenerIdDesdeUrl(pokemonUrl);

                    boolean pokemonNacional = id <= 1025;
                    boolean varianteRegional = esVarianteRegional(nombre);

                    if (pokemonNacional) {

                        listaPokemon.add(
                                new Pokemon(id, nombre, id)
                        );

                    } else if (varianteRegional) {

                        String nombreBase = obtenerNombreBase(nombre);

                        Integer numeroNacional =
                                numerosNacionales.get(nombreBase);

                        // Evitamos mostrar variantes sin número nacional identificado.
                        if (numeroNacional != null) {

                            listaPokemon.add(
                                    new Pokemon(
                                            id,
                                            nombre,
                                            numeroNacional
                                    )
                            );
                        }
                    }
                }
            }

        } finally {

            if (conexion != null) {
                conexion.disconnect();
            }
        }

        Collections.sort(
                listaPokemon,
                Comparator
                        .comparingInt(
                                (Pokemon p) -> obtenerOrdenRegion(p.getRegion())
                        )
                        .thenComparingInt(Pokemon::getNumeroNacional)
                        .thenComparingInt(Pokemon::getId)
        );

        return listaPokemon;
    }

    private static int obtenerIdDesdeUrl(String pokemonUrl) {

        String numeroTexto = pokemonUrl.replaceAll(
                ".*/(\\d+)/?$",
                "$1"
        );

        return Integer.parseInt(numeroTexto);
    }

    private static boolean esVarianteRegional(String nombre) {

        return nombre.contains("-alola")
                || nombre.contains("-galar")
                || nombre.contains("-hisui")
                || nombre.contains("-paldea");
    }

    private static String obtenerNombreBase(String nombre) {

        String[] sufijos = {
                "-alola",
                "-galar",
                "-hisui",
                "-paldea"
        };

        for (String sufijo : sufijos) {

            int posicion = nombre.indexOf(sufijo);

            if (posicion != -1) {
                return nombre.substring(0, posicion);
            }
        }

        return nombre;
    }

    private static int obtenerOrdenRegion(String region) {

        switch (region) {

            case "Kanto": return 0;
            case "Johto": return 1;
            case "Hoenn": return 2;
            case "Sinnoh": return 3;
            case "Teselia": return 4;
            case "Kalos": return 5;
            case "Alola": return 6;
            case "Galar": return 7;
            case "Paldea": return 8;

            case "Formas de Alola": return 9;
            case "Formas de Galar": return 10;
            case "Formas de Hisui": return 11;
            case "Formas de Paldea": return 12;

            default: return 13;
        }
    }
}
