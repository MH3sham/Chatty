package app.com.example.vip.chattyapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class PhoneLoginActivity extends AppCompatActivity {

    Button sendCodeBtn , verifyBtn;
    EditText phoneNumberET, codeET;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    String  mVerificationId;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    FirebaseAuth mAuth;
    ProgressDialog loadingBar;
    DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        intializeVars();
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference();

        sendCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String phoneNumber = "+2" + phoneNumberET.getText().toString();
                if (TextUtils.isEmpty(phoneNumber)){
                    //Toast enter phone
                }
                else{
                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("please wait, while we authenticating your phone..");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,                     // Phone number to verify
                            60,                           // Timeout duration
                            TimeUnit.SECONDS,                // Unit of timeout
                            PhoneLoginActivity.this, // Activity (for callback binding)
                            mCallbacks);                    // OnVerificationStateChangedCallbacks
                }

            }
        });


        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCodeBtn.setVisibility(View.INVISIBLE);
                phoneNumberET.setVisibility(View.INVISIBLE);
                String mReceviedCode = codeET.getText().toString();
                if (TextUtils.isEmpty(mReceviedCode)){
                    //show toast
                }
                else {
                    loadingBar.setTitle("Code Verification");
                    loadingBar.setMessage("please wait, while we verifying your code");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, mReceviedCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });


        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d("TAGERROR", "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w("TAGERROR", "onVerificationFailed", e);
                Toast.makeText(PhoneLoginActivity.this, "Please Enter a valid phone number!", Toast.LENGTH_SHORT).show();

                sendCodeBtn.setVisibility(View.VISIBLE);
                phoneNumberET.setVisibility(View.VISIBLE);
                verifyBtn.setVisibility(View.INVISIBLE);
                codeET.setVisibility(View.INVISIBLE);
                loadingBar.dismiss();

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("TAGERROR", "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                Toast.makeText(PhoneLoginActivity.this, "Code Sent!", Toast.LENGTH_SHORT).show();

                mVerificationId = verificationId;
                mResendToken = token;


                loadingBar.dismiss();
                sendCodeBtn.setVisibility(View.INVISIBLE);
                phoneNumberET.setVisibility(View.INVISIBLE);
                verifyBtn.setVisibility(View.VISIBLE);
                codeET.setVisibility(View.VISIBLE);

            }
        };

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, add device token
                            String currentUserid = mAuth.getCurrentUser().getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            userRef.child("users").child(currentUserid).child("device_token")
                                    .setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        loadingBar.dismiss();
                                        sendUserToMainActivity();
                                    }
                                }
                            });
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                loadingBar.dismiss();
                                Toast.makeText(PhoneLoginActivity.this, "The verification code entered was invalid", Toast.LENGTH_SHORT).show();

                            }
                        }
                    }
                });
    }


    private void intializeVars() {
        sendCodeBtn = findViewById(R.id.btn_Sendverify);
        verifyBtn = findViewById(R.id.btn_verify);
        phoneNumberET = findViewById(R.id.phoneET);
        codeET = findViewById(R.id.codeET);
        loadingBar = new ProgressDialog(this);
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this , MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
