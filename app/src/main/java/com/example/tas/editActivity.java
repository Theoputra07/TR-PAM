package com.example.tas;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

public class editActivity extends AppCompatActivity {
    private EditText editname,editharga,editketerangan;
    private ImageView avatar;
    private Button btnsave;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ProgressDialog progressDialog;
    private String id ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        editname = findViewById(R.id.name);
        editharga =findViewById(R.id.harga);
        editketerangan =findViewById(R.id.keterangan);
        btnsave =findViewById(R.id.btn_save);
        avatar =findViewById(R.id.avatar);


        progressDialog =new ProgressDialog(editActivity.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("menyimpan...");

        avatar.setOnClickListener(v ->{
            selectImage();
        });
        btnsave.setOnClickListener(v ->{
            if(editname.getText().length()>0 && editharga.getText().length()>0 && editketerangan.getText().length()>0){
                upload(editname.getText().toString(), editharga.getText().toString(), editketerangan.getText().toString());

            }else{
                Toast.makeText(getApplicationContext(), "Silahkan ini datanya semua!", Toast.LENGTH_SHORT).show();
            }
        });
        Intent intent = getIntent();
        if (intent!= null){
            id =intent.getStringExtra("id");
            editname.setText(intent.getStringExtra("name"));
            editharga.setText(intent.getStringExtra("harga"));
            editketerangan.setText(intent.getStringExtra("keterangan"));
            Glide.with(getApplicationContext()).load(intent.getStringExtra("avatar")).into(avatar);
        }
    }

    private void selectImage(){
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(editActivity.this);
        builder.setTitle(getString(R.string.app_name));
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setItems(items, (dialog, item) -> {
            if(items[item].equals("Take Photo")){
                Intent intent =new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 20);
            }else if (items[item].equals("Choose from Library")){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Image"), 20);
            }else if (items[item].equals("Cancel")){
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 20 && resultCode == RESULT_OK && data != null){
            final Uri path = data.getData();
            Thread thread = new Thread(() ->{
                try {
                    InputStream inputStream = getContentResolver().openInputStream(path);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    avatar.post(() ->{
                        avatar.setImageBitmap(bitmap);
                    });
                } catch (IOException e){
                    e.printStackTrace();
                }
            });
        }
        if(requestCode == 10 && resultCode == RESULT_OK){
            final Bundle extras = data.getExtras();
            Thread thread = new Thread(() ->{
                Bitmap bitmap = (Bitmap) extras.get("data");
                avatar.post(() ->{
                   avatar.setImageBitmap(bitmap);
                });
            });
            thread.start();
        }
    }
     private void upload(String name, String harga, String keterangan){
        progressDialog.show();


        avatar.setDrawingCacheEnabled(true);
        avatar.buildDrawingCache();
         Bitmap bitmap = ((BitmapDrawable) avatar.getDrawable()).getBitmap();
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
         byte[] data = baos.toByteArray();

         FirebaseStorage storage = FirebaseStorage.getInstance();
         StorageReference reference = storage.getReference("image").child("IMG"+new Date().getTime()+".jpeg");
         UploadTask uploadTask =reference.putBytes(data);
         uploadTask.addOnFailureListener(new OnFailureListener() {
             @Override
             public void onFailure(@NonNull Exception e) {
                 Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                 progressDialog.dismiss();
             }
         }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
             @Override
             public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                 if(taskSnapshot.getMetadata()!=null){
                     if(taskSnapshot.getMetadata().getReference()!=null){
                         taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                             @Override
                             public void onComplete(@NonNull Task<Uri> task) {
                                 if(task.getResult()!=null) {
                                     savedata(name, harga, keterangan, task.getResult().toString());
                                 }
                             }
                         });
                     }else {
                         progressDialog.dismiss();
                         Toast.makeText(getApplicationContext(), "Gagal", Toast.LENGTH_SHORT).show();
                     }
                 }else {
                     progressDialog.dismiss();
                     Toast.makeText(getApplicationContext(), "Gagal", Toast.LENGTH_SHORT).show();
                 }
             }
         });
     }

    private void savedata(String name, String harga, String keterangan, String avatar){
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("harga", harga);
        user.put("keterangan", keterangan);
        user.put("avatar", avatar);

        progressDialog.show();
        if(id!=null){
            db.collection("users").document(id)
                    .set(user)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getApplicationContext(), "Berhasil", Toast.LENGTH_SHORT).show();
                                finish();
                            }else{
                                Toast.makeText(getApplicationContext(), "gagal!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }else {
            db.collection("users")
                    .add(user)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(getApplicationContext(), "Berhasil!", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@Nonnull Exception e) {
                            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });

        }

    }
}