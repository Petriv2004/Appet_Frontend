package co.unipiloto.appet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class CitaAdapter extends RecyclerView.Adapter<CitaAdapter.CitaViewHolder> {

    private List<Map<String, String>> listaCitas;

    public CitaAdapter(List<Map<String, String>> listaCitas) {
        this.listaCitas = listaCitas;
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
    }

    @Override
    public int getItemCount() {
        return listaCitas.size();
    }

    static class CitaViewHolder extends RecyclerView.ViewHolder {
        TextView textIdCita, textNombreMascota, textFecha, textHora, textRazon, textDescripcion;

        public CitaViewHolder(@NonNull View itemView) {
            super(itemView);
            textIdCita = itemView.findViewById(R.id.textIdCita);
            textNombreMascota = itemView.findViewById(R.id.textNombreMascota);
            textFecha = itemView.findViewById(R.id.textFecha);
            textHora = itemView.findViewById(R.id.textHora);
            textRazon = itemView.findViewById(R.id.textRazon);
            textDescripcion = itemView.findViewById(R.id.textDescripcion);
        }
    }
}
