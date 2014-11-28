package au.com.centrumsystems.hudson.plugin.buildpipeline;

/**
 * Two-dimensional finite sparse placement of things (such as projects and builds) into a grid/matrix layout.
 *
 * @param <T>
 *     The type of the data that gets placed in a two dimensional table.
 * @author Kohsuke Kawaguchi
 */
public abstract class Grid<T> {
    /**
     * Height of the grid. Total number of rows.
     *
     * @return positive integer
     */
    public abstract int getRows();

    /**
     * Width of the grid. Total number of columns.
     *
     * @return positive integer
     */
    public abstract int getColumns();

    /**
     * Obtains the project placed at the specific position.
     *
     * @param row
     *      {@code 0&lt;=row&lt;getRows()}
     * @param col
     *      {@code 0&lt;=col&lt;getColumns()}
     * @return
     *      null if there's nothing placed in that position.
     */
    public abstract T get(int row, int col);

    /**
     * Tests if the layout is empty.
     *
     * @return
     *      true if this grid contains no {@link ProjectForm} at all.
     */
    public boolean isEmpty() {
        return getRows() == 0; // && getColumns()==0; -- testing one is enough
    }

    /**
     * Determines the next row of the grid that should be populated.
     *
     * Given (currentRow,currentColumn), find a row R>=currentRow such that
     * the row R contains no project to any column to the right of current column.
     * That is, find the row in which we can place a sibling of the project
     * placed in (currentRow,currentColumn).
     *
     * This method is useful for determining the position to insert a {@link ProjectForm}
     * when the layout is tree-like.
     *
     * @param currentRow
     *            - The current row of the grid being used
     * @param currentColumn
     *            - The current column of the grid being used
     * @return - The row number to be used
     */
    public int getNextAvailableRow(final int currentRow, final int currentColumn) {
        final int rows = getRows();
        for (int nextRow = currentRow; nextRow < rows; nextRow++) {
            if (hasDataToRight(nextRow, currentColumn)) {
                nextRow++;
            } else {
                return nextRow;
            }
        }
        return rows;
    }

    /**
     * Tests if the row of the grid already contains entries in the columns greater than the entered column.
     *
     * @param row
     *            - The row of the grid
     * @param col
     *            - The current column of the grid
     * @return - true: The row does contain data in the columns greater than col, false: The row does not contain data in the columns
     *         greater than col
     */
    private boolean hasDataToRight(final int row, final int col) {
        final int cols = getColumns();
        for (int i = col; i < cols; i++) {
            if (get(row, i) != null) {
                return true;
            }
        }
        return false;
    }
}
