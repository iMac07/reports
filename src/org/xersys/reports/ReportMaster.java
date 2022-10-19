package org.xersys.reports;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.xersys.commander.iface.XNautilus;
import org.xersys.commander.iface.XReport;
import org.xersys.commander.util.MiscUtil;

public class ReportMaster {
    public final String REPORT_PATH = "/reports/";
    
    private XNautilus p_oNautilus;
    private String p_sMessagex;
    
    private CachedRowSet p_oMaster;
    private XReport p_oDetail;
    
    private LinkedList _rptparam = null;
    
    public ReportMaster(XNautilus foValue){
        p_oNautilus = foValue;
        
        p_oMaster = null;
        p_oDetail = null;
        
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
        
        clearProperties();
    }
    
    public void clearProperties(){          
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
    
    public int getItemCount() {
        try {
            p_oMaster.last();
            return p_oMaster.getRow();
        } catch (SQLException e) {
            e.printStackTrace();
            p_sMessagex = e.getMessage();
            return -1;
        }
    }
    
    public String getMessage(){
        return p_sMessagex;
    }
    
    public Object getMaster(int fnRow, String fsFieldNm) {
        try {
            p_oMaster.first();
            
            return getMaster(fnRow, MiscUtil.getColumnIndex(p_oMaster, fsFieldNm));
        } catch (SQLException e) {
            e.printStackTrace();
            p_sMessagex = e.getMessage();
        }
        
        return null;
    }

    public Object getMaster(int fnRow, int fnIndex) {
        try {
            p_oMaster.absolute(fnRow + 1);
            
            return p_oMaster.getObject(fnIndex);
        } catch (SQLException e) {
            e.printStackTrace();
            p_sMessagex = e.getMessage();
        }
        
        return null;
    }
    
    public boolean LoadReports(){
        if (p_oNautilus == null){
            p_sMessagex = "Application driver is not set.";
            return false;
        }
        
        try {
            String lsSQL;
            ResultSet loRS;

            RowSetFactory factory = RowSetProvider.newFactory();

            //create empty master record
            lsSQL = getSQ_Master();
            loRS = p_oNautilus.executeQuery(lsSQL);
            p_oMaster = factory.createCachedRowSet();
            p_oMaster.populate(loRS);
            MiscUtil.close(loRS);            
        } catch (Exception e) {
            e.printStackTrace();
            p_sMessagex = e.getMessage();
            return false;
        }
        
        return true;
    }
    
    public boolean LoadDetail(){
        return true;
    }
    
    private String getSQ_Master(){
        String lsSQL = "SELECT" +
                            "  sReportID" +
                            ", sReportNm" +
                            ", sProdctID" +
                            ", nUserRght" +
                            ", nObjAcces" +
                            ", cSaveRepx" +
                            ", cLogRepxx" +
                            ", sRepLibxx" +
                            ", sRepClass" +
                        " FROM xxxReportMaster" +
                        " ORDER BY sReportNm";
        
        lsSQL = MiscUtil.addCondition(lsSQL, "nUserRght & " + (int) p_oNautilus.getUserInfo("nUserLevl") + " != 0");
        lsSQL = MiscUtil.addCondition(lsSQL, "nObjAcces & " + (int) p_oNautilus.getUserInfo("nObjAcces") + " != 0");
    
        return lsSQL;
    }
}
