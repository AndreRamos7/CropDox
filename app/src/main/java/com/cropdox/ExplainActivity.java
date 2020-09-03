package com.cropdox;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.cropdox.ui.main.ExplainFragment;
/*
* um breve tutorial para o usuário para ser exibido no primeiro contato com o aplicativo
* */
public class ExplainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.explain_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, ExplainFragment.newInstance())
                    .commitNow();
        }
    }
}