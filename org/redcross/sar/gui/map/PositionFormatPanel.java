/**
 * 
 */
package org.redcross.sar.gui.map; 

import java.util.ArrayList;
import java.util.List;

import java.awt.FlowLayout;
import java.awt.Dimension;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author kennetgu
 *
 */
public class PositionFormatPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JToggleButton m_MGRSButton = null;
	private JToggleButton m_UTMButton = null;
	private ButtonGroup m_buttonGroup = null;
	
	private ArrayList<POIFormatEventListener> m_listeners = null;
	
	private int m_format = 1; // MGRS
	
	/**
	 * Constructor
	 */
	public PositionFormatPanel() {
		// forward
		super();
		// prepare
		m_listeners = new ArrayList<POIFormatEventListener>();
		// initialise GUI
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		FlowLayout fl = new FlowLayout();
		fl.setAlignment(FlowLayout.LEFT);
		fl.setHgap(5);
		fl.setVgap(0);
		setLayout(fl);
		setPreferredSize(new Dimension(350,50));
		JLabel label = new JLabel("Posisjonsformat");
		add(label,null);
		add(getMGRSButton(), null);
		add(getUTMButton(), null);
		m_buttonGroup = new ButtonGroup();
		m_buttonGroup.add(getMGRSButton());
		m_buttonGroup.add(getUTMButton());

		// set MGRS format
		setFormat(m_format); 
	}

	/**
	 * This method initializes m_MGRSButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JToggleButton getMGRSButton() {
		if (m_MGRSButton == null) {
			m_MGRSButton = DiskoButtonFactory.createToggleButton("FORMAT.MGRS",ButtonSize.NORMAL);
			m_MGRSButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					setFormat(1);
				}
			});
		}
		return m_MGRSButton;
	}

	/**
	 * This method initializes m_UTMButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JToggleButton getUTMButton() {
		if (m_UTMButton == null) {
			m_UTMButton = DiskoButtonFactory.createToggleButton("FORMAT.UTM",ButtonSize.NORMAL);
			m_UTMButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					setFormat(2);
				}
			});
		}
		return m_UTMButton;
	}
	
	public int getFormat() {
		return m_format;
	}
	
	public void setFormat(int format) {
		// set format
		m_format = format;
		// get formaters
		switch(format) {
		case 1: // MGRS
			if (!m_MGRSButton.isSelected())
				m_MGRSButton.setSelected(true);
			break;
		case 2: // UTM
			if (!m_UTMButton.isSelected())
				m_UTMButton.setSelected(true);
			break;
		}
		// notify
		fireFormatChanged();
	}
	
	public void addPOIFormatEventListener(POIFormatEventListener listener) {
		m_listeners.add(listener);
	}
	
	public void removePOIFormatEventListener(POIFormatEventListener listener) {
		m_listeners.remove(listener);
	}
	
  	/*========================================================
  	 * Helper methods
  	 *========================================================
  	 */
	
	private void fireFormatChanged() {
		for(POIFormatEventListener it: (List<POIFormatEventListener>)m_listeners) {
			it.formatChanged(m_format);
		}
	}
	
  	/*========================================================
  	 * Inner classes
  	 *========================================================
  	 */
  	
	public interface POIFormatEventListener {
		public void formatChanged(int format);
	}
	
	
}  //  @jve:decl-index=0:visual-constraint="7,7"
