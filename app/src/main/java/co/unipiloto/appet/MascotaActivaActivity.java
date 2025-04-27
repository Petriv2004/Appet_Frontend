package co.unipiloto.appet;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MascotaActivaActivity extends AppCompatActivity {

    private EditText etMonthYear;
    private Button btnCargar;
    private RecyclerView recyclerView;
    private MascotaAdapter mascotaAdapter;
    private List<Map<String,String>> listaMascotas;
    private com.android.volley.RequestQueue requestQueue;

    private TextView textActivaLabel, textSedentariaLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mascota_activa);

        ImageView leftIcon = findViewById(R.id.left_icon);
        leftIcon.setVisibility(View.VISIBLE);
        leftIcon.setOnClickListener(v -> finish());
        TextView title = findViewById(R.id.title);
        title.setText("Mascota Activa y Sedentaria");

        etMonthYear   = findViewById(R.id.etMonthYear);
        btnCargar     = findViewById(R.id.btnCargar);
        recyclerView  = findViewById(R.id.recyclerViewEstadisticas);
        textActivaLabel     = findViewById(R.id.textActivaLabel);
        textSedentariaLabel = findViewById(R.id.textSedentariaLabel);

        listaMascotas  = new ArrayList<>();
        mascotaAdapter = new MascotaAdapter(listaMascotas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mascotaAdapter);

        requestQueue = Volley.newRequestQueue(this);

        etMonthYear.setOnClickListener(v -> mostrarMonthYearPicker());

        btnCargar.setOnClickListener(v -> cargarEstadisticas());
    }

    private void mostrarMonthYearPicker() {
        View dlgView = getLayoutInflater().inflate(R.layout.dialog_month_year, null);
        Spinner spMes  = dlgView.findViewById(R.id.spinnerMonth);
        Spinner spAnio = dlgView.findViewById(R.id.spinnerYear);

        // Meses completos (01–12), los usaremos como "backup"
        String[] mesesCompletos = new String[12];
        for (int i = 0; i < 12; i++) {
            mesesCompletos[i] = String.format("%02d", i + 1);
        }

        // Año actual
        int anioActual = Calendar.getInstance().get(Calendar.YEAR);
        int mesActual  = Calendar.getInstance().get(Calendar.MONTH) + 1;

        List<String> listaAnios = new ArrayList<>();
        for (int a = 2000; a <= anioActual; a++) {
            listaAnios.add(String.valueOf(a));
        }
        ArrayAdapter<String> adAnio = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, listaAnios);
        adAnio.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAnio.setAdapter(adAnio);
        spAnio.setSelection(listaAnios.indexOf(String.valueOf(anioActual)));

        Runnable actualizarMeses = () -> {
            int añoSel = Integer.parseInt(spAnio.getSelectedItem().toString());
            List<String> mesesPermitidos = new ArrayList<>();
            int limite = (añoSel == anioActual) ? mesActual : 12;
            for (int m = 1; m <= limite; m++) {
                mesesPermitidos.add(String.format("%02d", m));
            }
            ArrayAdapter<String> adMes = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, mesesPermitidos);
            adMes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spMes.setAdapter(adMes);
        };

        spAnio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                actualizarMeses.run();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        actualizarMeses.run();

        new AlertDialog.Builder(this)
                .setTitle("Selecciona Mes y Año")
                .setView(dlgView)
                .setPositiveButton("OK", (dlg, which) -> {
                    String mes  = spMes.getSelectedItem().toString();
                    String anio = spAnio.getSelectedItem().toString();
                    etMonthYear.setText(mes + "-" + anio);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


    private void cargarEstadisticas() {
        String mm_yyyy = etMonthYear.getText().toString();
        String[] part = mm_yyyy.split("-");
        if(part[0].charAt(0) == '0'){
            mm_yyyy = part[0].charAt(1) + "/"+ part[1] ;
        }else{
            mm_yyyy = part[0] + "/"+ part[1] ;
        }

        if (mm_yyyy.isEmpty()) {
            Toast.makeText(this, "Seleccione mes y año", Toast.LENGTH_SHORT).show();
            return;
        }

        listaMascotas.clear();
        mascotaAdapter.notifyDataSetChanged();

        String correo = getSharedPreferences("AppPreferences", MODE_PRIVATE)
                .getString("correo", null);
        if (correo == null) {
            Toast.makeText(this, "No has iniciado sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        String urlActiva = Url.URL + "/estadisticas/mascota-activa/" + correo + "/" + mm_yyyy;
        JsonObjectRequest reqAct = new JsonObjectRequest(
                Request.Method.GET, urlActiva, null,
                resp -> {
                    try {
                        listaMascotas.add(jsonToMap(resp));
                        mascotaAdapter.notifyDataSetChanged();

                        String nombreActiva = resp.optString("nombre", "—");
                        textActivaLabel.setText("Mascota más activa: " + nombreActiva);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parseando activa", Toast.LENGTH_SHORT).show();
                    }
                },
                err -> Toast.makeText(this, "Error al obtener mascota activa", Toast.LENGTH_SHORT).show()
        );
        requestQueue.add(reqAct);

        String urlSed = Url.URL + "/estadisticas/mascota-sedentaria/" + correo + "/" + mm_yyyy;
        JsonObjectRequest reqSed = new JsonObjectRequest(
                Request.Method.GET, urlSed, null,
                resp -> {
                    try {
                        listaMascotas.add(jsonToMap(resp));
                        mascotaAdapter.notifyDataSetChanged();

                        String nombreSed = resp.optString("nombre", "—");
                        textSedentariaLabel.setText("Mascota más sedentaria: " + nombreSed);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parseando sedentaria", Toast.LENGTH_SHORT).show();
                    }
                },
                err -> Toast.makeText(this, "Error al obtener mascota sedentaria", Toast.LENGTH_SHORT).show()
        );
        requestQueue.add(reqSed);
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
