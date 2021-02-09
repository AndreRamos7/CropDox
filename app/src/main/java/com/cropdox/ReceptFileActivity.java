package com.cropdox;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import com.google.android.gms.ads.AdView;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class ReceptFileActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2,
        View.OnClickListener, View.OnTouchListener {
    private ImageView imageView;
    private Button btn_enviar;
    private String currentPhotoPath;
    private final String GENIAL_LOG = "ReceptFileActivity";
    private FileService fileService;
    private AdView mAdView;
    private FirebaseAuth mAuth;
    private String email_do_usuario_logado;
    private CameraBridgeViewBase cameraBridgeViewBase;
    private BaseLoaderCallback baseLoaderCallback;

    private boolean modo_QR;
    private boolean botoa_enviado_clicado;

    private Socket mSocket;
    private boolean qr_ja_reconhecido;
/*
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
    };*/
    private LinearLayout camera_controles;
    private Bitmap bitmap;
    private Button botao_enviar;
    private Button close_button;

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
        //mSocket.on("mensagem", onNewMessage);
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
        botao_enviar = (Button) findViewById(R.id.botao_enviar);
        close_button = (Button) findViewById(R.id.close_button);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();



        Log.v(GENIAL_LOG, email_do_usuario_logado);

        botoa_enviado_clicado = false;
        modo_QR = false;

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
             if (type.startsWith("image/") ) {
                handleSendImage(intent); // Handle single image being sent
            }
        } else {
            // Handle other intents, such as being started from the home screen
        }
        fileService = APIUtils.getFileService();

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
        botao_enviar.animate().rotation(botao_enviar.getRotation() - 90).start();
        close_button.animate().rotation(close_button.getRotation() - 90).start();
        botao_enviar.setOnClickListener(this);
        close_button.setOnClickListener(this);
        close_button.setOnTouchListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.botao_enviar){
            modo_QR = true;
            botoa_enviado_clicado = true;
            enviarImagem();
        }else if(v.getId() == R.id.close_button){
            finish();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if(view.getId() == R.id.close_button) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                view.setBackgroundResource(android.R.drawable.ic_menu_close_clear_cancel);
                //clicado = false;
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                view.setBackgroundResource(android.R.drawable.ic_media_previous);
            }
        }
        return false;
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }
    private void updateUI(FirebaseUser user) {
        //hideProgressBar();
        if (user != null) {
            String email = user.getEmail();
            email_do_usuario_logado = email;
            Log.v( GENIAL_LOG, "User in LogInActivity: " + email);
            //Toast.makeText(this, "User in LogInActivity: " + email, Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, LogInActivity.class);
            intent.putExtra("mensagem", "Você precisa fazer login para poder usar.");
            startActivity(intent);
        }
    }

    void handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        //MediaController mediaController = new MediaController(this.getApplication());
        //String fileName = MediaController.fixFileName(MediaController.getFileName(imageUri));

        //File photoUpload = new File(imageUri.getPath());
        if (imageUri != null) {
            InputStream stream = null;
            try {
                stream = this.getContentResolver().openInputStream(imageUri);
            }
            catch (FileNotFoundException fileEx) {
                Log.e(GENIAL_LOG, fileEx.getMessage());
            }

            if (stream != null) {
                bitmap = null;
                try {
                    bitmap = BitmapFactory.decodeStream(stream);
                }
                finally {
                    try {
                        stream.close();
                        stream = null;
                    }
                    catch (IOException e) {
                    }
                }
            }
            try {
                this.saveImage(bitmap);

                // Update UI to reflect image being shared
                imageView.setImageBitmap(bitmap);

                Log.v(GENIAL_LOG, currentPhotoPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                //Log.v(GENIAL_LOG, file.getName());
                Call<FileInfo> call = fileService.upload(body);
                call.enqueue(new Callback<FileInfo>() {
                    @Override
                    public void onResponse(Call<FileInfo> call, retrofit2.Response<FileInfo> response) {
                        Log.v(GENIAL_LOG, "response.message(): " + response.message());
                        //Toast.makeText(CameraActivity.this, "response.message(): " + response.message(), Toast.LENGTH_SHORT).show();
                        if(response.isSuccessful()){
                            Log.v(GENIAL_LOG,"Upload realizado com sucesso!!");

                            Toast.makeText(ReceptFileActivity.this, "Upload realizado com sucesso!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<FileInfo> call, Throwable t) {
                        Log.e(GENIAL_LOG, "Erro: " +  t.getMessage());
                        //Toast.makeText(ReceptFileActivity.this, "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    @Override
    public void onCameraViewStarted(int width, int height) {}

    @Override
    public void onCameraViewStopped() {}

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat frame =  inputFrame.rgba();
        //Log.v(GENIAL_LOG, "textoQr: nada" );
        if(botoa_enviado_clicado && modo_QR){
            QRCodeDetector qrCodeDetector = new QRCodeDetector();
            String textoQr = qrCodeDetector.detectAndDecode(frame);
            Log.v(GENIAL_LOG, "textoQr: " + textoQr);
            escrever_na_tela("Escaneando QR CODE...", frame);
            try {
                if (!qr_ja_reconhecido && !textoQr.equalsIgnoreCase("")) {
                    enviar_id_browser_ao_servidor(textoQr);
                    qr_ja_reconhecido = true;
                }else{
                    qr_ja_reconhecido = false;
                }
            } catch (JSONException e) {
                Log.e(GENIAL_LOG, "JSONException " + e.getMessage());
            }
        }
        return frame;
    }


    static int cont = 0;
    /**
     * Mostra o texto na tela no frame especificado
     *
     */
    public void escrever_na_tela(String texto, Mat frame){
        Point pt1 = new Point();
        Point pt2 = new Point();

        if(cont >= frame.cols()) {
            cont = 0;
        }else {
            cont += 100;
        }
        pt1.x = cont;
        pt1.y = 0;
        pt2.x = cont;
        pt2.y = frame.cols();

        Imgproc.line(frame, pt1, pt2, new Scalar(255, 255, 0), 7);
        //Imgproc.rectangle(frame,);
        Imgproc.putText(frame, texto, new Point(frame.cols() / 10 , frame.rows() * 0.1), Core.FONT_HERSHEY_SIMPLEX, 1.2, new Scalar(255, 255, 0));
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

    private File createImageFile() throws IOException {
        // Create an image file name
        String root = Environment.getExternalStorageDirectory().toString();
        File diretorio_mobile = new File(root + File.separator + "CropDox");
        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = null; //
        try {
            imageFileName = APIUtils.md5(email_do_usuario_logado);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        //File storageDir = diretorio_mobile;
        File image = new File(
                diretorio_mobile,      /* directory */
                imageFileName +  /* prefix */
                        ".jpg"        /* suffix */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
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

            out.flush();
            out.close();

        } catch (Exception e) {
            Toast.makeText(this.getApplicationContext(), "NÂO Salvo nos arquivos!" + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(GENIAL_LOG, e.getMessage());
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        modo_QR = false;
        botoa_enviado_clicado = false;
        botao_enviar.setVisibility(View.VISIBLE);
        if(cameraBridgeViewBase != null){
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        botao_enviar.setVisibility(View.VISIBLE);
        if(!OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(), "There's a problem, yo!", Toast.LENGTH_SHORT);
            //OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, baseLoaderCallback);
        }else{
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }
    }


}