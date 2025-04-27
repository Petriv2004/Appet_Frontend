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
import com.android.volley.toolbox.JsonObjectRequest;
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

    private TextView textVisitas, textRitmoAlto, textRitmoBajo, textFechaRitmo, textRecorridosMes, textKilometros;

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_estadisticas_mascota);

        spinnerMascotas = findViewById(R.id.spinnerMascotas);
        etFecha = findViewById(R.id.etFecha);
        textVisitas = findViewById(R.id.textVisitas);
        textRitmoAlto = findViewById(R.id.textRitmoAlto);
        textRitmoBajo = findViewById(R.id.textRitmoBajo);
        textFechaRitmo = findViewById(R.id.textFechaRitmo);
        textRecorridosMes = findViewById(R.id.textRecorridosMes);
        textKilometros = findViewById(R.id.textKilometros);

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

        reiniciarTexts();

        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correo = prefs.getString("correo", null);

        if (correo == null) {
            Toast.makeText(this, "No se encontró el correo del propietario", Toast.LENGTH_SHORT).show();
            return;
        }

        String mascota = spinnerMascotas.getSelectedItem().toString();
        String[] partes = mascota.split("-");
        int idMascota = Integer.parseInt(partes[0]);

        String url = Url.URL + "/estadisticas/visitas-veterinario/" + idMascota;

        JsonObjectRequest statsRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        int visitasVet = response.optInt("Visitas", -1);
                        textVisitas.setText("Visitas al veterinario: " + visitasVet);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar estadísticas", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Error al obtener estadísticas", Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(statsRequest);

        url = Url.URL + "/estadisticas/ritmoCardiacoMaximo/" + idMascota;

        statsRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        int visitasVet = response.optInt("RitmoCardiacoMaximo", -1);
                        textRitmoAlto.setText("Ritmo cardiaco más alto alcanzado: " + visitasVet);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar estadísticas", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "No hay mediciones de ritmo cardiaco", Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(statsRequest);

        url = Url.URL + "/estadisticas/ritmoCardiacoMinimo/" + idMascota;

        statsRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        int visitasVet = response.optInt("RitmoCardiacoMinimo", -1);
                        textRitmoBajo.setText("Ritmo cardiaco más bajo alcanzado: " + visitasVet);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar estadísticas", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Error al obtener estadísticas", Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(statsRequest);
    }
    public void onClickRitmo(View view){
        reiniciarTexts();

        String mascota = spinnerMascotas.getSelectedItem().toString();
        String[] partes = mascota.split("-");
        int idMascota = Integer.parseInt(partes[0]);

        String fecha = etFecha.getText().toString();

        String url = Url.URL + "/estadisticas/promedio-ritmoCardiaco/" + idMascota + "/" + fecha;

        JsonObjectRequest statsRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        double visitasVet = response.optDouble("Promedio", -1);
                        textFechaRitmo.setText("Promedio del ritmo cardiaco por fecha: " + visitasVet);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar estadísticas", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    textFechaRitmo.setText("Promedio del ritmo cardiaco por fecha: 0");
                    Toast.makeText(this, "Error al obtener promedio de ritmo cardiaco", Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(statsRequest);

        url = Url.URL + "/estadisticas/recorridoKm/" + idMascota + "/" + fecha;

        statsRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        double visitasVet = response.optDouble("kilometros", -1);
                        textKilometros.setText("Kilometros por fecha: " + visitasVet);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar estadísticas", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    textKilometros.setText("Kilometros por fecha: 0");
                    Toast.makeText(this, "Error al obtener kilometros por fecha", Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(statsRequest);

        String[] part = fecha.split("-");
        if(part[1].charAt(0) == '0'){
            fecha = part[1].charAt(1) + "/"+ part[0] ;
        }else{
            fecha = part[1] + "/"+ part[0] ;
        }

        url = Url.URL + "/estadisticas/total-recorridos/" + idMascota + "/" + fecha;

        statsRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        int visitasVet = response.optInt("Total", -1);
                        textRecorridosMes.setText("Recorridos realizados en el mes: " + visitasVet);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar estadísticas", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    textRecorridosMes.setText("Recorridos realizados en el mes: 0");
                    Toast.makeText(this, "Error al obtener recorridos por mes", Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(statsRequest);

    }

    private void reiniciarTexts(){
        textVisitas.setText("Visitas al Veterinario:");
        textRitmoAlto.setText("Ritmo cardiaco más alto alcanzado:");
        textRitmoBajo.setText("Ritmo cardiaco más bajo alcanzado:");
        textFechaRitmo.setText("Promedio del ritmo cardiaco por fecha:");
        textRecorridosMes.setText("Recorridos realizados en el mes:");
        textKilometros.setText("Kilometros por fecha:");
    }

}