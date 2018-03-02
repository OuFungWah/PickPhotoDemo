package com.crazywah.pickphotodemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_TAKE_PHOTO = 0;
    private static final int REQUEST_SELECT_PHOTO = 1;

    private boolean isGrantedCamera;
    private boolean isGrantedStorage;

    private Button takePhotoBtn;
    private Button selectPhotoBtn;
    private SimpleDraweeView photoSdv;

    private File tempFile;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        takePhotoBtn = findViewById(R.id.take_photo_btn);
        selectPhotoBtn = findViewById(R.id.select_photo_btn);
        photoSdv = findViewById(R.id.photo_sdv);

        takePhotoBtn.setOnClickListener(this);
        selectPhotoBtn.setOnClickListener(this);

        checkAllPermission();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.take_photo_btn:
                if (isGrantedCamera && isGrantedStorage) {
                    takePhoto();
                } else if (!isGrantedCamera) {
                    Toast.makeText(this, "您拒绝了给照相机权限", Toast.LENGTH_SHORT).show();
                    checkAllPermission();
                } else if (!isGrantedStorage) {
                    Toast.makeText(this, "您拒绝了给存储空间权限", Toast.LENGTH_SHORT).show();
                    checkAllPermission();
                }
                break;
            case R.id.select_photo_btn:
                if (isGrantedStorage) {
                    selectPhoto();
                } else {
                    Toast.makeText(this, "您拒绝了给访问存储空间权限", Toast.LENGTH_SHORT).show();
                    checkAllPermission();
                }
                break;
        }
    }

    private void selectPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_SELECT_PHOTO);
    }

    private void takePhoto() {
        // android 7.0系统解决拍照的问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            builder.detectFileUriExposure();
        }
        //新建照片的文件对象
        tempFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), System.currentTimeMillis() + ".png");
        imageUri = Uri.fromFile(tempFile);
        //新建打开照相机的意图
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //选择好捆绑用于装载拍摄好的照片的文件对象的Uri
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
        //发送意图
        startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO);
    }

    private void checkAllPermission() {
        //检测是否已有权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            isGrantedCamera = true;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            isGrantedStorage = true;
        }
        if (!isGrantedCamera || !isGrantedStorage) {
            //一次性申请所有权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissions.length > 0) {
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(this, permissions[i]) == PackageManager.PERMISSION_GRANTED) {
                    if (i == 0) {
                        isGrantedCamera = true;
                    }
                    if (i == 1) {
                        isGrantedStorage = true;
                    }

                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                if (resultCode == RESULT_OK && data != null) {
                    //因为调用拍照前我们已经获取了用来装载文件的Uri，所以这里可以直接使用
                    photoSdv.setImageURI(imageUri);
                }
                break;
            case REQUEST_SELECT_PHOTO:
                if (resultCode == RESULT_OK && data != null) {
                    //先获取返回来的文件的Uri
                    imageUri = data.getData();

                    photoSdv.setImageURI(imageUri);
                }
                break;
        }
    }
}
