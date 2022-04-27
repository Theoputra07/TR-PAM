package com.example.tas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class loginActivity2 extends AppCompatActivity {
    private Button btnlogin, btnregister;
    private EditText editemail, edipassword;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        editemail = findViewById(R.id.email);
        edipassword = findViewById(R.id.password);
        btnlogin = findViewById(R.id.btn_login);
        btnregister = findViewById(R.id.btn_register);
        mAuth = FirebaseAuth.getInstance();

        progressDialog =new ProgressDialog(loginActivity2.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Tunggu sebentar...");
        progressDialog.setCancelable(false);


        btnregister.setOnClickListener(v->{
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
        });

        btnlogin.setOnClickListener(v->{
           if(editemail.getText().length()>0 && edipassword.getText().length()>0){
               login(editemail.getText().toString(), edipassword.getText().toString());
           } else{
               Toast.makeText(getApplicationContext(), "Silahkan isi semua datanya!", Toast.LENGTH_SHORT).show();
           }
        });
    }

    private void  login(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful() && task.getResult()!=null){
                    if(task.getResult().getUser()!= null){
                        reload();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Login gagal", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Login gagal", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void reload(){
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            reload();
        }
    }
}