package co.unipiloto.appet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class registrar_mascota extends AppCompatActivity{

    private Spinner spinnerRaza;
    private RadioGroup rgTipo;

    private EditText editTextOtraRaza;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registrar_mascota);

        rgTipo = findViewById(R.id.rgTipo);
        spinnerRaza = findViewById(R.id.spinnerRaza);
        editTextOtraRaza = findViewById(R.id.editTextOtraRaza); // Referencia al campo de texto

        // Escuchar cambios en el tipo de mascota
        rgTipo.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbPerro) {
                actualizarSpinner(R.array.razas_perro);
            } else if (checkedId == R.id.rbGato) {
                actualizarSpinner(R.array.razas_gato);
            }
        });

        actualizarSpinner(R.array.razas_perro);

        spinnerRaza.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String seleccion = parent.getItemAtPosition(position).toString();
                if (seleccion.equals("Otro")) {
                    editTextOtraRaza.setVisibility(View.VISIBLE);
                    editTextOtraRaza.setError("Debe ingresar la raza");
                } else {
                    editTextOtraRaza.setVisibility(View.GONE);
                    editTextOtraRaza.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                editTextOtraRaza.setVisibility(View.GONE);
            }
        });
    }

    private void actualizarSpinner(int arrayId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, arrayId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRaza.setAdapter(adapter);


        editTextOtraRaza.setVisibility(View.GONE);
        editTextOtraRaza.setText("");
    }



    public void onClickRegistrarMascota(View view) {
        EditText etNombre = findViewById(R.id.editTextNombre);
        EditText etEdad = findViewById(R.id.editTextEdad);
        RadioGroup rgSexo = findViewById(R.id.rgSexo);

        String nombre = etNombre.getText().toString().trim();
        String edadTexto = etEdad.getText().toString().trim();
        int tipoSeleccionado = rgTipo.getCheckedRadioButtonId();
        String seleccion = spinnerRaza.getSelectedItem().toString();

        if (seleccion.equals("Otro")) {
            if (editTextOtraRaza.getText().toString().trim().isEmpty()) {
                editTextOtraRaza.setError("Debe ingresar la raza");
                return;
            }
        }
        if (nombre.isEmpty() || edadTexto.isEmpty() || rgSexo.getCheckedRadioButtonId() == -1 || tipoSeleccionado == -1) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        int edad;
        try {
            edad = Integer.parseInt(edadTexto);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "La edad debe ser un número válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (edad < 0) {
            Toast.makeText(this, "La edad no puede ser negativa", Toast.LENGTH_SHORT).show();
            return;
        }
        if (edad > 35) {
            Toast.makeText(this, "Revisa la edad ingresada", Toast.LENGTH_SHORT).show();
            return;
        }


        Toast.makeText(this, "Fue registrado con éxito su mascota", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LogIn.class);
        startActivity(intent);
    }



}
