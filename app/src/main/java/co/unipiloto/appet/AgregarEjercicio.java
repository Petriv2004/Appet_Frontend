package co.unipiloto.appet;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AgregarEjercicio extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    private ImageView imageView;
    private EditText etNombre, etDuracion, etDescanso;
    private Spinner intensidad;
    private RadioGroup especie;
    private String imagenBase64 = "";
    private Button btnCamera, btnGallery;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_ejercicio);

        imageView = findViewById(R.id.imageView);
        etNombre = findViewById(R.id.editTextNombre);
        etDuracion = findViewById(R.id.editDuracion);
        etDescanso = findViewById(R.id.editTextDescanso);
        intensidad = findViewById(R.id.spinnerIntensidad);
        especie = findViewById(R.id.rgEspecie);

        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);

        btnCamera.setOnClickListener(v -> abrirCamara());
        btnGallery.setOnClickListener(v -> abrirGaleria());

        requestQueue = Volley.newRequestQueue(this);
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

    private void abrirGaleria() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhotoIntent.setType("image/*");
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Bitmap bitmap = null;

            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                bitmap = (Bitmap) data.getExtras().get("data");
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                Uri imageUri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                imagenBase64 = convertirBase64(bitmap);
            }
        }
    }

    private String convertirBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public void onClickRegistrarEjercicio(View view) {
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correo = prefs.getString("correo", null);

        String nombre = etNombre.getText().toString().trim();
        String duracion = etDuracion.getText().toString().trim();
        String descanso = etDescanso.getText().toString().trim();
        int selectedId = especie.getCheckedRadioButtonId();

        if (nombre.isEmpty() || duracion.isEmpty() || descanso.isEmpty() || selectedId == -1) {
            Toast.makeText(this, "Todos los campos e imagen son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadioButton = findViewById(selectedId);
        String especieSeleccionada = selectedRadioButton.getText().toString();

        String url = "http://192.168.0.13:8080/propietarios/obtenerId/" + correo;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        int idPropietario = Integer.parseInt(response.trim());

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("nombre", nombre);
                        jsonObject.put("duracion", Integer.parseInt(duracion));
                        jsonObject.put("intensidad", intensidad.getSelectedItem().toString());
                        jsonObject.put("especie", especieSeleccionada);
                        jsonObject.put("tiempo_descanso", Integer.parseInt(descanso));
                        jsonObject.put("imagen", imagenBase64);

                        JSONObject propietarioObject = new JSONObject();
                        propietarioObject.put("id_propietario", idPropietario);
                        jsonObject.put("propietario", propietarioObject);

                        enviarDatosAlServidor(jsonObject);

                    } catch (NumberFormatException | JSONException e) {
                        Toast.makeText(this, "Error al obtener ID del propietario", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(this, "Error en la conexión: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                });

        requestQueue.add(request);
    }

    private void enviarDatosAlServidor(JSONObject jsonObject) {
        String url = "http://192.168.0.13:8080/propietarios/registrar_ejercicio";
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    Toast.makeText(this, "Ejercicio registrado con éxito", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(AgregarEjercicio.this, Ejercicios.class);
                    startActivity(intent);
                    finish();
                },
                error -> Toast.makeText(this, "Error al registrar ejercicio", Toast.LENGTH_LONG).show()
        );

        requestQueue.add(jsonObjectRequest);
    }
}
