package co.unipiloto.appet;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EstadisticasMascotaActivity extends AppCompatActivity {

    private Spinner spinnerMascotas;

    private EditText etFecha;

    private TextView textVisitas, textRecorridos, textRitmoAlto, textRitmoBajo, textFechaRitmo, textRecorridosMes;

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_estadisticas_mascota);

        spinnerMascotas = findViewById(R.id.spinnerMascotas);
        etFecha = findViewById(R.id.etFecha);
        textVisitas = findViewById(R.id.textVisitas);
        textRecorridos = findViewById(R.id.textRecorridos);
        textRitmoAlto = findViewById(R.id.textRitmoAlto);
        textRitmoBajo = findViewById(R.id.textRitmoBajo);
        textFechaRitmo = findViewById(R.id.textFechaRitmo);
        textRecorridosMes = findViewById(R.id.textRecorridosMes);

        ImageView leftIcon = findViewById(R.id.left_icon);
        leftIcon.setVisibility(View.VISIBLE);
        TextView title = findViewById(R.id.title);
        title.setText("Estadísticas por mascota");
        requestQueue = Volley.newRequestQueue(this);

        etFecha.setOnClickListener(v -> mostrarDatePicker());

        leftIcon.setOnClickListener(v -> {
            Intent intent = new Intent(EstadisticasMascotaActivity.this, MainActivity.class);
            startActivity(intent);
        });

        llenarSpinner();
    }

    private void llenarSpinner() {
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correo = prefs.getString("correo", null);

        if (correo == null ) {
            Toast.makeText(this, "No se encontró el correo del usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = Url.URL+"/propietarios/obtener_propietario/" + correo;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        List<String> mascotas = new ArrayList<>();
                        JSONArray mascotasList;

                        JSONObject propietario = new JSONObject(response);
                        mascotasList = propietario.getJSONArray("macotasList");


                        for (int i = 0; i < mascotasList.length(); i++) {
                            JSONObject mascota = mascotasList.getJSONObject(i);
                            int idMascota = mascota.getInt("id_mascota");
                            String nombreMascota = mascota.getString("nombre");
                            mascotas.add(idMascota + "-" + nombreMascota);
                        }

                        ArrayAdapter<String> adapterMascotas = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_dropdown_item, mascotas);
                        adapterMascotas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerMascotas.setAdapter(adapterMascotas);

                    } catch (JSONException e) {
                        Toast.makeText(this, "Error al procesar JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error al obtener datos", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(request);
    }
    private void mostrarDatePicker() {
        Calendar calendario = Calendar.getInstance();
        int año = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String fechaSeleccionada = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            etFecha.setText(fechaSeleccionada);
        }, año, mes, dia);

        datePickerDialog.getDatePicker().setMaxDate(calendario.getTimeInMillis());

        datePickerDialog.show();
    }
    public void onClickStats(View view){
    }

}