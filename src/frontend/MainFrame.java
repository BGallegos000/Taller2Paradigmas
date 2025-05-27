package frontend;

import backend.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Collections;
import java.util.Map;

public class MainFrame extends JFrame implements TestEventListener {
    private TestManager manager = new TestManager();
    private CardLayout cards = new CardLayout();
    private JPanel main = new JPanel(cards);

    // Panels
    private JPanel loadP, summaryP, quizP, resultsP, reviewP;
    private JLabel lblCount, lblTime, lblQ;
    private JPanel optsP;
    private ButtonGroup grp;
    private JButton btnPrev, btnNext;

    // Results & review
    private JTextArea txtResults;
    private int reviewIdx = 0;

    public MainFrame() {
        super("Pruebas Taxonomía de Bloom");
        setSize(800,600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        manager.addListener(this);

        createLoadPanel();
        createSummaryPanel();
        createQuizPanel();
        createResultsPanel();
        createReviewPanel();

        main.add(loadP,    "LOAD");
        main.add(summaryP, "SUMMARY");
        main.add(quizP,    "QUIZ");
        main.add(resultsP, "RESULTS");
        main.add(reviewP,  "REVIEW");
        add(main);
        cards.show(main, "LOAD");
    }

    private void createLoadPanel() {
        loadP = new JPanel(new BorderLayout());
        JLabel msg = new JLabel("Seleccione archivo CSV", SwingConstants.CENTER);
        JButton btn = new JButton("Cargar ítems");
        btn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    manager.loadFromFile(fc.getSelectedFile());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(),
                            "Error al cargar", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        loadP.add(msg, BorderLayout.CENTER);
        loadP.add(btn, BorderLayout.SOUTH);
    }

    private void createSummaryPanel() {
        summaryP = new JPanel(new GridBagLayout());
        lblCount = new JLabel();
        lblTime  = new JLabel();
        JButton start = new JButton("Iniciar prueba");
        start.addActionListener(e -> manager.startTest());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10,10,10,10);
        c.gridy = 0; summaryP.add(lblCount, c);
        c.gridy = 1; summaryP.add(lblTime, c);
        c.gridy = 2; summaryP.add(start, c);
    }

    private void createQuizPanel() {
        quizP = new JPanel(new BorderLayout());
        lblQ = new JLabel("", SwingConstants.CENTER);
        optsP = new JPanel();
        btnPrev = new JButton("Atrás");
        btnNext = new JButton("Siguiente");
        btnPrev.addActionListener(e -> { saveAnswer(); manager.prev(); });
        btnNext.addActionListener(e -> {
            saveAnswer();
            if (manager.isLast()) manager.submit();
            else manager.next();
        });
        JPanel nav = new JPanel();
        nav.add(btnPrev);
        nav.add(btnNext);
        quizP.add(lblQ, BorderLayout.NORTH);
        quizP.add(optsP, BorderLayout.CENTER);
        quizP.add(nav, BorderLayout.SOUTH);
    }

    private void createResultsPanel() {
        resultsP = new JPanel(new BorderLayout());
        txtResults = new JTextArea();
        txtResults.setEditable(false);

        // Botones: Revisar, Guardar, Salir
        JButton rev  = new JButton("Revisar respuestas");
        JButton save = new JButton("Guardar resultados");
        JButton exit = new JButton("Salir");

        rev.addActionListener(e -> {
            reviewIdx = 0;
            updateReview();
            cards.show(main, "REVIEW");
        });
        save.addActionListener(e -> saveResults());
        exit.addActionListener(e -> System.exit(0));

        JPanel south = new JPanel();
        south.add(rev);
        south.add(save);
        south.add(exit);

        resultsP.add(new JScrollPane(txtResults), BorderLayout.CENTER);
        resultsP.add(south, BorderLayout.SOUTH);
    }

    private void createReviewPanel() {
        reviewP = new JPanel(new BorderLayout());
        JLabel lblReview = new JLabel("", SwingConstants.CENTER);
        JLabel lblYour   = new JLabel();
        JLabel lblCorr   = new JLabel();
        JButton bPrev    = new JButton("Atrás");
        JButton bNext    = new JButton("Siguiente");
        JButton bBack    = new JButton("Volver al resumen");

        bPrev.addActionListener(e -> {
            if (reviewIdx > 0) {
                reviewIdx--;
                updateReview();
            }
        });
        bNext.addActionListener(e -> {
            if (reviewIdx < manager.totalItems()-1) {
                reviewIdx++;
                updateReview();
            }
        });
        bBack.addActionListener(e -> cards.show(main, "RESULTS"));

        JPanel center = new JPanel(new GridLayout(2,1));
        center.add(lblYour);
        center.add(lblCorr);

        JPanel nav = new JPanel();
        nav.add(bPrev);
        nav.add(bNext);
        nav.add(bBack);

        reviewP.add(lblReview, BorderLayout.NORTH);
        reviewP.add(center,    BorderLayout.CENTER);
        reviewP.add(nav,       BorderLayout.SOUTH);

        // Guardamos referencias para updateReview
        reviewP.putClientProperty("lblReview", lblReview);
        reviewP.putClientProperty("lblYour",   lblYour);
        reviewP.putClientProperty("lblCorr",   lblCorr);
    }

