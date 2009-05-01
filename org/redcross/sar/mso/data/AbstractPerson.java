package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.mso.Position;

import java.util.Calendar;

public abstract class AbstractPerson extends AbstractMsoObject implements IPersonIf
{
    private final AttributeImpl.MsoCalendar m_birthdate = new AttributeImpl.MsoCalendar(this, "Birthdate");
    private final AttributeImpl.MsoString m_firstname = new AttributeImpl.MsoString(this, "Firstname");
    private final AttributeImpl.MsoString m_iD = new AttributeImpl.MsoString(this, "ID");
    private final AttributeImpl.MsoString m_lastname = new AttributeImpl.MsoString(this, "Lastname");
    private final AttributeImpl.MsoInteger m_photo = new AttributeImpl.MsoInteger(this, "Photo");
    private final AttributeImpl.MsoPosition m_residence = new AttributeImpl.MsoPosition(this, "Residence");
    private final AttributeImpl.MsoString m_address = new AttributeImpl.MsoString(this, "Address");
    private final AttributeImpl.MsoString m_telephone1 = new AttributeImpl.MsoString(this, "Telephone1");
    private final AttributeImpl.MsoString m_telephone2 = new AttributeImpl.MsoString(this, "Telephone2");
    private final AttributeImpl.MsoString m_telephone3 = new AttributeImpl.MsoString(this, "Telephone3");

    private final AttributeImpl.MsoEnum<PersonGender> m_gender = new AttributeImpl.MsoEnum<PersonGender>(this, "Gender", 1, PersonGender.UNKNOWN);

    public AbstractPerson(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId)
    {
        super(theMsoModel,anObjectId);
    }

    protected void defineAttributes()
    {
        addAttribute(m_birthdate);
        addAttribute(m_firstname);
        addAttribute(m_iD);
        addAttribute(m_lastname);
        addAttribute(m_photo);
        addAttribute(m_residence);
        addAttribute(m_address);
        addAttribute(m_telephone1);
        addAttribute(m_telephone2);
        addAttribute(m_telephone3);
        addAttribute(m_gender);
    }

    protected void defineLists()
    {
    }

    protected void defineObjects()
    {
    }

    public void setBirthdate(Calendar aBirthdate)
    {
        m_birthdate.setValue(aBirthdate);
    }

    public Calendar getBirthdate()
    {
        return m_birthdate.getCalendar();
    }

    public IMsoModelIf.ModificationState getBirthdateState()
    {
        return m_birthdate.getState();
    }

    public IAttributeIf.IMsoCalendarIf getBirthdateAttribute()
    {
        return m_birthdate;
    }

    public void setFirstname(String aFirstname)
    {
        m_firstname.setValue(aFirstname);
    }

    public String getFirstName()
    {
        return m_firstname.getString();
    }

    public IMsoModelIf.ModificationState getFirstnameState()
    {
        return m_firstname.getState();
    }

    public IAttributeIf.IMsoStringIf getFirstnameAttribute()
    {
        return m_firstname;
    }

    public void setID(String anID)
    {
        m_iD.setValue(anID);
    }

    public String getID()
    {
        return m_iD.getString();
    }

    public IMsoModelIf.ModificationState getIDState()
    {
        return m_iD.getState();
    }

    public IAttributeIf.IMsoStringIf getIDAttribute()
    {
        return m_iD;
    }

    public void setLastname(String aLastname)
    {
        m_lastname.setValue(aLastname);
    }

    public String getLastName()
    {
        return m_lastname.getString();
    }

    public IMsoModelIf.ModificationState getLastnameState()
    {
        return m_lastname.getState();
    }

    public IAttributeIf.IMsoStringIf getLastnameAttribute()
    {
        return m_lastname;
    }

    public void setPhoto(int aPhoto)
    {
        m_photo.setValue(aPhoto);
    }

    public int getPhoto()
    {
        return m_photo.intValue();
    }

    public IMsoModelIf.ModificationState getPhotoState()
    {
        return m_photo.getState();
    }

    public IAttributeIf.IMsoIntegerIf getPhotoAttribute()
    {
        return m_photo;
    }

    public void setResidence(Position aResidence)
    {
        m_residence.setValue(aResidence);
    }

    public Position getResidence()
    {
        return m_residence.getPosition();
    }

    public IMsoModelIf.ModificationState getResidenceState()
    {
        return m_residence.getState();
    }

    public IAttributeIf.IMsoPositionIf getResidenceAttribute()
    {
        return m_residence;
    }

    public void setAddress(String address)
    {
    	m_address.setValue(address);
    }

    public String getAddress()
    {
    	return m_address.getString();
    }

    public IMsoModelIf.ModificationState getAddressState()
    {
    	return m_address.getState();
    }

    public IAttributeIf.IMsoStringIf getAddressAttribute()
    {
    	return m_address;
    }

    public void setTelephone1(String aTelephone1)
    {
        m_telephone1.setValue(aTelephone1);
    }

    public String getTelephone1()
    {
        return m_telephone1.getString();
    }

    public IMsoModelIf.ModificationState getTelephone1State()
    {
        return m_telephone1.getState();
    }

    public IAttributeIf.IMsoStringIf getTelephone1Attribute()
    {
        return m_telephone1;
    }

    public void setTelephone2(String aTelephone2)
    {
        m_telephone2.setValue(aTelephone2);
    }

    public String getTelephone2()
    {
        return m_telephone2.getString();
    }

    public IMsoModelIf.ModificationState getTelephone2State()
    {
        return m_telephone2.getState();
    }

    public IAttributeIf.IMsoStringIf getTelephone2Attribute()
    {
        return m_telephone2;
    }

    public void setTelephone3(String aTelephone3)
    {
        m_telephone3.setValue(aTelephone3);
    }

    public String getTelephone3()
    {
        return m_telephone3.getString();
    }

    public IMsoModelIf.ModificationState getTelephone3State()
    {
        return m_telephone3.getState();
    }

    public IAttributeIf.IMsoStringIf getTelephone3Attribute()
    {
        return m_telephone3;
    }

    public void setGender(PersonGender aGender)
    {
        m_gender.setValue(aGender);
    }

    public void setGender(String aGender)
    {
        m_gender.setValue(aGender);
    }

    public PersonGender getGender()
    {
        return m_gender.getValue();
    }

    public String getGenderText()
    {
    	return m_gender.getInternationalName();
    }

    public IMsoModelIf.ModificationState getGenderState()
    {
        return m_gender.getState();
    }

    public IAttributeIf.IMsoEnumIf<PersonGender> getGenderAttribute()
    {
        return m_gender;
    }

    public int getAge()
    {
        Calendar now = Calendar.getInstance();
        if (getBirthdate() != null)
        {
            long ageInMillis = now.getTimeInMillis() - getBirthdate().getTimeInMillis();
            return (int) (ageInMillis / (1000 * 3600 * 24 * 365.25));
        }
        return -1;
    }

//    public String toString()
//    {
//        if (m_firstname != null && m_lastname!= null)
//        {
//            return getFirstname() + " " + getLastname();
//        } else
//        {
//            return getMsoClassCode().name();
//        }
//    }
}