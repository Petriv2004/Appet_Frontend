package co.unipiloto.appet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.SharedPreferences;
import java.util.Map;
import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        SharedPreferences prefs = ctx.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

        for (Map.Entry<String, ?> e : prefs.getAll().entrySet()) {
            String key = e.getKey();
            if (!key.startsWith("reminder_")) continue;
            String idCita = key.substring("reminder_".length());
            boolean activo = prefs.getBoolean(key, false);
            if (!activo) continue;

            String fecha       = prefs.getString("fecha_"       + idCita, null);
            String hora        = prefs.getString("hora_"        + idCita, null);
            String mascota     = prefs.getString("mascota_"     + idCita, null);
            String descripcion = prefs.getString("descripcion_" + idCita, null);
            if (fecha == null || hora == null) continue;

            Intent i = new Intent(ctx, ReminderReceiver.class)
                    .putExtra("fecha", fecha)
                    .putExtra("hora", hora)
                    .putExtra("mascota", mascota)
                    .putExtra("descripcion", descripcion);

            Calendar calCita = parseFechaHora(fecha, hora);
            Calendar calAntes = (Calendar) calCita.clone();
            calAntes.add(Calendar.HOUR_OF_DAY, -1);

            PendingIntent piAntes = PendingIntent.getBroadcast(
                    ctx,
                    idCita.hashCode()*10,
                    i.putExtra("notifId", idCita.hashCode()*10),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    calAntes.getTimeInMillis(), piAntes);

            PendingIntent piHora = PendingIntent.getBroadcast(
                    ctx,
                    idCita.hashCode()*10+1,
                    i.putExtra("notifId", idCita.hashCode()*10+1),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    calCita.getTimeInMillis(), piHora);
        }
    }

    private Calendar parseFechaHora(String fecha, String hora) {
        String[] f = fecha.split("-"), h = hora.split(":");
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR,   Integer.parseInt(f[0]));
        c.set(Calendar.MONTH,  Integer.parseInt(f[1]) - 1);
        c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(f[2]));
        c.set(Calendar.HOUR_OF_DAY,   Integer.parseInt(h[0]));
        c.set(Calendar.MINUTE,        Integer.parseInt(h[1]));
        c.set(Calendar.SECOND,        0);
        return c;
    }
}
