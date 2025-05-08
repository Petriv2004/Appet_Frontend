package co.unipiloto.appet;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class EliminarCuentaActivity extends AppCompatActivity {

    private TextInputEditText etCodigo;
    private TextInputLayout tilCodigo;
    private Button btnEnviarCorreo, btnEnviarCodigo, btnEliminarCuenta;
    private RequestQueue queue;

    // Temporizador de token
    private TokenTimerService tokenService;
    private boolean isBound = false;
    private Handler handler = new Handler();

    // Estado del flujo
    private boolean emailSent = false;
    private boolean tokenVerified = false;

    private String correoGlobal;
    private static final String KEY_EMAIL_SENT     = "EMAIL_SENT";
    private static final String KEY_TOKEN_VERIFIED = "TOKEN_VERIFIED";

    // Conexión al servicio de temporizador
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TokenTimerService.TimerBinder binder = (TokenTimerService.TimerBinder) service;
            tokenService = binder.getService();
            isBound = true;
            if (emailSent && !tokenVerified) {
                iniciarActualizacionTiempo();
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eliminar_cuenta);

        // Inicializar vistas
        etCodigo          = findViewById(R.id.etCodigo);
        tilCodigo         = findViewById(R.id.tilCodigo);
        btnEnviarCorreo   = findViewById(R.id.btnEnviarCorreo);
        btnEnviarCodigo   = findViewById(R.id.btnEnviarCodigo);
        btnEliminarCuenta = findViewById(R.id.btnCambiarContrasena);

        // Configurar RequestQueue
        queue = Volley.newRequestQueue(this);

        // Icono de retroceso y título
        ImageView leftIcon = findViewById(R.id.left_icon);
        TextView title     = findViewById(R.id.title);
        leftIcon.setVisibility(View.VISIBLE);
        title.setText("Eliminar cuenta");
        leftIcon.setOnClickListener(v -> finish());

        // Recuperar correo de SharedPreferences
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        correoGlobal = prefs.getString("correo",
                prefs.getString("correoVet",
                        prefs.getString("correoCui", null)));

        // Restaurar estado si existe
        if (savedInstanceState != null) {
            emailSent     = savedInstanceState.getBoolean(KEY_EMAIL_SENT, false);
            tokenVerified = savedInstanceState.getBoolean(KEY_TOKEN_VERIFIED, false);
        }

        // Configurar listeners
        btnEnviarCorreo.setOnClickListener(v -> enviarCorreoToken());
        btnEnviarCodigo.setOnClickListener(v -> verificarToken());
        btnEliminarCuenta.setOnClickListener(v -> confirmarYEliminarCuenta());

        // Mostrar UI inicial
        actualizarUIsegunEstado();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_EMAIL_SENT,     emailSent);
        outState.putBoolean(KEY_TOKEN_VERIFIED, tokenVerified);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind al servicio de temporizador
        Intent svc = new Intent(this, TokenTimerService.class);
        bindService(svc, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }

    /**
     * Actualiza la visibilidad de las vistas según el estado
     */
    private void actualizarUIsegunEstado() {
        btnEnviarCorreo.setVisibility(emailSent ? View.GONE : View.VISIBLE);

        boolean showTokenEntry = emailSent && !tokenVerified;
        tilCodigo.setVisibility(showTokenEntry ? View.VISIBLE : View.GONE);
        etCodigo.setVisibility(showTokenEntry ? View.VISIBLE : View.GONE);
        btnEnviarCodigo.setVisibility(showTokenEntry ? View.VISIBLE : View.GONE);
        btnEnviarCodigo.setEnabled(true);

        btnEliminarCuenta.setVisibility((emailSent && tokenVerified) ? View.VISIBLE : View.GONE);
    }

    /**
     * Solicita el envío de correo con el token
     */
    private void enviarCorreoToken() {
        String url = Url.URL + "/token/obtener-token/" + correoGlobal;
        StringRequest req = new StringRequest(Request.Method.GET, url,
                resp -> {
                    emailSent = true;
                    Toast.makeText(this, "Correo enviado, revise su bandeja.", Toast.LENGTH_SHORT).show();
                    actualizarUIsegunEstado();
                    if (isBound) {
                        tokenService.iniciarTemporizador();
                        iniciarActualizacionTiempo();
                    }
                },
                err -> Toast.makeText(this, "Error al enviar correo", Toast.LENGTH_SHORT).show()
        );
        queue.add(req);
    }

    /**
     * Verifica el token ingresado por el usuario
     */
    private void verificarToken() {
        String code = etCodigo.getText().toString().trim();
        if (code.isEmpty()) {
            Toast.makeText(this, "Debe escribir el código", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = Url.URL + "/token/verify-token/" + code + "/" + correoGlobal;
        btnEnviarCodigo.setEnabled(false);
        StringRequest req = new StringRequest(Request.Method.GET, url,
                resp -> {
                    tokenVerified = true;
                    Toast.makeText(this, "Código correcto.", Toast.LENGTH_SHORT).show();
                    actualizarUIsegunEstado();
                    if (isBound) tokenService.cancelarTemporizador();
                },
                err -> {
                    Toast.makeText(this, "Código incorrecto o expirado", Toast.LENGTH_SHORT).show();
                    // Mantener vistas de token
                    actualizarUIsegunEstado();
                }
        );
        queue.add(req);
    }

    /**
     * Muestra diálogo de confirmación y elimina la cuenta
     */
    private void confirmarYEliminarCuenta() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Está seguro de que desea eliminar su cuenta? Esta acción es irreversible.")
                .setPositiveButton("Sí", (dialog, which) -> eliminarCuenta())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Llamada al DELETE para eliminar la cuenta y cerrar sesión
     */
    private void eliminarCuenta() {
        String url = Url.URL + "/propietarios/eliminar-cuenta/" + correoGlobal;
        StringRequest delReq = new StringRequest(Request.Method.DELETE, url,
                resp -> {
                    Toast.makeText(this, "Cuenta eliminada", Toast.LENGTH_SHORT).show();
                    cerrarSesion();
                },
                err -> {
                    Toast.makeText(this, "Error al eliminar cuenta", Toast.LENGTH_SHORT).show();
                    actualizarUIsegunEstado();
                }
        );
        queue.add(delReq);
    }

    /**
     * Inicia actualización del hint con el tiempo restante cada segundo
     */
    private void iniciarActualizacionTiempo() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isBound && tokenService.isActivo() && !tokenVerified) {
                    long rest = tokenService.getTiempoRestante();
                    long min  = rest / 60000;
                    long sec  = (rest / 1000) % 60;
                    etCodigo.setHint("Código (" + min + "m " + sec + "s)");
                    handler.postDelayed(this, 1000);
                } else {
                    etCodigo.setHint("Token expirado");
                    btnEnviarCodigo.setEnabled(false);
                }
            }
        }, 1000);
    }

    /**
     * Limpia SharedPreferences y regresa al MainActivity
     */
    private void cerrarSesion() {
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
