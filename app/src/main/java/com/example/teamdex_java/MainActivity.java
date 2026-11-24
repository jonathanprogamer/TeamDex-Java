
package com.example.teamdex_java;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teamdex_java.ui.EquiposActivity;
import com.example.teamdex_java.ui.PokedexActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button btnEquipos;
    private Button btnPokedex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnEquipos = findViewById(R.id.btnEquipos);
        btnPokedex = findViewById(R.id.btnPokedex);

        btnEquipos.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    EquiposActivity.class
            );
            startActivity(intent);
        });

        btnPokedex.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    PokedexActivity.class
            );
            startActivity(intent);
        });

        probarConexionFirebase();
    }

    private void probarConexionFirebase() {

        FirebaseFirestore db = FirebaseManager.obtenerDB();

        Map<String, Object> prueba = new HashMap<>();
        prueba.put("aplicacion", "TeamDex");
        prueba.put("mensaje", "Conexion con Firebase exitosa");
        prueba.put("plataforma", "Android Java");

        db.collection("pruebas")
                .document("conexion")
                .set(prueba)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(
                            MainActivity.this,
                            "¡Firebase conectado correctamente!",
                            Toast.LENGTH_LONG
                    ).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            MainActivity.this,
                            "Error Firebase: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}
