package org.redcross.sar.output;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;
import net.sf.jasperreports.engine.JRPrintPage;

import org.redcross.sar.IApplication;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.IDiskoMapManager;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitListIf;
import org.redcross.sar.util.mso.DTG;

import com.esri.arcgis.carto.IActiveView;
import com.esri.arcgis.display.IOutputRasterSettings;
import com.esri.arcgis.display.tagRECT;
import com.esri.arcgis.geometry.Envelope;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.output.ExportBMP;
import com.esri.arcgis.output.IExport;

public class DiskoReportManager {

	// print map units in meters. In iReport equals this 487, pixelsize = 0.000037 meters
	private final static double mapPrintWidthSize = 0.169;
	// print map units in meters. In iReport equals this 487, pixelsize = 0.000037 meters
	private final static double mapPrintHeigthSize = 0.169;
	private final static String outputPrintFormat = ".JPG";

	private IApplication m_app;

	private String m_templatePath;

	private DiskoMap m_map;
	private IActiveView m_activeView;
	private IDiskoMapManager m_mapManager;
	private ISpatialReference m_srs;

	private JasperPrint m_prints;

	private double m_reportMapScale;

	private String m_now;

	private JasperViewer m_viewer;

	public DiskoReportManager(IApplication app){

		// prepare
		m_app = app;
		m_reportMapScale = Double.parseDouble(m_app.getProperty("report.mapscale"));
		m_mapManager = m_app.getMapManager();
		m_templatePath = m_app.getProperty("report.template.path");

	}

