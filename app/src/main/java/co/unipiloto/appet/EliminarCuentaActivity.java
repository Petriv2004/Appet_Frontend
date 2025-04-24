package co.unipiloto.appet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

public class EliminarCuentaActivity extends AppCompatActivity  {

    private TextInputEditText etCodigo;
    private Button btnEnviarCodigo, btnCambiarContrasena, btnEnviarCorreo;
    private String correo;

    private TextInputLayout tilCodigo;

    // URLs de las peticiones
    private static final String urlEnviarCorreo = Url.URL+"/token/obtener-token/";
    private static final String urlEnviarToken = Url.URL+"/token/verify-token/";
    private static final String urlEliminarCuenta = Url.URL+"/propietarios/eliminar-cuenta/";

    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eliminar_cuenta);


        etCodigo = findViewById(R.id.etCodigo);
        btnEnviarCorreo = findViewById(R.id.btnEnviarCorreo);
        btnEnviarCodigo = findViewById(R.id.btnEnviarCodigo);
        btnCambiarContrasena = findViewById(R.id.btnCambiarContrasena);
        tilCodigo = findViewById(R.id.tilCodigo);
        btnEnviarCodigo.setVisibility(View.GONE);
        btnCambiarContrasena.setVisibility(View.GONE);
        etCodigo.setVisibility(View.GONE);
        tilCodigo.setVisibility(View.GONE);

        queue = Volley.newRequestQueue(this);

        ImageView leftIcon = findViewById(R.id.left_icon);
        leftIcon.setVisibility(View.VISIBLE);
        TextView title = findViewById(R.id.title);
        title.setText("Eliminar cuenta");

        leftIcon.setOnClickListener(v -> {
            Intent intent = new Intent(EliminarCuentaActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // Recupera el correo del usuario desde SharedPreferences
        SharedPreferences pref = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        correo = pref.getString("correo", pref.getString("correoVet", pref.getString("correoCui", null)));
        }

    // Método invocado al hacer clic en el botón "Enviar correo"
    public void onClickIrEnviarCorreo(View view) {
        fetchEnviarCorreo();
    }

    // Envía la solicitud para enviar el correo de token
    public void fetchEnviarCorreo(){
        StringRequest request = new StringRequest(Request.Method.GET, urlEnviarCorreo + correo,
                response -> {
                    Toast.makeText(EliminarCuentaActivity.this, "Se ha enviado un correo, revise su bandeja de entrada", Toast.LENGTH_SHORT).show();
                    btnEnviarCodigo.setVisibility(View.VISIBLE);
                    etCodigo.setVisibility(View.VISIBLE);
                    tilCodigo.setVisibility(View.VISIBLE);
                    btnEnviarCorreo.setVisibility(View.GONE);
                },
                error -> Toast.makeText(this, "Error al enviar correo", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

    public void onClickIrEnviarCodigo(View view){
        String codigo = etCodigo.getText().toString();
        if (codigo == null || codigo.isEmpty()){
            Toast.makeText(EliminarCuentaActivity.this, "Tiene que escribir el código", Toast.LENGTH_SHORT).show();
            return;
        }
        StringRequest request = new StringRequest(Request.Method.GET, urlEnviarToken  + codigo + "/" + correo,
                response -> {
                    Toast.makeText(EliminarCuentaActivity.this, "El token es correcto", Toast.LENGTH_SHORT).show();
                    btnEnviarCodigo.setVisibility(View.GONE);
                    etCodigo.setVisibility(View.GONE);
                    btnEnviarCorreo.setVisibility(View.GONE);
                    btnCambiarContrasena.setVisibility(View.VISIBLE);
                },
                error -> Toast.makeText(this, "Error al enviar código", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

    public void onClickIrCambiarContrasena(View view){
        new AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Está seguro de que desea eliminar su cuenta? \nEsta acción no se puede deshacer y se perderán todos sus datos.")
                .setPositiveButton("Sí", (dialog, which) -> {
                    StringRequest requestPut = new StringRequest(Request.Method.DELETE, urlEliminarCuenta + correo,
                            response -> {
                                Toast.makeText(EliminarCuentaActivity.this, "Se ha eliminado su cuenta", Toast.LENGTH_SHORT).show();
                                cerrarSesion();
                            },
                            error -> Toast.makeText(this, "Error al eliminar su cuenta", Toast.LENGTH_SHORT).show()
                    );
                    queue.add(requestPut);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Cierra la sesión y limpia el correo almacenado
    private void cerrarSesion() {
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("correo", null);
        editor.putString("correoVet", null);
        editor.putString("correoCui", null);
        editor.apply();
        Intent intent = new Intent(EliminarCuentaActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
