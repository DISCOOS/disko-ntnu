package org.redcross.sar.wp.states;

import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.border.*;
import javax.swing.BoxLayout;

import java.awt.event.ComponentListener;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ActionEvent;

import java.util.Calendar;
import java.util.logging.Logger;

import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.util.mso.Route;
import org.redcross.sar.wp.ds.old.RouteCostPanel;
import org.redcross.sar.ds.ete.RouteCost;

//import java.util.Random;

public class States extends JScrollPane implements ComponentListener, ActionListener {

	// Logger for this object
	private static final Logger LOGGER = Logger.getLogger(
			"org.redcross.sar.wp.states");
	
	private static final long serialVersionUID = 1L;
	private JPanel StateList = null;
	private JPanel Resources = null;
	private StateBar ProdState = null;
	private JButton b1 = null;
	private DiskoWpStatesImpl wp;

	/**
	 * This is the default constructor
	 */
	public States(DiskoWpStatesImpl wp) {
		super();
		this.wp = wp;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() 
	{
		this.setSize(470, 295);
		this.setViewportView(getStateList());
	}

	/**
	 * This method initializes StateList	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getStateList() {
		if (StateList == null) {
			StateList = new JPanel();
			StateList.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
			StateList.setLayout(new BoxLayout(getStateList(), BoxLayout.Y_AXIS));
			StateList.add(getProdState(), null);
			StateList.add(getResources(), null);
			getProdState().addComponentListener(this);
			getResources().addComponentListener(this);
		}
		return StateList;
	}

	/**
	 * This method initializes ProdState	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private StateBar getProdState() {
		if (ProdState == null) {
			ProdState = new StateBar(20,35);
			ProdState.setLimits(100,30,50);
			Border etched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			TitledBorder title = BorderFactory.createTitledBorder(etched, "Antall klare oppdrag");
			ProdState.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5), title));			
		}
		return ProdState;
	}
	
	/**
	 * This method initializes Resources	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getResources() {
		if (Resources == null) {
			Resources = new JPanel();
			Resources.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
			b1 = new JButton("Run");
			Resources.add(b1);
	        b1.addActionListener(this);
		}
		return Resources;
	}
	
	public void componentResized(ComponentEvent e) 
	{
		Dimension size = StateList.getSize();
		size.height = 130;
		e.getComponent().setMinimumSize(size);
		e.getComponent().setPreferredSize(size);
		e.getComponent().setMaximumSize(size);
		StateList.invalidate();
	}
	public void componentMoved(ComponentEvent e) {}
	public void componentShown(ComponentEvent e) {}
	public void componentHidden(ComponentEvent e) {}

	public void actionPerformed(ActionEvent e) {

		if(e.getSource() == b1) {
  
			// initialize
			double cost = 0;
			
			// create new route
	    	Route r = new Route("1","Test",100);
	    	for(int i = 0;i<100;i++){
	        	// add positions
	        	r.add(i, i);        		
	    	}
	    	
	    	// get new time instance
	    	Calendar t = Calendar.getInstance();
	    	
	    	// get first tic
	    	long tic1 = t.getTimeInMillis();
	    	
	    	/*
	    	// create estimator
	    	RouteCost rce = new RouteCost(r,2);
	    	
	    	// run the route cost estimator n times
	    	for(int i = 0; i < 100 ;i++) {
	    		cost += rce.ete();    		
	    	}
	    	*/
	    	
	    	// get calendar
	    	t = Calendar.getInstance();
	    	
	    	// get last tic
	    	long tic2 = t.getTimeInMillis();
	    	
	    	// logg result
	    	LOGGER.info("ete:=" + (int)cost + " sec calulcated in " + (tic2 - tic1) + "ms");
		}
	}
}  //  @jve:decl-index=0:visual-constraint="10,-2"
