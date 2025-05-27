package backend;

import java.util.Arrays;

public class PreguntaEleccionMultiple extends Pregunta {
    private String[] opciones;
    private int correcta;

    public PreguntaEleccionMultiple(String enunciado, String nivelBloom,
                                    int tiempoEstimado, String asignatura,
                                    String[] opciones, int correcta) {
        super(enunciado, nivelBloom, tiempoEstimado, asignatura);
        this.opciones = opciones;
        this.correcta = correcta;
    }

    @Override
    public String[] getOpciones() { return opciones; }

    @Override
    public String getRespuestaCorrecta() {
        return opciones[correcta];
    }

    @Override
    public String toCSV() {
        // enunciado;nivel;tiempo;asignatura;MC;op1|op2|...;correcta
        String opts = String.join("|", opciones);
        return String.format("%s;%s;%d;%s;MC;%s;%d",
                enunciado, nivelBloom, tiempoEstimado,
                asignatura, opts, correcta);
    }
}
