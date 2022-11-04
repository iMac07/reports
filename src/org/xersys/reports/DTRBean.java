package org.xersys.reports;

import java.util.List;

public class DTRBean {
    private List<DTRSPBean> subReportBeanList;
    
    public List<DTRSPBean> getSubReportBeanList(){
        return subReportBeanList;
    }
    
    public void setSubReportBeanList(List<DTRSPBean> foValue) {
        subReportBeanList = foValue;
    }
}
