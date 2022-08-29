package game;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.swing.*;


public class MainFrame extends JFrame {

    private MButton[][] buttons = new MButton[16][16];
    private JLabel lblTitle = new JLabel();
    private Container mainPanel = this.getContentPane();
    private JPanel buttonsPanel = new JPanel();
    private JPanel titlePanel = new JPanel();
    private int bombNr = 40;
    
    public MainFrame() throws HeadlessException {
        
        initFrame();
        initTitleBar();
        initButtons();
    }
    
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }

    private void initFrame() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(500, 600);
        this.setLayout(new BorderLayout());
        this.setLocationRelativeTo(null);
        this.setTitle("MineSweeper");    
    }

    private void initButtons() {
        
        buttonsPanel.setLayout(new GridLayout(16, 16));

        // i -> y ; j -> x
        for(int i = 0; i < buttons.length; ++i) {
            for(int j = 0; j < buttons[i].length; ++j) {
                buttons[i][j] = new MButton();
                buttons[i][j].setBackground(Color.gray);
                buttons[i][j].setTileI(i);
                buttons[i][j].setTileJ(j);
                buttons[i][j].setBorder(BorderFactory.createRaisedBevelBorder());

                addListenersToButton(buttons[i][j]);

                buttonsPanel.add(buttons[i][j]);
            }
        }

        generateBombs(bombNr);

        for(int i = 0; i < buttons.length; ++i) {
            for(int j = 0; j < buttons[i].length; ++j) {
                if(!buttons[i][j].isBomb()) {
                    buttons[i][j].setNumber(getSurrMinesNr(i, j));
                }
            }
        }


        this.add(buttonsPanel);


    }

    private void addListenersToButton(MButton b) {
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                MButton b = (MButton)e.getSource();
                if(b.isOpened()) return;

                if(SwingUtilities.isLeftMouseButton(e)) {
                    if(b.isFlagged()) return;

                    if(b.isBomb()) {
                        bombTriggered(b);
                    } else {
                        revealClearSection(b.getTileI(), b.getTileJ());
                    }

                } else if(SwingUtilities.isRightMouseButton(e)) {
                    toggleFlagged(b);
                }

            }
        });
    }

    // stops game
    private void bombTriggered(MButton b) {
        revealBoard();
        JOptionPane.showMessageDialog(
                this,
                "Game Over\nRestart Game?\n",
                "",
                JOptionPane.ERROR_MESSAGE);

        restartGame();
    }

    private void restartGame() {
        this.remove(buttonsPanel);
        buttonsPanel = new JPanel();

        buttons = new MButton[16][16];
        initButtons();
    }

    private void revealBoard() {
        for(int i = 0; i < buttons.length; ++i) {
            for(int j = 0; j < buttons[i].length; ++j) {
                if(buttons[i][j].isOpened()) continue;
                showTile(buttons[i][j]);
            }
        }
    }


    private void toggleFlagged(MButton b) {
        if(b.isOpened()) return;
        boolean flag = !b.isFlagged();
        b.setFlagged(flag);
        if(flag)
            b.setText("🚩");
        else
            b.setText("");
    }


    int[] di = {-1, -1, 0, 1, 1,  1,  0, -1};
    int[] dj = { 0,  1, 1, 1, 0, -1, -1, -1};

    private boolean checkTile(int i, int j) {
        if(i < 0 || i > 15 || j < 0 || j > 15)
            return false;
        return true;
    }

    private void revealClearSection(int i, int j) {
        if(!checkTile(i, j) || buttons[i][j].isOpened()) return;
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
            b.setText("💣");
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
            if(!checkTile(ni, nj)) continue;

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

    private void generateBombs(int bombs) {
        Random rand = new Random();
        Set<Point> controlSet = new HashSet<>();

        while(bombs > 0) {
            int x = rand.nextInt(16);
            int y = rand.nextInt(16);
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
