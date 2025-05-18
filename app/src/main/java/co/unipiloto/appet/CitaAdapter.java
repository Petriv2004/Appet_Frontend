package co.unipiloto.appet;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class CitaAdapter extends RecyclerView.Adapter<CitaAdapter.CitaViewHolder> {

    private List<Map<String, String>> listaCitas;
    private OnCitaUpdateListener updateListener;

    public CitaAdapter(List<Map<String, String>> listaCitas, OnCitaUpdateListener updateListener) {
        this.listaCitas = listaCitas;
        this.updateListener = updateListener;
    }

    @NonNull
    @Override
    public CitaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cita, parent, false);
        return new CitaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CitaViewHolder holder, int position) {
        Map<String, String> cita = listaCitas.get(position);
        holder.textIdCita.setText("ID Cita: " + cita.get("id_agenda"));
        holder.textNombreMascota.setText("Mascota: " + cita.get("nombre_mascota"));
        holder.textFecha.setText("Fecha: " + cita.get("fecha"));
        holder.textHora.setText("Hora: " + cita.get("hora"));
        holder.textRazon.setText("Razón: " + cita.get("razon"));
        holder.textDescripcion.setText("Descripción: " + cita.get("descripcion"));

        SharedPreferences prefs = holder.itemView.getContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        String correoUsuario = prefs.getString("correo", null);
        String correoVet = prefs.getString("correoVet", null);
        if (correoUsuario != null) {
            holder.btnAsistida.setVisibility(View.GONE);
        } else if (correoVet != null) {
            holder.btnAsistida.setVisibility(View.VISIBLE);
        }

        if ("true".equals(cita.get("asistencia"))) {
            holder.textAsistencia.setText("Estado: Asistido");
            holder.btnAsistida.setVisibility(View.GONE);
        } else if(correoVet != null) {
            holder.textAsistencia.setText("Estado: No asistido");
            holder.btnAsistida.setVisibility(View.VISIBLE);
        }else{
            holder.textAsistencia.setText("Estado: No asistido");
            holder.btnAsistida.setVisibility(View.GONE);
        }

        boolean recordado = prefs.getBoolean("reminder_"+cita.get("id_agenda"), false);
        holder.btnRecordatorio.setText(recordado ? "No recordarme" : "Recordarme");


        holder.btnRecordatorio.setOnClickListener(v ->
                updateListener.onToggleRecordatorio(cita.get("id_agenda"), position)
        );


        holder.btnAsistida.setOnClickListener(v -> {
            String idParaActualizar = cita.get("id_agenda");
            if (updateListener != null) {
                updateListener.onMarcarAsistida(idParaActualizar, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaCitas.size();
    }

    static class CitaViewHolder extends RecyclerView.ViewHolder {
        TextView textIdCita, textNombreMascota, textFecha, textHora, textRazon, textDescripcion, textAsistencia;
        Button btnAsistida, btnRecordatorio;

        public CitaViewHolder(@NonNull View itemView) {
            super(itemView);
            textIdCita = itemView.findViewById(R.id.textIdCita);
            textNombreMascota = itemView.findViewById(R.id.textNombreMascota);
            textFecha = itemView.findViewById(R.id.textFecha);
            textHora = itemView.findViewById(R.id.textHora);
            textRazon = itemView.findViewById(R.id.textRazon);
            textDescripcion = itemView.findViewById(R.id.textDescripcion);
            textAsistencia = itemView.findViewById(R.id.textAsistencia);
            btnAsistida = itemView.findViewById(R.id.btnAsistida);
            btnRecordatorio = itemView.findViewById(R.id.btnRecordatorio);
        }
    }
}
