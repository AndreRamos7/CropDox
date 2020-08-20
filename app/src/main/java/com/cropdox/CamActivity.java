package com.cropdox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/*
Classe da fase 1 do cropdox utilizada para tirar foto e salvar no diretório do celular com os  reguladores HSV
 */
public class CamActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2,
        View.OnClickListener, View.OnTouchListener {
    private CameraBridgeViewBase cameraBridgeViewBase;
    private BaseLoaderCallback baseLoaderCallback;
    private int counter = 0;
    private SeekBar min_seek_h = null; // initiate the Seek bar
    private SeekBar min_seek_s = null; // initiate the Seek bar
    private SeekBar min_seek_v = null; // initiate the Seek bar

    private SeekBar max_seek_h = null; // initiate the Seek bar
    private SeekBar max_seek_s = null; // initiate the Seek bar
    private SeekBar max_seek_v = null; // initiate the Seek bar
    private final String TAG = "Genial";
    private Mat foto  = null;
    private Rect rect_foto = new Rect();
    private ImageView mImageView;
    private ImageView mImageViewMask;
    private LinearLayout painel_fundo;
    private int MY_PERMISSIONS_REQUEST_CAMERA = 0;
    private int touch_x;
    private int touch_y;
    private final String TOUCH_GENIAL = "TOUCH GENIAL";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(CamActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(CamActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(CamActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(CamActivity.this, Manifest.permission.CAMERA)) {
                Toast.makeText(this.getApplicationContext(), " Você precisa permitir para o app poder funcionar.", Toast.LENGTH_LONG).show();
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(CamActivity.this,
                        new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.INTERNET}, MY_PERMISSIONS_REQUEST_CAMERA);
                // MY_PERMISSIONS_REQUEST_CAMERA is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            Toast.makeText(this.getApplicationContext(), "Permissões concedidas!", Toast.LENGTH_LONG).show();;
        }
        /*
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this.getApplicationContext(), "Sem permissão pra salvar a foto em seu dispositivo!", Toast.LENGTH_LONG).show();;
        }*/
        painel_fundo = (LinearLayout) findViewById(R.id.painel_fundo);
        //painel_fundo.setVisibility(View.INVISIBLE);
        Button btn_play = (Button) findViewById(R.id.button);
        mImageView = (ImageView) findViewById(R.id.imageViewPhoto);
        mImageViewMask = (ImageView) findViewById(R.id.imageViewMask);

        max_seek_h = (SeekBar) findViewById(R.id.max_seek_h);
        max_seek_s = (SeekBar) findViewById(R.id.max_seek_s);
        max_seek_v = (SeekBar) findViewById(R.id.max_seek_v);

        min_seek_h = (SeekBar) findViewById(R.id.min_seek_h);
        min_seek_s = (SeekBar) findViewById(R.id.min_seek_s);
        min_seek_v = (SeekBar) findViewById(R.id.min_seek_v);

        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
        //cameraBridgeViewBase.setCameraIndex(1);

        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);
                switch (status){
                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        btn_play.setOnClickListener(this);
        btn_play.animate().rotation(btn_play.getRotation() - 90).start();
        btn_play.setOnTouchListener(this);
    }
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP) {
            view.setBackgroundResource(android.R.drawable.ic_menu_camera);
        } else if(event.getAction() == MotionEvent.ACTION_DOWN) {
            view.setBackgroundResource(android.R.drawable.ic_menu_add);
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /*touch_x = (int) event.getX();
        touch_y = (int) event.getY();

        double[] pixel = foto.get(touch_x, touch_y);
        if(pixel == null) return super.onTouchEvent(event);
        Toast.makeText(this.getApplicationContext(), String.format("Touched layout - pixel: %f %f %f", pixel[0], pixel[1], pixel[2]) , Toast.LENGTH_SHORT).show();
        Log.v(TOUCH_GENIAL, "PIXEL size: " + pixel.length);
        Log.v(TOUCH_GENIAL, "PIXEL COR: " + pixel[0] + " " + pixel[1] + " " + pixel[2]);
        Log.v(TOUCH_GENIAL, "X = " + event.getX() + ": Y = " + event.getY());

        Mat cor = new Mat(1,1, CvType.CV_8UC3, new Scalar(pixel[0], pixel[1], pixel[2]));
        Imgproc.cvtColor(cor, cor, Imgproc.COLOR_RGB2HSV);

        max_seek_h.setProgress((int) cor.get(0, 0)[0]);
        max_seek_s.setProgress((int) cor.get(0, 0)[1]);
        max_seek_v.setProgress((int) cor.get(0, 0)[2]);



        painel_fundo.setVisibility(View.VISIBLE);*/
        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(View v) {
        if(rect_foto.width == 0) return;
        Bitmap analyzed = Bitmap.createBitmap(foto.cols(), foto.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(foto, analyzed);
        foto.release();
        //SHOW IMAGE
        Bitmap cortado = cortarBitmap(rect_foto.x, rect_foto.y, rect_foto.width, rect_foto.height, analyzed);
        mImageView.setImageBitmap(cortado);
        try {
            saveImage(cortado);
            galleryAddPic();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void saveImage(Bitmap finalBitmap) throws IOException {
        File file = createImageFile();

        //if (file.exists()) file.delete ();
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            //Bitmap source = BitmapFactory.decodeFile(file.getAbsolutePath(), options); Serve??

            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            Toast.makeText(this.getApplicationContext(), "Salvo nos arquivos!", Toast.LENGTH_LONG).show();
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    String currentPhotoPath;

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "CropDox_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = new File(
                storageDir,      /* directory */
                imageFileName +  /* prefix */
                ".jpg"        /* suffix */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    private void setImage(final ImageView imgV, final Bitmap bmp){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imgV.setImageBitmap(bmp);
            }
        });
    }
    @Override
    protected void onPause(){
        super.onPause();
        if(cameraBridgeViewBase != null){
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(!OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(), "There's a problem, yo!", Toast.LENGTH_SHORT);
            //OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, baseLoaderCallback);
        }else{
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {}

    @Override
    public void onCameraViewStopped() {}

    private Bitmap cortarBitmap(int startX, int startY, int width, int height, Bitmap bmp) {
        Bitmap source = bmp;
        Bitmap resized = Bitmap.createBitmap(source, startX, startY, width, height);
        return resized;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        int min_seek_h_value = min_seek_h.getProgress();
        int min_seek_s_value = min_seek_s.getProgress();
        int min_seek_v_value = min_seek_v.getProgress();

        int max_seek_h_value = max_seek_h.getProgress();
        int max_seek_s_value = max_seek_s.getProgress();
        int max_seek_v_value = max_seek_v.getProgress();

        Scalar lower = new Scalar(min_seek_h_value, min_seek_s_value, min_seek_v_value);
        Scalar upper = new Scalar(max_seek_h_value, max_seek_s_value, max_seek_v_value);

        Mat hrq =  new Mat();
        Point ponto = new Point();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        MatOfPoint retangulo = new MatOfPoint();
        Size sz = new Size(15, 15);

        Mat frame =  inputFrame.rgba();
        Mat frame_hsv =  new Mat();
        Mat frame_blur =  new Mat();
        Mat mascara =  new Mat();
        Mat mascara_inv =  new Mat();
        Mat mascara_inv_com_img =  new Mat();
        //Core.flip(frame, frame, 1);
        int h_frame = (int) frame.size().height;
        int w_frame = (int) frame.size().width;
        int a_frame_crop = (h_frame * w_frame);

        Imgproc.cvtColor(frame, frame_hsv, Imgproc.COLOR_RGB2HSV);
        Imgproc.blur(frame_hsv, frame_blur, sz, new Point(2,2));
        Core.inRange(frame_blur, lower, upper, mascara);

        Core.bitwise_not(mascara, mascara_inv);
        Core.bitwise_and(frame, frame, mascara_inv_com_img, mascara);

        Imgproc.findContours(mascara, contours, hrq, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        foto = frame.clone();
        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            Rect rect = Imgproc.boundingRect(contours.get(contourIdx));
            double area_contours = Imgproc.contourArea(contours.get(contourIdx));
            if(a_frame_crop/14 < area_contours && area_contours < a_frame_crop/2){
                int h = (int) contours.get(contourIdx).size().height;
                int w = (int) contours.get(contourIdx).size().width;
                Point pt1 = new Point(rect.x, rect.y);
                Point pt2 = new Point(rect.x + rect.width, rect.y + rect.height);
                Imgproc.rectangle(frame, pt1, pt2, new Scalar(0, 255, 0), 3);
                rect_foto = rect;
            }
        }
        //Imgproc.putText(frame, "Captured: " + frame.size(), new Point(frame.cols() / 3 * 2, frame.rows() * 0.1), Core.FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(255, 255, 0));

        Bitmap analyzed = Bitmap.createBitmap(mascara_inv.cols(), mascara_inv.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(mascara_inv_com_img, analyzed);
        //SHOW IMAGE
        setImage(mImageViewMask, analyzed);
        //frame.release();
        frame_blur.release();
        frame_hsv.release();
        mascara.release();
        mascara_inv.release();
        mascara_inv_com_img.release();

        return frame;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cameraBridgeViewBase != null){
            cameraBridgeViewBase.disableView();
        }
    }


    // ==================================== manter na tela cheia =============================================
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }


}
