package org.redcross.sar.mso.data;

import org.redcross.sar.Application;
import org.redcross.sar.data.IData;
import org.redcross.sar.mso.IChangeIf;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.util.mso.Position;
import org.redcross.sar.util.mso.TimePos;
import org.redcross.sar.util.mso.Track;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * Search or rescue unit.
 */
@SuppressWarnings("unchecked")
public abstract class AbstractUnit extends AbstractMsoObject implements IUnitIf
{
	private final AttributeImpl.MsoString m_name = new AttributeImpl.MsoString(this, "Name",0,0,"");
	private final AttributeImpl.MsoString m_callSign = new AttributeImpl.MsoString(this, "CallSign");
	private final AttributeImpl.MsoString m_toneId = new AttributeImpl.MsoString(this, "ToneID");
	private final AttributeImpl.MsoString m_trackingId = new AttributeImpl.MsoString(this, "TrackingID");
	private final AttributeImpl.MsoInteger m_number = new AttributeImpl.MsoInteger(this, "Number", true);
	private final AttributeImpl.MsoPosition m_position = new AttributeImpl.MsoPosition(this, "Position");
	private final AttributeImpl.MsoString m_remarks = new AttributeImpl.MsoString(this, "Remarks");
	private final AttributeImpl.MsoEnum<UnitType> m_type = new AttributeImpl.MsoEnum<UnitType>(this, "Type", 1, UnitType.CP);
	private final AttributeImpl.MsoEnum<UnitStatus> m_status = new AttributeImpl.MsoEnum<UnitStatus>(this, "Status", 1, UnitStatus.EMPTY);
    private final AttributeImpl.MsoString m_organization = new AttributeImpl.MsoString(this, "Organization");
    private final AttributeImpl.MsoString m_division = new AttributeImpl.MsoString(this, "Division");
    private final AttributeImpl.MsoString m_department = new AttributeImpl.MsoString(this, "Department");    
    
	private final AssignmentListImpl m_unitAssignments = new AssignmentListImpl(this, "UnitAssignments", false);
	private final PersonnelListImpl m_unitPersonnel = new PersonnelListImpl(this, "UnitPersonnel", false);

	private final MsoRelationImpl<IHierarchicalUnitIf> m_superiorUnit = new MsoRelationImpl<IHierarchicalUnitIf>(this, "SuperiorUnit", 0, false, null);
	private final MsoRelationImpl<IPersonnelIf> m_unitLeader = new MsoRelationImpl<IPersonnelIf>(this, "UnitLeader", 0, true, null);
	private final MsoRelationImpl<ITrackIf> m_track = new MsoRelationImpl<ITrackIf>(this, "Track", 0, true, null);

	private final static SelfSelector<IUnitIf, IMessageIf> simpleReferringMesssageSelector = new SelfSelector<IUnitIf, IMessageIf>()
	{
		public boolean select(IMessageIf anObject)
		{
			if(m_object.equals(anObject.getReceiver()) || m_object.equals(anObject.getSender())) {
				return true;
			}
			for(IMessageLineIf it : anObject.getMessageLineItems()) {
				if(m_object!=null && m_object==it.getLineUnit())
					return true;
			}
			return false;
		}
	};

	/*-------------------------------------------------------------------------------------------
	 * Public Static Methods
	 *-------------------------------------------------------------------------------------------*/

	public static String getText(String aKey)
	{
		return Internationalization.getString(Internationalization.getBundle(IUnitIf.class), aKey);
	}

	public static char getEnumLetter(Enum<?> anEnum)
	{
		String letter = getText(anEnum.getClass().getSimpleName() + "." + anEnum.name() + ".letter");
		if (letter.length() > 0)
		{
			return letter.charAt(0);
		} else
		{
			return '?';
		}
	}

	/*-------------------------------------------------------------------------------------------
	 * Constructors
	 *-------------------------------------------------------------------------------------------*/


	public AbstractUnit(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId, int aNumber)
	{
		super(theMsoModel, anObjectId);
		setNumber(aNumber);
		setType(getTypeBySubclass());
	}

	public IMsoManagerIf.MsoClassCode getClassCode()
	{
		return IMsoManagerIf.MsoClassCode.CLASSCODE_UNIT;
	}

	/*-------------------------------------------------------------------------------------------
	 * Required methods
	 *-------------------------------------------------------------------------------------------*/

