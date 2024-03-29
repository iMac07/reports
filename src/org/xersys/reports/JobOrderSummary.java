package org.xersys.reports;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.xersys.commander.iface.XNautilus;
import org.xersys.commander.iface.XReport;
import org.xersys.commander.util.MiscUtil;
import org.xersys.commander.util.SQLUtil;

public class JobOrderSummary implements XReport{
    private final String REPORTID = "230002";
    private final String REPORT_PATH = "/reports/";
    
    private XNautilus p_oNautilus;
    
    private String p_sMessagex; 
    
    private boolean p_bHasPreview;
    
    private JasperPrint _jrprint;
    private LinkedList _rptparam = null;
    
    private double xOffset = 0; 
    private double yOffset = 0;
    
    public JobOrderSummary(){
        _rptparam = new LinkedList();
        _rptparam.add("store.report.id");
        _rptparam.add("store.report.no");
        _rptparam.add("store.report.name");
        _rptparam.add("store.report.jar");
        _rptparam.add("store.report.class");
        _rptparam.add("store.report.is_save");
        _rptparam.add("store.report.is_log");
        
        _rptparam.add("store.report.criteria.presentation");
        _rptparam.add("store.report.criteria.branch");      
        _rptparam.add("store.report.criteria.group");        
        _rptparam.add("store.report.criteria.date.from"); 
        _rptparam.add("store.report.criteria.date.thru"); 
        
        _jrprint = null;
        
        clearProperties();
        
        System.setProperty("store.report.id", REPORTID);
    }
    
    @Override
    public void setNautilus(XNautilus foValue) {
        p_oNautilus = foValue;
    }

    @Override
    public void hasPreview(boolean fbValue) {
        p_bHasPreview = fbValue;
    }

    @Override
    public void list() {
        _rptparam.forEach(item->System.out.println(item));
    }

    @Override
    public String getFilterID() {
        return REPORTID;
    }
    
