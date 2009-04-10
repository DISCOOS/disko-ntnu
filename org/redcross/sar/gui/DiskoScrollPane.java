package org.redcross.sar.gui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.panel.BaseToolPanel;
import org.redcross.sar.gui.panel.PanelManager;
import org.redcross.sar.gui.panel.TogglePanel;
import org.redcross.sar.gui.util.AlignUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Rectangle2D;

public class DiskoScrollPane extends JScrollPane {

	private static final long serialVersionUID = 1L;

	private boolean isAutoFitToView = false;
	private boolean isAutoFitToParent = false;

	private ComponentListener listener;

	public DiskoScrollPane() {
		// forward
		super();
    }

	public DiskoScrollPane(Component view) {
    	// forward
		super();
		// prepare
		setViewportView(view);
	}

	public DiskoScrollPane(int vsbPolicy, int hsbPolicy) {
    	// forward
		super(vsbPolicy, hsbPolicy);
	}

    public DiskoScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
    	// forward
		super(vsbPolicy, hsbPolicy);
		// prepare
		setViewportView(view);
	}

    public boolean isAutoFitToParent() {
    	return isAutoFitToParent;
    }

    public void setAutoFitToParent(boolean isAutoFitToParent) {
    	this.isAutoFitToParent = isAutoFitToParent;
    }

    public boolean isAutoFitToView() {
    	return isAutoFitToParent;
    }

    public void fitToParent() {

    }

    public void setAutoFitToView(boolean isAutoFitToView) {
    	this.isAutoFitToView = isAutoFitToView;
    }

    public void fitToView() {

    }

    @Override
	public void setViewportView(Component view) {
    	if(getViewport().getView()!=null) {
    		removeComponentListener(listener);
    	}
		// update
		super.setViewportView(view);
		// register listener?
    	if(view!=null) {
    		listener = getScrollPaneComponentListener(view);
    		addComponentListener(listener);
    	}
	}

	@Override
    public Dimension getPreferredSize() {
		if(getViewport().getView()!=null) {
			getViewport().setPreferredSize(getViewport().getView().getPreferredSize());
		}
        return super.getPreferredSize();
    }

	private void initialize() {

		addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorAdded(AncestorEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void ancestorMoved(AncestorEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void ancestorRemoved(AncestorEvent e) {
				// TODO Auto-generated method stub

			}

		});
	}

    private ComponentListener getScrollPaneComponentListener(final Component view) {

        return new ComponentListener() {

        	@Override
            public void componentHidden(ComponentEvent e) {
                autoFit(e);
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                autoFit(e);
            }

            @Override
            public void componentResized(ComponentEvent e) {
                autoFit(e);
            }

            @Override
            public void componentShown(ComponentEvent e) {
                autoFit(e);
            }

            private void autoFit(ComponentEvent e) {
            	// is parent or view?
            	if(e.getComponent()==getParent() && isAutoFitToParent) {

            	}
            	else if(e.getComponent()==DiskoScrollPane.this && isAutoFitToView) {

            		getViewport().getView().setPreferredSize(null);

            		/*
                    Container viewContainer = view.getParent();
                    Double viewContainerWidth = viewContainer.getSize().getWidth();
                    Double viewPreferredWidth = view.getPreferredSize().getWidth();
                    if (viewContainer instanceof JViewport) {

                    }
                    */
            	}
            }
        };
    }

	/**
	 * The main method.
	 *
	 * @param args
	 */
	public static void main(String[] args)
	{

		UIFactory.initLookAndFeel();

		// initialize GUI on new thread
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{

				if(false) {

					final JDialog dialog = new JDialog();

					final JPanel content = new JPanel(new BorderLayout());
					content.setBorder(UIFactory.createBorder());
					content.setPreferredSize(new Dimension(300,100));

					final JPanel panel1 = new JPanel();
					panel1.setBorder(UIFactory.createBorder());
					panel1.setLayout(new BoxLayout(panel1,BoxLayout.Y_AXIS));
					panel1.setPreferredSize(new Dimension(300,100));

					final JPanel panel2 = new JPanel();
					panel2.setBorder(UIFactory.createBorder());
					panel2.setPreferredSize(new Dimension(300,100));

					final JPanel panel3 = new JPanel();
					panel3.setBorder(UIFactory.createBorder());
					panel3.setPreferredSize(new Dimension(300,100));

					final JPanel panel4 = new JPanel();
					panel4.setBorder(UIFactory.createBorder());
					panel4.setLayout(new BoxLayout(panel4,BoxLayout.X_AXIS));
					panel4.setPreferredSize(new Dimension(300,100));

					final JPanel panel5 = new JPanel();
					panel5.setBorder(UIFactory.createBorder());
					panel5.setPreferredSize(new Dimension(300,100));

					final JPanel panel6 = new JPanel();
					panel6.setBorder(UIFactory.createBorder());
					panel6.setPreferredSize(new Dimension(300,100));

					JButton button = new JButton("Test");
					button.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {

							test();

						}

						private void test() {
							// apply change
							panel2.setPreferredSize(new Dimension(300,100));
							Dimension d = pack(dialog.getContentPane());
							content.setPreferredSize(d);
							content.invalidate();
							dialog.pack();
						}

						private Dimension pack(Container c) {
							Rectangle frame = new Rectangle(c.getLocation(),new Dimension(0,0));
							for(Component it : c.getComponents()) {
								if(it instanceof Container) {
									frame = frame.union(pack((Container)it,frame));
								}
							}
							return frame.getSize();
						}

						private Rectangle pack(Container c, Rectangle frame) {
							for(Component it : c.getComponents()) {
								if(it instanceof Container) {
									frame = frame.union(pack((Container)it,frame));
								}
							}
							LayoutManager lm = c.getLayout();
							if(lm!=null && !c.isPreferredSizeSet())
								return frame.union(new Rectangle(c.getLocation(),c.getLayout().preferredLayoutSize(c)));
							else
								return frame.union(new Rectangle(c.getLocation(),c.getPreferredSize()));
						}

					});

					content.add(button,BorderLayout.NORTH);
					content.add(panel1,BorderLayout.CENTER);
					panel1.add(panel2);
					panel1.add(panel3);
					panel1.add(panel4);
					panel4.add(panel5);
					panel4.add(panel6);

					dialog.setUndecorated(false);
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setContentPane(content);
					dialog.pack();

					dialog.setVisible(true);

				}
				else {
					final JFrame frame = new JFrame("Dette er en test");
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

					final JDialog dialog = new JDialog(frame);
					final TogglePanel content = new TogglePanel("Main Toggle");
					final PanelManager manager = new PanelManager(null,dialog);

					final JPanel panel = new JPanel(new BorderLayout(5,5));
					JButton button = new JButton("Test");
					button.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {

							Rectangle rc = panel.getBounds();
							rc.setLocation(panel.getLocationOnScreen());

							Rectangle2D rc2d = AlignUtils.align(rc, AlignUtils.NORTH_WEST, 200,
									content.getHeight(), 10, AlignUtils.NORTH_WEST, AlignUtils.FIT);

							dialog.setBounds(rc2d.getBounds());
							//manager.requestFitToPreferredContentSize();

						}

					});

					panel.add(button,BorderLayout.CENTER);
					frame.setContentPane(panel);
					frame.setPreferredSize(new Dimension(400,500));
					frame.pack();
					frame.setLocationByPlatform(true);
					frame.setVisible(true);

					dialog.setUndecorated(false);
					//dialog.setPreferredSize(new Dimension(300,400));
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

					//content.setNotScrollBars();
					content.setParentManager(manager, true, false);
					content.setContainerBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					Container container = content.getContainer();
					container.setLayout(new BoxLayout(container,BoxLayout.Y_AXIS));

					final BaseToolPanel toggle1 = new BaseToolPanel("Tool 1",null);
					//final BasePanel toggle1 = new BasePanel("Sub Toggle 1");
					//final TogglePanel toggle1 = new TogglePanel("Sub Toggle 1");
					//toggle1.setNotScrollBars();
					toggle1.setParentManager(content, false, false);
					//toggle1.setPreferredSize(new Dimension(250,300));
					toggle1.setContainerLayout(new BoxLayout(toggle1.getContainer(),BoxLayout.Y_AXIS));
					//toggle1.setContainerBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), UIFactory.createBorder(1, 1, 1, 1, Color.WHITE)));

					final TogglePanel toggle2 = new TogglePanel("Sub Toggle 2");
					toggle2.setAlignmentY(TogglePanel.CENTER_ALIGNMENT);
					toggle2.setParentManager(toggle1, false, false);
					toggle2.setContainerBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), UIFactory.createBorder(1, 1, 1, 1, Color.RED)));
					//toggle2.setPreferredSize(new Dimension(200,200));

					final TogglePanel toggle3 = new TogglePanel("Sub Toggle 3");
					toggle3.setAlignmentY(TogglePanel.CENTER_ALIGNMENT);
					toggle3.setParentManager(toggle1, false, false);
					toggle3.setContainerBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), UIFactory.createBorder(1, 1, 1, 1, Color.BLUE)));
					//toggle3.setPreferredSize(new Dimension(200,200));

					final TogglePanel toggle4 = new TogglePanel("Sub Toggle 4");
					toggle4.setAlignmentY(TogglePanel.CENTER_ALIGNMENT);
					toggle4.setParentManager(toggle1, false, false);
					toggle4.setContainerBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), UIFactory.createBorder(1, 1, 1, 1, Color.BLUE)));
					//toggle4.setPreferredSize(new Dimension(200,200));

					content.addToContainer(toggle1,BorderLayout.CENTER);
					toggle1.addToContainer(toggle2);
					toggle1.addToContainer(Box.createVerticalStrut(5));
					toggle1.addToContainer(toggle3);
					toggle1.addToContainer(Box.createVerticalStrut(5));
					toggle1.addToContainer(toggle4);
					toggle2.addToContainer(new JLabel("Dette er nivå 2"),BorderLayout.CENTER);
					toggle4.addToContainer(new JLabel("Dette er nivå 3"),BorderLayout.CENTER);

					/*
					content.addToContainer(Box.createVerticalStrut(5));

					final BaseToolPanel tool2 = new BaseToolPanel("Tool 2",null);
					tool2.setParentManager(content, false, false);
					tool2.setPreferredSize(new Dimension(250,300));
					content.addToContainer(tool2);

					final TogglePanel toggle2 = new TogglePanel("Sub Toggle 2");
					toggle2.setParentManager(tool2, false, false);
					toggle2.setPreferredSize(new Dimension(200,200));
					center = new JLabel("Dette er rad 2");
					toggle2.addToContainer(center);
					tool2.addToContainer(toggle2);
					*/

					dialog.setContentPane(content);
					dialog.pack();
					dialog.setVisible(true);
				}
			}
		});
	}

}
