/**
 * Created by RD-games on 07.04.2017.
 */

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
//import javafx.scene.image.Image;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class Main extends Application{

    static JFrame frame;
    static JLabel lbl;
    static ImageIcon icon;
    static @FXML ImageView imageView;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/interface.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Hello");
        primaryStage.show();
    }

    @FXML
    public void buttonHandler(ActionEvent actionEvent) {
        System.out.println(getClass().getResourceAsStream("1.jpg"));
        System.out.println(getClass().getResource("1.jpg").getPath());
        //imageView.setImage(getClass().getResourceAsStream("1.jpg"));
    }

    public static void main(String[] args) {
        launch(args);
        //imageView.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("1.jpg")));
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        CascadeClassifier cascadeEyeClassifier = new CascadeClassifier("src/main/resources/haarcascades/haarcascade_eye.xml");
        VideoCapture videoDevice = new VideoCapture();
        videoDevice.open(0);
        if (videoDevice.isOpened()) {
            while (true) {
                Mat frame = new Mat();
                videoDevice.read(frame);

                //Поиск глаз
                MatOfRect eyes = new MatOfRect();
                cascadeEyeClassifier.detectMultiScale(frame, eyes);
                for (Rect rect : eyes.toArray()) {
                    Imgproc.putText(frame, "Eye", new Point(rect.x,rect.y-5), 1, 2, new Scalar(0,0,255));
                    Imgproc.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                            new Scalar(200, 200, 100),2);
                }

                PushImage(ConvertMat2Image(frame));
            }
        } else {
            System.out.println("Не удалось подключится к вебкамере.");
            return;
        }
    }

    private static BufferedImage ConvertMat2Image(Mat kameraVerisi) {
        MatOfByte byteMatVerisi = new MatOfByte();
        //Ara belle?e verilen formatta görüntü kodlar
        Imgcodecs.imencode(".jpg", kameraVerisi, byteMatVerisi);
        //Mat nesnesinin toArray() metodu elemanlary byte dizisine çevirir
        byte[] byteArray = byteMatVerisi.toArray();
        BufferedImage goruntu = null;
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            goruntu = ImageIO.read(in);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return goruntu;
    }

    //Bir frame (çerçeve) olu?turur
    public static void PencereHazirla() {
        frame = new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(700, 600);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    //Resmi gösterecek label olu?turur
    public static void PushImage(Image img2) {
        //Pencere olu?turulmamy? ise hazyrlanyr
        if (frame == null)
            PencereHazirla();
        //Daha önceden bir görüntü yüklenmi? ise yenisi için kaldyryr
        if (lbl != null)
            frame.remove(lbl);
        icon = new ImageIcon(img2);
        lbl = new JLabel();
        lbl.setIcon(icon);
        frame.add(lbl);
        //Frame nesnesini yeniler
        frame.revalidate();
    }
}
