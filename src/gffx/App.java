package gffx;

import gffx.gui.SetupStage;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    public static void main(String[]args) {
        launch(args);
    }

    @Override
    public void start(Stage pm) {
        SetupStage.show();
    }
}