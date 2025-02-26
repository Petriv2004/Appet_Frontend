package com.example.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class ActividadChat extends AppCompatActivity {

    private TextView textViewHistorial;
    private EditText editTextMensaje;
    private Button btnEnviar;
    private ArrayList<String> historialMensajes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividad_chat);

        textViewHistorial = findViewById(R.id.textViewHistorial);
        editTextMensaje = findViewById(R.id.editTextMensaje);
        btnEnviar = findViewById(R.id.btnEnviar);

        // Obtener historial anterior si existe
        historialMensajes = getIntent().getStringArrayListExtra("historial");
        if (historialMensajes == null) {
            historialMensajes = new ArrayList<>();
        }

        // Mostrar historial
        actualizarHistorial();

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editTextMensaje.getText().toString().isEmpty()){
                    String mensaje = "Cuidador: " + editTextMensaje.getText().toString();
                    historialMensajes.add(mensaje);
                    editTextMensaje.setText("");

                    // Enviar historial a MainActivity
                    Intent intent = new Intent(ActividadChat.this, MainActivity.class);
                    intent.putStringArrayListExtra("historial", historialMensajes);
                    startActivity(intent);
                }
            }
        });
    }

    private void actualizarHistorial() {
        StringBuilder historialTexto = new StringBuilder();
        for (String mensaje : historialMensajes) {
            historialTexto.append(mensaje).append("\n");
        }
        textViewHistorial.setText(historialTexto.toString());
    }
}
