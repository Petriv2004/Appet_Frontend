package co.unipiloto.appet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EliminarEjercicio extends AppCompatActivity {

    private Spinner spinnerEjercicio;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eliminar_ejercicio);

        spinnerEjercicio = findViewById(R.id.spinnerEjercicio);
        requestQueue = Volley.newRequestQueue(this);

        llenarSpinner();
    }

    public void onClickEliminarEjercicio(View view) {
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correo = prefs.getString("correo", null);

        if (correo == null) {
            Toast.makeText(this, "No se encontró el correo del propietario", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spinnerEjercicio.getSelectedItem() == null) {
            Toast.makeText(this, "No hay ejercicios para eliminar", Toast.LENGTH_SHORT).show();
            return;
        }

        String opcAgenda = spinnerEjercicio.getSelectedItem().toString();
        if (opcAgenda.equals("No hay ejercicios disponibles")) {
            Toast.makeText(this, "No hay ejercicios para eliminar", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] sel = opcAgenda.split("-");
        String idEjercicio = sel[0].trim();

        new AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Está seguro de que desea eliminar este ejercicio?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    String url = "http://192.168.0.13:8080/propietarios/eliminarEjercicio/" + correo + "/" + idEjercicio;
                    StringRequest request = new StringRequest(Request.Method.DELETE, url,
                            response -> {
                                Toast.makeText(EliminarEjercicio.this, "Ejercicio eliminado correctamente", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(EliminarEjercicio.this, Ejercicios.class);
                                startActivity(intent);
                            },
                            error -> {
                                Log.e("VolleyError", "Error al eliminar el ejercicio", error);
                                Toast.makeText(EliminarEjercicio.this, "Error al eliminar el ejercicio", Toast.LENGTH_SHORT).show();
                            }
                    );

                    requestQueue.add(request);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


    private void llenarSpinner() {
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correo = prefs.getString("correo", null);

        if (correo == null) {
            Toast.makeText(this, "No se encontró el correo del propietario", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://192.168.0.13:8080/propietarios/obtenerEjercicios/" + correo;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        List<String> ejercicios = new ArrayList<>();

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject ejercicio = response.getJSONObject(i);
                            int idEjercicio = ejercicio.getInt("id_ejercicio");
                            String nombreEjercicio = ejercicio.getString("nombre");
                            String especie = ejercicio.getString("especie");
                            ejercicios.add(idEjercicio + " - " + nombreEjercicio + " - " + especie);
                        }

                        if (ejercicios.isEmpty()) {
                            Toast.makeText(this, "No hay ejercicios disponibles", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ArrayAdapter<String> adapterEjercicios = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_dropdown_item, ejercicios);
                        adapterEjercicios.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerEjercicio.setAdapter(adapterEjercicios);

                    } catch (JSONException e) {
                        Log.e("VolleyError", "Error al procesar JSON", e);
                        Toast.makeText(this, "Error al procesar JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", "Error al obtener datos", error);
                    Toast.makeText(this, "Error de conexión con el servidor", Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(request);
    }

}
