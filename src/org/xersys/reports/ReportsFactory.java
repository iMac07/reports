package org.xersys.reports;

import org.xersys.commander.iface.XReport;

public class ReportsFactory {
    public static XReport make(String fsReportID){
        switch (fsReportID) {
            case "220001": //Purchase Order
                return new PurchaseOrder();
            case "220002": //Delivery Acceptance
                return new DeliveryAcceptance();
            case "220004": //Branch Inventory
                return new BranchInventory();
            case "220011": //Branch Inventory w/o SRP
                return new BranchInventoryNoSRP();
            default:
                return null;
        }
    }
}
