package co.unipiloto.appet;

import org.json.JSONObject;

public class Recorrido {
    private String latitud;
    private String longitud;
    private String rango;
    private String fecha;

    public Recorrido(String latitud, String longitud, String rango, String fecha) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.rango = rango;
        this.fecha = fecha;
    }

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("latitud", latitud);
            jsonObject.put("longitud", longitud);
            jsonObject.put("rango", rango);
            jsonObject.put("fecha", fecha);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public String getRango() {
        return rango;
    }

    public void setRango(String rango) {
        this.rango = rango;
    }

    public String toString() {
        return "Latitud: " + latitud + ", Longitud: " + longitud + ", Rango: " + rango + ", Fecha: " + fecha;
    }
}
