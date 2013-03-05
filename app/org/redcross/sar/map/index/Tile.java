package org.redcross.sar.map.index;

import java.io.IOException;
import java.util.ArrayList;

import com.esri.arcgis.geometry.Envelope;

public class Tile {

	// Internal data
	@SuppressWarnings("unused")
	private int childDir;

	private int level;

	// Relatives
	private Tile parent;

	private Tile[] children = new Tile[4];

	// tile extent
	private Envelope extent;

	// list of data assosiated with this tile
	private ArrayList<Object> userdata;

	// Flag that tells this Tile is filled
	private boolean isFilled = false;

	// Tile is inside or outside
	public static final int INSIDE = 0;

	// Tile is outside
	public static final int OUTSIDE = 1;

	// Tile si intersecting
	public static final int INTERSECTING = 2;

	//The SouthWest child quadrant.
	static final int SW_CHILD = 0;

	// The SouthEast child quadrant.
	static final int SE_CHILD = 1;

	// The NorthWest child quadrant.
	static final int NW_CHILD = 2;

	// The NorthEast child quadrant.
	static final int NE_CHILD = 3;

	/**
	 * Constructs a Tile in the QuadTree
	 * @param extent The extent of this Tile
	 */
	public Tile(Envelope extent) {
		this.level = 1;
		this.extent = extent;
	}

	/**
	 * Constructs a Tile in the QuadTree
	 * @param parent The parent Tile of this Tile
	 * @param childDir The quadrant this Tile resides (NW,NE,SE,SW).
	 */
	public Tile(Tile parent, int childDir) throws IOException {
		this.parent = parent;
		this.childDir = childDir;
		this.level = parent.level + 1;
		calculateExtent(parent.extent, childDir);
		parent.children[childDir] = this;
	}

	private void calculateExtent(Envelope parentExtent, int childDir) throws IOException {
		double xmin = 0, ymin = 0, xmax = 0, ymax = 0;
		if (childDir == NW_CHILD) {
			xmin = parentExtent.getXMin();
			ymin = parentExtent.getYMin() + parentExtent.getHeight() / 2;
			xmax = parentExtent.getXMin() + parentExtent.getWidth() / 2;
			ymax = parentExtent.getYMax();
		} else if (childDir == NE_CHILD) {
			xmin = parentExtent.getXMin() + parentExtent.getWidth() / 2;
			ymin = parentExtent.getYMin() + parentExtent.getHeight() / 2;
			xmax = parentExtent.getXMax();
			ymax = parentExtent.getYMax();
		} else if (childDir == SE_CHILD) {
			xmin = parentExtent.getXMin() + parentExtent.getWidth() / 2;
			ymin = parentExtent.getYMin();
			xmax = parentExtent.getXMax();
			ymax = parentExtent.getYMin() + parentExtent.getHeight() / 2;
		} else if (childDir == SW_CHILD) {
			xmin = parentExtent.getXMin();
			ymin = parentExtent.getYMin();
			xmax = parentExtent.getXMin() + parentExtent.getWidth() / 2;
			ymax = parentExtent.getYMin() + parentExtent.getHeight() / 2;
		}
		extent = new Envelope();
		extent.setXMin(xmin);
		extent.setYMin(ymin);
		extent.setXMax(xmax);
		extent.setYMax(ymax);
	}

	/**
	 * Gets the extent
	 */
	public Envelope getExtent() {
		return extent;
	}

	/**
	 * Gets the levels this Tile has in the quad tree structure.
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * Mark this tile visited
	 */
	public Tile traverse(int child_dir) throws IOException {
		return getChild(child_dir);
	}

	public Tile getParent() {
		return parent;
	}

	/**
	 * Get an array containing all 4 children
	 * @return Tile[]
	 */
	public Tile[] getChildren() {
		return children;
	}

	/**
	 * Get a new child at the specified direction
	 * @param child_dir int
	 * @return A child Tile
	 */
	public Tile getChild(int child_dir) throws IOException {
		if (children[child_dir] == null) {
			children[child_dir] = new Tile(this, child_dir);
		}
		return children[child_dir];
	}

	/**
	 * Removes a child tile at the specified direction
	 * @param child_dir int
	 */
	public void removeChild(int child_dir) {
		children[child_dir].parent = null;
		children[child_dir] = null;
	}

	/**
	 * Removes the specified child
	 * @param child The child to remove
	 */
	public void removeChild(Tile child) {
		for (int child_dir = 0; child_dir < 4; child_dir++) {
			if (children[child_dir] == child) {
				removeChild(child_dir);
			}
		}
	}

	/**
	 * Checks if the specified map extent contains, intersects of is outside
	 * this tile extent
	 * @param mapExtent.
	 */
	int getVisibility(Envelope view) throws IOException {
		if (extent.contains(view)) {
			return INSIDE;
		}
		if (!extent.disjoint(view)) {
			return INTERSECTING;
		}
		return OUTSIDE;
	}

	/**
	 * Add userdata to this tile
	 * @param data Object theobject to add
	 */
	public void addData(Object data) {
		if (userdata == null) {
			userdata = new ArrayList<Object>();
		}
		userdata.add(data);
	}

	public Object getdata(int index) {
		if (userdata == null) {
			return null;
		}
		return userdata.get(index);
	}

	public int getNumUserData() {
		if (userdata == null) {
			return 0;
		}
		return userdata.size();
	}

	public boolean isFilled() {
		return isFilled;
	}

	public void setIsFilled(boolean b) {
		isFilled = b;
	}

	/**
	 * Getting information about this ImageTile.
	 */
	public String toString() {
		return "level=" + level + " extent=" + extent;
	}
}
