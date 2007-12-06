/**
 * 
 */
package org.redcross.sar.gui; 

import java.util.Arrays;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;

import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.GridBagConstraints;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.text.MaskFormatter;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.app.Utils;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.util.mso.Position;

import com.esri.arcgis.geometry.Point;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author kennetgu
 *
 */
public class POIField extends JPanel {

	private static final long serialVersionUID = 1L;
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
	private String[] squares = {"KQ","KP","KN","KM","KL",
								"LQ","LP","LN","LM","LL","LK",
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
	private JPanel m_formatPanel = null;
	private JPanel m_positionPanel = null;
	private JToggleButton m_MGRSButton = null;
	private JToggleButton m_UTMButton = null;
	private ButtonGroup m_buttonGroup = null;
	
	/**
	 * Constructor
	 */
	public POIField(IDiskoApplication app) {
		super();
		m_app = app;
		initialize();
	}

	/**
	 * Constructor
	 * 
	 * @param arg0
	 */
	public POIField(IDiskoApplication app, LayoutManager arg0) {
		super(arg0);
		m_app = app;
		initialize();
	}

	/**
	 * @param arg0
	 */
	public POIField(IDiskoApplication app, boolean arg0) {
		super(arg0);
		m_app = app;
		initialize();
	}

	/**
	 * Constructor
	 * 
	 * @param arg0
	 * @param arg1
	 */
	public POIField(IDiskoApplication app, LayoutManager arg0, boolean arg1) {
		super(arg0, arg1);
		m_app = app;
		initialize();
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
					switch(m_format) {
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
			getLatitudeText().setText(null);
			getLongetudeText().setText(null);
		}			
	}
	
	/**
	 * This method get mgrs text
	 * 
	 * @return String
	 */
	public String getText() {
		String text = null;
		switch(m_format) {
		case 1:
			text = (getZoneCombo().getSelectedItem()==null ? "" : 
					getZoneCombo().getSelectedItem().toString()) +
					(getSquareCombo().getSelectedItem()==null ? "" : 
						getSquareCombo().getSelectedItem().toString()) +
				getLatitudeText().getText() + getLongetudeText().getText();
			break;	
		case 2:
			text = (getZoneCombo().getSelectedItem()==null ? "" : 
				getZoneCombo().getSelectedItem().toString()) +
				getLatitudeText().getText() + getLongetudeText().getText();
			break;	
		}
		return text;
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		// setup content pane
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		Dimension dim = new Dimension(250, 120);
		this.setSize(dim);
		this.setPreferredSize(dim);
		this.setMinimumSize(dim);
		this.setMaximumSize(dim);
		// add components
		this.add(getPositionPanel());
		this.add(new JSeparator(JSeparator.HORIZONTAL));
		this.add(getFormatPanel());
		setFormat(m_format); // MGRS format
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
	 * This method initializes m_formatPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getFormatPanel() {
		if (m_formatPanel == null) {
			FlowLayout fl = new FlowLayout();
			fl.setAlignment(FlowLayout.LEFT);
			fl.setHgap(0);
			fl.setVgap(0);
			m_formatPanel = new JPanel();
			m_formatPanel.setLayout(fl);
			JLabel label = new JLabel("Posisjonsformat");
			label.setPreferredSize(new Dimension(100,20));
			m_formatPanel.add(label,null);
			m_formatPanel.add(getMGRSButton(), null);
			m_formatPanel.add(getUTMButton(), null);
			m_buttonGroup = new ButtonGroup();
			m_buttonGroup.add(getMGRSButton());
			m_buttonGroup.add(getUTMButton());
		}
		return m_formatPanel;
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

	/**
	 * This method initializes m_MGRSButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JToggleButton getMGRSButton() {
		if (m_MGRSButton == null) {
			m_MGRSButton = new JToggleButton();
			Dimension dim = Utils.getApp().getUIFactory().getSmallButtonSize();
			m_MGRSButton.setPreferredSize(dim);
			m_MGRSButton.setIcon(Utils.getIcon("IconEnum.MGRS.icon"));
			m_MGRSButton.setToolTipText(Utils.getProperty("IconEnum.MGRS.text"));
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
			m_UTMButton = new JToggleButton();
			Dimension dim = Utils.getApp().getUIFactory().getSmallButtonSize();
			m_UTMButton.setPreferredSize(dim);
			m_UTMButton.setIcon(Utils.getIcon("IconEnum.UTM.icon"));
			m_UTMButton.setToolTipText(Utils.getProperty("IconEnum.UTM.text"));
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
			m_latitudeText.setFormatterFactory(new MGRSFormat());
			m_longetudeText.setFormatterFactory(new MGRSFormat());
			if (!m_MGRSButton.isSelected())
				m_MGRSButton.setSelected(true);
			break;
		case 2: // UTM
			m_SquareCombo.setVisible(false);
			m_squareLabel.setVisible(false);
			m_latitudeText.setFormatterFactory(new UTMFormat("N"));
			m_longetudeText.setFormatterFactory(new UTMFormat("E"));
			if (!m_UTMButton.isSelected())
				m_UTMButton.setSelected(true);
			break;
		}
		setPosition(p);
	}

	public Point getPoint() {
		try {
			switch(m_format) {
			case 1: // MGRS
				return MapUtil.getPointFromMGRS(getText());
			case 2: // UTM
				return MapUtil.getPointFromUTM(getText());
			}
		}
		catch (Exception e) {
			// invalid format
		}
		return null;
	}
	
	public Position getPosition() {
		try {
			switch(m_format) {
			case 1: // MGRS
				return MapUtil.getPositionFromMGRS(getText());
			case 2: // UTM
				return MapUtil.getPositionFromUTM(getText());
			}
		}
		catch (Exception e) {
			// invalid format
		}
		return null;
	}
		
	public void setPosition(Position p) {
		try {
			switch(m_format) {
			case 1: // MGRS
				setText(MapUtil.getMGRSfromPosition(p));
				break;
			case 2: // UTM
				setText(MapUtil.getUTMfromPosition(p));
				break;
			}
		}
		catch (Exception e) {
			// invalid format
		}
	}	
	
	public void setPoint(Point p) {
		try {
			switch(m_format) {
			case 1: // MGRS
				setText(MapUtil.getMGRSfromPoint(p));
				break;
			case 2: // UTM
				setText(MapUtil.getUTMfromPoint(p));
				break;
			}
		}
		catch (Exception e) {
			// invalid format
		}
	}
	
	class MGRSFormat extends JFormattedTextField.AbstractFormatterFactory {

		@Override
		public AbstractFormatter getFormatter(JFormattedTextField arg0) {
			MaskFormatter mf1 = null;
			try {
				mf1 = new MaskFormatter("#####");
				mf1.setPlaceholder("00000");
				mf1.setPlaceholderCharacter('0');
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return mf1;
		}
		
	}

	class UTMFormat extends JFormattedTextField.AbstractFormatterFactory {

		String m_direction = "E";
		
		UTMFormat(String direction) {
			m_direction = direction;
		}
		
		@Override
		public AbstractFormatter getFormatter(JFormattedTextField arg0) {
			MaskFormatter mf1 = null;
			try {
				mf1 = new MaskFormatter("#######"+m_direction);
				mf1.setPlaceholder("0000000"+m_direction);
				mf1.setPlaceholderCharacter('0');
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return mf1;
		}
		
	}
	
}  //  @jve:decl-index=0:visual-constraint="7,7"
