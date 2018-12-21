package com.ziedkh.tracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class Login extends AppCompatActivity {

    EditText EDTNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        EDTNumber = (EditText)findViewById(R.id.EDTNumber);
    }

    public void BuNext(View view) {

        GlobaLInfo.PhoneNumber= GlobaLInfo.FormatPhoneNumber (EDTNumber.getText().toString());
        GlobaLInfo.UpdatesInfo(GlobaLInfo.PhoneNumber);
        finish();
        Intent intent = new Intent(this , MyTracker.class);
        startActivity(intent);
    }
}
