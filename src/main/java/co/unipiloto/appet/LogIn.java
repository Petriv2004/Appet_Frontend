package co.unipiloto.appet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsCompat.Type;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class LogIn extends AppCompatActivity {

    private EditText etCorreo, etContrasena;
    private static final String URL_LOGIN = "http://192.168.0.13:8080/propietarios/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(insets.getInsets(Type.systemBars()).left,
                    insets.getInsets(Type.systemBars()).top,
                    insets.getInsets(Type.systemBars()).right,
                    insets.getInsets(Type.systemBars()).bottom);
            return insets;
        });
    }

    public void onClickLogIn(View view) {
        String correo = etCorreo.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();

        if (correo.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Correo no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        iniciarSesion(correo, contrasena);
    }

    private void iniciarSesion(String correo, String contrasena) {

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.trim().isEmpty()) {
                            Toast.makeText(LogIn.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();

                            SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("correo", correo);
                            editor.apply();


                            Intent intent = new Intent(LogIn.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LogIn.this, "Respuesta inesperada del servidor", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            if (statusCode == 400) {
                                Toast.makeText(LogIn.this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LogIn.this, "Error en el servidor: " + statusCode, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LogIn.this, "Error de conexión. Verifica tu internet", Toast.LENGTH_SHORT).show();
                        }
                    }
                }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                try {
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("correo", correo);
                    jsonBody.put("contrasena", contrasena);
                    return jsonBody.toString().getBytes("utf-8");
                } catch (JSONException | java.io.UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };

        queue.add(stringRequest);
    }

    public void guardarCorreo(String correo) {
        SharedPreferences preferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("correo", correo);
        editor.apply();
    }

}
