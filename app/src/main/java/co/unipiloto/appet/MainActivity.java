package co.unipiloto.appet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class MainActivity extends AppCompatActivity {

    private ImageButton btnIrPerfil;

    private TextView txtVerPerfil;

    private Button btnIrLogin,btnRegistrar, historial_mascota, citas_mascota, ejercicios_mascota,
            perfil_mascota, ritmo_cardiaco, btnVincular, btnIrRecorridoFecha, btnHacerRecorrido,
            btnUbicacionActual, btnRitmoCardiacoFecha, btnCancion;
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
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnIrLogin = findViewById(R.id.btnIrLogin);
        historial_mascota = findViewById(R.id.historial_mascota);
        citas_mascota = findViewById(R.id.citas_mascota);
        ejercicios_mascota = findViewById(R.id.ejercicios_mascota);
        perfil_mascota = findViewById(R.id.perfil_mascota);
        ritmo_cardiaco = findViewById(R.id.ritmo_cardiaco);
        btnIrRecorridoFecha = findViewById(R.id.btnIrRecorridoFecha);
        btnHacerRecorrido = findViewById(R.id.btnHacerRecorrido);
        btnVincular = findViewById(R.id.vincular);
        btnUbicacionActual = findViewById(R.id.btnUbicacionActual);
        btnRitmoCardiacoFecha = findViewById(R.id.ritmo_cardiaco_grafica);
        btnCancion = findViewById(R.id.btnCancion);
        mostrarBotones();
    }
    private void mostrarBotones(){
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correo = sharedPreferences.getString("correo", null);
        String correoVet  = sharedPreferences.getString("correoVet", null);
        String correoCui = sharedPreferences.getString("correoCui", null);

        if (correo != null && !correo.isEmpty()) {
            btnIrPerfil.setVisibility(View.VISIBLE);
            txtVerPerfil.setVisibility(View.VISIBLE);
            historial_mascota.setVisibility(View.VISIBLE);
            citas_mascota.setVisibility(View.VISIBLE);
            btnRegistrar.setVisibility(View.GONE);
            btnIrLogin.setVisibility(View.GONE);
            ejercicios_mascota.setVisibility(View.VISIBLE);
            perfil_mascota.setVisibility(View.VISIBLE);
            ritmo_cardiaco.setVisibility(View.GONE);
            btnVincular.setVisibility(View.VISIBLE);
            btnIrRecorridoFecha.setVisibility(View.VISIBLE);
            btnHacerRecorrido.setVisibility(View.VISIBLE);
            btnUbicacionActual.setVisibility(View.VISIBLE);
            btnRitmoCardiacoFecha.setVisibility(View.VISIBLE);
            btnCancion.setVisibility(View.VISIBLE);
        }else if (correoVet != null && !correoVet.isEmpty()){
            btnIrPerfil.setVisibility(View.VISIBLE);
            txtVerPerfil.setVisibility(View.VISIBLE);
            historial_mascota.setVisibility(View.VISIBLE);
            citas_mascota.setVisibility(View.VISIBLE);
            btnRegistrar.setVisibility(View.GONE);
            btnIrLogin.setVisibility(View.GONE);
            ejercicios_mascota.setVisibility(View.GONE);
            perfil_mascota.setVisibility(View.VISIBLE);
            ritmo_cardiaco.setVisibility(View.VISIBLE);
            btnVincular.setVisibility(View.GONE);
            btnIrRecorridoFecha.setVisibility(View.GONE);
            btnHacerRecorrido.setVisibility(View.GONE);
            btnUbicacionActual.setVisibility(View.GONE);
            btnRitmoCardiacoFecha.setVisibility(View.VISIBLE);
            btnCancion.setVisibility(View.GONE);
        }else if(correoCui != null && !correoCui.isEmpty()){
            btnIrPerfil.setVisibility(View.VISIBLE);
            txtVerPerfil.setVisibility(View.VISIBLE);
            historial_mascota.setVisibility(View.GONE);
            citas_mascota.setVisibility(View.GONE);
            btnRegistrar.setVisibility(View.GONE);
            btnIrLogin.setVisibility(View.GONE);
            ejercicios_mascota.setVisibility(View.VISIBLE);
            perfil_mascota.setVisibility(View.VISIBLE);
            ritmo_cardiaco.setVisibility(View.GONE);
            btnVincular.setVisibility(View.GONE);
            btnIrRecorridoFecha.setVisibility(View.VISIBLE);
            btnHacerRecorrido.setVisibility(View.VISIBLE);
            btnUbicacionActual.setVisibility(View.VISIBLE);
            btnRitmoCardiacoFecha.setVisibility(View.GONE);
            btnCancion.setVisibility(View.VISIBLE);
        }else{
            btnIrPerfil.setVisibility(View.GONE);
            txtVerPerfil.setVisibility(View.GONE);
            historial_mascota.setVisibility(View.GONE);
            citas_mascota.setVisibility(View.GONE);
            btnRegistrar.setVisibility(View.VISIBLE);
            btnIrLogin.setVisibility(View.VISIBLE);
            ejercicios_mascota.setVisibility(View.GONE);
            perfil_mascota.setVisibility(View.GONE);
            ritmo_cardiaco.setVisibility(View.GONE);
            btnVincular.setVisibility(View.GONE);
            btnIrRecorridoFecha.setVisibility(View.GONE);
            btnIrPerfil.setVisibility(View.GONE);
            txtVerPerfil.setVisibility(View.GONE);
            btnHacerRecorrido.setVisibility(View.GONE);
            btnUbicacionActual.setVisibility(View.GONE);
            btnRitmoCardiacoFecha.setVisibility(View.GONE);
            btnCancion.setVisibility(View.GONE);
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

    public void onClickIrHistorial(View view){
        Intent intent = new Intent(MainActivity.this, HistorialMedico.class);
        startActivity(intent);
    }

    public void onClickIrAgenda(View view){
        Intent intent = new Intent(MainActivity.this, PerfilAgenda.class);
        startActivity(intent);
    }

    public void onClickIrEjercicios(View view){
        Intent intent = new Intent(MainActivity.this, Ejercicios.class);
        startActivity(intent);
    }

    public void onClickIrRitmo(View view){
        Intent intent = new Intent(MainActivity.this, RitmoCardiaco.class);
        startActivity(intent);
    }

    public void onClickIrPerfilMascota(View view){
        Intent intent = new Intent(MainActivity.this, PerfilMascota.class);
        startActivity(intent);
    }

    public void onClickIrVincular(View view){
        Intent intent = new Intent(MainActivity.this, Vincular.class);
        startActivity(intent);
    }

    public void onClickIrRecorridoFecha(View view){
        Intent intent = new Intent(MainActivity.this, RecorridoFechaActivity.class);
        startActivity(intent);
    }

    public void onClickHacerRecorrido(View view){
        Intent intent = new Intent(MainActivity.this, HacerRecorridoActivity.class);
        startActivity(intent);
    }

    public void onClickUbicacionActual(View view){
        Intent intent = new Intent(MainActivity.this, UbicacionActualActivity.class);
        startActivity(intent);
    }

    public void onClickIrRitmoGrafica(View view){
        Intent intent = new Intent(MainActivity.this, EstadisticasActivity.class);
        startActivity(intent);
    }

    public void onClickCancion(View view){
        Intent intent = new Intent(MainActivity.this, CancionActivity.class);
        startActivity(intent);
    }

}
