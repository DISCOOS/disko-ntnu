package org.redcross.sar.wp.ds;

import org.redcross.sar.ds.DsBinder;
import org.redcross.sar.ds.IDsObject;
import org.redcross.sar.ds.mso.ICue;
import org.redcross.sar.ds.mso.MsoAdvisor;
import org.redcross.sar.gui.model.AbstractDsTableModel;

public class LevelTableModel extends AbstractDsTableModel<ICue,IDsObject> {

	private static final long serialVersionUID = 1L;

	public static final int NAME_INDEX = 0;
	public static final int INPUT_INDEX = 1;
	public static final int LEVEL_INDEX = 2;
	public static final int OUTPUT_INDEX = 3;

	private static final String NAME = "name";
	private static final String INPUT = "rin";
	private static final String LEVEL = "level";
	private static final String OUTPUT = "rout";

	public static final String[] ATTRIBUTES =

		new String[]{	NAME,INPUT,LEVEL,OUTPUT};

	private static final String[] CAPTIONS =

		new String[]{
						"Navn","Rate inn","Niv�","Rate ut"
					};


	private static final String[] TOOLTIPS =

		new String[]{
						"Navn p� niv�",
						"Rate inn til niv�",
						"N�v�rende niv�",
						"Rate ut fra niv�"
					};

	/* ================================================================
	 * Constructors
	 * ================================================================ */

	public LevelTableModel() {

		// forward
		super(IDsObject.class, ATTRIBUTES, CAPTIONS, TOOLTIPS, true);

	}

	/* ================================================================
	 * DsTableModel implementation
	 * ================================================================ */

	protected Object getCellValue(int index, String name) {

		if(NAME.equals(name)) {
			return getObject(index);
		}

		// failed
		return null;

	}

	/* ================================================================
	 * Public methods
	 * ================================================================ */

	public static DsBinder<ICue,IDsObject> createBinder(MsoAdvisor advisor) {

		DsBinder<ICue,IDsObject> binder = new DsBinder<ICue, IDsObject>(IDsObject.class);
		binder.connect(advisor);

		return binder;

	}

}
