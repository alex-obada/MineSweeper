package com.obada.minesweeper;

import javax.swing.JButton;

class MButton extends JButton {
    private boolean opened = false;
    private boolean mine;
    private int cellY, cellX;
    private int number;
    private boolean flagged = false;
    private boolean focused = false;
    private final ResourceManager resourceManager = ResourceManager.getInstance();

    public void reset() {
        opened = false;
        mine = false;
        flagged = false;
        focused = false;
        number = 0;
        this.setText("");
        this.setIcon(null);
        this.setBackground(resourceManager.closedCell);
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

    public boolean isMine() {
        return mine;
    }

    public void setMine(boolean mine) {
        this.mine = mine;
    }

    public int getCellX() {
        return cellX;
    }

    public int getCellY() {
        return cellY;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public void setCellX(int x) {
        this.cellX = x;
    }

    public void setCelleY(int y) {
        this.cellY = y;
    }
}