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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

public class CambiarContrasenaActivity extends AppCompatActivity {

    private EditText etCorreo, etCodigo, etContrasena, etConfirmarContrasena;
    private Button btnEnviarCodigo, btnCambiarContrasena, btnCorreo;
    private TextInputLayout tilContrasena, tilConfirmarContrasena;

    private RequestQueue queue;

    private TokenTimerService tokenService;
    private boolean isBound = false;
    private Handler handler = new Handler();

    private boolean emailSent     = false;
    private boolean tokenVerified = false;

    private String storedCorreo;

    private String correoGlobal;

    private static final String KEY_EMAIL_SENT     = "EMAIL_SENT";
    private static final String KEY_TOKEN_VERIFIED = "TOKEN_VERIFIED";

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TokenTimerService.TimerBinder binder = (TokenTimerService.TimerBinder) service;
            tokenService = binder.getService();
            isBound = true;
            // Si ya envié correo y no he verificado token, reanudo el timer
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
    protected void onStart() {
        super.onStart();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_contrasena);

        // Referencias a vistas
        etCorreo              = findViewById(R.id.etCorreo);
        etCodigo              = findViewById(R.id.etCodigo);
        etContrasena          = findViewById(R.id.etContrasena);
        etConfirmarContrasena = findViewById(R.id.etConfirmarContrasena);
        btnEnviarCodigo       = findViewById(R.id.btnEnviarCodigo);
        btnCambiarContrasena  = findViewById(R.id.btnCambiarContrasena);
        btnCorreo             = findViewById(R.id.btnEnviarCorreo);
        tilContrasena         = findViewById(R.id.tilContrasena);
        tilConfirmarContrasena= findViewById(R.id.tilConfirmarContrasena);

        queue = Volley.newRequestQueue(this);

        ImageView leftIcon = findViewById(R.id.left_icon);
        TextView title     = findViewById(R.id.title);
        leftIcon.setVisibility(View.VISIBLE);
        title.setText("Cambia Tu Contraseña");
        leftIcon.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));

        SharedPreferences pref = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        storedCorreo = pref.getString("correo",
                pref.getString("correoVet",
                        pref.getString("correoCui", null)));

        correoGlobal = storedCorreo;

        if (savedInstanceState != null) {
            emailSent     = savedInstanceState.getBoolean(KEY_EMAIL_SENT, false);
            tokenVerified = savedInstanceState.getBoolean(KEY_TOKEN_VERIFIED, false);
        }

        actualizarUIsegunEstado();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_EMAIL_SENT,     emailSent);
        outState.putBoolean(KEY_TOKEN_VERIFIED, tokenVerified);
    }

    private void actualizarUIsegunEstado() {
        etCodigo.setVisibility(View.GONE);
        btnEnviarCodigo.setVisibility(View.GONE);
        tilContrasena.setVisibility(View.GONE);
        tilConfirmarContrasena.setVisibility(View.GONE);
        btnCambiarContrasena.setVisibility(View.GONE);

        if (!emailSent) {
            if (storedCorreo == null) {
                etCorreo.setVisibility(View.VISIBLE);
            } else {
                etCorreo.setVisibility(View.GONE);
            }
            btnCorreo.setVisibility(View.VISIBLE);

        } else if (!tokenVerified) {
            // Etapa 2: validación de token
            etCorreo.setVisibility(View.GONE);
            btnCorreo.setVisibility(View.GONE);
            etCodigo.setVisibility(View.VISIBLE);
            btnEnviarCodigo.setVisibility(View.VISIBLE);

        } else {
            // Etapa 3: cambio de contraseña
            etCorreo.setVisibility(View.GONE);
            btnCorreo.setVisibility(View.GONE);
            etCodigo.setVisibility(View.GONE);
            btnEnviarCodigo.setVisibility(View.GONE);
            tilContrasena.setVisibility(View.VISIBLE);
            tilConfirmarContrasena.setVisibility(View.VISIBLE);
            btnCambiarContrasena.setVisibility(View.VISIBLE);
        }
    }

    public void onClickIrEnviarCorreo(View view) {
        String input = etCorreo.getText().toString().trim();
        if (storedCorreo == null) {
            // Usuario debe ingresar correo
            if (input.isEmpty()) {
                Toast.makeText(this, "Tiene que escribir su correo", Toast.LENGTH_SHORT).show();
                return;
            }
            correoGlobal = input;
        }
        // Si ya venía de prefs, correoGlobal = storedCorreo
        fetchEnviarCorreo(correoGlobal);
    }

    private void fetchEnviarCorreo(String correo) {
        String url = Url.URL + "/token/obtener-token/" + correo;
        StringRequest req = new StringRequest(Request.Method.GET, url,
                resp -> {
                    emailSent = true;
                    Toast.makeText(this, "Se ha enviado un correo, revise su bandeja de entrada", Toast.LENGTH_SHORT).show();
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

    public void onClickIrEnviarCodigo(View view) {
        String code = etCodigo.getText().toString().trim();
        if (code.isEmpty()) {
            Toast.makeText(this, "Tiene que escribir el código", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = Url.URL + "/token/verify-token/" + code + "/" + correoGlobal;
        StringRequest req = new StringRequest(Request.Method.GET, url,
                resp -> {
                    tokenVerified = true;
                    Toast.makeText(this, "El token es correcto, por favor ingrese su nueva contraseña", Toast.LENGTH_SHORT).show();
                    actualizarUIsegunEstado();
                    if (isBound) {
                        tokenService.cancelarTemporizador();
                    }
                },
                err -> Toast.makeText(this, "Código incorrecto o expirado", Toast.LENGTH_SHORT).show()
        );
        queue.add(req);
    }

    public void onClickIrCambiarContrasena(View view) {
        String pass1 = etContrasena.getText().toString();
        String pass2 = etConfirmarContrasena.getText().toString();

        if (pass1.isEmpty() || pass2.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!pass1.equals(pass2)) {
            Toast.makeText(this, "Las contraseñas tienen que coincidir", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject body = new JSONObject();
        try {
            body.put("contrasena", pass1);
            body.put("correo",     correoGlobal);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = Url.URL + "/propietarios/actualizar-contrasena";
        JsonObjectRequest put = new JsonObjectRequest(Request.Method.PUT, url, body,
                resp -> {
                    Toast.makeText(this, "Se ha actualizado la contraseña", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                },
                err -> Toast.makeText(this, "Error al actualizar contraseña", Toast.LENGTH_SHORT).show()
        );
        queue.add(put);
    }

    private void iniciarActualizacionTiempo() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isBound && tokenService.isActivo() && !tokenVerified) {
                    long rest = tokenService.getTiempoRestante();
                    long min  = rest / 60000;
                    long sec  = (rest / 1000) % 60;
                    etCodigo.setHint("Código de verificación (" + min + "m " + sec + "s)");
                    handler.postDelayed(this, 1000);
                } else {
                    etCodigo.setHint("Token expirado");
                    btnEnviarCodigo.setEnabled(false);
                    //btnCorreo.setVisibility(View.VISIBLE);
                }
            }
        }, 1000);
    }
}