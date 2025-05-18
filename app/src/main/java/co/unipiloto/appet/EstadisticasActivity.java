package co.unipiloto.appet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EstadisticasActivity extends AppCompatActivity {

    private Button ritmo_cardiaco_grafica, estadisticas_mascota, mascota_activa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas);
        ImageView leftIcon = findViewById(R.id.left_icon);
        leftIcon.setVisibility(View.VISIBLE);
        TextView title = findViewById(R.id.title);
        title.setText("Estadísticas");

        leftIcon.setOnClickListener(v -> {
            Intent intent = new Intent(EstadisticasActivity.this, MainActivity.class);
            startActivity(intent);
        });

        ritmo_cardiaco_grafica = findViewById(R.id.ritmo_cardiaco_grafica);
        estadisticas_mascota = findViewById(R.id.estadisticas_por_mascota);
        mascota_activa = findViewById(R.id.mascota);

        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correo = sharedPreferences.getString("correo", null);

        if (correo != null) {
            ritmo_cardiaco_grafica.setVisibility(View.VISIBLE);
            estadisticas_mascota.setVisibility(View.VISIBLE);
            mascota_activa.setVisibility(View.VISIBLE);
        } else {
            ritmo_cardiaco_grafica.setVisibility(View.VISIBLE);
            estadisticas_mascota.setVisibility(View.GONE);
            mascota_activa.setVisibility(View.GONE);
        }

    }
    public void onClickIrRitmoGrafica(View view){
        Intent intent = new Intent(EstadisticasActivity.this, RitmoCardiacoFecha.class);
        startActivity(intent);
    }

    public void onClickIrEstadisticas(View view){
        Intent intent = new Intent(EstadisticasActivity.this, EstadisticasMascotaActivity.class);
        startActivity(intent);
    }

    public void onClickIrMascota(View view){
        Intent intent = new Intent(EstadisticasActivity.this, MascotaActivaActivity.class);
        startActivity(intent);
    }


}