package org.redcross.sar.wp.simulator;

import java.util.Comparator;

import org.redcross.sar.gui.model.MsoObjectTableModel;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.util.mso.Selector;

public class UnitTableModel extends MsoObjectTableModel<IUnitIf> {

	private static final long serialVersionUID = 1L;
	private static final String MSO_OBJECT = "Unit";	
	private static final String[] ATTRIBUTES = new String[]{MSO_OBJECT};
	private static final String[] CAPTIONS = new String[]{"Enhet"};

	private final Selector<IUnitIf> m_unitSelector = new Selector<IUnitIf>()
	{
		public boolean select(IUnitIf anObject)
		{
			// get history flag
			boolean isHistory = anObject.isReleased();
			// finished
			return m_archived ? isHistory : !isHistory;	
		}
	};

	private static final Comparator<IMsoObjectIf> m_unitComparator = new Comparator<IMsoObjectIf>()
	{
		public int compare(IMsoObjectIf o1, IMsoObjectIf o2)
		{
			IUnitIf u1 = (IUnitIf)o1;
			IUnitIf u2 = (IUnitIf)o2;
			if(u1.getType() == u2.getType())
			{
				return u1.getNumber() - u2.getNumber();
			}
			else
			{
				return u1.getType().ordinal() - u2.getType().ordinal();
			}
		}
	};
	
	private boolean m_archived;
	
	public UnitTableModel(IMsoModelIf model,boolean archived) {
		// forward
		super(model, MsoClassCode.CLASSCODE_UNIT, ATTRIBUTES, CAPTIONS);
		// prepare
		m_archived = archived;
		// get command post
		ICmdPostIf cmdPost = model.getMsoManager().getCmdPost();
		// load data?
		if(cmdPost!=null) {
			load(cmdPost.getUnitList());
		}
	}

	protected Object getMsoValue(IMsoObjectIf msoObj, String name) {
		if(MSO_OBJECT.equals(name)) {
			return (IUnitIf)msoObj;
		}
		// failed
		return null;
	}

	@Override
	public boolean select(IUnitIf msoObj) {
		return m_unitSelector.select(msoObj);	
	}
	
	@Override
	public void sort() {
		sort(m_unitComparator);
	}

	public IUnitIf getUnit(int iRow) {
		return (IUnitIf)getValueAt(iRow, 0);
	}


}
