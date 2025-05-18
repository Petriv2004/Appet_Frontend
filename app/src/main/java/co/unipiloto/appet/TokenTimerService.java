package co.unipiloto.appet;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;

public class TokenTimerService extends Service {

    private final IBinder binder = new TimerBinder();
    private long startTime;
    private static final long TOTAL_TIME = 5 * 60 * 1000; // 5 minutos
    private boolean activo = false;

    public class TimerBinder extends Binder {
        public TokenTimerService getService() {
            return TokenTimerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Recuperar datos guardados
        SharedPreferences prefs = getSharedPreferences("TokenPrefs", MODE_PRIVATE);
        startTime = prefs.getLong("startTime", 0);
        activo = prefs.getBoolean("activo", false);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void iniciarTemporizador() {
        startTime = System.currentTimeMillis();
        activo = true;

        // Guardar en preferencias
        SharedPreferences prefs = getSharedPreferences("TokenPrefs", MODE_PRIVATE);
        prefs.edit()
                .putLong("startTime", startTime)
                .putBoolean("activo", true)
                .apply();
    }

    public void cancelarTemporizador() {
        activo = false;

        // Limpiar preferencias
        SharedPreferences prefs = getSharedPreferences("TokenPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    public boolean isActivo() {
        return activo && getTiempoRestante() > 0;
    }

    public long getTiempoRestante() {
        long elapsed = System.currentTimeMillis() - startTime;
        long restante = TOTAL_TIME - elapsed;
        return Math.max(restante, 0);
    }
}
