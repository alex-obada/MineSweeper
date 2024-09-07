package game;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MainFrame extends JFrame {

    private final int gridLen = 16; // 16
    private final int bombNr = 40; // 40
    private       int bombCounter = bombNr;
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
    private Font font = null;

    public MainFrame() throws HeadlessException {
        loadResources();
        initFrame();
        initTitleBar();
        initButtons();
        timeKeeper = new TimeKeeper(time -> timeLabel.setText(formatNumber(time)));
    }

    private void loadResources() {
        File fontFile = new File("res/DS-DIGII.TTF");

        try {
            font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
        } catch (FontFormatException | IOException e) {
            JOptionPane.showMessageDialog(mainPanel, "Could not load font.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        font = font.deriveFont(Font.BOLD, 50);
    }

    private void initFrame() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(500, 600);
        this.setLayout(new BorderLayout());
        this.setLocationRelativeTo(null);
        this.setTitle("MineSweeper");    
    }

    private void initButtons() {
        
        buttonsPanel.setLayout(new GridLayout(gridLen, gridLen));

        // i -> y ; j -> x
        for(int y = 0; y < buttons.length; ++y) {
            for(int x = 0; x < buttons[y].length; ++x) {
                MButton button;

                button = new MButton();
                button.setBackground(Color.gray);
                button.setTileY(y);
                button.setTileX(x);
                button.setBorder(BorderFactory.createRaisedBevelBorder());

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
                if(!buttons[y][x].isBomb())
                    buttons[y][x].setNumber(getSurrMinesNr(y, x));
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

                setTilesColor(b, Color.white);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                MButton b = (MButton)e.getSource();
                if(!b.isOpened()) return;
                if(!b.isFocused()) return;
                b.setFocused(false);
                setTilesColor(b, Color.gray);
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

        bombCounter = bombNr;
        updateBombCounter();
        generateBombs();
        generateNumbers();
        timeKeeper.stop();
        timeKeeper.resetTimer();
    }

    private void revealBoard() {
        for(MButton[] array : buttons)
            for(MButton button : array) {
                if(button.isOpened()) continue;
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
            b.setText("🚩");
            bombCounter--;
        } else {
            b.setText("");
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
            b.setBackground(Color.red);
            b.setText("💣");
            b.setOpened(true);
            return;
        }

        b.setBackground(Color.white);
        b.setText(b.getNumber() != 0 ? String.valueOf(b.getNumber()) : "");
        b.setOpened(true);
    }

    private int getSurrMinesNr(int y, int x) {
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
        titlePanel.setLayout(new BorderLayout());
        titlePanel.setPreferredSize(new Dimension(0, 100));



        timeLabel.setHorizontalAlignment(JLabel.RIGHT);
        timeLabel.setPreferredSize(new Dimension(100, 70));
        timeLabel.setFont(font);

        updateBombCounter();
        lblBombCounter.setPreferredSize(new Dimension(100, 70));
        lblBombCounter.setHorizontalAlignment(JLabel.LEFT);
        lblBombCounter.setFont(font);

        var restartButton = new JButton("Restart");
        restartButton.setPreferredSize(new Dimension(70, 70));
        restartButton.addActionListener(e -> restartGame());

        titlePanel.add(restartButton, BorderLayout.CENTER);
        titlePanel.add(lblBombCounter, BorderLayout.WEST);
        titlePanel.add(timeLabel, BorderLayout.EAST);
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
        int bombs = bombNr;
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
