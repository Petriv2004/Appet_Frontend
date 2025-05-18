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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ActualizarEjercicio extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    private Spinner spinnerEjercicio;
    private RequestQueue requestQueue;

    private EditText editTextNombre, editDuracion,  editTextDescanso;

    private Button btnCamera, btnGallery;
    private ImageView imageView;
    private Spinner spinnerIntensidad;
    private RadioGroup rgEspecie;
    private Bitmap imagenSeleccionada;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizar_ejercicio);

        spinnerEjercicio = findViewById(R.id.spinnerEjercicio);
        editTextNombre = findViewById(R.id.editTextNombre);
        editDuracion = findViewById(R.id.editDuracion);
        spinnerIntensidad = findViewById(R.id.spinnerIntensidad);
        editTextDescanso = findViewById(R.id.editTextDescanso);
        rgEspecie = findViewById(R.id.rgEspecie);
        imageView = findViewById(R.id.imageView);

        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);

        btnCamera.setOnClickListener(v -> abrirCamara());
        btnGallery.setOnClickListener(v -> abrirGaleria());

        requestQueue = Volley.newRequestQueue(this);

        llenarSpinner();
        spinnerEjercicio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                inicializarCampos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(ActualizarEjercicio.this, "Seleccione una mascota", Toast.LENGTH_SHORT).show();
            }
        });
        ImageView leftIcon = findViewById(R.id.left_icon);
        leftIcon.setVisibility(View.VISIBLE);
        TextView title = findViewById(R.id.title);
        title.setText("Actualiza un Ejercicio");

        leftIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ActualizarEjercicio.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void abrirCamara() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
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

    private void inicializarCampos(){
        if (spinnerEjercicio.getSelectedItem() == null) {
            Toast.makeText(this, "Seleccione una mascota", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] ejercicioData = spinnerEjercicio.getSelectedItem().toString().split(" - ");
        int idEjercicio = Integer.parseInt(ejercicioData[0]);

        String urlGet = Url.URL+"/propietarios/obtenerEjercicio/" + idEjercicio;
        JsonObjectRequest requestGet = new JsonObjectRequest(Request.Method.GET, urlGet, null,
                response -> {
                    Log.d("JSON Response", response.toString());

                    String nombre = response.optString("nombre", "");
                    int duracion = response.optInt("duracion", 0);
                    String intensidad = response.optString("intensidad", "");
                    String especie = response.optString("especie", "");
                    int descanso = response.optInt("tiempo_descanso", 0);
                    String fotobase64 = response.optString("imagen", "");

                    if (!fotobase64.isEmpty()) {
                        Bitmap decodedByte = decodificarBase64(fotobase64);
                        imageView.setImageBitmap(decodedByte);
                        imagenSeleccionada = decodedByte;
                    } else {
                        imageView.setImageResource(R.mipmap.ic_launcher);
                    }

                    editTextNombre.setText(nombre);
                    editDuracion.setText(String.valueOf(duracion));
                    editTextDescanso.setText(String.valueOf(descanso));

                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerIntensidad.getAdapter();
                    if (adapter != null) {
                        int position = adapter.getPosition(intensidad);
                        if (position >= 0) {
                            spinnerIntensidad.setSelection(position);
                        }
                    }

                    if (especie.equals("Felino")) {
                        rgEspecie.check(R.id.rbFelino);
                    } else if (especie.equals("Canino")) {
                        rgEspecie.check(R.id.rbCanino);
                    }
                },
                error -> {
                    Log.e("VolleyError", "Error en la conexión", error);
                    if (error.networkResponse != null) {
                        Log.e("VolleyError", "Código de respuesta: " + error.networkResponse.statusCode);
                        Log.e("VolleyError", "Datos de respuesta: " + new String(error.networkResponse.data));
                    }
                    Toast.makeText(this, "Error en la conexión", Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(requestGet);

    }

    public void onClickRegistrarEjercicio(View view) {
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correo = prefs.getString("correo", null);

        if (correo == null) {
            Toast.makeText(this, "No se encontró el correo del propietario", Toast.LENGTH_SHORT).show();
            return;
        }
        if (spinnerEjercicio.getSelectedItem() == null) {
            Toast.makeText(this, "Seleccione un ejercicio para actualizar", Toast.LENGTH_SHORT).show();
            return;
        }
        String selectedItem = spinnerEjercicio.getSelectedItem().toString();
        String[] parts = selectedItem.split(" - ");
        if (parts.length < 3) {
            Toast.makeText(this, "Error al obtener el ID del ejercicio", Toast.LENGTH_SHORT).show();
            return;
        }
        String idEjercicio = parts[0].trim();

        String nombre = editTextNombre.getText().toString().trim();
        String duracion = editDuracion.getText().toString().trim();
        String intensidad = spinnerIntensidad.getSelectedItem().toString();
        String descanso = editTextDescanso.getText().toString().trim();
        int selectedId = rgEspecie.getCheckedRadioButtonId();

        if (nombre.isEmpty() || duracion.isEmpty() || descanso.isEmpty() || selectedId == -1) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton especieSeleccionadaRadioButton = findViewById(selectedId);
        String especieSeleccionada = especieSeleccionadaRadioButton.getText().toString();

        String imagenBase64 = "";
        if (imagenSeleccionada != null) {
            imagenBase64 = convertirImagenABase64(imagenSeleccionada);
        }

        String url = Url.URL+"/propietarios/actualizar_ejercicio/" + idEjercicio;
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("nombre", nombre);
            jsonObject.put("duracion", Integer.parseInt(duracion));
            jsonObject.put("intensidad", intensidad);
            jsonObject.put("especie", especieSeleccionada);
            jsonObject.put("tiempo_descanso", Integer.parseInt(descanso));
            jsonObject.put("imagen", imagenBase64);
            jsonObject.put("correo", correo);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                    response -> {
                        try {
                            // Si el servidor responde correctamente
                            if (response.has("id_propietario")) {
                                int idPropietario = response.getInt("id_propietario");
                            }

                            Toast.makeText(this, "Ejercicio actualizado con éxito", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(ActualizarEjercicio.this, Ejercicios.class);
                            startActivity(intent);
                            finish();
                        } catch (JSONException e) {
                            Log.e("Volley", "Error al procesar la respuesta del servidor", e);
                            Toast.makeText(this, "Error en la respuesta del servidor", Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> {
                        Log.e("Volley", "Error al actualizar el ejercicio", error);
                        Toast.makeText(this, "No se pudo actualizar el ejercicio. Intente nuevamente.", Toast.LENGTH_LONG).show();
                    }
            );

            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            Log.e("JSON", "Error al crear JSON", e);
            Toast.makeText(this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
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
    private Bitmap decodificarBase64(String encodedImage) {
        byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
    private void llenarSpinner() {
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String correo = prefs.getString("correo", null);

        if (correo == null) {
            Toast.makeText(this, "No se encontró el correo del propietario", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = Url.URL+"/propietarios/obtenerEjercicios/" + correo;

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