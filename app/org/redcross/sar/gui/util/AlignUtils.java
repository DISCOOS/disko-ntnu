package org.redcross.sar.gui.util;

import java.awt.geom.Rectangle2D;

public class AlignUtils {

    /** Center alignment. */
    public static final int CENTER = 0x00;

    /** Top alignment. */
    public static final int TOP = 0x01;

    /** Bottom alignment. */
    public static final int BOTTOM = 0x02;

    /** Left alignment. */
    public static final int LEFT = 0x04;

    /** Right alignment. */
    public static final int RIGHT = 0x08;

    /** Top/Left alignment. */
    public static final int TOP_LEFT = TOP | LEFT;

    /** Top/Right alignment. */
    public static final int TOP_RIGHT = TOP | RIGHT;

    /** Bottom/Left alignment. */
    public static final int BOTTOM_LEFT = BOTTOM | LEFT;

    /** Bottom/Right alignment. */
    public static final int BOTTOM_RIGHT = BOTTOM | RIGHT;

    /** Horizontal fit. */
    public static final int FIT_HORIZONTAL = LEFT | RIGHT;

    /** Vertical fit. */
    public static final int FIT_VERTICAL = TOP | BOTTOM;

    /** Complete fit. */
    public static final int FIT = FIT_HORIZONTAL | FIT_VERTICAL;

    /** North alignment (same as TOP). */
    public static final int NORTH = TOP;

    /** South alignment (same as BOTTOM). */
    public static final int SOUTH = BOTTOM;

    /** West alignment (same as LEFT). */
    public static final int WEST = LEFT;

    /** East alignment (same as RIGHT). */
    public static final int EAST = RIGHT;

    /** North/West alignment (same as TOP_LEFT). */
    public static final int NORTH_WEST = NORTH | WEST;

    /** North/East alignment (same as TOP_RIGHT). */
    public static final int NORTH_EAST = NORTH | EAST;

    /** South/West alignment (same as BOTTOM_LEFT). */
    public static final int SOUTH_WEST = SOUTH | WEST;

    /** South/East alignment (same as BOTTOM_RIGHT). */
    public static final int SOUTH_EAST = SOUTH | EAST;

    /** Vertical alignment (NORTH | SOUTH). */
    public static final int VERTICAL = NORTH | SOUTH;

    /** Horizontal alignment (WEST | EAST). */
    public static final int HORIZONTAL = WEST | EAST;

    /** unit length of movement between quadrants in x-direction */
    private static final double[][] qX = new double[][]{
        {0,-0.5,0.5,0.5,-0.5}, 	// CENTER --> ? quadrant
        {0.5,0,1,1,0}, 			// NW --> ? quadrant
        {-0.5,-1,0,1,-1}, 		// NE --> ? quadrant
        {-0.5,-1,0,0,-1}, 		// SE --> ? quadrant
        {0.5,0,1,1,0}, 			// SW --> ? quadrant
    };

    /** unit length of movement between quadrants in y-direction */
    private static final double[][] qY = new double[][]{
        {0,-0.5,-0.5,0.5,0.5}, 	// CENTER --> ? quadrant
        {0.5,0,0,1,1}, 			// NW --> ? quadrant
        {0.5,0,0,1,1}, 			// NE --> ? quadrant
        {-0.5,-1,-1,0,0}, 		// SE --> ? quadrant
        {-0.5,-1,-1,0,0}, 		// SW --> ? quadrant
    };

