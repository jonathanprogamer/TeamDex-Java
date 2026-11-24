package com.example.teamdex_java.data;

import com.example.teamdex_java.model.PokemonDetalle;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class PokemonInfoRepository {

    private static final String API_URL =
            "https://pokeapi.co/api/v2/pokemon/";

    public static PokemonDetalle obtenerDetalle(int id)
            throws Exception {

        HttpURLConnection conexion = null;

        try {
            URL url = new URL(API_URL + id + "/");

            conexion = (HttpURLConnection) url.openConnection();

            conexion.setRequestMethod("GET");
            conexion.setConnectTimeout(15000);
            conexion.setReadTimeout(20000);
            conexion.setRequestProperty(
                    "Accept",
                    "application/json"
            );

            int codigo = conexion.getResponseCode();

            if (codigo != HttpURLConnection.HTTP_OK) {
                throw new Exception(
                        "Error al consultar Pokémon: " + codigo
                );
            }

            StringBuilder respuesta = new StringBuilder();

            try (
                    InputStream entrada = conexion.getInputStream();
                    BufferedReader lector = new BufferedReader(
                            new InputStreamReader(
                                    entrada,
                                    StandardCharsets.UTF_8
                            )
                    )
            ) {
                String linea;

                while ((linea = lector.readLine()) != null) {
                    respuesta.append(linea);
                }
            }

            JSONObject pokemon = new JSONObject(
                    respuesta.toString()
            );

            JSONObject sprites = pokemon.getJSONObject("sprites");

            String spriteUrl = sprites.optString(
                    "front_default",
                    ""
            );

            String imagenUrl = "";

            JSONObject otros = sprites.optJSONObject("other");

            if (otros != null) {

                JSONObject artwork = otros.optJSONObject(
                        "official-artwork"
                );

                if (artwork != null) {
                    imagenUrl = artwork.optString(
                            "front_default",
                            ""
                    );
                }
            }

            if (imagenUrl.isEmpty()) {
                imagenUrl = spriteUrl;
            }

            JSONArray tiposJson = pokemon.getJSONArray("types");

            ArrayList<String> tipos = new ArrayList<>();

            for (int i = 0; i < tiposJson.length(); i++) {

                JSONObject tipoObjeto =
                        tiposJson.getJSONObject(i);

                JSONObject tipo =
                        tipoObjeto.getJSONObject("type");

                tipos.add(tipo.getString("name"));
            }

            return new PokemonDetalle(
                    id,
                    spriteUrl,
                    imagenUrl,
                    tipos
            );

        } finally {

            if (conexion != null) {
                conexion.disconnect();
            }
        }
    }
}
