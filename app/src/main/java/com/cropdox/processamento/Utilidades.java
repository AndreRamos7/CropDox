package com.cropdox.processamento;

import android.util.Log;

import com.cropdox.model.FileInfo;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class Utilidades {
    private static final String TAG_GENIAL = "matriz";
    private final String GENIAL_LOG = "Utilidades";

    public Utilidades() {

    }
    public static Mat escanear(Mat src){

        Mat frame =  src.clone();
        Mat dest =  new Mat();
        Mat frame_blur =  new Mat();
        //Imgproc.medianBlur(frame, frame_blur, 5);
        Imgproc.adaptiveThreshold(frame, dest,255,Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,11,2.0);


        return dest;

    }
    /*
    * mostra as linhas de acordo com os limites detectados
    * */
    public static Mat transformacao_de_hough(Mat frame, Mat edges){
        Mat frame_cp = frame.clone();
        Mat edgeColor = new Mat();
        Imgproc.cvtColor(edges, edgeColor, Imgproc.COLOR_GRAY2BGR);
        //Detecting the hough lines from (canny)
        Mat lines = new Mat();
        Imgproc.HoughLines(edges, lines, 1, Math.PI/180, 150);
        for (int i = 0; i < lines.rows(); i++) {
            double[] data = lines.get(i, 0);
            double rho = data[0];
            double theta = data[1];
            double a = Math.cos(theta);
            double b = Math.sin(theta);
            double x0 = a*rho;
            double y0 = b*rho;
            //Drawing lines on the image
            Point pt1 = new Point();
            Point pt2 = new Point();
            pt1.x = Math.round(x0 + 1000*(-b));
            pt1.y = Math.round(y0 + 1000*(a));
            pt2.x = Math.round(x0 - 1000*(-b));
            pt2.y = Math.round(y0 - 1000 *(a));
            Imgproc.line(frame_cp, pt1, pt2, new Scalar(0, 0, 255), 3);
        }

        return frame_cp;
    }


}
