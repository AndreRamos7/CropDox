package com.cropdox;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.FpsMeter;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.objdetect.QRCodeDetector;
import io.socket.client.Socket;
import io.socket.client.IO;
import io.socket.emitter.Emitter;
import java.net.URISyntaxException;

public class QrActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{
    private CameraBridgeViewBase cameraBridgeViewBase;
    private BaseLoaderCallback baseLoaderCallback;
    private ImageView mImageViewQR;
    private final static String QR_GENIAL = "QR_GENIAL";
    private FpsMeter mFpsMeter;
    private Socket mSocket;
    private TextView text_view_descricao;

    {
        try {
            mSocket = IO.socket("https://cropdox.com/");
            Log.d("SOCKET.IO: ", "conectou");
        } catch (URISyntaxException e) {
            Log.e("SOCKET.IO: ", "nao conectou");
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        mSocket.on("mensagem", onNewMessage);
        mSocket.connect();

        text_view_descricao = (TextView) findViewById(R.id.text_view_descricao);
        mImageViewQR = (ImageView) findViewById(R.id.imageViewQR);
        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.cameraQR);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

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
        this.enableFpsMeter();

    }
    /**
     * This method enables label with fps value on the screen
     */
    public void enableFpsMeter() {
        if (mFpsMeter == null) {
            mFpsMeter = new FpsMeter();
            mFpsMeter.setResolution(300, 300);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        //attemptSend();
    }

    @Override
    public void onCameraViewStopped() {}

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat frame =  inputFrame.rgba();
        Mat img = new Mat();
        Mat points = new Mat();
        QRCodeDetector qrCodeDetector = new QRCodeDetector();
        String textoQr = qrCodeDetector.detectAndDecode(frame);
        //Toast.makeText(this.getApplicationContext(),textoQr,Toast.LENGTH_LONG).show();
        Log.v(QR_GENIAL, "textoQr: " + textoQr);

        try {
            enviar_id_browser_ao_servidor(textoQr);
        } catch (JSONException e) {
            Log.e(QR_GENIAL, "JSONException " + e.getMessage());
        }

        Bitmap analyzed = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(frame, analyzed);
        //SHOW IMAGE
        setImage(mImageViewQR, analyzed);
        return frame;
    }

    private void enviar_id_browser_ao_servidor(String browser_id_qr) throws JSONException {
        String message = "attemptSend ANDREOID";

        String jsonString = "{url: \"/imagem_do_servidor\", cel_id: \"" + mSocket.id() + "\", browser_id: \"" + browser_id_qr + "\"}";
        JSONObject listasJSON = new JSONObject(jsonString);

        if (TextUtils.isEmpty(message)) {
            return;
        }
        mSocket.emit("mensagem android", listasJSON);
        Log.v(QR_GENIAL,"listaJSON: " + listasJSON.toString());
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    /*JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                    } catch (JSONException e) {
                        return;
                    }*/

                    // add the message to view
                    //Toast.makeText(getApplicationContext(), "IO socket " + message, Toast.LENGTH_LONG).show();
                    //addMessage(username, message);
                    addMessage((String) args[0].toString());
                }
            });
        }
    };

    private void addMessage(String username, String message) {
        text_view_descricao.setText("username: " + username + "; message: " + message);
    }

    private void addMessage(String mensagem) {
        text_view_descricao.setText("mensagemmm: " + mensagem);
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
    protected void onResume(){
        super.onResume();
        if(!OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(), "There's a problem, yo!", Toast.LENGTH_SHORT);
        }else{
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }
    }
    @Override
    protected void onPause(){
        super.onPause();
        if(cameraBridgeViewBase != null){
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        mSocket.off("mensagem", onNewMessage);
        if(cameraBridgeViewBase != null){
            cameraBridgeViewBase.disableView();
        }
    }
}