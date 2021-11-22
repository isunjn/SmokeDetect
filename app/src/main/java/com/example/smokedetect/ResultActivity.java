package com.example.smokedetect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ResultActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS_EXTERNAL_STORAGE = 0;

    private File resultImageFile; // TODO: 处理图片重复生成的问题

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        String cropImagePath = intent.getStringExtra(MainActivity.EXTRA_CROP_IMAGE_PATH);
        int result = intent.getIntExtra(MainActivity.EXTRA_RESULT_NUMBER, 0);

        ImageView imageView = findViewById(R.id.image_view_result);
        imageView.setImageBitmap(BitmapFactory.decodeFile(cropImagePath));

        TextView textViewResult = findViewById(R.id.text_view_result);
        textViewResult.setText(String.valueOf(result));

        resultImageFile = null;
    }

    // 生成检测结果图（剪切后图片+检测结果）
    private void generateResultImage(boolean inSave) throws IOException {
        // 由view生成bitmap
        View v = findViewById(R.id.result_wrap);
        v.setDrawingCacheEnabled(true);
        Bitmap bitmap = v.getDrawingCache();

        // 保存bitmap到文件
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "RESULT_" + timeStamp;
        File storageDir;
        if (inSave) {
            storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        } else {
            storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        }
        File f = new File(storageDir.getAbsoluteFile(), imageFileName + ".png");
        FileOutputStream ostream = new FileOutputStream(f);
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, ostream);
        ostream.flush();
        ostream.close();
        resultImageFile = f;
    }

    // onClickListener 保存到系统相册
    public void saveToAlbum(View view) {
        // 在运行时申请存储权限
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_PERMISSIONS_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    generateResultImage(true);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "保存失败！", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(resultImageFile);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);
                Toast.makeText(this, "成功保存至相册！", Toast.LENGTH_LONG).show();
            }
        }
    }

    // onClickListener 调用系统分享
    public void shareToSystem(View view) {
        try {
            generateResultImage(false);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "结果图生成失败！", Toast.LENGTH_LONG).show();
            return;
        }
        Uri imageUri = FileProvider.getUriForFile(this, "com.example.smokedetect.fileprovider", resultImageFile);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        intent.setType("image/png");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "分享到"));
    }
}
