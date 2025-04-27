package co.unipiloto.appet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EstadisticasActivity extends AppCompatActivity {

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