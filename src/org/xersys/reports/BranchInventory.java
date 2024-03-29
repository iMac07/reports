package org.xersys.reports;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
import net.sf.jasperreports.engine.data.JsonDataSource;
import net.sf.jasperreports.view.JasperViewer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.xersys.commander.iface.XNautilus;
import org.xersys.commander.iface.XReport;
import org.xersys.commander.util.SQLUtil;

public class BranchInventory implements XReport{
    private final String REPORTID = "220004";
    private final String REPORT_PATH = "/reports/";
    
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
        
        System.out.println(getReportSQL());
        ResultSet rs = p_oNautilus.executeQuery(getReportSQL());
        
        //Convert the data-source to JasperReport data-source
        //JRResultSetDataSource jrRS = new JRResultSetDataSource(rs);
        
        //Create the parameter
        Map<String, Object> params = new HashMap<>();
        params.put("sCompnyNm", System.getProperty("store.company.name"));  
        params.put("sBranchNm", (String) p_oNautilus.getBranchConfig("sCompnyNm"));
        params.put("sAddressx", (String) p_oNautilus.getBranchConfig("sAddressx") + ", " + (String) p_oNautilus.getBranchConfig("xTownName"));      
        params.put("sReportNm", System.getProperty("store.report.header"));      
        params.put("sPrintdBy", (String) p_oNautilus.getUserInfo("xClientNm"));
        
        try {
//            _jrprint = JasperFillManager.fillReport((String) p_oNautilus.getAppConfig("sApplPath") + REPORT_PATH +
//                                                    System.getProperty("store.report.file"),
//                                                    params, 
//                                                    jrRS);
            JSONArray json_arr = new JSONArray();
            json_arr.clear();
            
            JSONObject json_obj;
            
            int lnCtr = 1;
            
            while (rs.next()){
                json_obj = new JSONObject();
                
                json_obj.put("nField01", lnCtr);
                json_obj.put("sField01", rs.getString("sField01").toUpperCase());
                json_obj.put("sField02", rs.getString("sField02").toUpperCase());
                json_obj.put("sField03", rs.getString("sField03").toUpperCase());
                json_obj.put("sField04", rs.getString("sField04").toUpperCase());
                json_obj.put("sField05", rs.getString("sField05").toUpperCase());
                json_obj.put("sField06", rs.getString("sField06").toUpperCase());
                json_obj.put("nField02", rs.getInt("nField02"));
                json_obj.put("lField01", rs.getDouble("lField01"));
                json_obj.put("lField02", rs.getDouble("lField02"));
                json_obj.put("nField03", rs.getInt("nField03"));
                json_obj.put("lField03", rs.getDouble("lField03"));
                
                lnCtr++;
                json_arr.add(json_obj); 
            }
            
            InputStream stream = new ByteArrayInputStream(json_arr.toJSONString().getBytes("UTF-8"));
            JsonDataSource jrjson = new JsonDataSource(stream); 

            _jrprint = JasperFillManager.fillReport(
                            (String) p_oNautilus.getAppConfig("sApplPath") + REPORT_PATH +
                            System.getProperty("store.report.file"), params, jrjson);
            
            //JasperViewer jv = new JasperViewer(_jrprint, false);
            //jv.setVisible(true);
            
        } catch (SQLException | JRException | UnsupportedEncodingException ex) {
            ex.printStackTrace();        
        }
        
        return _jrprint;
    }
    
    private String getReportSQL(){
        return "SELECT" +
                    "  b.sBarCodex sField01" +
                    ", b.sDescript sField02" +
                    ", IFNULL(c.sDescript, '') sField03" +
                    ", 'MODEL' sField04" +
                    ", 'SIZE' sField05" +
                    ", 'OUM' sField06" +
                    ", a.nQtyOnHnd nField02" +
                    ", b.nSelPrce1 lField01" +
                    ", a.nQtyOnHnd * b.nSelPrce1 lField02" +
                    ", 0 nField03" +
                    ", b.nUnitPrce lField03" +	
                " FROM Inv_Master a" +
                    ", Inventory b" +
                    " LEFT JOIN Brand c ON b.sBrandCde = c.sBrandCde AND c.sInvTypCd = 'SP'" +
                " WHERE a.sStockIDx = b.sStockIDx" +
                    " AND a.sBranchCd = " + SQLUtil.toSQL((String) p_oNautilus.getBranchConfig("sBranchCd")) +
                    " AND a.cRecdStat = '1'" +
                    " AND a.nQtyOnHnd > 0" +
                " ORDER BY b.sBrandCde, b.sBarCodex, b.sDescript, a.nQtyOnHnd";
    }

    @Override
    public String getMessage() {
        return p_sMessagex;
    }
}
