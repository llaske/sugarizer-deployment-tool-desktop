package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {
    @FXML private BorderPane container;
    @FXML private Button devices;
    @FXML private Button inventory;

    private HashMap<Views, Node> views;

    private enum Views {
        DEVICES,
        INVENTORY
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            views = new HashMap<>();

            views.put(Views.DEVICES, FXMLLoader.load(getClass().getResource("../view/" + "view-devices.fxml")));
            views.put(Views.INVENTORY, FXMLLoader.load(getClass().getResource("../view/" + "view-inventory.fxml")));

            devices.setOnMouseClicked(v -> load(Views.DEVICES));
            inventory.setOnMouseClicked(v -> load(Views.INVENTORY));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load(Views view){
//        if (views.containsKey(view)) {
//            Node newView = views.get(view);
//
//            if (!container.getChildren().contains(newView)) {
//                container.getChildren().add(newView);
//            } else {
//                newView.resize(container.getMaxWidth(), container.getMaxHeight());
//                newView.toFront();
//            }
//        } else {
//            System.out.println("Error: key not found in map");
//        }
        if (views.containsKey(view)) {
            Node newView = views.get(view);
            container.setCenter(newView);
        } else {
            System.out.println("Error: key not found in map");
        }
    }
}
