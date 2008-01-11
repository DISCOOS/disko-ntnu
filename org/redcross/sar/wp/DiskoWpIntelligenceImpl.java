package org.redcross.sar.wp;

import java.lang.instrument.IllegalClassFormatException;

import javax.swing.JButton;

import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.map.DiskoMap;

/**
 *
 */
public class DiskoWpIntelligenceImpl extends AbstractDiskoWpModule implements IDiskoWpIntelligence
{

    private JButton m_ListButton = null;
    private JButton m_SituationButton = null;
    private JButton m_PoiButton = null;
    private JButton m_HypothesisButton = null;

    public DiskoWpIntelligenceImpl(IDiskoRole role) throws IllegalClassFormatException
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

	public String getCaption() {
		return "Etterretning";
	}


    public JButton getListButton()
    {
        if (m_ListButton == null)
        {
            try
            {
                m_ListButton = createNormalButton("Liste", new java.awt.event.ActionListener()
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
                m_SituationButton = createNormalButton("Situasjon", new java.awt.event.ActionListener()
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
                m_PoiButton = createNormalButton("PUI", new java.awt.event.ActionListener()
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
                m_HypothesisButton = createNormalButton("Hypotese", new java.awt.event.ActionListener()
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

	public boolean cancel() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean finish() {
		// TODO Auto-generated method stub
		return false;
	}

}
