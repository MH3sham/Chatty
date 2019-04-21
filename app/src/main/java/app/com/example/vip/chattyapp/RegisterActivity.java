package app.com.example.vip.chattyapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    Button signUpButton;
    EditText userEmail , userPassword;
    TextView alreadyHaveAccLink;
    ProgressDialog progressDialog;
    DatabaseReference databaseReferenceRoot;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        databaseReferenceRoot = FirebaseDatabase.getInstance().getReference();

        intitializeVars();
        alreadyHaveAccLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToLoginActivity();
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewAccount();
            }
        });
    }

    private void createNewAccount() {
        String email = userEmail.getText().toString();
        String pass = userPassword.getText().toString();

        if (email.isEmpty()) {
            userEmail.setError(getString(R.string.input_error_email));
            userEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            userEmail.setError(getString(R.string.input_error_email_invalid));
            userEmail.requestFocus();
            return;
        }

        if (pass.isEmpty()) {
            userPassword.setError(getString(R.string.input_error_password));
            userPassword.requestFocus();
            return;
        }

        if (pass.length() < 6) {
            userPassword.setError(getString(R.string.input_error_password_length));
            userPassword.requestFocus();
            return;
        }

        progressDialog.setTitle("Creating New Account");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    databaseReferenceRoot.child("users").child(currentUserId).setValue("");

                    //add device token
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    databaseReferenceRoot.child("users").child(currentUserId).child("device_token").setValue(deviceToken);

                    Toast.makeText(RegisterActivity.this, "Account Registered Successfully!" , Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    sendUserToMainActivity();
                }
                else {
                    String mesgError = task.getException().toString();
                    Toast.makeText(RegisterActivity.this, "Error: " + mesgError , Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();

                }
            }
        });
    }

    private void intitializeVars() {
        progressDialog = new ProgressDialog(this);
        signUpButton = findViewById(R.id.btn_signup);
        userEmail = findViewById(R.id.email_signup);
        userPassword = findViewById(R.id.password_signup);
        alreadyHaveAccLink = findViewById(R.id.have_account_link);
    }

    private void sendUserToLoginActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this , LoginActivity.class);
        startActivity(mainIntent);
    }
    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this , MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
