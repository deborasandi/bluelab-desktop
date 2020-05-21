package com.bluelab.job;


import java.time.LocalDate;
import java.util.Date;

import com.bluelab.client.Client;
import com.bluelab.database.DBClient;
import com.bluelab.database.DBJob;
import com.bluelab.database.DBJobPrice;
import com.bluelab.database.DBProductColor;
import com.bluelab.jobprice.JobPrice;
import com.bluelab.productcolor.ProductColor;
import com.bluelab.util.AlertDialog;
import com.bluelab.util.FxmlInterface;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;


public class JobCtrl implements FxmlInterface {

    @FXML
    private Spinner<Integer> jobQtd;

    @FXML
    private Spinner<Double> shipping;

    @FXML
    private JFXDatePicker date;

    @FXML
    private JFXComboBox<Client> client;

    @FXML
    private JFXComboBox<JobPrice> jobPrice;

    @FXML
    private JFXComboBox<ProductColor> productColor;

    @FXML
    private TableView<Job> viewJob;

    @FXML
    private TableColumn<Job, Client> colClient;

    @FXML
    private TableColumn<Job, Job> colJobType;

    @FXML
    private TableColumn<Job, ProductColor> colProductColor;

    @FXML
    private TableColumn<Job, Date> colDate;

    @FXML
    private JFXCheckBox repetition;

    @FXML
    private JFXCheckBox nocost;

    @FXML
    private Label lblTotal;

    private Job currentJob;
    
    boolean consumeList = false;

    public void initialize() {
        client.setItems(DBClient.getList());
        productColor.setItems(DBProductColor.getList());
        viewJob.setItems(DBJob.getList());

        date.setValue(LocalDate.now());

        createColumns();

        viewJob.setOnMouseClicked(new EventHandler<Event>() {

            @Override
            public void handle(Event event) {
                currentJob = viewJob.getSelectionModel().getSelectedItem();
                if (currentJob != null)
                    loadJob(currentJob);
            }
        });

        jobQtd.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10));
        jobQtd.getValueFactory().setValue(1);
        jobQtd.valueProperty().addListener((obs, oldValue, newValue) -> calcTotal());

        shipping.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1000));
        shipping.getValueFactory().setValue(0.0);
        shipping.valueProperty().addListener((obs, oldValue, newValue) -> calcTotal());
    }

    private void createColumns() {
        colClient.setCellValueFactory(new PropertyValueFactory<>("client"));
        colJobType.setCellValueFactory(new PropertyValueFactory<>("jobPrice"));
        colProductColor.setCellValueFactory(new PropertyValueFactory<>("productColor"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
    }

    private void clearFields() {
        client.getSelectionModel().clearSelection();
        jobPrice.getSelectionModel().clearSelection();
        productColor.getSelectionModel().clearSelection();
        jobQtd.getValueFactory().setValue(1);
        shipping.getValueFactory().setValue(0.0);
        date.setValue(LocalDate.now());
        repetition.setSelected(false);
        nocost.setSelected(false);
    }

    @Override
    public void disableFields(boolean d) {
        client.setDisable(d);
        jobPrice.setDisable(d);
        productColor.setDisable(d);
        jobQtd.setDisable(d);
        shipping.setDisable(d);
        date.setDisable(d);
        repetition.setDisable(d);
        nocost.setDisable(d);
    }

    private void loadJob(Job j) {
        findClient(j.getClient());
        findJobPrice(j.getJobPrice() != null ? j.getJobPrice().getId() : 0);
        findProductColor(j.getProductColor());
        jobQtd.getValueFactory().setValue(j.getQtd());
        shipping.getValueFactory().setValue(j.getShipping());
        date.setValue(j.getDate().toLocalDate());
        repetition.setSelected(j.isRepetition());
        nocost.setSelected(j.isNocost());

        disableFields(true);
    }

    private void findClient(Client c) {
        if (c == null)
            return;

        for (Client c1 : DBClient.getList()) {
            if (c1.getId() == c.getId())
                client.getSelectionModel().select(c1);
        }
    }

    private void findJobPrice(int id) {
        if(id == 0)
            return;
        
        int i;
        for (i = 0; i < jobPrice.getItems().size(); i++) {
            if (jobPrice.getItems().get(i).getId() == id)
                break;
        }
        
        if(i < jobPrice.getItems().size())
            jobPrice.getSelectionModel().select(i);
    }

    private void findProductColor(ProductColor p) {
        if (p == null)
            return;

        for (ProductColor p1 : DBProductColor.getList()) {
            if (p1.getId() == p.getId())
                productColor.getSelectionModel().select(p1);
        }
    }

    @FXML
    void loadJobPrice() {
        Client c = client.getSelectionModel().getSelectedItem();

        if (c == null)
            return;

        int id = 0;
        if (jobPrice.getValue() != null) {
            id = jobPrice.getValue().getId();
            consumeList = true;
        }

        jobPrice.getItems().clear();
        for (JobPrice jp : DBJobPrice.getList()) {
            if (jp.getPriceTable().getId() == c.getPriceTable().getId())
                jobPrice.getItems().add(jp);
        }

        findJobPrice(id);
    }

    @FXML
    void verifyPrice() {
//        if(consumeList) {
//            consumeList = false;
//            return;
//        }
        
        JobPrice j = jobPrice.getValue();

        if (j == null)
            return;

        if (j.getPrice() == 0) {
            double newValue = AlertDialog.insertPriceAlert(j);
            if (newValue != 0) {
                j.setPrice(newValue);
                DBJobPrice.updatePrice(j);
            }
        }
        calcTotal();
    }

    @FXML
    void calcTotal() {
        JobPrice jp = jobPrice.getSelectionModel().getSelectedItem();

        if (jp == null)
            return;

        if (jobQtd.getValue() == null)
            jobQtd.getValueFactory().setValue(0);

        if (shipping.getValue() == null)
            shipping.getValueFactory().setValue(0.0);

        int nc = (nocost.isSelected() || repetition.isSelected()) ? 0 : 1;

        double total = ((jp.getPrice() * jobQtd.getValue()) * nc) + shipping.getValue();
        lblTotal.setText(String.valueOf(total));
    }

    @FXML
    @Override
    public void create() {
        currentJob = null;
        clearFields();
        disableFields(false);
    }

    @FXML
    @Override
    public void edit() {
        disableFields(false);
    }

    @FXML
    @Override
    public void save() {
        Job j = new Job();

        j.setClient(client.getValue());
        j.setJobPrice(jobPrice.getValue());
        j.setProductColor(productColor.getValue());
        j.setQtd(jobQtd.getValue());
        j.setShipping(shipping.getValue());
        j.setDate(java.sql.Date.valueOf(date.getValue()));
        j.setRepetition(repetition.isSelected());
        j.setNocost(nocost.isSelected());
        j.setTotal(Double.parseDouble(lblTotal.getText()));

        if (currentJob != null) {
            if (AlertDialog.updateAlert(j)) {
                j.setId(currentJob.getId());
                DBJob.update(j);
            }
        }
        else {
            if (AlertDialog.saveNewAlert(j))
                DBJob.insert(j);
        }

        clearFields();
        disableFields(false);
    }

    @Override
    public void delete() {
        if (currentJob != null) {
            if (AlertDialog.deleteAlert(currentJob)) {
                DBJob.delete(currentJob.getId());
                clearFields();
            }
        }
        disableFields(false);
    }
}
