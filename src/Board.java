import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tabuleiro e regras do xadrez (sem nenhuma parte gráfica).
 *
 * Letras maiúsculas = peças brancas, minúsculas = pretas, '.' = casa vazia.
 * A linha 0 é a 8ª fileira (topo) e a linha 7 é a 1ª fileira (base).
 */
public final class Board {
    static final int[][] CAVALO = {{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};
    static final int[][] REI = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
    static final int[][] DIAGONAIS = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
    static final int[][] RETAS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    char[][] sq = new char[8][8];
    boolean whiteToMove = true;
    boolean wK = true, wQ = true, bK = true, bQ = true; // direitos de roque
    int epR = -1, epC = -1; // casa de destino do en passant (se houver)

    static Board novo() {
        Board b = new Board();
        b.reset();
        return b;
    }

    void reset() {
        String fundo = "rnbqkbnr";
        for (int r = 0; r < 8; r++) Arrays.fill(sq[r], '.');
        for (int c = 0; c < 8; c++) {
            sq[0][c] = fundo.charAt(c);
            sq[1][c] = 'p';
            sq[6][c] = 'P';
            sq[7][c] = Character.toUpperCase(fundo.charAt(c));
        }
        whiteToMove = true;
        wK = wQ = bK = bQ = true;
        epR = epC = -1;
    }

    Board copy() {
        Board b = new Board();
        for (int r = 0; r < 8; r++) b.sq[r] = sq[r].clone();
        b.whiteToMove = whiteToMove;
        b.wK = wK; b.wQ = wQ; b.bK = bK; b.bQ = bQ;
        b.epR = epR; b.epC = epC;
        return b;
    }

    static boolean dentro(int r, int c) {
        return r >= 0 && r < 8 && c >= 0 && c < 8;
    }

    static boolean daCor(char p, boolean branca) {
        return branca ? Character.isUpperCase(p) : Character.isLowerCase(p);
    }

    /** Lances "pseudo-legais": respeitam o movimento da peça, mas podem deixar o rei em xeque. */
    List<Move> pseudo(boolean branca) {
        List<Move> out = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                char p = sq[r][c];
                if (!daCor(p, branca)) continue;
                switch (Character.toLowerCase(p)) {
                    case 'p': peao(out, r, c, branca); break;
                    case 'n': passos(out, r, c, branca, CAVALO); break;
                    case 'k': passos(out, r, c, branca, REI); roque(out, branca); break;
                    case 'b': deslizar(out, r, c, branca, DIAGONAIS); break;
                    case 'r': deslizar(out, r, c, branca, RETAS); break;
                    case 'q':
                        deslizar(out, r, c, branca, DIAGONAIS);
                        deslizar(out, r, c, branca, RETAS);
                        break;
                    default: break;
                }
            }
        }
        return out;
    }

    private void adicionar(List<Move> out, int fr, int fc, int tr, int tc, boolean promocao) {
        if (promocao) {
            for (char ch : "qrbn".toCharArray()) out.add(new Move(fr, fc, tr, tc, ch));
        } else {
            out.add(new Move(fr, fc, tr, tc));
        }
    }

    private void peao(List<Move> out, int r, int c, boolean branca) {
        int dir = branca ? -1 : 1;
        int inicio = branca ? 6 : 1;
        int ultima = branca ? 0 : 7;
        int nr = r + dir;
        if (dentro(nr, c) && sq[nr][c] == '.') {
            adicionar(out, r, c, nr, c, nr == ultima);
            if (r == inicio && sq[nr + dir][c] == '.') out.add(new Move(r, c, nr + dir, c));
        }
        for (int dc = -1; dc <= 1; dc += 2) {
            int nc = c + dc;
            if (!dentro(nr, nc)) continue;
            char alvo = sq[nr][nc];
            boolean captura = alvo != '.' && daCor(alvo, !branca);
            boolean enPassant = (nr == epR && nc == epC);
            if (captura || enPassant) adicionar(out, r, c, nr, nc, nr == ultima);
        }
    }

    private void passos(List<Move> out, int r, int c, boolean branca, int[][] deltas) {
        for (int[] d : deltas) {
            int nr = r + d[0], nc = c + d[1];
            if (!dentro(nr, nc)) continue;
            if (!daCor(sq[nr][nc], branca)) out.add(new Move(r, c, nr, nc));
        }
    }

    private void deslizar(List<Move> out, int r, int c, boolean branca, int[][] direcoes) {
        for (int[] d : direcoes) {
            int nr = r + d[0], nc = c + d[1];
            while (dentro(nr, nc)) {
                char alvo = sq[nr][nc];
                if (alvo == '.') {
                    out.add(new Move(r, c, nr, nc));
                } else {
                    if (daCor(alvo, !branca)) out.add(new Move(r, c, nr, nc));
                    break;
                }
                nr += d[0];
                nc += d[1];
            }
        }
    }

    private void roque(List<Move> out, boolean branca) {
        int r = branca ? 7 : 0;
        char rei = branca ? 'K' : 'k';
        char torre = branca ? 'R' : 'r';
        if (sq[r][4] != rei) return;
        boolean curto = branca ? wK : bK;
        boolean longo = branca ? wQ : bQ;
        boolean inimigo = !branca;
        if (curto && sq[r][7] == torre && sq[r][5] == '.' && sq[r][6] == '.'
                && !atacada(r, 4, inimigo) && !atacada(r, 5, inimigo) && !atacada(r, 6, inimigo)) {
            out.add(new Move(r, 4, r, 6));
        }
        if (longo && sq[r][0] == torre && sq[r][1] == '.' && sq[r][2] == '.' && sq[r][3] == '.'
                && !atacada(r, 4, inimigo) && !atacada(r, 3, inimigo) && !atacada(r, 2, inimigo)) {
            out.add(new Move(r, 4, r, 2));
        }
    }

    /** A casa (r, c) está sendo atacada por alguma peça da cor indicada? */
    boolean atacada(int r, int c, boolean porBranca) {
        int pr = porBranca ? r + 1 : r - 1;
        char peao = porBranca ? 'P' : 'p';
        for (int dc = -1; dc <= 1; dc += 2) {
            if (dentro(pr, c + dc) && sq[pr][c + dc] == peao) return true;
        }
        char cav = porBranca ? 'N' : 'n';
        char rei = porBranca ? 'K' : 'k';
        char bis = porBranca ? 'B' : 'b';
        char tor = porBranca ? 'R' : 'r';
        char dam = porBranca ? 'Q' : 'q';
        for (int[] d : CAVALO) {
            int nr = r + d[0], nc = c + d[1];
            if (dentro(nr, nc) && sq[nr][nc] == cav) return true;
        }
        for (int[] d : REI) {
            int nr = r + d[0], nc = c + d[1];
            if (dentro(nr, nc) && sq[nr][nc] == rei) return true;
        }
        for (int[] d : DIAGONAIS) {
            if (raioAcerta(r, c, d, bis, dam)) return true;
        }
        for (int[] d : RETAS) {
            if (raioAcerta(r, c, d, tor, dam)) return true;
        }
        return false;
    }

    private boolean raioAcerta(int r, int c, int[] d, char a, char b) {
        int nr = r + d[0], nc = c + d[1];
        while (dentro(nr, nc)) {
            char t = sq[nr][nc];
            if (t != '.') return t == a || t == b;
            nr += d[0];
            nc += d[1];
        }
        return false;
    }

    boolean emXeque(boolean branca) {
        char rei = branca ? 'K' : 'k';
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (sq[r][c] == rei) return atacada(r, c, !branca);
            }
        }
        return false;
    }

    /** Aplica o lance no tabuleiro (assume que o lance é válido). */
    void aplicar(Move m) {
        char p = sq[m.fr][m.fc];
        boolean branca = Character.isUpperCase(p);
        char minuscula = Character.toLowerCase(p);

        // en passant: peão anda na diagonal para casa vazia -> captura o peão ao lado
        if (minuscula == 'p' && m.fc != m.tc && sq[m.tr][m.tc] == '.') {
            sq[m.fr][m.tc] = '.';
        }
        // roque: o rei anda duas casas -> move também a torre
        if (minuscula == 'k' && Math.abs(m.tc - m.fc) == 2) {
            if (m.tc == 6) { sq[m.fr][5] = sq[m.fr][7]; sq[m.fr][7] = '.'; }
            else           { sq[m.fr][3] = sq[m.fr][0]; sq[m.fr][0] = '.'; }
        }

        sq[m.tr][m.tc] = p;
        sq[m.fr][m.fc] = '.';
        if (m.promo != '\0') sq[m.tr][m.tc] = branca ? Character.toUpperCase(m.promo) : m.promo;

        // direitos de roque
        if (p == 'K') { wK = false; wQ = false; }
        if (p == 'k') { bK = false; bQ = false; }
        if (toca(m, 7, 0)) wQ = false;
        if (toca(m, 7, 7)) wK = false;
        if (toca(m, 0, 0)) bQ = false;
        if (toca(m, 0, 7)) bK = false;

        // casa de en passant disponível para o próximo lance
        epR = epC = -1;
        if (minuscula == 'p' && Math.abs(m.tr - m.fr) == 2) {
            epR = (m.fr + m.tr) / 2;
            epC = m.fc;
        }
        whiteToMove = !whiteToMove;
    }

    private boolean toca(Move m, int r, int c) {
        return (m.fr == r && m.fc == c) || (m.tr == r && m.tc == c);
    }

    /** Lances legais: os pseudo-legais que não deixam o próprio rei em xeque. */
    List<Move> legais() {
        List<Move> res = new ArrayList<>();
        boolean lado = whiteToMove;
        for (Move m : pseudo(lado)) {
            Board b = copy();
            b.aplicar(m);
            if (!b.emXeque(lado)) res.add(m);
        }
        return res;
    }
}
