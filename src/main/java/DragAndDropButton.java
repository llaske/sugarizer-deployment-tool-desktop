import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class DragAndDropButton extends Application {

    private final DataFormat buttonFormat = new DataFormat("com.example.myapp.formats.button");

    private Button draggingButton ;

    @Override
    public void start(Stage primaryStage) {
        FlowPane pane1 = new FlowPane();
        FlowPane pane2 = new FlowPane();

        for (int i = 1 ; i <= 10; i++) {
            pane1.getChildren().add(createButton("Button "+i));
        }

        addDropHandling(pane1);
        addDropHandling(pane2);

        SplitPane splitPane = new SplitPane(pane1, pane2);
        splitPane.setOrientation(Orientation.VERTICAL);

        Scene scene = new Scene(splitPane, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Button createButton(String text) {
        Button button = new Button(text);
        button.setOnDragDetected(e -> {
            Dragboard db = button.startDragAndDrop(TransferMode.MOVE);
            db.setDragView(button.snapshot(null, null));
            ClipboardContent cc = new ClipboardContent();
            cc.put(buttonFormat, "button");
            db.setContent(cc);
            draggingButton = button ;
        });
        button.setOnDragDone(e -> draggingButton = null);
        return button ;
    }

    private void addDropHandling(Pane pane) {
        pane.setOnDragOver(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasContent(buttonFormat)
                    && draggingButton != null
                    && draggingButton.getParent() != pane) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
        });

        pane.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasContent(buttonFormat)) {
                ((Pane)draggingButton.getParent()).getChildren().remove(draggingButton);
                pane.getChildren().add(draggingButton);
                e.setDropCompleted(true);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}