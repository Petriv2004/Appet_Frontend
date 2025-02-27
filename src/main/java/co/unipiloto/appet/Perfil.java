package co.unipiloto.appet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Perfil extends AppCompatActivity {

    private Spinner spinnerElegir;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);

        spinnerElegir = findViewById(R.id.spinnerElegir);

        ArrayAdapter<CharSequence> spinnerelegir = ArrayAdapter.createFromResource(
                this, R.array.perfil_mascota, android.R.layout.simple_spinner_item);
        spinnerelegir.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerElegir.setAdapter(spinnerelegir);
    }

    public void onClick(View view) {
        if (spinnerElegir != null && spinnerElegir.getSelectedItem() != null) {
            if (spinnerElegir.getSelectedItem().toString().equals("Registre a su mascota")) {
                Intent intent = new Intent(Perfil.this, RegistroMascota.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(Perfil.this, PerfilMascota.class);
                startActivity(intent);
            }
        }
    }


}
