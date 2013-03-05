package org.redcross.sar.wp;

import java.awt.event.ActionListener;
import java.lang.instrument.IllegalClassFormatException;

import javax.swing.JButton;

import org.apache.log4j.Logger;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.map.DiskoMap;


/**
 *
 */
public class DiskoWpBriefingImpl extends AbstractDiskoWpModule implements IDiskoWpBriefing
{

    private JButton m_situationButton = null;

    public DiskoWpBriefingImpl() throws IllegalClassFormatException
    {
        super(Logger.getLogger(DiskoWpBriefingImpl.class));
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
        layoutButton(getSituationButton());
    }

	public String getCaption() {
		return "5PO";
	}

    private JButton getSituationButton()
    {
        //IApplication app = getDiskoRole().getApplication();
        if (m_situationButton == null)
        {
            try
            {
                m_situationButton = DiskoButtonFactory.createButton("Situasjon","",null,ButtonSize.NORMAL);
                m_situationButton.addActionListener(new ActionListener()
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

	public boolean rollback() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean commit() {
		// TODO Auto-generated method stub
		return false;
	}

}
