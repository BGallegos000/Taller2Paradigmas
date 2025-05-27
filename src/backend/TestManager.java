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
    public void loadFromFile(File f) throws IOException {
        items.clear();
        respuestas.clear();
        current = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            // 1) Leer y descartar la cabecera
            String linea = br.readLine();
            // 2) Procesar resto de líneas
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] p = linea.split(";");
                String enun    = p[0];
                String nivel   = p[1];
                int tiempo     = Integer.parseInt(p[2]);
                String asig    = p[3];
                String tipo    = p[4];

                if ("MC".equals(tipo)) {
                    String[] ops = p[5].split("\\|");
                    int corr     = Integer.parseInt(p[6]);
                    items.add(new PreguntaEleccionMultiple(enun, nivel, tiempo, asig, ops, corr));
                }
                else if ("TF".equals(tipo)) {
                    boolean corr = Boolean.parseBoolean(p[6]);
                    items.add(new PreguntaVerdaderoFalso(enun, nivel, tiempo, asig, corr));
                }
                else {
                    throw new IOException("Tipo desconocido en CSV: " + tipo);
                }
            }
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
