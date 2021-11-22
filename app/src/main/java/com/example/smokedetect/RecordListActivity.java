package com.example.smokedetect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class RecordListActivity extends AppCompatActivity {

    AppDatabase db; // TODO: 处理数据库重复建立的问题，改为单例模式

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_list);
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "db_smokedetect").allowMainThreadQueries().build();
        generateRecordList();
    }

    // 生成历史记录view
    void generateRecordList() {
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

    // TODO: 删除记录

    // TODO: 点开记录到结果页
}