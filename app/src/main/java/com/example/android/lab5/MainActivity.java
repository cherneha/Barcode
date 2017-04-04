package com.example.android.lab5;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Button takePictureButton;
    private ImageView imageView;
    private Uri file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        takePictureButton = (Button)findViewById(R.id.takePicBut);
        imageView = (ImageView) findViewById(R.id.imageTaken);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            takePictureButton.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                takePictureButton.setEnabled(true);
            }
        }
    }

    public void takePicture(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = Uri.fromFile(getOutputMediaFile());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, file);

        startActivityForResult(intent, 100);
    }

    private static File getOutputMediaFile(){
        File photoFile = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "BarcodeApp");

        if (!photoFile.exists()){
            if (!photoFile.mkdirs()){
                Log.d("MainActivity", "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(photoFile.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");
    }

    private void addPhotoToGallery() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(file);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                imageView.setImageURI(file);
                addPhotoToGallery();
            }
        }
    }
}

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        Button takePictureButton = (Button)findViewById(R.id.takePicBut);
//        if (requestCode == 0) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
//                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//                takePictureButton.setEnabled(true);
//            }
//        }
//    }
//
//    private static final int CAMERA_CAPTURE = 1;
//    String absolutePath;
//
//    public void takePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            File photoFile = null;
//            try {
//                photoFile = createImageFile();
//            } catch (Exception ex) {
//                Log.e("MYAPP", "exception", ex);
//            }
//
//            if (photoFile != null) {
//                Uri photoURI = null;
//                try {
//                    photoURI = FileProvider.getUriForFile(this,
//                            "com.example.android.fileprovider",
//                            photoFile);
//                }catch (Exception ex){
//                    Log.e("MYAPP", "exception", ex);
//                }
//                Log.v("Main Activity", "+++2");
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//                Log.v("Main Activity", "+++3");
//                startActivityForResult(takePictureIntent, CAMERA_CAPTURE);
//                Log.v("Main Activity", "+++4");
//            }
//            Log.v("Main Activity", "****************************************444444**********");
//        }
//    }
//
//    private File createImageFile() throws Exception
//    {
//        File imgPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = "JPEG_" + timeStamp + "_";
//        File takenImg = File.createTempFile(imageFileName, ".jpg", imgPath);
//        absolutePath = takenImg.getAbsolutePath();
//        Log.v("Main Activity", absolutePath);
//        return takenImg;
//    }
//
//
//    private void galleryAddPic(){
//        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        Log.v("Main Activity", "&&&&&&&&&&");
//        Log.v("Main Activity", absolutePath);
//        File f = new File(absolutePath);
//        Log.v("Main Activity", "===2");
//        Uri contentUri = Uri.fromFile(f);
//        Log.v("Main Activity", "===3");
//        mediaScanIntent.setData(contentUri);
//        Log.v("Main Activity", "===4");
//        this.sendBroadcast(mediaScanIntent);
//    }
//
//    private void setPic() {
//        // Get the dimensions of the View
//        ImageView mImageView = (ImageView)findViewById(R.id.imageTaken);
//        Log.v("Main Activity", "===6");
//        int targetW = mImageView.getWidth();
//        int targetH = mImageView.getHeight();
//        Log.v("Main Activity", "===7");
//        // Get the dimensions of the bitmap
//        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//        bmOptions.inJustDecodeBounds = false;
//        bmOptions.inSampleSize = 3;
//        BitmapFactory.decodeFile(absolutePath, bmOptions);
//
//        Log.v("Main Activity", "===10");
//        int photoW = bmOptions.outWidth;
//        int photoH = bmOptions.outHeight;
//
//
//        Log.v("Main Activity", "===11");
//        // Determine how much to scale down the image
//        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
//        Log.v("Main Activity", "===12");
//
//        // Decode the image file into a Bitmap sized to fill the View
//        bmOptions.inJustDecodeBounds = true;
//        bmOptions.inSampleSize = scaleFactor;
//
//        Bitmap bitmap = BitmapFactory.decodeFile(absolutePath, bmOptions);
//        mImageView.setImageBitmap(bitmap);
//    }
//
//    public void takingPicture(){
//        Log.v("Main Activity", "**********!!!!!!!!!!!!!!!!!!!**************");
//        takePictureIntent();
//        galleryAddPic();
//        setPic();
//    }
//
//    public void takingPictureButton(View view) {
//        Log.v("Main Activity", "###################################");
//        takingPicture();
//    }
//}
