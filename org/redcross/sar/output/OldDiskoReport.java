package org.redcross.sar.output;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanArrayDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.view.JasperViewer;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.IDiskoMapManager;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IPOIListIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitListIf;
import org.redcross.sar.wp.tactics.DiskoWpTacticsImpl;

import com.esri.arcgis.carto.IActiveView;
import com.esri.arcgis.display.IOutputRasterSettings;
import com.esri.arcgis.display.tagRECT;
import com.esri.arcgis.geometry.Envelope;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.output.ExportPNG;
import com.esri.arcgis.output.IExport;

public class OldDiskoReport {
	
	private IDiskoApplication app = null;
	private String reportTemplate_path = null;
	private String reportPrint_path = null;
		
	private MsoModelImpl m_msoModel = null;
	private IActiveView activeView = null;
		
	private DiskoMap diskoMap_print = null;
	private IDiskoMapManager mapManager = null;
	
	private final static String outputPrintFormat = ".PNG";
	
	private double reportMapScale;	
	private final static double mapPrintWidthSize = 0.169;//print map size, meters. i iReport tilsvarer det 487
	private final static double mapPrintHeigthSize = 0.169;//print map size, meters. i iReport tilsvarer det 487. pixelsize = 0.000037 meter.
	 
	//test
	private com.esri.arcgis.carto.Map cartoMap = null;
	//private IMap 
	private ISpatialReference srs = null;
	private String timeNow = new String();
	public static final String DATE_FORMAT_NOW = "ddHHmm";
	
	private DiskoWpTacticsImpl wp = null;
	
	public OldDiskoReport(IDiskoApplication app){
		System.out.println("test");
		this.app = app;
		
		m_msoModel = (MsoModelImpl) app.getMsoModel();	
		this.reportMapScale = Double.parseDouble(app.getProperty("report.mapscale"));
				
		mapManager = app.getMapManager();

		
		this.reportTemplate_path = app.getProperty("report.template.path");
		this.reportPrint_path = app.getProperty("report.printfile.path");	
		
	}	
	
	/**
	 * 
	 *
	 */
	public void compile(String jrxmlFileName, String jasperFileName){
		System.out.println("DiskoReport: compile()");		
		try{			
			JasperCompileManager.compileReportToFile(this.reportTemplate_path + jrxmlFileName, this.reportTemplate_path + jasperFileName);			
		}		
		catch(JRException jre){
			jre.printStackTrace();
			System.out.println("Kompilering av .jrxml fil feilet");
		}		
		
		
	}
	