	/**
	 * Initialize the report class
	 *
	 */
	private void initialize() {

		// get map objects
		try{
			// get map?
			if(m_map==null) m_map = (DiskoMap)m_mapManager.getPrintMap();
			// get spatial reference?
			if(m_srs==null) m_srs = m_map.getSpatialReference();
			// update active view
			m_activeView = m_map.getMapImpl().getActiveView();
			// update map?
			m_map.execute(true,true);
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	/**
	 *
	 *
	 */
	public void compile(String jrxmlFileName, String jasperFileName){
		//System.out.println("DiskoReport: compile()");
		try{
			JasperCompileManager.compileReportToFile(m_templatePath + jrxmlFileName, m_templatePath + jasperFileName);
		}
		catch(JRException jre){
			jre.printStackTrace();
			//System.out.println("Kompilering av .jrxml fil feilet");
		}
	}

	/**
	 * Prints out an list of assignments to a single report
	 * @param assignments
	 */
	public void printAssignments(List<IAssignmentIf> assignments){
		printAssignments(assignments,m_reportMapScale,false);
	}

	/**
	 * Prints out an list of assignments to a single report
	 * @param assignments
	 * @param map scale
	 */
	public void printAssignments(List<IAssignmentIf> assignments, double scale, boolean append){

		// initialize report
		initialize();

		// reset previous prints?
		if(!append) m_prints = null;

		// update scale
		m_reportMapScale = scale;

		// get jasper file name
		String jasperFileName = m_app.getProperty("report.TACTICS_TEMPLATE")+".jasper";

		/*/ compile to jasper file format?
		if(!(new File(jasperFileName)).exists()) {
			String jrxmlFileName = m_app.getProperty("report.TACTICS_TEMPLATE")+".jrxml";
			compile(jrxmlFileName, jasperFileName);
		}*/

		// loop over all assignments
		for (int i=0;i<assignments.size(); i++){

			// get assignment
			IAssignmentIf assignment = assignments.get(i);

			// prepare map for image export
			prepareMap(assignment);

			// export map til image file
			String exportMapPath = exportMap();

			// get assigment values
			Map<String, Object> assignmentsMap = getAssignmentValues(assignment, exportMapPath);

			// fill assignment values to jasper report
			JasperPrint jPrint = fill(jasperFileName, assignmentsMap);

			// delete map
			File file = new File(exportMapPath);
			if(file.exists()) file.delete();

			// append pages?
			if (m_prints!=null) {
				// get all pages
				List pages = jPrint.getPages();
				// loop over all pages
				for(int j=0;j<pages.size();j++) {
					m_prints.addPage((JRPrintPage)pages.get(j));
				}
			}
			else {
				// initialize
				m_prints = jPrint;
			}

		}

		//preview
		view(m_prints);

	}

	public void printUnitLog(IUnitIf unit){
		printUnitLog(unit,m_reportMapScale,false);
	}

	public void printUnitLog(IUnitIf unit, double scale, boolean append){

		// initialize report
		initialize();

		// reset previous prints?
		if(!append) m_prints = null;

		// update scale
		m_reportMapScale = scale;

		// get report template file name
		String jasperFileName = m_app.getProperty("report.UNITLOG_TEMPLATE")+".jasper";

		//inntil videre kjøres kompilering også

		//String jrxmlFileName = m_app.getProperty("report.UNITLOG_TEMPLATE")+".jrxml";
		//compile(jrxmlFileName, jasperFileName);


		ICmdPostIf cmdPost = m_app.getMsoModel().getMsoManager().getCmdPost();
		IUnitListIf unitList = cmdPost.getUnitList();

		UnitlogReportParams unitLogParams = new UnitlogReportParams();

		//ekstrahere unit verdier
		Map unitsMap = unitLogParams.getUnitlogReportParams(unitList, unit);

		JasperPrint jPrint = fill(jasperFileName, unitsMap);

		// append pages?
		if (m_prints!=null && append) {
			// get all pages
			List pages = jPrint.getPages();
			// loop over all pages
			for(int j=0;j<pages.size();j++) {
				m_prints.addPage((JRPrintPage)pages.get(j));
			}
		}
		else {
			// initialize
			m_prints = jPrint;
		}

		//preview
		view(m_prints);

	}

	private JasperPrint fill(String jasperFileName, Map<String,Object> data){
		// initialize
		JasperPrint jasperPrint = null;

		try{

			// create file
			File sourceFile = new File(m_templatePath + jasperFileName);

			// load jasper report from file
			JasperReport jasperReport = (JasperReport)JRLoader.loadObject(sourceFile);

			// fill jasper report
			jasperPrint = JasperFillManager.fillReport(jasperReport, data, new JREmptyDataSource());

		} catch(JRException jre){
			jre.printStackTrace();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return jasperPrint;
	}



	private void view(JasperPrint jrprintFile){
		//System.out.println("DiskoReport: view() " + jrprintFile);
		try{
			// close current?
			if(m_viewer!=null)
				m_viewer.dispose();
			// create new
			m_viewer = new JasperViewer(jrprintFile, false);
			m_viewer.setVisible(true);
		}
		catch(Exception e){
			e.printStackTrace();

		}
	}

	private Map<String, Object> getAssignmentValues(IAssignmentIf assignment, String exportMapPath){

		String role_name = m_app.getCurrentRole().getTitle() + " (" + m_app.getCurrentRole().getName()+ ")";

		m_now = DTG.CalToDTG(Calendar.getInstance());

		MissionOrderReportParams missionOrderPrint = new MissionOrderReportParams();
		Map<String, Object> map = missionOrderPrint.setPrintParams(assignment, exportMapPath, role_name, m_templatePath, m_srs, m_reportMapScale, m_now);

		return map;

	}

	public double getMapScale() {
		return m_reportMapScale;
	}

	public void setMapScale(double scale) {
		m_reportMapScale = scale;
	}

	private void prepareMap(IAssignmentIf assignment){
		try{

			// activate map
			updateMap();

			// hide layers
			m_map.setMsoLayersVisible(IMsoManagerIf.MsoClassCode.CLASSCODE_OPERATIONAREA,false);
			m_map.setMsoLayersVisible(IMsoManagerIf.MsoClassCode.CLASSCODE_SEARCHAREA,false);

			// get planned area object
			IMsoObjectIf msoObj = assignment.getPlannedArea();

			// get area feature layer
			IMsoFeatureLayer layer = m_map.getMsoLayer(IMsoFeatureLayer.LayerCode.AREA_LAYER);

			// hide all other areas
			layer.setVisibleFeatures(msoObj, true, false);

			// get assignment poi list
			List<IMsoObjectIf> msoObjs = new ArrayList<IMsoObjectIf>(assignment.getPlannedArea().getAreaPOIsItems());

			// get poi feature layer
			layer = m_map.getMsoLayer(IMsoFeatureLayer.LayerCode.POI_LAYER);

			// hide all other poi
			layer.setVisibleFeatures(msoObjs, true, false);

			// zoom to assignment and apply specified scale
			m_map.zoomToPrintMapExtent(assignment, m_reportMapScale, mapPrintHeigthSize, mapPrintWidthSize);

		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}
	}

	private void updateMap() {
		// get state
		boolean isMapReady = m_map.getMsoBinder().getBufferCount()==0;
		// activate?
		if(!isMapReady) m_map.execute(true,true);
	}

	private String exportMap(){

		// initialize
		long imgResolution = 100;
		long imgResampleRatio = 1;
		long hdc = 0;
		tagRECT exportRECT = new tagRECT();
		String outputDir = m_app.getProperty("report.output.path");
		long randomNumber = ((long)(Math.random() * 100000));

		//System.out.println("Math.random(): " + Math.random() + "randomNumber: " + randomNumber);

		String outputFileName = m_app.getProperty("report.output.printname") + randomNumber;
		String exportPath = outputDir+outputFileName+outputPrintFormat;
		IExport docExport = null;
		IEnvelope pixelBoundsEnv = null;

		try{

			// create objects
			docExport = new ExportBMP();

			//set export filename
			//System.out.println(exportPath);
	        docExport.setExportFileName(exportPath);

	        //set outputResolution
	        docExport.setResolution(imgResolution);

			//always set the output quality of the DISPLAY to 1 for image export formats
	        setOutputQuality(m_activeView, imgResampleRatio);

	        //Assign envelope object to the export object
	        pixelBoundsEnv = new Envelope();
	        exportRECT.bottom = 1250;
	        exportRECT.right = 1250;
	        exportRECT.top = 0;
	        exportRECT.left = 0;
	        pixelBoundsEnv.putCoords(exportRECT.left, exportRECT.top, exportRECT.right, exportRECT.bottom);

	        docExport.setPixelBounds(pixelBoundsEnv);

	        //ready to export
	        hdc = docExport.startExporting();

	        // Redraw the active view, rendering it to the exporter object
	        m_activeView.output((int) hdc, (int)docExport.getResolution(), exportRECT, null, null);

	        //finish export
	        docExport.finishExporting();
	        docExport.cleanup();

		}catch(IOException ioe){
			ioe.printStackTrace();
		}

		return exportPath;
	}

	private void setOutputQuality(IActiveView activeView, long imgResampleRatio){
		try{
			IOutputRasterSettings outputRasterSettings = (IOutputRasterSettings) activeView.getScreenDisplay().getDisplayTransformation();
			outputRasterSettings.setResampleRatio((int)imgResampleRatio);
		} catch(IOException ioe){
			ioe.printStackTrace();
		}

	}

}
