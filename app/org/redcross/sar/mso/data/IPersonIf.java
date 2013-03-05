package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;
import org.redcross.sar.util.mso.Position;

import java.util.Calendar;

/**
 *
 */
public interface IPersonIf extends IMsoObjectIf
{
    public static final String bundleName  = "org.redcross.sar.mso.data.properties.Person";

    public enum PersonGender
    {
        FEMALE,
        MALE,
        UNKNOWN
    }

    public void setBirthdate(Calendar aBirthdate);

    public Calendar getBirthdate();

    public IData.DataOrigin getBirthdateState();

    public IMsoAttributeIf.IMsoCalendarIf getBirthdateAttribute();

    public void setFirstname(String aFirstname);

    public String getFirstName();

    public IData.DataOrigin getFirstnameState();

    public IMsoAttributeIf.IMsoStringIf getFirstnameAttribute();

    public void setID(String anID);

    public String getID();

    public IData.DataOrigin getIDState();

    public IMsoAttributeIf.IMsoStringIf getIDAttribute();

    public void setLastname(String aLastname);

    public String getLastName();

    public IData.DataOrigin getLastnameState();

    public IMsoAttributeIf.IMsoStringIf getLastnameAttribute();

    public void setPhoto(int aPhoto);

    public int getPhoto();

    public IData.DataOrigin getPhotoState();

    public IMsoAttributeIf.IMsoIntegerIf getPhotoAttribute();

    public void setResidence(Position aResidence);

    public Position getResidence();

    public IData.DataOrigin getResidenceState();

    public IMsoAttributeIf.IMsoPositionIf getResidenceAttribute();

    public void setAddress(String address);

    public String getAddress();

    public IData.DataOrigin getAddressState();

    public IMsoAttributeIf.IMsoStringIf getAddressAttribute();

    public void setTelephone1(String aTelephone1);

    public String getTelephone1();

    public IData.DataOrigin getTelephone1State();

    public IMsoAttributeIf.IMsoStringIf getTelephone1Attribute();

    public void setTelephone2(String aTelephone2);

    public String getTelephone2();

    public IData.DataOrigin getTelephone2State();

    public IMsoAttributeIf.IMsoStringIf getTelephone2Attribute();

    public void setTelephone3(String aTelephone3);

    public String getTelephone3();

    public IData.DataOrigin getTelephone3State();

    public IMsoAttributeIf.IMsoStringIf getTelephone3Attribute();


    public void setGender(PersonGender aGender);

    public void setGender(String aGender);

    public PersonGender getGender();

    public String getGenderText();

    public IData.DataOrigin getGenderState();

    public IMsoAttributeIf.IMsoEnumIf<PersonGender> getGenderAttribute();


    public int getAge();
}
