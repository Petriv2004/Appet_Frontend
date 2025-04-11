package co.unipiloto.appet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class CambiarContrasenaActivity extends AppCompatActivity {

    private EditText etCorreo, etCodigo, etContrasena, etConfirmarContrasena;
    private Button  btnEnviarCodigo, btnCambiarContrasena, btnCorreo;
    private String correo;

    private TextInputLayout tilConfirmarContrasena, tilContrasena;

    private static final String urlEnviarCorreo = "http://192.168.0.13:8080/token/obtener-token/";

    private static final String urlEnviarToken = "http://192.168.0.13:8080/token/verify-token/";

    private static final String urlActualizarContrasena = "http://192.168.0.13:8080/propietarios/actualizar-contrasena";

    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_contrasena);

        etCorreo = findViewById(R.id.etCorreo);
        etCodigo = findViewById(R.id.etCodigo);
        etContrasena = findViewById(R.id.etContrasena);
        etConfirmarContrasena = findViewById(R.id.etConfirmarContrasena);
        btnEnviarCodigo = findViewById(R.id.btnEnviarCodigo);
        btnCambiarContrasena = findViewById(R.id.btnCambiarContrasena);
        btnCorreo = findViewById(R.id.btnEnviarCorreo);
        tilContrasena = findViewById(R.id.tilContrasena);
        tilConfirmarContrasena = findViewById(R.id.tilConfirmarContrasena);
        tilContrasena.setVisibility(View.GONE);
        tilConfirmarContrasena.setVisibility(View.GONE);
        btnEnviarCodigo.setVisibility(View.GONE);
        btnCambiarContrasena.setVisibility(View.GONE);
        etCodigo.setVisibility(View.GONE);

        queue = Volley.newRequestQueue(this);

        ImageView leftIcon = findViewById(R.id.left_icon);
        leftIcon.setVisibility(View.VISIBLE);
        TextView title = findViewById(R.id.title);
        title.setText("Cambia Tu Contraseña");

        leftIcon.setOnClickListener(v -> {
            Intent intent = new Intent(CambiarContrasenaActivity.this, MainActivity.class);
            startActivity(intent);
        });

        SharedPreferences pref = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        correo = pref.getString("correo", pref.getString("correoVet", pref.getString("correoCui", null)));
        if (correo == null) {
            etCorreo.setVisibility(View.VISIBLE);
        } else {
            etCorreo.setVisibility(View.GONE);
        }
    }

    public void onClickIrEnviarCorreo(View view){
        if(correo == null || correo.isEmpty()){
            correo = etCorreo.getText().toString();
            if (correo == null || correo.isEmpty()){
                Toast.makeText(CambiarContrasenaActivity.this, "Tiene que escribir su correo", Toast.LENGTH_SHORT).show();
                return;
            }
            fetchEnviarCorreo();
        }else{
            fetchEnviarCorreo();
        }
    }

    public void fetchEnviarCorreo(){
        StringRequest request = new StringRequest(Request.Method.GET, urlEnviarCorreo + correo,
                response -> {
                    Toast.makeText(CambiarContrasenaActivity.this, "Se ha enviado un correo, revise su bandeja de entrada", Toast.LENGTH_SHORT).show();
                    btnEnviarCodigo.setVisibility(View.VISIBLE);
                    etCodigo.setVisibility(View.VISIBLE);
                    etCorreo.setVisibility(View.GONE);
                    btnCorreo.setVisibility(View.GONE);
                },
                error -> Toast.makeText(this, "Error al enviar correo", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }



    public void onClickIrEnviarCodigo(View view){
        String codigo = etCodigo.getText().toString();
        if (codigo == null || codigo.isEmpty()){
            Toast.makeText(CambiarContrasenaActivity.this, "Tiene que escribir el código", Toast.LENGTH_SHORT).show();
            return;
        }
        String token = etCodigo.getText().toString();
        StringRequest request = new StringRequest(Request.Method.GET, urlEnviarToken  + token + "/" + correo,
                response -> {
                    Toast.makeText(CambiarContrasenaActivity.this, "El token es correcto, por favor ingrese su nueva contraseña", Toast.LENGTH_SHORT).show();
                    btnEnviarCodigo.setVisibility(View.GONE);
                    etCodigo.setVisibility(View.GONE);
                    etCorreo.setVisibility(View.GONE);
                    btnCorreo.setVisibility(View.GONE);
                    tilContrasena.setVisibility(View.VISIBLE);
                    tilConfirmarContrasena.setVisibility(View.VISIBLE);
                    btnCambiarContrasena.setVisibility(View.VISIBLE);
                },
                error -> Toast.makeText(this, "Error al enviar código", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);

    }

    public void onClickIrCambiarContrasena(View view){
        String contrasena = etContrasena.getText().toString(), contrasena1 = etConfirmarContrasena.getText().toString();
        if (!contrasena.equals(contrasena1)){
            Toast.makeText(CambiarContrasenaActivity.this, "Las contraseñas tienen que coincidir", Toast.LENGTH_SHORT).show();
            return;
        }
        if(contrasena.isEmpty() || contrasena1.isEmpty() || contrasena==null|| contrasena1==null ){
            Toast.makeText(CambiarContrasenaActivity.this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("contrasena", contrasena);
            jsonBody.put("correo", correo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest requestPut = new JsonObjectRequest(Request.Method.PUT, urlActualizarContrasena, jsonBody,
                response -> {
                    Toast.makeText(CambiarContrasenaActivity.this, "Se ha actualizado la contraseña", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CambiarContrasenaActivity.this, MainActivity.class);
                    startActivity(intent);
                },
                error -> Toast.makeText(this, "Error al actualizar historial", Toast.LENGTH_SHORT).show()
        );
        queue.add(requestPut);
    }
}