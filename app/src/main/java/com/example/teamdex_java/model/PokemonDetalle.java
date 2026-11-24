package com.example.teamdex_java.model;

import java.util.ArrayList;

public class PokemonDetalle {

    private final int id;
    private final String spriteUrl;
    private final String imagenUrl;
    private final ArrayList<String> tipos;

    public PokemonDetalle(
            int id,
            String spriteUrl,
            String imagenUrl,
            ArrayList<String> tipos
    ) {
        this.id = id;
        this.spriteUrl = spriteUrl;
        this.imagenUrl = imagenUrl;
        this.tipos = tipos;
    }

    public int getId() {
        return id;
    }

    public String getSpriteUrl() {
        return spriteUrl;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public ArrayList<String> getTipos() {
        return tipos;
    }
}
