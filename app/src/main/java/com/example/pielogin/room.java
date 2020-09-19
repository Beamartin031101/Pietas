package com.example.pielogin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class room extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room1);

        Button movie = findViewById(R.id.movieroom);

        movie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMovie();
            }
        });
    }

    public void openMovie() {
        Intent intent = new Intent(this, movie.class);
        startActivity(intent);
    }
}