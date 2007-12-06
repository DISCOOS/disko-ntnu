package org.redcross.sar.wp;

import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.map.DiskoMap;

import javax.swing.*;
/**
 * Created by IntelliJ IDEA.
 * User: vinjar
 * Date: 11.apr.2007
 */

/**
 *
 */
public class DiskoWpIntelligenceImpl extends AbstractDiskoWpModule implements IDiskoWpIntelligence
{

    private JButton m_ListButton = null;
    private JButton m_SituationButton = null;
    private JButton m_PoiButton = null;
    private JButton m_HypothesisButton = null;

    public DiskoWpIntelligenceImpl(IDiskoRole role)
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
        layoutButton(getListButton());
        layoutButton(getSituationButton());
        layoutButton(getPoiButton());
        layoutButton(getHypothesisButton());
    }

    /* (non-Javadoc)
    * @see com.geodata.engine.disko.task.DiskoAp#getName()
    */
    public String getName()
    {
        return "Etterretning";
    }

    public JButton getListButton()
    {
        if (m_ListButton == null)
        {
            try
            {
                m_ListButton = createLargeButton("Liste", new java.awt.event.ActionListener()
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
        return m_ListButton;
    }

    private JButton getSituationButton()
    {
        if (m_SituationButton == null)
        {
            try
            {
                m_SituationButton = createLargeButton("Situasjon", new java.awt.event.ActionListener()
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
        return m_SituationButton;
    }

    public JButton getPoiButton()
    {
        if (m_PoiButton == null)
        {
            try
            {
                m_PoiButton = createLargeButton("PUI", new java.awt.event.ActionListener()
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
        return m_PoiButton;
    }

    public JButton getHypothesisButton()
    {
        if (m_HypothesisButton == null)
        {
            try
            {
                m_HypothesisButton = createLargeButton("Hypotese", new java.awt.event.ActionListener()
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
        return m_HypothesisButton;
    }

	public void reInitWP()
	{
		// TODO Auto-generated method stub
		
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
