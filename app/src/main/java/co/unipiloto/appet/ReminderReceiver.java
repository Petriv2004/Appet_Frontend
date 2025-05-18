package co.unipiloto.appet;

import static android.content.ContentValues.TAG;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "citas_channel";

    @Override
    public void onReceive(Context ctx, Intent it) {
        String fecha       = it.getStringExtra("fecha");
        String hora        = it.getStringExtra("hora");
        String mascota     = it.getStringExtra("mascota");
        String descripcion = it.getStringExtra("descripcion");
        int notifId        = it.getIntExtra("notifId", 0);

        NotificationManager nm =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Citas Agenda", NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(ch);
        }

        // 1) Intent que abrirá tu actividad al tocar la notificación
        Intent appIntent = new Intent(ctx, PerfilAgenda.class);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(
                ctx,
                notifId,  // requestCode único por notificación
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 2) Construye la notificación con el contentIntent
        NotificationCompat.Builder nb = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Cita: " + mascota)
                .setContentText(fecha + " " + hora + " – " + descripcion)
                .setContentIntent(contentIntent)      // aquí lo añades
                .setAutoCancel(true);                  // se borra al tocarla

        // 3) Muestra la notificación
        nm.notify(notifId, nb.build());
    }
}


