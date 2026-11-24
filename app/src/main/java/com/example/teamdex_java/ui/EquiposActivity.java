
package com.example.teamdex_java.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.teamdex_java.R;
import com.example.teamdex_java.data.EquipoRepository;
import com.example.teamdex_java.data.TiposPokemonRepository;
import com.example.teamdex_java.model.Equipo;

import java.util.ArrayList;

public class EquiposActivity extends AppCompatActivity {

    private Button btnVolverEquipos;
    private Button btnCrearEquipo;

    private TextView txtCantidadEquipos;
    private TextView txtSinEquipos;

    private LinearLayout contenedorEquipos;

    private EquipoRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipos);

        repository = new EquipoRepository(this);

        btnVolverEquipos = findViewById(R.id.btnVolverEquipos);
        btnCrearEquipo = findViewById(R.id.btnCrearEquipo);

        txtCantidadEquipos = findViewById(R.id.txtCantidadEquipos);
        txtSinEquipos = findViewById(R.id.txtSinEquipos);

        contenedorEquipos = findViewById(R.id.contenedorEquipos);

        btnVolverEquipos.setOnClickListener(v -> finish());

        btnCrearEquipo.setOnClickListener(v -> {
            Intent intent = new Intent(
                    EquiposActivity.this,
                    CrearEquipoActivity.class
            );

            startActivity(intent);
        });
    }

    private int dp(int valor) {
        return (int) (
                valor * getResources().getDisplayMetrics().density
        );
    }

    private int obtenerTamanoSprite() {

        float densidad = getResources()
                .getDisplayMetrics().density;

        int anchoPantallaDp = (int) (
                getResources().getDisplayMetrics().widthPixels / densidad
        );

        // Espacio para los márgenes y el padding de la tarjeta.
        int espacioDisponible = anchoPantallaDp - 105;

        // Calculamos el espacio para seis Pokémon.
        int tamano = espacioDisponible / 6;

        // Mantenemos un tamaño cómodo en móviles y tablets.
        return Math.max(28, Math.min(44, tamano));
    }

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

    private String obtenerUrlSprite(int id) {
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/"
                + id + ".png";
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarEquipos();
    }

    private void cargarEquipos() {

        ArrayList<Equipo> equipos = repository.obtenerEquipos();

        contenedorEquipos.removeAllViews();

        int cantidad = equipos.size();

        txtCantidadEquipos.setText(
                cantidad + (cantidad == 1
                        ? " equipo creado"
                        : " equipos creados")
        );

        txtSinEquipos.setVisibility(
                cantidad == 0 ? View.VISIBLE : View.GONE
        );

        for (Equipo equipo : equipos) {
            agregarTarjeta(equipo);
        }
    }

    private void agregarTarjeta(Equipo equipo) {

        LinearLayout tarjeta = new LinearLayout(this);

        tarjeta.setOrientation(LinearLayout.VERTICAL);
        tarjeta.setPadding(dp(20), dp(18), dp(20), dp(18));
        tarjeta.setClipChildren(false);

        GradientDrawable fondo = new GradientDrawable();
        fondo.setColor(Color.rgb(21, 31, 48));
        fondo.setCornerRadius(dp(18));
        fondo.setStroke(dp(1), Color.rgb(38, 52, 73));

        tarjeta.setBackground(fondo);

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametros.setMargins(0, 0, 0, dp(14));
        tarjeta.setLayoutParams(parametros);

        TextView nombre = new TextView(this);
        nombre.setText(equipo.getNombre());
        nombre.setTextColor(Color.WHITE);
        nombre.setTextSize(19);
        nombre.setTypeface(null, Typeface.BOLD);

        TextView descripcion = new TextView(this);
        descripcion.setText(
                "Equipo Pokémon · " +
                        equipo.getPokemon().size() + " / 6"
        );
        descripcion.setTextColor(Color.rgb(154, 168, 189));
        descripcion.setTextSize(13);
        descripcion.setPadding(0, dp(5), 0, dp(8));

        LinearLayout filaSprites = new LinearLayout(this);
        filaSprites.setOrientation(LinearLayout.HORIZONTAL);
        filaSprites.setGravity(Gravity.CENTER_VERTICAL);
        filaSprites.setClipChildren(false);

        ArrayList<String> pokemon = equipo.getPokemon();

        int tamanoSprite = obtenerTamanoSprite();

        for (int i = 0; i < pokemon.size() && i < 6; i++) {

            int idPokemon = obtenerIdPokemon(pokemon.get(i));

            if (idPokemon <= 0) {
                continue;
            }

            // Contenedor del círculo y el sprite.
            FrameLayout contenedorSprite = new FrameLayout(this);

            LinearLayout.LayoutParams spriteParams =
                    new LinearLayout.LayoutParams(
                            dp(tamanoSprite),
                            dp(tamanoSprite)
                    );

            spriteParams.setMargins(0, 0, dp(5), 0);
            contenedorSprite.setLayoutParams(spriteParams);

            // Fondo con el color del tipo.
            View fondoTipo = new View(this);

            fondoTipo.setBackground(
                    new TipoPokemonDrawable(new ArrayList<>())
            );

            contenedorSprite.addView(
                    fondoTipo,
                    new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                    )
            );

            // Sprite del Pokémon.
            ImageView sprite = new ImageView(this);
            sprite.setScaleType(ImageView.ScaleType.FIT_CENTER);

            Glide.with(this)
                    .load(obtenerUrlSprite(idPokemon))
                    .override(64, 64)
                    .fitCenter()
                    .into(sprite);

            contenedorSprite.addView(
                    sprite,
                    new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                    )
            );

            filaSprites.addView(contenedorSprite);

            // Consultar los tipos y actualizar el círculo.
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
        }

        if (pokemon.isEmpty()) {
            TextView vacio = new TextView(this);
            vacio.setText("Aún no hay Pokémon");
            vacio.setTextColor(Color.rgb(120, 135, 155));
            vacio.setTextSize(12);

            filaSprites.addView(vacio);
        }

        tarjeta.addView(nombre);
        tarjeta.addView(descripcion);
        tarjeta.addView(filaSprites);

        tarjeta.setOnClickListener(v -> {

            Intent intent = new Intent(
                    EquiposActivity.this,
                    EquipoDetalleActivity.class
            );

            intent.putExtra("equipo_id", equipo.getId());

            startActivity(intent);
        });

        contenedorEquipos.addView(tarjeta);
    }
}
