package com.example.smokedetect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class RecordListActivity extends AppCompatActivity {

    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_list);
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "db_smokedetect").allowMainThreadQueries().build();
        fn();
    }

    void fn() {
        List<DetectRecord> detectRecordList = db.detectRecordDao().getAll();
        LayoutInflater vi = getLayoutInflater();
        ViewGroup insertPoint = (ViewGroup) findViewById(R.id.insert_point);

        for (DetectRecord r : detectRecordList) {
            View v = vi.inflate(R.layout.item_record_card, null);

            ImageView imageView = (ImageView) v.findViewById(R.id.item_image_view);
            imageView.setImageBitmap(BitmapFactory.decodeFile(r.path));

            TextView textView = (TextView) v.findViewById(R.id.item_text_view);
            textView.setText("林格曼黑度：" + r.result);

            insertPoint.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

}