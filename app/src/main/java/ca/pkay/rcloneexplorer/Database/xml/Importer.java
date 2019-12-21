package ca.pkay.rcloneexplorer.Database.xml;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import ca.pkay.rcloneexplorer.Database.Task;

public class Importer {

    public static final int READ_REQUEST_CODE = 896;
    public static final int PERM_REQUEST_CODE = 897;

    public static ArrayList<Task> createTasklist(Context c, String content) throws ParserConfigurationException, IOException, SAXException {
        ArrayList<Task> result = new ArrayList<>();

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(content));
        Document document = documentBuilder.parse(is);

        NodeList nList = document.getElementsByTagName("task");

        for (int i = 0; i < nList.getLength(); i++) {
            Node n = nList.item(i);
            NodeList children = n.getChildNodes();
            Task task = new Task(-1L);
            for (int j = 0; j < children.getLength(); j++) {
                Node type = children.item(j);

                String nodeContent = type.getTextContent();
                switch (type.getNodeName()) {
                    case "id":
                        task.setId(Long.valueOf(nodeContent));
                        break;
                    case "name":
                        task.setTitle(nodeContent);
                        break;
                    case "remote_name":
                        task.setRemote_id(nodeContent);
                        break;
                    case "remote_path":
                        task.setRemote_path(nodeContent);
                        break;
                    case "local_path":
                        task.setLocal_path(nodeContent);
                        break;
                    case "sync_direction":
                        task.setDirection(Integer.valueOf(nodeContent));
                        break;
                }
            }
            result.add(task);
        }
        return result;
    }

    public static boolean getFilePermission(Activity a) {
        boolean hasPermission = (ContextCompat.checkSelfPermission(a, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(a, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERM_REQUEST_CODE);
        }
        return hasPermission;
    }

}
