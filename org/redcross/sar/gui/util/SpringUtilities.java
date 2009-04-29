
/*
 * Copyright (c) 1995 - 2008 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

package org.redcross.sar.gui.util;

import javax.swing.*;
import javax.swing.SpringLayout.Constraints;

import org.apache.log4j.Logger;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;

/**
 * A 1.4 file that provides utility methods for
 * creating form- or grid-style layouts with SpringLayout.
 * 
 */
public class SpringUtilities {

	private static final Logger logger = Logger.getLogger(SpringUtilities.class);
	
	/**
	 * A debugging utility that prints to stdout the component's
	 * minimum, preferred, and maximum sizes.
	 */
	public static void printSizes(Component c) {
		System.out.println("minimumSize = " + c.getMinimumSize());
		System.out.println("preferredSize = " + c.getPreferredSize());
		System.out.println("maximumSize = " + c.getMaximumSize());
	}	

	/**
	 * Aligns the first <code>rows</code> * <code>cols</code>
	 * components of <code>parent</code> in
	 * a grid. Each component is as big as the maximum
	 * preferred width and height of the components.
	 * The parent is made just big enough to fit them all.
	 *
	 * @param rows number of rows
	 * @param cols number of columns
	 * @param initialX x location to start the grid at
	 * @param initialY y location to start the grid at
	 * @param xPad x padding between cells
	 * @param yPad y padding between cells
	 * @param autoResizeX content width is resized with parent in if <code>true</code>, constant otherwise
	 * @param autoResizeY content height is resized with parent in if <code>true</code>, constant otherwise
	 */
	public static void makeGrid(Container parent,
			int rows, int cols,
			int initialX, int initialY,
			int xPad, int yPad,
			boolean autoResizeX, boolean autoResizeY) 
	{
		SpringLayout layout;
		try {
			layout = (SpringLayout)parent.getLayout();
		} catch (ClassCastException exc) {
			logger.error("The first argument to makeGrid must use SpringLayout.",exc);
			return;
		}

		Spring xPadSpring = Spring.constant(xPad);
		Spring yPadSpring = Spring.constant(yPad);
		Spring initialXSpring = Spring.constant(initialX);
		Spring initialYSpring = Spring.constant(initialY);
		int max = Math.min(Math.min(rows * cols,parent.getComponentCount()),parent.getComponentCount());

		//Calculate Springs that are the max of the width/height so that all
		//cells have the same size.
		Spring maxWidthSpring = layout.getConstraints(parent.getComponent(0)).
		getWidth();
		Spring maxHeightSpring = layout.getConstraints(parent.getComponent(0)).
		getHeight();
		for (int i = 1; i < max; i++) {
			SpringLayout.Constraints cons = layout.getConstraints(
					parent.getComponent(i));

			maxWidthSpring = Spring.max(maxWidthSpring, cons.getWidth());
			maxHeightSpring = Spring.max(maxHeightSpring, cons.getHeight());
		}

		//Apply the new width/height Spring. This forces all the
		//components to have the same size.
		for (int i = 0; i < max; i++) {
			SpringLayout.Constraints cons = layout.getConstraints(
					parent.getComponent(i));

			cons.setWidth(maxWidthSpring);
			cons.setHeight(maxHeightSpring);
		}

		//Then adjust the x/y constraints of all the cells so that they
		//are aligned in a grid.
		SpringLayout.Constraints tailCons = null;
		SpringLayout.Constraints lastRowCons = null;
		for (int i = 0; i < max; i++) {
			SpringLayout.Constraints cons = layout.getConstraints(
					parent.getComponent(i));
			if (i % cols == 0) { //start a new row
				lastRowCons = tailCons;
				cons.setX(initialXSpring);
			} else { //x position depends on previous component
				cons.setX(Spring.sum(tailCons.getConstraint(SpringLayout.EAST),
						xPadSpring));
			}

			if (i / cols == 0) { // first row
				cons.setY(initialYSpring);
			} else { //y position depends on previous row
				cons.setY(Spring.sum(lastRowCons.getConstraint(SpringLayout.SOUTH),
						yPadSpring));
			}
			tailCons = cons;
		}
		
		//Set the parent's size
		setParentSize(parent, layout, 
				autoResizeY?tailCons.getConstraint(SpringLayout.SOUTH):null, 
				autoResizeY?tailCons.getConstraint(SpringLayout.EAST):null, xPad, yPad);
		
	}
	
