package org.xersys.reports.bean;

public class DTRSPBean {
    String barcodex;
    String descript;
    String refernox;
    int quantity;
    double selprice;
    double netsales;
    double discount;
    double discrate;
    
    public String getbarcodex(){
        return barcodex;
    }
    
    public void setbarcodex(String fsValue){
        barcodex = fsValue;
    }
    
    public String getdescript(){
        return descript;
    }
    
    public void setdescript(String fsValue){
        descript = fsValue;
    }
    
    public String getrefernox(){
        return refernox;
    }
    
    public void setrefernox(String fsValue){
        refernox = fsValue;
    }
    
    public int getquantity(){
        return quantity;
    }
    
    public void setquantity(int fnValue){
        quantity = fnValue;
    }
    
    public double getselprice(){
        return selprice;
    }
    
    public void setselprice(double fnValue){
        selprice = fnValue;
    }
    
    public double getdiscount(){
        return discount;
    }
    
    public void setdiscount(double fnValue){
        discount = fnValue;
    }
    
    public double getdiscrate(){
        return discrate;
    }
    
    public void setdiscrate(double fnValue){
        discrate = fnValue;
    }
    
    public double getnetsales(){
        return netsales;
    }
    
    public void setnetsales(double fnValue){
        netsales = fnValue;
    }
}