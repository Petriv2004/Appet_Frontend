package co.unipiloto.appet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MascotaActivaActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MascotaAdapter mascotaAdapter;
    private List<Map<String,String>> listaMascotas;
    private com.android.volley.RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mascota_activa);

        // Toolbar
        ImageView leftIcon = findViewById(R.id.left_icon);
        leftIcon.setVisibility(View.VISIBLE);
        leftIcon.setOnClickListener(v -> finish());
        TextView title = findViewById(R.id.title);
        title.setText("Mascota Activa y Sedentaria");

        // RecyclerView + Adapter
        recyclerView = findViewById(R.id.recyclerViewEstadisticas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listaMascotas  = new ArrayList<>();
        mascotaAdapter = new MascotaAdapter(listaMascotas);
        recyclerView.setAdapter(mascotaAdapter);

        requestQueue = Volley.newRequestQueue(this);

        //llenarEstadisticas();
    }

    private void llenarEstadisticas() {
        listaMascotas.clear();

        String correo = getSharedPreferences("AppPreferences", MODE_PRIVATE)
                .getString("correo", null);
        if (correo == null) {
            Toast.makeText(this, "No has iniciado sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        // Endpoints que devuelven un solo JSONObject
        String urlActiva    = Url.URL + "/propietarios/mascota-mas-activa/" + correo;
        String urlSedentaria= Url.URL + "/propietarios/mascota-mas-sedentaria/" + correo;

        JsonObjectRequest reqActiva = new JsonObjectRequest(
                Request.Method.GET, urlActiva, null,
                response -> {
                    try {
                        listaMascotas.add(jsonToMap(response));
                        mascotaAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al parsear mascota activa", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "No se pudo obtener la mascota activa", Toast.LENGTH_SHORT).show();
                }
        );

        JsonObjectRequest reqSedentaria = new JsonObjectRequest(
                Request.Method.GET, urlSedentaria, null,
                response -> {
                    try {
                        listaMascotas.add(jsonToMap(response));
                        mascotaAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al parsear mascota sedentaria", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "No se pudo obtener la mascota sedentaria", Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(reqActiva);
        requestQueue.add(reqSedentaria);
    }

    private Map<String,String> jsonToMap(JSONObject j) throws JSONException {
        Map<String,String> m = new HashMap<>();
        m.put("id_mascota",       String.valueOf(j.getInt("id_mascota")));
        m.put("nombre",           j.optString("nombre", "—"));
        m.put("tipo",             j.optString("tipo", "—"));
        m.put("raza",             j.optString("raza", "—"));
        m.put("fecha_nacimiento", j.optString("fecha_nacimiento", "—"));
        m.put("sexo",             j.optString("sexo", "Desconocido"));

        if (!j.isNull("historial")) {
            JSONObject h = j.getJSONObject("historial");
            m.put("peso",        String.valueOf(h.optInt("peso", 0)));
            m.put("vacunas",     h.optString("vacunas", "N/A"));
            m.put("enfermedades",h.optString("enfermedades", "N/A"));
            m.put("medicinas",   h.optString("medicinas", "N/A"));
            m.put("foto",        h.optString("foto", ""));
        }

        return m;
    }
}
