package co.unipiloto.appet;

import android.app.DatePickerDialog;
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

import java.util.Calendar;

public class RegistroMascota extends AppCompatActivity{

    private Spinner spinnerRaza;
    private RadioGroup rgEspecie;

    private EditText editTextOtraRaza;

    private EditText editTextNacimiento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro_mascota);

        rgEspecie = findViewById(R.id.rgEspecie);
        spinnerRaza = findViewById(R.id.spinnerRaza);
        editTextOtraRaza = findViewById(R.id.editTextOtraRaza);


        rgEspecie.setOnCheckedChangeListener((group, checkedId) -> {
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

        editTextNacimiento = findViewById(R.id.editTextNacimiento);

        editTextNacimiento.setOnClickListener(v -> mostrarDatePicker());
    }

    private void mostrarDatePicker() {
        final Calendar calendario = Calendar.getInstance();
        int anho = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String fechaSeleccionada = dayOfMonth + "/" + (month + 1) + "/" + year;
                    editTextNacimiento.setText(fechaSeleccionada);
                },
                anho, mes, dia
        );
        Calendar fechaMinima = Calendar.getInstance();
        fechaMinima.set(anho - 40, mes, dia);

        datePickerDialog.getDatePicker().setMinDate(fechaMinima.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
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
        EditText etEdad = findViewById(R.id.editTextNacimiento);
        RadioGroup rgSexo = findViewById(R.id.rgSexo);

        String nombre = etNombre.getText().toString().trim();
        String edadTexto = etEdad.getText().toString().trim();
        int tipoSeleccionado = rgEspecie.getCheckedRadioButtonId();
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

        Toast.makeText(this, "Fue registrado con éxito su mascota", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LogIn.class);
        startActivity(intent);
    }



}
