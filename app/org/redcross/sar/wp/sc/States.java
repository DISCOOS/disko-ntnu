package org.redcross.sar.wp.sc;

import java.awt.Color;

import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.redcross.sar.gui.panel.LevelPanel;
import org.redcross.sar.gui.panel.TimeLinePanel;
import org.redcross.sar.util.Utils;

public class States extends JScrollPane {

	private static final long serialVersionUID = 1L;
	private JPanel m_stateList;
	private LevelPanel m_state;
	private LevelPanel m_progress;
	private TimeLinePanel m_timeLine;

	/**
	 * This is the default constructor
	 */
	public States() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize()
	{
		this.setViewportView(getStateList());
	}

	/**
	 * This method initializes StateList
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getStateList() {
		if (m_stateList == null) {
			m_stateList = new JPanel();
			m_stateList.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
			m_stateList.setLayout(new BoxLayout(m_stateList, BoxLayout.Y_AXIS));
			m_stateList.add(getState());
			m_stateList.add(getUnits());
			m_stateList.add(getTimeLine());
		}
		return m_stateList;
	}

	/**
	 * This method initializes State
	 *
	 * @return javax.swing.JPanel
	 */
	private LevelPanel getState() {
		if (m_state == null) {
			m_state = new LevelPanel(20,30,1);
			Border etched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			TitledBorder title = BorderFactory.createTitledBorder(etched, "Tilstand");
			m_state.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5), title));
			m_state.setUnitName("oppdrag");
			m_state.setDrawLevelLabel(false);
			m_state.setBackground(Color.WHITE);
			m_state.setRange(0, 100);
			m_state.addLimit(0, "", new Color(255,102,0), false);
			m_state.addLimit(30, "Marginalt", new Color(51,102,255), false);
			m_state.addLimit(60, "Optimalt", new Color(255,204,0), false);
			Utils.setFixedHeight(m_state, 130);
		}
		return m_state;
	}

	/**
	 * This method initializes Units
	 *
	 * @return javax.swing.JPanel
	 */
	private LevelPanel getUnits() {
		if (m_progress == null) {
			m_progress = new LevelPanel(20,30,2);
			Border etched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			TitledBorder title = BorderFactory.createTitledBorder(etched, "Fremdrift");
			m_progress.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5), title));
			m_progress.setUnitName("min");
			m_progress.setDrawLevelLabel(true);
			m_progress.setBackground(Color.WHITE);
			m_progress.setRange(0, 100);
			m_progress.addLimit(0, "", new Color(51,102,255), false);
			m_progress.addLimit(30, "Snart ledig", new Color(255,204,0), false);
			m_progress.addLimit(60, "Ledig", new Color(255,102,0), false);
			Utils.setFixedHeight(m_progress, 400);
		}
		return m_progress;
	}

	/**
	 * This method initializes Resources
	 *
	 * @return javax.swing.JPanel
	 */
	private TimeLinePanel getTimeLine() {
		if (m_timeLine == null) {
			m_timeLine = new TimeLinePanel();
			m_timeLine.setSteps(5, 10);
			m_timeLine.setRange(1,40);
			Border etched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			TitledBorder title = BorderFactory.createTitledBorder(etched, "Tidslinje");
			m_timeLine.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5), title));
			Utils.setFixedHeight(m_timeLine, 150);
		}
		return m_timeLine;
	}

}  //  @jve:decl-index=0:visual-constraint="10,-2"
