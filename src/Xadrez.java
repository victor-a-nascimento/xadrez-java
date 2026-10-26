import javax.swing.*;
import java.awt.*;

/**
 * Jogo de Xadrez em Java (Swing) - dois jogadores no mesmo computador.
 *
 * Regras implementadas: movimento de todas as peças, xeque, xeque-mate,
 * afogamento (empate), roque, en passant e promoção do peão.
 * Ainda NÃO implementado (ótimas melhorias para o portfólio): empate por
 * material insuficiente, regra dos 50 lances, repetição tripla, relógio,
 * notação dos lances e um adversário computador (algoritmo Minimax).
 *
 * Estrutura do projeto:
 *   Xadrez.java          -> ponto de entrada (main) e montagem da janela
 *   Board.java           -> tabuleiro e regras
 *   Move.java            -> um lance
 *   PainelTabuleiro.java -> interface gráfica
 *
 * Como executar:
 *   javac Xadrez.java
 *   java Xadrez
 */
public class Xadrez {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Xadrez em Java");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JLabel status = new JLabel(" ", SwingConstants.CENTER);
            status.setFont(new Font("SansSerif", Font.BOLD, 16));
            status.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            PainelTabuleiro painel = new PainelTabuleiro(status);

            JButton novo = new JButton("Novo jogo");
            novo.addActionListener(e -> painel.novoJogo());
            JButton desfazer = new JButton("Desfazer");
            desfazer.addActionListener(e -> painel.desfazer());

            JPanel barra = new JPanel(new BorderLayout());
            JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
            botoes.add(desfazer);
            botoes.add(novo);
            barra.add(status, BorderLayout.CENTER);
            barra.add(botoes, BorderLayout.EAST);

            frame.add(painel, BorderLayout.CENTER);
            frame.add(barra, BorderLayout.SOUTH);
            frame.pack();
            frame.setMinimumSize(new Dimension(400, 440));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
