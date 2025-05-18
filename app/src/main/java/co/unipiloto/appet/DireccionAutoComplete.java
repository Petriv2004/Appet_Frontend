package co.unipiloto.appet;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class DireccionAutoComplete {

    private Context context;
    private AutoCompleteTextView editTextDireccion;
    private RequestQueue requestQueue;

    public DireccionAutoComplete(Context context, AutoCompleteTextView editTextDireccion) {
        this.context = context;
        this.editTextDireccion = editTextDireccion;
        requestQueue = Volley.newRequestQueue(context);

        setupAutoComplete();
    }

    private void setupAutoComplete() {
        editTextDireccion.setThreshold(2); // Mínimo 2 caracteres antes de sugerencias

        editTextDireccion.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) {
                    obtenerSugerencias(s.toString());
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        editTextDireccion.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                editTextDireccion.dismissDropDown();
            }
        });
    }

    private void obtenerSugerencias(String query) {
        String url = "https://nominatim.openstreetmap.org/search?format=json&q=" + query;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    List<String> direcciones = new ArrayList<>();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonObject = response.getJSONObject(i);
                            String direccion = jsonObject.getString("display_name");
                            direcciones.add(direccion);
                        }
                        actualizarSugerencias(direcciones);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("DireccionAutoComplete", "Error al obtener sugerencias: " + error.getMessage())
        );

        requestQueue.add(jsonArrayRequest);
    }

    private void actualizarSugerencias(List<String> direcciones) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, direcciones);
        editTextDireccion.setAdapter(adapter);

        // Solo muestra el dropdown si el usuario aún tiene el foco en el campo
        if (editTextDireccion.hasFocus()) {
            editTextDireccion.showDropDown();
        }
    }
}
