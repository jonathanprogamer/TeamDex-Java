
package com.example.teamdex_java.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.teamdex_java.R;
import com.example.teamdex_java.data.EquipoRepository;
import com.example.teamdex_java.data.TiposPokemonRepository;
import com.example.teamdex_java.model.Equipo;

public class EquipoDetalleActivity extends AppCompatActivity {

    private String equipoId;
    private EquipoRepository repository;

    private TextView txtNombreDetalle;
    private TextView txtCantidadPokemon;
    private EditText edtNombrePokemon;

    private LinearLayout contenedorPokemon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipo_detalle);

        equipoId = getIntent().getStringExtra("equipo_id");

        repository = new EquipoRepository(this);

        txtNombreDetalle = findViewById(R.id.txtNombreDetalle);
        txtCantidadPokemon = findViewById(R.id.txtCantidadPokemon);
        edtNombrePokemon = findViewById(R.id.edtNombrePokemon);
        contenedorPokemon = findViewById(R.id.contenedorPokemon);

        Button btnAgregar = findViewById(R.id.btnAgregarPokemon);
        Button btnVolver = findViewById(R.id.btnVolver);
        Button btnEditar = findViewById(R.id.btnEditarEquipo);
        Button btnEliminar = findViewById(R.id.btnEliminarEquipo);

        edtNombrePokemon.setVisibility(EditText.GONE);

        btnAgregar.setText("Elegir desde Pokédex");
        btnAgregar.setAllCaps(false);

        btnAgregar.setOnClickListener(v -> abrirPokedex());
        btnVolver.setOnClickListener(v -> finish());
        btnEditar.setOnClickListener(v -> mostrarDialogoEditarNombre());
        btnEliminar.setOnClickListener(v -> confirmarEliminarEquipo());

        cargarPokemon();
    }

    private int dp(int valor) {
        return (int) (
                valor * getResources().getDisplayMetrics().density
        );
    }

    // Obtiene el ID desde el formato id|nombre.
    private int obtenerIdPokemon(String registro) {
        try {
            if (registro != null && registro.contains("|")) {
                String[] partes = registro.split("\\|", 2);
                return Integer.parseInt(partes[0]);
            }
        } catch (Exception ignored) {
        }

        return -1;
    }

    // Obtiene el nombre desde el formato id|nombre.
    private String obtenerNombrePokemon(String registro) {
        if (registro == null) {
            return "Pokémon";
        }

        if (registro.contains("|")) {
            String[] partes = registro.split("\\|", 2);

            if (partes.length > 1) {
                return partes[1];
            }
        }

        return registro;
    }

    private String obtenerUrlSprite(int id) {
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/"
                + id + ".png";
    }

    // EDITAR NOMBRE DEL EQUIPO.

    private void mostrarDialogoEditarNombre() {

        Equipo equipo = repository.obtenerEquipo(equipoId);

        if (equipo == null) {
            Toast.makeText(this, "No se encontró el equipo",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        EditText entrada = new EditText(this);
        entrada.setSingleLine(true);
        entrada.setText(equipo.getNombre());
        entrada.setSelection(entrada.getText().length());
        entrada.setTextColor(Color.WHITE);
        entrada.setHintTextColor(Color.GRAY);
        entrada.setHint("Nuevo nombre");
        entrada.setPadding(40, 25, 40, 25);

        GradientDrawable fondo = new GradientDrawable();
        fondo.setColor(Color.rgb(21, 31, 48));
        fondo.setCornerRadius(16);
        fondo.setStroke(2, Color.rgb(38, 52, 73));
        entrada.setBackground(fondo);

        LinearLayout contenedor = new LinearLayout(this);
        contenedor.setPadding(24, 12, 24, 4);
        contenedor.addView(entrada);

        AlertDialog dialogo = new AlertDialog.Builder(this)
                .setTitle("Cambiar nombre")
                .setMessage("Escribe el nuevo nombre de tu equipo.")
                .setView(contenedor)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", null)
                .create();

        dialogo.setOnShowListener(v -> {

            dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(Color.rgb(245, 197, 66));

            dialogo.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(Color.LTGRAY);

            dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(boton -> {

                        String nuevoNombre = entrada.getText()
                                .toString().trim();

                        if (nuevoNombre.isEmpty()) {
                            entrada.setError("Escribe un nombre");
                            return;
                        }

                        boolean actualizado =
                                repository.actualizarNombreEquipo(
                                        equipoId,
                                        nuevoNombre
                                );

                        if (actualizado) {
                            txtNombreDetalle.setText(nuevoNombre);

                            Toast.makeText(
                                    this,
                                    "Nombre actualizado correctamente",
                                    Toast.LENGTH_SHORT
                            ).show();

                            dialogo.dismiss();

                        } else {
                            Toast.makeText(
                                    this,
                                    "No se pudo actualizar el equipo",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        });

        dialogo.show();
    }

    // ELIMINAR EQUIPO.

    private void confirmarEliminarEquipo() {

        Equipo equipo = repository.obtenerEquipo(equipoId);

        if (equipo == null) {
            Toast.makeText(this, "No se encontró el equipo",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        AlertDialog dialogo = new AlertDialog.Builder(this)
                .setTitle("Eliminar equipo")
                .setMessage(
                        "¿Estás seguro de que deseas eliminar \""
                                + equipo.getNombre()
                                + "\"?\n\nTambién se eliminarán todos sus Pokémon."
                )
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> {

                    boolean eliminado =
                            repository.eliminarEquipo(equipoId);

                    if (eliminado) {
                        Toast.makeText(
                                this,
                                "Equipo eliminado correctamente",
                                Toast.LENGTH_SHORT
                        ).show();

                        finish();

                    } else {
                        Toast.makeText(
                                this,
                                "No se pudo eliminar el equipo",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .create();

        dialogo.setOnShowListener(v -> {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(Color.rgb(220, 80, 80));

            dialogo.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(Color.LTGRAY);
        });

        dialogo.show();
    }

    // ABRIR POKÉDEX.

    private void abrirPokedex() {

        Equipo equipo = repository.obtenerEquipo(equipoId);

        if (equipo == null) {
            Toast.makeText(
                    this,
                    "No se encontró el equipo",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        if (equipo.getPokemon().size() >= 6) {
            Toast.makeText(
                    this,
                    "El equipo ya tiene 6 Pokémon",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Intent intent = new Intent(this, PokedexActivity.class);
        intent.putExtra("modo_seleccion", true);
        intent.putExtra("equipo_id", equipoId);

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPokemon();
    }

    // CARGAR POKÉMON.

    private void cargarPokemon() {

        Equipo equipo = repository.obtenerEquipo(equipoId);

        if (equipo == null) {
            Toast.makeText(
                    this,
                    "No se encontró el equipo",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        txtNombreDetalle.setText(equipo.getNombre());

        int cantidad = equipo.getPokemon().size();

        txtCantidadPokemon.setText(cantidad + " / 6 Pokémon");

        contenedorPokemon.removeAllViews();

        if (cantidad == 0) {
            TextView vacio = new TextView(this);

            vacio.setText("Todavía no hay Pokémon en este equipo.");
            vacio.setTextColor(Color.rgb(154, 168, 189));
            vacio.setTextSize(14);
            vacio.setPadding(0, 20, 0, 20);

            contenedorPokemon.addView(vacio);
        }

        for (int i = 0; i < cantidad; i++) {
            agregarTarjetaPokemon(equipo.getPokemon().get(i), i);
        }
    }

    // TARJETAS CON SPRITES Y CÍRCULOS DE TIPOS.

    private void agregarTarjetaPokemon(String registro, int posicion) {

        int idPokemon = obtenerIdPokemon(registro);
        String nombre = obtenerNombrePokemon(registro);

        LinearLayout tarjeta = new LinearLayout(this);

        tarjeta.setOrientation(LinearLayout.HORIZONTAL);
        tarjeta.setGravity(Gravity.CENTER_VERTICAL);
        tarjeta.setPadding(dp(12), dp(10), dp(8), dp(10));

        GradientDrawable fondo = new GradientDrawable();
        fondo.setColor(Color.rgb(21, 31, 48));
        fondo.setCornerRadius(dp(16));
        fondo.setStroke(dp(1), Color.rgb(38, 52, 73));

        tarjeta.setBackground(fondo);

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametros.setMargins(0, 0, 0, dp(12));
        tarjeta.setLayoutParams(parametros);

        TextView numero = new TextView(this);
        numero.setText(String.valueOf(posicion + 1));
        numero.setTextColor(Color.rgb(245, 197, 66));
        numero.setTextSize(17);
        numero.setTypeface(null, Typeface.BOLD);

        // Contenedor del círculo y el sprite.
        FrameLayout contenedorImagen = new FrameLayout(this);

        LinearLayout.LayoutParams imagenParams =
                new LinearLayout.LayoutParams(dp(68), dp(68));

        imagenParams.setMargins(dp(10), 0, dp(8), 0);
        contenedorImagen.setLayoutParams(imagenParams);

        // Círculo de tipo.
        View fondoTipo = new View(this);

        fondoTipo.setBackground(
                new TipoPokemonDrawable(new java.util.ArrayList<>())
        );

        contenedorImagen.addView(
                fondoTipo,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        // Sprite del Pokémon.
        ImageView imagen = new ImageView(this);
        imagen.setScaleType(ImageView.ScaleType.FIT_CENTER);

        if (idPokemon > 0) {
            Glide.with(this)
                    .load(obtenerUrlSprite(idPokemon))
                    .override(160, 160)
                    .fitCenter()
                    .into(imagen);
        }

        contenedorImagen.addView(
                imagen,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        // Obtener tipos desde PokéAPI.
        TiposPokemonRepository.obtenerTipos(
                idPokemon,
                tipos -> {
                    if (!isFinishing() && !isDestroyed()) {
                        fondoTipo.setBackground(
                                new TipoPokemonDrawable(tipos)
                        );
                    }
                }
        );

        TextView nombrePokemon = new TextView(this);
        nombrePokemon.setText(nombre);
        nombrePokemon.setTextColor(Color.WHITE);
        nombrePokemon.setTextSize(16);
        nombrePokemon.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams nombreParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        nombrePokemon.setLayoutParams(nombreParams);

        Button btnEliminar = new Button(this);
        btnEliminar.setText("Eliminar");
        btnEliminar.setAllCaps(false);
        btnEliminar.setTextColor(Color.WHITE);
        btnEliminar.setTextSize(12);

        btnEliminar.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        Color.rgb(130, 48, 58)
                )
        );

        btnEliminar.setOnClickListener(v -> {

            repository.eliminarPokemon(equipoId, posicion);

            Toast.makeText(
                    this,
                    nombre + " eliminado del equipo",
                    Toast.LENGTH_SHORT
            ).show();

            cargarPokemon();
        });

        tarjeta.addView(numero);
        tarjeta.addView(contenedorImagen);
        tarjeta.addView(nombrePokemon);
        tarjeta.addView(btnEliminar);

        contenedorPokemon.addView(tarjeta);
    }
}
