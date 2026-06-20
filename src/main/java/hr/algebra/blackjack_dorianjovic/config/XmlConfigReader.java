package hr.algebra.blackjack_dorianjovic.config;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.InputStream;

public class XmlConfigReader {

    public GameConfig readConfig(File file) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();

        GameConfigHandler handler = new GameConfigHandler();
        saxParser.parse(file, handler);

        return handler.getConfig();
    }

    public GameConfig readConfigFromClasspath(String resourcePath) throws Exception {
        try (InputStream in = XmlConfigReader.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalArgumentException("Classpath resource not found: " + resourcePath);
            }
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GameConfigHandler handler = new GameConfigHandler();
            saxParser.parse(in, handler);
            return handler.getConfig();
        }
    }

    private static class GameConfigHandler extends DefaultHandler {

        private final GameConfig config = new GameConfig();
        private final StringBuilder currentValue = new StringBuilder();
        private String currentElement = "";

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            currentValue.setLength(0);
            currentElement = qName;
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            currentValue.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            String value = currentValue.toString().trim();
            if (value.isEmpty()) {
                return;
            }

            switch (qName) {
                case "number-of-decks" -> config.setNumberOfDecks(Integer.parseInt(value));
                case "starting-balance" -> config.setStartingBalance(Integer.parseInt(value));
                case "min-bet" -> config.setMinBet(Integer.parseInt(value));
                case "max-bet" -> config.setMaxBet(Integer.parseInt(value));
                case "dealer-hits-soft17" -> config.setDealerHitsSoft17(Boolean.parseBoolean(value));
                case "server-host" -> config.setServerHost(value);
                case "server-port" -> config.setServerPort(Integer.parseInt(value));
                case "max-players" -> config.setMaxPlayers(Integer.parseInt(value));
            }

            currentElement = "";
        }

        public GameConfig getConfig() {
            return config;
        }
    }
}
