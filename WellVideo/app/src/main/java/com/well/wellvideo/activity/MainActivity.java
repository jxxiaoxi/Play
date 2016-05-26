package com.well.wellvideo.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.well.wellvideo.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tv_muxer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_muxer = (TextView) findViewById(R.id.tv_muxer);
        tv_muxer.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_muxer:
                startActivity(new Intent(MainActivity.this, MuxerActivity.class));
        }

    }
}
