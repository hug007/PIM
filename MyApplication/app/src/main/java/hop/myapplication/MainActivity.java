package hop.myapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.BuildConfig;
import com.example.R;

import java.io.File;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final String TAG = MainActivity.class.getName();

    protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;
    protected static final int ANALYSE_REQUEST_CODE = 5;
    final int captureActivityResult = 2;
    final int libraryActivityResult = 1;
    final int analyseActivityResult = 12;

    private Intent analysisIntent;
    /*
    final int analyseActivityResult = 300;

    final int photoRequestActivityResult = 400;
    final int resultCodeActivityResult = 500;
    */
    Button captureButton;
    Button libraryButton;
    Button analyseButton;

    ImageView photoView;

    private static final String SHARED_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        VolleyRequest volley = new VolleyRequest(this);
        volley.RequestSTRINGYML("http://www-rech.telecom-lille.fr/freeorb/vocabulary.yml",this);
        volley.RequestSTRINGJSON("http://www-rech.telecom-lille.fr/freeorb/index.json",this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        captureButton = (Button) findViewById(R.id.captureButton);
        captureButton.setOnClickListener(this);

        libraryButton = (Button) findViewById(R.id.libraryButton);
        libraryButton.setOnClickListener(this);

        analyseButton = (Button) findViewById(R.id.analyseButton);
        analyseButton.setOnClickListener(this);

        photoView = (ImageView) findViewById(R.id.imageAnalysed);

        analysisIntent = new Intent(MainActivity.this, Analyse.class);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                captureButton.setEnabled(true); // If user doesn't have the permission the button is hidden
                Log.i(TAG, "Permission wasn't allowed but now is granted");
            }
        }
    }

    public void onClick(View view) {
        if (view == findViewById(R.id.captureButton)) {
            startCaptureActivity();
        } else if (view == findViewById(R.id.libraryButton)) {
            startLibraryActivity();
        } else if (view == findViewById(R.id.analyseButton)) {
            startAnalyseActivity();
        } else if (view == findViewById(R.id.websiteButton)) {
            startWebsiteActivity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri photoUri = data.getData();
        if (requestCode == libraryActivityResult && resultCode == RESULT_OK) {
            //Toast.makeText(this,data.getExtras().get("data").toString(),Toast.LENGTH_LONG).show();
            //Log.i(TAG, photoUri.toString());
            //photoUri.getScheme().toString();
            photoView.setImageURI(photoUri);
            //processPhotoLibraryResult(data);
            Uri imageUri = data.getData();
            // Flux pour lire les donnees de la carte SD
            try {
                InputStream inputStream;
                inputStream = getContentResolver().openInputStream(imageUri);
                // obtention d'une image Bitmap
                Bitmap image = BitmapFactory.decodeStream(inputStream);
                MethodesImage.ToCache(this, "/photoProjet", "image", image);
            }catch(Exception e){

            }
        }

        if (requestCode == captureActivityResult && resultCode == RESULT_OK){
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            photoView.setImageBitmap(photo);
            Toast.makeText(this,data.getExtras().get("data").toString() ,Toast.LENGTH_LONG).show();
            MethodesImage.ToCache(this,"/photoProjet","image", photo);
        }
        if (requestCode == ANALYSE_REQUEST_CODE && resultCode == RESULT_OK){
        }
    }

    protected void processPhotoLibrary(Intent intent) {
        Uri photoUri = intent.getData();
        String pathToPhoto = MethodesImage.getRealPath(getApplicationContext(), photoUri);

        File pathToFile = new File(pathToPhoto);
        Bitmap photoBitmap = MethodesImage.decodeFile(pathToFile); // err -> Maybe on path

        photoView.setImageBitmap(photoBitmap);

        Log.i(TAG, pathToPhoto);
    }

    protected void startAnalyseActivity() {
        startActivityForResult(analysisIntent, ANALYSE_REQUEST_CODE);
    }

    protected void startCaptureActivity() {
        //use standard intent to capture an image
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //we will handle the returned data in onActivityResult
        startActivityForResult(intent, captureActivityResult);
    }

    protected void startLibraryActivity() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"select photo lib"), libraryActivityResult);
    }

    protected void startWebsiteActivity() {
        //lance internet
        Uri uri = Uri.parse("http://www.google.com/#"); //faire un tableau comprenant les differents sites web des marques, puis, selon la marque, aller chercher le bon site dans le tableau
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}