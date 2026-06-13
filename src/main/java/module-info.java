module hr.algebra.blackjack_dorianjovic {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.rmi;
    requires java.naming;
    requires java.xml;

    // Root package
    opens hr.algebra.blackjack_dorianjovic to javafx.fxml;
    exports hr.algebra.blackjack_dorianjovic;

    // Model — open to javafx.base for property bindings
    opens hr.algebra.blackjack_dorianjovic.model to javafx.base, javafx.fxml;
    exports hr.algebra.blackjack_dorianjovic.model;

    // Controllers
    opens hr.algebra.blackjack_dorianjovic.controller to javafx.fxml;
    exports hr.algebra.blackjack_dorianjovic.controller;

    // View components
    opens hr.algebra.blackjack_dorianjovic.view to javafx.fxml, javafx.base;
    exports hr.algebra.blackjack_dorianjovic.view;

    // Config (XML)
    opens hr.algebra.blackjack_dorianjovic.config;
    exports hr.algebra.blackjack_dorianjovic.config;

    // Engine
    opens hr.algebra.blackjack_dorianjovic.engine to javafx.base, javafx.fxml;
    exports hr.algebra.blackjack_dorianjovic.engine;

    // Network — RMI needs exports
    opens hr.algebra.blackjack_dorianjovic.network;
    exports hr.algebra.blackjack_dorianjovic.network;

    // Threading
    opens hr.algebra.blackjack_dorianjovic.threading to javafx.base;
    exports hr.algebra.blackjack_dorianjovic.threading;

    // Serialization
    opens hr.algebra.blackjack_dorianjovic.serialization;
    exports hr.algebra.blackjack_dorianjovic.serialization;

    // Util (annotations)
    opens hr.algebra.blackjack_dorianjovic.util;
    exports hr.algebra.blackjack_dorianjovic.util;
}