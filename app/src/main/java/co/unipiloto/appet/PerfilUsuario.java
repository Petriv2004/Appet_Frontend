package co.unipiloto.appet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class PerfilUsuario extends AppCompatActivity {

    private TextView textCorreo, textNombre, textNumero, textDireccion, textGenero;

    private static final String URL_OBTENER_PROPIETARIO = "http://192.168.0.13:8080/propietarios/obtener_propietario/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        textCorreo = findViewById(R.id.textCorreo);
        textNombre = findViewById(R.id.textNombre);
        textNumero = findViewById(R.id.textNumero);
        textDireccion = findViewById(R.id.textDireccion);
        textGenero = findViewById(R.id.textGenero);

        ArrayAdapter<CharSequence> spinnerelegir = ArrayAdapter.createFromResource(
                this, R.array.perfil_mascota, android.R.layout.simple_spinner_item);
        spinnerelegir.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        obtenerDatosUsuario();
    }
    private void obtenerDatosUsuario() {
        SharedPreferences pref = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correo = pref.getString("correo", "");
        String correoVet = pref.getString("correoVet", "");
        String correoCui = pref.getString("correoCui", "");

        if (correo.isEmpty() && correoVet.isEmpty() && correoCui.isEmpty()) {
            Toast.makeText(this, "No se encontró el correo guardado", Toast.LENGTH_SHORT).show();
            return;
        }else if (!correo.isEmpty()){
            textCorreo.setText("Correo: " + correo);
        }else if (!correoVet.isEmpty()){
            textCorreo.setText("Correo: " + correoVet);
            correo = correoVet;
        }else{
            textCorreo.setText("Correo: " + correoCui);
            correo = correoCui;
        }

        String url = URL_OBTENER_PROPIETARIO + correo;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String nombre = response.getString("nombre");
                        String celular = response.getString("celular");
                        String direccion = response.getString("direccion");
                        String genero = response.getString("genero");

                        textNombre.setText("Nombre: " + nombre);
                        textNumero.setText("Número de celular: " + celular);
                        textDireccion.setText("Dirección: " + direccion);
                        textGenero.setText("Género: " + genero);
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error en la conexión con el servidor", Toast.LENGTH_SHORT).show());

        queue.add(jsonRequest);
    }
    public void onClickIrPerfilMascota(View view) {
        Intent intent = new Intent(PerfilUsuario.this, PerfilMascota.class);
        startActivity(intent);
    }
    public void onClickCerrar(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Está seguro de que desea cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    cerrarSesion();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
    private void cerrarSesion() {
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("correo", null);
        editor.putString("correoVet", null);
        editor.putString("correoCui", null);
        editor.apply();
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(PerfilUsuario.this, MainActivity.class);
        startActivity(intent);
    }

}
