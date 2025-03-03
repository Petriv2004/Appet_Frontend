package co.unipiloto.appet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class MascotaAdapter extends RecyclerView.Adapter<MascotaAdapter.MascotaViewHolder> {

    private List<Map<String, String>> listaMascotas;

    public MascotaAdapter(List<Map<String, String>> listaMascotas) {
        this.listaMascotas = listaMascotas;
    }

    @NonNull
    @Override
    public MascotaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mascota, parent, false);
        return new MascotaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MascotaViewHolder holder, int position) {
        Map<String, String> mascota = listaMascotas.get(position);
        holder.textIdMascota.setText("ID: " + mascota.get("id_mascota"));
        holder.textNombreMascota.setText("Nombre: " + mascota.get("nombre"));
        holder.textTipoMascota.setText("Tipo: " + mascota.get("tipo"));
        holder.textRazaMascota.setText("Raza: " + mascota.get("raza"));
        holder.textFechaNacimientoMascota.setText("Fecha Nacimiento: " + mascota.get("fecha_nacimiento"));

        holder.textEnfermedadesMascota.setText("Enfermedades: " + (mascota.containsKey("enfermedades") ? mascota.get("enfermedades") : "N/A"));
        holder.textMedicinasMascota.setText("Medicinas: " + (mascota.containsKey("medicinas") ? mascota.get("medicinas") : "N/A"));
        holder.textSangreMascota.setText("Tipo de Sangre: " + (mascota.containsKey("sangre") ? mascota.get("sangre") : "N/A"));
        holder.textPesoMascota.setText("Peso: " + (mascota.containsKey("peso") ? mascota.get("peso") + " kg" : "N/A"));
        holder.textVacunasMascota.setText("Vacunas: " + (mascota.containsKey("vacunas") ? mascota.get("vacunas") : "N/A"));

        String fotoBase64 = mascota.get("foto");
        if (fotoBase64 != null && !fotoBase64.isEmpty()) {
            byte[] decodedString = Base64.decode(fotoBase64, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.imageViewMascota.setImageBitmap(decodedByte);
        } else {
            holder.imageViewMascota.setImageResource(R.mipmap.ic_launcher);
        }
    }

    @Override
    public int getItemCount() {
        return listaMascotas.size();
    }

    static class MascotaViewHolder extends RecyclerView.ViewHolder {
        TextView textIdMascota, textNombreMascota, textTipoMascota, textRazaMascota, textFechaNacimientoMascota;
        TextView textEnfermedadesMascota, textMedicinasMascota, textSangreMascota, textPesoMascota, textVacunasMascota;
        ImageView imageViewMascota;

        public MascotaViewHolder(@NonNull View itemView) {
            super(itemView);
            textIdMascota = itemView.findViewById(R.id.textIdMascota);
            textNombreMascota = itemView.findViewById(R.id.textNombreMascota);
            textTipoMascota = itemView.findViewById(R.id.textTipoMascota);
            textRazaMascota = itemView.findViewById(R.id.textRazaMascota);
            textFechaNacimientoMascota = itemView.findViewById(R.id.textFechaNacimientoMascota);

            textEnfermedadesMascota = itemView.findViewById(R.id.textEnfermedadesMascota);
            textMedicinasMascota = itemView.findViewById(R.id.textMedicinasMascota);
            textSangreMascota = itemView.findViewById(R.id.textSangreMascota);
            textPesoMascota = itemView.findViewById(R.id.textPesoMascota);
            textVacunasMascota = itemView.findViewById(R.id.textVacunasMascota);

            imageViewMascota = itemView.findViewById(R.id.imageViewMascota);
        }
    }
}