	protected void defineAttributes()
	{
		addAttribute(m_name);
		addAttribute(m_callSign);
		addAttribute(m_toneId);
		addAttribute(m_trackingId);
		addAttribute(m_number);
		addAttribute(m_position);
		addAttribute(m_remarks);
		addAttribute(m_type);
		addAttribute(m_status);
        addAttribute(m_organization);
        addAttribute(m_division);
        addAttribute(m_department);

	}

	protected void defineLists()
	{
		addList(m_unitAssignments);
		addList(m_unitPersonnel);
	}

	protected void defineObjects()
	{
		addObject(m_superiorUnit);
		addObject(m_unitLeader);
		addObject(m_track);
	}

	public void addListRelation(IMsoObjectIf anObject, String aReferenceListName)
	{
		if (anObject instanceof IAssignmentIf)
		{
			m_unitAssignments.add((IAssignmentIf) anObject);
		}
		if (anObject instanceof IPersonIf)
		{
			m_unitPersonnel.add((IPersonnelIf) anObject);
		}
	}

	public void removeListRelation(IMsoObjectIf anObject, String aReferenceListName)
	{
		if (anObject instanceof IAssignmentIf)
		{
			m_unitAssignments.remove((IAssignmentIf) anObject);
		}
		if (anObject instanceof IPersonIf)
		{
			m_unitPersonnel.remove((IPersonnelIf) anObject);
		}
	}

	/*-------------------------------------------------------------------------------------------
	 * Intercepted methods
	 *-------------------------------------------------------------------------------------------*/

	/**
	 * Local implementation of {@link AbstractMsoObject#registerModifiedData()}
	 * Resets correct subclass in case of incorrect changes by application or others.
	 * Renumber duplicate numbers
	 */
	@Override
	protected void registerModifiedData(IChangeIf aChange)
	{
		if (getType() != getTypeBySubclass())
		{
			setType(getTypeBySubclass());
		}
		super.registerModifiedData(aChange);
	}

	/*-------------------------------------------------------------------------------------------
	 * Abstract methods
	 *-------------------------------------------------------------------------------------------*/

	protected abstract UnitType getTypeBySubclass();

	/*-------------------------------------------------------------------------------------------
	 * Methods for ENUM attributes
	 *-------------------------------------------------------------------------------------------*/

	public abstract Enum<?> getSubType();

	public abstract String getSubTypeName();

	protected void setType(UnitType aType)
	{
		m_type.set(aType);
	}

	public UnitType getType()
	{
		return m_type.get();
	}

	public IData.DataOrigin getTypeState()
	{
		return m_type.getOrigin();
	}

	public IMsoAttributeIf.IMsoEnumIf<UnitType> getTypeAttribute()
	{
		return m_type;
	}

	public String getTypeName()
	{
		return m_type.getEnumName();
	}

	public String getInternationalTypeName()
	{
		return m_type.getInternationalName();
	}

	public UnitStatus getStatus()
	{
		return m_status.get();
	}

	public void setStatus(String aStatus) throws IllegalOperationException
	{
		setStatus(UnitStatus.valueOf(aStatus));
	}

	public void setStatus(UnitStatus aStatus) throws IllegalOperationException
	{
		// verify current status
		if(UnitStatus.RELEASED.equals(m_status.getAttrValue()))
		{
			throw new IllegalOperationException(
					"The unit is released, no status changed is allowed");
		}
		if(IUnitIf.MANAGED_SET.contains(aStatus))
		{
			throw new IllegalOperationException(
				"Status is managed by MSO model only");
		}
		if(getActiveAssignment()!=null)
		{
			throw new IllegalOperationException(
				"Unit has an active assignment, no manual status change is allowed");
		}
		// apply change
		m_status.set(aStatus);
	}

	public IData.DataOrigin getStatusState()
	{
		return m_status.getOrigin();
	}

	public IMsoAttributeIf.IMsoEnumIf<UnitStatus> getStatusAttribute()
	{
		return m_status;
	}

	public String getStatusText()
	{
		return m_status.getInternationalName();
	}
	
    public void setOrganization(String anOrganization)
    {
        m_organization.setValue(anOrganization);
    }

    public String getOrganization()
    {
        return m_organization.getString();
    }

