package org.xersys.reports;

import org.xersys.commander.iface.XReport;

public class ReportsFactory {
    public static XReport make(String fsReportID){
        switch (fsReportID) {
            case "220001": //Purchase Order
                return new PurchaseOrder();
            case "220002": //Delivery Acceptance
                return new DeliveryAcceptance();
            case "220003": //Purchase Return
                return new PurchaseReturn();
            case "220004": //Branch Inventory
                return new BranchInventory();
            case "220009": //Daily Transaction Report
                return new DailyTransactionReport();
            case "220010": //SP Sales Summary
                return new SPSalesSummary();
            default:
                return null;
        }
    }
}
