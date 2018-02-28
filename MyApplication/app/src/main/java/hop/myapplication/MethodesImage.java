package hop.myapplication;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Intent;
import android.util.Log;

public class MethodesImage {

    //declaration de variable
    static String  photoTakenPath;
    static Uri photoTakenUri;
    final String TAG = MainActivity.class.getName();

    //fin de declaration de variable
    //no validee
    protected static String getRealPath(Context context, Uri uri) {
        Cursor cursor;

        String[] projection = {MediaStore.Images.Media.DATA};
        cursor = context.getContentResolver().query(
                uri,
                projection,
                null,
                null,
                null
        );

        int dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(dataIndex);
    }

    protected static Bitmap decodeFile(File file) {
        Bitmap bitmap = null;
        try {
            FileInputStream inputStream = new FileInputStream(file); // System.err
            BitmapFactory.Options options = new BitmapFactory.Options();
            bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    // Create an image file name
    protected static File createImageFile(Context context) throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        // Save a file : path for use with ACTION_VIEW intents
        photoTakenPath = image.getAbsolutePath();
        photoTakenUri = Uri.fromFile(image);

        //galleryAddPic(image);
        //folder stuff
        /*File imagesFolder = new File(Environment.getExternalStorageDirectory(), "MyImages");
        imagesFolder.mkdirs();
        File image = new File(Environment.getExternalStorageDirectory(),"fname_" +
                String.valueOf(System.currentTimeMillis()) + ".jpg");
        Uri uriSavedImage = Uri.fromFile(image);*/
        return image;
    }

    public void galleryAddPic(Context context, File f) {
        Log.i(TAG, "TRUUUUC : " + f.getAbsolutePath());
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(photoTakenUri);
        context.sendBroadcast(mediaScanIntent);
    }

    public static void ToCache(Context context, String Path, String fileName, Bitmap nfiles) {
        FileOutputStream output;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        nfiles.compress(Bitmap.CompressFormat.JPEG,100,bos);
        Path = context.getCacheDir() + "/" + fileName;
        byte[] bitmapData = bos.toByteArray();
        try {
            output = new FileOutputStream(Path);
            output.write(bitmapData);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}