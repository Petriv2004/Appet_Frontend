package co.unipiloto.appet;

import androidx.fragment.app.FragmentActivity;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import co.unipiloto.appet.databinding.ActivityRecorridoFechaBinding;

public class RecorridoFechaActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Spinner spinner;
    private ActivityRecorridoFechaBinding binding;
    private RequestQueue requestQueue;

    private Button button;
    private EditText etFecha;

    private String URL = "http://192.168.0.13:8080/mascotas/recorridoPorFecha/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRecorridoFechaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        etFecha = findViewById(R.id.fechaHint);
        spinner = findViewById(R.id.spinner);
        button = findViewById(R.id.btn);
        button.setOnClickListener(v -> onClickRecorrido());
        requestQueue = Volley.newRequestQueue(this);

        llenarSpinner();

        etFecha.setOnClickListener(v -> mostrarDatePicker());

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mMap.clear();
                etFecha.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(RecorridoFechaActivity.this, "Seleccione una mascota", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void llenarSpinner() {
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correo = prefs.getString("correo", null);
        String correoCui = prefs.getString("correoCui", null);

        if (correo == null && correoCui == null) {
            Toast.makeText(this, "No se encontrÃ³ el correo del usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        String url;
        boolean esCuidador = correoCui != null;

        if (esCuidador) {
            url = "http://192.168.0.13:8080/propietarios/mascotas-veterinario/" + correoCui;
        } else {
            url = "http://192.168.0.13:8080/propietarios/obtener_propietario/" + correo;
        }

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        List<String> mascotas = new ArrayList<>();
                        JSONArray mascotasList;

                        if (esCuidador) {
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
                        spinner.setAdapter(adapterMascotas);

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

    private void onClickRecorrido() {
        String fecha = etFecha.getText().toString();
        String[] mascotaData = spinner.getSelectedItem().toString().split("-");
        int idMascota = Integer.parseInt(mascotaData[0]);

        String urlGet = URL+ idMascota + "/" + fecha;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, urlGet, null,
                response -> {
                    try {
                        procesarRecorrido(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "No hay Recorridos en la fecha seleccionada", Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(request);
    }

    private void procesarRecorrido(JSONArray recorridoArray) throws JSONException {
        ArrayList<LatLng> array = new ArrayList<>();
        for (int i = 0; i < recorridoArray.length(); i++) {
            JSONObject posicionJson = recorridoArray.getJSONObject(i);
            double latitud = Double.parseDouble(posicionJson.getString("latitud"));
            double longitud = Double.parseDouble(posicionJson.getString("longitud"));
            array.add(new LatLng(latitud, longitud));
        }
        if (!array.isEmpty()){
            hacerRecorrido(array);
        }else{
            Toast.makeText(this, "No se encontraron recorridos en la fecha seleccionada", Toast.LENGTH_SHORT).show();
        }
    }

    private void hacerRecorrido(ArrayList<LatLng> array){
        if (mMap == null){
            return;
        }else{
            mMap.clear();
            PolylineOptions polylineOptions = new PolylineOptions().addAll(array).width(8).color(Color.BLUE);
            mMap.addPolyline(polylineOptions);
            mMap.addMarker(new MarkerOptions().position(array.get(0)).title("Inicio"));
            mMap.addMarker(new MarkerOptions().position(array.get(array.size() - 1)).title("Fin"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(array.get(0), 15));
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}