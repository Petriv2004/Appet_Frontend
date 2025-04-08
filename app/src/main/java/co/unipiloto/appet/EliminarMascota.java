package co.unipiloto.appet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EliminarMascota extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eliminar_mascota);

        ImageView leftIcon = findViewById(R.id.left_icon);
        leftIcon.setVisibility(View.VISIBLE);
        TextView title = findViewById(R.id.title);
        title.setText("Eliminar Mascota");

        leftIcon.setOnClickListener(v -> {
            Intent intent = new Intent(EliminarMascota.this, MainActivity.class);
            startActivity(intent);
        });
    }

    public void onClickEliminarMascota(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Está seguro de que desea eliminar esta mascota? \nEsta " +
                        "acción no se puede deshacer y se perderan todos los datos relacionados a la mascota.")
                .setPositiveButton("Sí", (dialog, which) -> {
                    eliminarMascota();
                })
                .setNegativeButton("Cancelar", null)
                .show();


    }

    private void eliminarMascota() {
        Toast.makeText(this, "Mascota eliminada", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, PerfilMascota.class);
        startActivity(intent);
    }
}