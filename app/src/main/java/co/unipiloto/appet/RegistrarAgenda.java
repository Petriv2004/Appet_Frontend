package co.unipiloto.appet;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RegistrarAgenda extends AppCompatActivity {

    private EditText etFecha, etHora,  etDescripcion;
    private Spinner etIdMascota, etAgenda;

    private Spinner spinnerRazon;
    private RequestQueue requestQueue;

    private Button guardarAgenda, eliminarCita;

    private boolean isRequestInProgress = false;

    private static final String URL_REGISTRAR_AGENDA = Url.URL+"/agenda/registrar/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_agenda);

        spinnerRazon = findViewById(R.id.razonHint);

        String[] opciones = {"Veterinario", "Peluqueria", "Vacunacion", "Otro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opciones);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        guardarAgenda = findViewById(R.id.guardarAgenda);
        eliminarCita = findViewById(R.id.eliminarCita);
        spinnerRazon.setAdapter(adapter);
        etIdMascota = findViewById(R.id.Idspinner);
        etFecha = findViewById(R.id.fechaHint);
        etHora = findViewById(R.id.horaHint);
        etDescripcion = findViewById(R.id.descripcionHint);
        etAgenda = findViewById(R.id.Eliminarspinner);
        requestQueue = Volley.newRequestQueue(this);

        etFecha.setOnClickListener(v -> mostrarDatePicker());
        etHora.setOnClickListener(v -> mostrarTimePicker());

        llenarSpinner();

        ImageView leftIcon = findViewById(R.id.left_icon);
        leftIcon.setVisibility(View.VISIBLE);
        TextView title = findViewById(R.id.title);
        title.setText("Agenda tus citas");

        leftIcon.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrarAgenda.this, MainActivity.class);
            startActivity(intent);
        });
    }
    private void mostrarDatePicker() {
        Calendar calendario = Calendar.getInstance();
        //calendario.add(Calendar.DAY_OF_MONTH, 1);

        int año = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String fechaSeleccionada = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            etFecha.setText(fechaSeleccionada);
        }, año, mes, dia);

        datePickerDialog.getDatePicker().setMinDate(calendario.getTimeInMillis());

        datePickerDialog.show();
    }
    private void mostrarTimePicker() {
        Calendar calendario = Calendar.getInstance();
        int hora = calendario.get(Calendar.HOUR_OF_DAY);
        int minuto = calendario.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String horaSeleccionada = String.format("%02d:%02d:%02d", hourOfDay, minute, 0);
            etHora.setText(horaSeleccionada);
        }, hora, minuto, true);

        timePickerDialog.show();
    }
    private void llenarSpinner() {
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correo = prefs.getString("correo", null);

        if (correo == null) {
            Toast.makeText(this, "No se encontró el correo del propietario", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = Url.URL+"/propietarios/obtener_propietario/" + correo;

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

                            JSONArray citasList = mascota.getJSONArray("citas");
                            for (int j = 0; j < citasList.length(); j++) {
                                JSONObject cita = citasList.getJSONObject(j);
                                int idAgenda = cita.getInt("id_agenda");
                                String razon = cita.getString("razon");
                                String fecha = cita.getString("fecha");

                                citas.add(idAgenda + "-" + nombreMascota + "-" + razon + "-" + fecha);
                            }
                        }

                        ArrayAdapter<String> adapterMascotas = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mascotas);
                        adapterMascotas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        etIdMascota.setAdapter(adapterMascotas);

                        ArrayAdapter<String> adapterCitas = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, citas);
                        adapterCitas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        etAgenda.setAdapter(adapterCitas);

                    } catch (JSONException e) {
                        Toast.makeText(this, "Error al procesar JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error al obtener datos", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(request);
    }
    public void onClickGuardarAgenda(View view) {
        if (isRequestInProgress) return;

        isRequestInProgress = true;
        guardarAgenda.setClickable(false);

        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correo = prefs.getString("correo", null);

        if (correo == null) {
            Toast.makeText(this, "No se encontró el correo del propietario", Toast.LENGTH_SHORT).show();
            guardarAgenda.setClickable(true);
            isRequestInProgress = false;
            return;
        }

        String opcidMascota = String.valueOf(etIdMascota.getSelectedItem());
        String[] sel = opcidMascota.split("-");
        String idMascota = sel[0];
        String fecha = etFecha.getText().toString().trim();
        String hora = etHora.getText().toString().trim();
        String razon = spinnerRazon.getSelectedItem().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();

        if (idMascota.isEmpty() || fecha.isEmpty() || hora.isEmpty() || razon.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            guardarAgenda.setClickable(true);
            isRequestInProgress = false;
            return;
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("fecha", fecha);
            jsonBody.put("hora", hora);
            jsonBody.put("razon", razon);
            jsonBody.put("descripcion", descripcion);
            jsonBody.put("estado", false);
        } catch (JSONException e) {
            Toast.makeText(this, "Error al crear la solicitud", Toast.LENGTH_SHORT).show();
            guardarAgenda.setClickable(true);
            isRequestInProgress = false;
            return;
        }

        String url = URL_REGISTRAR_AGENDA + correo + "/" + idMascota;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    isRequestInProgress = false;
                    Toast.makeText(RegistrarAgenda.this, "Agenda registrada correctamente", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegistrarAgenda.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    Toast.makeText(RegistrarAgenda.this, "Error al registrar agenda", Toast.LENGTH_SHORT).show();
                    guardarAgenda.setClickable(true);
                    isRequestInProgress = false;
                }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                return jsonBody.toString().getBytes();
            }
        };

        requestQueue.add(stringRequest);
    }

    public void onClickEliminarCita(View view) {
        eliminarCita.setClickable(false);
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correo = prefs.getString("correo", null);

        if (correo == null) {
            Toast.makeText(this, "No se encontró el correo del propietario", Toast.LENGTH_SHORT).show();
            eliminarCita.setClickable(true);
            return;
        }

        String opcAgenda = String.valueOf(etAgenda.getSelectedItem());
        String[] sel = opcAgenda.split("-");
        String idCita = sel[0];

        new AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Está seguro de que desea eliminar esta cita?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                    Intent cancelIntent = new Intent(this, ReminderReceiver.class);

                    // Cancelar recordatorio 1h antes
                    PendingIntent piAntes = PendingIntent.getBroadcast(
                            this,
                            idCita.hashCode() * 10,
                            cancelIntent,
                            PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
                    );
                    if (piAntes != null) {
                        am.cancel(piAntes);
                        piAntes.cancel();
                    }

                    // Cancelar recordatorio a la hora exacta
                    PendingIntent piHora = PendingIntent.getBroadcast(
                            this,
                            idCita.hashCode() * 10 + 1,
                            cancelIntent,
                            PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
                    );
                    if (piHora != null) {
                        am.cancel(piHora);
                        piHora.cancel();
                    }

                    // Limpieza de SharedPreferences
                    prefs.edit()
                            .remove("reminder_"     + idCita)
                            .remove("fecha_"        + idCita)
                            .remove("hora_"         + idCita)
                            .remove("mascota_"      + idCita)
                            .remove("descripcion_"  + idCita)
                            .apply();

                    // Ahora sí eliminas la cita del servidor
                    String url = Url.URL + "/agenda/eliminar/" + correo + "/" + idCita;
                    StringRequest request = new StringRequest(
                            Request.Method.DELETE, url,
                            response -> {
                                Toast.makeText(this,
                                        "Cita eliminada correctamente", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            },
                            error -> {
                                Toast.makeText(this,
                                        "Error al eliminar la cita", Toast.LENGTH_SHORT).show();
                                eliminarCita.setClickable(true);
                            }
                    );
                    requestQueue.add(request);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    eliminarCita.setClickable(true);
                    dialog.dismiss();
                })
                .show();
    }


}