package co.unipiloto.appet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class MainActivity extends AppCompatActivity {

    private ImageButton btnIrPerfil;

    private TextView txtVerPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correo = sharedPreferences.getString("correo", null);
        btnIrPerfil = findViewById(R.id.btnIrPerfil);
        txtVerPerfil = findViewById(R.id.txtVerPerfil);
        if (correo == null || correo.isEmpty()) {
            btnIrPerfil.setVisibility(View.GONE);
            txtVerPerfil.setVisibility(View.GONE);
        }else{
            btnIrPerfil.setVisibility(View.VISIBLE);
            txtVerPerfil.setVisibility(View.VISIBLE);
        }
    }

    public void onClickIrRegistrar(View view){
        Intent intent = new Intent(MainActivity.this, RegistroUsuario.class);
        startActivity(intent);
    }

    public void onClickIrLogIn(View view){
        Intent intent = new Intent(MainActivity.this, LogIn.class);
        startActivity(intent);
    }
    public void onClickIrPerfil(View view){
        Intent intent = new Intent(MainActivity.this, PerfilUsuario.class);
        startActivity(intent);
    }


}
