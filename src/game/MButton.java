package game;

import javax.swing.JButton;
import java.awt.*;

class MButton extends JButton {
    private boolean opened = false;
    private boolean bomb;
    private int i, j;
    private int number;
    private boolean flagged = false;
    private boolean focused = false;

    public void reset() {
        opened = false;
        bomb = false;
        flagged = false;
        focused = false;
        number = 0;
        this.setText("");
        this.setBackground(Color.gray);
    }

    public MButton() {
        super();
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isBomb() {
        return bomb;
    }

    public void setBomb(boolean bomb) {
        this.bomb = bomb;
    }

    public int getJ() {
        return j;
    }

    public int getI() {
        return i;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public void setTileJ(int j) {
        this.j = j;
    }

    public void setTileI(int i) {
        this.i = i;
    }
}