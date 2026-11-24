
package com.example.teamdex_java.ui;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamdex_java.R;
import com.example.teamdex_java.data.EquipoRepository;
import com.example.teamdex_java.data.PokedexRepository;
import com.example.teamdex_java.model.Pokemon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PokedexActivity extends AppCompatActivity {

    private Button btnVolverPokedex;
    private Button btnTabPokemon;
    private Button btnTabFavoritos;
    private Button btnIrRegion;

    private EditText edtBuscarPokemon;
    private TextView txtEstadoPokemon;
    private RecyclerView recyclerPokemon;

    private PokemonAdapter adapter;

    private ArrayList<Pokemon> listaPokemon = new ArrayList<>();
    private ArrayList<Object> elementosActuales = new ArrayList<>();

    private Set<Integer> favoritos = new HashSet<>();

    private SharedPreferences preferencias;

    private boolean mostrandoFavoritos = false;
    private boolean cargando = false;

    // Cantidad de columnas adaptable
    private int cantidadColumnas = 3;

    // Modo selección para equipos
    private boolean modoSeleccion = false;
    private String equipoId;
    private EquipoRepository equipoRepository;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private final String[] regionesPrincipales = {
            "Kanto", "Johto", "Hoenn",
            "Sinnoh", "Teselia", "Kalos",
            "Alola", "Galar", "Paldea"
    };

    private final String[] variantesRegionales = {
            "Formas de Alola",
            "Formas de Galar",
            "Formas de Hisui",
            "Formas de Paldea"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokedex);

        // Activar modo inmersivo
        ocultarBarrasSistema();

        // Comprobar si llegamos desde un equipo
        modoSeleccion = getIntent().getBooleanExtra(
                "modo_seleccion", false
        );

        equipoId = getIntent().getStringExtra("equipo_id");

        if (modoSeleccion) {
            equipoRepository = new EquipoRepository(this);
        }

        btnVolverPokedex = findViewById(R.id.btnVolverPokedex);
        btnTabPokemon = findViewById(R.id.btnTabPokemon);
        btnTabFavoritos = findViewById(R.id.btnTabFavoritos);
        btnIrRegion = findViewById(R.id.btnIrRegion);

        edtBuscarPokemon = findViewById(R.id.edtBuscarPokemon);
        txtEstadoPokemon = findViewById(R.id.txtEstadoPokemon);
        recyclerPokemon = findViewById(R.id.recyclerPokemon);

        preferencias = getSharedPreferences(
                "teamdex_favoritos",
                MODE_PRIVATE
        );

        cargarFavoritos();

        btnVolverPokedex.setOnClickListener(v -> finish());

        configurarRecyclerView();
        configurarPestanas();
        configurarBuscador();
        configurarSelectorRegiones();

        cargarPokemonDesdeApi();
    }

    // ==========================================
    // MODO INMERSIVO
    // ==========================================

    private void ocultarBarrasSistema() {

        View decorView = getWindow().getDecorView();

        int flags =
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

        decorView.setSystemUiVisibility(flags);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            ocultarBarrasSistema();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ocultarBarrasSistema();
    }

    // ==========================================
    // DISEÑO ADAPTABLE
    // ==========================================

    private int calcularCantidadColumnas() {

        DisplayMetrics metrics =
                getResources().getDisplayMetrics();

        int anchoDisponible;

        if (recyclerPokemon != null && recyclerPokemon.getWidth() > 0) {
            anchoDisponible = (int) (
                    recyclerPokemon.getWidth() / metrics.density
            );
        } else {
            anchoDisponible = (int) (
                    metrics.widthPixels / metrics.density
            ) - 32;
        }

        // Aproximadamente 100dp por tarjeta.
        int columnas = anchoDisponible / 100;

        // Mínimo 2 y máximo 6 columnas.
        return Math.max(2, Math.min(6, columnas));
    }

    private void actualizarCantidadColumnas() {

        if (recyclerPokemon == null) {
            return;
        }

        RecyclerView.LayoutManager manager =
                recyclerPokemon.getLayoutManager();

        if (!(manager instanceof GridLayoutManager)) {
            return;
        }

        int nuevasColumnas = calcularCantidadColumnas();

        if (nuevasColumnas != cantidadColumnas) {

            cantidadColumnas = nuevasColumnas;

            ((GridLayoutManager) manager).setSpanCount(
                    cantidadColumnas
            );

            recyclerPokemon.requestLayout();
        }
    }

    // ==========================================
    // RECYCLER VIEW
    // ==========================================

    private void configurarRecyclerView() {

        cantidadColumnas = calcularCantidadColumnas();

        GridLayoutManager layoutManager =
                new GridLayoutManager(this, cantidadColumnas);

        layoutManager.setSpanSizeLookup(
                new GridLayoutManager.SpanSizeLookup() {

                    @Override
                    public int getSpanSize(int position) {

                        if (adapter.obtenerTipo(position) == 0) {
                            return cantidadColumnas;
                        }

                        return 1;
                    }
                }
        );

        recyclerPokemon.setLayoutManager(layoutManager);

        adapter = new PokemonAdapter(this::alternarFavorito);

        // Activar selección solamente cuando venimos desde un equipo
        if (modoSeleccion) {
            adapter.setOnPokemonClickListener(
                    this::seleccionarPokemon
            );
        }

        recyclerPokemon.setAdapter(adapter);

        // Recalcular cuando el RecyclerView ya tenga su tamaño real.
        recyclerPokemon.post(this::actualizarCantidadColumnas);
    }

    private void seleccionarPokemon(Pokemon pokemon) {

        if (!modoSeleccion || equipoRepository == null
                || equipoId == null) {
            return;
        }

        boolean agregado = equipoRepository.agregarPokemonApi(
                equipoId,
                pokemon.getId(),
                pokemon.getNombreMostrado()
        );

        if (agregado) {

            Toast.makeText(
                    this,
                    pokemon.getNombreMostrado() + " agregado al equipo",
                    Toast.LENGTH_SHORT
            ).show();

            // Volver automáticamente al detalle del equipo
            finish();

        } else {

            Toast.makeText(
                    this,
                    "No se pudo agregar. El equipo puede estar lleno o ya tener este Pokémon.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // ==========================================
    // PESTAÑAS
    // ==========================================

    private void configurarPestanas() {

        btnTabPokemon.setOnClickListener(v -> {

            mostrandoFavoritos = false;

            actualizarPestanas();
            filtrarPokemon();
        });

        btnTabFavoritos.setOnClickListener(v -> {

            mostrandoFavoritos = true;

            actualizarPestanas();
            filtrarPokemon();
        });

        actualizarPestanas();
    }

    private void actualizarPestanas() {

        if (mostrandoFavoritos) {

            btnTabFavoritos.setBackgroundTintList(
                    ColorStateList.valueOf(0xFFFACC15)
            );

            btnTabFavoritos.setTextColor(0xFF101827);

            btnTabPokemon.setBackgroundTintList(
                    ColorStateList.valueOf(0xFF263449)
            );

            btnTabPokemon.setTextColor(0xFFFFFFFF);

            btnIrRegion.setVisibility(View.GONE);

        } else {

            btnTabPokemon.setBackgroundTintList(
                    ColorStateList.valueOf(0xFFFACC15)
            );

            btnTabPokemon.setTextColor(0xFF101827);

            btnTabFavoritos.setBackgroundTintList(
                    ColorStateList.valueOf(0xFF263449)
            );

            btnTabFavoritos.setTextColor(0xFFFFFFFF);

            btnIrRegion.setVisibility(View.VISIBLE);
        }
    }

    // ==========================================
    // SELECTOR DE REGIONES
    // ==========================================

    private void configurarSelectorRegiones() {

        btnIrRegion.setOnClickListener(v -> mostrarSelectorRegiones());
    }

    private void mostrarSelectorRegiones() {

        LinearLayout contenido = new LinearLayout(this);
        contenido.setOrientation(LinearLayout.VERTICAL);
        contenido.setPadding(dp(20), dp(20), dp(20), dp(16));

        GradientDrawable fondo = new GradientDrawable();
        fondo.setColor(Color.rgb(16, 24, 39));
        fondo.setCornerRadius(dp(22));
        fondo.setStroke(dp(1), Color.rgb(52, 65, 86));

        contenido.setBackground(fondo);

        TextView titulo = new TextView(this);
        titulo.setText("Explorar regiones");
        titulo.setTextColor(Color.rgb(250, 204, 21));
        titulo.setTextSize(22);
        titulo.setTypeface(null, Typeface.BOLD);

        contenido.addView(titulo);

        TextView descripcion = new TextView(this);
        descripcion.setText(
                "Selecciona dónde quieres buscar Pokémon"
        );
        descripcion.setTextColor(Color.rgb(148, 163, 184));
        descripcion.setTextSize(13);

        LinearLayout.LayoutParams descripcionParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        descripcionParams.topMargin = dp(5);
        descripcionParams.bottomMargin = dp(18);

        contenido.addView(descripcion, descripcionParams);

        ScrollView scroll = new ScrollView(this);

        LinearLayout lista = new LinearLayout(this);
        lista.setOrientation(LinearLayout.VERTICAL);

        scroll.addView(lista);

        AlertDialog dialog = new AlertDialog.Builder(this).create();

        agregarTituloSeccion(lista, "REGIONES PRINCIPALES");

        for (String region : regionesPrincipales) {
            agregarBotonRegion(lista, region, dialog);
        }

        agregarTituloSeccion(lista, "VARIANTES REGIONALES");

        for (String region : variantesRegionales) {
            agregarBotonRegion(lista, region, dialog);
        }

        LinearLayout.LayoutParams scrollParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                );

        contenido.addView(scroll, scrollParams);

        dialog.setView(contenido);

        dialog.setOnShowListener(v -> {

            if (dialog.getWindow() != null) {

                dialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(Color.TRANSPARENT)
                );

                int anchoPantalla =
                        getResources().getDisplayMetrics().widthPixels;

                int altoPantalla =
                        getResources().getDisplayMetrics().heightPixels;

                // Ancho máximo para que el diálogo no sea enorme en tablets.
                int anchoDialogo = Math.min(
                        dp(480),
                        anchoPantalla - dp(32)
                );

                dialog.getWindow().setLayout(
                        anchoDialogo,
                        (int) (altoPantalla * 0.78)
                );
            }
        });

        dialog.show();
    }

    private void agregarTituloSeccion(
            LinearLayout lista,
            String titulo
    ) {

        TextView texto = new TextView(this);

        texto.setText(titulo);
        texto.setTextColor(Color.rgb(250, 204, 21));
        texto.setTextSize(12);
        texto.setTypeface(null, Typeface.BOLD);
        texto.setPadding(0, dp(12), 0, dp(10));

        lista.addView(texto);
    }

    private void agregarBotonRegion(
            LinearLayout lista,
            String region,
            AlertDialog dialog
    ) {

        Button boton = new Button(this);

        boton.setText(region + "    ›");
        boton.setTextColor(Color.WHITE);
        boton.setTextSize(14);
        boton.setAllCaps(false);
        boton.setGravity(Gravity.CENTER_VERTICAL);

        GradientDrawable fondo = new GradientDrawable();
        fondo.setColor(Color.rgb(27, 41, 61));
        fondo.setCornerRadius(dp(13));
        fondo.setStroke(dp(1), Color.rgb(52, 65, 86));

        boton.setBackground(fondo);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(48)
                );

        params.bottomMargin = dp(8);

        lista.addView(boton, params);

        boton.setOnClickListener(v -> {
            dialog.dismiss();
            irARegion(region);
        });
    }

    private int dp(int valor) {
        return (int) (
                valor * getResources().getDisplayMetrics().density
        );
    }

    private void irARegion(String region) {

        if (mostrandoFavoritos) {
            return;
        }

        if (!edtBuscarPokemon.getText().toString().isEmpty()) {
            edtBuscarPokemon.setText("");
        }

        recyclerPokemon.post(() -> {

            GridLayoutManager layoutManager =
                    (GridLayoutManager) recyclerPokemon.getLayoutManager();

            if (layoutManager == null) {
                return;
            }

            for (int i = 0; i < elementosActuales.size(); i++) {

                Object elemento = elementosActuales.get(i);

                if (elemento instanceof String
                        && elemento.equals(region)) {

                    layoutManager.scrollToPositionWithOffset(i, 0);
                    return;
                }
            }

            Toast.makeText(
                    this,
                    "No se encontraron Pokémon para " + region,
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    // ==========================================
    // BUSCADOR
    // ==========================================

    private void configurarBuscador() {

        edtBuscarPokemon.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count
            ) {
                filtrarPokemon();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // ==========================================
    // CARGA DE POKÉMON
    // ==========================================

    private void cargarPokemonDesdeApi() {

        if (cargando || !listaPokemon.isEmpty()) {
            return;
        }

        cargando = true;

        txtEstadoPokemon.setText("Cargando Pokémon...");

        executor.execute(() -> {

            try {

                ArrayList<Pokemon> resultado =
                        PokedexRepository.obtenerPokemon();

                handler.post(() -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    listaPokemon = resultado;
                    cargando = false;

                    txtEstadoPokemon.setText("");

                    filtrarPokemon();
                });

            } catch (Exception e) {

                handler.post(() -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    cargando = false;

                    txtEstadoPokemon.setText(
                            "No se pudieron cargar los Pokémon.\n" +
                                    "Comprueba tu conexión e inténtalo nuevamente."
                    );

                    Toast.makeText(
                            PokedexActivity.this,
                            "Error al conectar con PokéAPI",
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    // ==========================================
    // FILTRADO DE POKÉMON
    // ==========================================

    private void filtrarPokemon() {

        if (adapter == null) {
            return;
        }

        String busqueda = edtBuscarPokemon.getText()
                .toString()
                .trim()
                .toLowerCase(Locale.ROOT);

        busqueda = busqueda.replace("#", "");

        ArrayList<Object> elementos = new ArrayList<>();

        String ultimaRegion = "";

        int cantidadEncontrada = 0;

        for (Pokemon pokemon : listaPokemon) {

            if (mostrandoFavoritos &&
                    !favoritos.contains(pokemon.getId())) {
                continue;
            }

            String nombre = pokemon.getNombreMostrado()
                    .toLowerCase(Locale.ROOT);

            String nombreOriginal = pokemon.getNombre()
                    .toLowerCase(Locale.ROOT);

            String numero = String.valueOf(pokemon.getId());

            String numeroFormateado =
                    pokemon.getNumeroFormateado()
                            .replace("#", "");

            boolean coincide =
                    busqueda.isEmpty()
                            || nombre.contains(busqueda)
                            || nombreOriginal.contains(busqueda)
                            || numero.contains(busqueda)
                            || numeroFormateado.contains(busqueda);

            if (!coincide) {
                continue;
            }

            String region = pokemon.getRegion();

            if (!region.equals(ultimaRegion)) {

                elementos.add(region);

                ultimaRegion = region;
            }

            elementos.add(pokemon);

            cantidadEncontrada++;
        }

        elementosActuales = elementos;

        adapter.actualizar(elementos, favoritos);

        if (listaPokemon.isEmpty()) {

            txtEstadoPokemon.setText(
                    cargando
                            ? "Cargando Pokémon..."
                            : "No hay Pokémon disponibles."
            );

        } else if (cantidadEncontrada == 0) {

            txtEstadoPokemon.setText(
                    mostrandoFavoritos
                            ? "Todavía no tienes Pokémon favoritos."
                            : "No se encontraron Pokémon."
            );

        } else {

            txtEstadoPokemon.setText(
                    cantidadEncontrada +
                            (cantidadEncontrada == 1
                                    ? " Pokémon encontrado"
                                    : " Pokémon encontrados")
            );
        }
    }

    // ==========================================
    // FAVORITOS
    // ==========================================

    private void alternarFavorito(Pokemon pokemon) {

        if (favoritos.contains(pokemon.getId())) {

            favoritos.remove(pokemon.getId());

        } else {

            favoritos.add(pokemon.getId());
        }

        guardarFavoritos();

        filtrarPokemon();
    }

    private void cargarFavoritos() {

        Set<String> guardados = preferencias.getStringSet(
                "pokemon_ids",
                new HashSet<>()
        );

        favoritos.clear();

        for (String id : guardados) {

            try {
                favoritos.add(Integer.parseInt(id));
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private void guardarFavoritos() {

        Set<String> guardados = new HashSet<>();

        for (Integer id : favoritos) {
            guardados.add(String.valueOf(id));
        }

        preferencias.edit()
                .putStringSet("pokemon_ids", guardados)
                .apply();
    }

    // ==========================================
    // DESTRUCCIÓN
    // ==========================================

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
