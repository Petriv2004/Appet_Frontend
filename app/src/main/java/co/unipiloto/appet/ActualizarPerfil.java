package co.unipiloto.appet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

public class ActualizarPerfil extends AppCompatActivity {

    EditText etcorreo;

    EditText etnombre;
    EditText etcelular;
    EditText etdireccion;

    RadioGroup rgGenero;
    String URL_OBTENER_PROPIETARIO ="http://192.168.0.13:8080/propietarios/obtener_propietario/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizar_perfil);

        etcorreo = findViewById(R.id.etCorreo);
        etnombre = findViewById(R.id.etNombreCompleto);
        etcelular = findViewById(R.id.etNumeroCelular);
        etdireccion = findViewById(R.id.etDireccion);
        rgGenero = findViewById(R.id.rgGenero);

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

    public void onClickActualizar(View view) {
        Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ActualizarPerfil.this, MainActivity.class);
        startActivity(intent);
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
            etcorreo.setText(correo);
        }else if (!correoVet.isEmpty()){
            etcorreo.setText(correoVet);
            correo = correoVet;
        }else{
            etcorreo.setText(correoCui);
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

                        if (genero.equalsIgnoreCase("Masculino")) {
                            rgGenero.check(R.id.rbMasculino);
                        } else if (genero.equalsIgnoreCase("Femenino")) {
                            rgGenero.check(R.id.rbFemenino);
                        } else if (genero.equalsIgnoreCase("No Binario")) {
                            rgGenero.check(R.id.rbNoBinario);
                        } else if (genero.equalsIgnoreCase("Otro")) {
                            rgGenero.check(R.id.rbOtro);
                        }
                        etnombre.setText(nombre);
                        etcelular.setText(celular);
                        etdireccion.setText(direccion);

                    } catch (JSONException e) {
                        Toast.makeText(this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error en la conexión con el servidor", Toast.LENGTH_SHORT).show());

        queue.add(jsonRequest);
    }
}