package co.unipiloto.appet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
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
import java.util.List;

public class EliminarMascota extends AppCompatActivity {

    RequestQueue queue;
    private Spinner etIdMascota;

    private static final String urlEliminarMascota = "http://192.168.0.13:8080/mascotas/eliminar-mascota/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eliminar_mascota);

        etIdMascota = findViewById(R.id.Eliminarspinner);

        ImageView leftIcon = findViewById(R.id.left_icon);
        leftIcon.setVisibility(View.VISIBLE);
        TextView title = findViewById(R.id.title);

        title.setText("Eliminar Mascota");
        queue = Volley.newRequestQueue(this);

        leftIcon.setOnClickListener(v -> {
            Intent intent = new Intent(EliminarMascota.this, MainActivity.class);
            startActivity(intent);
        });
        llenarSpinner();
    }

    private void llenarSpinner() {
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correo = prefs.getString("correo", null);

        if (correo == null) {
            Toast.makeText(this, "No se encontró el correo del propietario", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://192.168.0.13:8080/propietarios/obtener_propietario/" + correo;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject propietario = new JSONObject(response);
                        JSONArray mascotasList = propietario.getJSONArray("macotasList");

                        List<String> mascotas = new ArrayList<>();
                        List<String> citas = new ArrayList<>();

                        for (int i = 0; i < mascotasList.length(); i++) {
                            JSONObject mascota = mascotasList.getJSONObject(i);
                            int idMascota = mascota.getInt("id_mascota");
                            String nombreMascota = mascota.getString("nombre");
                            mascotas.add(idMascota + "-" + nombreMascota);

                        }

                        ArrayAdapter<String> adapterMascotas = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mascotas);
                        adapterMascotas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        etIdMascota.setAdapter(adapterMascotas);

                    } catch (JSONException e) {
                        Toast.makeText(this, "Error al procesar JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error al obtener datos", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

    public void onClickEliminarMascota(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Está seguro de que desea eliminar esta mascota? \nEsta " +
                        "acción no se puede deshacer y se perderan todos los datos relacionados a la mascota.")
                .setPositiveButton("Sí", (dialog, which) -> {
                    eliminarMascota();
                })
                .setNegativeButton("Cancelar", null)
                .show();


    }

    private void eliminarMascota() {
        String idMascota = etIdMascota.getSelectedItem().toString().split("-")[0];

        StringRequest request = new StringRequest(Request.Method.DELETE, urlEliminarMascota + idMascota,
                response -> {
                    Toast.makeText(EliminarMascota.this, "Mascota eliminada correctamente", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EliminarMascota.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    Log.e("VolleyError", "Error al eliminar el ejercicio", error);
                    Toast.makeText(EliminarMascota.this, "Error al eliminar la mascota", Toast.LENGTH_SHORT).show();
                }
        );
        queue.add(request);
    }
}