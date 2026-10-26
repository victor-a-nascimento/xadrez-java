import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Parte gráfica: desenha o tabuleiro, trata os cliques do mouse e mostra o status da partida.
 */
public final class PainelTabuleiro extends JPanel {
    private static final Color CLARA = new Color(240, 217, 181);
    private static final Color ESCURA = new Color(181, 136, 99);

    private Board board = Board.novo();
    private final Deque<Board> historico = new ArrayDeque<>();
    private final Deque<Move> historicoLances = new ArrayDeque<>();
    private List<Move> legais;
    private int selR = -1, selC = -1;
    private Move ultimo;
    private final JLabel status;
    private final Font fontePecas;
    private int cell, ox, oy;

    PainelTabuleiro(JLabel status) {
        this.status = status;
        this.fontePecas = escolherFonte();
        setPreferredSize(new Dimension(640, 640));
        setBackground(new Color(49, 46, 43));
        atualizarStatus();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                clicar(e.getX(), e.getY());
            }
        });
    }

    private Font escolherFonte() {
        String[] candidatas = {"Segoe UI Symbol", "Apple Symbols", "DejaVu Sans",
                "Noto Sans Symbols2", "Arial Unicode MS", "SansSerif"};
        for (String nome : candidatas) {
            Font f = new Font(nome, Font.PLAIN, 40);
            if (f.canDisplay('\u265A')) return f;
        }
        return new Font("SansSerif", Font.PLAIN, 40);
    }

    void novoJogo() {
        board = Board.novo();
        historico.clear();
        historicoLances.clear();
        ultimo = null;
        selR = selC = -1;
        atualizarStatus();
        repaint();
    }

    void desfazer() {
        if (historico.isEmpty()) return;
        board = historico.pop();
        historicoLances.pop();
        ultimo = historicoLances.peek();
        selR = selC = -1;
        atualizarStatus();
        repaint();
    }

    private void atualizarStatus() {
        legais = board.legais();
        boolean xeque = board.emXeque(board.whiteToMove);
        if (legais.isEmpty()) {
            if (xeque) {
                status.setText("Xeque-mate! " + (board.whiteToMove ? "As pretas" : "As brancas") + " vencem.");
            } else {
                status.setText("Empate por afogamento.");
            }
        } else {
            status.setText("Vez das " + (board.whiteToMove ? "brancas" : "pretas") + (xeque ? " - XEQUE!" : ""));
        }
    }

    private void clicar(int x, int y) {
        if (legais.isEmpty() || cell == 0) return; // fim de jogo
        int c = (x - ox) / cell;
        int r = (y - oy) / cell;
        if (x < ox || y < oy || !Board.dentro(r, c)) return;

        // já existe uma peça selecionada: tenta mover
        if (selR >= 0) {
            List<Move> candidatos = new ArrayList<>();
            for (Move m : legais) {
                if (m.fr == selR && m.fc == selC && m.tr == r && m.tc == c) candidatos.add(m);
            }
            if (!candidatos.isEmpty()) {
                Move escolhido = candidatos.get(0);
                if (escolhido.promo != '\0') escolhido = escolherPromocao(candidatos);
                historico.push(board.copy());
                historicoLances.push(escolhido);
                board.aplicar(escolhido);
                ultimo = escolhido;
                selR = selC = -1;
                atualizarStatus();
                repaint();
                return;
            }
        }
        // seleciona uma peça da vez
        if (Board.daCor(board.sq[r][c], board.whiteToMove)) {
            selR = r;
            selC = c;
        } else {
            selR = selC = -1;
        }
        repaint();
    }

    private Move escolherPromocao(List<Move> candidatos) {
        String[] opcoes = {"Dama", "Torre", "Bispo", "Cavalo"};
        int i = JOptionPane.showOptionDialog(this, "Promover o peão para:", "Promoção",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes[0]);
        char desejada = "qrbn".charAt(i < 0 ? 0 : i);
        for (Move m : candidatos) {
            if (m.promo == desejada) return m;
        }
        return candidatos.get(0);
    }

    private String glifo(char p) {
        switch (Character.toLowerCase(p)) {
            case 'k': return "\u265A";
            case 'q': return "\u265B";
            case 'r': return "\u265C";
            case 'b': return "\u265D";
            case 'n': return "\u265E";
            default:  return "\u265F";
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        cell = Math.min(getWidth(), getHeight()) / 8;
        ox = (getWidth() - cell * 8) / 2;
        oy = (getHeight() - cell * 8) / 2;
        boolean xeque = board.emXeque(board.whiteToMove);
        char reiDaVez = board.whiteToMove ? 'K' : 'k';
        Font coord = new Font("SansSerif", Font.BOLD, Math.max(10, cell / 6));
        Font peca = fontePecas.deriveFont((float) (cell * 0.78));

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                int x = ox + c * cell, y = oy + r * cell;
                boolean clara = (r + c) % 2 == 0;
                g.setColor(clara ? CLARA : ESCURA);
                g.fillRect(x, y, cell, cell);

                if (ultimo != null && ((r == ultimo.fr && c == ultimo.fc) || (r == ultimo.tr && c == ultimo.tc))) {
                    g.setColor(new Color(205, 210, 106, 150));
                    g.fillRect(x, y, cell, cell);
                }
                if (xeque && board.sq[r][c] == reiDaVez) {
                    g.setColor(new Color(220, 50, 50, 170));
                    g.fillRect(x, y, cell, cell);
                }
                if (r == selR && c == selC) {
                    g.setColor(new Color(246, 246, 105, 170));
                    g.fillRect(x, y, cell, cell);
                }

                // coordenadas
                g.setFont(coord);
                g.setColor(clara ? ESCURA : CLARA);
                if (c == 0) g.drawString(String.valueOf(8 - r), x + 3, y + coord.getSize() + 1);
                if (r == 7) g.drawString(String.valueOf((char) ('a' + c)), x + cell - coord.getSize() + 1, y + cell - 4);
            }
        }

        // peças
        g.setFont(peca);
        FontMetrics fm = g.getFontMetrics();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                char p = board.sq[r][c];
                if (p == '.') continue;
                String s = glifo(p);
                int tx = ox + c * cell + (cell - fm.stringWidth(s)) / 2;
                int ty = oy + r * cell + (cell - fm.getHeight()) / 2 + fm.getAscent();
                boolean branca = Character.isUpperCase(p);
                if (branca) {
                    g.setColor(Color.BLACK);
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) g.drawString(s, tx + dx, ty + dy);
                    }
                    g.setColor(Color.WHITE);
                } else {
                    g.setColor(new Color(25, 25, 25));
                }
                g.drawString(s, tx, ty);
            }
        }

        // destinos possíveis da peça selecionada
        if (selR >= 0) {
            for (Move m : legais) {
                if (m.fr != selR || m.fc != selC) continue;
                int x = ox + m.tc * cell, y = oy + m.tr * cell;
                g.setColor(new Color(20, 85, 30, 120));
                if (board.sq[m.tr][m.tc] != '.') {
                    g.setStroke(new BasicStroke(Math.max(3f, cell / 12f)));
                    g.drawOval(x + 4, y + 4, cell - 8, cell - 8);
                } else {
                    int d = cell / 3;
                    g.fillOval(x + (cell - d) / 2, y + (cell - d) / 2, d, d);
                }
            }
        }
    }
}