	/**
	 * Aligns the first <code>rows</code> * <code>cols</code>
	 * components of <code>parent</code> in
	 * a grid. Each component is as big as the maximum
	 * preferred width and height of the components.
	 * The parent is made just big enough to fit them all.
	 *
	 * @param rows number of rows
	 * @param cols number of columns
	 * @param initialX x location to start the grid at
	 * @param initialY y location to start the grid at
	 * @param xPad x padding between cells
	 * @param yPad y padding between cells
	 * @param cellSpanX[] number of columns to span
	 * @param cellSpanY[] number of rows to span
	 * @param autoResizeX content width is resized with parent in if <code>true</code>, constant otherwise
	 * @param autoResizeY content height is resized with parent in if <code>true</code>, constant otherwise
	 */
	public static void makeSpannedGrid(Container parent,
			int rows, int cols,
			int initialX, int initialY,
			int xPad, int yPad, 
			int cellSpanX[], int cellSpanY[],
			boolean autoResizeX, boolean autoResizeY) 
	{
		
		// get layout
		SpringLayout layout;
		try {
			layout = (SpringLayout)parent.getLayout();
		} catch (ClassCastException exc) {
			logger.error("The first argument to makeGrid must use SpringLayout.",exc);
			return;
		}
		
		// calculate
		int count = Math.min(rows * cols,parent.getComponentCount());
		
		// get component index span map
		int[][] map = getSpanMap(parent, layout, rows, cols, count, cellSpanX, cellSpanY);

		// create constants
		Spring xPadSpring = Spring.constant(xPad);
		Spring yPadSpring = Spring.constant(yPad);
		Spring initialXSpring = Spring.constant(initialX);
		Spring initialYSpring = Spring.constant(initialY);
		
		/* Calculate Springs that are the max of the height so
		 * that all cells on have the same height on each row */ 
		Spring max = null;
		List<Spring> heights = new ArrayList<Spring>();
		for (int i = 0; i < rows; i++) {
			
			// get index of first component in this row
			int k = map[i][0];
			
			// no more components?
			if(k==-1) break;
			
			//start a new row
			max = layout.getConstraints(parent.getComponent(k)).getHeight();
			
			// loop over all cells in this row
			for(int j = 1; j < cols; j++) {
				
				// get component index
				k = map[i][j];
				
				// no more components?
				if(k==-1) break;
				
				// get current 
				SpringLayout.Constraints cons = layout.getConstraints(parent.getComponent(k));
				
				// get maximum spring
				max = Spring.max(max, cons.getHeight());

				// span cells?
				while(j<cols-1&&map[i][j+1]==k) j++;
				
			}
			
			// add height of row
			heights.add(max);
			
		}
		
		/* Calculate Springs that are the max of the width so
		 * that all cells on have the same width on each column */ 
		List<Spring> widths = new ArrayList<Spring>();
		for (int j = 0; j < cols; j++) {
			
			// get index of first component in this column
			int k = map[0][j];
			
			// no more components?
			if(k==-1) break;
			
			//start a new row
			max = layout.getConstraints(parent.getComponent(k)).getWidth();
			
			// loop over all cells in this column
			for(int i = 1; i < rows; i++) {
				
				// get component index
				k = map[i][j];
				
				// no more components?
				if(k==-1) break;
				
				// get current 
				SpringLayout.Constraints cons = layout.getConstraints(parent.getComponent(k));
				
				// get maximum spring
				max = Spring.max(max, cons.getWidth());

				// span cells?
				while(i<rows-1&&map[i+1][j]==k) i++;
				
			}
			
			// add height of row
			widths.add(max);
			
		}
		
		// initialize visit matrix
		boolean[] isSet = new boolean[count];
		
		// Apply heights to cells in each row.
		Spring height = null;
		for (int i = 0; i < heights.size(); i++) {
			
			// get height of current row
			height = heights.get(i);
			
			// loop over all cells in this row
			for(int j = 0; j < cols; j++) {
				
				// get index of component
				int k = map[i][j];
				
				// no more components?
				if(k==-1) break;
				
				// get component constraints
				SpringLayout.Constraints cons = layout.getConstraints(parent.getComponent(k));
				
				// is height set already?
				if(isSet[k]) {
					cons.setHeight(Spring.sum(Spring.sum(height,cons.getHeight()),yPadSpring));
				}
				else {
					isSet[k] = true;
					cons.setHeight(height);
				}

				// span cells?
				while(j<cols-1&&map[i][j+1]==k) j++;				
				
			}			
		}
		
		// reset visit matrix
		isSet = new boolean[count];
		
		// Apply width to cells in each column.
		Spring width = null;
		for (int j = 0; j < cols; j++) {
			
			// get width of current column
			width = widths.get(j);
			
			// loop over all cells in this row
			for(int i = 0; i < rows; i++) {
				
				// get component index
				int k = map[i][j];
				
				// no more components?
				if(k==-1) break;
				
				// get component constraints
				SpringLayout.Constraints cons = layout.getConstraints(parent.getComponent(k));
				
				// is width set already?
				if(isSet[k]) {
					cons.setWidth(Spring.sum(Spring.sum(width,cons.getWidth()),xPadSpring));
				}
				else {
					isSet[k] = true;
					cons.setWidth(width);						
				}
				
				// span cells
				while(i<rows-1&&map[i+1][j]==k) {i++;}
				
			}			
		}

		/* Adjust the x and y constraints of all the cells 
		 * so that they are properly spanned and aligned 
		 * in a grid  */
		
		// initialize
		isSet = new boolean[count];
		Spring right = null;
		Spring bottom = null;
		SpringLayout.Constraints cons = null;			
		SpringLayout.Constraints tail = null;
		List<Constraints> prev = new ArrayList<Constraints>(count);
		
		// loop over all rows
		for (int i = 0; i < rows; i++) {			
			
			// get index of first component on current row
			int k = map[i][0];
			
			// no more components?
			if(k==-1) break;
			
			// get component constraints
			tail = layout.getConstraints(parent.getComponent(k));
			
			// only set position constraints for each component once
			if(!isSet[k]) {
				
				// set flag
				isSet[k] = true;
				
				// initialize first element on row 
				tail.setX(initialXSpring);
				if(prev.size()==0) { 
					tail.setY(initialYSpring);
					prev.add(tail);
				} else {
					tail.setY(Spring.sum(prev.get(0).getConstraint(SpringLayout.SOUTH),yPadSpring));
					prev.set(0,tail); 
				}
			}
			
			// loop over all cells in this row
			for(int j = 1; j < cols; j++) {
				
				// get index of component on current row
				k = map[i][j];
				
				// no more components?
				if(k==-1) break;
				
				// get component constraints
				cons = layout.getConstraints(parent.getComponent(k));
				
				// only set position constraints for each component once
				if(!isSet[k]) {
				
					// set flag
					isSet[k] = true;
					
					// set horizontal position
					cons.setX(Spring.sum(tail.getConstraint(SpringLayout.EAST),xPadSpring));
					if(prev.size()<cols) { 
						cons.setY(initialYSpring);
					} else {
						cons.setY(Spring.sum(prev.get(j).getConstraint(SpringLayout.SOUTH),yPadSpring));
					}
				}
				
				// update tail and row array
				tail = cons;
				if(j<prev.size()) {
					prev.set(j,cons);
				} else {
					prev.add(cons);
				}

			}		
			
			// update bottom boundary
			bottom = tail.getConstraint(SpringLayout.SOUTH);
			
			// no more components?
			if(k==-1) break;
			
			// update right boundary
			right = tail.getConstraint(SpringLayout.EAST);
			
		}		

		//Set the parent's size
		setParentSize(parent, layout, autoResizeY?bottom:null, autoResizeX?right:null, xPad, yPad);
		
	}
	
