package ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.ImageIcon;

import model.Card;
import util.ResourceLoader;

//Swing component that visually displays a card and its selection state.
public class CardView extends JComponent {

    private Card card;
    private boolean selected;

    private int prefW = 110;
    private int prefH = 160;

    public CardView() {
        setOpaque(false);
        setFocusable(true);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        setPreferredSize(new Dimension(prefW, prefH));
    }

    public void setCard(Card card) {
        this.card = card;
        repaint();
    }

    public Card getCard() {
        return card;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setPreferredCardSize(int w, int h) {
        prefW = Math.max(20, w);
        prefH = Math.max(30, h);
        setPreferredSize(new Dimension(prefW, prefH));
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int w = getWidth();
        int h = getHeight();

        if (selected) {
            g.drawRect(1, 1, w - 3, h - 3);
            g.drawRect(2, 2, w - 5, h - 5);
        }

        if (card == null) {
            g.drawRect(4, 4, w - 9, h - 9);
            return;
        }

        String path = card.getImagePath();
        ImageIcon icon = (path == null || path.trim().isEmpty())
                ? null
                : ResourceLoader.getScaledIcon(path, w - 8, h - 8);

        if (icon != null) {
            Image img = icon.getImage();
            g.drawImage(img, 4, 4, w - 8, h - 8, null);
            return;
        }

        g.drawRect(4, 4, w - 9, h - 9);
        g.drawString(card.getName(), 10, 20);
        g.drawString("ATK: " + card.getAttack(), 10, 40);
        g.drawString("DEF: " + card.getDefense(), 10, 55);
        g.drawString("COST: " + card.getCost(), 10, 70);
    }
}
