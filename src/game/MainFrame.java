package game;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.swing.*;

// todo timer, bomb counter
public class MainFrame extends JFrame {

    private final int gridLen = 16; // 16
    private final int bombNr = 40; // 40
    private       int bombCounter = bombNr;

    private final MButton[][] buttons = new MButton[gridLen][gridLen];
    private final JLabel lblTitle = new JLabel();
    private final Container mainPanel = this.getContentPane();
    private final JPanel buttonsPanel = new JPanel();
    private final JPanel titlePanel = new JPanel();
    private MouseEvent lastEvt;

    public MainFrame() throws HeadlessException {
        
        initFrame();
        initTitleBar();
        initButtons();
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
        for(int i = 0; i < buttons.length; ++i) {
            for(int j = 0; j < buttons[i].length; ++j) {
                MButton button;

                button = new MButton();
                button.setBackground(Color.gray);
                button.setTileI(i);
                button.setTileJ(j);
                button.setBorder(BorderFactory.createRaisedBevelBorder());

                addListenersToButton(button);

                buttonsPanel.add(button);
                buttons[i][j] = button;
            }
        }

        generateBombs();
        generateNumbers();

        this.add(buttonsPanel);
    }

    private void generateNumbers() {
        for(int i = 0; i < buttons.length; ++i)
            for(int j = 0; j < buttons[i].length; ++j)
                if(!buttons[i][j].isBomb())
                    buttons[i][j].setNumber(getSurrMinesNr(i, j));
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
                        revealClearSection(b.getI(), b.getJ());
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

                setTilesColour(b, Color.white);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                MButton b = (MButton)e.getSource();
                if(!b.isOpened()) return;
                if(!b.isFocused()) return;
                b.setFocused(false);
                setTilesColour(b, Color.gray);
            }
        });
    }

    private void setTilesColour(MButton b, Color c) {
        int i = b.getI();
        int j = b.getJ();

        for(int d = 0; d < 8; ++d) {
            int ni = i + di[d];
            int nj = j + dj[d];

            if(isInvalidTile(ni, nj)
                || buttons[ni][nj].isOpened()
                || buttons[ni][nj].isFlagged())
                continue;

            buttons[ni][nj].setBackground(c);
        }
    }

    // stops game
    private void stopGame() {
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
        generateBombs();
        generateNumbers();

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
        boolean flag = !b.isFlagged();
        b.setFlagged(flag);

        if(flag) {
            b.setText("????");
            bombCounter--;
        } else {
            b.setText("");
            bombCounter++;
        }
        checkFinish();
    }

    private void checkFinish() {
        if(bombCounter != 0) return;
        for(MButton[] array : buttons)
            for(MButton button : array) 
                if(button.isBomb() && !button.isFlagged())
                    return;

        revealBoard();

        int result = JOptionPane.showConfirmDialog(
                this,
                "Congrats! You won.\n" +
                "Play again?",
                "",
                JOptionPane.YES_NO_OPTION);

        if(result == JOptionPane.NO_OPTION)
            System.exit(0);

        restartGame();

    }

    final int[] di = {-1, -1, 0, 1, 1,  1,  0, -1};
    final int[] dj = { 0,  1, 1, 1, 0, -1, -1, -1};

    private boolean isInvalidTile(int i, int j) {
        return i < 0 || i > gridLen - 1
                || j < 0 || j > gridLen - 1;
    }

    private void revealClearSection(int i, int j) {
        if(isInvalidTile(i, j) || buttons[i][j].isOpened()) return;
        if(buttons[i][j].getNumber() != 0) {
            showTile(buttons[i][j]);
            return;
        }

        showTile(buttons[i][j]);
        for(int d = 0; d < 8; ++d) {
            int ni = i + di[d];
            int nj = j + dj[d];
            revealClearSection(ni, nj);
        }
    }

    private void showTile(MButton b) {
        if(b.isOpened()) return;
        if(b.isFlagged()) return;

        if(b.isBomb()) {
            b.setBackground(Color.red);
            b.setText("????");
            b.setOpened(true);
            return;
        }

        b.setBackground(Color.white);
        b.setText(b.getNumber() != 0 ? String.valueOf(b.getNumber()) : "");
        b.setOpened(true);

    }

    private int getSurrMinesNr(int i, int j) {
        int count = 0;

        for(int d = 0; d < 8; ++d) {
            int ni = i + di[d];
            int nj = j + dj[d];

            // out of bounds check
            if(isInvalidTile(ni, nj)) continue;

            if(buttons[ni][nj].isBomb())
                count++;
        }

        return count;
    }

    private void initTitleBar() {
        
        lblTitle.setHorizontalAlignment(JLabel.CENTER);
        lblTitle.setText("sal ba");
//        lblTitle.setFont(new Font());
        titlePanel.add(lblTitle);
        this.add(titlePanel, BorderLayout.NORTH);
        
    }

    private void generateBombs() {
        int bombs = bombNr;
        Random rand = new Random();
        Set<Point> controlSet = new HashSet<>();

        while(bombs > 0) {
            int x = rand.nextInt(gridLen);
            int y = rand.nextInt(gridLen);
            Point p = new Point(x, y);

            if(!controlSet.contains(p)) {

                buttons[x][y].setBomb(true);
                buttons[x][y].setNumber(-1);
                bombs--;
                controlSet.add(p);
            }
        }
    }
}
