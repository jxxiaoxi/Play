package com.well.wellvideo.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.well.wellvideo.R;
import com.well.wellvideo.codec.Encoder;

/**
 * Created by liuwei on 5/25/16.
 */
public class MuxerActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText ed_audio,ed_video;
    private Button bt_muxer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muxer);
        bt_muxer = (Button) findViewById(R.id.bt_muxer);
        bt_muxer.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_muxer:
                startMuxer();
                break;
        }
    }

    private void startMuxer() {
        new Encoder().statMediaExtractor("");

    }


}
