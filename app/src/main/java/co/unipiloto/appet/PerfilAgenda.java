package co.unipiloto.appet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerfilAgenda extends AppCompatActivity implements OnCitaUpdateListener {

    private RecyclerView recyclerView;
    private CitaAdapter citaAdapter;
    private List<Map<String, String>> listaCitas;
    private RequestQueue requestQueue;

    private Button btnAgregarCita;
    private String url;
    private boolean esVeterinario = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_agenda);

        recyclerView = findViewById(R.id.recyclerViewCitas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaCitas = new ArrayList<>();
        citaAdapter = new CitaAdapter(listaCitas, this);
        recyclerView.setAdapter(citaAdapter);
        btnAgregarCita = findViewById(R.id.btnAgregarCita);

        requestQueue = Volley.newRequestQueue(this);

        ImageView leftIcon = findViewById(R.id.left_icon);
        leftIcon.setVisibility(View.VISIBLE);
        TextView title = findViewById(R.id.title);
        title.setText("Agenda");

        leftIcon.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilAgenda.this, MainActivity.class);
            startActivity(intent);
        });

        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correoUsuario = prefs.getString("correo", null);
        String correoVet = prefs.getString("correoVet", null);

        if (correoUsuario != null) {
            btnAgregarCita.setVisibility(View.VISIBLE);
            url = "http://192.168.0.13:8080/propietarios/obtener_propietario/";
            obtenerCitasPropietario(correoUsuario);
        } else if (correoVet != null) {
            btnAgregarCita.setVisibility(View.GONE);
            url = "http://192.168.0.13:8080/agenda/citas-veterinario/";
            esVeterinario = true;
            obtenerCitasPropietario(correoVet);
        } else {
            Toast.makeText(this, "Error: No se encontró el correo del usuario", Toast.LENGTH_SHORT).show();
        }
    }

    private void obtenerCitasPropietario(String correo) {
        StringRequest request = new StringRequest(Request.Method.GET, url + correo,
                response -> {
                    try {
                        listaCitas.clear();
                        if (esVeterinario) {
                            JSONArray citasArray = new JSONArray(response);
                            for (int i = 0; i < citasArray.length(); i++) {
                                JSONObject citaJson = citasArray.getJSONObject(i);
                                Map<String, String> cita = new HashMap<>();
                                cita.put("id_agenda", String.valueOf(citaJson.getInt("id_agenda")));
                                cita.put("nombre_mascota", citaJson.getString("nombre")); // ya viene la mascota
                                cita.put("fecha", citaJson.getString("fecha"));
                                cita.put("hora", citaJson.getString("hora"));
                                cita.put("razon", citaJson.getString("razon"));
                                cita.put("descripcion", citaJson.getString("descripcion"));
                                cita.put("asistencia", String.valueOf(citaJson.getBoolean("asistencia")));
                                listaCitas.add(cita);
                            }
                        } else {
                            JSONObject propietario = new JSONObject(response);
                            JSONArray mascotasArray = propietario.getJSONArray("macotasList");
                            for (int i = 0; i < mascotasArray.length(); i++) {
                                JSONObject mascotaJson = mascotasArray.getJSONObject(i);
                                String nombreMascota = mascotaJson.getString("nombre");

                                JSONArray citasArray = mascotaJson.optJSONArray("citas");
                                if (citasArray != null) {
                                    for (int j = 0; j < citasArray.length(); j++) {
                                        JSONObject citaJson = citasArray.getJSONObject(j);
                                        Map<String, String> cita = new HashMap<>();
                                        cita.put("id_agenda", String.valueOf(citaJson.getInt("id_agenda")));
                                        cita.put("nombre_mascota", nombreMascota);
                                        cita.put("fecha", citaJson.getString("fecha"));
                                        cita.put("hora", citaJson.getString("hora"));
                                        cita.put("razon", citaJson.getString("razon"));
                                        cita.put("descripcion", citaJson.getString("descripcion"));
                                        cita.put("asistencia", String.valueOf(citaJson.getBoolean("asistencia")));
                                        listaCitas.add(cita);
                                    }
                                }
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

    @Override
    public void onMarcarAsistida(String idCita, int position) {
        String urlActualizar = "http://192.168.0.13:8080/agenda/cambiar-asistido/" + idCita;
        StringRequest putRequest = new StringRequest(Request.Method.PUT, urlActualizar,
                response -> {
                    Toast.makeText(PerfilAgenda.this, "Cita marcada como asistida", Toast.LENGTH_SHORT).show();
                    listaCitas.get(position).put("asistencia", "true");
                    citaAdapter.notifyItemChanged(position);
                },
                error -> {
                    Toast.makeText(PerfilAgenda.this, "Error al actualizar la cita", Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(putRequest);
    }

    public void onClickIrGestionAgenda(View view) {
        Intent intent = new Intent(PerfilAgenda.this, RegistrarAgenda.class);
        startActivity(intent);
    }
}
