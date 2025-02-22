package co.unipiloto.appet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Registro extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void onClickRegistrar(View view) {
        EditText etCorreo = findViewById(R.id.etCorreo);
        EditText etContrasena = findViewById(R.id.etContrasena);
        EditText etConfirmarContrasena = findViewById(R.id.etConfirmarContrasena);
        EditText etNombreCompleto = findViewById(R.id.etNombreCompleto);
        EditText etNumeroCelular = findViewById(R.id.etNumeroCelular);
        EditText etDireccion = findViewById(R.id.etDireccion);
        RadioGroup rgGenero = findViewById(R.id.rgGenero);

        String correo = etCorreo.getText().toString().trim();
        String contrasena = etContrasena.getText().toString();
        String confirmarContrasena = etConfirmarContrasena.getText().toString();
        String nombre = etNombreCompleto.getText().toString().trim();
        String celular = etNumeroCelular.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();
        int generoSeleccionado = rgGenero.getCheckedRadioButtonId();

        if (correo.isEmpty() || contrasena.isEmpty() || confirmarContrasena.isEmpty() ||
                nombre.isEmpty() || celular.isEmpty() || generoSeleccionado == -1 || direccion.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Correo no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (contrasena.length() < 8) {
            Toast.makeText(this, "Tu contraseña debe tener al menos 8 caracteres", Toast.LENGTH_LONG).show();
            return;
        }

        if (!contrasena.matches(".*[A-Z].*")) {
            Toast.makeText(this, "Escribe una mayúscula en su contraseña", Toast.LENGTH_LONG).show();
            return;
        }

        if (!contrasena.matches(".*\\d.*")) {
            Toast.makeText(this, "Tu contraseña debe contener al menos un número", Toast.LENGTH_LONG).show();
            return;
        }

        if (!contrasena.matches(".*[^a-zA-Z0-9].*")) {
            Toast.makeText(this, "Agrega un carácter especial a tu contraseña (!@#$%^&* etc.)", Toast.LENGTH_LONG).show();
            return;
        }

        if (!contrasena.equals(confirmarContrasena)) {
            Toast.makeText(this, "Tus contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        if(celular.length() != 10){
            Toast.makeText(this, "El teléfono debe tener 10 números", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Fue registrado con éxito", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LogIn.class);
        startActivity(intent);
    }
}
