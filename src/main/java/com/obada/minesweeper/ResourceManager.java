package com.obada.minesweeper;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ResourceManager {
    private static ResourceManager instance = null;

    public static ResourceManager getInstance()
    {

        if(instance == null)
            instance = new ResourceManager();
        return instance;
    }

    private static final String resourcesPath = "resources";

    private Font titlePanelFont = null;
    private Font tileFont = null;
    private BufferedImage iconImage = null;
    private BufferedImage flagImage = null;
    private BufferedImage bombImage = null;
    private BufferedImage restartGameImage = null;

    public final Color bombBackground = Color.red;
    public final Color openedTile = Color.white;
    public final Color closedTile = Color.gray;
    public final Color[] numberColors = new Color[] {
            new Color(0xffffffff),
            new Color(26, 23, 249),
            new Color(61, 125, 23),
            new Color(228, 60, 26),
            new Color(7, 6, 125),
            new Color(109, 24, 7),
            new Color(63, 126, 127),
            new Color(0),
            new Color(128, 128, 128),
    };

    private ResourceManager() { loadResources(); }

    private void loadResources() {
        try {
            titlePanelFont = loadFont("DS-DIGII.TTF", 50);
            tileFont = loadFont("PressStart2P-Regular.ttf", 20);

            flagImage = loadImage("flag.png");
            iconImage = loadImage("icon.png");
            bombImage = loadImage("bomb.png");
            restartGameImage = loadImage("coolFace.png");
        } catch (FontFormatException | IOException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Could not load resources. Error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private Font loadFont(String path, int size) throws IOException, FontFormatException {
        path = "fonts/" + path;
        try (InputStream fontStream = getClass().getClassLoader().getResourceAsStream(path)) {
            if(fontStream == null)
                throw new IOException("Font not found: " + path);
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            return font.deriveFont(Font.BOLD, size);
        }
    }

    private BufferedImage loadImage(String path) throws IOException, FontFormatException {
        path = "images/" + path;
        try (InputStream imageStream = getClass().getClassLoader().getResourceAsStream(path)) {
            if (imageStream == null) {
                throw new IOException("Image not found: " + path);
            }
            return ImageIO.read(imageStream);
        }
    }

    public void updateTileFontToFit(JComponent component) {
        int width = component.getWidth();
        int height = component.getHeight();

        if (width <= 0 || height <= 0)
            return;

        /*
        px pt
        27 20
        54 50
         */
        // good enough approximation
        tileFont = tileFont.deriveFont(Font.BOLD, Math.min(width, height) - 10);
    }

    public static ImageIcon getResizedIcon(BufferedImage originalImage, JComponent component, boolean preferredSize) {
        int width = component.getWidth();
        int height = component.getHeight();
        height = width = Math.min(height, width);

        if(preferredSize) {
            width = component.getPreferredSize().width;
            height = component.getPreferredSize().height;
        }

        Image resizedImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage bufferedResizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedResizedImage.createGraphics();
        g.drawImage(resizedImage, 0, 0, width, height, null);
        g.dispose();

        return new ImageIcon(bufferedResizedImage);
    }

    public Font getTitlePanelFont() {
        return titlePanelFont;
    }

    public Font getTileFont() {
        return tileFont;
    }

    public BufferedImage getIconImage() {
        return iconImage;
    }

    public BufferedImage getFlagImage() {
        return flagImage;
    }

    public BufferedImage getBombImage() {
        return bombImage;
    }

    public BufferedImage getRestartGameImage() {
        return restartGameImage;
    }
}
