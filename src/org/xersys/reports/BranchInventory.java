package org.xersys.reports;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;
import org.xersys.commander.iface.XNautilus;
import org.xersys.commander.iface.XReport;
import org.xersys.commander.util.SQLUtil;

public class BranchInventory implements XReport{
    private final String FILTER_ID = "RPT220004";
    private final String REPORT_PATH = "/report/";
    
    private XNautilus p_oNautilus;
    
    private String p_sMessagex; 
    
    private boolean p_bHasPreview;
    
    private JasperPrint _jrprint;
    private LinkedList _rptparam = null;
    
    public BranchInventory(){
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
        return FILTER_ID;
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
                    return printSummary();
                case 2: 
                    return printDetail();
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
        params.put("sBranchNm", (String) p_oNautilus.getBranchConfig("sBranchNm"));
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
                    "  b.sBarCodex" +
                    ", b.sDescript" +
                    ", c.sDescript xBrandNme" +
                    ", a.cClassify" +
                    ", a.nMaxLevel" +
                    ", a.nMinLevel" +
                    ", b.nSelPrce1" +
                    ", a.nQtyOnHnd" +
                    ", b.nUnitPrce" +	
                " FROM Inv_Master a" +
                    " LEFT JOIN Inventory b ON a.sStockIDx = b.sStockIDx" +
                    " LEFT JOIN Brand c ON b.sBrandCde = c.sBrandCde AND c.sInvTypCd = 'SP'" +
                " WHERE a.sBranchCd = " + SQLUtil.toSQL((String) p_oNautilus.getBranchConfig("sBranchCd")) +
                    " AND a.cRecdStat = '1'" +
                " ORDER BY a.nQtyOnHnd DESC, c.sDescript, b.sBarCodex, b.sDescript";
    }
}
