package hop.myapplication;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;


public class VolleyRequest extends AppCompatActivity implements View.OnClickListener{

    final String TAG = MainActivity.class.getName();

    //String urlYML = "http://www-rech.telecom-lille.fr/nonfreesift/vocabulary.yml";
    //String urlJSON = "http://www-rech.telecom-lille.fr/nonfreesift/index.json";
    String name = "";
    RequestQueue queue;

    public VolleyRequest(Context context) {
        this.queue = com.android.volley.toolbox.Volley.newRequestQueue(context);
    }

    public void onClick(View view) {
    }

    public void RequestSTRINGYML(String url, final Context context){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        // mTextView.setText("Response is: "+ response.substring(0,500));
                        // Log.d(TAG,"@@REST"+"Response is: "+ response.toString());
                        Log.d(TAG,"@@REST vocab"+" Response is: ok");

                        try
                        {
                            File fileDirectory = new File(context.getCacheDir().toString()+File.separator);
                            File file = new File(fileDirectory, "vocabulary");

                            file.createNewFile();

                            FileOutputStream fOut = new FileOutputStream(file);
                            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                            myOutWriter.append(response);
                            myOutWriter.close();
                            fOut.flush();
                            fOut.close();
                        }
                        catch (IOException e)
                        {
                            Log.e("Exception", "File write failed: " + e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // mTextView.setText("That didn't work!");
                        Log.d(TAG,"@@REST That didn't work!");
                        // Toast.makeText(,TAG+"Return ok ",Toast.LENGTH_SHORT).show();
                    }
                }
        );
        queue.add(stringRequest);
    }

    public void RequestSTRINGJSON(String urlJSON, final Context context){
        //obtention et stockage du fichier JSON
        JsonObjectRequest JSONRequest = new JsonObjectRequest(Request.Method.GET, urlJSON, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject JSON) {
                Log.d(TAG, "@@REST index" + " Response is: ok");
                try {
                    File fileDirectory = new File(context.getCacheDir().toString() + File.separator);
                    File file = new File(fileDirectory, "index");

                    file.createNewFile();

                    FileOutputStream fOut = new FileOutputStream(file);
                    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                    myOutWriter.append(JSON.toString());
                    myOutWriter.close();
                    fOut.flush();
                    fOut.close();

                    int classNumber = JSON.getJSONArray("brands").length();
                    for(int i=0;i<JSON.getJSONArray("brands").length(); i++){
                        // Log.d(TAG, "@@JSON " + "brandname: " + i + " " +  json.getJSONArray("brands").getJSONObject(i).getString("brandname"));
                        // Log.d(TAG, "@@JSON " + "url: " + i + " " +  json.getJSONArray("brands").getJSONObject(i).getString("url"));
                        // Log.d(TAG, "@@JSON " + "classifieur: " + i + " " +  json.getJSONArray("brands").getJSONObject(i).getString("classifier"));
                        // Log.d(TAG, "@@JSON " + "image: " + i + " " +  json.getJSONArray("brands").getJSONObject(i).getJSONArray("images").getString(0));
                        name =JSON.getJSONArray("brands").getJSONObject(i).getString("classifier");
                        System.out.println("azerty "+name);
                        String u="http://www-rech.telecom-lille.fr/freeorb/classifiers/"+JSON.getJSONArray("brands").getJSONObject(i).getString("classifier");
                        Log.d(TAG, "@@REST json " + "URL: " +u);
                        requestXML(context, u, name);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
                ,null);
        queue.add(JSONRequest);
    }

    private void requestXML(final Context context, String u,final String name){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, u,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        // mTextView.setText("Response is: "+ response.substring(0,500));
                        // Log.d(TAG,"@@REST1"+"Response is: "+ response.toString())
                        try
                        {
                            File fileDirectory = new File(context.getCacheDir().toString()+File.separator);
                            File file = new File(fileDirectory, name);
                            System.out.println("azerty NAME:"+name);
                            file.createNewFile();
                            System.out.println("azerty "+context.getCacheDir().toString()+File.separator+name);
                            System.out.println("azerty "+file.length());
                            FileOutputStream fOut = new FileOutputStream(file);
                            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                            myOutWriter.append(response);
                            myOutWriter.close();
                            fOut.flush();
                            fOut.close();
                        }
                        catch (IOException e)
                        {
                            Log.e("Exception", "File write failed: " + e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // mTextView.setText("That didn't work!");
                        Log.d(TAG,"@@REST That didn't work!");
                    }
                }
        );
        queue.add(stringRequest);
    }
}