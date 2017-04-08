import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayInputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by RD-games on 08.04.2017.
 */
public class Controller {

    private CascadeClassifier cascadeEyeClassifier = new CascadeClassifier("src/main/resources/haarcascades/haarcascade_eye.xml");
    private ScheduledExecutorService timer; // a timer for acquiring the video stream
    private VideoCapture videoDevice = new VideoCapture(); // the OpenCV object that realizes the video capture
    @FXML ImageView imageView;
    @FXML AnchorPane anchorPane;
    @FXML Canvas draw;

    private void startCamera() {
            // grab a frame every 33 ms (30 frames/sec)
            Runnable frameGrabber = new Runnable() {
                @Override
                public void run() {
                    grabFrame();
                }
            };

            this.timer = Executors.newSingleThreadScheduledExecutor();
            this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
    }

    private void grabFrame() {
        videoDevice.open(0);
        if (videoDevice.isOpened()) {
            while (true) {
                Mat mat = new Mat();
                if(videoDevice.read(mat)) {
                    videoDevice.read(mat);
                    cascadeDetector(mat);
                    setImageView(mat2Image(mat));
                }
            }
        } else {
            System.out.println("Не удалось подключится к вебкамере.");
        }
        videoDevice.release();
    }

    private static Image mat2Image(Mat mat) {
        MatOfByte byteMat = new MatOfByte();
        Imgcodecs.imencode(".bmp", mat, byteMat);
        return new Image(new ByteArrayInputStream(byteMat.toArray()));
    }

    private void cascadeDetector(Mat mat) {
        MatOfRect eyes = new MatOfRect();
        cascadeEyeClassifier.detectMultiScale(mat, eyes);
        for (Rect rect : eyes.toArray()) {
            Imgproc.putText(mat, "Eye", new Point(rect.x, rect.y - 5), 1, 2, new Scalar(0, 0, 255));
            Imgproc.rectangle(mat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(200, 200, 100), 2);
        }
        /*
        Rectangle rectangle = drawRect(30, 60, 100, 100);
        anchorPane.getChildren().add(rectangle);
        anchorPane.getChildren().remove(rectangle);*/
    }

    @FXML
    private void buttonHandler(ActionEvent actionEvent) {
        startCamera();
    }

    @FXML
    private void setImageView(Image image) {
        imageView.setImage(image);
    }

    @FXML
    private Rectangle drawRect(int width, int height, int x, int y) {
        Rectangle rectangle = new Rectangle(width, height, x, y);
        rectangle.strokeProperty();
        rectangle.setFill(Color.TRANSPARENT);
        rectangle.setStroke(Color.web("#72cc00"));
        rectangle.setStrokeWidth(1);
        rectangle.setArcHeight(8);
        rectangle.setArcWidth(8);
        rectangle.setId("eye");
        return rectangle;
    }

    private void stopAcquisition() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            }catch (InterruptedException e) {
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }
        if (videoDevice.isOpened()) {
            videoDevice.release();
        }
    }

    protected void setClosed()
    {
        this.stopAcquisition();
    }
}
