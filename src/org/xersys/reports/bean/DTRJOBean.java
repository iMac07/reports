package org.xersys.reports.bean;

public class DTRJOBean {
    String transact;
    String refernox;
    String clientnm;
    String serial01;
    String descript;
    double netsales;
    
    public String gettransact(){
        return transact;
    }
    
    public void settransact(String fsValue){
        transact = fsValue;
    }
    
    public String getrefernox(){
        return refernox;
    }
    
    public void setrefernox(String fsValue){
        refernox = fsValue;
    }
    
    public String getclientnm(){
        return clientnm;
    }
    
    public void setclientnm(String fsValue){
        clientnm = fsValue;
    }
    
    public String getserial01(){
        return serial01;
    }
    
    public void setserial01(String fsValue){
        serial01 = fsValue;
    }
    
    public String getdescript(){
        return descript;
    }
    
    public void setdescript(String fsValue){
        descript = fsValue;
    }
    
    public double getnetsales(){
        return netsales;
    }
    
    public void setnetsales(double fnValue){
        netsales = fnValue;
    }
}