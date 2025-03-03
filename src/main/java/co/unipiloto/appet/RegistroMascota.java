package co.unipiloto.appet;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class RegistroMascota extends AppCompatActivity {

    private EditText etNombre, etNacimiento;
    private RadioGroup rgSexo, rgEspecie;
    private Spinner spinnerRaza;
    private Button btnRegistrar;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_mascota);

        etNombre = findViewById(R.id.editTextNombre);
        etNacimiento = findViewById(R.id.editTextNacimiento);
        rgSexo = findViewById(R.id.rgSexo);
        rgEspecie = findViewById(R.id.rgEspecie);
        spinnerRaza = findViewById(R.id.spinnerRaza);
        btnRegistrar = findViewById(R.id.btnRegistrarMascota);

        requestQueue = Volley.newRequestQueue(this);

        etNacimiento.setOnClickListener(v -> showDatePicker());
        rgEspecie.setOnCheckedChangeListener((group, checkedId) -> actualizarRazas(checkedId));
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            String fechaSeleccionada = year1 + "-" + String.format("%02d", (month1 + 1)) + "-" + String.format("%02d", dayOfMonth);
            etNacimiento.setText(fechaSeleccionada);
        }, year, month, day);

        datePickerDialog.show();
    }

    private void actualizarRazas(int checkedId) {
        int razasArray = (checkedId == R.id.rbPerro) ? R.array.razas_perro : R.array.razas_gato;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, razasArray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRaza.setAdapter(adapter);
    }

    public void onClickRegistrarMascota(View view) {
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correoUsuario = prefs.getString("correo", null);

        if (correoUsuario != null) {
            obtenerIdPropietario(correoUsuario);
        } else {
            Toast.makeText(this, "Error: No se encontró el correo del usuario", Toast.LENGTH_SHORT).show();
        }
    }

    private void obtenerIdPropietario(String correo) {
        String url = "http://192.168.0.13:8080/propietarios/obtenerId/" + correo;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        int idPropietario = Integer.parseInt(response.trim());
                        registrarMascota(idPropietario);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Error al obtener ID del propietario", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error en la conexión", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(request);
    }

    private void registrarMascota(int idPropietario) {
        String url = "http://192.168.0.13:8080/mascotas/registrar";

        String nombre = etNombre.getText().toString().trim();
        String fechaNacimiento = etNacimiento.getText().toString().trim();
        String raza = spinnerRaza.getSelectedItem().toString();

        int sexoSeleccionado = rgSexo.getCheckedRadioButtonId();
        String sexo = (sexoSeleccionado == R.id.rbMacho) ? "Macho" : "Hembra";

        int especieSeleccionada = rgEspecie.getCheckedRadioButtonId();
        String especie = (especieSeleccionada == R.id.rbPerro) ? "Canino" : "Felino";

        if (nombre.isEmpty() || fechaNacimiento.isEmpty() || sexoSeleccionado == -1 || especieSeleccionada == -1) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("nombre", nombre);
            jsonBody.put("tipo", especie);
            jsonBody.put("raza", raza);
            jsonBody.put("especie", especie);
            jsonBody.put("fecha_nacimiento", fechaNacimiento);

            JSONObject propietario = new JSONObject();
            propietario.put("id_propietario", idPropietario);
            jsonBody.put("propietario", propietario);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> {
                        Toast.makeText(this, "Mascota registrada exitosamente", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, PerfilUsuario.class));
                        finish();
                    },
                    error -> Toast.makeText(this, "Error al registrar la mascota", Toast.LENGTH_SHORT).show()
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
