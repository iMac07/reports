package org.xersys.reports;

import java.io.IOException;
import org.xersys.reports.bean.DTRSPBean;
import org.xersys.reports.bean.DTRBean;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.xersys.commander.iface.XNautilus;
import org.xersys.commander.iface.XReport;
import org.xersys.commander.util.SQLUtil;
import org.xersys.reports.bean.DTRJOBean;
import org.xersys.reports.bean.DTRSum;
import java.util.List;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class DailyTransactionReport implements XReport{
    private final String REPORTID = "220009";
    private final String REPORT_PATH = "reports/";
    
    private XNautilus p_oNautilus;
    
    private String p_sMessagex; 
    
    private boolean p_bHasPreview;
    
    private JasperPrint _jrprint;
    private LinkedList _rptparam = null;
    
    private double xOffset = 0; 
    private double yOffset = 0;
    
    public DailyTransactionReport(){
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
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("DateSingleCriteria.fxml"));
        fxmlLoader.setLocation(getClass().getResource("DateSingleCriteria.fxml"));

        DateSingleCriteriaController instance = new DateSingleCriteriaController();
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
            return true;
        }
        
        return false; 
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

        try {     
            double spamount = 0.00, joamount = 0.00;
            
            String lsSQL = getSPTranSQL();
            ResultSet rs = p_oNautilus.executeQuery(lsSQL);
            
            //SP Transactions
            DTRSPBean spbean;
            List<DTRSPBean> splist = new LinkedList();   
            while (rs.next()){
                spbean = new DTRSPBean();
                spbean.setbarcodex(rs.getString("sBarCodex"));
                spbean.setdescript(rs.getString("sDescript"));
                spbean.setrefernox(rs.getString("sReferNox"));
                spbean.setquantity(rs.getInt("nQuantity"));
                spbean.setselprice(rs.getDouble("nSelPrice"));
                spbean.setdiscount(rs.getDouble("xDiscount"));
                spbean.setnetsales(rs.getDouble("nNetSales"));
                
                splist.add(spbean);
                spamount += rs.getDouble("nNetSales");
            }
            
            //JO Transactions
            lsSQL = getJOTranSQL();
            rs = p_oNautilus.executeQuery(lsSQL);
            
            DTRJOBean jobean;
            List<DTRJOBean> jolist = new LinkedList();   
            while (rs.next()){
                jobean = new DTRJOBean();
                jobean.settransact(rs.getString("dTransact"));
                jobean.setrefernox(rs.getString("sReferNox"));
                jobean.setclientnm(rs.getString("sClientNm"));
                jobean.setserial01(rs.getString("sSerial01"));
                jobean.setdescript(rs.getString("sDescript"));
                if (rs.getString("cTranStat").equals("4")){
                    jobean.setnetsales(0.00);
                } else{
                    jobean.setnetsales(rs.getDouble("nNetSales"));
                    joamount += rs.getDouble("nNetSales");
                }
                
                jolist.add(jobean);
            }
            
            //summary
            DTRSum sumbean = new DTRSum();
            sumbean.setjoamount(joamount);
            sumbean.setspamount(spamount);
            sumbean.setadvancep(0.00);
            sumbean.setdadvance(0.00);

            List<DTRSum> sumlist = new LinkedList();
            sumlist.add(sumbean);
            
            DTRBean dtr = new DTRBean();
            dtr.setsubSPData(splist);
            dtr.setsubJOData(jolist);
            dtr.setsubSum(sumlist);
            
            ArrayList<DTRBean> datalist = new ArrayList<>();
            datalist.add(dtr);
            JRBeanCollectionDataSource data = new JRBeanCollectionDataSource(datalist);
            
            //Create the parameter
            Map<String, Object> params = new HashMap<>();
            params.put("sCompnyNm", System.getProperty("store.company.name"));  
            params.put("sBranchNm", (String) p_oNautilus.getBranchConfig("sCompnyNm"));
            params.put("sAddressx", (String) p_oNautilus.getBranchConfig("sAddressx") + ", " + (String) p_oNautilus.getBranchConfig("xTownName"));      
            params.put("sReportNm", System.getProperty("store.report.header"));      
            params.put("sPrintdBy", (String) p_oNautilus.getUserInfo("xClientNm"));
            params.put("sReportDt", SQLUtil.dateFormat(SQLUtil.toDate(System.getProperty("store.report.criteria.date.from"), SQLUtil.FORMAT_SHORT_DATE), SQLUtil.FORMAT_MEDIUM_DATE));

            params.put("subSPDIR", (String) p_oNautilus.getAppConfig("sApplPath") + REPORT_PATH +
                                    "DTR_SP.jasper");

            _jrprint = JasperFillManager.fillReport((String) p_oNautilus.getAppConfig("sApplPath") + REPORT_PATH +
                                                    "DTR.jasper",
                                                    params,
                                                    data);
        } catch (JRException | SQLException ex) {
            ex.printStackTrace();
        }
        
        return _jrprint;
    }
    
    private String getJOTranSQL(){
        return "SELECT" +
                    "  DATE_FORMAT(a.dTransact, '%Y-%m-%d') dTransact" +
                    ", IFNULL(e.sInvNumbr, '') `sReferNox`" +
                    ", d.sClientNm" +
                    ", f.sSerial01" +
                    ", c.sDescript" +
                    ", b.nQuantity" +
                    ", b.nQuantity * ((b.nUnitPrce - (b.nUnitPrce * b.nDiscount / 100)) - b.nAddDiscx) nNetSales" +
                    ", a.cTranStat" +
                " FROM Job_Order_Master a" +
                    " LEFT JOIN Job_Order_Detail b" +
                        " ON a.sTransNox = b.sTransNox" +
                    " LEFT JOIN Labor c" +
                        " ON b.sLaborCde = c.sLaborCde" +
                    " LEFT JOIN Receipt_Master e" +
                        " ON e.sSourceNo = a.sTransNox" +
                    " LEFT JOIN Inv_Serial f" +
                        " ON a.sSerialID = f.sSerialID" +
                    ", Client_Master d" +
                " WHERE a.sClientID = d.sClientID" +
                    " AND DATE_FORMAT(a.dTransact, '%Y-%m-%d') = " + SQLUtil.toSQL(System.getProperty("store.report.criteria.date.from")) +
                    " AND ((a.cTranstat <> '3' AND a.nTranTotl <= a.nAmtPaidx) OR a.cTranstat = '4')";
    }
    
    private String getSPTranSQL(){
        return "SELECT" + 
                    "  DATE_FORMAT(a.dTransact, '%Y-%m-%d') dTransact" +
                    ", IFNULL(e.sInvNumbr, '') `sReferNox`" +
                    ", c.sBarCodex" +
                    ", c.sDescript" +
                    ", b.nQuantity" +
                    ", b.nUnitPrce `nSelPrice`" +
                    ", (b.nUnitPrce * b.nDiscount / 100) `xDiscount`" +
                    ", b.nQuantity * (b.nUnitPrce - (b.nUnitPrce * b.nDiscount / 100) - b.nAddDiscx) `nNetSales`" +
                    ", a.nAmtPaidx" +
                    ", a.cTranStat" +
                    ", a.sTransNox" +
                " FROM SP_Sales_Master a" +
                    " LEFT JOIN SP_Sales_Detail b" +
                            " ON a.sTransNox = b.sTransNox" +
                    " LEFT JOIN Inventory c" +
                            " ON b.sStockIDx = c.sStockIDx" +
                    " LEFT JOIN Sales_Invoice e" +
                            " ON e.sSourceNo = a.sTransNox" +
                " WHERE DATE_FORMAT(a.dTransact, '%Y-%m-%d') = " + SQLUtil.toSQL(System.getProperty("store.report.criteria.date.from")) +
                    " AND a.cTranStat = '2'" +
                " HAVING sBarCodex IS NOT NULL" +
                " UNION SELECT" +
                    "  DATE_FORMAT(a.dTransact, '%Y-%m-%d') dTransact" +
                    ", IFNULL(e.sInvNumbr, '') `sReferNox`" +
                    ", c.sBarCodex" +
                    ", c.sDescript" +
                    ", b.nQuantity" +
                    ", b.nUnitPrce `nSelPrice`" +
                    ", (b.nUnitPrce * b.nDiscount / 100) `xDiscount`" +
                    ", b.nQuantity * (b.nUnitPrce - (b.nUnitPrce * b.nDiscount / 100) - b.nAddDiscx) `nNetSales`" +
                    ", a.nPartPaid nAmtPaidx" +
                    ", a.cTranStat" +
                    ", a.sTransNox" +
                " FROM Job_Order_Master a" +
                    " LEFT JOIN Job_Order_Parts b" +
                            " ON a.sTransNox = b.sTransNox" +
                    " LEFT JOIN Inventory c" +
                            " ON b.sStockIDx = c.sStockIDx" +
                    " LEFT JOIN Sales_Invoice e" +
                            " ON e.sSourceNo = a.sTransNox" +
                    ", Client_Master d" +
                " WHERE a.sClientID = d.sClientID" +
                    " AND DATE_FORMAT(a.dTransact, '%Y-%m-%d') = " + SQLUtil.toSQL(System.getProperty("store.report.criteria.date.from")) +
                    " AND ((a.cTranstat <> '3' AND a.nTranTotl <= a.nAmtPaidx) OR a.cTranstat = '4')" +
                " HAVING sBarCodex IS NOT NULL";
    }

    @Override
    public String getMessage() {
        return p_sMessagex;
    }
}
