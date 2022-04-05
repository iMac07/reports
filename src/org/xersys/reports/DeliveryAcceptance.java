package org.xersys.reports;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.xersys.commander.iface.XNautilus;
import org.xersys.commander.iface.XReport;
import org.xersys.commander.util.SQLUtil;

public class DeliveryAcceptance implements XReport{
    private final String REPORTID = "220002";
    private final String REPORT_PATH = "/reports/";
    
    private XNautilus p_oNautilus;
    
    private String p_sMessagex; 
    
    private boolean p_bHasPreview;
    
    private JasperPrint _jrprint;
    private LinkedList _rptparam = null;
    
    public DeliveryAcceptance(){
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
        _rptparam.add("store.report.criteria.date"); 
        
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
        //dito lalabas yung criteria form pagkinakailangan
        return true;
    }

    @Override
    public JasperPrint processReport() {
        boolean bResult = false;
        
        if(System.getProperty("store.report.criteria.presentation").equals("1")){
            System.setProperty("store.report.no", "1");
         }else{
            System.setProperty("store.report.no", "2");
        }
        
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
        System.clearProperty("store.report.criteria.date");   
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
        return null;
    }
    
    private JasperPrint printDetail(){
        System.out.println("Printing Detailed");
        
        _jrprint = null;
        
        ResultSet rs = p_oNautilus.executeQuery(getReportSQL());
        
        //Convert the data-source to JasperReport data-source
        JRResultSetDataSource jrRS = new JRResultSetDataSource(rs);
        
        //Create the parameter
        Map<String, Object> params = new HashMap<>();
        params.put("sCompnyNm", "Company Name");  
        params.put("sBranchNm", (String) p_oNautilus.getBranchConfig("sCompnyNm"));
        params.put("sAddressx", (String) p_oNautilus.getBranchConfig("sAddressx") + ", " + (String) p_oNautilus.getBranchConfig("xTownName"));      
        params.put("sReportNm", System.getProperty("store.report.header"));      
        params.put("sPrintdBy", (String) p_oNautilus.getUserInfo("xClientNm"));
        
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
    
    private String getReportSQL(){
        return "SELECT" +
                    "  b.sClientNm sField01" +
                    ", a.dRefernce sField02" +
                    ", a.sReferNox sField03" +
                    ", d.sBarCodex sField04" +
                    ", d.sDescript sField05" +
                    ", c.nQuantity nField01" +
                    ", c.nUnitPrce lField01" +
                    ", CASE cTranStat" +
                        " WHEN '0' THEN 'OPEN'" +
                        " WHEN '1' THEN 'CLOSED'" +
                        " WHEN '2' THEN 'POSTED'" +
                        " WHEN '3' THEN 'CANCELLED'" +
                        " WHEN '4' THEN 'VOID'" +
                        " END sField06" +
                    ", a.sTransNox sField07" +
                " FROM PO_Receiving_Master a" +
                        " LEFT JOIN Client_Master b ON a.sSupplier = b.sClientID" +
                    ", PO_Receiving_Detail c" +
                        " LEFT JOIN Inventory d ON c.sStockIDx = d.sStockIDx" +
                " WHERE a.sTransNox = c.sTransNox" +
                    " AND a.sBranchCd = " + SQLUtil.toSQL((String) p_oNautilus.getBranchConfig("sBranchCd"));
    }

    @Override
    public String getMessage() {
        return p_sMessagex;
    }
}