    /**
     * Aligns a rectangle with given frame using the specified alignment method
     *
     * @param Rectangle2D frame - The frame rectangle to align with
     * @param int anchor - The alignment anchor point on frame rectangle
     * @param int width - The requested rectangle width after alignment
     * @param int height - The requested rectangle height after alignment
     * @param int alignment - The alignment to the anchor point
     *
     * @return Rectangle2D - The rectangle after alignment. Depending on the options
     */
    public static Rectangle2D align(Rectangle2D frame, int anchor, double width, double height, double gap, int alignment, int options) {

    	// initialize
    	Rectangle2D rc = null;

    	/* =====================================================
    	 * Algorithm
    	 * -----------------------------------------------------
    	 *
    	 * This algorithm is based on a two-step unit length
    	 * translation along the x and y-axis.
    	 *
    	 * The steps are:
    	 *
    	 * 1. Calculate anchor position from frame position
    	 * 2. Calculate rectangle position from anchor position
    	 *
    	 * ===================================================== */

    	// get anchor quadrant
    	int q1 = quadrant(anchor);

    	// quadrant found?
    	if(q1!=-1) {

    		// get frame width and height
    		double w = frame.getWidth();
    		double h = frame.getHeight();

	    	// get anchor position in frame coordinates
	    	double ax = frame.getX() + w*qX[1][q1];
	    	double ay = frame.getY() + h*qY[1][q1];

	    	// alignment quadrant
	    	int q2 = quadrant(alignment);

	    	// quadrant found?
	    	if(q2!=-1) {

	    		// get translation
	    		double dx = width*qX[q2][1];
	    		double dy = height*qY[q2][1];

		    	// translate anchor position into rectangle coordinates
		    	double rx = ax + dx;
		    	double ry = ay + dy;

	    		// get rectangle position relative to frame
		    	int p = position(frame, ax + qX[q2][1], ay + qY[q2][1]);

		    	// fit to frame width?
	    		if (p==0 && (options & FIT_HORIZONTAL) == FIT_HORIZONTAL) {
	    			double ex = (rx + width) - (frame.getX() + w);
	    			if(ex!=0) width -= ex;
		    	}

		    	// fit to frame height?
	    		if (p==0 && (options & FIT_VERTICAL) == FIT_VERTICAL) {
	    			double ey = (ry + height) - (frame.getY() + h);
	    			if(ey!=0) height -= ey;
	    		}

	    		// apply options?
		    	if(options!=0) {

			    	// fit to frame width?
		    		if ((options & FIT_HORIZONTAL) == FIT_HORIZONTAL) {
		    			// fit to width?
		    			if(p == VERTICAL) {
			    			rx = frame.getX();
			    			width = w;
		    			}
			    	}

			    	// limit to frame height?
		    		if ((options & FIT_VERTICAL) == FIT_VERTICAL) {
		    			// fit to height?
		    			if(p == HORIZONTAL) {
			    			ry = frame.getY();
			    			height = h;
		    			}

		    		}
		    	}

		    	// adjust for gap
		    	//switch(p) {
		    	//case
		    	//}


		    	// create alignment rectangle
		    	rc = new Rectangle2D.Double(rx,ry,width,height);

	    	}

    	}

    	// finished
        return rc;
    }

    private static int quadrant(int align) {
        if(align == CENTER) return 0;
        if((align & NORTH_WEST) == NORTH_WEST) return 1;
        else if((align & NORTH_EAST) == NORTH_EAST) return 2;
        else if((align & SOUTH_EAST) == SOUTH_EAST) return 3;
        else if((align & SOUTH_WEST) == SOUTH_WEST) return 4;
        return -1;
    }

    private static int position(Rectangle2D frame, double x, double y) {
    	int qx = CENTER;
    	int qy = CENTER;
    	if(!frame.contains(x, y)) {
    		qx = (x >= frame.getX() && frame.getX() + frame.getWidth() <= x ? VERTICAL : (x < frame.getX() ? WEST : EAST));
    		qy = (y >= frame.getY() && frame.getY() + frame.getHeight() <= y ? HORIZONTAL : (y < frame.getY() ? NORTH : SOUTH));
    	}
    	// finished
    	return qx | qy;
    }

	/**
	 * The main method.
	 *
	 * @param args
	 */
	public static void main(String[] args)
	{
		Rectangle2D frame = new Rectangle2D.Double(0,0,10,10);
		Rectangle2D rc = align(frame, NORTH_WEST, 30, 5, 2, NORTH_EAST, FIT);

		System.out.println("IN:="+frame);
		System.out.println("OUT:="+rc);

	}


}
