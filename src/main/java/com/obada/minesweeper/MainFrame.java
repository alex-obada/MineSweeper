package com.obada.minesweeper;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class MainFrame extends JFrame {

    private final int GRID_LENGTH = 16; // 16
    private final int MINE_NUMBER = 40; // 40
    private final int MINIMUM_SIZE_CHANGE = 10;
    private       int mineCounter = MINE_NUMBER;
    private       int revealedCells = 0;
    final int[] dy = {-1, -1, 0, 1, 1,  1,  0, -1};
    final int[] dx = { 0,  1, 1, 1, 0, -1, -1, -1};

    private final MButton[][] buttons = new MButton[GRID_LENGTH][GRID_LENGTH];
    private final JLabel lblMineCounter = new JLabel();
    private final JLabel timeLabel = new JLabel();
    private final JPanel buttonsPanel = new JPanel();
    private final JPanel titlePanel = new JPanel();
    private MouseEvent lastEvt;
    private Dimension lastSize;
    private TimeKeeper timeKeeper;
    private final ResourceManager resourceManager = ResourceManager.getInstance();
    private ImageIcon mineIcon;
    private ImageIcon flagIcon;

    public MainFrame() throws HeadlessException {
        // debugging purposes
        if(MINE_NUMBER > GRID_LENGTH * GRID_LENGTH) {
            System.out.println(MINE_NUMBER + " mines cant fit in a " + GRID_LENGTH + "x" + GRID_LENGTH + " grid");
            System.exit(1);
        }

        initFrame();
        initTitleBar();
        initButtons();
        addWindowEventListeners();
        lastSize = this.getSize();
        timeKeeper = new TimeKeeper(time -> timeLabel.setText(formatNumber(time)));
    }

    private void addWindowEventListeners() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                updateButtonIcons();
            }

        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if(shouldUpdateIcons()) {
                    updateButtonFonts();
                    updateButtonIcons();
                }
            }
        });
    }

    private boolean shouldUpdateIcons() {
        Dimension currentSize = this.getSize();
        int widthChange = Math.abs(currentSize.width - lastSize.width);
        int heightChange = Math.abs(currentSize.height - lastSize.height);
        int sizeChange = Math.min(widthChange, heightChange);

        if (sizeChange > MINIMUM_SIZE_CHANGE) {
            lastSize = currentSize;
            return true;
        }
        return false;
    }

    private void updateButtonFonts() {
        resourceManager.updateCellFontToFit(buttons[0][0]);
        for (MButton[] buttonList : buttons)
            for (MButton button : buttonList)
                button.setFont(resourceManager.getCellFont());
    }

    private void updateButtonIcons() {
        mineIcon = ResourceManager.getResizedIcon(resourceManager.getMineImage(), buttons[0][0], false);
        flagIcon = ResourceManager.getResizedIcon(resourceManager.getFlagImage(), buttons[0][0], false);
        for (MButton[] buttonList : buttons)
            for (MButton button : buttonList)
                if(button.isFlagged())
                    button.setIcon(flagIcon);
    }

    private void initFrame() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        var dimension = new Dimension(492, 610);
        this.setMinimumSize(dimension);
        this.setSize(dimension);
        this.setLayout(new BorderLayout());
        this.setLocationRelativeTo(null);
        this.setTitle("Minesweeper");
        this.setIconImage(resourceManager.getIconImage());
    }

    private void initButtons() {
        buttonsPanel.setLayout(new GridLayout(GRID_LENGTH, GRID_LENGTH));

        for(int y = 0; y < buttons.length; ++y) {
            for(int x = 0; x < buttons[y].length; ++x) {
                MButton button;

                button = new MButton();
                button.setBackground(resourceManager.closedCell);
                button.setCelleY(y);
                button.setCellX(x);
                button.setBorder(BorderFactory.createRaisedBevelBorder());
                button.setFont(resourceManager.getCellFont());
                button.setFocusPainted(false);
                addListenersToButton(button);

                buttonsPanel.add(button);
                buttons[y][x] = button;
            }
        }

        generateMines();
        generateNumbers();

        this.add(buttonsPanel);
    }

    private void generateNumbers() {
        for(int y = 0; y < buttons.length; ++y)
            for(int x = 0; x < buttons[y].length; ++x)
                if(!buttons[y][x].isMine()) {
                    int surroundingBombs = getSurroundingMinesCount(y, x);
                    buttons[y][x].setNumber(surroundingBombs);
                    buttons[y][x].setForeground(resourceManager.numberColors[surroundingBombs]);
                }
    }

    private void addListenersToButton(MButton b) {
        b.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                MButton b = (MButton)e.getSource();
                if(b.isOpened()) return;

                if(SwingUtilities.isLeftMouseButton(e)) {
                    if(b.isFlagged()) return;

                    if(b.isMine()) {
                        stopGame();
                    } else {
                        revealClearSection(b.getCellY(), b.getCellX());
                    }

                    checkFinish();
                } else if(SwingUtilities.isRightMouseButton(e)
                            && lastEvt != e) {
                    toggleFlagged(b);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                lastEvt = e;
                MButton b = (MButton)e.getSource();
                if(!b.isOpened()) {
                    mouseClicked(e);
                    return;
                }
                b.setFocused(true);

                setCellColor(b, resourceManager.openedCell);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                MButton b = (MButton)e.getSource();
                if(!b.isOpened()) return;
                if(!b.isFocused()) return;
                b.setFocused(false);
                setCellColor(b, resourceManager.closedCell);
            }
        });
    }

    private void setCellColor(MButton b, Color c) {
        int y = b.getCellY();
        int x = b.getCellX();

        for(int d = 0; d < 8; ++d) {
            int ny = y + dy[d];
            int nx = x + dx[d];

            if(isInvalidCell(ny, nx)
                || buttons[ny][nx].isOpened()
                || buttons[ny][nx].isFlagged())
                continue;

            buttons[ny][nx].setBackground(c);
        }
    }

    private void stopGame() {
        timeKeeper.stop();
        revealBoard();
        JOptionPane.showMessageDialog(
                this,
                "Game Over\n" +
                "Restart Game?",
                "",
                JOptionPane.ERROR_MESSAGE);

        restartGame();
    }

    private void restartGame() {

        for(MButton[] array : buttons)
            for(MButton button : array)
                button.reset();

        mineCounter = MINE_NUMBER;
        revealedCells = 0;
        updateMineCounter();
        generateMines();
        generateNumbers();
        timeKeeper.stop();
        timeKeeper.resetTimer();
    }

    private void revealBoard() {
        boolean isWon = GRID_LENGTH * GRID_LENGTH - MINE_NUMBER == revealedCells;
        for(MButton[] array : buttons)
            for(MButton button : array) {
                if(button.isOpened() || button.isFlagged()) continue;
                if(isWon && button.isMine()) {
                    toggleFlagged(button);
                    continue;
                }
                revealCell(button);
            }
    }

    private void toggleFlagged(MButton b) {
        if(b.isOpened()) return;
        boolean flag = b.isFlagged();
        if(!flag && mineCounter == 0)
            return;

        flag = !flag;
        b.setFlagged(flag);
        if(flag) {
            b.setIcon(flagIcon);
            mineCounter--;
        } else {
            b.setIcon(null);
            mineCounter++;
        }
        updateMineCounter();
    }

    private void checkFinish() {
        if(revealedCells != GRID_LENGTH * GRID_LENGTH - MINE_NUMBER)
            return;

        timeKeeper.stop();
        revealBoard();

        int result = JOptionPane.showConfirmDialog(
                this,
                "Congrats!\n" +
                "You won in " + (timeKeeper.getTime() - 1) + " seconds.\n" +
                "Play again?",
                "",
                JOptionPane.YES_NO_OPTION);

        if(result == JOptionPane.NO_OPTION)
            System.exit(0);

        restartGame();
    }

    private boolean isInvalidCell(int y, int x) {
        return y < 0 || y > GRID_LENGTH - 1 || x < 0 || x > GRID_LENGTH - 1;
    }

    /**
     * Reveals the cells in the grid that are connected to the cell
     * specified by the <code>x</code> and <code>y</code> coordinates
     * and that have no mines in their 3x3 neighborhood. This method
     * uses a recursive fill algorithm to reveal all contiguous
     * cells with no mines. If the initial cell has a non-zero number
     * of surrounding mines, only that cell will be revealed.
     *
     * @param y The row coordinate of the cell to start revealing.
     * @param x The column coordinate of the cell to start revealing.
     */
    private void revealClearSection(int y, int x) {
        if(isInvalidCell(y, x) || buttons[y][x].isOpened()) return;
        if(buttons[y][x].getNumber() != 0) {
            revealCell(buttons[y][x]);
            return;
        }

        revealCell(buttons[y][x]);
        for(int d = 0; d < 8; ++d) {
            int ny = y + dy[d];
            int nx = x + dx[d];
            revealClearSection(ny, nx);
        }
    }

    private void revealCell(MButton b) {
        if(b.isOpened()) return;
        if(b.isFlagged()) return;

        if(b.isMine()) {
            b.setBackground(resourceManager.mineBackground);
            b.setIcon(mineIcon);
            b.setOpened(true);
            return;
        }

        revealedCells++;
        b.setBackground(resourceManager.openedCell);
        b.setText(b.getNumber() != 0 ? b.getNumber() + "" : "");
        b.setOpened(true);
    }

    private int getSurroundingMinesCount(int y, int x) {
        int count = 0;

        for(int d = 0; d < 8; ++d) {
            int ny = y + dy[d];
            int nx = x + dx[d];

            // out of bounds check
            if(isInvalidCell(ny, nx)) continue;

            if(buttons[ny][nx].isMine())
                count++;
        }

        return count;
    }

    private void initTitleBar() {
        titlePanel.setLayout(new GridLayout(1, 3));
        titlePanel.setPreferredSize(new Dimension(0, 100));

        timeLabel.setHorizontalAlignment(JLabel.RIGHT);
        timeLabel.setPreferredSize(new Dimension(100, 70));
        timeLabel.setFont(resourceManager.getTitlePanelFont());
        timeLabel.setBorder(new LineBorder(Color.BLACK));
        timeLabel.setToolTipText("Timer in seconds");

        updateMineCounter();
        lblMineCounter.setPreferredSize(new Dimension(100, 70));
        lblMineCounter.setHorizontalAlignment(JLabel.LEFT);
        lblMineCounter.setFont(resourceManager.getTitlePanelFont());
        lblMineCounter.setBorder(new LineBorder(Color.BLACK));
        lblMineCounter.setToolTipText("The number of mines left");

        var restartButton = new JButton();
        restartButton.setPreferredSize(new Dimension(100, 100));
        restartButton.setIcon(ResourceManager.getResizedIcon(resourceManager.getRestartGameImage(), restartButton, true));
        restartButton.addActionListener(e -> restartGame());
        restartButton.setToolTipText("Restarts the game");
        restartButton.setFocusPainted(false);
        restartButton.setBorder(new LineBorder(Color.BLACK));

        titlePanel.add(lblMineCounter);
        titlePanel.add(restartButton);
        titlePanel.add(timeLabel);
        titlePanel.setBorder(new EmptyBorder(0, 5, 0, 5));

        this.add(titlePanel, BorderLayout.NORTH);
    }

    private void updateMineCounter() {
        lblMineCounter.setText(formatNumber(mineCounter));
    }

    String formatNumber(int number) {
        String text = "";
        if(number / 100 == 0)
            text += '0';
        if(number / 10 == 0)
            text += '0';
        return text + number;
    }

    private void generateMines() {
        int mines = MINE_NUMBER;
        Random rand = new Random();
        Set<Point> controlSet = new HashSet<>();

        while(mines > 0) {
            int x = rand.nextInt(GRID_LENGTH);
            int y = rand.nextInt(GRID_LENGTH);
            Point p = new Point(y, x);

            if(!controlSet.contains(p)) {

                buttons[y][x].setMine(true);
                buttons[y][x].setNumber(-1);
                mines--;
                controlSet.add(p);
            }
        }
    }
}
