package org.redcross.sar.wp;

import java.lang.instrument.IllegalClassFormatException;

import javax.swing.JButton;

import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.map.DiskoMap;


/**
 *
 */
public class DiskoWpBriefingImpl extends AbstractDiskoWpModule implements IDiskoWpBriefing
{

    private JButton m_situationButton = null;

    public DiskoWpBriefingImpl(IDiskoRole role) throws IllegalClassFormatException
    {
        super(role);
        initialize();
    }

    private void initialize()
    {
        loadProperties("properties");
        defineSubMenu();

//        DiskoMap map = getMap();
//        map.setIsEditable(true);
//        layoutComponent(map);
    }

    public DiskoMap getMap()
    {
        return null;
    }


    private void defineSubMenu()
    {
        layoutButton(getSituationButton());
    }

	public String getCaption() {
		return "5PO";
	}

    private JButton getSituationButton()
    {
        IDiskoApplication app = getDiskoRole().getApplication();
        if (m_situationButton == null)
        {
            try
            {
                m_situationButton = createNormalButton("Situasjon", new java.awt.event.ActionListener()
                {
                    public void actionPerformed(java.awt.event.ActionEvent e)
                    {
                    }
                });
            }
            catch (java.lang.Throwable e)
            {
                e.printStackTrace();
            }
        }
        return m_situationButton;
    }

	public boolean cancel() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean finish() {
		// TODO Auto-generated method stub
		return false;
	}

}
