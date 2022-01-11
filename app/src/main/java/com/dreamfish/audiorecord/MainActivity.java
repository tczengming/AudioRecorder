package com.dreamfish.audiorecord;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.dreamfish.record.AudioRecorder;
import com.dreamfish.record.FileUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity implements View.OnClickListener {
    Button start;
    Button pause;
    Button pcmList;
    Button wavList;

    AudioRecorder audioRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        addListener();
        verifyPermissions(this);
        FileUtil.setRootPath(getExternalFilesDir("").getAbsolutePath());
    }

    //申请录音权限

    private static final int GET_RECODE_AUDIO = 1;

    private static String[] PERMISSION_ALL = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    /** 申请录音权限*/
    public static void verifyPermissions(Activity activity) {
        boolean permission = (ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED);
        if (permission) {
            ActivityCompat.requestPermissions(activity, PERMISSION_ALL,
                    GET_RECODE_AUDIO);
        }
    }

    private void addListener() {
        start.setOnClickListener(this);
        pause.setOnClickListener(this);
        pcmList.setOnClickListener(this);
        wavList.setOnClickListener(this);
    }

    private void init() {
        start = findViewById(R.id.start);
        pause = findViewById(R.id.pause);
        pcmList = findViewById(R.id.pcmList);
        wavList = findViewById(R.id.wavList);
        pause.setVisibility(View.GONE);
        audioRecorder = AudioRecorder.getInstance();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                try {
                    if (audioRecorder.getStatus() == AudioRecorder.Status.STATUS_NO_READY) {
                        //初始化录音
                        String fileName = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
                        //audioRecorder.createDefaultAudio(fileName);
                        audioRecorder.createAudio(fileName, MediaRecorder.AudioSource.MIC,
                                44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
                        audioRecorder.startRecord(null);

                        start.setText("停止录音");

                        pause.setVisibility(View.VISIBLE);

                    } else {
                        //停止录音
                        audioRecorder.stopRecord();
                        start.setText("开始录音");
                        pause.setText("暂停录音");
                        pause.setVisibility(View.GONE);
                    }

                } catch (IllegalStateException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.pause:
                try {
                    if (audioRecorder.getStatus() == AudioRecorder.Status.STATUS_START) {
                        //暂停录音
                        audioRecorder.pauseRecord();
                        pause.setText("继续录音");
                        break;

                    } else {
                        audioRecorder.startRecord(null);
                        pause.setText("暂停录音");
                    }
                } catch (IllegalStateException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.pcmList:
                Intent showPcmList = new Intent(MainActivity.this, ListActivity.class);
                showPcmList.putExtra("type", "pcm");
                startActivity(showPcmList);
                break;

            case R.id.wavList:
                Intent showWavList = new Intent(MainActivity.this, ListActivity.class);
                showWavList.putExtra("type", "wav");
                startActivity(showWavList);
                break;
            default:
                break;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (audioRecorder.getStatus() == AudioRecorder.Status.STATUS_START) {
            audioRecorder.pauseRecord();
            pause.setText("继续录音");
        }

    }

    @Override
    protected void onDestroy() {
        audioRecorder.release();
        super.onDestroy();

    }
}
