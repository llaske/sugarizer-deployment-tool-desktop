package model;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Pane root = FXMLLoader.load(getClass().getResource("../view/sample.fxml"));

        primaryStage.setTitle("Sugarizer");
        primaryStage.setResizable(true);
        primaryStage.setScene(new Scene(root, 600, 300));
        primaryStage.show();
        primaryStage.sizeToScene();
    }


    public static void main(String[] args) {
        for (String tmp : args){
            Logger.getLogger("ARGS").log(Level.ALL, "" + tmp);
            System.out.println("" + tmp);
        }

        launch(args);
    }
}
