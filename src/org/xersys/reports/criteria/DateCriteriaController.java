/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xersys.reports.criteria;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author Mac
 */
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

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void btnOkay_Click(ActionEvent event) {
    }

    @FXML
    private void btnCancel_Click(ActionEvent event) {
    }
    
}
