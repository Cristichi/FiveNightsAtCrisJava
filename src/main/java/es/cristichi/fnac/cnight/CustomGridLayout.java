package es.cristichi.fnac.cnight;

import java.awt.*;

/**
 * Implementation of {@link LayoutManager} that orders everything in a Grid on the fly, prioritizing top and left.
 */
public class CustomGridLayout implements LayoutManager {
    /** Horizontal spacing between components. */
    private final int hGap;
    /** Vertical spacing between components. */
    private final int vGap;
    
    /**
     * Creates a new CustomGridLayout.
     * @param hGap Horizontal gap.
     * @param vGap Vertical gap.
     */
    public CustomGridLayout(int hGap, int vGap) {
        this.hGap = hGap;
        this.vGap = vGap;
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            int width = parent.getWidth(); // Available width in the parent container
            if (width == 0) return new Dimension(0, 0); // Prevents issues during initialization

            Insets insets = parent.getInsets();
            width -= insets.left + insets.right; // Subtract insets

            int maxComponentWidth = 0;
            int maxComponentHeight = 0;

            // Calculate the maximum preferred size of components
            for (Component comp : parent.getComponents()) {
                Dimension size = comp.getPreferredSize();
                maxComponentWidth = Math.max(maxComponentWidth, size.width);
                maxComponentHeight = Math.max(maxComponentHeight, size.height);
            }

            // Calculate the number of columns that fit in the available width
            int cols = Math.max(1, (width + hGap) / (maxComponentWidth + hGap));
            int rows = (int) Math.ceil((double) parent.getComponentCount() / cols);

            // Total preferred size of the container
            int totalWidth = cols * maxComponentWidth + (cols - 1) * hGap;
            int totalHeight = rows * maxComponentHeight + (rows - 1) * vGap;

            return new Dimension(totalWidth + insets.left + insets.right,
                    totalHeight + insets.top + insets.bottom);
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        // We can reuse preferred size here for simplicity
        return preferredLayoutSize(parent);
    }

    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            int width = parent.getWidth();
            Insets insets = parent.getInsets();
            width -= insets.left + insets.right; // Subtract insets

            int x = insets.left;
            int y = insets.top;

            int maxComponentWidth = 0;
            int maxComponentHeight = 0;

            // Calculate the maximum preferred size of components
            for (Component comp : parent.getComponents()) {
                Dimension size = comp.getPreferredSize();
                maxComponentWidth = Math.max(maxComponentWidth, size.width);
                maxComponentHeight = Math.max(maxComponentHeight, size.height);
            }

            // Calculate the number of columns that fit in the available width
            int cols = Math.max(1, (width + hGap) / (maxComponentWidth + hGap));

            // Position each component in the grid
            int col = 0;
            for (Component comp : parent.getComponents()) {
                if (comp.isVisible()) {
                    comp.setBounds(x, y, maxComponentWidth, maxComponentHeight);
                    col++;
                    if (col < cols) {
                        x += maxComponentWidth + hGap;
                    } else {
                        col = 0;
                        x = insets.left;
                        y += maxComponentHeight + vGap;
                    }
                }
            }
        }
    }
}