	/**
	 * Aligns the first <code>rows</code> * <code>cols</code>
	 * components of <code>parent</code> in
	 * a grid. Each component in a column is as wide as the maximum
	 * preferred width of the components in that column;
	 * height is similarly determined for each row.
	 * The parent is made just big enough to fit them all.
	 *
	 * @param rows number of rows
	 * @param cols number of columns
	 * @param initialX x location to start the grid at
	 * @param initialY y location to start the grid at
	 * @param xPad x padding between cells
	 * @param yPad y padding between cells
	 * @param autoResizeX content width is resized with parent in if <code>true</code>, constant otherwise
	 * @param autoResizeY content height is resized with parent in if <code>true</code>, constant otherwise
	 */
	public static void makeCompactGrid(Container parent,
			int rows, int cols,
			int initialX, int initialY,
			int xPad, int yPad,
			boolean autoResizeX, boolean autoResizeY) 
	{
		
		SpringLayout layout;
		try {
			layout = (SpringLayout)parent.getLayout();
		} catch (ClassCastException exc) {
			logger.error("The first argument to makeCompactGrid must use SpringLayout.",exc);
			return;
		}

		//Align all cells in each column and make them the same width.
		Spring x = Spring.constant(initialX);
		for (int c = 0; c < cols; c++) {
			Spring width = Spring.constant(0);
			for (int r = 0; r < rows; r++) {
				Constraints constraints = getConstraintsForCell(r, c, parent, cols);
				if(constraints!=null) {
					width = Spring.max(width,constraints.getWidth());
				}
			}
			for (int r = 0; r < rows; r++) {
				Constraints constraints = getConstraintsForCell(r, c, parent, cols);
				if(constraints!=null) {
					constraints.setX(x);
					constraints.setWidth(width);
				}
			}
			x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
		}

		//Align all cells in each row and make them the same height.
		Spring y = Spring.constant(initialY);
		for (int r = 0; r < rows; r++) {
			Spring height = Spring.constant(0);
			for (int c = 0; c < cols; c++) {
				Constraints constraints = getConstraintsForCell(r, c, parent, cols);
				if(constraints!=null) {
					height = Spring.max(height,constraints.getHeight());
				}
			}
			for (int c = 0; c < cols; c++) {
				Constraints constraints = getConstraintsForCell(r, c, parent, cols);
				if(constraints!=null) {				
					constraints.setY(y);
					constraints.setHeight(height);
				}
			}
			y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
		}

		//Set the parent's size
		SpringLayout.Constraints pCons = layout.getConstraints(parent);
		if(autoResizeY) pCons.setConstraint(SpringLayout.SOUTH, y);
		if(autoResizeX) pCons.setConstraint(SpringLayout.EAST, x);
		
	}
	
