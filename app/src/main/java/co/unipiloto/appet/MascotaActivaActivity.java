package co.unipiloto.appet;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MascotaActivaActivity extends AppCompatActivity {

    private EditText etMonthYear;
    private Button btnCargar;
    private MascotaAdapter mascotaAdapter;
    private List<Map<String,String>> listaMascotas;
    private com.android.volley.RequestQueue requestQueue;
    private TextView txId,txNombre, txSexo, txTipo, txRaza, txFechaNacimiento, txEnfermedades, txMedicinas, txTiposangre, txPeso, txVacunas, txCirugias;
    private RadioButton radioActiva, radioSedentaria;
    private ImageView imageView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mascota_activa);

        ImageView leftIcon = findViewById(R.id.left_icon);
        leftIcon.setVisibility(View.VISIBLE);
        leftIcon.setOnClickListener(v -> finish());
        TextView title = findViewById(R.id.title);
        title.setText("Mascota Activa y Sedentaria");

        etMonthYear   = findViewById(R.id.etMonthYear);
        btnCargar     = findViewById(R.id.btnCargar);
        listaMascotas  = new ArrayList<>();
        mascotaAdapter = new MascotaAdapter(listaMascotas);
        requestQueue = Volley.newRequestQueue(this);
        etMonthYear.setOnClickListener(v -> mostrarMonthYearPicker());
        btnCargar.setOnClickListener(v -> cargarEstadisticas());

        imageView2 = findViewById(R.id.imageView2);
        txId = findViewById(R.id.txId);
        txNombre = findViewById(R.id.txNombre);
        txSexo = findViewById(R.id.txSexo);
        txTipo = findViewById(R.id.txTipo);
        txRaza = findViewById(R.id.txRaza);
        txFechaNacimiento = findViewById(R.id.txFechaNacimiento);
        txEnfermedades = findViewById(R.id.txEnfermedades);
        txMedicinas = findViewById(R.id.txMedicinas);
        txTiposangre = findViewById(R.id.txTiposangre);
        txPeso = findViewById(R.id.txPeso);
        txVacunas = findViewById(R.id.txVacunas);
        txCirugias = findViewById(R.id.txCirugias);

        radioActiva = findViewById(R.id.radioActiva);
        radioSedentaria = findViewById(R.id.radioSedentaria);

    }

    private void mostrarMonthYearPicker() {
        View dlgView = getLayoutInflater().inflate(R.layout.dialog_month_year, null);
        Spinner spMes  = dlgView.findViewById(R.id.spinnerMonth);
        Spinner spAnio = dlgView.findViewById(R.id.spinnerYear);

        // Meses completos (01–12), los usaremos como "backup"
        String[] mesesCompletos = new String[12];
        for (int i = 0; i < 12; i++) {
            mesesCompletos[i] = String.format("%02d", i + 1);
        }

        // Año actual
        int anioActual = Calendar.getInstance().get(Calendar.YEAR);
        int mesActual  = Calendar.getInstance().get(Calendar.MONTH) + 1;

        List<String> listaAnios = new ArrayList<>();
        for (int a = 2000; a <= anioActual; a++) {
            listaAnios.add(String.valueOf(a));
        }
        ArrayAdapter<String> adAnio = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, listaAnios);
        adAnio.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAnio.setAdapter(adAnio);
        spAnio.setSelection(listaAnios.indexOf(String.valueOf(anioActual)));

        Runnable actualizarMeses = () -> {
            int añoSel = Integer.parseInt(spAnio.getSelectedItem().toString());
            List<String> mesesPermitidos = new ArrayList<>();
            int limite = (añoSel == anioActual) ? mesActual : 12;
            for (int m = 1; m <= limite; m++) {
                mesesPermitidos.add(String.format("%02d", m));
            }
            ArrayAdapter<String> adMes = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, mesesPermitidos);
            adMes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spMes.setAdapter(adMes);
        };

        spAnio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                actualizarMeses.run();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        actualizarMeses.run();

        new AlertDialog.Builder(this)
                .setTitle("Selecciona Mes y Año")
                .setView(dlgView)
                .setPositiveButton("OK", (dlg, which) -> {
                    String mes  = spMes.getSelectedItem().toString();
                    String anio = spAnio.getSelectedItem().toString();
                    etMonthYear.setText(mes + "-" + anio);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


    private void cargarEstadisticas() {
        String mm_yyyy = etMonthYear.getText().toString();
        String[] part = mm_yyyy.split("-");
        if(part[0].charAt(0) == '0'){
            mm_yyyy = part[0].charAt(1) + "/"+ part[1] ;
        }else{
            mm_yyyy = part[0] + "/"+ part[1] ;
        }

        if (mm_yyyy.isEmpty()) {
            Toast.makeText(this, "Seleccione mes y año", Toast.LENGTH_SHORT).show();
            return;
        }

        String correo = getSharedPreferences("AppPreferences", MODE_PRIVATE)
                .getString("correo", null);
        if (correo == null) {
            Toast.makeText(this, "No has iniciado sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        //String urlActiva = Url.URL + "/estadisticas/mascota-activa/" + correo + "/" + mm_yyyy;

        String endpoint;
        if (radioActiva.isChecked()) {
            endpoint = "/estadisticas/mascota-activa/";
        } else if (radioSedentaria.isChecked()) {
            endpoint = "/estadisticas/mascota-sedentaria/";
        } else {
            Toast.makeText(this, "Seleccione un tipo de estadística", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = Url.URL + endpoint + correo + "/" + mm_yyyy;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        // Actualizar vistas
                        txId.setText("ID: " + response.optString("id_mascota", "—"));
                        txNombre.setText("Nombre: " + response.optString("nombre", "—"));
                        txSexo.setText("Sexo: " + response.optString("sexo", "—"));
                        txTipo.setText("Tipo: " + response.optString("tipo", "—"));
                        txRaza.setText("Raza: " + response.optString("raza", "—"));
                        txFechaNacimiento.setText("Fecha Nacimiento: " + response.optString("fecha_nacimiento", "—"));

                        if (!response.isNull("historial")) {
                            JSONObject h = response.getJSONObject("historial");
                            txEnfermedades.setText("Enfermedades: " + h.optString("enfermedades", "N/A"));
                            txMedicinas.setText("Medicinas: " + h.optString("medicinas", "N/A"));
                            txTiposangre.setText("Tipo de Sangre: " + h.optString("sangre", "N/A"));
                            txPeso.setText("Peso: " + h.optInt("peso", 0) + " kg");
                            txVacunas.setText("Vacunas: " + h.optString("vacunas", "N/A"));
                            txCirugias.setText("Cirugías: " + h.optString("cirugias", "N/A"));

                            // Imagen base64
                            String fotoBase64 = h.optString("foto", "");
                            if (!fotoBase64.isEmpty()) {
                                byte[] decodedString = android.util.Base64.decode(fotoBase64, android.util.Base64.DEFAULT);
                                android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                imageView2.setImageBitmap(decodedByte);
                            } else {
                                imageView2.setImageResource(R.drawable.border_white);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error procesando los datos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Error en la solicitud", Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(req);
    }
}