    @Override
    public boolean getParam() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("DateCriteria.fxml"));
        fxmlLoader.setLocation(getClass().getResource("DateCriteria.fxml"));

        DateCriteriaController instance = new DateCriteriaController();
        instance.setNautilus(p_oNautilus);
        
        try {
            
            fxmlLoader.setController(instance);
            Parent parent = fxmlLoader.load();
            Stage stage = new Stage();

            /*SET FORM MOVABLE*/
            parent.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    xOffset = event.getSceneX();
                    yOffset = event.getSceneY();
                }
            });
            parent.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    stage.setX(event.getScreenX() - xOffset);
                    stage.setY(event.getScreenY() - yOffset);
                }
            });
            /*END SET FORM MOVABLE*/

            Scene scene = new Scene(parent);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setAlwaysOnTop(true);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            p_sMessagex = e.getMessage();
            return false;
        }
        
        if (!instance.isCancelled()){
            System.setProperty("store.report.criteria.date.from", instance.getDateFrom());
            System.setProperty("store.report.criteria.date.thru", instance.getDateThru());
            return true;
        }
        
        return false; 
    }

    @Override
    public JasperPrint processReport() {
        boolean bResult = false;
        
        if(System.getProperty("store.report.criteria.presentation").equals("1")){
            System.setProperty("store.report.no", "1"); //detailed
        }else{
            System.setProperty("store.report.no", "2"); //summarized
        }
        //3 - global
        //4 - chart
        
        //Load the jasper report to be use by this object
        String lsSQL = "SELECT sFileName, sReportHd" + 
                      " FROM xxxReportDetail" + 
                      " WHERE sReportID = " + SQLUtil.toSQL(System.getProperty("store.report.id")) +
                        " AND nEntryNox = " + SQLUtil.toSQL(System.getProperty("store.report.no"));
        
        ResultSet loRS = p_oNautilus.executeQuery(lsSQL);
        
        try {
            if(!loRS.next()){
                p_sMessagex = "Invalid report was detected...";
                closeReport();
                return null;
            }
            
            System.setProperty("store.report.file", loRS.getString("sFileName"));
            System.setProperty("store.report.header", loRS.getString("sReportHd"));
            
            switch(Integer.valueOf(System.getProperty("store.report.no"))){
                case 1:
                    return printDetail();
                case 2: 
                    return printSummary();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void clearProperties(){          
        System.clearProperty("store.report.id");
        System.clearProperty("store.report.no");
        System.clearProperty("store.report.name");
        System.clearProperty("store.report.jar");
        System.clearProperty("store.report.class");
        System.clearProperty("store.report.is_save");
        System.clearProperty("store.report.is_log");
        System.clearProperty("store.report.criteria.presentation");
        System.clearProperty("store.report.criteria.branch");      
        System.clearProperty("store.report.criteria.group");        
        System.clearProperty("store.report.criteria.date.from");   
        System.clearProperty("store.report.criteria.date.thru");   
    }
    
    private void closeReport(){
        _rptparam.forEach(item->System.clearProperty((String) item));
        System.clearProperty("store.report.file");
        System.clearProperty("store.report.header");
    }
    
    private void logReport(){
        _rptparam.forEach(item->System.clearProperty((String) item));
        System.clearProperty("store.report.file");
        System.clearProperty("store.report.header");
    }
    
    private JasperPrint printSummary(){
        System.out.println("Printing Summary");
        _jrprint = null;
        
        String lsCondition = "a.dTransact BETWEEN " + SQLUtil.toSQL(System.getProperty("store.report.criteria.date.from") + " 00:00:01") +
                                " AND " + SQLUtil.toSQL(System.getProperty("store.report.criteria.date.thru") + " 23:59:59");
        
        String lsSQL = MiscUtil.addCondition(getReportSQLMaster(), lsCondition);
        
        ResultSet rs = p_oNautilus.executeQuery(lsSQL);
        
        //Convert the data-source to JasperReport data-source
        JRResultSetDataSource jrRS = new JRResultSetDataSource(rs);
        
        //Create the parameter
        Map<String, Object> params = new HashMap<>();
        params.put("sCompnyNm", System.getProperty("store.company.name"));  
        params.put("sBranchNm", (String) p_oNautilus.getBranchConfig("sCompnyNm"));
        params.put("sAddressx", (String) p_oNautilus.getBranchConfig("sAddressx") + ", " + (String) p_oNautilus.getBranchConfig("xTownName"));      
        params.put("sReportNm", System.getProperty("store.report.header"));      
        params.put("sPrintdBy", (String) p_oNautilus.getUserInfo("xClientNm"));
        params.put("sReportDt", System.getProperty("store.report.criteria.date.from") +
                                " to " + System.getProperty("store.report.criteria.date.thru"));
         
        try {
            _jrprint = JasperFillManager.fillReport((String) p_oNautilus.getAppConfig("sApplPath") + REPORT_PATH +
                                                    System.getProperty("store.report.file"),
                                                    params, 
                                                    jrRS);
        } catch (JRException ex) {
            ex.printStackTrace();
        }
        
        return _jrprint;
    }
    
    private JasperPrint printDetail(){
        System.out.println("Printing Detailed");
        _jrprint = null;
        
        String lsCondition = "a.dTransact BETWEEN " + SQLUtil.toSQL(System.getProperty("store.report.criteria.date.from") + " 00:00:01") +
                                " AND " + SQLUtil.toSQL(System.getProperty("store.report.criteria.date.thru") + " 23:59:59");
        
        String lsSQL = MiscUtil.addCondition(getReportSQLDetail(), lsCondition);
        
        ResultSet rs = p_oNautilus.executeQuery(lsSQL);
        
        //Convert the data-source to JasperReport data-source
        JRResultSetDataSource jrRS = new JRResultSetDataSource(rs);
        
        //Create the parameter
        Map<String, Object> params = new HashMap<>();
        params.put("sCompnyNm", System.getProperty("store.company.name"));  
        params.put("sBranchNm", (String) p_oNautilus.getBranchConfig("sCompnyNm"));
        params.put("sAddressx", (String) p_oNautilus.getBranchConfig("sAddressx") + ", " + (String) p_oNautilus.getBranchConfig("xTownName"));      
        params.put("sReportNm", System.getProperty("store.report.header"));      
        params.put("sPrintdBy", (String) p_oNautilus.getUserInfo("xClientNm"));
        params.put("sReportDt", System.getProperty("store.report.criteria.date.from") +
                                " to " + System.getProperty("store.report.criteria.date.thru"));
         
        try {
            _jrprint = JasperFillManager.fillReport((String) p_oNautilus.getAppConfig("sApplPath") + REPORT_PATH +
                                                    System.getProperty("store.report.file"),
                                                    params, 
                                                    jrRS);
        } catch (JRException ex) {
            ex.printStackTrace();
        }
        
        return _jrprint;
    }
    
    private String getReportSQLDetail(){
        return "SELECT" +
                    "  DATE_FORMAT(a.dTransact, '%Y-%m-%d') sField01" +
                    ", d.sClientNm sField02" +	
                    ", f.sClientNm sField03" +
                    ", c.sDescript sField04" +
                    ", b.nQuantity nField01" +
                    ", (b.nUnitPrce - (b.nUnitPrce * b.nDiscount / 100) - b.nAddDiscx) lField01" +
                    ", b.nQuantity * (b.nUnitPrce - (b.nUnitPrce * b.nDiscount / 100) - b.nAddDiscx) lField02" +
                    ", b.nUnitPrce" +
                    ", b.nDiscount" +
                    ", b.nAddDiscx" +	
                    ", e.sSerial01" +
                " FROM Job_Order_Master a" +
                    " LEFT JOIN Job_Order_Detail b ON a.sTransNox = b.sTransNox" +
                    " LEFT JOIN Labor c ON b.sLaborCde = c.sLaborCde" +
                    " LEFT JOIN Client_Master d ON a.sMechanic = d.sClientID" +
                    " LEFT JOIN Inv_Serial e ON a.sSerialID = e.sSerialID" +
                    " LEFT JOIN Client_Master f ON a.sClientID = f.sClientID" +
                " WHERE LEFT(a.sTransNox, 4) = " + SQLUtil.toSQL((String) p_oNautilus.getBranchConfig("sBranchCd")) +
                    " AND a.cTranStat = '2'";
    }

    private String getReportSQLMaster(){
        return "SELECT" + 
                    "  d.sClientNm sField01" + 	
                    ", COUNT(a.sTransNox) nField01" +
                    ", SUM(b.nQuantity * (b.nUnitPrce - (b.nUnitPrce * b.nDiscount / 100) - b.nAddDiscx)) lField01" + 
                " FROM Job_Order_Master a" + 
                    " LEFT JOIN Job_Order_Detail b ON a.sTransNox = b.sTransNox" + 
                    " LEFT JOIN Labor c ON b.sLaborCde = c.sLaborCde" + 
                    " LEFT JOIN Client_Master d ON a.sMechanic = d.sClientID" + 
                    " LEFT JOIN Inv_Serial e ON a.sSerialID = e.sSerialID" + 
                    " LEFT JOIN Client_Master f ON a.sClientID = f.sClientID" + 
                " WHERE LEFT(a.sTransNox, 4) = " + SQLUtil.toSQL((String) p_oNautilus.getBranchConfig("sBranchCd")) +
                    " AND a.cTranStat = '2'" +
                " GROUP BY sField01";
    }

    @Override
    public String getMessage() {
        return p_sMessagex;
    }
}