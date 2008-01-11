package org.redcross.sar.wp;

import java.lang.instrument.IllegalClassFormatException;

import javax.swing.JButton;

import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.map.DiskoMap;

/**
 *
 */
public class DiskoWpPlanImpl extends AbstractDiskoWpModule implements IDiskoWpPlan
{

    private JButton m_tidsplanButton = null;
    private JButton m_grovplanButton = null;

    public DiskoWpPlanImpl(IDiskoRole role) throws IllegalClassFormatException
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
        layoutButton(getTidsplanButton());
        layoutButton(getGrovplanButton());
    }

	public String getCaption() {
		return "Plan";
	}
	

   private JButton getTidsplanButton()
    {
        IDiskoApplication app = getDiskoRole().getApplication();
        if (m_tidsplanButton == null)
        {
            try
            {
                m_tidsplanButton = createNormalButton("Tidsplan", new java.awt.event.ActionListener()
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
        return m_tidsplanButton;
    }

    private JButton getGrovplanButton()
    {
        IDiskoApplication app = getDiskoRole().getApplication();
        if (m_grovplanButton == null)
        {
            try
            {
                m_grovplanButton = createNormalButton("Grovplan", new java.awt.event.ActionListener()
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
        return m_grovplanButton;
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
