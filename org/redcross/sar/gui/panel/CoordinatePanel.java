package org.redcross.sar.gui.panel; 

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.awt.GridBagLayout;

import java.awt.Insets;
import java.awt.Dimension;
import java.awt.GridBagConstraints;

import javax.swing.BoxLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.redcross.sar.gui.document.UpperCaseDocument;
import org.redcross.sar.gui.format.DEGFormat;
import org.redcross.sar.gui.format.DEMFormat;
import org.redcross.sar.gui.format.DESFormat;
import org.redcross.sar.gui.format.MGRSFormat;
import org.redcross.sar.gui.format.UTMFormat;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.mso.Position;

import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.geometry.Point;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author kennetgu
 *
 */
public class CoordinatePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final String NULL_POSITION = "Ingen posisjon";
	
	private static final String[] zones = {"32V","33V","32W","33W","34W","35W","36W"};  //  @jve:decl-index=0:
	private static final String[] squares = {"KR","KQ","KP","KN","KM","KL",
								"LR","LQ","LP","LN","LM","LL","LK",
								"MR","MQ","MP","MN","MM","ML","MK",
								"NT","NS","NR","NQ","NP","NN","NM","NL","NK",
								"PA","PV","PU","PT","PS","PR","PQ","PP","PN","PM","PL",
								"UR","UQ","UP","UN","UM","UL","UK","UJ","UH","UG",
								"VS","VR","VQ","VP","VN","VM","VL",
								"WT","WS","WR","WQ","WP",
								"DD","DC","DB","DA",
								"ED","EC","EB",
								"FD","FC","FB",
								"LU","LT","LS",
								"MU","MT","MS",
								"NU","NT","NS",
								"PU","PT","PS",
								"UD","UC",
								"VD","VC"};  //  @jve:decl-index=0:
	
	private JPanel m_right = null;
	private JLabel m_latitudeLabel = null;
	private JFormattedTextField m_latitudeText = null;
	private JLabel m_longetudeLabel = null;
	private JPanel m_left = null;
	private JFormattedTextField m_longetudeText = null;
	private JLabel m_zoneLabel = null;
	private JLabel m_squareLabel = null;
	
	private int m_format = 1; // MGRS
	
	private JComboBox m_ZoneCombo = null;
	private JComboBox m_SquareCombo = null;
	
	private int workCount = 0;	
	private boolean isInvalid = true;
	
	private Position current = null;
	
	private List<ChangeListener> m_listeners = null;
	
	/**
	 * Constructor
	 * 
	 * @throws ParseException 
	 */
	public CoordinatePanel() throws ParseException {
		// forward
		super();
		// prepare
		m_listeners = new ArrayList<ChangeListener>();
		// initialize GUI
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		// setup content pane
		this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
		Utils.setFixedSize(this, 225, 50);
		// add components
		this.add(getLeft());
		this.add(getRight());
		// set MGRS format
		setFormat(1); 
	}
	
	public void setEditable(boolean isEditable) {
		m_ZoneCombo.setEditable(isEditable);
		m_SquareCombo.setEditable(isEditable);
		m_ZoneCombo.setEnabled(isEditable);
		m_SquareCombo.setEnabled(isEditable);
		m_latitudeText.setEditable(isEditable);
		m_longetudeText.setEditable(isEditable);		
	}
	
	public boolean isEditable() {
		return m_latitudeText.isEditable();
	}
	
	public boolean isPositionValid() {
		return !isInvalid;
	}
	
	private void setInvalidPosiiton(boolean isInvalid) {
		this.isInvalid = isInvalid;
	}
	
	/**
	 * This method set mgrs from text
	 * 
	 * @return void
	 */
	public void setText(String text) {
		
		// consume?
		if(isWorking()) return;
		
		// prevent reentry
		setIsWorking();
		
		// initialize flag
		boolean bEmpty = true;
		
		// get current caret positions
		int ix = m_longetudeText.getCaretPosition();
		int iy = m_latitudeText.getCaretPosition();
		
		// assume valid position
		setInvalidPosiiton(false);
		
		try {
			// is not null?
			if (text != null) {
				// is empty?
				if (text.length() > 0) { 
					switch(getFormat()) {
					case 1: // MGRS						
						String zone = text.subSequence(0, 3).toString();
						String square = text.subSequence(3, 5).toString();
						// get coordinates
						String coords[] = MapUtil.getCoords(text.subSequence(5, text.length()).toString());						
						// get x and y coordinates
						String x = coords[0];
						String y = coords[1];
						
						getZoneCombo().setSelectedItem(zone);
						getSquareCombo().setSelectedItem(square);
						getLatitudeText().setValue(x);
						getLongetudeText().setValue(y);
						bEmpty = false;
						break;
					case 2: // UTM
						zone = text.subSequence(0, 3).toString();
						// get coordinates
						coords = MapUtil.getCoords(text.subSequence(3, text.length()).toString());						
						// get x and y coordinates
						x = coords[0];
						y = coords[1];
						
						getZoneCombo().setSelectedItem(zone);
						getLatitudeText().setValue(x);
						getLongetudeText().setValue(y);
						bEmpty = false;
						break;					
					case 3: // DES
						String[] split = text.split("E");
						getLatitudeText().setValue(split[0]);
						split = split[1].split("N");
						getLongetudeText().setValue(split[0]);
						bEmpty = false;
						break;					
					case 4: // DEM
						split = text.split("E");
						x = MapUtil.unformatDEM(split[0]);
						getLatitudeText().setValue(x);
						split = split[1].split("N");
						y = MapUtil.unformatDEM(split[0]);
						getLongetudeText().setValue(y);
						bEmpty = false;
						break;
					case 5: // DEG
						split = text.split("E");
						x = MapUtil.unformatDEG(split[0]);
						getLatitudeText().setValue(x);
						split = split[1].split("N");
						y = MapUtil.unformatDEG(split[0]);
						getLongetudeText().setValue(y);
						bEmpty = false;
						break;					
					}
					getLatitudeText().commitEdit();
					getLongetudeText().commitEdit();
				}
			}
		}
		catch(Exception e) {
			setInvalidPosiiton(true);
		}
		
		if (bEmpty) {
			getZoneCombo().setSelectedIndex(0);
			getSquareCombo().setSelectedIndex(0);
			getLatitudeText().setText("");
			getLongetudeText().setText("");
		}	
		
		// get current position
		current = getPositionFromText();
		
		// resume caret positions
		text = m_longetudeText.getText();		
		if(text!=null)
			m_longetudeText.setCaretPosition(Math.min(text.length(), ix));
		text = m_latitudeText.getText();
		if(text!=null)
			m_latitudeText.setCaretPosition(Math.min(text.length(), iy));
		
		// finished
		setIsNotWorking();
		
		// notify
		fireChangeEvent(this);
		
	}
	
	/**
	 * This method get mgrs text
	 * 
	 * @return String
	 */
	public String getText() {
		
		// initalize
		String text = NULL_POSITION;
		
		// is allowed?
		if(isPositionValid()) { 
			
			// translate to format
			switch(getFormat()) {
			case 1: // MGRS
				text = (getZoneCombo().getSelectedItem()==null ? "" : 
						getZoneCombo().getSelectedItem().toString()) +
						(getSquareCombo().getSelectedItem()==null ? "" : 
							getSquareCombo().getSelectedItem().toString()) +
					getLatitudeText().getText() + getLongetudeText().getText();
				break;	
			case 2:	// UTM
				text = (getZoneCombo().getSelectedItem()==null ? "" : 
					getZoneCombo().getSelectedItem().toString()) +
					getLatitudeText().getText() + getLongetudeText().getText();
				break;	
			case 3: // DES
				text = getLatitudeText().getText() + getLongetudeText().getText();
				break;	
			case 4: // DEM
				text = getLatitudeText().getText() + getLongetudeText().getText();
				break;	
			case 5: // DEG
				text = getLatitudeText().getText() + getLongetudeText().getText();
				break;	
			}
		}
		
		// finished
		return text;
	}
	
	/**
	 * This method initializes m_left	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getLeft() {
		if (m_left == null) {
			// create panel
			m_left = new JPanel();
			m_left.setLayout(new GridBagLayout());
			m_left.setAlignmentX(JPanel.LEFT_ALIGNMENT);
			m_left.setAlignmentY(JPanel.TOP_ALIGNMENT);
			m_left.setOpaque(false);			
			Utils.setFixedSize(m_left, 90,50);
			// create labels
			m_zoneLabel = new JLabel();
			m_zoneLabel.setOpaque(false);
			m_zoneLabel.setText("Sone");
			m_squareLabel = new JLabel();
			m_squareLabel.setOpaque(false);
			m_squareLabel.setText("Rute");
			// create constraints
			GridBagConstraints c1 = new GridBagConstraints();
			c1.gridx = 0;
			c1.gridy = 0;
			c1.fill = GridBagConstraints.HORIZONTAL;
			c1.anchor = GridBagConstraints.NORTHWEST;
			c1.insets = new Insets(0, 0, 5, 5);
			GridBagConstraints c2 = new GridBagConstraints();
			c2.gridx = 1;
			c2.gridy = 0;
			c2.weightx = 0.5;
			c2.weighty = 0.5;
			c2.fill = GridBagConstraints.HORIZONTAL;
			c2.anchor = GridBagConstraints.NORTHWEST;
			c2.insets = new Insets(0, 0, 5, 0);
			GridBagConstraints c3 = new GridBagConstraints();
			c3.gridx = 0;
			c3.gridy = 1;
			c3.fill = GridBagConstraints.HORIZONTAL;
			c3.anchor = GridBagConstraints.NORTHWEST;
			c3.insets = new Insets(0, 0, 0, 0);
			GridBagConstraints c4 = new GridBagConstraints();
			c4.gridx = 1;
			c4.gridy = 1;
			c4.weightx = 0.5;
			c4.weighty = 0.5;
			c4.fill = GridBagConstraints.HORIZONTAL;
			c4.anchor = GridBagConstraints.NORTHWEST;
			c4.insets = new Insets(0, 0, 0, 0);
			m_left.add(m_zoneLabel, c1);
			m_left.add(getZoneCombo(), c2);
			m_left.add(m_squareLabel, c3);
			m_left.add(getSquareCombo(), c4);
		}
		return m_left;
	}

	/**
	 * This method initializes m_right	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getRight() {
		if (m_right == null) {
			// create panel
			m_right = new JPanel();
			m_right.setLayout(new GridBagLayout());
			m_right.setAlignmentX(JPanel.LEFT_ALIGNMENT);
			m_right.setAlignmentY(JPanel.TOP_ALIGNMENT);
			m_right.setOpaque(false);
			Utils.setFixedSize(m_right, 135, 50);
			// create labels
			m_latitudeLabel = new JLabel();
			m_latitudeLabel.setOpaque(false);
			m_latitudeLabel.setText("X");
			m_longetudeLabel = new JLabel();
			m_longetudeLabel.setOpaque(false);
			m_longetudeLabel.setText("Y");
			// create constraints
			GridBagConstraints c1 = new GridBagConstraints();
			c1.gridx = 0;
			c1.gridy = 0;
			c1.fill = GridBagConstraints.HORIZONTAL;
			c1.anchor = GridBagConstraints.NORTHWEST;
			c1.weighty = 0.0;
			c1.weightx = 0.0;
			c1.insets = new Insets(0, 15, 5, 5);
			GridBagConstraints c2 = new GridBagConstraints();
			c2.gridx = 1;
			c2.gridy = 0;
			c2.weightx = 0.5;
			c2.weighty = 0.5;
			c2.fill = GridBagConstraints.HORIZONTAL;
			c2.anchor = GridBagConstraints.NORTHWEST;
			c2.insets = new Insets(0, 0, 5, 0);
			GridBagConstraints c3 = new GridBagConstraints();
			c3.gridx = 0;
			c3.gridy = 1;
			c3.anchor = GridBagConstraints.NORTHWEST;
			c3.fill = GridBagConstraints.NONE;
			c3.weightx = 0.0;
			c3.weighty = 0.0;
			c3.insets = new Insets(0, 15, 5, 5);
			GridBagConstraints c4 = new GridBagConstraints();
			c4.gridx = 1;
			c4.gridy = 1;
			c4.weightx = 0.5;
			c4.weighty = 0.5;
			c4.fill = GridBagConstraints.HORIZONTAL;
			c4.anchor = GridBagConstraints.NORTHWEST;
			c4.insets = new Insets(0, 0, 0, 0);
			m_right.add(m_latitudeLabel, c1);
			m_right.add(getLatitudeText(), c2);
			m_right.add(m_longetudeLabel, c3);
			m_right.add(getLongetudeText(), c4);
		}
		return m_right;
	}	
	
	/**
	 * This method initializes m_latitudeText	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JFormattedTextField getLatitudeText() {
		if (m_latitudeText == null) {
			m_latitudeText = new JFormattedTextField(new FormatFactory("E"));
			m_latitudeText.setPreferredSize(new Dimension(4, 20));
			m_latitudeText.setText("");
			m_latitudeText.addPropertyChangeListener("value", new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent e) {
				    // consume?
					if(isWorking()) return;
					// get position
					current = getPositionFromText();
					// notify
					fireChangeEvent(e.getSource());
				}		
			});
		}
		return m_latitudeText;
	}
	
	/**
	 * This method initializes m_longetudeText	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JFormattedTextField getLongetudeText() {
		if (m_longetudeText == null) {
			m_longetudeText = new JFormattedTextField(new FormatFactory("N"));
			m_longetudeText.setPreferredSize(new Dimension(4, 20));
			m_longetudeText.addPropertyChangeListener("value", new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent e) {
				    // consume?
					if(isWorking()) return;
					// get position
					current = getPositionFromText();
					// notify
					fireChangeEvent(e.getSource());
				}		
			});
		}
		return m_longetudeText;
	}

	/**
	 * This method initializes m_ZoneCombo	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getZoneCombo() {
		if (m_ZoneCombo == null) {
			Arrays.sort(zones);
			m_ZoneCombo = new JComboBox(zones);
			//m_ZoneCombo.setEditable(true);
			m_ZoneCombo.setPreferredSize(new Dimension(60, 20));
			m_ZoneCombo.getModel().addListDataListener(m_listDataListener);
			JTextField editor = (JTextField)m_ZoneCombo.getEditor().getEditorComponent();
			editor.setDocument( new UpperCaseDocument() );		
			m_ZoneCombo.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent arg0) { /* NOP */ }
				
				@Override
				public void focusGained(FocusEvent e) {
					JTextField editor = (JTextField)m_ZoneCombo.getEditor().getEditorComponent();
					editor.selectAll();
				}

			});
			m_ZoneCombo.setSelectedIndex(0);
		}
		return m_ZoneCombo;
	}

	/**
	 * This method initializes m_SquareCombo	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getSquareCombo() {
		if (m_SquareCombo == null) {
			Arrays.sort(squares);
			m_SquareCombo = new JComboBox(squares);
			//m_SquareCombo.setEditable(true);
			m_SquareCombo.setPreferredSize(new Dimension(60, 20));
			m_SquareCombo.getModel().addListDataListener(m_listDataListener);
			JTextField editor = (JTextField)m_SquareCombo.getEditor().getEditorComponent();
			editor.setDocument( new UpperCaseDocument() );		
			m_SquareCombo.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent arg0) { /* NOP */ }
				
				@Override
				public void focusGained(FocusEvent e) {
					JTextField editor = (JTextField)m_SquareCombo.getEditor().getEditorComponent();
					editor.selectAll();
				}

			});
			m_SquareCombo.setSelectedIndex(0);
		}
		return m_SquareCombo;
	}
	
	public int getFormat() {
		return m_format;
	}
	
	public void setFormat(int format) {
		// consume?
		if(isWorking()) return;
		// limit format to legal range of [1,5]
		m_format = Math.min(5,Math.max(1,format));
		// get formaters
		switch(format) {
		case 1: // MGRS
			m_SquareCombo.setVisible(true);
			m_squareLabel.setVisible(true);
			m_ZoneCombo.setVisible(true);
			m_zoneLabel.setVisible(true);
			break;
		case 2: // UTM
			m_SquareCombo.setVisible(false);
			m_squareLabel.setVisible(false);
			m_ZoneCombo.setVisible(true);
			m_zoneLabel.setVisible(true);
			break;
		case 3: // DES
			m_SquareCombo.setVisible(false);
			m_squareLabel.setVisible(false);
			m_ZoneCombo.setVisible(false);
			m_zoneLabel.setVisible(false);
			break;
		case 4: // DEM
			m_SquareCombo.setVisible(false);
			m_squareLabel.setVisible(false);
			m_ZoneCombo.setVisible(false);
			m_zoneLabel.setVisible(false);
			break;
		case 5: // DEG
			m_SquareCombo.setVisible(false);
			m_squareLabel.setVisible(false);
			m_ZoneCombo.setVisible(false);
			m_zoneLabel.setVisible(false);
			break;
		}		
		// apply current position again
		setPosition(current);
		// notify
		fireChangeEvent(this);
	}

	public Point getPoint(ISpatialReference srs) {
		
		// initialize
		Position p = null;

		// get text
		String text = getText();		

		try {
			switch(getFormat()) {
			case 1: // MGRS
				p = MapUtil.getPositionFromMGRS(text);
				break;
			case 2: // UTM
				p = MapUtil.getPositionFromUTM(text);
				break;
			case 3: // DES
				p = MapUtil.getPositionFromDES(text);
				break;
			case 4: // DEM
				p = MapUtil.getPositionFromDEM(text);
				break;
			case 5: // DEG
				p = MapUtil.getPositionFromDEG(text);
				break;
			}
			return p!=null ? MapUtil.getEsriPoint(p.getGeoPos(), srs) : null;
		}
		catch (Exception e) {
			// consume;
		}

		return null;
	}
	
	public Position getPosition() {
		return current;
	}
		
	private Position getPositionFromText() {
		// assume valid position
		setInvalidPosiiton(false);
		
		// get text
		String text = getText();
		
		try {
			switch(getFormat()) {
			case 1: // MGRS
				return MapUtil.getPositionFromMGRS(text);
			case 2: // UTM
				return MapUtil.getPositionFromUTM(text);
			case 3: // DES
				return MapUtil.getPositionFromDES(text);
			case 4: // DEM
				return MapUtil.getPositionFromDEM(text);
			case 5: // DEG
				return MapUtil.getPositionFromDEG(text);
			}
		}
		catch (Exception e) {
			setInvalidPosiiton(true);		
		}

		return null;
	}
		
	public void setPosition(Position p) {
		
		// assume valid position
		setInvalidPosiiton(false);
		
		// is convertable
		if(p==null) {
			setText(null);
			current = null;
		}
		else {
			try {
				switch(getFormat()) {
				case 1: // MGRS
					setText(MapUtil.getMGRSfromPosition(p,5));
					break;
				case 2: // UTM
					setText(MapUtil.getUTMfromPosition(p));
					break;
				case 3: // DEG
					setText(MapUtil.getDESfromPosition(p));
					break;
				case 4: // DEM
					setText(MapUtil.getDEMfromPosition(p));
					break;
				case 5: // DEG
					setText(MapUtil.getDEGfromPosition(p));
					break;
				}
				// get current position
				current = p;
			}
			catch (Exception e) {
				setInvalidPosiiton(true);
			}
		}
	}	
	
	public void setPoint(Point p) {
		
		// assume valid position
		setInvalidPosiiton(false);
		
		// is convertable
		if(p==null) {
			setText(null);
			current = null;
		}
		else {
			try {
				switch(getFormat()) {
				case 1: // MGRS
					setText(MapUtil.getMGRSfromPoint(p,5));
					break;
				case 2: // UTM
					setText(MapUtil.getUTMfromPoint(p));
					break;
				case 3: // DES
					setText(MapUtil.getDESfromPoint(p));
					break;
				case 4: // DEM
					setText(MapUtil.getDEMfromPoint(p));
					break;
				case 5: // DEG
					setText(MapUtil.getDEGfromPoint(p));
					break;
				}
				// get current position
				current = MapUtil.getMsoPosistion(p);
			}
			catch (Exception e) {
				setInvalidPosiiton(true);
			}
		}
	}
	
	private void fireChangeEvent(Object source) {
		ChangeEvent e = new ChangeEvent(source);
		for(ChangeListener it: m_listeners) 
			it.stateChanged(e);
	}
	
	public boolean addChangeListener(ChangeListener listener) {
		if(!m_listeners.contains(listener))
			return m_listeners.add(listener);
		return false;
	}
	
	public boolean removeChangeListener(ChangeListener listener) {
		if(m_listeners.contains(listener))
			return m_listeners.remove(listener);
		return false;
	}
	
    private boolean isWorking() {
		return (workCount>0);
	}

    private int setIsWorking() {
		workCount++;
		return workCount; 
	}
	
    private int setIsNotWorking() {
		if(workCount>0) {
			workCount--;
		}
		return workCount; 
	}
	
    
    class FormatFactory extends JFormattedTextField.AbstractFormatterFactory {

    	private String direction = null;
    	
    	public FormatFactory(String direction) {
    		this.direction = direction;
    	}
    	
		@Override
		public AbstractFormatter getFormatter(JFormattedTextField tf) {
			try {
				// create formatter
				switch(getFormat()) {
				case 1: // MGRS
					return new MGRSFormat(direction);
				case 2: // UTM
					return new UTMFormat(direction);
				case 3: // DES
					return new DESFormat(direction);
				case 4: // DEM
					return new DEMFormat(direction);
				case 5: // DEG
					return new DEGFormat(direction);
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// failed
			return null;
		}
	   
    }
	
    private final ListDataListener m_listDataListener = new ListDataListener() {

		public void contentsChanged(ListDataEvent e) {
			if(isWorking()) return;
			fireChangeEvent(e.getSource());
		}

		public void intervalAdded(ListDataEvent e) {
			if(isWorking()) return;
			fireChangeEvent(e.getSource());
		}

		public void intervalRemoved(ListDataEvent e) {
			if(isWorking()) return;
			fireChangeEvent(e.getSource());
		}
		
	};
	
	@Override
	public void setEnabled(boolean isEnabled) {
		getSquareCombo().setEnabled(isEnabled);
		getZoneCombo().setEnabled(isEnabled);
		getLatitudeText().setEditable(isEnabled);
		getLongetudeText().setEditable(isEnabled);
		super.setEnabled(isEnabled);
	}
	
}  //  @jve:decl-index=0:visual-constraint="7,7"