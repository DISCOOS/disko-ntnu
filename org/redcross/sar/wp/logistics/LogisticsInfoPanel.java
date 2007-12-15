package org.redcross.sar.wp.logistics;


import org.redcross.sar.app.Utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/**
 *
 */
public class LogisticsInfoPanel extends JPanel
{
    private final static Dimension m_buttonDimension = new Dimension(50, 50);
    private JPanel m_topInfoPanel;
    private ScrollInfoPanel m_centerScrollPanel;
    private JPanel m_lowButtonPanel;
    private InternalInfoPanel[] m_topPanelElements;
    private InternalInfoPanel[] m_centerPanelElements;
    private JButton[] m_lowPanelButtons;


    public LogisticsInfoPanel(int topPanelPartCount, int centerPanelPartCount, int buttonCount)
    {
        super(new BorderLayout(0, 0));
//        setBackground(Color.WHITE);
//        setOpaque(true);

        m_topInfoPanel = new JPanel();
        add(m_topInfoPanel, BorderLayout.NORTH);
        initTopPanel(topPanelPartCount);

        m_centerScrollPanel = new ScrollInfoPanel();
        JScrollPane scrollpane = new JScrollPane(m_centerScrollPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollpane, BorderLayout.CENTER);
        initCenterPanel(centerPanelPartCount);

        m_lowButtonPanel = new JPanel();
        add(m_lowButtonPanel, BorderLayout.SOUTH);
        initLowPanel(buttonCount);
    }

    private void initTopPanel(int topPanelPartCount)
    {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        m_topInfoPanel.setLayout(gridbag);
//        m_topInfoPanel.setBackground(Color.WHITE);

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;

        boolean hasHeader = topPanelPartCount % 2 == 1; // Odd number of items
        m_topPanelElements = new InternalInfoPanel[topPanelPartCount];

        c.gridwidth = hasHeader ? 2 : 1;
        c.gridx = 0;
        c.gridy = 0;
        for (int i = 0; i < topPanelPartCount; i++)
        {
            boolean headerPanel = hasHeader && i == 0;
            InternalInfoPanel ip = new InternalInfoPanel("P" + i, !headerPanel);
            ip.setText("T" + i);
            if (headerPanel)
            {
                JLabel h = ip.getHeaderLabel();
                h.setHorizontalAlignment(SwingConstants.CENTER);
            }
            m_topInfoPanel.add(ip, c);
            m_topPanelElements[i] = ip;
            c.gridwidth = 1;
            if (i % 2 == (hasHeader ? 0 : 1))
            {
                c.gridx = 0;
                c.gridy++;
            } else
            {
                c.gridx++;
            }
        }
    }

    private void initCenterPanel(int centerPanelPartCount)
    {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        m_centerScrollPanel.setLayout(gridbag);

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;

        m_centerPanelElements = new InternalInfoPanel[centerPanelPartCount];

        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridx = 0;
        c.gridy = 0;
        for (int i = 0; i < centerPanelPartCount; i++)
        {
            InternalInfoPanel ip = new InternalInfoPanel("P" + i, true);
            ip.setText(" ");
            m_centerScrollPanel.add(ip, c);
            m_centerPanelElements[i] = ip;
            c.gridy++;
        }
    }

    private void initLowPanel(int buttonCount)
    {
        m_lowButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
//        m_lowButtonPanel.setBackground(Color.WHITE);
        m_lowPanelButtons = new JButton[buttonCount];
        for (int i = 0; i < buttonCount; i++)
        {
            JButton b = new JButton("B" + i);
            b.setMinimumSize(m_buttonDimension);
            b.setPreferredSize(m_buttonDimension);
            m_lowButtonPanel.add(b);
            m_lowPanelButtons[i] = b;
            b.setVisible(false);
        }
    }

    void setHeaders(String[] theHeaders)
    {
        int i = 0;
        // set headers in top panel
        for (InternalInfoPanel p : m_topPanelElements)
        {
            if (i < theHeaders.length)
            {
                p.setHeader(theHeaders[i]);
            }
            i++;
        }
        // set headers in center panel
        for (InternalInfoPanel p : m_centerPanelElements)
        {
            if (i < theHeaders.length)
            {
                p.setHeader(theHeaders[i]);
            }
            i++;
        }
    }

    void setTopText(int aFieldIndex, String aText)
    {
        if (aFieldIndex >= 0 && aFieldIndex < m_topPanelElements.length)
        {
            m_topPanelElements[aFieldIndex].setText(aText);
        }
    }

    void setCenterText(int aFieldIndex, String aTexts)
    {
        if (aFieldIndex >= 0 && aFieldIndex < m_centerPanelElements.length)
        {
            m_centerPanelElements[aFieldIndex].setText(aTexts);
        }
    }

    void setCenterText(int aFieldIndex, Iterable<String> theTexts)
    {
        if (aFieldIndex >= 0 && aFieldIndex < m_centerPanelElements.length)
        {
            m_centerPanelElements[aFieldIndex].setText(theTexts);
        }
    }

