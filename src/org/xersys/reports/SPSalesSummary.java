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

public class SPSalesSummary implements XReport{
    private final String REPORTID = "220010";
    private final String REPORT_PATH = "/reports/";
    
    private XNautilus p_oNautilus;
    
    private String p_sMessagex; 
    
    private boolean p_bHasPreview;
    
    private JasperPrint _jrprint;
    private LinkedList _rptparam = null;
    
    private double xOffset = 0; 
    private double yOffset = 0;
    
    public SPSalesSummary(){
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
        //return null;
        _jrprint = null;
        
        //String lsCondition = " ";
        //String lsSQL = MiscUtil.addCondition(getReportSQLMaster(), lsCondition);
        
        String lsSQL = getReportSQLMaster();
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
        
        String lsCondition = "a.dTransact BETWEEN " + SQLUtil.toSQL(System.getProperty("store.report.criteria.date.from") + " 00:00:01" ) +
                                " AND " + SQLUtil.toSQL(System.getProperty("store.report.criteria.date.thru")+ " 23:59:00" );
        
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
                    "  e.sInvNumbr sField01" +
                    ", DATE_FORMAT(a.dTransact, '%Y-%m-%d') sField02" +
                    ", d.sBarCodex sField03" +
                    ", d.sDescript sField04" +
                    ", d.sBrandCde sField05" +
                    ", c.nQuantity nField01" +
                    ", c.nUnitPrce lField01" +
                    ", c.nQuantity * ((c.nUnitPrce) * (c.nDiscount / 100) + c.nAddDiscx)  lField02" +
                    ", (c.nQuantity * c.nUnitPrce) - c.nQuantity * ((c.nUnitPrce) * (c.nDiscount / 100) + c.nAddDiscx) lField03" +
                " FROM SP_Sales_Master a" +
                        " LEFT JOIN Sales_Invoice e" +
                            " ON a.sTransNox = e.sSourceNo" +
                                " AND e.sSourceCd = 'SO'" +
                    ", SP_Sales_Detail c" +
                    ", Inventory d " +
                " WHERE a.sTransNox = c.sTransNox" +
                    " AND c.sStockIDx = d.sStockIDx" +
                    " AND a.cTranStat <> '3'" +
                    " AND a.sBranchCd = " + SQLUtil.toSQL((String) p_oNautilus.getBranchConfig("sBranchCd"));
    }

    private String getReportSQLMaster(){
        return "SELECT" +
                    "  c.sClientNm sField01" +
                    ", a.dTransact sField02" +
                    ", c.sInvNumbr sField03" +
                    ", a.nTranTotl lField01" +
                    ", a.nVATAmtxx lField02" +
                    ", (a.nDiscount + a.nAddDiscx) lField03" +
                    ", a.nAmtPaidx lField04" +
                    ", CASE a.cTranStat" +
                        " WHEN '0' THEN 'OPEN'" +
                        " WHEN '1' THEN 'CLOSED'" +
                        " WHEN '2' THEN 'POSTED'" +
                        " WHEN '3' THEN 'CANCELLED'" +
                        " WHEN '4' THEN 'VOID'" +
                        " END sField04" +
                " FROM SP_Sales_Master a" +
                    " LEFT JOIN Client_Master b ON a.sClientID = b.sClientID" +
                    " LEFT JOIN Sales_Invoice c" +
                        " ON a.sTransNox = c.sSourceNo" +
                            " AND c.sSourceCd = 'SO'" +
                " WHERE a.sBranchCd = " + SQLUtil.toSQL((String) p_oNautilus.getBranchConfig("sBranchCd")) +
                    " AND a.cTranStat <> 3" +
                    " AND a.dTransact BETWEEN " + SQLUtil.toSQL(System.getProperty("store.report.criteria.date.from") + " 00:00:01" ) +
                        " AND " + SQLUtil.toSQL(System.getProperty("store.report.criteria.date.thru")+ " 23:59:00" ) +
                " GROUP BY c.sInvNumbr";
    }

    @Override
    public String getMessage() {
        return p_sMessagex;
    }
}

