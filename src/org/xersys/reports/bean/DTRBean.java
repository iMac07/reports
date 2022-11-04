package org.xersys.reports.bean;

import java.util.List;

public class DTRBean {
    private List<DTRSPBean> subSPData;
    private List<DTRJOBean> subJOData;
    private List<DTRSum> subSum;
    
    public List<DTRSPBean> getsubSPData(){
        return subSPData;
    }
    
    public void setsubSPData(List<DTRSPBean> foValue) {
        subSPData = foValue;
    }
    
    public List<DTRJOBean> getsubJOData(){
        return subJOData;
    }
    
    public void setsubJOData(List<DTRJOBean> foValue) {
        subJOData = foValue;
    }
    
    public List<DTRSum> getsubSum(){
        return subSum;
    }
    
    public void setsubSum(List<DTRSum> foValue) {
        subSum = foValue;
    }
}
