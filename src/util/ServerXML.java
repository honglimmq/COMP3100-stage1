package util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;

public class ServerXML {
    String serverType;
    int limit;
    int bootupTime;
    float hourlyRate;
    int cores;
    int memory;
    int disk;

    public ServerXML(String serverType, int limit, int bootupTime, float hourlyRate, int cores,
            int memory, int disk) {
        this.serverType = serverType;
        this.limit = limit;
        this.bootupTime = bootupTime;
        this.hourlyRate = hourlyRate;
        this.cores = cores;
        this.memory = memory;
        this.disk = disk;
    }

    public static List<ServerXML> parse(String xmlFilePath) {
        List<ServerXML> servers = new ArrayList<>();

        try {
            // Parse the XML file to a Document object
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(new File(xmlFilePath));

            // Retrieve and store all "server" elements under "servers" element
            Element serversElement =
                    (Element) document.getDocumentElement().getElementsByTagName("servers").item(0);
            NodeList serverNodes = serversElement.getElementsByTagName("server");

            // Loop over server elements and extra server info
            for (int i = 0; i < serverNodes.getLength(); i++) {
                Element serverElement = (Element) serverNodes.item(i);
                String serverType = serverElement.getAttribute("type");
                int limit = Integer.parseInt(serverElement.getAttribute("limit"));
                int bootupTime = Integer.parseInt(serverElement.getAttribute("bootupTime"));
                Float hourlyRate = Float.parseFloat(serverElement.getAttribute("hourlyRate"));
                int cores = Integer.parseInt(serverElement.getAttribute("cores"));
                int memory = Integer.parseInt(serverElement.getAttribute("memory"));
                int disk = Integer.parseInt(serverElement.getAttribute("disk"));

                servers.add(new ServerXML(serverType, limit, bootupTime, hourlyRate, cores, memory,
                        disk));
            }
        } catch (IOException | ParserConfigurationException | NumberFormatException
                | SAXException e) {
            e.printStackTrace();
        }
        return servers;
    }
}