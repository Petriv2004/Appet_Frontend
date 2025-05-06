package co.unipiloto.appet;

import static android.content.ContentValues.TAG;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerfilAgenda extends AppCompatActivity implements OnCitaUpdateListener {

    private RecyclerView recyclerView;
    private CitaAdapter citaAdapter;
    private List<Map<String, String>> listaCitas;
    private RequestQueue requestQueue;

    private Button btnAgregarCita;
    private String url;
    private boolean esVeterinario = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_agenda);

        recyclerView = findViewById(R.id.recyclerViewCitas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaCitas = new ArrayList<>();
        citaAdapter = new CitaAdapter(listaCitas, this);
        recyclerView.setAdapter(citaAdapter);
        btnAgregarCita = findViewById(R.id.btnAgregarCita);

        requestQueue = Volley.newRequestQueue(this);

        ImageView leftIcon = findViewById(R.id.left_icon);
        leftIcon.setVisibility(View.VISIBLE);
        TextView title = findViewById(R.id.title);
        title.setText("Agenda");

        leftIcon.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilAgenda.this, MainActivity.class);
            startActivity(intent);
        });

        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correoUsuario = prefs.getString("correo", null);
        String correoVet = prefs.getString("correoVet", null);

        if (correoUsuario != null) {
            btnAgregarCita.setVisibility(View.VISIBLE);
            url = Url.URL+"/propietarios/obtener_propietario/";
            obtenerCitasPropietario(correoUsuario);
        } else if (correoVet != null) {
            btnAgregarCita.setVisibility(View.GONE);
            url = Url.URL+"/agenda/citas-veterinario/";
            esVeterinario = true;
            obtenerCitasPropietario(correoVet);
        } else {
            Toast.makeText(this, "Error: No se encontró el correo del usuario", Toast.LENGTH_SHORT).show();
        }
    }

    private void obtenerCitasPropietario(String correo) {
        StringRequest request = new StringRequest(Request.Method.GET, url + correo,
                response -> {
                    try {
                        listaCitas.clear();
                        if (esVeterinario) {
                            JSONArray citasArray = new JSONArray(response);
                            for (int i = 0; i < citasArray.length(); i++) {
                                JSONObject citaJson = citasArray.getJSONObject(i);
                                Map<String, String> cita = new HashMap<>();
                                cita.put("id_agenda", String.valueOf(citaJson.getInt("id_agenda")));
                                cita.put("nombre_mascota", citaJson.getString("nombre")); // ya viene la mascota
                                cita.put("fecha", citaJson.getString("fecha"));
                                cita.put("hora", citaJson.getString("hora"));
                                cita.put("razon", citaJson.getString("razon"));
                                cita.put("descripcion", citaJson.getString("descripcion"));
                                cita.put("asistencia", String.valueOf(citaJson.getBoolean("asistencia")));
                                listaCitas.add(cita);
                            }
                        } else {
                            JSONObject propietario = new JSONObject(response);
                            JSONArray mascotasArray = propietario.getJSONArray("macotasList");
                            for (int i = 0; i < mascotasArray.length(); i++) {
                                JSONObject mascotaJson = mascotasArray.getJSONObject(i);
                                String nombreMascota = mascotaJson.getString("nombre");

                                JSONArray citasArray = mascotaJson.optJSONArray("citas");
                                if (citasArray != null) {
                                    for (int j = 0; j < citasArray.length(); j++) {
                                        JSONObject citaJson = citasArray.getJSONObject(j);
                                        Map<String, String> cita = new HashMap<>();
                                        cita.put("id_agenda", String.valueOf(citaJson.getInt("id_agenda")));
                                        cita.put("nombre_mascota", nombreMascota);
                                        cita.put("fecha", citaJson.getString("fecha"));
                                        cita.put("hora", citaJson.getString("hora"));
                                        cita.put("razon", citaJson.getString("razon"));
                                        cita.put("descripcion", citaJson.getString("descripcion"));
                                        cita.put("asistencia", String.valueOf(citaJson.getBoolean("asistencia")));
                                        listaCitas.add(cita);
                                    }
                                }
                            }
                        }
                        citaAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error en la conexión", Toast.LENGTH_SHORT).show()
        );
        requestQueue.add(request);
    }

    @Override
    public void onMarcarAsistida(String idCita, int position) {
        String urlActualizar = Url.URL+"/agenda/cambiar-asistido/" + idCita;
        StringRequest putRequest = new StringRequest(Request.Method.PUT, urlActualizar,
                response -> {
                    Toast.makeText(PerfilAgenda.this, "Cita marcada como asistida", Toast.LENGTH_SHORT).show();
                    listaCitas.get(position).put("asistencia", "true");
                    citaAdapter.notifyItemChanged(position);
                },
                error -> {
                    Toast.makeText(PerfilAgenda.this, "Error al actualizar la cita", Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(putRequest);
    }

    public void onClickIrGestionAgenda(View view) {
        Intent intent = new Intent(PerfilAgenda.this, RegistrarAgenda.class);
        startActivity(intent);
    }

    @Override
    public void onToggleRecordatorio(String idCita, int pos) {
        Map<String,String> cita = listaCitas.get(pos);
        String fecha       = cita.get("fecha");
        String hora        = cita.get("hora");
        String mascota     = cita.get("nombre_mascota");
        String descripcion = cita.get("razon");

        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String key = "reminder_" + idCita;
        boolean ya = prefs.getBoolean(key, false);

        // Guardar o eliminar datos
        SharedPreferences.Editor ed = prefs.edit();
        if (!ya) {
            ed.putBoolean(key, true)
                    .putString("fecha_"      + idCita, fecha)
                    .putString("hora_"       + idCita, hora)
                    .putString("mascota_"    + idCita, mascota)
                    .putString("descripcion_"+ idCita, descripcion);
        } else {
            ed.remove(key)
                    .remove("fecha_"      + idCita)
                    .remove("hora_"       + idCita)
                    .remove("mascota_"    + idCita)
                    .remove("descripcion_"+ idCita);
        }
        ed.apply();

        // Reprogramar o cancelar alarmas
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, ReminderReceiver.class)
                .putExtra("fecha", fecha)
                .putExtra("hora", hora)
                .putExtra("mascota", mascota)
                .putExtra("descripcion", descripcion);

        Calendar calCita = parsearAfecha(fecha, hora);
        if (!ya) {
            Log.d(TAG, "Programando recordatorio para id " + idCita);
            // programar
            Calendar calAntes = (Calendar) calCita.clone();
            calAntes.add(Calendar.HOUR_OF_DAY, -1);
            PendingIntent piAntes = PendingIntent.getBroadcast(
                    this,
                    idCita.hashCode()*10,
                    i.putExtra("notifId", idCita.hashCode()*10),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            am.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calAntes.getTimeInMillis(),
                    piAntes
            );

            PendingIntent piHora = PendingIntent.getBroadcast(
                    this,
                    idCita.hashCode()*10+1,
                    i.putExtra("notifId", idCita.hashCode()*10+1),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            am.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calCita.getTimeInMillis(),
                    piHora
            );

            Toast.makeText(this, "Recordatorio programado", Toast.LENGTH_SHORT).show();

        } else {
            Log.d(TAG, "Cancelando recordatorio para id " + idCita);
            // cancelar
            PendingIntent.getBroadcast(
                    this,
                    idCita.hashCode()*10,
                    i,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            ).cancel();

            PendingIntent.getBroadcast(
                    this,
                    idCita.hashCode()*10+1,
                    i,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            ).cancel();

            Toast.makeText(this, "Recordatorio cancelado", Toast.LENGTH_SHORT).show();
        }

        // Refrescar el adapter para actualizar el texto del botón
        citaAdapter.notifyItemChanged(pos);
    }


    private Calendar parsearAfecha(String fecha, String hora) {
        String[] f = fecha.split("-"), h = hora.split(":");
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR,   Integer.parseInt(f[0]));
        c.set(Calendar.MONTH,  Integer.parseInt(f[1]) - 1);
        c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(f[2]));
        c.set(Calendar.HOUR_OF_DAY,   Integer.parseInt(h[0]));
        c.set(Calendar.MINUTE,        Integer.parseInt(h[1]));
        c.set(Calendar.SECOND,        0);
        return c;
    }

}
