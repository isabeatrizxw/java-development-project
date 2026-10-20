import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.concurrent.*;

/** Responde JOptionPanes reais automaticamente, na ordem da fila, registrando o que foi exibido. */
public class DialogBot {
    public record Visto(String titulo, String mensagem, Object inicial, java.util.List<String> opcoes) {}
    public interface Acao { void aplicar(JOptionPane p); }
    private static final BlockingQueue<Acao> fila = new LinkedBlockingQueue<>();
    public static final java.util.List<Visto> vistos = Collections.synchronizedList(new ArrayList<>());
    private static final Set<JOptionPane> tratados = Collections.newSetFromMap(new IdentityHashMap<>());

    public static Acao digita(String texto) { return p -> { p.setInputValue(texto); p.setValue(JOptionPane.OK_OPTION); }; }
    public static Acao mantem() { return p -> { Object i = p.getInitialSelectionValue(); p.setInputValue(i == null ? null : i.toString()); p.setValue(JOptionPane.OK_OPTION); }; }
    public static Acao escolhe(String rotulo) { return p -> {
        Object alvo = Arrays.stream(p.getSelectionValues()).filter(o -> o.toString().equals(rotulo)).findFirst()
                .orElseThrow(() -> new IllegalStateException("opção não existe: " + rotulo));
        p.setInputValue(alvo); p.setValue(JOptionPane.OK_OPTION); }; }
    public static Acao ok() { return p -> p.setValue(JOptionPane.OK_OPTION); }
    public static Acao cancela() { return p -> p.setValue(JOptionPane.CANCEL_OPTION); }

    public static void responder(Acao... acoes) { fila.addAll(Arrays.asList(acoes)); }
    public static int pendentes() { return fila.size(); }

    static {
        Thread t = new Thread(() -> {
            while (true) {
                try { Thread.sleep(40); } catch (InterruptedException e) { return; }
                SwingUtilities.invokeLater(DialogBot::varrer);
            }
        }, "dialog-bot");
        t.setDaemon(true);
        t.start();
    }

    private static void varrer() {
        for (Window w : Window.getWindows()) {
            if (!(w instanceof JDialog d) || !d.isShowing()) continue;
            JOptionPane p = achar(d.getContentPane());
            if (p == null || tratados.contains(p)) continue;
            tratados.add(p);
            Object[] sv = p.getSelectionValues();
            vistos.add(new Visto(d.getTitle(), String.valueOf(p.getMessage()), p.getInitialSelectionValue(),
                    sv == null ? null : Arrays.stream(sv).map(Object::toString).toList()));
            Acao a = fila.poll();
            if (a == null) { System.out.println("!! diálogo sem resposta: " + p.getMessage()); a = cancela(); }
            a.aplicar(p);
        }
    }

    private static JOptionPane achar(Container c) {
        if (c instanceof JOptionPane p) return p;
        for (Component k : c.getComponents()) if (k instanceof Container cc) { JOptionPane r = achar(cc); if (r != null) return r; }
        return null;
    }
}
