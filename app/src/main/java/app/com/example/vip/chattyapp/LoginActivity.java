package app.com.example.vip.chattyapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    Button loginButton , phoneLoginButton;
    EditText userEmail , userPassword;
    TextView needNewAccLink , forgotPasswordLink;
    ProgressDialog progressDialog;
    DatabaseReference userRef;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference();

        intitializeVars();
        needNewAccLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkIfNoInternetDisplayDialogElseLoadMyData()) {
                    sendUserToRegisterActivity();
                }
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkIfNoInternetDisplayDialogElseLoadMyData()){
                    loginUserWithEmail();
                }
            }
        });

        phoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent phonelogin = new Intent(LoginActivity.this , PhoneLoginActivity.class);
                startActivity(phonelogin);
            }
        });
    }

    private void loginUserWithEmail() {
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

        progressDialog.setTitle("Logging in");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    //add user device token for notifications
                    String currentUserid = mAuth.getUid();
                    if (currentUserid != null){
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                        userRef.child("users").child(currentUserid).child("device_token")
                                .setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    sendUserToMainActivity();
                                    progressDialog.dismiss();
                                }
                            }
                        });
                    }
                }else {
                    Toast.makeText(LoginActivity.this, "Email or password is incorrect, try again!", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }

            }
        });

    }

    private void intitializeVars() {
        progressDialog = new ProgressDialog(this);
        loginButton = findViewById(R.id.btn_login);
        phoneLoginButton = findViewById(R.id.btn_login_phone);
        userEmail = findViewById(R.id.email_login);
        userPassword = findViewById(R.id.password_login);
        needNewAccLink = findViewById(R.id.signup_link);
        forgotPasswordLink = findViewById(R.id.forgot_pass_link);
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this , MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
    private void sendUserToRegisterActivity() {
        Intent mainIntent = new Intent(LoginActivity.this , RegisterActivity.class);
        startActivity(mainIntent);
    }


    private boolean checkIfNoInternetDisplayDialogElseLoadMyData() {
        //method that check if no internet then display AlertDialg says "No internet connection!" else run the thread and get my data.
        ConnectivityManager conMgr =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
        if (netInfo == null){
            new AlertDialog.Builder(LoginActivity.this , R.style.myDialog)
                    .setTitle(getResources().getString(R.string.app_name))
                    .setMessage(getResources().getString(R.string.internet_error))
                    .setIcon(R.drawable.ic_launcher)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //at this point i am trying to exit the app
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                            System.exit(0);
                        }
                    }).show();
        }else{
            return true;
        }
        return false;
    }
}
