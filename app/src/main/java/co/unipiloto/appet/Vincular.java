package co.unipiloto.appet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

public class Vincular extends AppCompatActivity {

    private Button vincular;

    private EditText etCorreo;

    private RequestQueue requestQueue;

    private RadioGroup rgRol;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vincular);

        vincular = findViewById(R.id.vincular);
        etCorreo = findViewById(R.id.etCorreo);
        rgRol = findViewById(R.id.rgRol);
        requestQueue = Volley.newRequestQueue(this);
    }

    public void onClickVincular(View view) {
        String url = Url.URL+"/propietarios/asociar-veterinario-por-correo";
        if (rgRol.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Tiene que seleccionar un rol", Toast.LENGTH_SHORT).show();
            return;
        }else if (rgRol.getCheckedRadioButtonId() == R.id.rbVeterinario) {
             url = Url.URL+"/propietarios/asociar-veterinario-por-correo";
        }else{
             url = Url.URL+"/propietarios/asociar-cuidador-por-correo";
        }


        String correoVet = etCorreo.getText().toString().trim().toLowerCase();

        if (correoVet.isEmpty() ) {
            Toast.makeText(this, "Tiene que escribir el correo", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
            String correo = prefs.getString("correo", null);
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("correoPropietario", correo);
            jsonBody.put("correoVeterinario", correoVet);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> {
                        Toast.makeText(this, "Vinculado Exitosamente", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    },
                    error -> Toast.makeText(this, "Error al Vincular", Toast.LENGTH_SHORT).show()
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
