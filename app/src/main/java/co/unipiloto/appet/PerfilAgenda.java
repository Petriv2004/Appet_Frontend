package co.unipiloto.appet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerfilAgenda extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CitaAdapter citaAdapter;
    private List<Map<String, String>> listaCitas;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_agenda);

        recyclerView = findViewById(R.id.recyclerViewCitas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaCitas = new ArrayList<>();
        citaAdapter = new CitaAdapter(listaCitas);
        recyclerView.setAdapter(citaAdapter);

        requestQueue = Volley.newRequestQueue(this);

        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correoUsuario = prefs.getString("correo", null);

        if (correoUsuario != null) {
            obtenerCitasPropietario(correoUsuario);
        } else {
            Toast.makeText(this, "Error: No se encontró el correo del usuario", Toast.LENGTH_SHORT).show();
        }

        ImageView leftIcon = findViewById(R.id.left_icon);
        leftIcon.setVisibility(View.VISIBLE);
        TextView title = findViewById(R.id.title);
        title.setText("Tus citas");

        leftIcon.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilAgenda.this, MainActivity.class);
            startActivity(intent);
        });
    }
    private void obtenerCitasPropietario(String correo) {
        String url = "http://192.168.0.13:8080/propietarios/obtener_propietario/" + correo;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray mascotasArray = response.getJSONArray("macotasList");
                        listaCitas.clear();
                        for (int i = 0; i < mascotasArray.length(); i++) {
                            JSONObject mascotaJson = mascotasArray.getJSONObject(i);
                            String nombreMascota = mascotaJson.getString("nombre");
                            JSONArray citasArray = mascotaJson.getJSONArray("citas");

                            for (int j = 0; j < citasArray.length(); j++) {
                                JSONObject citaJson = citasArray.getJSONObject(j);

                                Map<String, String> cita = new HashMap<>();
                                cita.put("id_agenda", String.valueOf(citaJson.getInt("id_agenda")));
                                cita.put("nombre_mascota", nombreMascota);
                                cita.put("fecha", citaJson.getString("fecha"));
                                cita.put("hora", citaJson.getString("hora"));
                                cita.put("razon", citaJson.getString("razon"));
                                cita.put("descripcion", citaJson.getString("descripcion"));

                                listaCitas.add(cita);
                            }
                        }
                        citaAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error en la conexión", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(request);
    }
    public void onClickIrGestionAgenda(View view) {
        Intent intent = new Intent(PerfilAgenda.this, RegistrarAgenda.class);
        startActivity(intent);
    }
}
