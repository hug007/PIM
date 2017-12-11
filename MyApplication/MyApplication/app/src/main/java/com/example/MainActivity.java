package com.example;
// ajout pour git
// ajout github
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_nonfree;
import org.bytedeco.javacpp.opencv_features2d;
import static org.bytedeco.javacpp.opencv_highgui.imread;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //declaration des variables

    private Button CaptureBtn = null;
    private Button GalleryBtn = null;
    private Button AnalyzeBtn = null;
    private ImageView imageView = null;

    //keep track of camera capture intent
    final int CAMERA_CAPTURE = 2;
    //captured picture uri
    private Uri picUri;

    private static final int PHOTO_LIB_REQUEST = 1;
    //static final String WIN_NAME = "Display window"; inutile ?

    final String TAG = MainActivity.class.getName();
    private String pathToPhoto;
    private Bitmap  photoBitmap;
    //private static final int REQUEST_IMAGE_CAPTURE = 1; inutile ?

    // variables pour le SIFT (keypoint features)
    private static final int N_FEATURES = 0;
    private static final int N_OCTAVE_LAYERS = 3;
    private static final double CONTRAST_THRESHOLD = 0.04;
    private static final double EDGE_THRESHOLD = 10;
    private static final double SIGMA = 1.6;

    public opencv_core.Mat img;
    private opencv_nonfree.SIFT SiftDesc;

    // fin de declaration des variables

    private void startPhotoLibraryActivity() {
        Intent photoLibIntent = new Intent();
        photoLibIntent.setType("image/*");
        photoLibIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(photoLibIntent,"select photo lib"), PHOTO_LIB_REQUEST);
    }

    protected void processPhotoLibraryResult(Intent intent){
        Uri photoUri = intent.getData();
        pathToPhoto = getRealPath(getApplicationContext(),photoUri);
        File photoFile = new File(pathToPhoto);
        photoBitmap = decodeFile(photoFile);
        imageView.setImageBitmap(photoBitmap);
    }

    protected Bitmap decodeFile(File file) {
        Bitmap bitmap = null;
        try{
            FileInputStream fis = new FileInputStream(file);
            BitmapFactory.Options options = new BitmapFactory.Options();
            bitmap = BitmapFactory.decodeStream(fis,null,options);
        }
        catch(Exception e){}
        return bitmap;
    }

    protected String getRealPath(Context context,Uri uri){
        Cursor cursor;
        try{
            String[] projection = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(uri,projection,null,null,null);
            int dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(dataIndex);
        }
        catch(Exception e)
        {
            return "1";
        }
    }

    private void captureCamera() {
        //use standard intent to capture an image
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //we will handle the returned data in onActivityResult
        startActivityForResult(captureIntent, CAMERA_CAPTURE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_LIB_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = data.getData();
            //Toast.makeText(this,data.getExtras().get("data").toString(),Toast.LENGTH_LONG).show();
            //Log.i(TAG, photoUri.toString());
            //photoUri.getScheme().toString();
            imageView.setImageURI(photoUri);
            //processPhotoLibraryResult(data);
            //
        }
        if (requestCode == CAMERA_CAPTURE && resultCode == RESULT_OK){
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
            Toast.makeText(this,data.getExtras().get("data").toString() ,Toast.LENGTH_LONG).show();
        }
    }



    public static File ToCache(Context context, String Path, String fileName) {
        InputStream input;
        FileOutputStream output;
        byte[] buffer;
        String filePath = context.getCacheDir() + "/" + fileName;
        File file = new File(filePath);
        AssetManager assetManager = context.getAssets();

        try {
            input = assetManager.open(Path);
            buffer = new byte[input.available()];
            input.read(buffer);
            input.close();

            output = new FileOutputStream(filePath);
            output.write(buffer);
            output.close();
            return file;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState){
        //Block rotation screen
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CaptureBtn = (Button) findViewById(R.id.CaptureBtn);
        CaptureBtn.setOnClickListener(this);

        GalleryBtn = (Button) findViewById(R.id.GalleryBtn);
        GalleryBtn.setOnClickListener(this);

        AnalyzeBtn = (Button) findViewById(R.id.AnalyzeBtn);
        AnalyzeBtn.setOnClickListener(this);

        imageView = (ImageView) findViewById(R.id.imageView);

        // A supprimer ?
        //Ajout venu du prof

        String refFile = "Pepsi_10.jpg";
        this.pathToPhoto = this.ToCache(this, "images" + "/" + refFile, refFile).getPath();

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        Bitmap bitmap = BitmapFactory.decodeFile(pathToPhoto);
        imageView.setImageBitmap(bitmap);
////
        Button keypointsButton = (Button) findViewById(R.id.AnalyzeBtn);

        keypointsButton.setOnClickListener(this);
        //fin de l'ajout du prof

    }

    @Override
    public void onClick(View v){
        switch(v.getId()) {
            case R.id.CaptureBtn:
                Toast.makeText(this,TAG, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "OnClick");
                captureCamera();
                break;
            case R.id.GalleryBtn:
                Toast.makeText(this,TAG, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "OnClick");
                startPhotoLibraryActivity();

                break;
            case R.id.AnalyzeBtn:
                Toast.makeText(this,TAG,Toast.LENGTH_SHORT).show();
                Log.i(TAG,"OnClick");
                break;
        }

        //debut de l'ajout du prof

        img = imread(this.pathToPhoto);
        SiftDesc = new opencv_nonfree.SIFT(N_FEATURES, N_OCTAVE_LAYERS, CONTRAST_THRESHOLD, EDGE_THRESHOLD, SIGMA);

        opencv_core.Mat descriptor = new opencv_core.Mat();
        opencv_features2d.KeyPoint keypoints = new opencv_features2d.KeyPoint();
        SiftDesc.detect(img, keypoints);

        Toast.makeText(this, "Nb of detected keypoints:" + keypoints.capacity(), Toast.LENGTH_LONG).show();

        //fin de l'ajout du prof

        //Algorithme propose pour l'analyse des images
        /*
        Lire les images d'entrainement
        Lire l'image selectionee (issue de la galerie ou de l'appareil photo)
        Calculer leur distance
        Classer l'image selectionnee selon quoi
        */
        //
    }
}