    private void saveAnswer() {
        for (AbstractButton b : Collections.list(grp.getElements())) {
            if (b.isSelected()) {
                manager.setAnswer(manager.getCurrentIndex(), b.getText());
            }
        }
    }

    private void updateQuiz() {
        Pregunta q = manager.getCurrent();
        lblQ.setText("<html><body style='width:700px'>" + q.getEnunciado() + "</body></html>");
        optsP.removeAll();
        grp = new ButtonGroup();
        optsP.setLayout(new BoxLayout(optsP, BoxLayout.Y_AXIS));
        for (String o : q.getOpciones()) {
            JRadioButton rb = new JRadioButton(o);
            if (o.equals(manager.getAnswer(manager.getCurrentIndex()))) {
                rb.setSelected(true);
            }
            grp.add(rb);
            optsP.add(rb);
        }
        btnPrev.setEnabled(!manager.isFirst());
        btnNext.setText(manager.isLast() ? "Enviar respuestas" : "Siguiente");
        optsP.revalidate();
        optsP.repaint();
    }

    private void updateResults() {
        StringBuilder sb = new StringBuilder("=== Resumen ===\n\n");
        sb.append("Por nivel Bloom:\n");
        for (Map.Entry<String, Double> e : manager.pctByNivel().entrySet()) {
            sb.append(String.format("• %s: %.2f%%%n", e.getKey(), e.getValue()));
        }
        sb.append("\nPor tipo ítem:\n");
        for (Map.Entry<String, Double> e : manager.pctByType().entrySet()) {
            sb.append(String.format("• %s: %.2f%%%n", e.getKey(), e.getValue()));
        }
        txtResults.setText(sb.toString());
    }

    private void updateReview() {
        JLabel lblReview = (JLabel) reviewP.getClientProperty("lblReview");
        JLabel lblYour   = (JLabel) reviewP.getClientProperty("lblYour");
        JLabel lblCorr   = (JLabel) reviewP.getClientProperty("lblCorr");

        Pregunta q = manager.getItemAt(reviewIdx);
        lblReview.setText("<html><body style='width:700px'>" + q.getEnunciado() + "</body></html>");
        String your = manager.getAnswer(reviewIdx);
        lblYour.setText("Tu respuesta: " + (your.isEmpty() ? "—" : your));
        lblCorr.setText("Correcta: " + q.getRespuestaCorrecta());
    }

    /** Abre un diálogo para guardar el resumen y la revisión en un archivo de texto */
    private void saveResults() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar resultados");
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File out = fc.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(new FileWriter(out))) {
                pw.println("=== Resultados de la prueba ===\n");
                pw.println("Por nivel Bloom:");
                for (Map.Entry<String, Double> e : manager.pctByNivel().entrySet()) {
                    pw.printf("%s: %.2f%%%n", e.getKey(), e.getValue());
                }
                pw.println("\nPor tipo ítem:");
                for (Map.Entry<String, Double> e : manager.pctByType().entrySet()) {
                    pw.printf("%s: %.2f%%%n", e.getKey(), e.getValue());
                }
                pw.println("\n=== Revisión detallada ===\n");
                for (int i = 0; i < manager.totalItems(); i++) {
                    Pregunta q = manager.getItemAt(i);
                    String your = manager.getAnswer(i);
                    pw.printf("Pregunta %d: %s%n", i+1, q.getEnunciado());
                    pw.printf("Tu respuesta: %s%n", your.isEmpty() ? "—" : your);
                    pw.printf("Correcta: %s%n%n", q.getRespuestaCorrecta());
                }
                JOptionPane.showMessageDialog(this,
                        "Resultados guardados en:\n" + out.getAbsolutePath());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al guardar: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override public void onItemsLoaded() {
        SwingUtilities.invokeLater(() -> {
            lblCount.setText("Ítems: " + manager.totalItems());
            lblTime .setText("Tiempo total: " + manager.totalTime() + " min");
            cards.show(main, "SUMMARY");
        });
    }
    @Override public void onQuestionChanged(int idx) {
        SwingUtilities.invokeLater(() -> {
            updateQuiz();
            cards.show(main, "QUIZ");
        });
    }
    @Override public void onResultsReady() {
        SwingUtilities.invokeLater(() -> {
            updateResults();
            cards.show(main, "RESULTS");
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
