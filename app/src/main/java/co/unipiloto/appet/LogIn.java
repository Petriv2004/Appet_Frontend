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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class LogIn extends AppCompatActivity {

    private EditText etCorreo, etContrasena;
    private static final String URL_LOGIN = "http://192.168.0.13:8080/propietarios/login";

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);
        requestQueue = Volley.newRequestQueue(this);

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

    private void iniciarSesion(String correo, String contrasena){
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("correo", correo);
            jsonBody.put("contrasena", contrasena);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL_LOGIN, jsonBody,
                    response -> {
                        try{
                            SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            String rol = response.getString("rol");
                            if (rol.equals("Propietario")){
                                editor.putString("correo", correo);
                            }else if (rol.equals("Veterinario")){
                                editor.putString("correoVet", correo);
                            }else if (rol.equals("Cuidador")){
                                editor.putString("correoCui", correo);
                            }
                            editor.apply();
                        }catch(JSONException e){
                            Toast.makeText(this, "Error agregando el correo", Toast.LENGTH_SHORT).show();
                        }
                        Toast.makeText(this, "Ha iniciado sesión exitosamente", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    },
                    error -> Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
            ) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };

            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
