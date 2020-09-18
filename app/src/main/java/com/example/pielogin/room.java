package com.example.pielogin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;

public class room extends AppCompatActivity {

    Button bleft, shell1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room1);

        bleft = (Button)findViewById((R.id.butLeft));
        shell1 = (Button)findViewById(R.id.movieroom);

        bleft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnimationUtils.loadAnimation()
            }
        });
    }
}
