package au.com.centrumsystems.hudson.plugin.buildpipeline;

import hudson.model.AbstractBuild;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link BuildGrid} implementation backed by a sparse array.
 *
 * @author Kohsuke Kawaguchi
 */
public class DefaultBuildGridImpl extends BuildGrid {
    /**
     * Actual data.
     */
    private final Map<Integer/* row */, Map<Integer/* height */, BuildForm>> data = new HashMap<Integer, Map<Integer, BuildForm>>();

    /**
     * Dimension of the {@link #data}
     */
    private int rows, cols;

    /**
     * Mutable, but only for the code that instantiates {@link DefaultBuildGridImpl}.
     *
     * @param row
     *      position of the form
     * @param col
     *      position of the form
     * @param p
     *      The build to add. null to remove the value.
     */
    public void set(int row, int col, BuildForm p) {
        Map<Integer, BuildForm> c = data.get(row);
        if (c == null) {
            c = new HashMap<Integer, BuildForm>();
            data.put(row, c);
        }
        c.put(col, p);

        rows = Math.max(rows, row + 1);
        cols = Math.max(cols, col + 1);
    }

    @Override
    public BuildForm get(int row, int col) {
        final Map<Integer, BuildForm> cols = data.get(row);
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

    @Override
    public BuildForm findBuildForm(AbstractBuild<?, ?> build) {
        if (build != null) {
            final String fullBuildName = build.getFullDisplayName();
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    final BuildForm form = get(r, c);
                    if (form != null) {
                        if (fullBuildName.equals(form.getFullBuildName())) {
                            return form;
                        }
                    }
                }
            }
        }
        return null;
    }
}
