package hop.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.example.R;
import com.squareup.picasso.Picasso;

import static org.bytedeco.javacpp.opencv_highgui.cvLoadImage;
import static org.bytedeco.javacpp.opencv_highgui.imread;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_features2d.BOWImgDescriptorExtractor;
import org.bytedeco.javacpp.opencv_features2d.FlannBasedMatcher;
import org.bytedeco.javacpp.opencv_features2d.KeyPoint;
import org.bytedeco.javacpp.opencv_ml.CvSVM;
import org.bytedeco.javacpp.opencv_nonfree.SIFT;
import org.json.JSONException;
import org.json.JSONObject;


public class Analyse extends AppCompatActivity implements View.OnClickListener {

    public void onClick(View view) {
    }

    static ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analyse);
        startAnalyseActivity(getApplicationContext());
        logo = (ImageView) findViewById(R.id.logo);
    }

    public static void startAnalyseActivity(Context context) {
        //prepare BOW descriptor extractor from the vocabulary already computed
        //final String pathToVocabulary = "vocabulary.yml" ; // to be define
        final opencv_core.Mat vocabulary;

        System.out.println("read vocabulary from file... ");
        Loader.load(opencv_core.class);
        opencv_core.CvFileStorage storage = opencv_core.cvOpenFileStorage(context.getCacheDir().toString() + File.separator + "/vocabulary", null, opencv_core.CV_STORAGE_READ); //yml en cache
        Pointer p = opencv_core.cvReadByName(storage, null, "vocabulary", opencv_core.cvAttrList());
        opencv_core.CvMat cvMat = new opencv_core.CvMat(p);
        vocabulary = new opencv_core.Mat(cvMat);
        System.out.println("azerty : vocabulary loaded " + vocabulary.rows() + " x " + vocabulary.cols());
        opencv_core.cvReleaseFileStorage(storage);

        //create SIFT feature point extracter
        final SIFT detector;
        // default parameters ""opencv2/features2d/features2d.hpp""
        detector = new SIFT(0, 3, 0.04, 10, 1.6);

        //create a matcher with FlannBased Euclidien distance (possible also with BruteForce-Hamming)
        final FlannBasedMatcher matcher;
        matcher = new FlannBasedMatcher();

        //create BoF (or BoW) descriptor extractor
        final BOWImgDescriptorExtractor bowide;
        bowide = new BOWImgDescriptorExtractor(detector.asDescriptorExtractor(), matcher);

        //Set the dictionary with the vocabulary we created in the first step
        bowide.setVocabulary(vocabulary);
        System.out.println("Vocab   azert " + vocabulary);

        File file = new File(context.getCacheDir().toString() + File.separator + "/index");

        try {
            JSONObject JSON = new JSONObject(getFileContents(file));
            int classNumber = JSON.getJSONArray("brands").length();

            String[] class_names;
            class_names = new String[classNumber];
            for (int i = 0; i < JSON.getJSONArray("brands").length(); i++) {
                class_names[i] = JSON.getJSONArray("brands").getJSONObject(i).getString("classifier").substring(0, JSON.getJSONArray("brands").getJSONObject(i).getString("classifier").lastIndexOf('.'));
            }

            final CvSVM[] classifiers;
            //System.out.println("azerty : " +classNumber);
            classifiers = new CvSVM[classNumber];
            for (int i = 0; i < classNumber; i++) {
                //System.out.println("Ok. Creating class name from " + className);
                //open the file to write the resultant descriptor
                classifiers[i] = new CvSVM();
                classifiers[i].load(context.getCacheDir().toString() + File.separator + class_names[i] + ".xml");
                System.out.println("Vocab   azert " + context.getCacheDir().toString() + File.separator + class_names[i] + ".xml");
                System.out.println("Vocab   azert " + classifiers[i].get_support_vector_count());
            }

            Mat response_hist = new Mat();
            KeyPoint keypoints = new KeyPoint();
            Mat inputDescriptors = new Mat();

            //System.out.println("path:" + im.getName());
            Mat imageTest = imread(context.getCacheDir() + "/" + "image", 1);
            detector.detectAndCompute(imageTest, Mat.EMPTY, keypoints, inputDescriptors);
            bowide.compute(imageTest, keypoints, response_hist);

            // Finding best match
            float minf = Float.MAX_VALUE;
            String bestMatch = null;

            //long timePrediction = System.currentTimeMillis();
            // loop for all classes
            System.out.println("azerty 1 : " + classNumber);
            for (int i = 0; i < classNumber; i++) {
                // classifier prediction based on reconstructed histogram
                System.out.println("azerty 2 :" + class_names[i] + " " + classifiers[i]);
                float res = classifiers[i].predict(response_hist, true);
                System.out.println("azerty 3 :" + res);
                System.out.println("azerty 4 :" + class_names[i] + " is " + res);
                if (res < minf) {
                    minf = res;
                    bestMatch = class_names[i];
                }
            }
            // timePrediction = System.currentTimeMillis() - timePrediction;
            System.out.println("L'image  predicted as " + bestMatch + " in " + 0 + " ms");
            Toast.makeText(context, "L'image  predicted as " + bestMatch + " in " + 0 + " ms", Toast.LENGTH_LONG).show();
            //VolleyRequest.requestImage(logo, url);
            VolleyRequest volley = new VolleyRequest(context);
            String url = "";
            for(int i = 0;i < classNumber; i++){

                if(bestMatch.equals(JSON.getJSONArray("brands").getJSONObject(i).getString("classifier").substring(0, JSON.getJSONArray("brands").getJSONObject(i).getString("classifier").lastIndexOf('.')))){
                    url += "http://www-rech.telecom-lille.fr/freeorb/train-images/" + JSON.getJSONArray("brands").getJSONObject(i).getJSONArray("images").getString(0);
                }
            }
            System.out.println("azerty :"+url);
            volley.requestImage(context,url);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void photo (Context context, ImageView logo){
        Uri myUri = Uri.parse(context.getCacheDir() + "/" + "imageResponse");
        logo.setImageURI(myUri);
    }

    public static String getFileContents(final File file) throws IOException {
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
}