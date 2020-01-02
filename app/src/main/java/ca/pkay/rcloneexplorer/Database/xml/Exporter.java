package ca.pkay.rcloneexplorer.Database.xml;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import ca.pkay.rcloneexplorer.Database.DatabaseHandler;
import ca.pkay.rcloneexplorer.Database.Task;
import ca.pkay.rcloneexplorer.R;
import es.dmoral.toasty.Toasty;

public class Exporter {

    private static final int PERMISSION_WRITE_EXTERNAL = 739;

    public static void export(Activity activity){
        try {
            String xml = Exporter.create(activity);
            Exporter.storeFile(activity, xml);
        } catch (ParserConfigurationException | TransformerException | IOException e) {
            e.printStackTrace();
        }
    }

    public static String create(Activity a) throws ParserConfigurationException, TransformerException {

        DocumentBuilder documentBuilder =  DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        Element rootElement = document.createElement("tasks");
        document.appendChild(rootElement);

        DatabaseHandler dbHandler = new DatabaseHandler(a);

        for(Task task : dbHandler.getAllTasks()){
            Element em = document.createElement("task");
            em.appendChild(createChild(document, "id", String.valueOf(task.getId())));
            em.appendChild(createChild(document, "name", task.getTitle()));
            em.appendChild(createChild(document, "remote_name", task.getRemote_id()));
            em.appendChild(createChild(document, "remote_path", task.getRemote_path()));
            em.appendChild(createChild(document, "local_path", task.getLocal_path()));
            em.appendChild(createChild(document, "sync_direction", String.valueOf(task.getDirection())));
            rootElement.appendChild(em);
        }


        StringWriter sw = new StringWriter();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(new DOMSource(document), new StreamResult(sw));


        return sw.toString();
    }

    public static void storeFile(Activity activity, String content) throws IOException {

        if(!Exporter.isWriteStoragePermissionGranted(activity)){
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateAndTime = sdf.format(new Date());

        String filename = "rcloneExplorer_"+currentDateAndTime+".xml";


        File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/rcloneExplorer");
        path.mkdirs();
        File file = new File(path, filename);
        FileOutputStream stream = new FileOutputStream(file);
        OutputStreamWriter myOutWriter = new OutputStreamWriter(stream);

        try {
            myOutWriter.append(content);
            Toasty.info(activity, activity.getResources().getString(R.string.exporter_success)+" "+Environment.getExternalStorageDirectory().getAbsolutePath()+"/rcloneExplorer/"+filename, Toast.LENGTH_SHORT, true).show();
        } finally {
            myOutWriter.close();
            stream.close();
        }

    }

    private static Element createChild(Document document, String name, String content){
        Element child = document.createElement(name);
        child.setTextContent(content);
        return child;
    }


    public static boolean isWriteStoragePermissionGranted(Activity a) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (a.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(a, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL);
                return false;
            }
        }else{
            //permission is always granted on lower versions
            return true;
        }
    }

}
