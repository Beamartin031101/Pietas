package com.example.pielogin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class movie extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movieroom);

        Button chat = findViewById(R.id.butchat);

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openchat();
            }
        });

    }

    public void openchat() {
        Intent intent = new Intent(this, chatroom.class);
        startActivity(intent);
    }
}
