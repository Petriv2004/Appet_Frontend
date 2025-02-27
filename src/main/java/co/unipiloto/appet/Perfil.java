package co.unipiloto.appet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Perfil extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);
    }

    public void onClickPerfilMascota(View view) {
        Intent intent = new Intent(Perfil.this, RegistroMascota.class);
        startActivity(intent);
    }
}
