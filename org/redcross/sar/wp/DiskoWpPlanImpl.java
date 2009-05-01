package org.redcross.sar.wp;

import java.awt.event.ActionListener;
import java.lang.instrument.IllegalClassFormatException;

import javax.swing.JButton;

import org.redcross.sar.IApplication;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.map.DiskoMap;

/**
 *
 */
public class DiskoWpPlanImpl extends AbstractDiskoWpModule implements IDiskoWpPlan
{

    private JButton m_tidsplanButton = null;
    private JButton m_grovplanButton = null;

    public DiskoWpPlanImpl() throws IllegalClassFormatException
    {
        super();
        initialize();
    }

    private void initialize()
    {
        defineSubMenu();
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
        IApplication app = getDiskoRole().getApplication();
        if (m_tidsplanButton == null)
        {
            try
            {
                m_tidsplanButton = DiskoButtonFactory.createButton("Tidsplan","",null,ButtonSize.NORMAL);
                m_tidsplanButton.addActionListener(new ActionListener() {
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
        IApplication app = getDiskoRole().getApplication();
        if (m_grovplanButton == null)
        {
            try
            {
                m_grovplanButton = DiskoButtonFactory.createButton("Grov plan","",null,ButtonSize.NORMAL);
                m_grovplanButton.addActionListener(new ActionListener() {
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

	public boolean rollback() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean commit() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
