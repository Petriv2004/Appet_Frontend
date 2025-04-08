package co.unipiloto.appet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ActualizarPerfil extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizar_perfil);
        ImageView leftIcon = findViewById(R.id.left_icon);
        leftIcon.setVisibility(View.VISIBLE);
        TextView title = findViewById(R.id.title);
        title.setText("Actualiza tu perfil");

        leftIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ActualizarPerfil.this, MainActivity.class);
            startActivity(intent);
        });
        obtenerDatosUsuario();
    }

    private void obtenerDatosUsuario() {

    }

    public void onClickActualizar(View view) {
        Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ActualizarPerfil.this, MainActivity.class);
        startActivity(intent);
    }
}