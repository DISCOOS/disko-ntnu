package org.redcross.sar.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JToggleButton;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.renderers.IconRenderer;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.ICommunicatorIf;
import org.redcross.sar.mso.data.IUnitIf;

/**
 * Creates buttons
 * 
 * @author thomasl
 */
public class DiskoButtonFactory
{
	public final static Dimension SMALL_BUTTON_SIZE = new Dimension(35, 35);
	public final static Dimension NORMAL_BUTTON_SIZE = new Dimension(50, 50);
	public final static Dimension LONG_BUTTON_SIZE = new Dimension(100, 50);
	
	private static Properties m_properties = null;
	private final static ResourceBundle m_unitResources = ResourceBundle.getBundle("org.redcross.sar.mso.data.properties.Unit");
	
	private final static Font BUTTON_FONT = new Font("DiskoButtonFactoryFont", Font.PLAIN, 12);
	
	public enum ButtonType
	{
		CancelButton,
		OkButton,
		FinishedButton,
		DeleteButton,
		BackButton,
		NextButton
	};
	
	private static Properties getProperties()
	{
		if(m_properties == null)
		{
			try
			{
				m_properties = Utils.loadProperties("properties");
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return m_properties;
	}
	
	/**
	 * Creates a normal JToggleButton based on the communicator
	 */
	public static JToggleButton createNormalToggleButton(ICommunicatorIf communicator)
	{
		JToggleButton button = createNormalToggleButton();
		
		if(communicator instanceof ICmdPostIf)
		{
			try
			{
				button.setIcon(Utils.createImageIcon(m_unitResources.getString("UnitType.CP.icon"), ""));
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else if(communicator instanceof IUnitIf)
		{
			String unitType = ((IUnitIf)communicator).getType().name();
			try
			{
				button.setIcon(Utils.createImageIcon(m_unitResources.getString("UnitType."+unitType+".icon"), ""));
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return button;
	}
	
	/**
	 * Creates a long JToggleButton based on the communicator
	 */
	public static JToggleButton createLongToggleButton(ICommunicatorIf communicator)
	{
		JToggleButton button = createLongToggleButton();
		
		if(communicator instanceof ICmdPostIf)
		{
			try
			{
				button.setIcon(Utils.createImageIcon(m_unitResources.getString("UnitType.CP.icon"), ""));
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else if(communicator instanceof IUnitIf)
		{
			String unitType = ((IUnitIf)communicator).getType().name();
			try
			{
				button.setIcon(Utils.createImageIcon(m_unitResources.getString("UnitType."+unitType+".icon"), ""));
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		button.setText(communicator.getCommunicatorNumber() + "  " + communicator.getCallSign());
		button.setToolTipText(button.getText());
		
		button.setBorder(null);
		
		return button;
	}
	
	public static JButton createNormalButton()
	{
		JButton button = new JButton();
		
		button.setFont(BUTTON_FONT);
		
		button.setMinimumSize(NORMAL_BUTTON_SIZE);
		button.setPreferredSize(NORMAL_BUTTON_SIZE);
		button.setMaximumSize(NORMAL_BUTTON_SIZE);
		
		button.setBorder(null);
		
		return button;
	}

	public static JButton createNormalButton(String text)
	{
		JButton button = createNormalButton();
		
		if(!text.isEmpty())
		{
			button.setText(text);
		}
		button.setToolTipText(text);
		
		return button;
	}
	
	public static JButton createNormalButton(String name, String iconPath)
	{
		JButton button = createNormalButton();
		if(name.equals(""))
		{
			try
			{
				button.setIcon(Utils.createImageIcon(iconPath, name));
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			button.setText(name);
		}
		button.setToolTipText(name);
		
		return button;
	}
	
	public static JButton createNormalButton(ButtonType type)
	{
		JButton button = createNormalButton(getProperties().getProperty(type.name() + ".text"), 
				getProperties().getProperty(type.name() + ".icon"));
		return button;
	}

	public static JToggleButton createNormalToggleButton(String text)
	{
		JToggleButton button = createNormalToggleButton();
		
		if(!text.isEmpty())
		{
			//button.setText(name);
		}
		button.setToolTipText(text);
		
		return button;
	}
	
	public static JToggleButton createNormalToggleButton(String name, String iconPath)
	{
		JToggleButton button = createNormalToggleButton();
		
		try
		{
			button.setIcon(Utils.createImageIcon(iconPath, name));
		} 
		catch (Exception e)
		{
			button.setText(name);
		}
		button.setToolTipText(name);
		
		return button;
	}

	public static JButton createAssignmentButton(IAssignmentIf assignment)
	{
		JButton button = new JButton();
		
		button.setMinimumSize(LONG_BUTTON_SIZE);
		button.setPreferredSize(LONG_BUTTON_SIZE);
		button.setMaximumSize(LONG_BUTTON_SIZE);
		
		IconRenderer.AssignmentIcon icon = new IconRenderer.AssignmentIcon(assignment, false, null);
		button.setIcon(icon);
		
		return button;
	}

	public static JToggleButton createLongToggleButton(IAssignmentIf assignment)
	{
		JToggleButton button = createLongToggleButton();

		button.setToolTipText(assignment.getTypeText()+ " " + assignment.getNumber());


		return button;
	}

	public static JToggleButton createNormalAssignmentToggleButton(IAssignmentIf assignment)
	{
		JToggleButton button = createNormalToggleButton();
		
		IconRenderer.AssignmentIcon icon = new IconRenderer.AssignmentIcon(assignment, false, null);
		button.setIcon(icon);
		
		return button;
	}

	public static JToggleButton createLongToggleButton(String text) {
		return createLongToggleButton(text,0,0);
	}
	
	public static JToggleButton createLongToggleButton(String text, int dx,int dy)
	{
		JToggleButton button = createLongToggleButton(dx,dy);
		
		button.setText(text);
		button.setToolTipText(text);

		return button;
	}
	
	public static JToggleButton createLongToggleButton() {
		return createLongToggleButton(0,0);
	}
	
	public static JToggleButton createLongToggleButton(int dx,int dy)
	{
		JToggleButton button = new JToggleButton();
		
		button.setFont(BUTTON_FONT);
		
		Dimension dim = new Dimension(LONG_BUTTON_SIZE);
		dim.setSize(dim.getWidth()+dx, dim.getHeight()+dy);
		
		button.setMinimumSize(dim);
		button.setPreferredSize(dim);
		button.setMaximumSize(dim);
		
		return button;
	}

	public static JButton createSmallButton(String text)
	{
		JButton button = new JButton();
		button.setText(text);
		button.setToolTipText(text);
		button.setFont(BUTTON_FONT);
		button.setBorder(null);
		
		button.setMinimumSize(SMALL_BUTTON_SIZE);
		button.setPreferredSize(SMALL_BUTTON_SIZE);
		button.setMaximumSize(SMALL_BUTTON_SIZE);
		
		button.setFocusable(false);
		
		return button;
	}

	public static JToggleButton createNormalToggleButton()
	{
		JToggleButton button = new JToggleButton();
		
		button.setFont(BUTTON_FONT);

		button.setMinimumSize(NORMAL_BUTTON_SIZE);
		button.setPreferredSize(NORMAL_BUTTON_SIZE);
		button.setMaximumSize(NORMAL_BUTTON_SIZE);
		
		button.setBorder(null);

		return button;
	}
}
