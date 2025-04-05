package co.unipiloto.appet;


import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


public class RitmoCardiacoFecha extends AppCompatActivity {

    private LineChart lineChart;

    private Spinner spinnerMascotas;

    private List<String> horas = new ArrayList<>();

    private List<String> bpm = new ArrayList<>();

    private RequestQueue requestQueue;

    private ArrayList<Ritmo> ritmos;

    private EditText etFecha;

    private String URL= "http://192.168.0.13:8080/mascotas/ritmos/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ritmo_cardiaco_fecha);

        lineChart = findViewById(R.id.chart);
        spinnerMascotas = findViewById(R.id.spinner);
        requestQueue = Volley.newRequestQueue(this);
        etFecha = findViewById(R.id.etFecha);
        ritmos = new ArrayList<>();
        etFecha.setOnClickListener(v -> showDatePicker());
        llenarSpinner();

    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            String fechaSeleccionada = year1 + "-" + String.format("%02d", (month1 + 1)) + "-" + String.format("%02d", dayOfMonth);
            etFecha.setText(fechaSeleccionada);
        }, year, month, day);

        Calendar minCalendar = Calendar.getInstance();
        minCalendar.set(year - 40, month, day);

        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePickerDialog.getDatePicker().setMinDate(minCalendar.getTimeInMillis());

        datePickerDialog.show();
    }
    private void actualizarGrafica(String nombre, String fecha) {
        if (ritmos.isEmpty()) {
            Toast.makeText(this, "No hay datos para graficar", Toast.LENGTH_SHORT).show();
            return;
        }

        horas.clear();
        bpm.clear();

        for (Ritmo ritmo : ritmos) {
            horas.add(ritmo.getHora());
            bpm.add(String.valueOf(ritmo.getRitmoCardiaco()));
        }

        Description description = new Description();
        description.setText("Ritmo cardiaco promedio " + promedio() + " bpm");
        lineChart.setDescription(description);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(horas));
        xAxis.setLabelCount(horas.size());
        xAxis.setGranularity(1f);

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);

        List<Entry> entriest1 = new ArrayList<>();
        for (int i = 0; i < ritmos.size(); i++) {
            entriest1.add(new Entry(i, ritmos.get(i).getRitmoCardiaco()));
        }

        LineDataSet dataSet1 = new LineDataSet(entriest1, "Ritmo Cardiaco de " + nombre + " el " + fecha);
        dataSet1.setColor(R.color.lightPurple);
        dataSet1.setValueTextColor(R.color.purple);
        dataSet1.setCircleColor(R.color.purple);
        dataSet1.setLineWidth(2f);

        LineData lineData = new LineData(dataSet1);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    private double promedio(){
        double promedio = 0;
        for (Ritmo ritmo : ritmos) {
            promedio += ritmo.getRitmoCardiaco();
        }
        return promedio / ritmos.size();
    }

    private void request(int id, String nombre, String date){
        String url = URL + id +  "/"+ date;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        ritmos.clear();
                        horas.clear();
                        bpm.clear();

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject posicionJson = response.getJSONObject(i);
                            int ritmoCardiaco = posicionJson.getInt("ritmoCardiaco");
                            String fecha = posicionJson.getString("fecha");
                            String hora = posicionJson.getString("hora");
                            ritmos.add(new Ritmo(ritmoCardiaco, fecha, hora));
                        }

                        actualizarGrafica(nombre, date);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Error en el response", Toast.LENGTH_SHORT).show();
                    Log.e("Error", error.toString());
                }
        );
        requestQueue.add(request);
    }

    public void onClickRitmo(View view) {
        String[] mascotaData = spinnerMascotas.getSelectedItem().toString().split("-");
        int idMascota = Integer.parseInt(mascotaData[0]);
        String fecha = etFecha.getText().toString();
        String nombre = mascotaData[1];

        if (fecha.isEmpty()){
            Toast.makeText(this, "Debes seleccionar una fecha", Toast.LENGTH_SHORT).show();
            return;
        }
        request(idMascota, nombre, fecha);
    }


    private void llenarSpinner() {
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correo = prefs.getString("correo", null);
        String correoVet = prefs.getString("correoVet", null);

        if (correo == null && correoVet == null) {
            Toast.makeText(this, "No se encontrÃ³ el correo del usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        String url;
        boolean esVeterinario = correoVet != null;

        if (esVeterinario) {
            url = "http://192.168.0.13:8080/propietarios/mascotas-veterinario/" + correoVet;
        } else {
            url = "http://192.168.0.13:8080/propietarios/obtener_propietario/" + correo;
        }

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        List<String> mascotas = new ArrayList<>();
                        JSONArray mascotasList;

                        if (esVeterinario) {
                            mascotasList = new JSONArray(response);
                        } else {
                            JSONObject propietario = new JSONObject(response);
                            mascotasList = propietario.getJSONArray("macotasList");
                        }

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

}