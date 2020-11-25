package com.example.pielogin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.LinkedList;


//TODO: gotoRecam에서 발생하는 에러 해결
public class login extends AppCompatActivity {

    //Intent for userData
    private Intent intent;
    //Google Variable init
    private FirebaseAuth mAuth = null;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private static final int FB_SIGN_IN = 64206;
    private static final int TW_SIGN_IN = 140;
    private static final String TAG = "LoginActivity";

    //facebook Variable init
    private CallbackManager mCallbackManager;

    //twitter Variable init
    private FirebaseAuth.AuthStateListener mAuthListener;
    private TwitterLoginButton mTwitterBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TwitterAuthConfig mTwitterAuthConfig = new TwitterAuthConfig(getString(R.string.twitter_consumer_key),
                getString(R.string.twitter_consumer_secret));
        TwitterConfig twitterConfig = new TwitterConfig.Builder(this)
                .twitterAuthConfig(mTwitterAuthConfig)
                .build();
        Twitter.initialize(twitterConfig);

        setContentView(R.layout.login);
        //CheckLogIn
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            checkFirstUser();
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

        //init twitter login
        mTwitterBtn = findViewById(R.id.tbutton);

        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                if (firebaseAuth.getCurrentUser() != null){
                    checkFirstUser();
                }
            }
        };

        mTwitterBtn.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Toast.makeText(login.this, "Signed in to twitter successful", Toast.LENGTH_LONG).show();
                signInToFirebaseWithTwitterSession(result.data);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(login.this, "Login failed. No internet or No Twitter app found on your phone", Toast.LENGTH_LONG).show();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });

        //Please write explanation
        Button button = findViewById(R.id.confirm);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoRecom();
            }
        });
    }

    //Check User sign in
    @Override
    protected void onStart() {
        super.onStart();
        // Check If user is signed in -> update UI
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
        //이 친구는 구글 공식 문서에서는 signInBtn 을 invisible 로 바꾸고 signOutBtn 을 Visible 로 바꾸는 친구임
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    private void checkFirstUser(){
        String currentUser = getUserInfo();
        LinkedList<String> LoginUserList = parseUserList(getUserList());
        //TODO: (First) 처음 로그인한건지 조사하는 코드 작성하기
    }

    //USerInfo 얻어오는 곳임
    private String getUserInfo(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        return user.getUid();
    }

    //JSON 에서 UserList 만드는 코드
    private LinkedList<String> parseUserList(forJson json){
        //데이터 개수를 알 수 없기 때문에 LinkedList 를 이용한다
        LinkedList<String> userList = new LinkedList<String>();
        //여기에 json 파일을 파싱한다
        return userList;
    }

    class forJson{
        private String json;

        public String getJson() {
            return json;
        }

        public void setJson(String json) {
            this.json = json;
        }
    }

    // RDB에서 JSON 가져오는 코드
    private forJson getUserList(){
        final forJson json = new forJson();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("User").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("ValueEventListening", (String) snapshot.getValue());
                json.setJson((String) snapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return json;
    }

    //PutExtra 하는 곳임
    private void putUserInfo(String Uid){
        intent.putExtra("Uid",Uid);
    }

    private void gotoRecom(){
        intent = new Intent(login.this , recomendation.class);
        putUserInfo(getUserInfo());
        startActivity(intent);
    }

    private void gotoSignUp(){
        // intent = new Intent(login.this , something.class) TODO: 여기에 회원가입 페이지로 이동하도록 하기
        //TODO: SignUp 에서 Userdata 올리기
        putUserInfo(getUserInfo());
        //startActivity(intent);
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
            case TW_SIGN_IN:{
                mTwitterBtn.onActivityResult(requestCode, resultCode, data);
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
                            checkFirstUser();
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
                            checkFirstUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(login.this, "Login failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //twitter login 마치는 부분
    private void signInToFirebaseWithTwitterSession(TwitterSession session){
        AuthCredential credential = TwitterAuthProvider.getCredential(session.getAuthToken().token,
                session.getAuthToken().secret);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Toast.makeText(login.this, "Signed in firebase twitter successful", Toast.LENGTH_LONG).show();
                        if (!task.isSuccessful()){
                            Toast.makeText(login.this, "Auth firebase twitter failed", Toast.LENGTH_LONG).show();
                        }
                        else {
                            checkFirstUser();
                        }
                    }
                });
    }
}
