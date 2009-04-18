package org.redcross.sar.wp;

import java.awt.event.ActionListener;
import java.lang.instrument.IllegalClassFormatException;

import javax.swing.JButton;

import org.redcross.sar.IDiskoRole;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
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

    public DiskoWpIntelligenceImpl() throws IllegalClassFormatException
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
                m_ListButton = DiskoButtonFactory.createButton("List","",null,ButtonSize.NORMAL);
                m_ListButton.addActionListener(new ActionListener()
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
                m_SituationButton = DiskoButtonFactory.createButton("Sitasjon","",null,ButtonSize.NORMAL);
                m_SituationButton.addActionListener(new ActionListener() {
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
                m_PoiButton = DiskoButtonFactory.createButton("PUI","",null,ButtonSize.NORMAL);
                m_PoiButton.addActionListener(new ActionListener() {
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
                m_HypothesisButton = DiskoButtonFactory.createButton("Hypotese","",null,ButtonSize.NORMAL);
                m_HypothesisButton.addActionListener(new ActionListener() {
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

	public boolean rollback() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean commit() {
		// TODO Auto-generated method stub
		return false;
	}

}
