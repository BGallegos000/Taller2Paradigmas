package backend;

public class PreguntaVerdaderoFalso extends Pregunta {
    private boolean correcta;

    public PreguntaVerdaderoFalso(String enunciado, String nivelBloom,
                                  int tiempoEstimado, String asignatura,
                                  boolean correcta) {
        super(enunciado, nivelBloom, tiempoEstimado, asignatura);
        this.correcta = correcta;
    }

    @Override
    public String[] getOpciones() {
        return new String[] { "Verdadero", "Falso" };
    }

    @Override
    public String getRespuestaCorrecta() {
        return correcta ? "Verdadero" : "Falso";
    }

    @Override
    public String toCSV() {
        // enunciado;nivel;tiempo;asignatura;TF;;true/false
        return String.format("%s;%s;%d;%s;TF;;%b",
                enunciado, nivelBloom, tiempoEstimado,
                asignatura, correcta);
    }
}
