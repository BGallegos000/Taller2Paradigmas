package backend;

public abstract class Pregunta {
    protected String enunciado;
    protected String nivelBloom;
    protected int tiempoEstimado;
    protected String asignatura;

    public Pregunta(String enunciado, String nivelBloom, int tiempoEstimado, String asignatura) {
        this.enunciado = enunciado;
        this.nivelBloom = nivelBloom;
        this.tiempoEstimado = tiempoEstimado;
        this.asignatura = asignatura;
    }

    public String getEnunciado() { return enunciado; }
    public String getNivelBloom() { return nivelBloom; }
    public int getTiempoEstimado() { return tiempoEstimado; }
    public String getAsignatura() { return asignatura; }

    // Para mostrar en revisión
    public abstract String[] getOpciones();
    public abstract String getRespuestaCorrecta();
    // Para cargar/el archivo
    public abstract String toCSV();
}
