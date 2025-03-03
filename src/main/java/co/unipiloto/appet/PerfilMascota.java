package co.unipiloto.appet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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

public class PerfilMascota extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MascotaAdapter mascotaAdapter;
    private List<Map<String, String>> listaMascotas;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_mascota);

        recyclerView = findViewById(R.id.recyclerViewMascotas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaMascotas = new ArrayList<>();
        mascotaAdapter = new MascotaAdapter(listaMascotas);
        recyclerView.setAdapter(mascotaAdapter);

        requestQueue = Volley.newRequestQueue(this);

        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correoUsuario = prefs.getString("correo", null);

        if (correoUsuario != null) {
            obtenerMascotasPropietario(correoUsuario);
        } else {
            Toast.makeText(this, "Error: No se encontró el correo del usuario", Toast.LENGTH_SHORT).show();
        }
    }

    private void obtenerMascotasPropietario(String correo) {
        String url = "http://192.168.0.13:8080/propietarios/obtener_propietario/" + correo;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray mascotasArray = response.getJSONArray("macotasList");
                        listaMascotas.clear();
                        for (int i = 0; i < mascotasArray.length(); i++) {
                            JSONObject mascotaJson = mascotasArray.getJSONObject(i);
                            Map<String, String> mascota = new HashMap<>();
                            mascota.put("id_mascota", String.valueOf(mascotaJson.getInt("id_mascota")));
                            mascota.put("nombre", mascotaJson.getString("nombre"));
                            mascota.put("tipo", mascotaJson.getString("tipo"));
                            mascota.put("raza", mascotaJson.getString("raza"));
                            mascota.put("fecha_nacimiento", mascotaJson.getString("fecha_nacimiento"));

                            if (!mascotaJson.isNull("historial")) {
                                JSONObject historial = mascotaJson.getJSONObject("historial");
                                mascota.put("enfermedades", historial.optString("enfermedades", "N/A"));
                                mascota.put("medicinas", historial.optString("medicinas", "N/A"));
                                mascota.put("sangre", historial.optString("sangre", "N/A"));
                                mascota.put("peso", String.valueOf(historial.optInt("peso", 0)));
                                mascota.put("vacunas", historial.optString("vacunas", "N/A"));
                                mascota.put("foto", historial.optString("foto", ""));
                            }
                            listaMascotas.add(mascota);
                        }
                        mascotaAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error en la conexión", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(request);
    }


    public void onClickIrHistorial(View view){
        Intent intent = new Intent(PerfilMascota.this, HistorialMedico.class);
        startActivity(intent);
    }

    public void onClickIrAgenda(View view){
        Intent intent = new Intent(PerfilMascota.this, RegistrarAgenda.class);
        startActivity(intent);
    }

    public void onClickAgregarMascota(View view){
        Intent intent = new Intent(PerfilMascota.this, RegistroMascota.class);
        startActivity(intent);
    }
}