	private static int[][] getSpanMap(Container parent,
			SpringLayout layout, int rows, int cols,
			int count, int cellSpanX[], int cellSpanY[]) {
		
		// initialize map
		int[][] map = new int[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				map[i][j] = -1;
			}
		}
		
		// loop over all components row for row
		int s = 0; 
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {				
				// span?
				if(map[i][j]==-1) {
					int k = cols * i + j - s;
					if(k<count) {
						// span in x direction
						int spanX = cellSpanX[k];
						int spanY = cellSpanY[k];
						for(int l=j;l<j+spanX&&l<cols;l++) {
							if(map[i][l]!=-1&&map[i][l]!=k) {s++; break;}
							map[i][l] = k;
							for(int m=i;m<i+spanY&&m<rows;m++) {
								if(map[m][l]!=-1&&map[m][l]!=k) {s++; break;}
								map[m][l] = k;
							}
						}
					}
				} else {s++;}
			}
		}
				
		return map;
	}
	
	private static void setParentSize(Container parent, 
			SpringLayout layout, 
			Spring bottom,
			Spring right,
			int xPad, int yPad) {
		SpringLayout.Constraints pCons = layout.getConstraints(parent);
		if(bottom!=null) {
			pCons.setConstraint(SpringLayout.SOUTH,
					Spring.sum(Spring.constant(yPad),bottom));
		}
		if(right!=null) {
			pCons.setConstraint(SpringLayout.EAST,
					Spring.sum(Spring.constant(xPad),right));
		}
	}

	/* Used by makeCompactGrid. */
	private static SpringLayout.Constraints getConstraintsForCell(
			int row, int col,
			Container parent,
			int cols)
	{
		int index = row * cols + col;
		SpringLayout layout = (SpringLayout) parent.getLayout();
		if(index<parent.getComponentCount())
		{
			Component c = parent.getComponent(index);
			return layout.getConstraints(c);
		}
		else 
		{
			return null;
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame(
						"SpringUtilities::makeSpannedGrid() Tester");
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.setPreferredSize(new Dimension(625,160));
				JPanel panel = new JPanel(new SpringLayout());
				
				panel.add(getTextField("JTextField 1",150,25));
				panel.add(getTextField("JTextField 2",350,25));
				panel.add(getTextField("JTextField 3",250,25));
				panel.add(getTextField("JTextField 4",250,25));
				panel.add(getTextField("JTextField 5",250,25));
				panel.add(getTextField("JTextField 6",250,25));
				//makeGrid(panel, 3, 2, 5, 5, 5, 5);
				//makeCompactGrid(panel, 3, 2, 5, 5, 5, 5);
				makeSpannedGrid(panel, 18/2, 2, 5, 5, 5, 5, 
						new int[] { 2, 1, 2, 1, 1, 1 },
						new int[] { 2, 1, 2, 1, 1, 1 },true,true);
				frame.setContentPane(panel);
				frame.pack();
				frame.setVisible(true);
			}
			
			private JTextField getTextField(String text, int width, int height) {
				JTextField textField = new JTextField(text);
				textField.setPreferredSize(new Dimension(width,height));
				return textField;
			}
		});
	}
	
	
}

