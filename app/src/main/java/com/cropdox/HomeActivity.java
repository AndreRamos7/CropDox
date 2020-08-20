package com.cropdox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String EXTRA_MESSAGE = "com.genialsoftwares.utilities.cropdox.MESSAGE";
    private int MY_PERMISSIONS_REQUEST_CAMERA = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove a title bar
        //this.requestWindowFeature(Window.FEATURE_ACTION_BAR);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this.getApplicationContext(), " Você precisa conceder permissões para o app poder funcionar.", Toast.LENGTH_LONG).show();
            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

            ActivityCompat.requestPermissions(HomeActivity.this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.INTERNET}, MY_PERMISSIONS_REQUEST_CAMERA);
            // MY_PERMISSIONS_REQUEST_CAMERA is an
            // app-defined int constant. The callback method gets the
            // result of the request.

        } else {
            Toast.makeText(this.getApplicationContext(), "Permissões concedidas!", Toast.LENGTH_LONG).show();;
        }
        setContentView(R.layout.activity_home);
        Button btn_inicial = (Button) findViewById(R.id.btn_iniciar);
        //Button btn_about = (Button) findViewById(R.id.btn_about);
        Button btn_qr = (Button) findViewById(R.id.btn_qr);
        Button btn_cv = (Button) findViewById(R.id.btn_cv);

        //possibilita exibir Texto em HTML em textViews
        TextView textHtml = TextView.class.cast(findViewById(R.id.text_view_descricao));
        final Spanned textoEmHtml = Html.fromHtml( getApplicationContext().getString(R.string.instrucao_de_uso));
        textHtml.setText(textoEmHtml);

        btn_inicial.setOnClickListener(this);
        btn_cv.setOnClickListener(this);
        btn_qr.setOnClickListener(this);
    }

    /** Called when the user taps the Send button */
    public void iniciarCaptura(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        //EditText editText = (EditText) findViewById(R.id.editText);
        //String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
    /** Called when the user taps the Send button */
    public void iniciarCapturaOpenCV(View view) {
        Intent intent = new Intent(this, CamActivity.class);
        //EditText editText = (EditText) findViewById(R.id.editText);
        //String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    /** Called when the user taps the Send button */
    public void iniciarCapturaQR(View view) {
        Intent intent = new Intent(this, QrActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_iniciar){
            iniciarCaptura(v);
        }else if(v.getId() == R.id.btn_qr) {
            iniciarCapturaQR(v);
        }else if(v.getId() == R.id.btn_cv){
            iniciarCapturaOpenCV(v);
            //Toast.makeText(this.getApplicationContext(), "Função em desenvolvimento.", Toast.LENGTH_LONG).show();
        }
    }
}
