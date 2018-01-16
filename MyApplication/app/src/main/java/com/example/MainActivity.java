package com.example;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_features2d;
import org.bytedeco.javacpp.opencv_ml;
import org.bytedeco.javacpp.opencv_nonfree;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //declaration des variables

    private Button CaptureBtn = null;
    private Button GalleryBtn = null;
    private Button AnalyzeBtn = null;
    private ImageView imageView = null;
    private TextView mtextView = null;

    //keep track of camera capture intent
    final int CAMERA_CAPTURE = 2;
    //captured picture uri
    private Uri picUri;

    private static final int PHOTO_LIB_REQUEST = 1;
    static final String WIN_NAME = "Display window";

    final String TAG = MainActivity.class.getName();
    private String pathToPhoto;
    private Bitmap  photoBitmap;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    // SIFT keypoint features
    private static final int N_FEATURES = 0;
    private static final int N_OCTAVE_LAYERS = 3;
    private static final double CONTRAST_THRESHOLD = 0.04;
    private static final double EDGE_THRESHOLD = 10;
    private static final double SIGMA = 1.6;

    public opencv_core.Mat img;
    private opencv_nonfree.SIFT SiftDesc;

    //Variables V2
    //private opencv_core.Mat vocabulary;

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
        catch(Exception e){return "1";}
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

    public interface DataListener {
        void onDataReceived(ArrayList<HashMap<String, Object>> response);
        void onError(String resp); }

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

    public static String getFileContents(final File file)throws IOException {
        final InputStream inputStream = new FileInputStream(file);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        final StringBuilder stringBuilder = new StringBuilder();
        boolean done = false;
        while (!done) {
            final String line = reader.readLine();
            done = (line == null);

            if (line != null) {
                stringBuilder.append(line);
            }
        }
        reader.close();
        inputStream.close();
        return stringBuilder.toString();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
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

        mtextView = (TextView) findViewById(R.id.mTextView);

        Log.d(TAG, "@@ test");
        //TEST

        //Déclaration de la queue
        RequestQueue queue = Volley.newRequestQueue(this);

        //URL d'accès au serveur (pour les request sur la queue)
        String urlYML = "http://www-rech.telecom-lille.fr/nonfreesift/vocabulary.yml";
        String urlJSON = "http://www-rech.telecom-lille.fr/nonfreesift/index.json";

        File fileJSON = new File(this.getFilesDir(), "index.json");

        //Request JSON
        StringRequest stringRequestJSON = new StringRequest(Request.Method.GET, urlJSON,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        FileOutputStream outputStream;
                        try {
                            outputStream = openFileOutput("index.json", Context.MODE_PRIVATE);
                            outputStream.write(response.getBytes());
                            outputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "@@ marche pas");
                    }
                }
        );

        queue.add(stringRequestJSON);

        File fileYML = new File(this.getFilesDir(), "vocabulary.yml");
        //Request YML
        StringRequest stringRequestYML = new StringRequest(Request.Method.GET, urlYML,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        FileOutputStream outputStream;
                        try {
                            outputStream = openFileOutput("vocabulary.yml", Context.MODE_PRIVATE);
                            outputStream.write(response.getBytes());
                            outputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Log.d(TAG, "@@ ");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // mTextView.setText("That didn't work!");
                        Log.d(TAG, "@@ marche pas");
                    }
                }
        );

        queue.add(stringRequestYML);

        //Toast.makeText(this, fileYML.getName().toString(), Toast.LENGTH_LONG).show();
        List classifiersList = new ArrayList<>();
        int classNumber = 0;

        try {
            String stringJSON = getFileContents(fileJSON);
            try {
                Toast.makeText(this, getFileContents(fileJSON), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(stringJSON);
                JSONArray A = null;
                A = jsonObject.getJSONArray("brands");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONArray A = null;
            try {
                A = jsonObject.getJSONArray("brands");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            for (classNumber = 0; classNumber <= jsonObject.length(); classNumber++) {
                JSONObject j = null;
                try {
                    j = A.getJSONObject(classNumber);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    classifiersList.add(j.get("classifier").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mtextView.setText("Response is: " + classifiersList.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        final opencv_ml.CvSVM[] classifiers;
        classifiers = new opencv_ml.CvSVM[3];
        for (int i = 0 ; i < classNumber ; i++) {
            System.out.println("Ok. Creating class name from " + classifiersList.get(i).toString());
            //Request Classifiers
            StringRequest stringRequestClassifiers = new StringRequest(Request.Method.GET, "http://www-rech.telecom-lille.fr/nonfreesift/"+classifiersList.get(i).toString(),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            FileOutputStream outputStream;
                            try {
                                outputStream = openFileOutput("vocabulary.yml", Context.MODE_PRIVATE);
                                outputStream.write(response.getBytes());
                                outputStream.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Log.d(TAG, "@@ ");
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // mTextView.setText("That didn't work!");
                            Log.d(TAG, "@@ marche pas");
                        }
                    }
            );

            classifiers[i] = new opencv_ml.CvSVM();
            classifiers[i].load("Data_BOW/classifiers/" + classifiersList.get(i).toString() + ".xml");
        }

        //String refFile = "vocabulary.yml";
        //this.ToCache(this, "files" + "/" + refFile, refFile).getPath();

        /*File file;
        try {
            file = File.createTempFile("vocabulary.yml", null);
            Toast.makeText(this, file.getAbsolutePath()+" "+file.getName(),Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            outputStream = openFileOutput("vocabulary.yml", Context.MODE_PRIVATE);
            outputStream.write(YMLTry[0].getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
*/
        //Toast.makeText(this, YMLTry[0] + "YOLO", Toast.LENGTH_LONG).show();
        //File file = new File(Environment.getExternalStorageDirectory(),"response");
        Toast.makeText(this, fileYML.getName()+" ",Toast.LENGTH_LONG).show();

        //fileYML


        //Code Prof V2
        final opencv_core.Mat vocabulary;
        Loader.load(opencv_core.class);
        opencv_core.CvFileStorage storage = opencv_core.cvOpenFileStorage("vocabulary.yml", null, opencv_core.CV_STORAGE_READ);
        //Toast.makeText(this, fileYML.getAbsolutePath(), Toast.LENGTH_LONG).show();
        //Pointer p = opencv_core.cvReadByName(storage, null, "vocabulary", opencv_core.cvAttrList());
        /*opencv_core.CvMat cvMat = new opencv_core.CvMat(p);
        vocabulary = new opencv_core.Mat(cvMat);
        System.out.println("vocabulary loaded " + vocabulary.rows() + " x " + vocabulary.cols());
        opencv_core.cvReleaseFileStorage(storage);

        //create SIFT feature point extracter
        final opencv_nonfree.SIFT detector;
        // default parameters ""opencv2/features2d/features2d.hpp""
        detector = new opencv_nonfree.SIFT(0, 3, 0.04, 10, 1.6);

        //create a matcher with FlannBased Euclidien distance (possible also with BruteForce-Hamming)
        final opencv_features2d.FlannBasedMatcher matcher;
        matcher = new opencv_features2d.FlannBasedMatcher();

        //create BoF (or BoW) descriptor extractor
        final opencv_features2d.BOWImgDescriptorExtractor bowide;
        bowide = new opencv_features2d.BOWImgDescriptorExtractor(detector.asDescriptorExtractor(), matcher);

        //Set the dictionary with the vocabulary we created in the first step
        bowide.setVocabulary(vocabulary);
        System.out.println("Vocab is set");
        */

        //Request JSON viendra ici
        //ArrayList<HashMap<String, Object>> list;

        //Ajout venu du prof
        /*
        String refFile = "Pepsi_10.jpg";
        this.pathToPhoto = this.ToCache(this, "images" + "/" + refFile, refFile).getPath();

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        Bitmap bitmap = BitmapFactory.decodeFile(pathToPhoto);
        imageView.setImageBitmap(bitmap);

        Button keypointsButton = (Button) findViewById(R.id.Keypoints);

        keypointsButton.setOnClickListener(this);
        //fin de l'ajout du prof
        */
    }
    private void captureCamera() {
        //use standard intent to capture an image
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //we will handle the returned data in onActivityResult
        startActivityForResult(captureIntent, CAMERA_CAPTURE);

    }
    
    @Override
    public void onClick(View v){
        switch(v.getId()) {
            case R.id.CaptureBtn:
                Toast.makeText(this, TAG + " OnClick", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "OnClick");
                captureCamera();
                break;
            case R.id.GalleryBtn:
                Toast.makeText(this, TAG + " OnClick", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "OnClick");
                startPhotoLibraryActivity();

                break;
            case R.id.AnalyzeBtn:
                Toast.makeText(this,TAG+" OnClick",Toast.LENGTH_SHORT).show();
                Log.i(TAG,"OnClick");
                break;
        }

        //debut de l'ajout du prof

        /*
        img = imread(this.pathToPhoto);
        SiftDesc = new opencv_nonfree.SIFT(N_FEATURES, N_OCTAVE_LAYERS, CONTRAST_THRESHOLD, EDGE_THRESHOLD, SIGMA);

        opencv_core.Mat descriptor = new opencv_core.Mat();
        opencv_features2d.KeyPoint keypoints = new opencv_features2d.KeyPoint();
        SiftDesc.detect(img, keypoints);

        Toast.makeText(this, "Nb of detected keypoints:" + keypoints.capacity(), Toast.LENGTH_LONG).show();
        */
        //fin de l'ajout du prof
    }
}