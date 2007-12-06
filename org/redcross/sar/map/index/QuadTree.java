package org.redcross.sar.map.index;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import com.esri.arcgis.geometry.Envelope;

public class QuadTree {

	private Tile root; // The root of the quad tree

	private int numLevels;
	
	// Logger for this object
	private static final Logger LOGGER = Logger.getLogger(
			"com.geodata.engine.disko.index");

	/**
	 * Constructs av QuadTree
	 * @param extent The extent of this QuadTree
	 * @param numLevels Number of levels of this QuadTree
	 */
	public QuadTree(Envelope extent, int numLevels) {
		this.numLevels = numLevels;

		// constructs the root
		root = new Tile(extent);
	}

	/**
	 * Get all tiles in this QuadTree which intersects the given bounding box
	 * @param list A list of intersecting tiles
	 * @param bounds The bounding box
	 */
	public void getTiles(List<Tile> list, Envelope view) throws IOException {
		getTiles(root, Tile.INTERSECTING, view, list);
	}
	
	/**
	 * Invalidate the tiles in the quad tree which intersect the given Envelope
	 * @param view Envelope
	 */
	public void invalidate(Envelope view) throws IOException {
		ArrayList<Tile> tiles = new ArrayList<Tile>();
		getTiles(tiles, view);
		int num = 0;
		for (int i = 0; i < tiles.size(); i++) {
			Tile tile = (Tile) tiles.get(i);
			Tile parent = tile.getParent();
			if (parent != null) {
				parent.removeChild(tile);
				num++;
			}
		}
		System.gc(); // call garbage colllector
		LOGGER.info("Number of tiles invalidated: " + num);
	}


	private boolean getTiles(Tile tile, int parentVis, Envelope view, 
			List<Tile> list) throws IOException {
		if (tile == null) {
			return false;
		}
		parentVis = tile.getVisibility(view);
		if (parentVis == Tile.OUTSIDE) {
			return false;
		}
		// Search children
		if (tile.getLevel() < numLevels) {
			boolean has_child = false;
			for (int child_dir = 0; child_dir < 4; child_dir++) {
				if (getTiles(tile.traverse(child_dir), parentVis, view, list)) {
					has_child = true;
				}
			}
			if (has_child) {
				return true;
			}
		}
		list.add(tile); // tile found
		return true;
	}
}
