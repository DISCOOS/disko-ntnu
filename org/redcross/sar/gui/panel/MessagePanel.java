
package org.redcross.sar.gui.panel;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;

import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.util.Utils;

public class MessagePanel extends DefaultPanel {

	private static final long serialVersionUID = 1L;

	private JEditorPane m_msgPane;

	private int m_maxWidth = 1024;
    private boolean m_isAutoFit = true;
    private boolean m_isJustified = false;
    private Insets m_margin = new Insets(5,5,5,5);

	public MessagePanel() {
		// forward
		super();
		// initialize GUI
		initialize();
	}

	/**
	 * Initialize this
	 */
	private void initialize() {
		// set table
		setBodyComponent(getMessagePane());
	}

	/**
	 * Initialize the message pane
	 */
	private JEditorPane getMessagePane() {
		if(m_msgPane == null) {
			m_msgPane = new JEditorPane();
			m_msgPane.setFont(UIFactory.DEFAULT_PLAIN_MEDIUM_FONT);
			m_msgPane.setContentType("text/html");
			m_msgPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			m_msgPane.setEditable(false);
			m_msgPane.setOpaque(false);
		}
		return m_msgPane;
	}

	public String getMessage() {
		return getMessagePane().getText();
	}

	public void setMessage(String msg) {
		getMessagePane().setText("<html>"+Utils.trimHtml(msg)+"</html>");
		calcSize();
	}

	public int getMaxWidth()
    {
        return m_maxWidth;
    }

    public void setMaxWidth(int width)
    {
        if (m_maxWidth <= 0) throw new IllegalArgumentException();
        m_maxWidth = width;
        calcSize();
    }

    public boolean isJustified()
    {
        return m_isJustified;
    }

    public void setJustified(boolean isJustified)
    {
        m_isJustified = isJustified;
        calcSize();
    }

    public boolean isAutoFit()
    {
        return m_isAutoFit;
    }

    public void setAutoFit(boolean isAutoFit)
    {
    	m_isAutoFit = isAutoFit;
    	calcSize();
    }

    private void calcSize()
    {
    	if(m_isAutoFit)
    	{
			Graphics2D g2d = (Graphics2D)getMessagePane().getGraphics();
			if(g2d!=null) {
				Dimension size = calcSize(g2d, m_maxWidth);
				getMessagePane().setPreferredSize(size);
				if(getManager()!=null) {
					int h = size.height + getHeaderPanel().getHeight()+10;
					getManager().requestResize(size.width,(h > 300 ? 300 : h), false);
				}

			}
    	}
    }

	private Dimension calcSize(Graphics2D g, int width)
    {
		String text = Utils.stripHtml(getMessage());
        Insets insets = getInsets();
        width -= insets.left + insets.right + m_margin.left + m_margin.right;
        float w = insets.left + insets.right + m_margin.left + m_margin.right;
        float x = insets.left + m_margin.left, y=insets.top + m_margin.top;

        if (width > 0 && text != null && text.length() > 0)
        {
              AttributedString as = new AttributedString(text);
              as.addAttribute(TextAttribute.FONT, g.getFont());
              AttributedCharacterIterator aci = as.getIterator();
              FontRenderContext frc = g.getFontRenderContext();
              LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc);
              float max = 0;
              while (lbm.getPosition() < aci.getEndIndex())
              {
                    TextLayout textLayout = lbm.nextLayout(m_maxWidth);
                    if (g != null)
                        textLayout.draw(g, x, y + textLayout.getAscent());
                    y += textLayout.getDescent() + textLayout.getLeading() + textLayout.getAscent();
                    max = Math.max(max, textLayout.getVisibleAdvance());
              }
              w += max;
        }

        return new Dimension((int)Math.ceil(w), (int)Math.ceil(y) + insets.bottom + m_margin.bottom);

    }

}
