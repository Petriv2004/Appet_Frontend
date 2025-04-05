package co.unipiloto.appet;

import androidx.fragment.app.FragmentActivity;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import co.unipiloto.appet.databinding.ActivityRecorridoFechaBinding;

public class UbicacionActualActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Spinner spinner;
    private static final int UPDATE_INTERVAL = 3000;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Polyline polyLine;
    private Marker mascotaMarker;
    private ArrayList<LatLng> lista = new ArrayList<>();
    private RequestQueue requestQueue;
    private Button button;
    private String URL = "http://192.168.0.13:8080/mascotas/obtenerUbicacion/";
    private Runnable updateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubicacion_actual);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        spinner = findViewById(R.id.spinner);
        button = findViewById(R.id.btn);
        button.setOnClickListener(v -> iniciarSeguimiento());

        requestQueue = Volley.newRequestQueue(this);
        llenarSpinner();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                detenerSeguimiento();
                mMap.clear();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(UbicacionActualActivity.this, "Seleccione una mascota", Toast.LENGTH_SHORT).show();
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

    private void iniciarSeguimiento() {
        detenerSeguimiento();
        onClickRecorrido();
    }

    private void detenerSeguimiento() {
        if (updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
        }
    }

    private void onClickRecorrido() {
        String url = URL + spinner.getSelectedItem().toString().split("-")[0];

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        double latitud = response.getDouble("latitud");
                        double longitud = response.getDouble("longitud");
                        LatLng latLng = new LatLng(latitud, longitud);

                        if (!lista.isEmpty() && lista.get(lista.size() - 1).equals(latLng)) {
                            return;
                        }

                        lista.add(latLng);
                        if (lista.size() > 50) {
                            lista.remove(0);
                        }

                        if (mascotaMarker == null) {
                            mascotaMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Ubicación de la mascota"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
                        } else {
                            mascotaMarker.setPosition(latLng);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
                        }

                        if (polyLine != null) {
                            polyLine.remove();
                        }
                        polyLine = mMap.addPolyline(new PolylineOptions().addAll(lista).width(10).color(Color.BLUE));

                        updateRunnable = this::onClickRecorrido;
                        handler.postDelayed(updateRunnable, UPDATE_INTERVAL);

                    } catch (JSONException e) {
                        Toast.makeText(this, "Error al procesar JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error al obtener la ubicación", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(request);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}