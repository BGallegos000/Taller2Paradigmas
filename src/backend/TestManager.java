package backend;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class TestManager {
    private List<Pregunta> items = new ArrayList<>();
    private Map<Integer, String> respuestas = new HashMap<>();
    private int current = 0;
    private List<TestEventListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(TestEventListener l) {
        listeners.add(l);
    }

    private void notifyLoaded() {
        for (TestEventListener l : listeners) l.onItemsLoaded();
    }
    private void notifyChanged() {
        for (TestEventListener l : listeners) l.onQuestionChanged(current);
    }
    private void notifyResults() {
        for (TestEventListener l : listeners) l.onResultsReady();
    }

    /** Carga preguntas desde un CSV con formato:
     *  enunciado;nivelBloom;tiempoEstimado;asignatura;MC|TF;opciones|ó vacío;correcta
     */
    public void loadFromFile(File f, String asignaturaSeleccionada) throws IOException {
        items.clear();
        respuestas.clear();
        current = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            // Leer y descartar la cabecera
            String linea = br.readLine();
            // Procesar el resto de las líneas
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] p = linea.split(";");

                // Si la línea no tiene suficientes datos, la ignoramos
                if (p.length < 7) {
                    System.err.println("Línea mal formada (menos de 7 columnas): " + linea);
                    continue;
                }

                String enun    = p[0];
                String nivel   = p[1];
                String tiempoStr = p[2];  // Tiempo como String
                String asig    = p[3];
                String tipo    = p[4];

                // Verificar si la asignatura coincide con la seleccionada
                if (!asig.equals(asignaturaSeleccionada)) {
                    continue; // Si la asignatura no coincide, omitir esta pregunta
                }

                // Manejar tiempo vacío
                int tiempo = 0;
                try {
                    if (!tiempoStr.trim().isEmpty()) {
                        tiempo = Integer.parseInt(tiempoStr);
                    } else {
                        tiempo = 5; // Valor por defecto
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Error al parsear tiempo en la línea: " + linea);
                    continue;
                }

                // Procesar las preguntas de tipo MC o TF
                if ("MC".equals(tipo)) {
                    String[] ops = p[5].split("\\|");
                    String respuestaCorrectaStr = p[6];  // Respuesta correcta como String
                    int corr = 0;
                    try {
                        if (!respuestaCorrectaStr.trim().isEmpty()) {
                            corr = Integer.parseInt(respuestaCorrectaStr);
                        } else {
                            corr = 1; // Valor por defecto
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Error al parsear respuesta correcta en la línea: " + linea);
                        continue;
                    }
                    items.add(new PreguntaEleccionMultiple(enun, nivel, tiempo, asig, ops, corr));
                } else if ("TF".equals(tipo)) {
                    String respuestaCorrectaStr = p[6];  // Respuesta correcta como String
                    boolean corr = false;
                    if (!respuestaCorrectaStr.trim().isEmpty()) {
                        corr = Boolean.parseBoolean(respuestaCorrectaStr);
                    }
                    items.add(new PreguntaVerdaderoFalso(enun, nivel, tiempo, asig, corr));
                } else {
                    System.err.println("Tipo desconocido en CSV: " + tipo);
                    continue;
                }
            }
        } catch (IOException e) {
            throw new IOException("Error al leer el archivo: " + e.getMessage());
        }

        // Si no se encontraron preguntas para la asignatura seleccionada
        if (items.isEmpty()) {
            System.err.println("No se encontraron preguntas para la asignatura seleccionada.");
        }

        notifyLoaded();
    }


    public int totalItems() { return items.size(); }
    public int totalTime()  { return items.stream().mapToInt(Pregunta::getTiempoEstimado).sum(); }

    public void startTest() { notifyChanged(); }
    public void next()      { if (current < items.size()-1) { current++; notifyChanged(); }}
    public void prev()      { if (current > 0) { current--; notifyChanged(); }}

    public boolean isFirst() { return current == 0; }
    public boolean isLast()  { return current == items.size()-1; }

    public Pregunta getCurrent()     { return items.get(current); }
    public int      getCurrentIndex(){ return current; }

    public void setAnswer(int idx, String ans) { respuestas.put(idx, ans); }
    public String getAnswer(int idx)           { return respuestas.getOrDefault(idx, ""); }

    public Map<String,Double> pctByNivel() {
        Map<String,Integer> tot = new HashMap<>(), corr = new HashMap<>();
        for (int i = 0; i < items.size(); i++) {
            String n = items.get(i).getNivelBloom();
            tot.merge(n,1,Integer::sum);
            if (items.get(i).getRespuestaCorrecta().equals(respuestas.get(i)))
                corr.merge(n,1,Integer::sum);
        }
        Map<String,Double> pct = new LinkedHashMap<>();
        for (String n : tot.keySet())
            pct.put(n, corr.getOrDefault(n,0)*100.0/tot.get(n));
        return pct;
    }

    public Map<String,Double> pctByType() {
        Map<String,Integer> tot = new HashMap<>(), corr = new HashMap<>();
        for (int i = 0; i < items.size(); i++) {
            String tipo = items.get(i) instanceof PreguntaEleccionMultiple ? "MC" : "TF";
            tot.merge(tipo,1,Integer::sum);
            if (items.get(i).getRespuestaCorrecta().equals(respuestas.get(i)))
                corr.merge(tipo,1,Integer::sum);
        }
        Map<String,Double> pct = new LinkedHashMap<>();
        for (String t : tot.keySet())
            pct.put(t, corr.getOrDefault(t,0)*100.0/tot.get(t));
        return pct;
    }

    public void submit() { notifyResults(); }

    /** Permite obtener cualquier pregunta para revisión */
    public Pregunta getItemAt(int index) {
        return items.get(index);
    }
}
