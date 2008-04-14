package org.redcross.sar.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import org.redcross.sar.app.Utils;

public class DiskoPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private JPanel m_titlePanel = null;
	private JLabel m_iconLabel = null;
	private JLabel m_captionLabel = null;
	private DiskoButtons m_buttons = null;
	private JScrollPane m_scrollPane = null;

	private Component m_content = null;
	
	private Map<String,ActionEvent> m_actions = null;
	private ArrayList<ActionListener> m_listeners = null;
	
	public DiskoPanel() {
		this("");
	}
	
	public DiskoPanel(String caption) {
		// prepare
		m_actions = new HashMap<String, ActionEvent>();
		m_listeners = new ArrayList<ActionListener>();
		// initialize GUI
		initialize();
		// set caption
		setCaptionText(caption);
		// set caption heigth
		setCaptionHeight();
	}
	
	/**
	 * This method initializes the panel
	 * 	
	 */
	private void initialize() {
		this.setLayout(new BorderLayout());		
		this.setBorder(new CustomBorder(1,1,1,1,Color.GRAY));
		this.add(getTitlePanel(), BorderLayout.NORTH);
		this.add(getScrollPane(), BorderLayout.CENTER);
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				// forward
				super.componentResized(e);
				// forward
				setFixedSize();	
		    }
			@Override
			public void componentShown(ComponentEvent e) {
				// forward
				super.componentShown(e);
				// forward
				setFixedSize();	
			}			
		});		

	}
	
	/**
	 * This method initializes titlePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getTitlePanel() {
		if (m_titlePanel == null) {
			try {
				FlowLayout fl = new FlowLayout();
				fl.setAlignment(FlowLayout.LEFT);
				fl.setHgap(5);
				fl.setVgap(0);
				JPanel labels = new JPanel();
				labels.setLayout(fl);
				labels.add(getIconLabel(),null);
				labels.add(getCaptionLabel(),null);
				m_titlePanel = new JPanel();
				m_titlePanel.setBorder(new CustomBorder(0,0,0,1,Color.GRAY));
				m_titlePanel.setLayout(new BorderLayout());
				m_titlePanel.add(labels,BorderLayout.CENTER);
				m_titlePanel.add(getButtons(),BorderLayout.EAST);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return m_titlePanel;
	}
	
	/**
	 * This method gets the caption icon
	 * 
	 * @return Icon
	 */
	public Icon getCaptionIcon() {
		return getIconLabel().getIcon();
	}	
	
	/**
	 * This method sets the caption icon
	 *
	 */
	public void setCaptionIcon(Icon icon) {
		getIconLabel().setIcon(icon);
		getIconLabel().setVisible(icon!=null);
		setCaptionHeight();
	}	
	
	private void setCaptionHeight() {
		Icon icon = getCaptionIcon();
		int	w = icon!=null ? 
				Math.max(getWidth()-getButtons().getWidth()-icon.getIconWidth()-50,25) : 
				Math.max(getWidth()-getButtons().getWidth(),25);
		Utils.setFixedSize(getCaptionLabel(),w,25);
	}

	
	/**
	 * This method gets the caption text
	 *
	 * @return String
	 */	
	public String getCaptionText() {
		return getCaptionLabel().getText();
	}
	
	/**
	 * This method sets the caption text
	 *
	 */
	public void setCaptionText(String caption) {
		getCaptionLabel().setText(caption);
		setCaptionVisible();
	}	
	
	private void setCaptionVisible() {
		boolean isVisible = getCaptionIcon() !=null || 
			getCaptionText() !=null && getCaptionText().length()>0;
			
		getIconLabel().setVisible(isVisible);
		getCaptionLabel().setVisible(isVisible);
	}
	
	/**
	 * This method sets the border color
	 *
	 */
	public void setBorderColor(Color color) {
		this.setBorder(new CustomBorder(1,1,1,1,color));
		getTitlePanel().setBorder(new CustomBorder(0,0,0,1,color));
	}	
	
	/**
	 * This method sets the border color
	 *
	 */
	public void setTitleColor(Color background, Color foreground) {
		getIconLabel().setBackground(background);
		getIconLabel().setForeground(foreground);
		getCaptionLabel().setBackground(background);
		getCaptionLabel().setForeground(background);
		getTitlePanel().setBackground(background);
		getTitlePanel().setForeground(foreground);
	}	
		
	/**
	 * This method initializes iconLabel
	 *
	 * @return javax.swing.JLabel
	 */
	private JLabel getIconLabel() {
		if (m_iconLabel == null) {
			m_iconLabel = new JLabel();
			m_iconLabel.setOpaque(true);
			m_iconLabel.setVisible(false);
			m_iconLabel.setVerticalAlignment(SwingConstants.CENTER);
		}
		return m_iconLabel;
	}
	
	private JLabel getCaptionLabel() {
		if(m_captionLabel==null) {
			m_captionLabel = new JLabel("");
			m_captionLabel.setOpaque(true);
			m_captionLabel.setVerticalAlignment(SwingConstants.CENTER);
		}
		return m_captionLabel;
	}
		
	/**
	 * This method initializes cancelButton
	 *
	 * @return javax.swing.JButton
	 */
	public DiskoButtons getButtons() {
		if (m_buttons == null) {
			try {
				FlowLayout fl = new FlowLayout();
				fl.setHgap(5);
				fl.setVgap(5);
				fl.setAlignment(FlowLayout.RIGHT);
				m_buttons = new DiskoButtons(FlowLayout.RIGHT);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return m_buttons;
	}	
	
	private JScrollPane getScrollPane() {
		if(m_scrollPane==null) {
			m_scrollPane = new JScrollPane();
			m_scrollPane.setBorder(null);
			m_scrollPane.setOpaque(true);
		}
		return m_scrollPane;
	}
	
	public boolean isScrollBarVisible(String scrollbar) {
		if(JScrollPane.HORIZONTAL_SCROLLBAR.equalsIgnoreCase(scrollbar)) {
			return getScrollPane().getHorizontalScrollBar().isVisible();
		}
		if(JScrollPane.VERTICAL_SCROLLBAR.equalsIgnoreCase(scrollbar)) {
			return getScrollPane().getVerticalScrollBar().isVisible();
		}
		return false;
	}
	
	public void setScrollBarPolicies(int vert, int horz) {
		getScrollPane().setVerticalScrollBarPolicy(vert);
		getScrollPane().setHorizontalScrollBarPolicy(horz);
	}
	
	public void setFixedSize() {
		setCaptionHeight();
	}
	
	public Object finish() { return false; }
	
	public void cancel() {}
	
	public Component getContent() {
		return m_content;
	}
	
	public void setContent(Component content) {
		// update viewport
		getScrollPane().setViewportView(content);
		// update hool
		m_content = content;
	}
	
	public AbstractButton addButton(AbstractButton button, String command) {
		return getButtons().addButton(button,command);
	}
	
	public AbstractButton addButton(String command, String caption) {
		return getButtons().addButton(command, caption);
	}
	
	public void removeButton(String command) {
		getButtons().removeButton(command);
	}
	
	public boolean containsButton(String command) {
		return getButtons().containsButton(command);
	}
	
	public AbstractButton getButton(String command) {
		return getButtons().getButton(command);
	}
	
	public boolean isButtonVisible(String command) {
		return getButtons().isButtonVisible(command);
	}
	
	public void setButtonVisible(String command, boolean isVisible) {
		getButtons().setButtonVisible(command,isVisible);
	}
	
	public boolean isButtonEnabled(String command) {
		return getButtons().isButtonEnabled(command);
	}
	
	public void setButtonEnabled(String command, boolean isEnabled) {
		getButtons().setButtonEnabled(command,isEnabled);
	}
	
	public void addAction(String command) {
		m_actions.put(command,new ActionEvent(this,ActionEvent.ACTION_PERFORMED,command));
	}
	
	public void removeAction(String command) {
		m_actions.remove(command);
	}
	
	public void addActionListener(ActionListener listener) {
		m_listeners.add(listener);
		getButtons().addActionListener(listener);
	}
	
	public void removeActionListener(ActionListener listener) {
		m_listeners.remove(listener);
		getButtons().removeActionListener(listener);
	}
	
	public boolean doAction(String command) {
		if(!getButtons().doAction(command)) {
			if(m_actions.containsKey(command)) {
				ActionEvent e = m_actions.get(command);
				for(ActionListener it: m_listeners) {
					it.actionPerformed(e);
				}
				return true;
			}
			return false;
		}
		return true;
	}
	
	private class CustomBorder implements Border {

		  private int m_top;  
		  private int m_left;   
		  private int m_bottom;   
		  private int m_right;  
		  private Color m_color = null;   

		  public CustomBorder() {  
			  this(1,1,1,1,Color.GRAY);   
		  }   
		  
		  public CustomBorder(int left, int top, int right, int bottom, Color color) {  
		      this.m_top = top;   
		      this.m_left = left;   
		      this.m_bottom = bottom;   
		      this.m_right = right;   
		      this.m_color = color;   
		  }   

		  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {


			  // get insets
		      Insets insets = getBorderInsets(c);
		      
		      // get old color
		      Color old = g.getColor();
		      
		      // apply color
		      if (m_color != null)  
		          g.setColor(m_color);  
		      
		      // draw borders
		      g.fill3DRect(0, 0, width-insets.right, insets.top, true); 
		      g.fill3DRect(0, insets.top, insets.left, height-insets.top, true);   
		      g.fill3DRect(insets.left, height-insets.bottom, width-insets.left, insets.bottom, true);   
		      g.fill3DRect(width-insets.right, 0, insets.right, height-insets.bottom+1, true);
		      
		      // restore state
		      g.setColor(old);	      
		  }   

		  public Insets getBorderInsets(Component c) {  
		      return new Insets(m_top, m_left, m_bottom, m_right);  
		  }   

		  public boolean isBorderOpaque() {   
		      return true;  
		  }   
	}	
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
