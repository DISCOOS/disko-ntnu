package org.redcross.sar.gui.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.command.ScaleCommand;

import com.borland.jbcl.layout.VerticalFlowLayout;
import com.esri.arcgis.beans.map.MapBean;
import com.esri.arcgis.controls.IMapControlEvents2Adapter;
import com.esri.arcgis.controls.IMapControlEvents2OnMapReplacedEvent;
import com.esri.arcgis.controls.IMapControlEvents2OnExtentUpdatedEvent;
import com.esri.arcgis.interop.AutomationException;

public class ScaleDialog extends DiskoDialog {
	
	private static final long serialVersionUID = 1L;
	private static final String[] scales = {"1:2500", "1:5000", "1:10000", 
											"1:25 000", "1:50 000","1:250 000",
											"1:500 000","1:1000 000"};
	
	private JPanel m_mainPanel = null;
	private JPanel m_contentPanel = null;
	private JPanel m_scaleListPanel = null;
	private JScrollPane m_scrollPane = null;
	private IDiskoMap m_map = null;
	private ButtonGroup m_group = null;
	private JToggleButton m_custom = null;
	private IDiskoApplication m_app = null;
	private HashMap<JToggleButton,JTextField> m_textFields = null;
	private boolean m_isApplying = false; 
	
	public ScaleDialog(IDiskoApplication app, ScaleCommand tool) {
		super(app.getFrame());
		m_app = app;
		m_group = new ButtonGroup();
		m_textFields = new HashMap<JToggleButton,JTextField>();
		initialize();
	}
	
	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		try {
			
            this.setContentPane(getContentPanel());
            this.setPreferredSize(new Dimension(200, 320));
            this.pack();
		}
		catch (java.lang.Throwable e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method initializes m_contentPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getContentPanel() {
		if (m_contentPanel == null) {			
			try {
				BorderLayout borderLayout = new BorderLayout();
				borderLayout.setVgap(5);
				m_contentPanel = new JPanel();
				m_contentPanel.setLayout(borderLayout);
				m_contentPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
				m_contentPanel.add(getMainPanel(), BorderLayout.CENTER);

			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}			
		}
		return m_contentPanel;
	}
	
	/**
	 * This method initializes m_mainPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getMainPanel() {
		if (m_mainPanel == null) {			
			try {
				BorderLayout bl = new BorderLayout();
				m_mainPanel = new JPanel();
				m_mainPanel.setLayout(bl);
				m_mainPanel.setBorder(BorderFactory.createTitledBorder(null, 
						null, TitledBorder.LEFT, TitledBorder.TOP, 
						new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
				m_mainPanel.add(getScrollPane(), BorderLayout.CENTER);

			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}			
		}
		return m_mainPanel;
	}
	
	private JScrollPane getScrollPane() {
		if (m_scrollPane == null) {
			try {						
				// create scroll pane
				m_scrollPane = new JScrollPane(getScaleListPanel());
				m_scrollPane.setBorder(null);
			} catch (java.lang.Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return m_scrollPane;		
	}

	private JPanel getScaleListPanel() {
		if (m_scaleListPanel == null) {
			try {						
				
				VerticalFlowLayout vfl = new VerticalFlowLayout();
				vfl.setVgap(0);
				vfl.setHgap(0);
				vfl.setAlignment(VerticalFlowLayout.TOP);
				m_scaleListPanel = new JPanel();
				m_scaleListPanel.setLayout(vfl);
				m_scaleListPanel.setBorder(null);
				// add items
				addScale("1:1",true,true);
				for(int i=0;i<scales.length;i++) {
					// get new panel
					addScale(scales[i],false,false);					
				}				
				
			} catch (java.lang.Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return m_scaleListPanel;
	}	

	private void addScale(String scale, boolean editable, boolean custom) {
		// get item
		JPanel item = createScaleItemPanel(scale,editable,custom);
		// add to panel
		m_scaleListPanel.add(item);		
	}
	
	private JPanel createScaleItemPanel(String scale, boolean editable, boolean custom) {
		
		// initialize
		JToggleButton button = null;
		JTextField textField = null;
		
		// add new item panel
		JPanel item = new JPanel();
		item.setBorder(null);
		FlowLayout layout = new FlowLayout();
		layout.setHgap(5);
		item.setLayout(layout);
		
		// add object pair
		textField = createScaleItemText(scale);
		textField.setEditable(editable);
		button = createScaleItemButton();
		item.add(textField, FlowLayout.LEFT);
		item.add(button, FlowLayout.CENTER);
		// add to list
		m_textFields.put(button, textField);
		// add to button group
		m_group.add(button);
		// set as custom?
		if (custom) 
			m_custom = button;
		// return panel
		return item;
	}

	private JTextField createScaleItemText(String scale) {
		
		// add new item panel
		BorderLayout borderLayout = new BorderLayout();
		borderLayout.setHgap(5);
		JTextField item = new JTextField(scale);
		Dimension size = new Dimension(120,30);
		item.setPreferredSize(size);
		
		// add action listener
		item.addActionListener(new java.awt.event.ActionListener() {
			
			double oldScale = 0;
			
			public void actionPerformed(ActionEvent e) {
				// get item
				JTextField item = (JTextField)e.getSource();
				// get new scale
				double newScale = getScaleValue(item.getText());
				// invalid?
				if(newScale==0) {
					// notify user
					notifyInvalidNumber();
				}
				else {
					// changed?
					if(oldScale != newScale) {
						oldScale = newScale;
					}					
				}
				// replace text old text
				item.setText("1:"+oldScale);
			}			
		});
		return item;
	}
	
	private JToggleButton createScaleItemButton() {
		// add new button
		Dimension size = new Dimension(30,30);
		JToggleButton item = new JToggleButton("Bruk");
		item.setPreferredSize(size);
		// add action listener
		item.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// get button
				JToggleButton button = (JToggleButton)e.getSource();
				// is selected?
				if(button.isSelected()) {
					// initialize
					double newScale = 0;
					// get text field
					JTextField textField = m_textFields.get(button);
					// get new scale
					newScale = getScaleValue(textField.getText());
					// forward
					apply(newScale);
					// hide me
					setVisible(false);
				}
			}			
		});
		return item;
	}
	
	private void apply(double newScale) {
		if(m_isApplying)
			return;
		m_isApplying = true;
		try {
			// is new scale valid?
			if(newScale!=0) {
				// get current scale
				double oldScale = ((MapBean)m_map).getMapScale();
				// any change?
				if(newScale != oldScale) {
					// apply new scale
					((MapBean)m_map).setMapScale((int)newScale);
					// apply to map
					m_map.refresh();
					// set button
					gotoItem("1:"+(int)newScale);
				}
			}
			else {
				notifyInvalidNumber();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		m_isApplying = false;
	}
	
	private void update() {
		if(m_isApplying)
			return;
		try {
			// get current map scale
			double scale = ((MapBean)m_map).getMapScale();
			// goto item
			gotoItem("1:"+(int)scale);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	// update to scale ratio
	private void gotoItem(String item) {
		try {
			// get key iterator
			Iterator<JToggleButton> it = m_textFields.keySet().iterator();
			// loop over all
			while(it.hasNext()){
				// get button
				JToggleButton button = it.next();
				// is not custom button?
				if(!m_custom.equals(button)) {
					// get text field
					JTextField textField = m_textFields.get(button);
					// found?
					if(item.equals(textField.getText().replace(" ", ""))) {
						button.setSelected(true);
						return;
					}
				}
			}
			// not found, update custom scale and select it
			JTextField textField = m_textFields.get(m_custom);
			textField.setText(item);
			m_custom.setSelected(true);						
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private double getScaleValue(String scale) {
		// remove spaces
		scale = scale.replace(" ", "");
		// get text
		String prefix = scale.substring(0, 2);
		String number = scale.substring(2);
		// check text
		if(prefix.equals("1:")) {
			// is not numeric?
			if(!isNumeric(number)){
				notifyInvalidNumber();
				return 0;
			}
		}
		else {
			// get number
			number = scale;
			// is not numeric?
			if(!isNumeric(number)) {
				notifyInvalidNumber();
				return 0;
			}
		}
		return Double.valueOf(number);
	}
		
	private boolean isNumeric(String s) {
		try
		{
			Integer.parseInt(s);
			return true;
		}
			catch(NumberFormatException e)
		{
			return false;
		}				
	}
	
	private void notifyInvalidNumber() {
		// notfiy user
		JOptionPane.showMessageDialog(
            m_app.getFrame(),
            "Du må benytte formatet 1:<skala>",
            "Ulovlig verdi",
            JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void onLoad(IDiskoMap map) throws IOException {
		
		// save m_map instanse
		m_map = map;
		
		//listen to do actions when the m_map is loaded
		((MapBean)m_map).addIMapControlEvents2Listener(new IMapControlEvents2Adapter() {
			private static final long serialVersionUID = 1L;
			public void onMapReplaced(IMapControlEvents2OnMapReplacedEvent e)
                   	throws java.io.IOException, AutomationException {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						update();
					}
				});				
			}
			public void onExtentUpdated(IMapControlEvents2OnExtentUpdatedEvent theEvent)
            	throws java.io.IOException, AutomationException {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						update();
					}
				});				
			}

		});
		update();
	}

}
