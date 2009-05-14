package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;
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

    public IMsoModelIf.ModificationState getBirthdateState();

    public IMsoAttributeIf.IMsoCalendarIf getBirthdateAttribute();

    public void setFirstname(String aFirstname);

    public String getFirstName();

    public IMsoModelIf.ModificationState getFirstnameState();

    public IMsoAttributeIf.IMsoStringIf getFirstnameAttribute();

    public void setID(String anID);

    public String getID();

    public IMsoModelIf.ModificationState getIDState();

    public IMsoAttributeIf.IMsoStringIf getIDAttribute();

    public void setLastname(String aLastname);

    public String getLastName();

    public IMsoModelIf.ModificationState getLastnameState();

    public IMsoAttributeIf.IMsoStringIf getLastnameAttribute();

    public void setPhoto(int aPhoto);

    public int getPhoto();

    public IMsoModelIf.ModificationState getPhotoState();

    public IMsoAttributeIf.IMsoIntegerIf getPhotoAttribute();

    public void setResidence(Position aResidence);

    public Position getResidence();

    public IMsoModelIf.ModificationState getResidenceState();

    public IMsoAttributeIf.IMsoPositionIf getResidenceAttribute();

    public void setAddress(String address);

    public String getAddress();

    public IMsoModelIf.ModificationState getAddressState();

    public IMsoAttributeIf.IMsoStringIf getAddressAttribute();

    public void setTelephone1(String aTelephone1);

    public String getTelephone1();

    public IMsoModelIf.ModificationState getTelephone1State();

    public IMsoAttributeIf.IMsoStringIf getTelephone1Attribute();

    public void setTelephone2(String aTelephone2);

    public String getTelephone2();

    public IMsoModelIf.ModificationState getTelephone2State();

    public IMsoAttributeIf.IMsoStringIf getTelephone2Attribute();

    public void setTelephone3(String aTelephone3);

    public String getTelephone3();

    public IMsoModelIf.ModificationState getTelephone3State();

    public IMsoAttributeIf.IMsoStringIf getTelephone3Attribute();


    public void setGender(PersonGender aGender);

    public void setGender(String aGender);

    public PersonGender getGender();

    public String getGenderText();

    public IMsoModelIf.ModificationState getGenderState();

    public IMsoAttributeIf.IMsoEnumIf<PersonGender> getGenderAttribute();


    public int getAge();
}
