package org.xersys.reports;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.xersys.commander.iface.XNautilus;
import org.xersys.commander.iface.XReport;
import org.xersys.commander.util.MiscUtil;

public class ReportMaster {
    private XNautilus p_oNautilus;
    private String p_sMessagex;
    
    private CachedRowSet p_oMaster;
    private XReport p_oDetail;
    
    public ReportMaster(XNautilus foValue){
        p_oNautilus = foValue;
        
        p_oMaster = null;
        p_oDetail = null;
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
