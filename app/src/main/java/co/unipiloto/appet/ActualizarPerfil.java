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
import org.json.JSONObject;

public class ActualizarPerfil extends AppCompatActivity {

    EditText etcorreo;
    String correoViejo;
    EditText etnombre;
    EditText etcelular;
    EditText etdireccion;

    RadioGroup rgGenero;
    String URL_OBTENER_PROPIETARIO =Url.URL+"/propietarios/obtener_propietario/";

    RequestQueue queue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizar_perfil);

        queue = Volley.newRequestQueue(this);
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
        String correo = etcorreo.getText().toString();
        String nombre = etnombre.getText().toString();
        String numero = etcelular.getText().toString();
        String direccion = etdireccion.getText().toString();
        int genero = rgGenero.getCheckedRadioButtonId();

        if (correo.isEmpty() || nombre.isEmpty() || numero.isEmpty() || direccion.isEmpty() || genero == -1) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        String Strgenero;
        if (genero == R.id.rbMasculino) {
            Strgenero = "Masculino";
        } else if (genero == R.id.rbFemenino) {
            Strgenero = "Femenino";
        } else if (genero == R.id.rbNoBinario) {
            Strgenero = "No Binario";
        } else {
            Strgenero = "Otro";
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("correo", correo);
            jsonBody.put("nombre", nombre);
            jsonBody.put("celular", numero);
            jsonBody.put("direccion", direccion);
            jsonBody.put("genero", Strgenero);
        } catch (JSONException e) {
            Toast.makeText(this, "Error al crear el JSON", Toast.LENGTH_SHORT).show();
            return;
        }

        String urlPut = Url.URL+"/propietarios/actualizar-propietario/" + correoViejo;

        JsonObjectRequest requestPut = new JsonObjectRequest(Request.Method.PUT, urlPut, jsonBody,
                response -> {
                    Toast.makeText(this, "Usuario actualizado exitosamente", Toast.LENGTH_SHORT).show();

                    SharedPreferences pref = getSharedPreferences("AppPreferences", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();

                    String correoAnterior = pref.getString("correo", null);
                    String correoVet = pref.getString("correoVet", null);
                    String correoCui = pref.getString("correoCui", null);

                    if (correoAnterior != null) {
                        editor.putString("correo", correo);
                    } else if (correoVet != null) {
                        editor.putString("correoVet", correo);
                    } else if (correoCui != null) {
                        editor.putString("correoCui", correo);
                    } else {
                        editor.putString("correo", correo);
                    }

                    editor.apply();

                    Intent intent = new Intent(ActualizarPerfil.this, MainActivity.class);
                    startActivity(intent);
                },
                error -> Toast.makeText(this, "Error al actualizar el usuario", Toast.LENGTH_SHORT).show()
        );

        queue.add(requestPut);
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
        correoViejo = correo;
        String url = URL_OBTENER_PROPIETARIO + correo;

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