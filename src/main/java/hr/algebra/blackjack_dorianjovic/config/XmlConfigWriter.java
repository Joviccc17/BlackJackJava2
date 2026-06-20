package hr.algebra.blackjack_dorianjovic.config;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class XmlConfigWriter {

    public void saveConfig(GameConfig config, File file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElement("blackjack-config");
        doc.appendChild(root);

        Element gameSettings = doc.createElement("game-settings");
        root.appendChild(gameSettings);

        appendElement(doc, gameSettings, "number-of-decks",
                String.valueOf(config.getNumberOfDecks()));
        appendElement(doc, gameSettings, "starting-balance",
                String.valueOf(config.getStartingBalance()));
        appendElement(doc, gameSettings, "min-bet",
                String.valueOf(config.getMinBet()));
        appendElement(doc, gameSettings, "max-bet",
                String.valueOf(config.getMaxBet()));
        appendElement(doc, gameSettings, "dealer-hits-soft17",
                String.valueOf(config.isDealerHitsSoft17()));

        Element networkSettings = doc.createElement("network-settings");
        root.appendChild(networkSettings);

        appendElement(doc, networkSettings, "server-host",
                config.getServerHost());
        appendElement(doc, networkSettings, "server-port",
                String.valueOf(config.getServerPort()));
        appendElement(doc, networkSettings, "max-players",
                String.valueOf(config.getMaxPlayers()));

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }

    private void appendElement(Document doc, Element parent, String tagName, String textContent) {
        Element element = doc.createElement(tagName);
        element.setTextContent(textContent);
        parent.appendChild(element);
    }
}
