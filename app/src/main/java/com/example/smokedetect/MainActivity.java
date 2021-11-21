package com.example.smokedetect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.room.Room;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_RESULT_NUMBER = "com.example.smokedetect.RESULT_NUMBER";
    public static final String EXTRA_CROP_IMAGE_PATH = "com.example.smokedetect.CROP_IMAGE_PATH";

    public static final int REQUEST_IMAGE_CAPTURE = 0;
    public static final int REQUEST_IMAGE_PICK = 1;
    public static final int REQUEST_IMAGE_CROP = 2;

    private File imageFile;
    private File cropImageFile;
    private String cropImagePath;

    private ImageView imageView;
    private RadioGroup weatherChooseRadioGroup;
    private Button confirmButton;

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.image_view_result);
        weatherChooseRadioGroup = findViewById(R.id.radioGroup);
        confirmButton = findViewById(R.id.btnConfirm);
        imageFile = null;
        cropImageFile = null;
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "db_smokedetect").allowMainThreadQueries().build();
    }

    // 创建图片文件，拍照产生的图片 && 剪切后的图片
    private File createImageFile(boolean isCrop) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName;
        if (isCrop) {
            imageFileName = "CROP_" + timeStamp + "_";
        } else {
            imageFileName = "JPEG_" + timeStamp + "_";
        }
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File f;
        try {
            f = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            if (isCrop) {
                cropImagePath = f.getPath();
            }
            return f;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 拍照生成图片
    public void takeImageFromCamera(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageFile = createImageFile(false);
        if (imageFile != null) {
            Uri imageUri;
            if(Build.VERSION.SDK_INT>=24){
                imageUri = FileProvider.getUriForFile(MainActivity.this,"com.example.smokedetect.fileprovider", imageFile);
            }else{
                imageUri =Uri.fromFile(imageFile);
            }
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    // 相册选择图片
    public void pickImageFromLocal(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    // 图片剪切
    public void getImageCrop(Uri uri) {
        cropImageFile = createImageFile(true);
        if (cropImageFile != null) {
            Uri imageCropUri = Uri.fromFile(cropImageFile);
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(uri, "image/*")
                    .putExtra("crop", "true")
                    .putExtra("aspectX", 1)
                    .putExtra("aspectY", 1)
                    .putExtra("outputX", 900)
                    .putExtra("outputY", 900)
                    .putExtra("scale", true)
                    .putExtra("return-data", false)
                    .putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
                    .putExtra(MediaStore.EXTRA_OUTPUT, imageCropUri)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_IMAGE_CROP);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Uri imageUri = FileProvider.getUriForFile(MainActivity.this,"com.example.smokedetect.fileprovider", imageFile);
            getImageCrop(imageUri);
            return;
        }

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            getImageCrop(data.getData());
            return;
        }

        if (requestCode == REQUEST_IMAGE_CROP && resultCode == RESULT_OK) {
            imageView.setImageBitmap(BitmapFactory.decodeFile(cropImageFile.getAbsolutePath()));
            weatherChooseRadioGroup.setVisibility(View.VISIBLE);
            confirmButton.setVisibility(View.VISIBLE);
        }
    }

    // 触发图像处理
    public void confirmToProcess(View view) {
        // Toast 开始处理
        int weatherLevel = 0;
        int selectID = weatherChooseRadioGroup.getCheckedRadioButtonId();
        switch (selectID) {
            case R.id.radio_weather_0:
                weatherLevel = 0;
                break;
            case R.id.radio_weather_1:
                weatherLevel = 1;
                break;
            case R.id.radio_weather_2:
                weatherLevel = 2;
                break;
        }
        int result = getImageResult(weatherLevel);

        // 保存路径和结果到数据库
        DetectRecord r = new DetectRecord(cropImagePath, result);
        db.detectRecordDao().insert(r);

        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(EXTRA_RESULT_NUMBER, result);
        intent.putExtra(EXTRA_CROP_IMAGE_PATH, cropImagePath);
        startActivity(intent);
    }

    // 由位图计算林格曼黑度
    int getImageResult(int weatherLevel) {
        Bitmap bitmap = BitmapFactory.decodeFile(cropImageFile.getAbsolutePath());
        List<Double> greyScaleList = new ArrayList<Double>();
        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                int color = bitmap.getPixel(x, y);
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);
                double greyScale = r * 0.299 + g * 0.587 + b * 0.114;
                greyScaleList.add(greyScale);
            }
        }
        double sum = 0;
        for (double d : greyScaleList) {
            sum += d;
        }
        double average = sum / greyScaleList.size();
        double gray = 1 - average / 255;

        double adjustment = 0;
        switch (weatherLevel) {
            case 0:
                adjustment = 0.01;
                break;
            case 1:
                adjustment = 0.05;
                break;
            case 2:
                adjustment = 0.10;
                break;
        }

        int RingelmannScale;
        if (gray <= 0.2 + adjustment ) {
            RingelmannScale = 0;
        } else if (gray < 0.4 + adjustment ) {
            RingelmannScale = 1;
        } else if (gray < 0.6 + adjustment ) {
            RingelmannScale = 2;
        } else if (gray < 0.8 + adjustment ) {
            RingelmannScale = 3;
        } else if (gray < 1 - adjustment) {
            RingelmannScale = 4;
        } else {
            RingelmannScale = 5;
        }
        return RingelmannScale;
    }

    public void showRecordList(View view) {
        Intent intent = new Intent(this, RecordListActivity.class);
        startActivity(intent);
    }
}