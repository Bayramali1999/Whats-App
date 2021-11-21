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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import uz.soft.whatsapp.R;

public class RegisterActivity extends AppCompatActivity {

    private Button registerButton;
    private EditText registerEmail, registerPassword;
    private TextView alreadyHaveAccount;
    private ProgressDialog progressDialog;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        bindView();


        alreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToLoginActivity();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount();
                progressDialog.setTitle("Creating new Account");
                progressDialog.setMessage("Please wait account was creates");
                progressDialog.setCanceledOnTouchOutside(true);
                progressDialog.show();
            }
        });
    }

    private void createAccount() {
        String email = registerEmail.getText().toString();
        String password = registerPassword.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Check all inputs", Toast.LENGTH_SHORT).show();
        } else {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String uid = firebaseAuth.getCurrentUser().getUid();
                                databaseReference.child("Users").child(uid).setValue("Login and password");

                                goToMainActivity();
                                Toast.makeText(RegisterActivity.this, "Account successfully created", Toast.LENGTH_SHORT).show();
                            } else {
                                String msg = task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                            progressDialog.dismiss();
                        }
                    });
        }
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void goToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void bindView() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        registerButton = findViewById(R.id.register_btn);
        registerEmail = findViewById(R.id.register_email);
        registerPassword = findViewById(R.id.register_password);
        alreadyHaveAccount = findViewById(R.id.already_have_account_link);
    }
}