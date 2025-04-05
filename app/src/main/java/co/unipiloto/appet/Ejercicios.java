package co.unipiloto.appet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicios extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EjerciciosAdapter ejercicioAdapter;
    private List<Map<String, String>> listaEjercicios;
    private RadioGroup radioEspecie;
    private RequestQueue requestQueue;

    private Button btnAgregarEjercicio, btnEliminarEjercicio, btnActualizarEjercicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ejercicios);

        recyclerView = findViewById(R.id.recyclerViewEjercicios);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaEjercicios = new ArrayList<>();
        ejercicioAdapter = new EjerciciosAdapter(listaEjercicios);
        recyclerView.setAdapter(ejercicioAdapter);
        radioEspecie = findViewById(R.id.rgEspecie);
        btnAgregarEjercicio = findViewById(R.id.agregarEjercicio);
        btnEliminarEjercicio = findViewById(R.id.eliminarEjercicio);
        btnActualizarEjercicio = findViewById(R.id.actualizarEjercicio);

        requestQueue = Volley.newRequestQueue(this);

        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correoUsuario = prefs.getString("correo", null);
        String correoCui = prefs.getString("correoCui", null);

        if (correoUsuario != null ) {
            obtenerEjerciciosUsuario(correoUsuario, "todos");
        }else if(correoCui != null){
            obtenerEjerciciosCuidador(correoCui, "todos");
        }else {
            Toast.makeText(this, "Error: No se encontró el correo del usuario", Toast.LENGTH_SHORT).show();
        }

        radioEspecie.setOnCheckedChangeListener((group, checkedId) -> {
            if (correoUsuario != null) {
                if (checkedId == R.id.rbFelinoYCanino) {
                    obtenerEjerciciosUsuario(correoUsuario, "todos");
                } else if (checkedId == R.id.rbFelino) {
                    obtenerEjerciciosUsuario(correoUsuario, "felino");
                } else if (checkedId == R.id.rbCanino) {
                    obtenerEjerciciosUsuario(correoUsuario, "canino");
                }
            }else if(correoCui != null){
                if (checkedId == R.id.rbFelinoYCanino) {
                    obtenerEjerciciosCuidador(correoCui, "todos");
                } else if (checkedId == R.id.rbFelino) {
                    obtenerEjerciciosCuidador(correoCui, "felino");
                }else if (checkedId == R.id.rbCanino) {
                    obtenerEjerciciosCuidador(correoCui, "canino");
                }
            }
        });
        mostrarBotones();
    }

    public void mostrarBotones(){
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correo = prefs.getString("correo", null);
        if (correo != null) {
            btnAgregarEjercicio.setVisibility(View.VISIBLE);
            btnEliminarEjercicio.setVisibility(View.VISIBLE);
            btnActualizarEjercicio.setVisibility(View.VISIBLE);
        }else{
            btnAgregarEjercicio.setVisibility(View.GONE);
            btnEliminarEjercicio.setVisibility(View.GONE);
            btnActualizarEjercicio.setVisibility(View.GONE);
        }
    }

    private void obtenerEjerciciosCuidador(String correo, String tipo) {
        String baseUrl = "http://192.168.0.13:8080/propietarios/obtenerEjercicios";

        switch (tipo) {
            case "felino":
                baseUrl += "FelinoVeterinario/";
                break;
            case "canino":
                baseUrl += "CaninoVeterinario/";
                break;
            default:
                baseUrl += "Veterinario/";
                break;
        }

        String url = baseUrl + correo;
        Log.d("VolleyDebug", "URL solicitada: " + url);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        listaEjercicios.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject ejercicioJson = response.getJSONObject(i);
                            Map<String, String> ejercicio = new HashMap<>();
                            ejercicio.put("nombre", ejercicioJson.getString("nombre"));
                            ejercicio.put("intensidad", ejercicioJson.getString("intensidad"));
                            ejercicio.put("especie", ejercicioJson.getString("especie"));
                            ejercicio.put("id_ejercicio", String.valueOf(ejercicioJson.getInt("id_ejercicio")));
                            ejercicio.put("duracion", String.valueOf(ejercicioJson.getInt("duracion")));
                            ejercicio.put("tiempo_descanso", String.valueOf(ejercicioJson.getInt("tiempo_descanso")));
                            ejercicio.put("imagen", ejercicioJson.optString("imagen", ""));

                            listaEjercicios.add(ejercicio);
                        }
                        ejercicioAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e("VolleyError", "Error procesando JSON", e);
                        Toast.makeText(this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", "Error en la solicitud", error);
                    Toast.makeText(this, "Error de conexión con el servidor", Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(request);
    }
    private void obtenerEjerciciosUsuario(String correo, String tipo) {
        String baseUrl = "http://192.168.0.13:8080/propietarios/obtenerEjercicios";

        switch (tipo) {
            case "felino":
                baseUrl += "Felino/";
                break;
            case "canino":
                baseUrl += "Canino/";
                break;
            default:
                baseUrl += "/";
                break;
        }

        String url = baseUrl + correo;
        Log.d("VolleyDebug", "URL solicitada: " + url);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        listaEjercicios.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject ejercicioJson = response.getJSONObject(i);
                            Map<String, String> ejercicio = new HashMap<>();
                            ejercicio.put("nombre", ejercicioJson.getString("nombre"));
                            ejercicio.put("intensidad", ejercicioJson.getString("intensidad"));
                            ejercicio.put("especie", ejercicioJson.getString("especie"));
                            ejercicio.put("id_ejercicio", String.valueOf(ejercicioJson.getInt("id_ejercicio")));
                            ejercicio.put("duracion", String.valueOf(ejercicioJson.getInt("duracion")));
                            ejercicio.put("tiempo_descanso", String.valueOf(ejercicioJson.getInt("tiempo_descanso")));
                            ejercicio.put("imagen", ejercicioJson.optString("imagen", ""));

                            listaEjercicios.add(ejercicio);
                        }
                        ejercicioAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e("VolleyError", "Error procesando JSON", e);
                        Toast.makeText(this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", "Error en la solicitud", error);
                    Toast.makeText(this, "Error de conexión con el servidor", Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(request);
    }



    public void onClickAgregarEjercicio(View view) {
        Intent intent = new Intent(Ejercicios.this, AgregarEjercicio.class);
        startActivity(intent);
    }

    public void onClickEliminarEjercicio(View view) {
        Intent intent = new Intent(Ejercicios.this, EliminarEjercicio.class);
        startActivity(intent);
    }

    public void onClickActualizarEjercicio(View view) {
        Intent intent = new Intent(Ejercicios.this, ActualizarEjercicio.class);
        startActivity(intent);
    }
}
