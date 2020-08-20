package com.cropdox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cropdox.model.FileInfo;
import com.cropdox.remote.APIUtils;
import com.cropdox.remote.FileService;
import com.cropdox.utilitarios.RemoteUploader;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

/*
Classe da Fase 2 do cropdox utilizada para tirar foto e salvar no diretório do celular.
Esta Classe que está valendo para o primeiro lançamento no pacote com.cropdox
 */
public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2,
        View.OnClickListener, View.OnTouchListener {
    private FileService fileService;

    private static final String ENDERECO = "http://cropdox.com/receber-arquivo";
    private CameraBridgeViewBase cameraBridgeViewBase;
    private BaseLoaderCallback baseLoaderCallback;
    private int counter = 0;
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
    private final String GENIAL_LOG = "GENIAL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(CameraActivity.this, Manifest.permission.CAMERA)) {
                Toast.makeText(this.getApplicationContext(), " Você precisa permitir para o app poder funcionar.", Toast.LENGTH_LONG).show();
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(CameraActivity.this,
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
        fileService = APIUtils.getFileService();

        painel_fundo = (LinearLayout) findViewById(R.id.camera_painel_fundo);

        Button btn_play = (Button) findViewById(R.id.camera_button);
        Button next_btn = (Button) findViewById(R.id.next_button);
        mImageView = (ImageView) findViewById(R.id.camera_imageViewPhoto);
        mImageViewMask = (ImageView) findViewById(R.id.camera_preview);

        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.camera_cv_cropdox);
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
        btn_play.animate().rotation(btn_play.getRotation() - 90).start();
        btn_play.setOnClickListener(this);
        next_btn.setOnClickListener(this);
        btn_play.setOnTouchListener(this);

    }
/*
    private class SendHttpRequestTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            String param1 = params[1];
            String param2 = params[2];
            //Bitmap b = BitmapFactory.decodeResource(this.getResources(), R.drawable.logo);
            Bitmap b = Bitmap.createBitmap(foto.cols(), foto.rows(), Bitmap.Config.RGB_565);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.PNG, 0, baos);

            try {
                RemoteUploader client = new RemoteUploader(CameraActivity.ENDERECO);
                client.connectForMultipart();
                client.addFormPart("param1", param1);
                client.addFormPart("param2", param2);
                client.addFilePart("file", "logo.png", baos.toByteArray());
                client.finishMultipart();
                String data = client.getResponse();
                Log.v(GENIAL_LOG, data);
            }
            catch(Throwable t) {
                t.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String data) {
            //item.setActionView(null);

        }

    }
*/
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP) {
            view.setBackgroundResource(android.R.drawable.ic_menu_camera);
        } else if(event.getAction() == MotionEvent.ACTION_DOWN) {
            view.setBackgroundResource(android.R.drawable.ic_menu_add);
        }
        return false;
    }

    private void enviarImagem(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                File file = new File(currentPhotoPath);
                RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestBody);

                Call<FileInfo> call = fileService.upload(body);

                call.enqueue(new Callback<FileInfo>() {
                    @Override
                    public void onResponse(Call<FileInfo> call, retrofit2.Response<FileInfo> response) {
                        Toast.makeText(CameraActivity.this, "response.message(): " + response.message(), Toast.LENGTH_SHORT).show();
                        if(response.isSuccessful()){
                            Toast.makeText(CameraActivity.this, "Upload realizado com sucesso", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<FileInfo> call, Throwable t) {
                        Log.e(GENIAL_LOG, t.getMessage());
                        Toast.makeText(CameraActivity.this, "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private String get_endereco_diretorio_cropDox() {
        String root = Environment.getExternalStorageDirectory().toString();
        File meu_diretorio = new File(root + "/CropDox");

        if (!meu_diretorio.exists()){
            meu_diretorio.mkdir();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }
        Toast.makeText(this.getApplicationContext(), "Endereço obtido com sucesso!", Toast.LENGTH_LONG).show();
        return meu_diretorio.toString();
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

        painel_fundo.setVisibility(View.VISIBLE);*/
        return super.onTouchEvent(event);
    }

    public void iniciarCapturaQr(View view) {
        Intent intent = new Intent(this, QrActivity.class);
        //EditText editText = (EditText) findViewById(R.id.editText);
        //String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.camera_button){
            if(rect_foto.width == 0) return;
            Bitmap analyzed = Bitmap.createBitmap(foto.cols(), foto.rows(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(foto, analyzed);
            foto.release();
            //SHOW IMAGE
            //Bitmap cortado = cortarBitmap(rect_foto.x, rect_foto.y, rect_foto.width, rect_foto.height, analyzed);
            mImageView.setImageBitmap(analyzed);

            try {
                saveImage(analyzed);
                //galleryAddPic();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else if(v.getId() == R.id.next_button){
            iniciarCapturaQr(v);
            Toast.makeText(this.getApplicationContext(), "NextButton!", Toast.LENGTH_LONG).show();
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
            enviarImagem();
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
        String root = Environment.getExternalStorageDirectory().toString();
        File meu_diretorio = new File(root + "/CropDox");

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "CropDox_" + timeStamp + "_";
        File storageDir = meu_diretorio;
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
        Mat frame =  inputFrame.rgba();

        Rect rect = new Rect(frame.cols(), frame.rows(), frame.width(), frame.height());
        rect_foto = rect;
        foto = frame.clone();

        Imgproc.putText(frame, "Captured: " + frame.size(), new Point(frame.cols() / 3 * 2, frame.rows() * 0.1), Core.FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(255, 255, 0));

        Bitmap analyzed = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(frame, analyzed);
        //SHOW IMAGE
        setImage(mImageViewMask, analyzed);

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
