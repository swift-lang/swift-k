
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.Arrays;

/**
 *  This class is a layout manager which places it's components in a grid layout.
 *  The difference from the standard grid layout is that this one adjusts the
 *  sizes of the rows/columns according to the dimensions of the components,
 *  therefore not restricting a colummn or row to a specific width/height. It
 *  also takes into account the alignment of the components when placing them
 *  into the cells
 */
public class SimpleGridLayout implements LayoutManager2 {

	private int hGap = 4;
	private int vGap = 4;
	private int nRows, nCols;
	private Component[][] grid;

	public final static int Expand = 99999;

	public SimpleGridLayout(int nRows, int nCols) {
		if ((nRows == 0) || (nCols == 0)) {
			throw new RuntimeException("Invalid grid size for MyGridLayout");
		}
		this.nRows = nRows;
		this.nCols = nCols;
		grid = new Component[nRows][nCols];

		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++) {
				grid[i][j] = null;
			}
		}
	}

	public SimpleGridLayout(int nRows, int nCols, int hGap, int vGap) {
		this(nRows, nCols);
		this.hGap = hGap;
		this.vGap = vGap;
	}

	/**
	 *  Add a new component to the layout manager.
	 *
	 *@param  comp         The feature to be added to the LayoutComponent attribute
	 *@param  constraints  The feature to be added to the LayoutComponent attribute
	 */
	public void addLayoutComponent(Component comp, Object constraints) {
		if (constraints instanceof GridPosition) {
			int row = ((GridPosition) constraints).getRow();
			int col = ((GridPosition) constraints).getCol();

			if ((row >= nRows) || (col >= nCols)) {
				this.addLayoutComponent(comp, null);
			}
			else {
				grid[row][col] = comp;
			}
		}
		else {
			//add it to the first available spot
			for (int i = 0; i < nRows; i++) {
				for (int j = 0; j < nCols; j++) {
					if (grid[i][j] == null) {
						grid[i][j] = comp;
						i = nRows;
						break;
					}
				}
			}
		}
	}

	public void addLayoutComponent(String title, Component comp) {
		this.addLayoutComponent(comp, null);
	}

	/**
	 *  set the container of this layout manager
	 *
	 *@param  container  the container of this layout manager
	 */
	public void layoutContainer(Container container) {

		Insets insets = container.getInsets();
		int crtX;
		int crtY;

		crtX = insets.left;
		crtY = insets.top;

		int[] widths = new int[nCols];
		int[] heights = new int[nRows];
		int height = 0;
		int width = 0;
		int xSprings = 0;
		int ySprings = 0;

		Arrays.fill(widths, 0);
		Arrays.fill(heights, 0);

		//first pass; calculate the fixed cells
		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++) {
				if (grid[i][j] == null) {
					continue;
				}

				Dimension cSize = grid[i][j].getPreferredSize();
				if (cSize == null) {
					cSize = new Dimension(Expand, Expand);
				}

				if (widths[j] < cSize.width) {
					widths[j] = cSize.width;
				}

				if (heights[i] < cSize.height) {
					heights[i] = cSize.height;
				}

			}
		}
		width += hGap * (nCols - 1) + insets.left + insets.right;
		height += vGap * (nRows - 1) + insets.top + insets.bottom;

		//sum the heights/spring heights
		for (int i = 0; i < nRows; i++) {
			if (heights[i] != Expand) {
				height += heights[i];
			}
			else {
				ySprings++;
			}
		}

		for (int i = 0; i < nCols; i++) {
			if (widths[i] != Expand) {
				width += widths[i];
			}
			else {
				xSprings++;
			}
		}

		//second pass; fill in the expandable cells
		int xSpringFactor = 1;
		int ySpringFactor = 1;
		float xFactor = 1;
		float yFactor = 1;

		Dimension CSize = container.getSize();
		if (CSize == null) {
			CSize = new Dimension(width, height);
		}

		if (xSprings != 0) {
			xSpringFactor = (CSize.width - width) / xSprings;
		}
		else if (CSize.width != width) {
			xFactor = (float) CSize.width / width;
			for (int i = 0; i < nCols; i++) {
				widths[i] = (int) (xFactor * widths[i]);
			}
		}

		if (ySprings != 0) {
			ySpringFactor = (CSize.height - height) / ySprings;
		}
		else if (CSize.height != height) {
			yFactor = (float) CSize.height / height;
			for (int i = 0; i < nRows; i++) {
				heights[i] = (int) (yFactor * heights[i]);
			}
		}

		for (int i = 0; i < nRows; i++) {
			if (heights[i] == Expand) {
				heights[i] = ySpringFactor;
			}
		}

		for (int i = 0; i < nCols; i++) {
			if (widths[i] == Expand) {
				widths[i] = xSpringFactor;
			}
		}

		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++) {
				if (grid[i][j] != null) {

					Component Comp = grid[i][j];

					Dimension cSize = Comp.getPreferredSize();

					if (cSize == null) {
						cSize = new Dimension(Expand, Expand);
					}

					int cw = cSize.width;
					int ch = cSize.height;

					if (cw == Expand) {
						cw = xSpringFactor;
					}

					if (ch == Expand) {
						ch = ySpringFactor;
					}

					float hSpace = 0;
					float vSpace = 0;

					if (widths[j] != Expand) {
						hSpace = widths[j] - cw;
					}
					if (heights[i] != Expand) {
						vSpace = heights[i] - ch;
					}

					int vPos = (int) (vSpace * Comp.getAlignmentY());
					int hPos = (int) (hSpace * Comp.getAlignmentX());

					Comp.setSize(cw, ch);
					Comp.setLocation(crtX + hPos, crtY + vPos);
				}
				crtX += widths[j] + hGap;

				if (j == nCols - 1) {
					crtY += heights[i] + vGap;
					crtX = insets.left;
				}
			}
		}
	}

	/**
	 *  get the minimum layout size for the specified container
	 *
	 *@param  container  the container to be checked for it's minimum layout size
	 *@return            the minimum layout size as a Dimension object
	 */

	public Dimension minimumLayoutSize(Container container) {
		Insets insets = container.getInsets();
		int height = insets.top + insets.bottom;
		int width = insets.left + insets.right;

		int[] widths = new int[nCols];
		int[] heights = new int[nRows];

		Arrays.fill(widths, 0);
		Arrays.fill(heights, 0);

		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++) {
				if (grid[i][j] == null) {
					continue;
				}

				Dimension cSize = grid[i][j].getMinimumSize();

				if (widths[j] < cSize.width) {
					widths[j] = cSize.width;
				}
				if (heights[i] < cSize.height) {
					heights[i] = cSize.height;
				}
			}
		}
		for (int i = 0; i < nRows; i++) {
			height += heights[i];
		}
		height += vGap * (nRows - 1);

		for (int i = 0; i < nCols; i++) {
			width += widths[i];
		}
		width += hGap * (nCols - 1);
		return new Dimension(width, height);
	}

	public Dimension preferredLayoutSize(Container container) {
		if (container instanceof GridContainer) {
			return container.getPreferredSize();
		}
		return container.getSize();
	}

	/**
	 *  get the preferred layout size for the specified container
	 *
	 *@param  container  the container to be checked for it's preferred layout size
	 *@return            the preferred layout size as a Dimension object
	 */
	public Dimension preferredLayoutSize1(Container container) {
		Insets insets = container.getInsets();
		int height = insets.top + insets.bottom;
		int width = insets.left + insets.right;
		int[] widths = new int[nCols];
		int[] heights = new int[nRows];

		Arrays.fill(widths, 0);
		Arrays.fill(heights, 0);
		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++) {
				if (grid[i][j] == null) {
					continue;
				}

				Dimension cSize = grid[i][j].getPreferredSize();

				if (widths[j] < cSize.width) {
					widths[j] = cSize.width;
				}
				if (heights[i] < cSize.height) {
					heights[i] = cSize.height;
				}
			}
		}
		for (int i = 0; i < nRows; i++) {
			height += heights[i];
		}
		height += vGap * (nRows - 1);
		for (int i = 0; i < nCols; i++) {
			width += widths[i];
		}
		width += hGap * (nCols - 1);
		return new Dimension(width, height);
	}

	public Dimension maximumLayoutSize(Container container) {
		Insets insets = container.getInsets();
		int height = insets.top + insets.bottom;
		int width = insets.left + insets.right;

		int[] widths = new int[nCols];
		int[] heights = new int[nRows];

		Arrays.fill(widths, 0);
		Arrays.fill(heights, 0);

		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++) {
				if (grid[i][j] == null) {
					continue;
				}

				Dimension cSize = grid[i][j].getMaximumSize();

				if (widths[j] < cSize.width) {
					widths[j] = cSize.width;
				}
				if (heights[i] < cSize.height) {
					heights[i] = cSize.height;
				}
			}
		}
		for (int i = 0; i < nRows; i++) {
			height += heights[i];
		}
		height += vGap * (nRows - 1);

		for (int i = 0; i < nCols; i++) {
			width += widths[i];
		}
		width += hGap * (nCols - 1);
		return new Dimension(width, height);
	}

	public float getLayoutAlignmentX(Container c) {
		return (float) 0.5;
	}

	public float getLayoutAlignmentY(Container c) {
		return (float) 0.5;
	}

	/**
	 *  remove the specified component from this layout manager
	 *
	 *@param  component  the component to be removed
	 */
	public void removeLayoutComponent(Component component) {
	}

	public void invalidateLayout(Container c) {
	}
}
