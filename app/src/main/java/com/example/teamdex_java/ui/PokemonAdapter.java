
package com.example.teamdex_java.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.teamdex_java.R;
import com.example.teamdex_java.model.Pokemon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PokemonAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TIPO_REGION = 0;
    private static final int TIPO_POKEMON = 1;

    private final ArrayList<Object> elementos = new ArrayList<>();
    private final Set<Integer> favoritos = new HashSet<>();

    public interface OnFavoritoClickListener {
        void onFavoritoClick(Pokemon pokemon);
    }

    public interface OnPokemonClickListener {
        void onPokemonClick(Pokemon pokemon);
    }

    private final OnFavoritoClickListener listener;

    private OnPokemonClickListener pokemonClickListener;

    public PokemonAdapter(OnFavoritoClickListener listener) {
        this.listener = listener;
    }

    public void setOnPokemonClickListener(
            OnPokemonClickListener listener
    ) {
        this.pokemonClickListener = listener;
    }

    public void actualizar(
            ArrayList<Object> nuevosElementos,
            Set<Integer> nuevosFavoritos
    ) {

        elementos.clear();
        elementos.addAll(nuevosElementos);

        favoritos.clear();
        favoritos.addAll(nuevosFavoritos);

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {

        if (elementos.get(position) instanceof String) {
            return TIPO_REGION;
        }

        return TIPO_POKEMON;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TIPO_REGION) {

            View vista = inflater.inflate(
                    R.layout.item_region,
                    parent,
                    false
            );

            return new RegionViewHolder(vista);
        }

        View vista = inflater.inflate(
                R.layout.item_pokemon,
                parent,
                false
        );

        return new PokemonViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder,
            int position
    ) {

        Object elemento = elementos.get(position);

        if (holder instanceof RegionViewHolder) {

            RegionViewHolder regionHolder =
                    (RegionViewHolder) holder;

            regionHolder.txtNombreRegion.setText((String) elemento);

        } else if (holder instanceof PokemonViewHolder) {

            PokemonViewHolder pokemonHolder =
                    (PokemonViewHolder) holder;

            Pokemon pokemon = (Pokemon) elemento;

            pokemonHolder.txtNumero.setText(
                    pokemon.getNumeroFormateado()
            );

            pokemonHolder.txtNombre.setText(
                    pokemon.getNombreMostrado()
            );

            // Etiqueta de variantes regionales.
            String etiqueta = pokemon.getEtiquetaFormaRegional();

            if (etiqueta != null) {

                pokemonHolder.txtFormaRegional.setText(etiqueta);
                pokemonHolder.txtFormaRegional.setVisibility(View.VISIBLE);

            } else {

                pokemonHolder.txtFormaRegional.setVisibility(View.GONE);
            }

            Glide.with(pokemonHolder.imgPokemon.getContext())
                    .load(pokemon.getImagenUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(pokemonHolder.imgPokemon);

            // Estado de favoritos.
            boolean esFavorito = favoritos.contains(pokemon.getId());

            pokemonHolder.btnFavorito.setText(
                    esFavorito ? "♥" : "♡"
            );

            pokemonHolder.btnFavorito.setTextColor(
                    esFavorito
                            ? android.graphics.Color.rgb(250, 204, 21)
                            : android.graphics.Color.LTGRAY
            );

            // Pulsar el corazón administra favoritos.
            pokemonHolder.btnFavorito.setOnClickListener(v -> {

                if (listener != null) {
                    listener.onFavoritoClick(pokemon);
                }
            });

            // Pulsar la tarjeta permite seleccionar un Pokémon.
            pokemonHolder.itemView.setOnClickListener(v -> {

                if (pokemonClickListener != null) {
                    pokemonClickListener.onPokemonClick(pokemon);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return elementos.size();
    }

    public int obtenerTipo(int position) {
        return getItemViewType(position);
    }

    static class RegionViewHolder extends RecyclerView.ViewHolder {

        TextView txtNombreRegion;

        RegionViewHolder(View itemView) {
            super(itemView);

            txtNombreRegion = itemView.findViewById(
                    R.id.txtNombreRegion
            );
        }
    }

    static class PokemonViewHolder extends RecyclerView.ViewHolder {

        ImageView imgPokemon;
        TextView txtNumero;
        TextView txtNombre;
        TextView btnFavorito;
        TextView txtFormaRegional;

        PokemonViewHolder(View itemView) {
            super(itemView);

            imgPokemon = itemView.findViewById(R.id.imgPokemon);
            txtNumero = itemView.findViewById(R.id.txtNumeroPokemon);
            txtNombre = itemView.findViewById(R.id.txtNombrePokemon);
            btnFavorito = itemView.findViewById(R.id.btnFavorito);
            txtFormaRegional = itemView.findViewById(R.id.txtFormaRegional);
        }
    }
}
