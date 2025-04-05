package co.unipiloto.appet;

public class Ritmo {

    private int ritmoCardiaco;
    private String fecha;
    private String hora;

    public Ritmo(int ritmoCardiaco, String fecha, String hora) {
        this.ritmoCardiaco = ritmoCardiaco;
        this.fecha = fecha;
        this.hora = hora;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public int getRitmoCardiaco() {
        return ritmoCardiaco;
    }

    public void setRitmoCardiaco(int ritmoCardiaco) {
        this.ritmoCardiaco = ritmoCardiaco;
    }

    @Override
    public String toString() {
        return "Ritmo{" +
                "ritmoCardiaco=" + ritmoCardiaco +
                ", fecha='" + fecha + '\'' +
                ", hora='" + hora + '\'' +
                '}';
    }
}
