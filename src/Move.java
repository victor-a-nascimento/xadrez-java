/**
 * Um lance de xadrez: casa de origem, casa de destino e, se houver, a peça da promoção.
 */
public final class Move {
    final int fr, fc, tr, tc; // de (linha, coluna) -> para (linha, coluna)
    final char promo;         // 'q', 'r', 'b', 'n' ou '\0' se não houver promoção

    Move(int fr, int fc, int tr, int tc, char promo) {
        this.fr = fr; this.fc = fc; this.tr = tr; this.tc = tc; this.promo = promo;
    }

    Move(int fr, int fc, int tr, int tc) {
        this(fr, fc, tr, tc, '\0');
    }
}
