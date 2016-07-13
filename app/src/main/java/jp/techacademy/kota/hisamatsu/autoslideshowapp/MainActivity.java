package jp.techacademy.kota.hisamatsu.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private Timer timer;
    private boolean playing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
        }

        // Get cursor
        ContentResolver resolver = getContentResolver();
        final Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
        );
        // Show first picture
        cursor.moveToFirst();
        getContentsInfo(cursor);

        // Prev button
        final Button buttonPrev = (Button) findViewById(R.id.buttonPrev);
        buttonPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPrevContentsInfo(cursor);
            }
        });

        // Next button
        final Button buttonNext = (Button) findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNextContentsInfo(cursor);
            }
        });

        // Play button
        final Button buttonPlay = (Button) findViewById(R.id.buttonPlay);
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playing) {
                    // Stop
                    timer.cancel();
                    buttonPlay.setText("再生");
                    buttonPrev.setEnabled(true);
                    buttonNext.setEnabled(true);
                    playing = false;
                } else {
                    // Start
                    timer = new Timer();
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    getNextContentsInfo(cursor);
                                }
                            });
                        }
                    }, 0, 2000);
                    buttonPlay.setText("停止");
                    buttonPrev.setEnabled(false);
                    buttonNext.setEnabled(false);
                    playing = true;
                }
            }
        });

    }

    private void getContentsInfo(Cursor cursor) {
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageURI(imageUri);
    }

    private void getNextContentsInfo(Cursor cursor) {
        if (cursor.moveToNext()) {
            getContentsInfo(cursor);
        } else {
            cursor.moveToFirst();
            getContentsInfo(cursor);
        }
    }

    private void getPrevContentsInfo(Cursor cursor) {
        if (cursor.moveToPrevious()) {
            getContentsInfo(cursor);
        } else {
            cursor.moveToLast();
            getContentsInfo(cursor);
        }
    }
}
