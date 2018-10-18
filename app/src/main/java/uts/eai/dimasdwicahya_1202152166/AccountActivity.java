package uts.eai.dimasdwicahya_1202152166;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountActivity extends AppCompatActivity {

    private Button mLogoutBtn;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        mAuth = FirebaseAuth.getInstance();

        mLogoutBtn = findViewById(R.id.logoutBtn);

        mLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                LoginManager.getInstance().logOut();

                updateUI();
            }
        });

    }

    private void updateUI() {
        Toast.makeText(AccountActivity.this,"Akun sudah Logout", Toast.LENGTH_LONG).show();
        Intent accountIntent = new Intent(AccountActivity.this, MainActivity.class);
        startActivity(accountIntent);
        finish();
    }
}
