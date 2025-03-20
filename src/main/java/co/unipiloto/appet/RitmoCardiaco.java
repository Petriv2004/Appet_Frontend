package co.unipiloto.appet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RitmoCardiaco extends AppCompatActivity {

    private Spinner spinnerMascotas;
    private RequestQueue requestQueue;

    private Handler handler;

    private Runnable runnable;

    private final int UPDATE_INTERVAL = 5000;

    private TextView textViewRitmo;

    private TextView textViewEstado;

    private ArrayList<String> mediciones;

    private Button buttonEmpezarMedicion;

    private int estado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ritmo);

        spinnerMascotas = findViewById(R.id.spinnerMascotas);
        textViewRitmo = findViewById(R.id.textView);
        textViewEstado = findViewById(R.id.textViewEstado);
        buttonEmpezarMedicion = findViewById(R.id.buttonEmpezarMedicion);
        textViewEstado.setTextColor(Color.GRAY);
        requestQueue = Volley.newRequestQueue(this);
        handler = new Handler();
        mediciones = new ArrayList<>();

        llenarSpinner();

        spinnerMascotas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                handler.removeCallbacks(runnable);
                textViewRitmo.setText("Presione empezar medición para conocer el ritmo cardiaco de su mascota");
                estado = new Random().nextInt(2) + 1;
                mediciones.clear();
                textViewEstado.setText("No hay datos");
                textViewEstado.setTextColor(Color.GRAY);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(RitmoCardiaco.this, "Seleccione una mascota", Toast.LENGTH_SHORT).show();
            }
        });

        ImageView gifImageView = findViewById(R.id.gifImageView);

        Glide.with(this)
                .asGif()
                .load("https://images.emojiterra.com/google/noto-emoji/animated-emoji/1fac0.gif")
                .into(gifImageView);
    }
    public void onClickEmpezarMedicion(View view) {
        buttonEmpezarMedicion.setEnabled(false);
        if (spinnerMascotas.getSelectedItem() == null) {
            Toast.makeText(this, "Seleccione una mascota", Toast.LENGTH_SHORT).show();
            return;
        }

        runnable = new Runnable() {
            @Override
            public void run() {
                    String[] mascotaData = spinnerMascotas.getSelectedItem().toString().split("-");
                    int idMascota = Integer.parseInt(mascotaData[0]);
                    hacerPeticion(idMascota, estado);
                    handler.postDelayed(this, UPDATE_INTERVAL);
            }
        };
        handler.post(runnable);
    }
    private void hacerPeticion(int idMascota, int estado) {
        String url = "http://192.168.0.13:8080/mascotas/obtenerRitmo/" + idMascota + "/" + estado;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try{
                        String tipo = response.getString("tipo");
                        String tamanio = response.getString("tamanio");
                        int ritmoCardiaco = response.getInt("ritmoCardiaco");
                        mediciones.add(ritmoCardiaco + "bpm");
                        if (tipo.equals("Felino")) {
                            textViewRitmo.setText("El Ritmo Cardiaco de su mascota es: " + verificarRitmo(tipo, ritmoCardiaco));

                        }else if(tamanio.equals("Grande") ){
                            textViewRitmo.setText("El Ritmo Cardiaco de su mascota es: " + verificarRitmo(tamanio, ritmoCardiaco));
                        }else if(tamanio.equals("Mediano")){
                            textViewRitmo.setText("El Ritmo Cardiaco de su mascota es: " + verificarRitmo(tamanio, ritmoCardiaco));
                        }else{
                            textViewRitmo.setText("El Ritmo Cardiaco de su mascota es: " + verificarRitmo(tamanio, ritmoCardiaco));
                        }

                    }catch(JSONException e){
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Error en la conexión", Toast.LENGTH_SHORT).show()
        );
        request.setTag("RitmoCardiaco");
        requestQueue.add(request);

}
    private String verificarRitmo(String tamanio, int ritmoCardiaco){
        if (tamanio.equals("Grande")) {
            if (ritmoCardiaco >= 60 && ritmoCardiaco <= 100) {
                textViewEstado.setText("Se mascota se encuentra BIEN");
                textViewEstado.setTextColor(Color.GREEN);
                return ritmoCardiaco + "bpm";
            }else{
                textViewEstado.setText("Se mascota se encuentra MAL");
                textViewEstado.setTextColor(Color.RED);
                return ritmoCardiaco + "bpm";
            }
        }else if(tamanio.equals("Mediano")){
            if (ritmoCardiaco >= 80 && ritmoCardiaco <= 120) {
                textViewEstado.setText("Se mascota se encuentra BIEN");
                textViewEstado.setTextColor(Color.GREEN);
                return ritmoCardiaco + "bpm";
            }else{
                textViewEstado.setText("Se mascota se encuentra MAL");
                textViewEstado.setTextColor(Color.RED);
                return ritmoCardiaco + "bpm";
            }
        }else if(tamanio.equals("Pequeño")){
            if (ritmoCardiaco >= 100 && ritmoCardiaco <= 160) {
                textViewEstado.setText("Se mascota se encuentra BIEN");
                textViewEstado.setTextColor(Color.GREEN);
                return ritmoCardiaco + "bpm";
            }else{
                textViewEstado.setText("Se mascota se encuentra MAL");
                textViewEstado.setTextColor(Color.RED);
                return ritmoCardiaco + "bpm";
            }
        }else{
            if (ritmoCardiaco >= 140 && ritmoCardiaco <= 220) {
                textViewEstado.setText("Se mascota se encuentra BIEN");
                textViewEstado.setTextColor(Color.GREEN);
                return ritmoCardiaco + "bpm";
            }else{
                textViewEstado.setText("Se mascota se encuentra MAL");
                textViewEstado.setTextColor(Color.RED);
                return ritmoCardiaco + "bpm";
            }
        }
    }
    public void onClickPararMedicion(View view) {
        buttonEmpezarMedicion.setEnabled(true);
        handler.removeCallbacks(runnable);
        textViewEstado.setText("Se ha parado la medición");
        textViewEstado.setTextColor(Color.GRAY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        if (requestQueue != null) {
            requestQueue.cancelAll("RitmoCardiaco");
        }
        mediciones.clear();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
        if (requestQueue != null) {
            requestQueue.cancelAll("RitmoCardiaco");
        }
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

    public void onClickReporte(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Está seguro de enviarle el reporte al dueño de la mascota?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    enviarCorreo();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void enviarCorreo() {
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correoVet = prefs.getString("correoVet", null);

        String cuerpo = "Estas son las mediciones: ";
        for (int i = 0; i < mediciones.size(); i++) {
            if (i == mediciones.size() - 1) {
                cuerpo += mediciones.get(i) + ".";
            }else{
                cuerpo += mediciones.get(i) + ", ";
            }
        }
        if (estado == 1) {
            cuerpo += "\nSu mascota se encuentra BIEN";
        }else{
            cuerpo += "\nSu mascota se encuentra MAL, por favor comuniquese lo más pronto posible.";
        }

        String[] mascotaData = spinnerMascotas.getSelectedItem().toString().split("-");
        int idMascota = Integer.parseInt(mascotaData[0]);

        String url = "http://192.168.0.13:8080/email/mensaje-cardiaco/" + idMascota + "/" +correoVet;

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("asunto", "Mediciones del ritmo cardiaco de " + mascotaData[1]);
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
}
