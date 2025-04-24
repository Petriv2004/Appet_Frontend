package co.unipiloto.appet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HacerRecorridoActivity extends AppCompatActivity {

    private Spinner spinnerMascotas;
    private RequestQueue requestQueue;
    private TextView textViewPosicion;
    private ArrayList<Recorrido> recorrido;
    private boolean recorriendo;
    private static final int UPDATE_INTERVAL = 5000;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;
    private FusedLocationProviderClient fusedLocationClient;
    private final int REQUEST_CODE_LOCATION = 100;
    JSONObject jsonObject = new JSONObject();
    private boolean hayDatos = false;
    private Button buttonReporte;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hacer_recorrido);

        spinnerMascotas = findViewById(R.id.spinnerMascotas);
        textViewPosicion = findViewById(R.id.textView);
        buttonReporte = findViewById(R.id.buttonReporte);
        requestQueue = Volley.newRequestQueue(this);
        recorrido = new ArrayList<>();
        recorriendo = false;

        ImageView gifImageView = findViewById(R.id.gifImageView);
        Glide.with(this)
                .asGif()
                .load("https://cdn.pixabay.com/animation/2023/04/06/16/10/16-10-43-442_512.gif")
                .into(gifImageView);

        ImageView leftIcon = findViewById(R.id.left_icon);
        leftIcon.setVisibility(View.VISIBLE);
        TextView title = findViewById(R.id.title);
        title.setText("Registro de recorrido");

        leftIcon.setOnClickListener(v -> {
            Intent intent = new Intent(HacerRecorridoActivity.this, MainActivity.class);
            startActivity(intent);
        });

        llenarSpinner();

        spinnerMascotas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                recorrido.clear();
                recorriendo = false;
                textViewPosicion.setText("Presione empezar recorrido para empezar a contar");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(HacerRecorridoActivity.this, "Seleccione una mascota", Toast.LENGTH_SHORT).show();
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correo = prefs.getString("correo", null);
        String correoCui = prefs.getString("correoCui", null);
        if (correo == null && correoCui == null) {
            Toast.makeText(this, "No se encontró el correo del usuario", Toast.LENGTH_SHORT).show();
            return;
        }
        if (correoCui != null) {
            buttonReporte.setVisibility(View.VISIBLE);
        } else {
            buttonReporte.setVisibility(View.GONE);
        }
    }

    public void onClickEmpezarMedicion(View view) {
        recorrido.clear();
        recorriendo = true;
        textViewPosicion.setText("Midiendo...");
        hacerFetch();
    }

    private void hacerFetch() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION
            );
        } else {
            obtenerUbicacionYEnviar();
        }
    }

    private void obtenerUbicacionYEnviar() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String fechaActual = sdf.format(new Date());

                String latitud = String.format(Locale.getDefault(), "%.8f", location.getLatitude());
                String longitud = String.format(Locale.getDefault(), "%.8f", location.getLongitude());

                recorrido.add(new Recorrido(latitud, longitud, "Permitido", fechaActual));

                try {
                    jsonObject = new JSONObject();
                    jsonObject.put("latitud", latitud);
                    jsonObject.put("longitud", longitud);
                    jsonObject.put("rango", "Permitido");
                    jsonObject.put("fecha", fechaActual);
                    hayDatos = true;

                    textViewPosicion.setText("Midiendo ... Latitud:" + latitud + "\nLongitud:" + longitud);

                    enviarRecorrido();
                } catch (JSONException e) {
                    Toast.makeText(HacerRecorridoActivity.this, "Error al crear JSON", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(HacerRecorridoActivity.this, "Ubicación no disponible", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void enviarRecorrido() {
        String[] mascotaData = spinnerMascotas.getSelectedItem().toString().split("-");
        int idMascota = Integer.parseInt(mascotaData[0]);
        String URL = Url.URL+"/mascotas/registrarRecorrido/" + idMascota;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                jsonObject,
                response -> Toast.makeText(this, "Recorrido enviado correctamente", Toast.LENGTH_SHORT).show(),
                error -> {
                    Log.e("Volley", "Error al enviar el recorrido", error);
                    Toast.makeText(this, "Error al enviar el recorrido", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
        updateRunnable = this::hacerFetch;
        handler.postDelayed(updateRunnable, UPDATE_INTERVAL);
    }

    public void onClickPararMedicion(View view) {
        if (updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
        }
    }

    public void onClickReporte(View view) {
        if (recorriendo) {
            new AlertDialog.Builder(this)
                    .setTitle("Confirmación")
                    .setMessage("¿Está seguro de enviarle el reporte al dueño de la mascota?")
                    .setPositiveButton("Sí", (dialog, which) -> enviarCorreo())
                    .setNegativeButton("Cancelar", null)
                    .show();
        } else {
            Toast.makeText(this, "No se ha iniciado el recorrido", Toast.LENGTH_SHORT).show();
        }
    }

    private void enviarCorreo() {
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correoCui = prefs.getString("correoCui", "");

        String cuerpo = "Este es el recorrido que se hizo: " + generarMapa();
        String[] mascotaData = spinnerMascotas.getSelectedItem().toString().split("-");
        int idMascota = Integer.parseInt(mascotaData[0]);

        String url = Url.URL+"/email/mensaje-recorrido/" + idMascota + "/" + correoCui;

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("asunto", "Recorrido de " + mascotaData[1]);
            jsonBody.put("mensaje", cuerpo);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> {
                        Toast.makeText(this, "Correo enviado exitosamente", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    },
                    error -> Toast.makeText(this, "Error al enviar el correo", Toast.LENGTH_SHORT).show()
            ) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };

            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String generarMapa() {
        StringBuilder salida = new StringBuilder("http://www.google.com/maps/dir/");
        for (Recorrido rec : recorrido) {
            salida.append(rec.getLatitud()).append(",").append(rec.getLongitud()).append("/");
        }
        return salida.toString();
    }

    private void llenarSpinner() {
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correo = prefs.getString("correo", null);
        String correoCui = prefs.getString("correoCui", null);

        if (correo == null && correoCui == null) {
            Toast.makeText(this, "No se encontró el correo del usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = correoCui != null
                ? Url.URL+"/propietarios/mascotas-veterinario/" + correoCui
                : Url.URL+"/propietarios/obtener_propietario/" + correo;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        List<String> mascotas = new ArrayList<>();
                        JSONArray mascotasList;

                        if (correoCui != null) {
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