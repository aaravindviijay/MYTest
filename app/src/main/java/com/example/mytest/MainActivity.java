package com.example.mytest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.loader.content.CursorLoader;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    Button prev, next, pick, create;
    ImageSwitcher imageSwitcher;

    private ArrayList<Uri> imageUris;
    private static final int IMAHE_PICK_CODE = 0;

    int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageSwitcher = findViewById(R.id.is);
        prev = findViewById(R.id.pre);
        next = findViewById(R.id.nxt);
        pick = findViewById(R.id.pick);
        create = findViewById(R.id.create);

        imageUris = new ArrayList<>();

        imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(getApplicationContext());
                return imageView;
            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                permissionCheck();
            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (position > 0) {

                    position--;
                    imageSwitcher.setImageURI(imageUris.get(position));
                } else {

                    Toast.makeText(MainActivity.this, "no prev images", Toast.LENGTH_SHORT).show();
                }

            }
        });

        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                imagePickIntent();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (position < imageUris.size() - 1) {

                    position++;
                    imageSwitcher.setImageURI(imageUris.get(position));
                } else {

                    Toast.makeText(MainActivity.this, "no more images", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }



    private void imagePickIntent() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image(s)"), IMAHE_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAHE_PICK_CODE) {

            if (resultCode == Activity.RESULT_OK) {

                if (data.getClipData() != null) {

                    int count = data.getClipData().getItemCount();

                    for (int i = 0; i < count; i++) {

                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        imageUris.add(imageUri);
                    }

                    imageSwitcher.setImageURI(imageUris.get(0));
                    position = 0;

                } else {

                    Uri uri = data.getData();
                    String path =getPath(uri);
                    String path1 =getRealPathFromUri(uri);
                   // Log.i("myPath", "onActivityResult: " + path);
                    imageUris.add(uri);
                    imageSwitcher.setImageURI(imageUris.get(0));
                    position = 0;
                }
            }
        }
    }

    private String getRealPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this, uri, projection, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        int column = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column);
        cursor.close();
        return result;
    }

   private String getPath(Uri uri){

        String displayName ="";
        String uriString = uri.toString();
        File myFile = new File(uriString);
       if (uriString.startsWith("content://")) {
           Cursor cursor = null;
           try {
               cursor = this.getContentResolver().query(uri, null, null, null, null);
               if (cursor != null && cursor.moveToFirst()) {
                   displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

               }
           } finally {
               cursor.close();
           }
       } else if (uriString.startsWith("file://")) {
           displayName = myFile.getName();


       }
       return displayName;
   }

    private void permissionCheck(){

        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){

            createFolder();
        }else {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},100);
        }
    }

    private void createFolder() {
/*
        File file = new File(Environment.getRootDirectory()+"myNewProin");
        if (!file.exists())
            Toast.makeText(this,
                    (file.mkdirs() ? "Directory has been created" : "Directory not created"),
                    Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Directory exists", Toast.LENGTH_SHORT).show();*/

        File myDirectory = new File(Environment.getExternalStorageDirectory(), "dirsssName");

        if(!myDirectory.exists()) {
            myDirectory.mkdirs();
            Toast.makeText(this, "done", Toast.LENGTH_SHORT).show();
        }else {

            Toast.makeText(this, "already created", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100 && (grantResults.length >0)&& (grantResults[0] == PackageManager.PERMISSION_GRANTED)){

            createFolder();

        }else {

            Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}