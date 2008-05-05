/**
 * 
 */
package org.redcross.sar.gui.map; 

import java.util.Arrays;
import java.awt.GridBagLayout;

import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.GridBagConstraints;

import javax.swing.BoxLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.text.MaskFormatter;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.NumPadDialog;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.util.mso.Position;

import com.esri.arcgis.geometry.Point;

import java.awt.BorderLayout;

/**
 * @author kennetgu
 *
 */
public class PositionField extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private final MGRSFormat m_mgrsFormatE = new MGRSFormat("E");
	private final MGRSFormat m_mgrsFormatN = new MGRSFormat("N");
	private final UTMFormat m_utmFormatE = new UTMFormat("E");
	private final UTMFormat m_utmFormatN = new UTMFormat("N");
	private final DESFormat m_desFormatE = new DESFormat("E");
	private final DESFormat m_desFormatN = new DESFormat("N");
	private final DEGFormat m_degFormatE = new DEGFormat("E");
	private final DEGFormat m_degFormatN = new DEGFormat("N");
	
	private JPanel m_right = null;
	private JLabel m_latitudeLabel = null;
	private JFormattedTextField m_latitudeText = null;
	private JLabel m_longetudeLabel = null;
	private JPanel m_left = null;
	private JFormattedTextField m_longetudeText = null;
	private JLabel m_zoneLabel = null;
	private JLabel m_squareLabel = null;
	private IDiskoApplication m_app = null;
	
	private int m_format = 1; // MGRS
	
	private String[] zones = {"32V","33V","32W","33W","34W","35W","36W"};  //  @jve:decl-index=0:
	private String[] squares = {"KR","KQ","KP","KN","KM","KL",
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
	private JComboBox m_ZoneCombo = null;
	private JComboBox m_SquareCombo = null;
	private JPanel m_positionPanel = null;
	
	/**
	 * Constructor
	 */
	public PositionField() {
		super();
		m_app = Utils.getApp();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		// setup content pane
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		Utils.setFixedSize(this, 250, 55);
		// add components
		this.add(getPositionPanel());
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
	
	/**
	 * This method set mgrs from text
	 * 
	 * @return void
	 */
	public void setText(String text) {
		boolean bEmpty = false;
		try {
			// is not null?
			if (text != null) {
				// is empty?
				if (text.length() > 0) { 
					switch(getFormat()) {
					case 1: // MGRS
						String zone = text.subSequence(0, 3).toString();
						String square = text.subSequence(3, 5).toString();
						String x = text.subSequence(5, 10).toString();
						String y = text.subSequence(10, 15).toString();
						getZoneCombo().setSelectedItem(zone);
						getSquareCombo().setSelectedItem(square);
						getLatitudeText().setText(x);
						getLongetudeText().setText(y);
						break;
					case 2: // UTM
						zone = text.subSequence(0, 3).toString();
						x = text.subSequence(3, 10).toString();
						y = text.subSequence(11, 18).toString();
						getZoneCombo().setSelectedItem(zone);
						getLatitudeText().setText(x);
						getLongetudeText().setText(y);
						break;					
					case 3: // DES
						String[] split = text.split("E");
						getLatitudeText().setText(split[0]);
						getLongetudeText().setText(split[1]);
						break;					
					case 4: // DEG
						split = text.split("E");
						getLatitudeText().setText(split[0]);
						getLongetudeText().setText(split[1]);
						break;					
					}
				}
				else 
					bEmpty = true;
			}
			else
				bEmpty = true;
		}
		catch(Exception e) {
			// invalid format
			bEmpty = true;
		}
		
		if (bEmpty) {
			getZoneCombo().setSelectedIndex(0);
			getSquareCombo().setSelectedIndex(0);
			getLatitudeText().setText("");
			getLongetudeText().setText("");
		}			
	}
	
	/**
	 * This method get mgrs text
	 * 
	 * @return String
	 */
	public String getText() {
		String text = null;
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
		case 4: // DEG
			text = getLatitudeText().getText() + getLongetudeText().getText();
			break;	
		}
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
			Dimension dim = new Dimension(150, 45);
			m_left.setPreferredSize(dim);
			m_left.setMinimumSize(dim);
			m_left.setMaximumSize(dim);
			m_left.setLayout(new GridBagLayout());
			// create labels
			m_zoneLabel = new JLabel();
			m_zoneLabel.setText("Sone");
			m_squareLabel = new JLabel();
			m_squareLabel.setText("100-km rute");
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
			c3.insets = new Insets(0, 0, 0, 5);
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
			Dimension dim = new Dimension(100, 45);
			m_right.setPreferredSize(dim);
			m_right.setPreferredSize(dim);
			m_right.setPreferredSize(dim);
			m_right.setLayout(new GridBagLayout());
			// create labels
			m_latitudeLabel = new JLabel();
			m_latitudeLabel.setText("X");
			m_longetudeLabel = new JLabel();
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
			m_latitudeText = new JFormattedTextField();
			m_latitudeText.setPreferredSize(new Dimension(4, 20));
			m_latitudeText.setText("");
			m_latitudeText.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {					
					if (e.getClickCount() == 2){
						NumPadDialog numPadDialog = m_app.getUIFactory().getNumPadDialog();
						java.awt.Point p = m_latitudeText.getLocationOnScreen();
						p.setLocation(p.x + (m_latitudeText.getWidth()+7), p.y);
						numPadDialog.setLocation(p);					
						numPadDialog.setTextField(m_latitudeText);
						numPadDialog.setVisible(true);	
					}
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
			m_longetudeText = new JFormattedTextField();
			m_longetudeText.setPreferredSize(new Dimension(4, 20));
			m_longetudeText.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {					
					if (e.getClickCount() == 2){
						NumPadDialog numPadDialog = m_app.getUIFactory().getNumPadDialog();
						java.awt.Point p = m_longetudeText.getLocationOnScreen();
						p.setLocation(p.x + (m_longetudeText.getWidth()+7), p.y);
						numPadDialog.setLocation(p);					
						numPadDialog.setTextField(m_longetudeText);
						numPadDialog.setVisible(true);	
					}
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
			m_ZoneCombo.setPreferredSize(new Dimension(60, 20));
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
			m_SquareCombo.setPreferredSize(new Dimension(60, 20));
		}
		return m_SquareCombo;
	}

	/**
	 * This method initializes m_positionPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPositionPanel() {
		if (m_positionPanel == null) {
			m_positionPanel = new JPanel();
			FlowLayout fl = new FlowLayout();
			fl.setAlignment(FlowLayout.LEFT);
			fl.setHgap(0);
			fl.setVgap(0);
			m_positionPanel.setLayout(fl);
			// add components
			m_positionPanel.add(getLeft(), BorderLayout.WEST);
			m_positionPanel.add(getRight(), BorderLayout.EAST);
		}
		return m_positionPanel;
	}
	
	public int getFormat() {
		return m_format;
	}
	
	public void setFormat(int format) {
		// get current
		Position p = getPosition();
		// reset
		setText(null);
		// save format
		m_format = format;
		// get formaters
		switch(format) {
		case 1: // MGRS
			m_SquareCombo.setVisible(true);
			m_squareLabel.setVisible(true);
			m_latitudeText.setFormatterFactory(m_mgrsFormatE);
			m_longetudeText.setFormatterFactory(m_mgrsFormatN);
			break;
		case 2: // UTM
			m_SquareCombo.setVisible(false);
			m_squareLabel.setVisible(false);
			m_latitudeText.setFormatterFactory(m_utmFormatE);
			m_longetudeText.setFormatterFactory(m_utmFormatN);
			break;
		case 3: // DES
			m_SquareCombo.setVisible(false);
			m_squareLabel.setVisible(false);
			m_latitudeText.setFormatterFactory(m_desFormatE);
			m_longetudeText.setFormatterFactory(m_desFormatN);
			break;
		case 4: // DEG
			m_SquareCombo.setVisible(false);
			m_squareLabel.setVisible(false);
			m_latitudeText.setFormatterFactory(m_degFormatE);
			m_longetudeText.setFormatterFactory(m_degFormatN);
			break;
		}
		// foreward
		setPosition(p);
	}

	public Point getPoint() {
		try {
			switch(getFormat()) {
			case 1: // MGRS
				return MapUtil.getPointFromMGRS(getText());
			case 2: // UTM
				return MapUtil.getPointFromUTM(getText());
			case 3: // DES
				return MapUtil.getPointFromDES(getText());
			case 4: // DEG
				return MapUtil.getPointFromDEG(getText());
			}
		}
		catch (Exception e) {
			// invalid format
		}
		return null;
	}
	
	public Position getPosition() {
		try {
			switch(getFormat()) {
			case 1: // MGRS
				return MapUtil.getPositionFromMGRS(getText());
			case 2: // UTM
				return MapUtil.getPositionFromUTM(getText());
			case 3: // DES
				return MapUtil.getPositionFromDES(getText());
			case 4: // DEG
				return MapUtil.getPositionFromDEG(getText());
			}
		}
		catch (Exception e) {
			// invalid format
		}
		return null;
	}
		
	public void setPosition(Position p) {
		try {
			switch(getFormat()) {
			case 1: // MGRS
				setText(MapUtil.getMGRSfromPosition(p));
				break;
			case 2: // UTM
				setText(MapUtil.getUTMfromPosition(p));
				break;
			case 3: // DES
				setText(MapUtil.getDESfromPosition(p));
				break;
			case 4: // DEG
				setText(MapUtil.getDEGfromPosition(p));
				break;
			}
		}
		catch (Exception e) {
			// invalid format
		}
	}	
	
	public void setPoint(Point p) {
		try {
			switch(getFormat()) {
			case 1: // MGRS
				setText(MapUtil.getMGRSfromPoint(p));
				break;
			case 2: // UTM
				setText(MapUtil.getUTMfromPoint(p));
				break;
			case 3: // DES
				setText(MapUtil.getDESfromPoint(p));
				break;
			case 4: // DEG
				setText(MapUtil.getDEGfromPoint(p));
				break;
			}
		}
		catch (Exception e) {
			// invalid format
		}
	}
	
	class MGRSFormat extends JFormattedTextField.AbstractFormatterFactory {

		MaskFormatter m_mf = null;
		
		MGRSFormat(String direction) {
			try {
				m_mf = new MaskFormatter("#####"+direction);
				m_mf.setPlaceholder("00000"+direction);
				m_mf.setPlaceholderCharacter('0');
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public AbstractFormatter getFormatter(JFormattedTextField arg0) {
			return m_mf;
		}
		
	}

	class UTMFormat extends JFormattedTextField.AbstractFormatterFactory {

		MaskFormatter m_mf = null;
		
		UTMFormat(String direction) {
			try {
				m_mf = new MaskFormatter("#######"+direction);
				m_mf.setPlaceholder("0000000"+direction);
				m_mf.setPlaceholderCharacter('0');
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public AbstractFormatter getFormatter(JFormattedTextField arg0) {
			return m_mf;
		}
		
	}

	class DEGFormat extends JFormattedTextField.AbstractFormatterFactory {

		MaskFormatter m_mf = null;
		
		DEGFormat(String direction) {
			try {
				String mask = "##"+Character.toString((char)186)+
					"##"+Character.toString((char)39)+
					"##"+Character.toString((char)34)+ direction;
				m_mf = new MaskFormatter(mask);
				m_mf.setPlaceholder(mask.replace("#", "0"));
				m_mf.setPlaceholderCharacter('0');
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public AbstractFormatter getFormatter(JFormattedTextField arg0) {
			return m_mf;
		}
		
	}
	
	class DESFormat extends JFormattedTextField.AbstractFormatterFactory {

		MaskFormatter m_mf = null;
		
		DESFormat(String direction) {
			try {
				String mask = "##.";
				for(int i=0;i<4;i++) {
					mask = mask.concat("#");
				}
				mask = mask.concat(direction);
				m_mf = new MaskFormatter(mask);
				m_mf.setPlaceholder(mask.replace("#", "0"));
				m_mf.setPlaceholderCharacter('0');
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public AbstractFormatter getFormatter(JFormattedTextField arg0) {
			return m_mf;
		}
		
	}
	
}  //  @jve:decl-index=0:visual-constraint="7,7"
