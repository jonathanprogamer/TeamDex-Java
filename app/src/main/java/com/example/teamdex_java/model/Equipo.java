
package com.example.teamdex_java.model;

import java.util.ArrayList;

public class Equipo {

    private String id;
    private String nombre;
    private ArrayList<String> pokemon;

    public Equipo(String nombre) {
        this.id = java.util.UUID.randomUUID().toString();
        this.nombre = nombre;
        this.pokemon = new ArrayList<>();
    }

    public Equipo(
            String id,
            String nombre,
            ArrayList<String> pokemon
    ) {
        this.id = id;
        this.nombre = nombre;
        this.pokemon = pokemon;
    }

    public Equipo(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
        this.pokemon = new ArrayList<>();
    }

    // Obtener ID
    public String getId() {
        return id;
    }

    // Modificar ID
    public void setId(String id) {
        this.id = id;
    }

    // Obtener nombre
    public String getNombre() {
        return nombre;
    }

    // Modificar nombre
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // Obtener Pokémon del equipo
    public ArrayList<String> getPokemon() {
        return pokemon;
    }

    // Modificar lista de Pokémon
    public void setPokemon(ArrayList<String> pokemon) {
        this.pokemon = pokemon;
    }
}
