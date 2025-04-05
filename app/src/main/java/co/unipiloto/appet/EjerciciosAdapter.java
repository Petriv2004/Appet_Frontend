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

public class EjerciciosAdapter extends RecyclerView.Adapter<EjerciciosAdapter.EjercicioViewHolder> {

    private List<Map<String, String>> listaEjercicios;

    public EjerciciosAdapter(List<Map<String, String>> listaEjercicios) {
        this.listaEjercicios = listaEjercicios;
    }

    @NonNull
    @Override
    public EjercicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ejercicio, parent, false);
        return new EjercicioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EjercicioViewHolder holder, int position) {
        Map<String, String> ejercicio = listaEjercicios.get(position);
        holder.textIdEjercicio.setText("Id del ejercicio: " + ejercicio.get("id_ejercicio"));
        holder.textNombreEjercicio.setText("Ejercicio: " + ejercicio.get("nombre"));
        holder.textDuracionEjercicio.setText("Duración: " + ejercicio.get("duracion") + " min");
        holder.textIntensidadEjercicio.setText("Intensidad: " + ejercicio.get("intensidad"));
        holder.textEspecieEjercicio.setText("Especie: " + ejercicio.get("especie"));
        holder.textDescansoEjercicio.setText("Descanso: " + ejercicio.get("tiempo_descanso") + " min");

        String fotoBase64 = ejercicio.get("imagen");
        if (fotoBase64 != null && !fotoBase64.isEmpty()) {
            byte[] decodedString = Base64.decode(fotoBase64, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.imageViewEjercicio.setImageBitmap(decodedByte);
        } else {
            holder.imageViewEjercicio.setImageResource(R.mipmap.ic_launcher);
        }
    }

    @Override
    public int getItemCount() {
        return listaEjercicios.size();
    }

    static class EjercicioViewHolder extends RecyclerView.ViewHolder {
        TextView textNombreEjercicio, textDuracionEjercicio, textIntensidadEjercicio, textEspecieEjercicio, textDescansoEjercicio, textIdEjercicio;
        ImageView imageViewEjercicio;

        public EjercicioViewHolder(@NonNull View itemView) {
            super(itemView);
            textIdEjercicio = itemView.findViewById(R.id.textIdEjercicio);
            textNombreEjercicio = itemView.findViewById(R.id.textNombreEjercicio);
            textDuracionEjercicio = itemView.findViewById(R.id.textDuracion);
            textIntensidadEjercicio = itemView.findViewById(R.id.textIntensidad);
            textEspecieEjercicio = itemView.findViewById(R.id.textEspecie);
            textDescansoEjercicio = itemView.findViewById(R.id.textDescanso);
            imageViewEjercicio = itemView.findViewById(R.id.imageViewEjercicio);
        }
    }
}
