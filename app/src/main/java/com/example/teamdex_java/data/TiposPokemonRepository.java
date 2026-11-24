
package com.example.teamdex_java.data;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TiposPokemonRepository {

    public interface Callback {
        void onTiposObtenidos(List<String> tipos);
    }

    private static final ConcurrentHashMap<Integer, List<String>> CACHE =
            new ConcurrentHashMap<>();

    private static final ExecutorService EJECUTOR =
            Executors.newFixedThreadPool(4);

    private static final Handler MAIN =
            new Handler(Looper.getMainLooper());

    public static void obtenerTipos(int idPokemon, Callback callback) {

        if (idPokemon <= 0) {
            MAIN.post(() -> callback.onTiposObtenidos(new ArrayList<>()));
            return;
        }

        List<String> guardados = CACHE.get(idPokemon);

        if (guardados != null) {
            callback.onTiposObtenidos(guardados);
            return;
        }

        EJECUTOR.execute(() -> {

            ArrayList<String> tipos = new ArrayList<>();
            HttpURLConnection conexion = null;
            boolean correcto = false;

            try {
                URL url = new URL(
                        "https://pokeapi.co/api/v2/pokemon/"
                                + idPokemon + "/"
                );

                conexion = (HttpURLConnection) url.openConnection();
                conexion.setRequestMethod("GET");
                conexion.setConnectTimeout(10000);
                conexion.setReadTimeout(15000);
                conexion.setRequestProperty(
                        "Accept", "application/json"
                );

                if (conexion.getResponseCode()
                        != HttpURLConnection.HTTP_OK) {
                    throw new Exception("Error al consultar los tipos");
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

                    JSONObject objeto =
                            new JSONObject(respuesta.toString());

                    JSONArray listaTipos = objeto.getJSONArray("types");

                    for (int i = 0; i < listaTipos.length(); i++) {
                        JSONObject elemento =
                                listaTipos.getJSONObject(i);

                        String nombreTipo = elemento
                                .getJSONObject("type")
                                .getString("name");

                        tipos.add(nombreTipo);
                    }

                    correcto = true;
                }

            } catch (Exception e) {
                e.printStackTrace();

            } finally {
                if (conexion != null) {
                    conexion.disconnect();
                }
            }

            if (correcto) {
                CACHE.put(idPokemon, tipos);
            }

            MAIN.post(() -> callback.onTiposObtenidos(tipos));
        });
    }
}