    public IData.DataOrigin getOrganizationState()
    {
        return m_organization.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getOrganizationAttribute()
    {
        return m_organization;
    }

    public void setDivision(String division)
    {
    	m_division.setValue(division);
    }

	public String getDivision()
	{
		return m_division.getString();
	}

	public IData.DataOrigin getDivisionState()
	{
		return m_division.getOrigin();
	}

    public IMsoAttributeIf.IMsoStringIf getDivisionAttribute()
    {
    	return m_division;
    }
    
    public void setDepartment(String aDepartment)
    {
        m_department.setValue(aDepartment);
    }

    public String getDepartment()
    {
        return m_department.getString();
    }

    public IData.DataOrigin getDepartmentState()
    {
        return m_department.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getDepartmentAttribute()
    {
        return m_department;
    }	

	/*-------------------------------------------------------------------------------------------
	 * Methods for attributes
	 *-------------------------------------------------------------------------------------------*/

    public void setName(String aName)
    {
        m_name.setValue(aName);
    }

    public String getName()
    {
        return m_name.getString();
    }

    public IData.DataOrigin getNameState()
    {
        return m_name.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getNameAttribute()
    {
        return m_name;
    }

	public void setCallSign(String aCallSign)
	{
		m_callSign.setValue(aCallSign);
	}

	public String getCallSign()
	{
		return m_callSign.getString();
	}

	public IData.DataOrigin getCallSignState()
	{
		return m_callSign.getOrigin();
	}

	public IMsoAttributeIf.IMsoStringIf getCallSignAttribute()
	{
		return m_callSign;
	}

	public void setToneID(String toneId)
	{
		m_toneId.setValue(toneId);
	}

	public String getToneID()
	{
		return m_toneId.getString();
	}

	public IData.DataOrigin getToneIDState()
	{
		return m_toneId.getOrigin();
	}

	public IMsoAttributeIf.IMsoStringIf getToneIDAttribute()
	{
		return m_toneId;
	}

	public void setTrackingID(String trackingId)
	{
		m_trackingId.setValue(trackingId);
	}

	public String getTrackingID()
	{
		return m_trackingId.getString();
	}

	public IData.DataOrigin getTrackingIDState()
	{
		return m_trackingId.getOrigin();
	}

	public IMsoAttributeIf.IMsoStringIf getTrackingIDAttribute()
	{
		return m_trackingId;
	}
	
	public void setPosition(Position aPosition)
	{

		// update position
		m_position.set(aPosition);

	}

	public Position getPosition()
	{
		return m_position.get();
	}

	public IData.DataOrigin getPositionState()
	{
		return m_position.getOrigin();
	}

	public IMsoAttributeIf.IMsoPositionIf getPositionAttribute()
	{
		return m_position;
	}

	public void setRemarks(String aRemarks)
	{
		m_remarks.setValue(aRemarks);
	}

	public String getRemarks()
	{
		return m_remarks.getString();
	}

	public IData.DataOrigin getRemarksState()
	{
		return m_remarks.getOrigin();
	}

	public IMsoAttributeIf.IMsoStringIf getRemarksAttribute()
	{
		return m_remarks;
	}

	// From ISerialNumberedIf
	public void setNumber(int aNumber)
	{
		setNumber(m_number,aNumber);
	}

	public int getNumber()
	{
		return m_number.intValue();
	}

	public IData.DataOrigin getNumberState()
	{
		return m_number.getOrigin();
	}

	public IMsoAttributeIf.IMsoIntegerIf getNumberAttribute()
	{
		return m_number;
	}

	/*-------------------------------------------------------------------------------------------
	 * Methods for lists
	 *-------------------------------------------------------------------------------------------*/

	public IAssignmentListIf getUnitAssignments()
	{
		return m_unitAssignments;
	}

	public IData.DataOrigin getUnitAssignmentsState(IAssignmentIf anAssignment)
	{
		return m_unitAssignments.getOrigin(anAssignment);
	}

	public Collection<IAssignmentIf> getUnitAssignmentsItems()
	{
		return m_unitAssignments.getObjects();
	}

	public void addUnitPersonnel(IPersonnelIf anPersonnel)
	{
		suspendChange();
		m_unitPersonnel.add(anPersonnel);
		m_status.set(getAutoStatus(isPaused()));
		resumeChange(true);

	}

	public void removeUnitPersonnel(IPersonnelIf anPersonnel)
	{
		suspendChange();
		m_unitPersonnel.remove(anPersonnel);
		m_status.set(getAutoStatus(isPaused()));
		resumeChange(true);
	}

	public IPersonnelListIf getUnitPersonnel()
	{
		return m_unitPersonnel;
	}

	public IData.DataOrigin getUnitPersonnelState(IPersonnelIf anPersonnel)
	{
		return m_unitPersonnel.getOrigin(anPersonnel);
	}

	public Collection<IPersonnelIf> getUnitPersonnelItems()
	{
		return m_unitPersonnel.getObjects();
	}

	/*-------------------------------------------------------------------------------------------
	 * Methods for references
	 *-------------------------------------------------------------------------------------------*/

	public boolean setSuperiorUnit(IHierarchicalUnitIf aSuperior)
	{
		IHierarchicalUnitIf tu = aSuperior;
		while (tu != null && tu != this)
		{
			tu = tu.getSuperiorUnit();
		}
		if (tu != null)
		{
			return false;
		}
		m_superiorUnit.set(aSuperior);
		return true;
	}

	public IHierarchicalUnitIf getSuperiorUnit()
	{
		return m_superiorUnit.get();
	}

	public IData.DataOrigin getSuperiorUnitState()
	{
		return m_superiorUnit.getOrigin();
	}

	public IMsoRelationIf<IHierarchicalUnitIf> getSuperiorUnitAttribute()
	{
		return m_superiorUnit;
	}

	public void setUnitLeader(IPersonnelIf aPersonnel)
	{
		m_unitLeader.set(aPersonnel);
	}

	public IPersonnelIf getUnitLeader()
	{
		return m_unitLeader.get();
	}

	public IData.DataOrigin getUnitLeaderState()
	{
		return m_unitLeader.getOrigin();
	}

	public IMsoRelationIf<IPersonnelIf> getUnitLeaderAttribute()
	{
		return m_unitLeader;
	}

	public void setTrack(ITrackIf aTrack) {
		m_track.set(aTrack);
	}

	public ITrackIf getTrack() {
		return m_track.get();
	}

	public IData.DataOrigin getTrackState() {
		return m_track.getOrigin();
	}

	public IMsoRelationIf<ITrackIf> geTrackAttribute() {
		return m_track;
	}

	/*-------------------------------------------------------------------------------------------
	 * Methods for hierarchy
	 *-------------------------------------------------------------------------------------------*/

	/**
	 * Generate list of subordinates for this unit
	 *
	 * @return The list
	 */
	public List<IHierarchicalUnitIf> getSubOrdinates()
	{
		return getSubOrdinates(this);
	}

	/**
	 * Generate list of subordinates for a {@link IHierarchicalUnitIf} unit.
	 *
	 * @param aUnit The unit that has subordinates.
	 * @return The list.
	 */
	public static List<IHierarchicalUnitIf> getSubOrdinates(IHierarchicalUnitIf aUnit)
	{
		ArrayList<IHierarchicalUnitIf> resultList = new ArrayList<IHierarchicalUnitIf>();
		IUnitListIf mainList = Application.getInstance().getMsoModel().getMsoManager().getCmdPost().getUnitList();
		for (IUnitIf u : mainList.getObjects())
		{
			if (u.getSuperiorUnit() == aUnit)
			{
				resultList.add(u);
			}
		}
		return resultList;
	}

	/*-------------------------------------------------------------------------------------------
	 * Other methods
	 *-------------------------------------------------------------------------------------------*/

	public char getNumberPrefix()
	{
		return getEnumLetter(getType());
	}

	public String getShortName()
	{
		return (getNumberPrefix() + " " + getNumber()).trim();
	}

	public String getDefaultName()
	{
		return (getInternationalTypeName() + " " + getNumber()).trim();
	}

	public char getCommunicatorNumberPrefix()
	{
		return getNumberPrefix();
	}

	public int getCommunicatorNumber()
	{
		return getNumber();
	}

	public String getCommunicatorShortName()
	{
		return getShortName();
	}

	public boolean isReleased() {
		return UnitStatus.RELEASED.equals(m_status.getAttrValue());
	}

	public boolean isPaused() {
		return UnitStatus.PAUSED.equals(m_status.getAttrValue());
	}

	public IChangeIf pause() throws IllegalOperationException {
		// get current status
		UnitStatus status = getStatus();
		// verify valid state
		if(UnitStatus.EMPTY.equals(status) ||
				UnitStatus.RELEASED.equals(status)) {
			throw new IllegalOperationException(
					"Unit can not be paused if status is EMPTY or RELEASED");
		}
		// forward
		m_status.set(UnitStatus.PAUSED);
		// finished
		return m_status.getChange();
	}

	public IChangeIf resume() throws IllegalOperationException {
		UnitStatus status = getStatus();
		// verify valid state
		if(!UnitStatus.PAUSED.equals(status)) {
			throw new IllegalOperationException(
					"Only a paused unit can be resumed");
		}
		// forward
		m_status.set(getAutoStatus(false));
		// finished
		return m_status.getChange();
	}

	public IAssignmentIf getActiveAssignment()
	{
		IAssignmentIf retVal;
		retVal = getAllocatedAssignment();
		if (retVal != null)
		{
			return retVal;
		}
		return getExecutingAssigment();
	}

	public IAssignmentIf releaseAssignment() throws IllegalOperationException {
		// get allocated assignment
		IAssignmentIf released = getAllocatedAssignment();
		// release?
		if(released!=null)
			released.setOwningUnit(AssignmentStatus.READY, null);
		// finished
		return released;
	}

	public boolean releaseAssignment(IAssignmentIf anAssignment) throws IllegalOperationException {
		IAssignmentIf released = null;
		if(getAllocatedAssignment()==anAssignment)
			released = anAssignment;
		if(getEnqueuedAssignments().contains(anAssignment))
			released = anAssignment;
		// release?
		if(released!=null)
			released.setOwningUnit(AssignmentStatus.READY, null);
		// finished
		return released!=null;
	}

	public boolean enqueueAssignment(IAssignmentIf anAssignment) throws IllegalOperationException
	{
		return enqueueAssignment(anAssignment,null);
	}

	public boolean enqueueAssignment(IAssignmentIf newAssignment, IAssignmentIf beforeAssignment) throws IllegalOperationException
	{
		if (newAssignment == beforeAssignment)
		{
			return false;
		}

		// suspend MSO update
		suspendChange();

		/*
		 * the assignment will be added by AssignmentImpl
		 * with latest prioritySequence using helper method
		 * addUnitAssignment(). AssignmentImpl update its status
		 * accordingly.
		 */
		newAssignment.setOwningUnit(AssignmentStatus.QUEUED, this);
		// check for update of priority sequence is required?
		boolean bUpdate = (beforeAssignment != null
				&& beforeAssignment.getStatus() == AssignmentStatus.QUEUED && beforeAssignment
				.getOwningUnit() == this);
		// calculate the
		int newPrioritySequence = bUpdate ? beforeAssignment
				.getPrioritySequence() : Integer.MAX_VALUE;
		// move forwards in list if not last
		if (newPrioritySequence != Integer.MAX_VALUE) {
			boolean insertionPointFound = false;
			int lastPri = -1;
			for (IAssignmentIf asg : getEnqueuedAssignments()) {
				if (asg == newAssignment) {
					continue;
				}
				lastPri = asg.getPrioritySequence();
				if (lastPri == newPrioritySequence) {
					insertionPointFound = true;
					newAssignment.setPrioritySequence(newPrioritySequence);
				}
				if (insertionPointFound) {
					asg.setPrioritySequence(lastPri + 1);
				}
			}
		}

		// resume MSO update
		resumeChange(true);

		// finished
		return true;
	}

	public boolean dequeueAssignment(IAssignmentIf anAssignment) throws IllegalOperationException
	{
		if (anAssignment == null)
		{
			return false;
		}

		/*
		 * the assignment will be removed by AssignmentImpl
		 * using helper method removeUnitAssignment(). AssignmentImpl
		 * will update its status accordingly.
		 */
		anAssignment.setOwningUnit(AssignmentStatus.READY, null);

		// success
		return true;

	}
	
    public Collection<IAssignmentIf> getAssignments(AssignmentStatus status) {
    	switch(status) {
	    	case QUEUED: return getEnqueuedAssignments();
	    	case ALLOCATED: return getAllocatedAssignments();
	    	case EXECUTING: return getExecutingAssigments();
	    	case FINISHED: return getEnqueuedAssignments();
	    	//case ABORTED: return getAAssignments();
	    	//case REPORTED: return getEnqueuedAssignments();
    	}
    	return new Vector<IAssignmentIf>(0);
    }


	public List<IAssignmentIf> getEnqueuedAssignments()
	{
		return m_unitAssignments.selectItems(IAssignmentIf.QUEUED_SELECTOR, IAssignmentIf.PRIORITY_SEQUENCE_COMPARATOR);
	}

	public IAssignmentIf allocateAssignment() throws IllegalOperationException {
		return allocateAssignment(null);
	}

	public IAssignmentIf allocateAssignment(IAssignmentIf anAssignment) throws IllegalOperationException {
		// is available?
		if(getActiveAssignment()==null)
		{
			// get first in queue?
			if(anAssignment!=null) {
				// get available assignments
				List<IAssignmentIf> queue = getEnqueuedAssignments();
				// has enqueued assignments?
				if(queue.size()>0)
				{
					// get first assignment in queue
					anAssignment = queue.get(0);
				}
			}
			// forward?
			if(anAssignment!=null)
			{
				anAssignment.setOwningUnit(AssignmentStatus.ALLOCATED, this);
			}
		}
		// finished
		return anAssignment;
	}

	public IAssignmentIf getAllocatedAssignment()
	{
		return m_unitAssignments.selectSingleItem(IAssignmentIf.ALLOCATED_SELECTOR);
	}

	public Set<IAssignmentIf> getAllocatedAssignments()
	{
		return m_unitAssignments.selectItems(IAssignmentIf.ALLOCATED_SELECTOR);
	}

	public IAssignmentIf startAssignment() throws IllegalOperationException {
		return startAssignment(null);
	}

	public IAssignmentIf startAssignment(IAssignmentIf anAssignment) throws IllegalOperationException {

		// initialize
		IAssignmentIf start = null;

		// get valid assignment
		if(getAllocatedAssignment()==anAssignment)
		{
			start = anAssignment;
		}
		else if(getAllocatedAssignment()==null && anAssignment!=null)
		{
			start = anAssignment;
		}
		else if(getAllocatedAssignment()!=null && anAssignment==null)
		{
			start = getAllocatedAssignment();
		}

		// forward?
		if(start!=null)
		{
			start.setOwningUnit(AssignmentStatus.EXECUTING, this);
		}

		// finished
		return start;
	}

	public IAssignmentIf getExecutingAssigment()
	{
		return m_unitAssignments.selectSingleItem(IAssignmentIf.EXECUTING_SELECTOR);
	}

	public Set<IAssignmentIf> getExecutingAssigments()
	{
		return m_unitAssignments.selectItems(IAssignmentIf.EXECUTING_SELECTOR);
	}

	public IAssignmentIf finishAssignment() throws IllegalOperationException {
		return finishAssignment(null);
	}

	public IAssignmentIf finishAssignment(IAssignmentIf anAssignment) throws IllegalOperationException {

		// initialize
		IAssignmentIf finish = null;

		// get valid assignment
		if(getExecutingAssigment()==anAssignment) {
			finish = anAssignment;
		}
		else if(getExecutingAssigment()==null && anAssignment!=null)
		{
			finish = anAssignment;
		}
		else if(getExecutingAssigment()!=null && anAssignment==null)
		{
			finish = getExecutingAssigment();
		}

		// forward?
		if(finish!=null)
		{
			finish.setOwningUnit(AssignmentStatus.FINISHED, this);
		}

		// finished
		return finish;
	}

	public Set<IAssignmentIf> getFinishedAssigments()
	{
		return m_unitAssignments.selectItems(IAssignmentIf.FINISHED_SELECTOR);
	}

	@Override
	public String shortDescriptor()
	{
		return getInternationalTypeName() + " " + getNumber();
	}

	public String toString()
	{
		return "AbstractUnit" + " " + getObjectId();
	}

	public boolean logPosition() {
		return logPosition(Calendar.getInstance());
	}

	public boolean logPosition(Calendar aTime) {
		// ensure a time stamp is given
		if(aTime==null) aTime = Calendar.getInstance();
		// get MSO track
		ITrackIf msoTrack = m_track.get();
		// create track?
		if(msoTrack==null) {
			// get command post
			ICmdPostIf cmdPost = Application.getInstance().getMsoModel().getMsoManager().getCmdPost();
			// create new track
			msoTrack = cmdPost.getTrackList().createTrack();
			// set geodata
			msoTrack.setGeodata(new Track(null, null, 1));
			// set track reference in unit
			m_track.set(msoTrack);
		}
		// is possible to log position?
		if(msoTrack!=null) {
			// get current position
			Point2D.Double p = m_position.get().getPosition();
			// create time position
			TimePos tp = new TimePos(p,aTime);
			// add to track
			msoTrack.addTrackPoint(tp);
			// finished
			return true;
		}
		// failure
		return false;
	}

	public boolean logPosition(Position aPosition, Calendar aTime) {
		// save
		setPosition(aPosition);
		// forward
		return logPosition(aTime);
	}

	public TimePos getLastKnownPosition() {
		// initialize
		TimePos p = null;
		// get MSO track
		ITrackIf msoTrack = m_track.get();
		// is possible to log position?
		if(msoTrack!=null) {
			p = msoTrack.getTrackStopPoint();
		}
		// finished
		return p;
	}

	public Set<IMessageIf> getReferringMessages()
	{
		simpleReferringMesssageSelector.setSelfObject(this);
		ICmdPostIf cmdPost = Application.getInstance().getMsoModel().getMsoManager().getCmdPost();
		return cmdPost != null ? cmdPost.getMessageLog().selectItems(simpleReferringMesssageSelector) : null;
	}

	public Set<IMessageIf> getReferringMessages(Collection<IMessageIf> aCollection)
	{
		simpleReferringMesssageSelector.setSelfObject(this);
		return MsoListImpl.selectItemsInCollection(simpleReferringMesssageSelector,aCollection);
	}


	public double getBearing()
	{
		// get MSO track
		ITrackIf msoTrack = m_track.get();
		// has track?
		if(msoTrack!=null) {
        	// initialize
        	Track track = msoTrack.getGeodata();
        	int count = track.size();
        	// has points?
        	if(count>0)
        	{
        		// get current position
            	Position p = m_position.get();
            	if(p!=null)
            	{
            		// current bearing
            		return track.getStopPoint().bearing(p.getGeoPos());
            	}
            	else if(count>1)
            	{
            		// current bearing is not known, use last known bearing
            		return track.getBearing(count-2,count-1);
            	}
        	}
        }
		// failed
        return 0.0;
	}

	public double getSpeed()
	{
		// get MSO track
		ITrackIf msoTrack = m_track.get();
		// has track?
		if(msoTrack!=null) {
        	// initialize
        	Track track = msoTrack.getGeodata();
        	int count = track.size();
        	// has more than one point?
        	if(count>1)
        	{

        		// TODO: Implement decision between real time tracking information and logged track information

        		// current speed is not known, use last known leg speed
        		return track.getSpeed(count-2,count-1,false);

        	}
        }
		// failed
		return 0.0;
	}

	public double getAverageSpeed()
	{
		// get MSO track
		ITrackIf msoTrack = m_track.get();
		// has track?
		if(msoTrack!=null) {
        	// initialize
        	Track track = msoTrack.getGeodata();
        	int count = track.size();
        	// has more than one point?
        	if(count>1)
        	{
        		// get speed from start to finish
        		return track.getSpeed(count-1);
        	}
        }
		// failed
		return 0.0;
	}

	public double getMaximumSpeed()
	{
		// get MSO track
		ITrackIf msoTrack = m_track.get();
		// has track?
		if(msoTrack!=null) {
        	// initialize
        	Track track = msoTrack.getGeodata();
        	int count = track.size();
        	// has more than one point?
        	if(count>1)
        	{
        		// get speed from start to finish
        		return track.getMaximumSpeed();
        	}
        }
		// failed
		return 0.0;
	}

	public double getMinimumSpeed()
	{
		// get MSO track
		ITrackIf msoTrack = m_track.get();
		// has track?
		if(msoTrack!=null) {
        	// initialize
        	Track track = msoTrack.getGeodata();
        	int count = track.size();
        	// has more than one point?
        	if(count>1)
        	{
        		// get speed from start to finish
        		return track.getMaximumSpeed();
        	}
        }
		// failed
		return 0.0;
	}

    /**
     * Get duration of given unit status. </p>
     *
     * @param aStatus - The status to get duration for
     * @param total - If <code>true</code> the sum of all durations for a given status
     * is returned, the duration of the last occurrence otherwise.
     *
     * @return Duration (second)
     */
	public double getDuration(UnitStatus aStatus, boolean total) {

        return getStatusAttribute().getDuration(aStatus,total);

	}

    /**
     * Get duration of given set of unit status. </p>
     *
     * @param aList - The status to get duration for
     * @param total - If <code>true</code> the sum of all durations for a given status
     * is returned, the duration of the last occurrence otherwise.
     *
     * @return Duration (second)
     */
	public double getDuration(EnumSet<UnitStatus> aList, boolean total) {

        return getStatusAttribute().getDuration(aList,total);

	}

	public double getDistance()
	{
		// get MSO track
		ITrackIf msoTrack = m_track.get();
		// has track?
		if(msoTrack!=null) {
        	// initialize
        	Track track = msoTrack.getGeodata();
        	int count = track.size();
        	// has more than one point?
        	if(count>1)
        	{
        		// get distance from start to finish
        		return track.getDistance();
        	}
        }
		// failed
		return 0.0;
	}

    public Calendar getTime(Enum<?> aStatus)
    {
    	// translate status type
    	if(aStatus instanceof UnitStatus)
    	{
	        return getStatusAttribute().getLastTime((UnitStatus)aStatus);
    	}
        // failed
        return null;
    }


	/*-------------------------------------------------------------------------------------------
	 * Helper methods
	 *-------------------------------------------------------------------------------------------*/

	protected void addUnitAssignment(IAssignmentIf anAssignment) throws IllegalOperationException
	{
		if(anAssignment!=null) {
			// suspend MSO update
			suspendChange();
			m_unitAssignments.add(anAssignment);
			rearrangeAsgPrioritiesAfterReferenceChange(anAssignment);
			m_status.set(getAutoStatus(isPaused()));
			m_position.set(getAutoPosition(anAssignment));
			// resume MSO update
			resumeChange(true);
		}
	}

	protected void removeUnitAssignment(IAssignmentIf anAssignment) throws IllegalOperationException
	{
		if(anAssignment!=null) {
			// suspend MSO update
			suspendChange();
			m_unitAssignments.remove(anAssignment);
			rearrangeAsgPrioritiesAfterReferenceChange(anAssignment);
			getAutoStatus(isPaused());
			// resume MSO update
			resumeChange(true);
		}
	}

	private UnitStatus getAutoStatus(boolean isPaused) {

		// initialize on current status
		UnitStatus aStatus = getStatus();

		// is allowed?
		if(!(isPaused || isReleased())) {

			if(getAllocatedAssignment()!=null) {
				aStatus = UnitStatus.INITIALIZING;
			}
			else if(getExecutingAssigment()!=null) {
				aStatus = UnitStatus.WORKING;
			}
			else
			{
				aStatus = m_unitPersonnel.size()>0 ? UnitStatus.READY : UnitStatus.EMPTY;
			}

		}

		// finished
		return aStatus;

	}

	private Position getAutoPosition(IAssignmentIf anAssignment) {

		// initialize at current
		Position p = getPosition();

		// is allowed?
		if(!isReleased()) {

			// get status
			AssignmentStatus aStatus = anAssignment.getStatus();

			// set position of unit automatically?
			switch(aStatus) {
			case EXECUTING:
				setPosition(MsoUtils.getStartPosition(anAssignment));
				break;
			case FINISHED:
				setPosition(MsoUtils.getStopPosition(anAssignment));
			}

		}

		// finished
		return p;

	}

	protected void assignmentChanged(IAssignmentIf anAssignment, AssignmentStatus oldStatus, boolean add) throws IllegalOperationException {
		// suspend MSO update
		suspendChange();
		m_status.set(getAutoStatus(isPaused()));
		m_position.set(getAutoPosition(anAssignment));
		rearrangeAsgPrioritiesAfterStatusChange(anAssignment, oldStatus);
		// resume MSO update
		resumeChange(true);
	}

	private void rearrangeAsgPrioritiesAfterStatusChange(IAssignmentIf anAssignment, AssignmentStatus oldStatus)
	{
		if (anAssignment.getStatus() == AssignmentStatus.QUEUED || oldStatus == AssignmentStatus.QUEUED)
		{
			rearrangeAsgPriorities();
		}
	}

	private void rearrangeAsgPrioritiesAfterReferenceChange(IAssignmentIf anAssignment)
	{
		if (anAssignment.getStatus() == AssignmentStatus.QUEUED)
		{
			rearrangeAsgPriorities();
		}
	}

	private void rearrangeAsgPriorities()
	{
		// suspend MSO update
		suspendChange();
		int actPriSe = 0;
		for (IAssignmentIf asg : getEnqueuedAssignments()) {
			if (asg.getPrioritySequence() != actPriSe) {
				asg.setPrioritySequence(actPriSe);
			}
			actPriSe++;
		}
		// resume MSO update
		resumeChange(true);
	}

}


