package org.xersys.reports.criteria;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class DateCriteriaController implements Initializable {

    @FXML
    private AnchorPane AnchorPaneHeader;
    @FXML
    private Button btnOkay;
    @FXML
    private Button btnCancel;
    @FXML
    private DatePicker dateFrom;
    @FXML
    private DatePicker dateThru;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }    

    @FXML
    private void btnOkay_Click(ActionEvent event) {
        psUsername = txtField01.getText();
        psPassword = txtField02.getText();
        pbCancelled = false;
        unloadScene(event);
    }

    @FXML
    private void btnCancel_Click(ActionEvent event) {
        pbCancelled = true;
        unloadScene(event);
    }
    
    
    private void unloadScene(ActionEvent event){
        Node source = (Node)  event.getSource(); 
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
    
    public boolean isCancelled(){return pbCancelled;}
    
    private boolean pbCancelled = true;
}
