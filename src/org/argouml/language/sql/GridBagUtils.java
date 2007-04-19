package org.argouml.language.sql;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;

public final class GridBagUtils {
    private static Insets buttonInsets;

    private static Insets captionInsets;

    private static Insets inputFieldInsets;

    private static int rowDistance;

    static {
        rowDistance = 2;
        buttonInsets = new Insets(rowDistance, 5, rowDistance, 5);
        captionInsets = new Insets(rowDistance + 2, 5, rowDistance, 5);
        inputFieldInsets = new Insets(rowDistance, 0, rowDistance, 0);

        instances = new HashMap();
    }

    private GridBagUtils() {
    }

    private static Map instances;

    public static GridBagConstraints buttonConstraints(int x, int y) {
        return buttonConstraints(x, y, 1, 1);
    }

    public static GridBagConstraints buttonConstraints(int x, int y, int width,
            int height) {
        GridBagConstraints gbc = createConstraints(x, y, width, height);
        // gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = buttonInsets;
        return gbc;
    }

    public static GridBagConstraints captionConstraints(int x, int y) {
        return captionConstraints(x, y, 1, 1, right);
    }

    public static GridBagConstraints captionConstraints(int x, int y,
            int alignment) {
        return captionConstraints(x, y, 1, 1, alignment);
    }

    /**
     * Create GridBagConstraints for a caption.
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
    public static GridBagConstraints captionConstraints(int x, int y,
            int width, int height) {
        return captionConstraints(x, y, width, height, right);
    }

    public static final int left = 0;

    public static final int right = 1;

    public static final int center = 2;

    public static GridBagConstraints captionConstraints(int x, int y,
            int width, int height, int alignment) {
        GridBagConstraints gbc = createConstraints(x, y, width, height);
        if (alignment == left) {
            gbc.anchor = GridBagConstraints.NORTHWEST;
        } else if (alignment == right) {
            gbc.anchor = GridBagConstraints.NORTHEAST;
        } else if (alignment == center) {
            gbc.anchor = GridBagConstraints.CENTER;
        }
        gbc.insets = captionInsets;
        return gbc;
    }

    public static GridBagConstraints clientAlignConstraints(int x, int y) {
        return clientAlignConstraints(x, y, 1, 1);
    }

    public static GridBagConstraints clientAlignConstraints(int x, int y,
            int width, int height) {
        GridBagConstraints gbc = createConstraints(x, y, width, height);
        gbc.fill = GridBagConstraints.BOTH;
        return gbc;
    }

    /**
     * Create GridBagConstraints for placing a component at (x,y) with the
     * dimension (width x height).
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
    public static GridBagConstraints createConstraints(int x, int y, int width,
            int height) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        return gbc;
    }

    public static GridBagConstraints textFieldConstraints(int x, int y) {
        return textFieldConstraints(x, y, 1, 1);
    }

    /**
     * Create GridBagConstraints for placing a text component at (x,y) with the
     * dimension (width x height).
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
    public static GridBagConstraints textFieldConstraints(int x, int y,
            int width, int height) {
        GridBagConstraints gbc = createConstraints(x, y, width, height);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = inputFieldInsets;
        return gbc;
    }

    public static Object textAreaConstraints(int x, int y) {
        return textAreaConstraints(x, y, 1, 1);
    }

    public static Object textAreaConstraints(int x, int y, int width, int height) {
        GridBagConstraints gbc = createConstraints(x, y, width, height);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = inputFieldInsets;
        return gbc;
    }
}
