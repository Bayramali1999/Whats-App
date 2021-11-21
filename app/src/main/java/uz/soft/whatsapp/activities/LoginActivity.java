package uz.soft.whatsapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import uz.soft.whatsapp.R;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button LoginButton, phoneButton;
    private EditText etLogin, etPassword;
    private TextView forgotPassword, needNewAccount;
    private ProgressDialog progressDialog;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        bindView();

        needNewAccount.setOnClickListener(view -> {
            goToRegisterActivity();
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loggedIn();
                progressDialog.show();
            }
        });
    }

    private void loggedIn() {
        String email = etLogin.getText().toString();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {

        } else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                goToMainActivity();
                                Toast.makeText(LoginActivity.this, "You have successfully login in",
                                        Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(LoginActivity.this, "You have successfully login",
                                        Toast.LENGTH_SHORT).show();
                            }
                            progressDialog.dismiss();
                        }
                    });
        }
    }


    private void goToRegisterActivity() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void bindView() {

//        currentUser = FirebaseUser.ge
        mAuth = FirebaseAuth.getInstance();
        LoginButton = findViewById(R.id.login_btn);
        phoneButton = findViewById(R.id.login_phone);
        etLogin = findViewById(R.id.login_email);
        etPassword = findViewById(R.id.login_password);
        forgotPassword = findViewById(R.id.forgot_password);
        needNewAccount = findViewById(R.id.need_new_account);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Login in");
        progressDialog.setMessage("Please wait checking your password and email");
        progressDialog.setCanceledOnTouchOutside(true);
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}