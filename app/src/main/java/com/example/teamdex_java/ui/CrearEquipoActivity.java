package com.example.teamdex_java.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teamdex_java.R;
import com.example.teamdex_java.data.EquipoRepository;
import com.example.teamdex_java.model.Equipo;

public class CrearEquipoActivity extends AppCompatActivity {

    private EditText edtNombreEquipo;
    private Button btnGuardarEquipo;
    private Button btnCancelar;

    private EquipoRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_equipo);

        repository = new EquipoRepository(this);

        edtNombreEquipo = findViewById(R.id.edtNombreEquipo);
        btnGuardarEquipo = findViewById(R.id.btnGuardarEquipo);
        btnCancelar = findViewById(R.id.btnCancelar);

        btnGuardarEquipo.setOnClickListener(v -> guardarEquipo());

        btnCancelar.setOnClickListener(v -> finish());
    }

    private void guardarEquipo() {

        String nombre = edtNombreEquipo.getText().toString().trim();

        if (nombre.isEmpty()) {
            edtNombreEquipo.setError("Escribe un nombre para tu equipo");
            edtNombreEquipo.requestFocus();
            return;
        }

        Equipo equipo = new Equipo(nombre);

        repository.guardarEquipo(equipo);

        Toast.makeText(
                this,
                "¡Equipo creado correctamente!",
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }
}
