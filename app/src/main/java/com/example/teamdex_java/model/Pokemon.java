
package com.example.teamdex_java.model;

import java.util.Locale;

public class Pokemon {

    private final int id;
    private final int numeroNacional;
    private final String nombre;
    private final String imagenUrl;

    public Pokemon(int id, String nombre) {
        this(id, nombre, id);
    }

    public Pokemon(int id, String nombre, int numeroNacional) {
        this.id = id;
        this.nombre = nombre;
        this.numeroNacional = numeroNacional;

        this.imagenUrl =
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/"
                        + id + ".png";
    }

    public int getId() {
        return id;
    }

    public int getNumeroNacional() {
        return numeroNacional;
    }

    public String getNombre() {
        return nombre;
    }

    public String getNombreMostrado() {

        if (nombre.contains("-alola")) {
            return capitalizar(nombre.substring(0, nombre.indexOf("-alola")))
                    + " de Alola";
        }

        if (nombre.contains("-galar")) {
            return capitalizar(nombre.substring(0, nombre.indexOf("-galar")))
                    + " de Galar";
        }

        if (nombre.contains("-hisui")) {
            return capitalizar(nombre.substring(0, nombre.indexOf("-hisui")))
                    + " de Hisui";
        }

        if (nombre.contains("-paldea")) {

            String nombreBase =
                    nombre.substring(0, nombre.indexOf("-paldea"));

            String resultado = capitalizar(nombreBase) + " de Paldea";

            String resto =
                    nombre.substring(nombre.indexOf("-paldea") + 7);

            if (!resto.isEmpty()) {
                resultado += " (" + resto.replace("-", " ") + ")";
            }

            return resultado;
        }

        return capitalizar(nombre.replace("-", " "));
    }

    private String capitalizar(String texto) {

        String[] palabras = texto.split(" ");
        StringBuilder resultado = new StringBuilder();

        for (String palabra : palabras) {

            if (palabra.isEmpty()) {
                continue;
            }

            if (resultado.length() > 0) {
                resultado.append(" ");
            }

            resultado.append(
                    palabra.substring(0, 1).toUpperCase(Locale.ROOT)
            );

            if (palabra.length() > 1) {
                resultado.append(palabra.substring(1));
            }
        }

        return resultado.toString();
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public String getNumeroFormateado() {
        return String.format(Locale.ROOT, "#%04d", numeroNacional);
    }

    public String getRegion() {

        if (nombre.contains("-alola")) return "Formas de Alola";
        if (nombre.contains("-galar")) return "Formas de Galar";
        if (nombre.contains("-hisui")) return "Formas de Hisui";
        if (nombre.contains("-paldea")) return "Formas de Paldea";

        if (numeroNacional <= 151) return "Kanto";
        if (numeroNacional <= 251) return "Johto";
        if (numeroNacional <= 386) return "Hoenn";
        if (numeroNacional <= 493) return "Sinnoh";
        if (numeroNacional <= 649) return "Teselia";
        if (numeroNacional <= 721) return "Kalos";
        if (numeroNacional <= 809) return "Alola";
        if (numeroNacional <= 905) return "Galar";
        if (numeroNacional <= 1025) return "Paldea";

        return "Otras regiones";
    }


    public String getEtiquetaFormaRegional() {

        if (nombre.contains("-alola")) return "Forma de Alola";
        if (nombre.contains("-galar")) return "Forma de Galar";
        if (nombre.contains("-hisui")) return "Forma de Hisui";
        if (nombre.contains("-paldea")) return "Forma de Paldea";

        return null;
    }

}