	/**
	 * Assignment report
	 * @param assignments
	 */
	public void printAssignments(List<IAssignmentIf> assignments){		
		
		
		String jasperFileName = app.getProperty("report.TACTICS_TEMPLATE")+".jasper";
		//inntil videre kjøres kompilering også
		
		//String jrxmlFileName = app.getProperty("report.TACTICS_TEMPLATE")+".jrxml";
		//compile(jrxmlFileName, jasperFileName);	
		
		
		//lopper igjennom oppdrag som skal printes. Foreløpig bare èn
		IAssignmentIf assignment = null;
		IPOIListIf poiList = null;
		String jasperFile = app.getProperty("report.TACTICS_TEMPLATE")+".jasper";
		
		if(srs == null){
			try{
				srs = diskoMap_print.getSpatialReference();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		for (int i=0;i<assignments.size(); i++){
			assignment = assignments.get(i);
			makePrintMap(assignment);
			try{
				activeView = diskoMap_print.getActiveView();
			}
			catch(IOException ioe){
				ioe.printStackTrace();
			}
			
			//eksportere mapobject
			String exportMapPath = exportMap();
			
			//ekstraherer verdrier fra assignmentslista
			Map assignmentsMap = extractAssignmentValues(assignment, exportMapPath);
						
			
			JasperPrint jPrint = fill(jasperFileName, assignmentsMap);
			
			//preview				
			view(jPrint);		
		}
		
	}
	
	public void printUnitLog(IUnitIf unit){		
				
		String jasperFileName = app.getProperty("report.UNITLOG_TEMPLATE")+".jasper";
		//inntil videre kjøres kompilering også
		
		//String jrxmlFileName = app.getProperty("report.UNITLOG_TEMPLATE")+".jrxml";
		//compile(jrxmlFileName, jasperFileName);
		
		
		ICmdPostIf cmdPost = app.getMsoModel().getMsoManager().getCmdPost();
		IUnitListIf unitList = cmdPost.getUnitList();		
				
		UnitlogReportParams unitLogParams = new UnitlogReportParams();
		//ekstrahere unit verdier
		Map unitsMap = unitLogParams.getUnitlogReportParams(unitList, unit);
				
		JasperPrint jPrint = fill(jasperFileName, unitsMap);
		
		//preview				
		view(jPrint);	
		
	}
			
	private JasperPrint fill(String jasperFileName, Map map){
		File sourceFile = null;
		File destFile = null;
		JasperPrint jasperPrint = null;
		try{			
			sourceFile = new File(reportTemplate_path + jasperFileName);
			
			//System.out.println("skal lese jasper fil..." + sourceFile.getPath() + ", " + sourceFile );
			JasperReport jasperReport = (JasperReport)JRLoader.loadObject(sourceFile);
			jasperPrint = JasperFillManager.fillReport(jasperReport, map, new JREmptyDataSource());				
											
			destFile = new File(reportPrint_path, jasperReport.getName() + ".jrprint");
			JRSaver.saveObject(jasperPrint, destFile);									
			
		} catch(JRException jre){
			jre.printStackTrace();
		}
		catch (Exception e){
			e.printStackTrace();
		}		
		return jasperPrint;
	}
	
	
	
	private void view(JasperPrint jrprintFile){		
		System.out.println("DiskoReport: view() " + jrprintFile);
		try{
			JasperViewer.viewReport(jrprintFile, false, null);
		}
		catch(Exception e){
			e.printStackTrace();
			
		}
	}
		
	private Map extractAssignmentValues(IAssignmentIf assignment, String exportMapPath){
		Map<String, Object> map = new HashMap<String,Object>();
		
		String role_name = app.getCurrentRole().getTitle() + " (" + app.getCurrentRole().getName()+ ")";
		
		//legg inn datoTidsgruppe
		timeNow = getTimeNow();
		
		MissionOrderReportParams missionOrderPrint = new MissionOrderReportParams();
		map = missionOrderPrint.setPrintParams(assignment, exportMapPath, role_name, reportTemplate_path, srs, reportMapScale, timeNow);
		
		return map;
		
	}
	
	private void makePrintMap(IAssignmentIf assignment){
		try{			
			//lager nytt kart				
			//diskoMap_print = new DiskoMap(diskoMap.getMxdDoc(), diskoMap.getMapManager(), app.getMsoModel());	
			
			diskoMap_print = (DiskoMap) mapManager.getPrintMap();
			
			//zoome til MSO object og sett riktig skala
			diskoMap_print.zoomToPrintMapExtent(assignment, reportMapScale, mapPrintHeigthSize, mapPrintWidthSize);			
			
			//sette aktivt object selected
			diskoMap_print.setSelected(assignment, true);
			
			//fjerne andre MSO assignment objekter i kartet.
			//todo			
			/* koden under funker ikke...
			for (int i = 0; i < diskoMap_print.getLayerCount(); i++){				
				ILayer layer = diskoMap_print.getLayer(i);
				if (layer instanceof AreaLayer){
					diskoMap_print.deleteLayer(i);
					//create IMso
					System.out.println("test");
					AreaLayer newLayer =  new AreaLayer(m_msoModel);
					newLayer.createMsoFeature(assignment);
				}
				
			}
			*/
			
						
			//for å legge på grid må det lages et Carto objekt
			//todo
			/*
			try{
				cartoMap = (com.esri.arcgis.carto.Map) diskoMap_print.getMap();
				PageLayout pageLayout = (PageLayout) diskoMap_print.getMap();
				//cartoMap.getAsIGraphicsContainer().findFrame();
				
				IFrameElement frameElement = pageLayout.findFrame(diskoMap_print);
							
				//lage grid
				MeasuredGrid mapGrid = new MeasuredGrid();				
				//populere grid med properties
				IProjectedGrid projGrid = (IProjectedGrid) cartoMap.getSpatialReference();
				
				//mapGrid.setSpatialReferenceByRef(projGrid);
								
				//cartoMap.setSpatialReferenceByRef();
			}
			catch(IOException ioe){
				System.out.println("funker ikke");
				ioe.printStackTrace();
			}
			*/			
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
	
	private String exportMap(){
		System.out.println("exportMap()");
		long imgResolution = 100;
		long imgResampleRatio = 1;
		long hdc;
		tagRECT exportRECT = new tagRECT();
		String outputDir = app.getProperty("report.output.path");
		long randomNumber = ((long)(Math.random() * 100000));
		System.out.println("Math.random(): " + Math.random() + "randomNumber: " + randomNumber);
		String outputFileName = app.getProperty("report.output.printname") + randomNumber;
		String exportPath = outputDir+outputFileName+outputPrintFormat;
		IExport docExport = null;
		IEnvelope pixelBoundsEnv = null;
		try{
			docExport = new ExportPNG();
			
			//set export filename
			System.out.println(exportPath);
	        docExport.setExportFileName(exportPath);
	        
	        //set outputResolution
	        docExport.setResolution(imgResolution);
	        			
			//always set the output quality of the DISPLAY to 1 for image export formats
	        setOutputQuality(activeView, imgResampleRatio);		
	       	        	        			
	        //Assign envelope object to the export object
	        pixelBoundsEnv = new Envelope();
	        exportRECT.bottom = 500;
	        exportRECT.right = 500;
	        exportRECT.top = 0;
	        exportRECT.left = 0;
	        pixelBoundsEnv.putCoords(exportRECT.left, exportRECT.top, exportRECT.right, exportRECT.bottom);
	       
	        docExport.setPixelBounds(pixelBoundsEnv);
	       
	        //ready to export
	        hdc = docExport.startExporting();
	        
	        //Redraw the active view, rendering it to the exporter object
	        activeView.output((int) hdc, (int)docExport.getResolution(), exportRECT, null, null);
	        
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
	
	private String getTimeNow(){
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
	    return sdf.format(cal.getTime());
	}
	

}
