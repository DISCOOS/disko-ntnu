package org.redcross.sar.gui.panel;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionListener;

import javax.swing.border.Border;

import org.redcross.sar.gui.IChangeable;
import org.redcross.sar.map.event.IMsoLayerEventListener;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.work.event.IWorkFlowListener;

public interface IPanel extends IChangeable,
								IMsoUpdateListenerIf,
								IMsoLayerEventListener,
								IWorkFlowListener,
								ActionListener {


	/* ================================================
	 * IChangeable interface
	 * ================================================ */

	public boolean isDirty();
	public void setDirty(boolean isDirty);

	public boolean isChangeable();
	public void setChangeable(boolean isChangable);

	public void reset();
	public boolean finish();
	public boolean cancel();

	/* ================================================
	 * IPanel interface
	 * ================================================ */

	public void update();

	public boolean isRequestHideOnFinish();
	public void setRequestHideOnFinish(boolean isEnabled);

	public boolean isRequestHideOnCancel();
	public void setRequestHideOnCancel(boolean isEnabled);

	public void addActionListener(ActionListener listener);
	public void removeActionListener(ActionListener listener);

	public void addWorkFlowListener(IWorkFlowListener listener);
	public void removeWorkFlowListener(IWorkFlowListener listener);

    public Container getContainer();
    public void setContainer(Container container);

    public Dimension getPreferredContainerSize();
    public void setPreferredContainerSize(Dimension size);

    public Dimension getMinimumContainerSize();
    public void setMinimumContainerSize(Dimension size);

    public Dimension getMaximumContainerSize();
    public void setMaximumContainerSize(Dimension size);

    public LayoutManager getContainerLayout();
    public void setContainerLayout(LayoutManager manager);

    public void setContainerBorder(Border border);

    public Component addToContainer(Component c);
    public Component addToContainer(Component c, int index);
    public Component addToContainer(String name, Component c);
	public void addToContainer(Component c, Object constraints);
	public void addToContainer(Component c, Object constraints, int index);

    public void removeFromContainer(int index);
    public void removeFromContainer(Component c);
    public void removeAllFromToContainer();

    public boolean isContainerEnabled();
    public void setContainerEnabled(Boolean isEnabled);

    public Dimension fitContainerToMinimumLayoutSize();
    public Dimension fitContainerToPreferredLayoutSize();
    public Dimension fitContainerToMaximumLayoutSize();

    public Dimension fitThisToMinimumContainerSize();
    public Dimension fitThisToPreferredContainerSize();
    public Dimension fitThisToMaximumContainerSize();

    public IPanelManager getManager();

    public IPanelManager getParentManager();
	public void setParentManager(IPanelManager parent, boolean requestMoveTo, boolean setAll);

}
