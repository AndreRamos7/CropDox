package com.cropdox;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.cropdox.model.FileInfo;
import com.cropdox.remote.FileService;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class ReceptFileActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView imageView;
    private Button btn_enviar;
    private String currentPhotoPath;
    private final String GENIAL_LOG = "ReceptFileActivity";
    private FileService fileService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recept_file);
        imageView = (ImageView) findViewById(R.id.imageView_img_recebida);
        btn_enviar = (Button) findViewById(R.id.btn_enviar);
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
        //...
        btn_enviar.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_enviar){
            enviarImagem();
        }else{

        }
    }
    void handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            currentPhotoPath = imageUri.getPath();
            // Update UI to reflect image being shared
            imageView.setImageURI(imageUri);
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

}