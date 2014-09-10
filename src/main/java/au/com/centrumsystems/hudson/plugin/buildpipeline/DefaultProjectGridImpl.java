package au.com.centrumsystems.hudson.plugin.buildpipeline;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link ProjectGrid} backed by map.
 *
 * @author Kohsuke Kawaguchi
 * @author Centrum Systems
 */
public abstract class DefaultProjectGridImpl extends ProjectGrid {
    /**
     * Actual data store is a sparse map.
     */
    private final Map<Integer/*row*/, Map<Integer/*height*/, ProjectForm>> data = new HashMap<Integer, Map<Integer, ProjectForm>>();

    /**
     * Dimension of the {@link #data}
     */
    private int rows, cols;

    /**
     * Mutable, but only for {@link ProjectGridBuilder}
     *
     * @param row
     *      position of the form
     * @param col
     *      position of the form
     * @param p
     *      The project to add. null to remove the value.
     */
    public void set(int row, int col, ProjectForm p) {
        Map<Integer, ProjectForm> c = data.get(row);
        if (c == null) {
            c = new HashMap<Integer, ProjectForm>();
            data.put(row, c);
        }
        c.put(col, p);

        rows = Math.max(rows, row + 1);
        cols = Math.max(cols, col + 1);
    }

    /**
     * Gets the project at the specified location.
     *
     * @param row
     *      position of the form
     * @param col
     *      position of the form
     * @return
     *      possibly null.
     */
    @Override
    public ProjectForm get(int row, int col) {
        final Map<Integer, ProjectForm> cols = data.get(row);
        if (cols == null) {
            return null;
        }
        return cols.get(col);
    }

    @Override
    public int getColumns() {
        return cols;
    }

    @Override
    public int getRows() {
        return rows;
    }

}
