package com.obada.minesweeper;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class MainFrame extends JFrame {

    private final int gridLen = 16; // 16
    private final int bombNumber = 40; // 40
    private       int bombCounter = bombNumber;
    final int[] dy = {-1, -1, 0, 1, 1,  1,  0, -1};
    final int[] dx = { 0,  1, 1, 1, 0, -1, -1, -1};

    private final MButton[][] buttons = new MButton[gridLen][gridLen];
    private final JLabel lblBombCounter = new JLabel();
    private final JLabel timeLabel = new JLabel();
    private final Container mainPanel = this.getContentPane();
    private final JPanel buttonsPanel = new JPanel();
    private final JPanel titlePanel = new JPanel();
    private MouseEvent lastEvt;
    private final TimeKeeper timeKeeper;
    private final ResourceManager resourceManager;
    private ImageIcon bombIcon;
    private ImageIcon flagIcon;

    public MainFrame() throws HeadlessException {
        resourceManager = ResourceManager.getInstance();
        initFrame();
        initTitleBar();
        initButtons();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                updateButtonIcons();
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateButtonIcons();
                updateButtonFonts();
            }
        });
        timeKeeper = new TimeKeeper(time -> timeLabel.setText(formatNumber(time)));
    }

    private void updateButtonFonts() {
        resourceManager.updateTileFontToFit(buttons[0][0]);
        for (MButton[] buttonList : buttons)
            for (MButton button : buttonList)
                button.setFont(resourceManager.getTileFont());
    }

    private void updateButtonIcons() {
        bombIcon = ResourceManager.getResizedIcon(resourceManager.getBombImage(), buttons[0][0], false);
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
        buttonsPanel.setLayout(new GridLayout(gridLen, gridLen));

        for(int y = 0; y < buttons.length; ++y) {
            for(int x = 0; x < buttons[y].length; ++x) {
                MButton button;

                button = new MButton();
                button.setBackground(resourceManager.closedTile);
                button.setTileY(y);
                button.setTileX(x);
                button.setBorder(BorderFactory.createRaisedBevelBorder());
                button.setFont(resourceManager.getTileFont());
                button.setFocusPainted(false);
                addListenersToButton(button);

                buttonsPanel.add(button);
                buttons[y][x] = button;
            }
        }

        generateBombs();
        generateNumbers();

        this.add(buttonsPanel);
    }

    private void generateNumbers() {
        for(int y = 0; y < buttons.length; ++y)
            for(int x = 0; x < buttons[y].length; ++x)
                if(!buttons[y][x].isBomb()) {
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

                    if(b.isBomb()) {
                        stopGame();
                    } else {
                        revealClearSection(b.getTileY(), b.getTileX());
                    }

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

                setTilesColor(b, resourceManager.openedTile);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                MButton b = (MButton)e.getSource();
                if(!b.isOpened()) return;
                if(!b.isFocused()) return;
                b.setFocused(false);
                setTilesColor(b, resourceManager.closedTile);
            }
        });
    }

    private void setTilesColor(MButton b, Color c) {
        int y = b.getTileY();
        int x = b.getTileX();

        for(int d = 0; d < 8; ++d) {
            int ny = y + dy[d];
            int nx = x + dx[d];

            if(isInvalidTile(ny, nx)
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

        bombCounter = bombNumber;
        updateBombCounter();
        generateBombs();
        generateNumbers();
        timeKeeper.stop();
        timeKeeper.resetTimer();
    }

    private void revealBoard() {
        for(MButton[] array : buttons)
            for(MButton button : array) {
                if(button.isOpened() || button.isFlagged()) continue;
                showTile(button);
            }
    }

    private void toggleFlagged(MButton b) {
        if(b.isOpened()) return;
        boolean flag = b.isFlagged();
        if(!flag && bombCounter == 0)
            return;

        flag = !flag;
        b.setFlagged(flag);
        if(flag) {
            b.setIcon(flagIcon);
            bombCounter--;
        } else {
            b.setIcon(null);
            bombCounter++;
        }
        updateBombCounter();
        checkFinish();
    }

    private void checkFinish() {
        if(bombCounter != 0) return;
        for(MButton[] array : buttons)
            for(MButton button : array)
                if(button.isBomb() && !button.isFlagged())
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

    private boolean isInvalidTile(int y, int x) {
        return y < 0 || y > gridLen - 1
                || x < 0 || x > gridLen - 1;
    }

    private void revealClearSection(int y, int x) {
        if(isInvalidTile(y, x) || buttons[y][x].isOpened()) return;
        if(buttons[y][x].getNumber() != 0) {
            showTile(buttons[y][x]);
            return;
        }

        showTile(buttons[y][x]);
        for(int d = 0; d < 8; ++d) {
            int ny = y + dy[d];
            int nx = x + dx[d];
            revealClearSection(ny, nx);
        }
    }

    private void showTile(MButton b) {
        if(b.isOpened()) return;
        if(b.isFlagged()) return;

        if(b.isBomb()) {
            b.setBackground(resourceManager.bombBackground);
            b.setIcon(bombIcon);
            b.setOpened(true);
            return;
        }

        b.setBackground(resourceManager.openedTile);
        b.setText(b.getNumber() != 0 ? b.getNumber() + "" : "");
        b.setOpened(true);
    }

    private int getSurroundingMinesCount(int y, int x) {
        int count = 0;

        for(int d = 0; d < 8; ++d) {
            int ny = y + dy[d];
            int nx = x + dx[d];

            // out of bounds check
            if(isInvalidTile(ny, nx)) continue;

            if(buttons[ny][nx].isBomb())
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

        updateBombCounter();
        lblBombCounter.setPreferredSize(new Dimension(100, 70));
        lblBombCounter.setHorizontalAlignment(JLabel.LEFT);
        lblBombCounter.setFont(resourceManager.getTitlePanelFont());
        lblBombCounter.setBorder(new LineBorder(Color.BLACK));
        lblBombCounter.setToolTipText("The number of bombs left");

        var restartButton = new JButton();
        restartButton.setPreferredSize(new Dimension(100, 100));
        restartButton.setIcon(ResourceManager.getResizedIcon(resourceManager.getRestartGameImage(), restartButton, true));
        restartButton.addActionListener(e -> restartGame());
        restartButton.setToolTipText("Restarts the com.obada.game");
        restartButton.setFocusPainted(false);
        restartButton.setBorder(new LineBorder(Color.BLACK));

        titlePanel.add(lblBombCounter);
        titlePanel.add(restartButton);
        titlePanel.add(timeLabel);
        titlePanel.setBorder(new EmptyBorder(0, 5, 0, 5));

        this.add(titlePanel, BorderLayout.NORTH);
    }

    private void updateBombCounter() {
        lblBombCounter.setText(formatNumber(bombCounter));
    }

    String formatNumber(int number) {
        String text = "";
        if(number / 100 == 0)
            text += '0';
        if(number / 10 == 0)
            text += '0';
        return text + number;
    }

    private void generateBombs() {
        int bombs = bombNumber;
        Random rand = new Random();
        Set<Point> controlSet = new HashSet<>();

        while(bombs > 0) {
            int x = rand.nextInt(gridLen);
            int y = rand.nextInt(gridLen);
            Point p = new Point(y, x);

            if(!controlSet.contains(p)) {

                buttons[y][x].setBomb(true);
                buttons[y][x].setNumber(-1);
                bombs--;
                controlSet.add(p);
            }
        }
    }
}
