package com.example.nearmedemo.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.nearmedemo.Constant.AllConstant;
import com.example.nearmedemo.Model.ProductModel;
import com.example.nearmedemo.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddProductActivity extends AppCompatActivity {
    private static final String TAG = "AddProductActivity";
    private Context context = AddProductActivity.this;
    private CircleImageView imageView;
    private String locationStr;

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        locationStr = getIntent().getStringExtra("l");
        EditText location = findViewById(R.id.location_text);
        location.setText(locationStr);

        imageView = findViewById(R.id.imgPickDialog);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });

        findViewById(R.id.addBtnProduct).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText name = findViewById(R.id.name_text);
                EditText price = findViewById(R.id.price_text);
                EditText location = findViewById(R.id.location_text);
                EditText shopName = findViewById(R.id.shop_name_text);

                if (imageUri == null) {
                    Toast.makeText(context, "Please select an image!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (name.getText().toString().isEmpty()) {
                    Toast.makeText(context, "Please enter a name!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (price.getText().toString().isEmpty()) {
                    Toast.makeText(context, "Please enter a price!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (shopName.getText().toString().isEmpty()) {
                    Toast.makeText(context, "Please enter shop name!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (location.getText().toString().isEmpty()) {
                    Toast.makeText(context, "Please enter your location coordinates!", Toast.LENGTH_SHORT).show();
                    return;
                }

                uploadProduct(name, price, location, shopName);

            }
        });

    }

    private ProgressDialog progressDialog;

    private void uploadProduct(EditText name, EditText price, EditText location, EditText shopName) {
        Log.d(TAG, "uploadProduct: ");
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        storageReference.child(mAuth.getUid() + AllConstant.IMAGE_PATH).putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {");
                        Task<Uri> image = taskSnapshot.getStorage().getDownloadUrl();
                        image.addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> imageTask) {
                                Log.d(TAG, "onComplete: public void onComplete(@NonNull Task<Uri> imageTask) {");
                                if (imageTask.isSuccessful()) {
                                    Log.d(TAG, "onComplete: if (imageTask.isSuccessful()) {");

                                    String url = imageTask.getResult().toString();

                                    String pushKey = databaseReference.child(AllConstant.SHOPS)
                                            .push().getKey();

                                    ProductModel model = new ProductModel();
                                    model.setName(name.getText().toString());
                                    model.setLocation(location.getText().toString());
                                    model.setPrice(price.getText().toString());
                                    model.setUrl(url);
                                    model.setPushKey(pushKey);
                                    model.setShopName(shopName.getText().toString());

                                    databaseReference.child(AllConstant.SHOPS)
                                            .child(pushKey)
                                            .setValue(model)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Log.d(TAG, "onComplete: public void onComplete(@NonNull Task<Void> task) {");
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, "onComplete: if (task.isSuccessful()){");

                                                        uploadTruePriceDetails(model);

//                                                        for (int i = 0; i <= 10; i++) {
//                                                            String pushKey1 = databaseReference.child(AllConstant.SHOPS)
//                                                                    .push().getKey();
//                                                            databaseReference.child(AllConstant.SHOPS)
//                                                                    .child(pushKey1)
//                                                                    .setValue(model);
//                                                        }

                                                        progressDialog.dismiss();
                                                        Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
                                                        finish();

                                                    } else {
                                                        Log.d(TAG, "onComplete: }else {");
                                                        progressDialog.dismiss();
                                                        Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                                    }

                                                }
                                            });

                                } else {
                                    Log.d(TAG, "onComplete: } else {");
                                    progressDialog.dismiss();
                                    Toast.makeText(context, imageTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: public void onFailure(@NonNull Exception e) {");
                        progressDialog.dismiss();
                        Toast.makeText(context, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadTruePriceDetails(ProductModel model) {
        databaseReference.child("truePrice")
                .child(model.getShopName() + "-" + model.getName())
                .child("name")
                .setValue("name");
//        for (int i = 0; i <= 10; i++) {
        databaseReference.child("truePrice")
                .child(model.getShopName() + "-" + model.getName())
                .push()
                .setValue(model.getPrice());
//        }
    }

    private void pickImage() {
        CropImage.activity()
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(this);
    }

    private Uri imageUri;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called");

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            Log.d(TAG, "onActivityResult: if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {");
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "onActivityResult: if (resultCode == RESULT_OK) {");
                imageUri = result.getUri();
                Glide.with(context).load(imageUri).into(imageView);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception exception = result.getError();
                Log.d(TAG, "onActivityResult: " + exception);
            }
        }

    }
}