package co.unipiloto.appet;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistorialMedico extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    private ImageView imageView;
    private Button btnCamera, btnGallery, btnGuardar;
    private LinearLayout layoutVacunas;
    private CheckBox[] checkBoxes;
    private Spinner spinnerSangre, spinnerMascotas;
    private EditText editTextPeso, editTextEnfermedades, editTextMedicinas, editTextCirugias;
    private RequestQueue requestQueue;
    private Bitmap imagenSeleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_medico);

        imageView = findViewById(R.id.imageView);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);
        btnGuardar = findViewById(R.id.btnGuardar);
        spinnerSangre = findViewById(R.id.spinnerSangre);
        spinnerMascotas = findViewById(R.id.spinnerMascotas);
        editTextPeso = findViewById(R.id.editTextPeso);
        editTextEnfermedades = findViewById(R.id.editTextEnfermedades);
        editTextMedicinas = findViewById(R.id.editTextMedicinas);
        editTextCirugias = findViewById(R.id.editTextCirugias);
        requestQueue = Volley.newRequestQueue(this);

        btnCamera.setOnClickListener(v -> abrirCamara());
        btnGallery.setOnClickListener(v -> abrirGaleria());
        btnGuardar.setOnClickListener(this::onClickGuardar);

        layoutVacunas = findViewById(R.id.layoutVacunas);
        String[] vacunas = getResources().getStringArray(R.array.vacunas_mascotas);
        checkBoxes = new CheckBox[vacunas.length];

        for (int i = 0; i < vacunas.length; i++) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(vacunas[i]);
            layoutVacunas.addView(checkBox);
            checkBoxes[i] = checkBox;
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.tipo_sangre, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSangre.setAdapter(adapter);


        llenarSpinner();

        spinnerMascotas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                inicializarCampos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(HistorialMedico.this, "Seleccione una mascota", Toast.LENGTH_SHORT).show();
            }
        });


    }


    private void abrirCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
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

    private void inicializarCampos(){
        if (spinnerMascotas.getSelectedItem() == null) {
            Toast.makeText(this, "Seleccione una mascota", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] mascotaData = spinnerMascotas.getSelectedItem().toString().split("-");
        int idMascota = Integer.parseInt(mascotaData[0]);

        String urlGet = "http://192.168.0.13:8080/mascotas/obtener/" + idMascota;

        JsonObjectRequest requestGet = new JsonObjectRequest(Request.Method.GET, urlGet, null,
                response -> {
                    JSONObject historial = response.optJSONObject("historial");
                    if (historial != null && historial.length() != 0) {
                        String enfermedades = historial.optString("enfermedades", "");
                        String medicinas = historial.optString("medicinas", "");
                        String sangre = historial.optString("sangre", "");
                        int peso = historial.optInt("peso", 0);
                        String vacunas = historial.optString("vacunas", "");
                        String fotobase64 = historial.optString("foto", "");
                        String cirugias = historial.optString("cirugias", "");
                        if  (!fotobase64.isEmpty()){
                            Bitmap decodedByte = decodificarBase64(fotobase64);
                            imageView.setImageBitmap(decodedByte);
                            imagenSeleccionada = decodedByte;
                        }else{
                            imageView.setImageResource(R.mipmap.ic_launcher);
                        }
                        editTextEnfermedades.setText(enfermedades);
                        editTextMedicinas.setText(medicinas);
                        editTextPeso.setText(String.valueOf(peso));
                        editTextCirugias.setText(cirugias);
                        for (int i = 0; i < checkBoxes.length; i++) {
                            checkBoxes[i].setChecked(vacunas.contains(checkBoxes[i].getText().toString()));
                        }
                        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerSangre.getAdapter();
                        if (adapter != null) {
                            int position = adapter.getPosition(sangre);
                            if (position >= 0) {
                                spinnerSangre.setSelection(position);
                            }
                        }

                    }else{
                        editTextEnfermedades.setText("");
                        editTextMedicinas.setText("");
                        editTextPeso.setText(String.valueOf(""));
                        editTextCirugias.setText("");
                        spinnerSangre.setSelection(0);
                        for (CheckBox checkBox : checkBoxes) {
                            checkBox.setChecked(false);
                        }
                        imageView.setImageResource(R.mipmap.ic_launcher);
                    }
                },
                error -> Toast.makeText(this, "Error en la conexión", Toast.LENGTH_SHORT).show()
        );
        requestQueue.add(requestGet);
    }

    private Bitmap decodificarBase64(String encodedImage) {
        byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    private void abrirGaleria() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhotoIntent.setType("image/*");
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bundle extras = data.getExtras();
                imagenSeleccionada = (Bitmap) extras.get("data");
                imageView.setImageBitmap(imagenSeleccionada);
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                Uri selectedImageUri = data.getData();
                imageView.setImageURI(selectedImageUri);
                try {
                    imagenSeleccionada = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String convertirImagenABase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public void onClickGuardar(View view) {
        if (spinnerMascotas.getSelectedItem() == null) {
            Toast.makeText(this, "Seleccione una mascota", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] mascotaData = spinnerMascotas.getSelectedItem().toString().split("-");
        int idMascota = Integer.parseInt(mascotaData[0]);

        String urlGet = "http://192.168.0.13:8080/mascotas/obtener/" + idMascota;

        JsonObjectRequest requestGet = new JsonObjectRequest(Request.Method.GET, urlGet, null,
                response -> {
                    JSONObject historial = response.optJSONObject("historial");
                    if (historial == null || historial.length() == 0) {
                        registrarHistorial(idMascota);
                    } else {
                        actualizarHistorial(idMascota);
                    }
                },
                error -> Toast.makeText(this, "Error en la conexión", Toast.LENGTH_SHORT).show()
        );
        requestQueue.add(requestGet);
    }


    private void registrarHistorial(int idMascota) {
        JSONObject jsonBody = construirJsonHistorial();
        String urlPost = "http://192.168.0.13:8080/historiales/registrar/" + idMascota;

        JsonObjectRequest requestPost = new JsonObjectRequest(Request.Method.POST, urlPost, jsonBody,
                response -> {
                    Toast.makeText(this, "Historial registrado exitosamente", Toast.LENGTH_SHORT).show();
                    redirigirPerfilUsuario();
                },
                error -> Toast.makeText(this, "Error al registrar historial", Toast.LENGTH_SHORT).show()
        );
        requestQueue.add(requestPost);
    }

    private void actualizarHistorial(int idMascota) {
        JSONObject jsonBody = construirJsonHistorial();
        String urlPut = "http://192.168.0.13:8080/historiales/actualizar/" + idMascota;

        JsonObjectRequest requestPut = new JsonObjectRequest(Request.Method.PUT, urlPut, jsonBody,
                response -> {
                    Toast.makeText(this, "Historial actualizado exitosamente", Toast.LENGTH_SHORT).show();
                    redirigirPerfilUsuario();
                },
                error -> Toast.makeText(this, "Error al actualizar historial", Toast.LENGTH_SHORT).show()
        );
        requestQueue.add(requestPut);
    }

    private JSONObject construirJsonHistorial() {
        String enfermedades = editTextEnfermedades.getText().toString().trim();
        String medicinas = editTextMedicinas.getText().toString().trim();
        String sangre = spinnerSangre.getSelectedItem().toString();
        String pesoStr = editTextPeso.getText().toString().trim();
        String cirugias = editTextCirugias.getText().toString().trim();
        if (pesoStr.isEmpty()) {
            Toast.makeText(this, "Ingrese el peso de la mascota", Toast.LENGTH_SHORT).show();
            return null;
        }
        int peso = Integer.parseInt(pesoStr);
        String vacunasConcatenadas = "";
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                if (!vacunasConcatenadas.isEmpty()) {
                    vacunasConcatenadas += ", ";
                }
                vacunasConcatenadas += checkBox.getText().toString();
            }
        }

        boolean alMenosUnoSeleccionado = false;
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                alMenosUnoSeleccionado = true;
                break;
            }
        }
        if (!alMenosUnoSeleccionado) {
            vacunasConcatenadas = "N/A";
        }
        
        String fotoBase64 = (imagenSeleccionada != null) ? convertirImagenABase64(imagenSeleccionada) : "";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("enfermedades", enfermedades);
            jsonBody.put("medicinas", medicinas);
            jsonBody.put("vacunas", vacunasConcatenadas);
            jsonBody.put("sangre", sangre);
            jsonBody.put("peso", peso);
            jsonBody.put("foto", fotoBase64);
            jsonBody.put("cirugias", cirugias);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonBody;
    }

    private void redirigirPerfilUsuario() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}