package hr.algebra.blackjack_dorianjovic;

import hr.algebra.blackjack_dorianjovic.threading.AppExecutorService;
import hr.algebra.blackjack_dorianjovic.view.SceneManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class BlackjackApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        SceneManager.getInstance().setPrimaryStage(stage);

        FXMLLoader fxmlLoader = new FXMLLoader(
                BlackjackApplication.class.getResource("main-menu-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        stage.setTitle("BlackJack - Dorian Jovic");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        AppExecutorService.getInstance().shutdown();
        super.stop();
    }
}