    void setButtonVisible(int aButtonIndex, boolean aFlag)
    {
        if (aButtonIndex >= 0 && aButtonIndex < m_lowPanelButtons.length)
        {
            m_lowPanelButtons[aButtonIndex].setVisible(aFlag);
        }
    }

    void setButtonEnabled(int aButtonIndex, boolean aFlag)
    {
        if (aButtonIndex >= 0 && aButtonIndex < m_lowPanelButtons.length)
        {
            m_lowPanelButtons[aButtonIndex].setEnabled(aFlag);
        }
    }

    void setButtons(String[] theButtonTexts, String[] theActionCommands, ActionListener aListener)
    {
        int i = 0;
        for (JButton b : m_lowPanelButtons)
        {
            if (i < theButtonTexts.length)
            {
                String buttonText = theButtonTexts[i].toLowerCase();
                boolean iconSet = false;
                if (buttonText.endsWith(".gif") || buttonText.endsWith(".png"))
                {
                    try
                    {
                        ImageIcon icon = Utils.createImageIcon(buttonText, null);
                        if (icon != null)
                        {
                            b.setIcon(icon);
                            b.setText(null);
                            iconSet = true;
                        }
                    }
                    catch (Exception e)
                    {
                    }

                }
                if (!iconSet)
                {
                    b.setText(buttonText);
                    b.setIcon(null);
                }
                b.setVisible(true);
                b.setEnabled(true);
            }
            // remove existing action listeners
            ActionListener[] listeners = b.getActionListeners();
            for (ActionListener l : listeners)
            {
                b.removeActionListener(l);
            }
            // add new listener and set command
            if (i < theActionCommands.length)
            {
                b.addActionListener(aListener);
                b.setActionCommand(theActionCommands[i]);
            }
            i++;
        }
    }

    public void clearTopTexts()
    {
        for (InternalInfoPanel p: m_topPanelElements)
        {
            p.setText("");
        }
    }

    public void clearCenterTexts()
    {
        for (InternalInfoPanel p: m_centerPanelElements)
        {
            p.setText("");
        }
    }

    public static class InternalInfoPanel extends JPanel
    {
        static final String lineSeparator = System.getProperty("line.separator");

        private JLabel m_headerLabel;
        private String m_headerText = "";
        private JTextArea m_textArea;
        private String m_displayText = "";
        private boolean m_hasTextField;

        public InternalInfoPanel(boolean hasTextField)
        {
            this("", hasTextField);
        }

        public InternalInfoPanel(String aHeader, boolean hasTextField)
        {
            super(new BorderLayout(0, 0));
//            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

            m_hasTextField = hasTextField;

            m_headerLabel = new JLabel();
            add(m_headerLabel, BorderLayout.NORTH);
            m_headerLabel.setOpaque(false); // get the same bacground as parent
            setHeader(aHeader);


            m_textArea = new JTextArea();
            add(m_textArea, BorderLayout.CENTER);
            m_textArea.setLineWrap(true);
            m_textArea.setWrapStyleWord(true);
            m_textArea.setOpaque(false);
            m_textArea.setEditable(false);

            setText("");
        }

        JLabel getHeaderLabel()
        {
            return m_headerLabel;
        }

        JTextArea getTextArea()
        {
            return m_textArea;
        }

        public void setHeader(String aHeader)
        {
            m_headerText = aHeader;
            if (m_hasTextField)
            {
                m_headerLabel.setText(m_headerText);
            } else
            {
                m_headerLabel.setText(m_headerText + " " + m_displayText);
            }
        }

        public void setText(Iterable<String> theLines)
        {
            int charCount = 0;
            for (String s : theLines)
            {
                charCount += s.length() + lineSeparator.length();
            }
            StringBuilder sb = new StringBuilder(charCount);
            int i = 0;
            for (String s : theLines)
            {
                sb.append(s);
                i++;
                if (i < s.length())
                {
                    sb.append(lineSeparator);
                }
            }
            setText(sb.toString());
        }

        public void setText(String aText)
        {
            m_textArea.setVisible(true);
            m_displayText = aText;
            if (m_hasTextField)
            {
                m_textArea.setText(m_displayText);
//                m_textArea.setVisible(m_displayText.length() > 0);
            } else
            {
                m_headerLabel.setText(m_headerText + " " + m_displayText);
//                m_textArea.setVisible(false);
            }
        }
    }

    private class ScrollInfoPanel extends JPanel implements Scrollable
    {

        public Dimension getPreferredScrollableViewportSize()
        {
            return getPreferredSize();
        }

        public Dimension getPreferredSize()
        {
            return super.getPreferredSize();
        }


        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
        {
            return 0;
        }

        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
        {
            return 0;
        }

        public boolean getScrollableTracksViewportWidth()
        {
            return true;
        }

        public boolean getScrollableTracksViewportHeight()
        {
            return false;
        }
    }
}
