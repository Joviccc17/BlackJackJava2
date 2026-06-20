package hr.algebra.blackjack_dorianjovic.controller;

import hr.algebra.blackjack_dorianjovic.config.GameConfig;
import hr.algebra.blackjack_dorianjovic.config.XmlConfigReader;
import hr.algebra.blackjack_dorianjovic.config.XmlConfigWriter;
import hr.algebra.blackjack_dorianjovic.view.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.File;
import java.io.IOException;

public class SettingsController {

    @FXML private Spinner<Integer> spnDecks;
    @FXML private Spinner<Integer> spnBalance;
    @FXML private Spinner<Integer> spnMinBet;
    @FXML private Spinner<Integer> spnMaxBet;
    @FXML private CheckBox chkDealerHitsSoft17;
    @FXML private TextField txtServerHost;
    @FXML private Spinner<Integer> spnServerPort;
    @FXML private Spinner<Integer> spnMaxPlayers;
    @FXML private Label lblSettingsStatus;

    private GameConfig config;
    private static final String CONFIG_FILE_PATH = "config/game-config.xml";
    private static final String CONFIG_CLASSPATH =
            "/hr/algebra/blackjack_dorianjovic/config/game-config.xml";

    @FXML
    public void initialize() {

        spnDecks.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8, 6));
        spnBalance.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(100, 100000, 1000, 100));
        spnMinBet.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 10, 5));
        spnMaxBet.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 10000, 500, 50));
        spnServerPort.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1024, 65535, 12345));
        spnMaxPlayers.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 6, 2));

        loadConfig();
    }

    private void loadConfig() {
        XmlConfigReader reader = new XmlConfigReader();
        try {
            File configFile = new File(CONFIG_FILE_PATH);
            if (configFile.exists()) {
                config = reader.readConfig(configFile);
            } else {
                config = reader.readConfigFromClasspath(CONFIG_CLASSPATH);
            }
        } catch (Exception e) {
            config = new GameConfig();
            lblSettingsStatus.setText("Failed to load config, using defaults.");
        }

        spnDecks.getValueFactory().setValue(config.getNumberOfDecks());
        spnBalance.getValueFactory().setValue(config.getStartingBalance());
        spnMinBet.getValueFactory().setValue(config.getMinBet());
        spnMaxBet.getValueFactory().setValue(config.getMaxBet());
        chkDealerHitsSoft17.setSelected(config.isDealerHitsSoft17());
        txtServerHost.setText(config.getServerHost());
        spnServerPort.getValueFactory().setValue(config.getServerPort());
        spnMaxPlayers.getValueFactory().setValue(config.getMaxPlayers());

        spnDecks.valueProperty().addListener((o, oldV, newV) -> config.setNumberOfDecks(newV));
        spnBalance.valueProperty().addListener((o, oldV, newV) -> config.setStartingBalance(newV));
        spnMinBet.valueProperty().addListener((o, oldV, newV) -> config.setMinBet(newV));
        spnMaxBet.valueProperty().addListener((o, oldV, newV) -> config.setMaxBet(newV));
        chkDealerHitsSoft17.selectedProperty().addListener((o, oldV, newV) -> config.setDealerHitsSoft17(newV));
        txtServerHost.textProperty().addListener((o, oldV, newV) -> config.setServerHost(newV));
        spnServerPort.valueProperty().addListener((o, oldV, newV) -> config.setServerPort(newV));
        spnMaxPlayers.valueProperty().addListener((o, oldV, newV) -> config.setMaxPlayers(newV));
    }

    @FXML
    private void onSave() {

        try {
            File configFile = new File(CONFIG_FILE_PATH);
            new XmlConfigWriter().saveConfig(config, configFile);
            lblSettingsStatus.setText("Settings saved successfully!");
        } catch (Exception e) {
            lblSettingsStatus.setText("Error saving settings: " + e.getMessage());
        }
    }

    @FXML
    private void onBack() {
        try {
            SceneManager.getInstance().switchScene("main-menu-view.fxml");
        } catch (IOException e) {
            lblSettingsStatus.setText("Error returning to menu.");
        }
    }
}
