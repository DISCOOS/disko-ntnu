package org.redcross.sar.gui.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.command.IMapCommand.MapCommandType;
import org.redcross.sar.util.Utils;

import com.esri.arcgis.beans.map.MapBean;
import com.esri.arcgis.controls.IMapControlEvents2Adapter;
import com.esri.arcgis.controls.IMapControlEvents2OnMapReplacedEvent;
import com.esri.arcgis.controls.IMapControlEvents2OnExtentUpdatedEvent;
import com.esri.arcgis.interop.AutomationException;

public class ScalePanel extends DefaultPanel {
	
	private static final long serialVersionUID = 1L;
	private static final String[] scales = {"1:2500", "1:5000", "1:10000", 
											"1:25 000", "1:50 000","1:250 000",
											"1:500 000","1:1000 000"};
	
	private JPanel m_scaleListPanel = null;
	private ButtonGroup m_group = null;
	private JToggleButton m_custom = null;
	
	private IDiskoMap m_map = null;
	
	private Map<JToggleButton,JTextField> m_textFields = null;

	
	public ScalePanel() {

		// forward
		super("Sett skala",false,true,ButtonSize.SMALL);
		
		// prepare
		m_group = new ButtonGroup();
		m_textFields = new HashMap<JToggleButton,JTextField>();
		
		// initialize gui
		initialize();
	}
	
	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		try {
			
			// create content
			setBodyComponent(getScaleListPanel());

		}
		catch (java.lang.Throwable e) {
			//  Do Something
			e.printStackTrace();
		}
	}
	
	private JPanel getScaleListPanel() {
		if (m_scaleListPanel == null) {
			try {						
				
				m_scaleListPanel = new JPanel();
				m_scaleListPanel.setBorder(null);
				m_scaleListPanel.setLayout(new BoxLayout(m_scaleListPanel,BoxLayout.Y_AXIS));
				
				// add items
				addScale("1:1",true,true,false);
				for(int i=0;i<scales.length;i++) {
					// get new panel
					addScale(scales[i],false,false,true);					
				}				
				
			} catch (java.lang.Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return m_scaleListPanel;
	}	

	private void addScale(String scale, boolean editable, boolean custom, boolean strut) {
		// add strut?
		//if(strut) m_scaleListPanel.add(Box.createVerticalStrut(5));
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
		Dimension size = new Dimension(150,32);
		item.setPreferredSize(size);
		
		// add action listener
		item.addActionListener(new ActionListener() {
			
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
				item.setText("1:"+String.valueOf((int)oldScale));
				// forward
				apply(newScale);
			}			
		});
		return item;
	}
	
	private JToggleButton createScaleItemButton() {
		// add new button
		JToggleButton item = DiskoButtonFactory.createToggleButton(
				MapCommandType.SCALE_COMMAND, ButtonSize.SMALL);
		// add action listener
		item.addActionListener(new ActionListener() {
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
				}
			}			
		});
		return item;
	}
	
	private void apply(double newScale) {
		
		// consume changes
		setChangeable(false);
		
		try {
			// is new scale valid?
			if(newScale!=0) {
				// get current scale
				double oldScale = ((MapBean)m_map).getMapScale();
				// any change?
				if(newScale != oldScale) {
					// notify
					doAction("finish");
					// apply new scale
					((MapBean)m_map).setMapScale((int)newScale);
					// a full refresh is needed!
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

		// resume changes
		setChangeable(true);
	}
	
	public void update() {
		
		// consume changes
		setChangeable(false);
		
		try {
			// get current map scale
			double scale = ((MapBean)m_map).getMapScale();
			// goto item
			gotoItem("1:"+(int)scale);
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		setChangeable(true);
		
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
		Utils.showMessage("Ulovlig verdi",
				"Du må benytte formatet 1:<skala>");
	}
	
	public void load(IDiskoMap map) {
		
		try {
			// remove old?
			if(m_map!=null)
				((MapBean)m_map).removeIMapControlEvents2Listener(adapter);
			
			// save m_map instanse
			m_map = map;
			
			//listen to do actions when the m_map is loaded
			((MapBean)m_map).addIMapControlEvents2Listener(adapter);
			
			// forward
			update();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	final IMapControlEvents2Adapter adapter = new IMapControlEvents2Adapter() {
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

	};

}
