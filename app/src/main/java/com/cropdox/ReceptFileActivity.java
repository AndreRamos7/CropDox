package com.cropdox;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cropdox.model.FileInfo;
import com.cropdox.remote.APIUtils;
import com.cropdox.remote.FileService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.QRCodeDetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class ReceptFileActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private ImageView imageView;
    private Button btn_enviar;
    private String currentPhotoPath;
    private final String GENIAL_LOG = "ReceptFileActivity";///item/0fdb4d76-3ae0-4a84-b9b0-45aace4fabb5
    private FileService fileService;
    private AdView mAdView;
    private FirebaseAuth mAuth;
    private String email_do_usuario_logado;
    private CameraBridgeViewBase cameraBridgeViewBase;
    private BaseLoaderCallback baseLoaderCallback;


    private Socket mSocket;
    private boolean qr_ja_reconhecido;
    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addMessage((String) args[0].toString());
                }
            });
        }
    };
    private LinearLayout camera_controles;

    {
        try {
            //mSocket = IO.socket("http://192.168.0.107/");
            email_do_usuario_logado = "cropdox";
            mSocket = IO.socket("https://cropdox.com/");
            Log.d("SOCKET.IO: ", "conectou");
        } catch (URISyntaxException e) {
            Log.e("SOCKET.IO: ", "nao conectou");
            throw new RuntimeException(e);
        }
    }

    public ReceptFileActivity() {
        mSocket.on("mensagem", onNewMessage);
        mSocket.connect();
    }

    private void addMessage(String mensagem) {
        Log.v(GENIAL_LOG, "Servidor Node diz: " + mensagem);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recept_file);
        imageView = (ImageView) findViewById(R.id.imageView_img_recebida);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        email_do_usuario_logado = user.getEmail();

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
             if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            }
        } else {
            // Handle other intents, such as being started from the home screen
        }

        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.camera_opencv2);
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

        imageView.animate().rotation(imageView.getRotation() - 90).start();
    }

    void handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            currentPhotoPath = imageUri.getPath();

            // Create an image file name
            String root = Environment.getExternalStorageDirectory().toString();
            File diretorio_mobile = new File(root + "/CropDox");
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = timeStamp;


            //File storageDir = diretorio_mobile;
            File dest = new File(
                    diretorio_mobile,      /* directory */
                    imageFileName +  /* prefix */
                            ".jpg"        /* suffix */
            );

            File source = new File(currentPhotoPath);

            //enviarImagem();

            Log.v(GENIAL_LOG, currentPhotoPath);
            // Update UI to reflect image being shared
            imageView.setImageURI(imageUri);
            //this.enviarImagem();
        }
    }
    /*
        Envia a imagem para o servidor VPS cropdox.com utilizando Retrofit
         */
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
                        Log.v(GENIAL_LOG, "response.message(): " + response.message());
                        //Toast.makeText(CameraActivity.this, "response.message(): " + response.message(), Toast.LENGTH_SHORT).show();
                        if(response.isSuccessful()){
                            Log.v(GENIAL_LOG,"Upload realizado com sucesso!!");
                            //Toast.makeText(CameraActivity.this, "Upload realizado com sucesso!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<FileInfo> call, Throwable t) {
                        Log.e(GENIAL_LOG, "Erro: " +  t.getMessage());
                        //Toast.makeText(CameraActivity.this, "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
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

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat frame =  inputFrame.rgba();
        QRCodeDetector qrCodeDetector = new QRCodeDetector();
        String textoQr = qrCodeDetector.detectAndDecode(frame);
        Log.v(GENIAL_LOG, "textoQr: " + textoQr);
        escrever_na_tela("EM MODO QR", frame);
        try {

            if (!qr_ja_reconhecido && !textoQr.equalsIgnoreCase("")) {
                enviar_id_browser_ao_servidor(textoQr);
                //enviarImagem();
                qr_ja_reconhecido = true;
            }else{
                qr_ja_reconhecido = false;
            }
        } catch (JSONException e) {
            Log.e(GENIAL_LOG, "JSONException " + e.getMessage());
        }

        return frame;
    }

    /**
     * Mostra o texto na tela no frame especificado
     *
     */
    public void escrever_na_tela(String texto, Mat frame){
        Imgproc.putText(frame, texto, new Point(frame.cols() / 5 * 2, frame.rows() * 0.1), Core.FONT_HERSHEY_SIMPLEX, 1.2, new Scalar(255, 255, 0));
    }

    /*
     * Envia socketid do browser ao servidor no formato JSON
     * */
    private void enviar_id_browser_ao_servidor(String browser_id_qr) throws JSONException {
        //String message = "attemptSend ANDREOID";
        try {
            email_do_usuario_logado = APIUtils.md5(email_do_usuario_logado);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        String jsonString = "{url: \"/imagem_do_servidor\", cel_id: \"" +
                mSocket.id() +
                "\", browser_id: \"" +
                browser_id_qr +
                "\", email_do_usuario_logado: \"" +
                email_do_usuario_logado +
                "\"}";
        JSONObject jsonObject = new JSONObject(jsonString);
        mSocket.emit("mensagem android", jsonObject);
        qr_ja_reconhecido = true;
        mostrarResultado();
    }

    public void mostrarResultado() {
        Intent intent = new Intent(this, TransferActivity.class);
        intent.putExtra("key", qr_ja_reconhecido);
        startActivity(intent);
    }
}