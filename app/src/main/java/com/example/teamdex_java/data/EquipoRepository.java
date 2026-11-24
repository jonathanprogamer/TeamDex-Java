
package com.example.teamdex_java.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.teamdex_java.FirebaseManager;
import com.example.teamdex_java.model.Equipo;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class EquipoRepository {

    private static final String PREFS = "teamdex_equipos";
    private static final String KEY_EQUIPOS = "equipos";

    private static final String COLECCION = "teamdex";
    private static final String DOCUMENTO = "equipos";

    private final SharedPreferences preferences;
    private final FirebaseFirestore db;

    public EquipoRepository(Context context) {
        preferences = context.getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
        );

        db = FirebaseManager.obtenerDB();

        sincronizarConFirebase();
    }

    // Obtener equipos guardados localmente.
    public ArrayList<Equipo> obtenerEquipos() {

        ArrayList<Equipo> equipos = new ArrayList<>();
        String contenido = preferences.getString(KEY_EQUIPOS, "[]");
        boolean necesitaActualizar = false;

        try {
            JSONArray array = new JSONArray(contenido);

            for (int i = 0; i < array.length(); i++) {

                JSONObject objeto = array.getJSONObject(i);

                String id = objeto.optString("id", "");

                if (id.isEmpty()) {
                    id = UUID.randomUUID().toString();
                    necesitaActualizar = true;
                }

                String nombre = objeto.getString("nombre");

                ArrayList<String> pokemon = new ArrayList<>();
                JSONArray pokemonsJson = objeto.optJSONArray("pokemon");

                if (pokemonsJson != null) {
                    for (int j = 0; j < pokemonsJson.length(); j++) {
                        pokemon.add(pokemonsJson.getString(j));
                    }
                }

                equipos.add(new Equipo(id, nombre, pokemon));
            }

            if (necesitaActualizar) {
                guardarLocal(equipos);
            }

        } catch (JSONException e) {
            Log.e("TeamDex", "Error leyendo equipos locales", e);
        }

        return equipos;
    }

    // Crear equipo.
    public void guardarEquipo(Equipo equipo) {

        ArrayList<Equipo> equipos = obtenerEquipos();
        equipos.add(equipo);

        guardarLista(equipos);
    }

    // Buscar equipo.
    public Equipo obtenerEquipo(String id) {

        for (Equipo equipo : obtenerEquipos()) {
            if (equipo.getId().equals(id)) {
                return equipo;
            }
        }

        return null;
    }

    // Cambiar nombre.
    public boolean actualizarNombreEquipo(
            String idEquipo,
            String nuevoNombre
    ) {

        ArrayList<Equipo> equipos = obtenerEquipos();

        for (Equipo equipo : equipos) {

            if (equipo.getId().equals(idEquipo)) {

                equipo.setNombre(nuevoNombre);
                guardarLista(equipos);

                return true;
            }
        }

        return false;
    }

    // Eliminar equipo.
    public boolean eliminarEquipo(String idEquipo) {

        ArrayList<Equipo> equipos = obtenerEquipos();

        for (int i = 0; i < equipos.size(); i++) {

            if (equipos.get(i).getId().equals(idEquipo)) {

                equipos.remove(i);
                guardarLista(equipos);

                return true;
            }
        }

        return false;
    }

    // Agregar Pokémon por nombre.
    public boolean agregarPokemon(
            String idEquipo,
            String nombrePokemon
    ) {

        ArrayList<Equipo> equipos = obtenerEquipos();

        for (Equipo equipo : equipos) {

            if (equipo.getId().equals(idEquipo)) {

                if (equipo.getPokemon().size() >= 6) {
                    return false;
                }

                equipo.getPokemon().add(nombrePokemon);
                guardarLista(equipos);

                return true;
            }
        }

        return false;
    }

    // Agregar Pokémon desde la Pokédex.
    public boolean agregarPokemonApi(
            String idEquipo,
            int idPokemon,
            String nombrePokemon
    ) {

        ArrayList<Equipo> equipos = obtenerEquipos();

        String identificador = idPokemon + "|" + nombrePokemon;

        for (Equipo equipo : equipos) {

            if (equipo.getId().equals(idEquipo)) {

                if (equipo.getPokemon().size() >= 6) {
                    return false;
                }

                for (String integrante : equipo.getPokemon()) {

                    if (integrante.equalsIgnoreCase(identificador)) {
                        return false;
                    }

                    if (integrante.contains("|")) {

                        String[] partes = integrante.split("\\|", 2);

                        if (partes.length == 2
                                && partes[0].equals(String.valueOf(idPokemon))) {
                            return false;
                        }
                    }
                }

                equipo.getPokemon().add(identificador);
                guardarLista(equipos);

                return true;
            }
        }

        return false;
    }

    // Eliminar Pokémon de un equipo.
    public void eliminarPokemon(String idEquipo, int posicion) {

        ArrayList<Equipo> equipos = obtenerEquipos();

        for (Equipo equipo : equipos) {

            if (equipo.getId().equals(idEquipo)) {

                if (posicion >= 0
                        && posicion < equipo.getPokemon().size()) {

                    equipo.getPokemon().remove(posicion);
                    guardarLista(equipos);
                }

                return;
            }
        }
    }

    // Guardar cambios localmente y enviarlos a Firebase.
    private void guardarLista(ArrayList<Equipo> equipos) {

        guardarLocal(equipos);

        String datos = convertirAJson(equipos);

        Map<String, Object> documento = new java.util.HashMap<>();
        documento.put("datos", datos);
        documento.put("actualizado", System.currentTimeMillis());

        db.collection(COLECCION)
                .document(DOCUMENTO)
                .set(documento)
                .addOnSuccessListener(unused ->
                        Log.d("TeamDex", "Equipos sincronizados con Firebase")
                )
                .addOnFailureListener(e ->
                        Log.e("TeamDex", "Error al guardar en Firebase", e)
                );
    }

    // Guardar únicamente en el teléfono.
    private void guardarLocal(ArrayList<Equipo> equipos) {

        String datos = convertirAJson(equipos);

        preferences.edit()
                .putString(KEY_EQUIPOS, datos)
                .apply();
    }

    // Convertir equipos a JSON.
    private String convertirAJson(ArrayList<Equipo> equipos) {

        JSONArray array = new JSONArray();

        try {
            for (Equipo equipo : equipos) {

                JSONObject objeto = new JSONObject();

                objeto.put("id", equipo.getId());
                objeto.put("nombre", equipo.getNombre());

                JSONArray pokemonArray = new JSONArray();

                for (String pokemon : equipo.getPokemon()) {
                    pokemonArray.put(pokemon);
                }

                objeto.put("pokemon", pokemonArray);
                array.put(objeto);
            }

        } catch (JSONException e) {
            Log.e("TeamDex", "Error convirtiendo equipos a JSON", e);
        }

        return array.toString();
    }

    // Recuperar equipos de Firebase y combinarlos con los locales.
    private void sincronizarConFirebase() {

        db.collection(COLECCION)
                .document(DOCUMENTO)
                .get()
                .addOnSuccessListener(snapshot -> {

                    ArrayList<Equipo> locales = obtenerEquipos();

                    if (!snapshot.exists()) {

                        // Primera sincronización: subir equipos locales.
                        guardarLista(locales);
                        return;
                    }

                    String datos = snapshot.getString("datos");

                    ArrayList<Equipo> remotos = convertirDesdeJson(datos);

                    // Combinar sin duplicar equipos.
                    LinkedHashMap<String, Equipo> combinados =
                            new LinkedHashMap<>();

                    for (Equipo equipo : remotos) {
                        combinados.put(equipo.getId(), equipo);
                    }

                    // Si existe el mismo ID, prevalece la copia local.
                    for (Equipo equipo : locales) {
                        combinados.put(equipo.getId(), equipo);
                    }

                    ArrayList<Equipo> resultado =
                            new ArrayList<>(combinados.values());

                    guardarLocal(resultado);

                    // Dejar la versión combinada en Firebase.
                    String jsonFinal = convertirAJson(resultado);

                    Map<String, Object> documento = new java.util.HashMap<>();
                    documento.put("datos", jsonFinal);
                    documento.put("actualizado", System.currentTimeMillis());

                    db.collection(COLECCION)
                            .document(DOCUMENTO)
                            .set(documento)
                            .addOnFailureListener(e ->
                                    Log.e("TeamDex", "Error actualizando sincronización", e)
                            );

                    Log.d("TeamDex", "Sincronización inicial completada");

                })
                .addOnFailureListener(e ->
                        Log.e("TeamDex", "Error recuperando equipos de Firebase", e)
                );
    }

    // Convertir JSON de Firebase a equipos.
    private ArrayList<Equipo> convertirDesdeJson(String contenido) {

        ArrayList<Equipo> equipos = new ArrayList<>();

        if (contenido == null || contenido.isEmpty()) {
            return equipos;
        }

        try {
            JSONArray array = new JSONArray(contenido);

            for (int i = 0; i < array.length(); i++) {

                JSONObject objeto = array.getJSONObject(i);

                String id = objeto.optString("id", "");
                String nombre = objeto.optString("nombre", "Mi equipo");

                if (id.isEmpty()) {
                    id = UUID.randomUUID().toString();
                }

                ArrayList<String> pokemon = new ArrayList<>();
                JSONArray pokemonsJson = objeto.optJSONArray("pokemon");

                if (pokemonsJson != null) {
                    for (int j = 0; j < pokemonsJson.length(); j++) {
                        pokemon.add(pokemonsJson.getString(j));
                    }
                }

                equipos.add(new Equipo(id, nombre, pokemon));
            }

        } catch (JSONException e) {
            Log.e("TeamDex", "Error leyendo equipos de Firebase", e);
        }

        return equipos;
    }
}
