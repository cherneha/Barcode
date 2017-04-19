package com.example.android.lab5;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;


public class MainActivity extends AppCompatActivity {

    private Button takePictureButton;
    private ImageView imageView;
    private Uri file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        takePictureButton = (Button) findViewById(R.id.takePicBut);
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
        Log.v("Main Activity", file.toString());
        startActivityForResult(intent, 100);
    }

    private static File getOutputMediaFile() {
        File photoFile = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "BarcodeApp");

        if (!photoFile.exists()) {
            if (!photoFile.mkdirs()) {
                Log.d("MainActivity", "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(photoFile.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");
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
                Log.v("Main Activity", "++++++++++++++++++++++");
                processImage();
                addPhotoToGallery();
            }
        }
    }
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
    }
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("SUCCESS", "OpenCV loaded successfully");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {

        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);

        return resizedBitmap;

    }
    public void processImage() {
        Mat img;
        Log.v("Main Activity", file.toString());
        img = Imgcodecs.imread(file.getPath(), Imgcodecs.IMREAD_GRAYSCALE);
        int x = img.cols();
        int y = img.rows();
        Mat xGrad = new Mat(x, y, CvType.CV_8UC1);
        Mat thres = new Mat(x, y, CvType.CV_8UC1);
        Mat blurred = new Mat(x, y, CvType.CV_8UC1);
        Mat er = new Mat(x, y, CvType.CV_8UC1);
        Mat dil = new Mat(x, y, CvType.CV_8UC1);
        Mat morph = new Mat(x, y, CvType.CV_8UC1);
        Mat finl = new Mat(x, y, CvType.CV_8UC1);

        Mat copy = Imgcodecs.imread(file.getPath(), Imgcodecs.IMREAD_COLOR);
        Mat copyOfCopy = copy.clone();
        Imgproc.Sobel(img, xGrad, CvType.CV_8UC1, 1, 0, -1, 1, 0);

        Imgproc.blur(xGrad, blurred, new Size(2, 2), new Point(-1,-1));
        Imgproc.threshold(blurred, thres, 230, 255, Imgproc.THRESH_BINARY);
        Mat strEl = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 12));

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15, 2));
        Mat kernel1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(40, 40));
        Mat kernel2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(50, 50));

        Imgproc.erode(thres, er, strEl, new Point(-1, -1), 4);
        Imgproc.dilate(er, dil, kernel, new Point(-1, -1), 4);
        Imgproc.erode(dil, morph, kernel1, new Point(-1, -1), 2);
        Imgproc.dilate(morph, finl, kernel1, new Point(-1, -1), 2);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(finl, contours, new Mat(), 0, 1);
        Scalar color = new Scalar(0, 255, 0);
        Imgproc.drawContours(copyOfCopy, contours, -1, color, 6);
        Imgproc.cvtColor(copyOfCopy, copy, Imgproc.COLOR_BGR2RGB);

        Bitmap temp;

        temp = Bitmap.createBitmap(finl.cols(), finl.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(finl, temp);
        Bitmap t = getResizedBitmap(temp, imageView.getHeight(), imageView.getWidth());
        imageView.setImageBitmap(t);

    }
}