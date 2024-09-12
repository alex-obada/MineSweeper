package com.obada.minesweeper;

import javax.swing.JButton;

class MButton extends JButton {
    private boolean opened = false;
    private boolean bomb;
    private int tileY, tileX;
    private int number;
    private boolean flagged = false;
    private boolean focused = false;
    private final ResourceManager resourceManager = ResourceManager.getInstance();

    public void reset() {
        opened = false;
        bomb = false;
        flagged = false;
        focused = false;
        number = 0;
        this.setText("");
        this.setIcon(null);
        this.setBackground(resourceManager.closedTile);
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

    public int getTileX() {
        return tileX;
    }

    public int getTileY() {
        return tileY;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public void setTileX(int x) {
        this.tileX = x;
    }

    public void setTileY(int y) {
        this.tileY = y;
    }
}