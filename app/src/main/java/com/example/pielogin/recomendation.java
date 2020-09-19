package com.example.pielogin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class recomendation extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recomendation);

        Button ch1 = findViewById(R.id.choice1);
        ch1.setVisibility(View.VISIBLE);
        ch1.setBackgroundColor(Color.TRANSPARENT);

        ch1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRoom();
            }
        });
    }

    public void openRoom() {
        Intent intent = new Intent(this, room.class);
        startActivity(intent);
    }
}