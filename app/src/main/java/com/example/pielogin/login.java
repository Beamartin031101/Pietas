package com.example.pielogin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

public class login extends AppCompatActivity {

    //Google Variable init
    private FirebaseAuth mAuth = null;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private static final int FB_SIGN_IN = 64206;
    private static final String TAG = "LoginActivity";

    //facebook Variable init
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        //CheckLogIn
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            openRecom();
        }

        //Google Variable setting
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))  //만약 default...에서 에러가 난다면 그냥 실행시키면 해결됨
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        //Google login button setting
        Button gLoginBtn = findViewById(R.id.gbutton);
        gLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        //Init facebook Login Button
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton FLoginButton = findViewById(R.id.fbutton);
        FLoginButton.setReadPermissions("email", "public_profile");
        FLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "Facebook - onSuccess" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG,"Facebook - onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "Facebook - onError" + error);
            }
        });

        //Please write explanation
        Button button = findViewById(R.id.confirm);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRecom();
            }
        });
    }

    //Check User sign in
    @Override
    protected void onStart() {
        super.onStart();
        // Check If user is signed in -> update UI
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser); //TODO: 이게 뭐하는 놈인지 알아보기
        //이 친구는 구글 공식 문서에서는 signInBtn을 invisible로 바꾸고 signOutBtn을 Visible로 바꾸는 친구임
    }

    //다음 페이지인 Recommendation으로 가는 부분
    public void openRecom() {
        Intent intent = new Intent(this, recomendation.class);
        startActivity(intent);
    }

    //로그인 하는 부분
    private void signIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    //결과 받아서 구글에 로그인 마치는 것
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        switch(requestCode){
            case RC_SIGN_IN:{
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    assert account != null; //만약 Account = Null이면 에러를 발생시킴
                    //TODO: 이거를 나중에 assert에서 로그인 실패로 바꾸기
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    Log.w(TAG, "Google sign in failed", e);
                    // ...
                }
            }
            case FB_SIGN_IN:{
                mCallbackManager.onActivityResult(requestCode, resultCode, data);
                // DANGER - 이거 그냥 내가 실험으로 FB_SIGN_IN 구한 거라서 에러의 가능성이 있음
            }
        }
    }

    //파이어베이스 Auth
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(login.this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                            openRecom();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(login.this, "로그인 실패!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //Upload userdata to firebase
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            openRecom();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(login.this, "Login failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
