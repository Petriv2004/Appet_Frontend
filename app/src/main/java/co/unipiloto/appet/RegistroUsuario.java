package co.unipiloto.appet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegistroUsuario extends AppCompatActivity {
    private static final String URL_REGISTRO = "http://192.168.0.13:8080/propietarios/registrar";
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro_usuario);

        requestQueue = Volley.newRequestQueue(this);
        AutoCompleteTextView etDireccion = findViewById(R.id.etDireccion);
        new DireccionAutoComplete(this, etDireccion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void onClickRegistrar(View view) {
        EditText etCorreo = findViewById(R.id.etCorreo);
        EditText etContrasena = findViewById(R.id.etContrasena);
        EditText etConfirmarContrasena = findViewById(R.id.etConfirmarContrasena);
        EditText etNombreCompleto = findViewById(R.id.etNombreCompleto);
        EditText etNumeroCelular = findViewById(R.id.etNumeroCelular);
        EditText etDireccion = findViewById(R.id.etDireccion);
        RadioGroup rgGenero = findViewById(R.id.rgGenero);
        RadioGroup rgRol = findViewById(R.id.rgRol);

        String correo = etCorreo.getText().toString().trim().toLowerCase();
        String contrasena = etContrasena.getText().toString();
        String confirmarContrasena = etConfirmarContrasena.getText().toString();
        String nombre = etNombreCompleto.getText().toString().trim();
        String celular = etNumeroCelular.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();

        int rol = rgRol.getCheckedRadioButtonId();
        if (rol == -1) {
            Toast.makeText(this, "Selecciona un rol", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton rbRol = findViewById(rol);
        String Strol = rbRol.getText().toString();

        int generoSeleccionado = rgGenero.getCheckedRadioButtonId();
        if (generoSeleccionado == -1) {
            Toast.makeText(this, "Selecciona un género", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton rbGenero = findViewById(generoSeleccionado);
        String genero = rbGenero.getText().toString();

        if (!validarCampos(correo, contrasena, confirmarContrasena, nombre, celular, direccion)) {
            return;
        }

        registrarUsuario(correo, contrasena, nombre, celular, direccion, genero, Strol);
    }

    private boolean validarCampos(String correo, String contrasena, String confirmarContrasena, String nombre, String celular, String direccion) {
        if (correo.isEmpty() || contrasena.isEmpty() || confirmarContrasena.isEmpty() || nombre.isEmpty() || celular.isEmpty() || direccion.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Correo no válido", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!contrasena.equals(confirmarContrasena)) {
            Toast.makeText(this, "Tus contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!celular.matches("\\d{10}")) {
            Toast.makeText(this, "El teléfono debe tener 10 números", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void registrarUsuario(String correo, String contrasena, String nombre, String celular, String direccion, String genero, String rol) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("nombre", nombre);
            jsonBody.put("correo", correo);
            jsonBody.put("celular", celular);
            jsonBody.put("contrasena", contrasena);
            jsonBody.put("direccion", direccion);
            jsonBody.put("genero", genero);
            jsonBody.put("rol", rol);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, URL_REGISTRO, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(RegistroUsuario.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegistroUsuario.this, MainActivity.class));
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null) {
                            int statusCode = networkResponse.statusCode;
                            if (statusCode == 400) {
                                Toast.makeText(RegistroUsuario.this, "El usuario ya existe o hay un error en los datos", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RegistroUsuario.this, "Error en la conexión: " + statusCode, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(RegistroUsuario.this, "Error desconocido", Toast.LENGTH_SHORT).show();
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(jsonRequest);
    }
}
