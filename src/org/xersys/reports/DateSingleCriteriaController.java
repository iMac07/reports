package org.xersys.reports;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.xersys.commander.iface.XNautilus;
import org.xersys.commander.util.SQLUtil;

public class DateSingleCriteriaController implements Initializable {
    @FXML
    private AnchorPane AnchorPaneHeader;
    @FXML
    private Button btnOkay;
    @FXML
    private Button btnCancel;
    @FXML
    private DatePicker dateFrom;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        p_sDateFrom = SQLUtil.dateFormat(p_oNautilus.getServerDate(), SQLUtil.FORMAT_SHORT_DATE);
        
        dateFrom.setValue(LocalDate.now());
    }    

    @FXML
    private void btnOkay_Click(ActionEvent event) {
        p_bCancelled = false;
        unloadScene(event);
    }

    @FXML
    private void btnCancel_Click(ActionEvent event) {        
        p_sDateFrom = "1900-01-01";
                
        p_bCancelled = true;
        unloadScene(event);
    }
    
    @FXML
    private void dateFrom_OnAction(ActionEvent event) {
        p_sDateFrom = dateFrom.valueProperty().getValue().toString();
    }
    
    private void unloadScene(ActionEvent event){
        Node source = (Node)  event.getSource(); 
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
    
    
    public String getDateFrom(){return p_sDateFrom;}
    public boolean isCancelled(){return p_bCancelled;}
    
    public void setNautilus(XNautilus foValue){p_oNautilus = foValue;}
    
    private boolean p_bCancelled = true;
    private XNautilus p_oNautilus;
    
    private String p_sDateFrom;
}
