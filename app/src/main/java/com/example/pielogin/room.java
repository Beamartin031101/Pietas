package com.example.pielogin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

public class room extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room1);

        final int[] a = {2};

        final Button game = findViewById(R.id.roomgame); //1
        final Button movie = findViewById(R.id.roommovie); //2 starting point
        final Button music = findViewById(R.id.roommusic); //3

        Button buttonleft = findViewById(R.id.butLeft);
        Button buttonright = findViewById(R.id.butRight);

        final Animation fromleft = AnimationUtils.loadAnimation(this,R.anim.fromleft);
        final Animation fromright = AnimationUtils.loadAnimation(this,R.anim.fromright);
        final Animation toleft = AnimationUtils.loadAnimation(this,R.anim.toleft);
        final Animation toright = AnimationUtils.loadAnimation(this,R.anim.toright);

        game.setEnabled(false);
        game.setVisibility(View.INVISIBLE);
        music.setEnabled(false);
        music.setVisibility(View.INVISIBLE);

        final TextView title = findViewById(R.id.roomname);

        movie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMovie();
            }
        });

        buttonleft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (a[0]){
                    case 2:
                        movie.startAnimation(toright);
                        movie.setEnabled(false);
                        movie.setVisibility(View.INVISIBLE);

                        title.setText(R.string.game);
                        game.setEnabled(true);
                        game.setVisibility(View.VISIBLE);
                        game.startAnimation(fromleft);
                        --a[0];
                        break;
                    case 3:
                        music.startAnimation(toright);
                        music.setEnabled(false);
                        music.setVisibility(View.INVISIBLE);

                        title.setText(R.string.movie);
                        movie.setEnabled(true);
                        movie.setVisibility(View.VISIBLE);
                        movie.startAnimation(fromleft);
                        --a[0];
                        break;
                    default:
                        break;
                }
            }
        });

        buttonright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (a[0]){
                    case 2:
                        movie.startAnimation(toleft);
                        movie.setEnabled(false);
                        movie.setVisibility(View.INVISIBLE);

                        title.setText(R.string.music);
                        music.setEnabled(true);
                        music.setVisibility(View.VISIBLE);
                        music.startAnimation(fromright);
                        ++a[0];
                        break;
                    case 1:
                        game.startAnimation(toleft);
                        game.setEnabled(false);
                        game.setVisibility(View.INVISIBLE);

                        title.setText(R.string.movie);
                        movie.setEnabled(true);
                        movie.setVisibility(View.VISIBLE);
                        movie.startAnimation(fromright);
                        ++a[0];
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public void openMovie() {
        Intent intent = new Intent(this, movie.class);
        startActivity(intent);
    }
}