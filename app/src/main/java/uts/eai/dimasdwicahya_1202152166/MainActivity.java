package uts.eai.dimasdwicahya_1202152166;



import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;

import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import com.google.firebase.auth.TwitterAuthProvider;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.Arrays;


public class MainActivity extends AppCompatActivity {
    private CallbackManager mCallbackManager;
    private static final String TAG = "FACELOG";
    private FirebaseAuth mAuth;

    private Button mFacebookBtn;
    private Button mEmailBtn;
    private TwitterLoginButton mTwitterBtn;

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleApiClient mGoogleApiClient;


    private FirebaseAuth firebaseAuth; //Untuk Autentifikasi
    private FirebaseAuth.AuthStateListener authStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Konfigurasi Twitter SDK
        TwitterAuthConfig authConfig = new TwitterAuthConfig(
                getString(R.string.twitter_consumer_key),
                getString(R.string.twitter_consumer_secret));

        TwitterConfig twitterConfig = new TwitterConfig.Builder(this)
                .twitterAuthConfig(authConfig)
                .build();

        Twitter.initialize(twitterConfig);

        setContentView(R.layout.activity_main);

        // Inisiasi  Firebase
        mAuth = FirebaseAuth.getInstance();

        // Inisiasi Facebook Login button
        mFacebookBtn = findViewById(R.id.facebookBtn);
        mCallbackManager = CallbackManager.Factory.create();

        mFacebookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.<String>asList("email", "public_profile"));
                LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "facebook:onSuccess:" + loginResult);
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "facebook:onCancel");
                        // ...
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d(TAG, "facebook:onError", error);
                        // ...
                    }
                });
            }
        });


        //KONFIGURASI GMAIL LOGIN

        mEmailBtn = findViewById(R.id.emailBtn);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Membuat a GoogleApiClient dengan memberikan akses GoogleSignIn.API (pilihan akun email login)
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(MainActivity.this, "Eror", Toast.LENGTH_LONG).show();

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });


        //KONFIGURASI TWITTER LOGIN
        mTwitterBtn = findViewById(R.id.login_button);
        mTwitterBtn.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.d(TAG, "twitterLogin:success" + result);
                handleTwitterSession(result.data);
            }


            @Override
            public void failure(TwitterException exception) {
                Log.w(TAG, "twitterLogin:failure", exception);
                updateUI();
            }

        });

        FirebaseConnect();


}


    private void signIn() {

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onStart() {
        super.onStart();
        // Cek apakah user ada atau belum
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            updateUI();
        }


    }

    private void updateUI() {
        Toast.makeText(MainActivity.this,"Anda sudah Login", Toast.LENGTH_LONG).show();
        Intent accountIntent = new Intent(MainActivity.this, AccountActivity.class);
        startActivity(accountIntent);
        finish();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Tampilkan Fb
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Tampilkan Gmail
        if (requestCode == RC_SIGN_IN) {

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()){
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);

            }
            else{
            }

        }
        //Tampilkan Twitter
        mTwitterBtn.onActivityResult(requestCode, resultCode, data);

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Apabila berhasil mendapatkan credential
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI();
                        } else {
                            // Kalau gagal mendapatkan crediantial
                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                            updateUI();
                        }

                        // ...
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Apabila berhasil mendapatkan credential
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            mFacebookBtn.setEnabled(true);

                            updateUI();
                        } else {
                            // Kalau gagal mendapatkan crediantial
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Gagal otentifikasi",
                                    Toast.LENGTH_SHORT).show();

                            mFacebookBtn.setEnabled(false);

                            updateUI();
                        }


                    }
                });
    }

    private void FirebaseConnect(){
        firebaseAuth = FirebaseAuth.getInstance(); //Mendapatkan Instance dari Firebase Auth

        //Listener untuk otentifikasi
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                /*
                 * Jika User sebelumnya telah masuk/login dan belum keluar/logout, secara otomatis
                 * Saat aplikasi dibuka kembali, Activity ini akan langsung dialihkan pada Activiy MainActivity
                 */
                if(firebaseAuth.getCurrentUser() != null){
                    startActivity(new Intent(MainActivity.this,MainActivity.class));
                    finish();
                }
            }
        };
    }

    private void handleTwitterSession(TwitterSession session) {
        Log.d(TAG, "handleTwitterSession:" + session);

        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Apabila berhasil mendapatkan credential

                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI();


                        } else {
                            // Kalau gagal mendapatkan crediantial
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI();
                        }

                    }
                });
    }


    @Override
    protected void onStop() {
        super.onStop();
        if(authStateListener != null){
            mAuth.removeAuthStateListener(authStateListener);
        }
    }



}