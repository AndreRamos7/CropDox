package com.cropdox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String EXTRA_MESSAGE = "com.cropdox.MESSAGE";
    private final int MY_PERMISSIONS_REQUEST_CAMERA = 0;
    private String email_do_usuario_logado;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            email_do_usuario_logado = extras.getString("email_do_usuario_logado");
        }
        setContentView(R.layout.activity_home);
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

        
        Button btn_iniciar = (Button) findViewById(R.id.btn_iniciar);
        Button btn_sair = (Button) findViewById(R.id.btn_sair);
        //Button btn_desconnect = (Button) findViewById(R.id.btn_desconnect);
        TextView textView_saudacoes = (TextView) findViewById(R.id.text_view_saudacoes);

        //possibilita exibir Texto em HTML em textViews
        TextView textHtml = TextView.class.cast(findViewById(R.id.text_view_descricao));
        final Spanned textoEmHtml = Html.fromHtml( getApplicationContext().getString(R.string.instrucao_de_uso));
        textHtml.setText(textoEmHtml);
        textView_saudacoes.setText("Olá, " + email_do_usuario_logado + "! Seja Bem-vindo!");

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        btn_iniciar.setOnClickListener(this);
        //btn_desconnect.setOnClickListener(this);
        btn_sair.setOnClickListener(this);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Toast.makeText(HomeActivity.this, "Anúncio", Toast.LENGTH_SHORT).show();
            }
        });

        mAdView = findViewById(R.id.adView_home_activity);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_iniciar) {
            iniciarCaptura(v);
        }else if (i == R.id.btn_sair) {
            signOut();
        }
    }
    private void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();
        // Google revoke access
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
        finish();
    }
    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
        finish();
    }

    private void updateUI(FirebaseUser user) {
        //hideProgressBar();
        if (user != null) {
            String email = user.getEmail();
            Toast.makeText(this, "User in LogInActivity: " + email, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "User: null", Toast.LENGTH_SHORT).show();
        }
    }

    /** Called when the user taps the Send button */
    public void iniciarCaptura(View view) {
        Toast.makeText(this, "User in HomeActivity: " + email_do_usuario_logado, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("email_do_usuario_logado", email_do_usuario_logado);
        startActivity(intent);
    }
    /** Called when the user taps the Send button */
    public void iniciarCapturaOpenCV(View view) {
        Intent intent = new Intent(this, CamActivity.class);
        //intent.putExtra("email_do_usuario_logado", email_do_usuario_logado);
        startActivity(intent);
    }

    /** Called when the user taps the Send button */
    public void iniciarCapturaQR(View view) {
        Intent intent = new Intent(this, QrActivity.class);
        intent.putExtra("email_do_usuario_logado", email_do_usuario_logado);
        startActivity(intent);
    }


}
