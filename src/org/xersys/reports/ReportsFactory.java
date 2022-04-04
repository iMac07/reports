package org.xersys.reports;

import org.xersys.commander.iface.XReport;

public class ReportsFactory {
    public static XReport make(String fsReportID){
        fsReportID = "RPT" + fsReportID;
        
        switch (fsReportID) {
            case "220004": //Branch Invntory
                return new BranchInventory();
            default:
                return null;
        }
    }
}
