package co.unipiloto.appet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
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

    private Button registrarMascotas, historial_mascota, citas_mascota;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_mascota);

        recyclerView = findViewById(R.id.recyclerViewMascotas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaMascotas = new ArrayList<>();
        mascotaAdapter = new MascotaAdapter(listaMascotas);
        recyclerView.setAdapter(mascotaAdapter);
        registrarMascotas = findViewById(R.id.registrarMascotas);
        historial_mascota = findViewById(R.id.historial_mascota);
        citas_mascota = findViewById(R.id.citas_mascota);

        ImageView leftIcon = findViewById(R.id.left_icon);
        leftIcon.setVisibility(View.VISIBLE);
        TextView title = findViewById(R.id.title);
        title.setText("Perfil de Mascotas");

        leftIcon.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilMascota.this, MainActivity.class);
            startActivity(intent);
        });

        requestQueue = Volley.newRequestQueue(this);

        mostrarBotones();

        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correo = prefs.getString("correo", null);
        String correoVet  = prefs.getString("correoVet", prefs.getString("correoCui", null));

        if (correo != null ) {
            obtenerMascotasPropietario();
        } else if (correoVet != null){
            obtenerMascotasPropietario();
        }else{
            Toast.makeText(this, "Error: No se encontró el correo del usuario", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarBotones(){
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correo = sharedPreferences.getString("correo", null);
        String correoVet  = sharedPreferences.getString("correoVet", null);

        if (correo != null && !correo.isEmpty()) {
            historial_mascota.setVisibility(View.VISIBLE);
            citas_mascota.setVisibility(View.VISIBLE);
            registrarMascotas.setVisibility(View.VISIBLE);
        }else if (correoVet != null && !correoVet.isEmpty()){
            historial_mascota.setVisibility(View.VISIBLE);
            citas_mascota.setVisibility(View.GONE);
            registrarMascotas.setVisibility(View.GONE);
        }else {
            historial_mascota.setVisibility(View.GONE);
            citas_mascota.setVisibility(View.GONE);
            registrarMascotas.setVisibility(View.GONE);
        }
    }

    private void obtenerMascotasPropietario() {
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String Prefcorreo = prefs.getString("correo", null);
        String correoVet  = prefs.getString("correoVet", prefs.getString("correoCui", null));


        String url;
        boolean esVeterinario = correoVet != null;

        if (esVeterinario) {
            url = "http://192.168.0.13:8080/propietarios/mascotas-veterinario/" + correoVet;
        } else if (Prefcorreo != null) {
            url = "http://192.168.0.13:8080/propietarios/obtener_propietario/" + Prefcorreo;
        } else {
            Toast.makeText(this, "No se encontró el correo del usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        if (esVeterinario) {
            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            procesarMascotas(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Toast.makeText(this, "Error en la conexión", Toast.LENGTH_SHORT).show();
                    }
            );
            requestQueue.add(request);
        } else {
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            JSONArray mascotasArray = response.getJSONArray("macotasList");
                            procesarMascotas(mascotasArray); // Maneja la lista desde el objeto propietario
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Toast.makeText(this, "Error en la conexión", Toast.LENGTH_SHORT).show();
                    }
            );
            requestQueue.add(request);
        }
    }


    private void procesarMascotas(JSONArray mascotasArray) throws JSONException {
        listaMascotas.clear();
        for (int i = 0; i < mascotasArray.length(); i++) {
            JSONObject mascotaJson = mascotasArray.getJSONObject(i);
            Map<String, String> mascota = new HashMap<>();
            mascota.put("id_mascota", String.valueOf(mascotaJson.getInt("id_mascota")));
            mascota.put("nombre", mascotaJson.getString("nombre"));
            mascota.put("tipo", mascotaJson.getString("tipo"));
            mascota.put("raza", mascotaJson.getString("raza"));
            mascota.put("fecha_nacimiento", mascotaJson.getString("fecha_nacimiento"));
            mascota.put("sexo", mascotaJson.optString("sexo", "Desconocido"));

            if (!mascotaJson.isNull("historial")) {
                JSONObject historial = mascotaJson.getJSONObject("historial");
                mascota.put("enfermedades", historial.optString("enfermedades", "N/A"));
                mascota.put("medicinas", historial.optString("medicinas", "N/A"));
                mascota.put("sangre", historial.optString("sangre", "N/A"));
                mascota.put("peso", String.valueOf(historial.optInt("peso", 0)));
                mascota.put("vacunas", historial.optString("vacunas", "N/A"));
                mascota.put("foto", historial.optString("foto", ""));
                mascota.put("cirugias", historial.optString("cirugias", "N/A"));
            }
            listaMascotas.add(mascota);
        }
        mascotaAdapter.notifyDataSetChanged();
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
