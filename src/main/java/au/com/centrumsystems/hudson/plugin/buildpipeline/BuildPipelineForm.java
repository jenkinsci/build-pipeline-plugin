package au.com.centrumsystems.hudson.plugin.buildpipeline;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Iterables;

/**
 * @author Centrum Systems
 * 
 *         Representation of the projects and their related builds making up the build pipeline view
 * 
 */
public class BuildPipelineForm {
    /**
     * projects laid out in a grid using maps to ease accessing (or maybe I made it way too complicated by not using a 2-dimensional array)
     * Outside map holds rows and inner map has ProjectForm at a particular position (defined with key)
     */
    private final ProjectGrid projectGrid;
    /**
     * a list of maps of map represents build pipelines laid out in grids, similar to projectGrid, but we have many of these grids
     */
    private final List<BuildGrid> buildGrids;

    /**
     * 
     * @param grid
     *            Project to be laid out in a grid
     * @param builds
     *            builds to be laid out in a grid
     */
    public BuildPipelineForm(final ProjectGrid grid, final Iterable<BuildGrid> builds) {
        projectGrid = grid;
        buildGrids = Arrays.asList(Iterables.toArray(builds, BuildGrid.class));
        // Handle lastSuccessful Builds correctly, when some jobs are used in multiple Views
        grid.correctProjectForms(builds);
    }

    public ProjectGrid getProjectGrid() {
        return projectGrid;
    }

    /**
     * grid width is the longest column map counting empties (keys represent position, so they are used to determine width)
     * 
     * @return width
     */
    public Integer getGridWidth() {
        return projectGrid.getColumns();
    }

    public Integer getGridHeight() {
        return projectGrid.getRows();
    }

    public List<BuildGrid> getBuildGrids() {
        return buildGrids;
    }

}
