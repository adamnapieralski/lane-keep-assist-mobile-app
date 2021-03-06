package com.example.lanekeepassist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    ImageView imageView;
    EditText et_TLx, et_TLy, et_TRx, et_TRy, et_BLx, et_BLy, et_BRx, et_BRy,
            et_RGB_R, et_HLS_L, et_LAB_L;
    Button b_update, b_save;
    SeekBar seekThresh;

    private Canvas mCanvas;
    private Paint mPaintRed = new Paint();
    private Bitmap orgBitmap, editBitmap;

    private BluetoothHandler btHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeViews();

        Intent i = getIntent();
        byte[] imageByteArray = i.getByteArrayExtra("ImageByteArray");
        Bitmap bitmap= BitmapFactory.decodeByteArray(imageByteArray,0,imageByteArray.length);

        orgBitmap = bitmap.copy(bitmap.getConfig(), true);

        String settingPoints = i.getStringExtra("Settings");

        handleSettingsReceived(settingPoints);

        mPaintRed.setColor(Color.RED);

        btHandler = new BluetoothHandler();
        btHandler.setSettingsActivity(this);

//        BitmapFactory.Options opt = new BitmapFactory.Options();
//        opt.inMutable = true;
//        orgBitmap = BitmapFactory.decodeResource(getResources(), R.raw.snap1, opt);

        updateImagePoints();


        b_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateImagePoints();
            }
        });

        b_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), MainActivity.class);

                String TLx = et_TLx.getText().toString();
                String TLy = et_TLy.getText().toString();
                String TRx = et_TRx.getText().toString();
                String TRy = et_TRy.getText().toString();
                String BLx = et_BLx.getText().toString();
                String BLy = et_BLy.getText().toString();
                String BRx = et_BRx.getText().toString();
                String BRy = et_BRy.getText().toString();

                String seekThreshStr = Integer.toString(seekThresh.getProgress());

//                String RGB_R = et_RGB_R.getText().toString();
//                String HLS_L = et_HLS_L.getText().toString();
//                String LAB_L = et_LAB_L.getText().toString();

                String del = " ";

                String settings = "save_settings" + del + BLx + del + BLy + del + BRx + del + BRy
                        + del + TRx + del + TRy + del + TLx + del + TLy + del + seekThreshStr;

                i.putExtra("settings", settings);
                setResult(0, i);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("settings", "");
        setResult(0, i);
        finish();
    }

    private void initializeViews() {
        imageView = findViewById(R.id.imageView);
        et_TLx = findViewById(R.id.et_TLx);
        et_TLy = findViewById(R.id.et_TLy);
        et_TRx = findViewById(R.id.et_TRx);
        et_TRy = findViewById(R.id.et_TRy);
        et_BLx = findViewById(R.id.et_BLx);
        et_BLy = findViewById(R.id.et_BLy);
        et_BRx = findViewById(R.id.et_BRx);
        et_BRy = findViewById(R.id.et_BRy);

//        et_RGB_R = findViewById(R.id.et_RGB_R);
//        et_HLS_L = findViewById(R.id.et_HLS_L);
//        et_LAB_L = findViewById(R.id.et_LAB_L);

        seekThresh = findViewById(R.id.seekThresh);

        b_update = findViewById(R.id.b_update);
        b_save = findViewById(R.id.b_save);
    }

    private void updateImagePoints() {
        editBitmap = orgBitmap.copy(orgBitmap.getConfig(), true);
        mCanvas = new Canvas(editBitmap);
        mCanvas.drawBitmap(editBitmap, 0, 0, null);

        int TLx = Integer.parseInt(et_TLx.getText().toString());
        int TLy = Integer.parseInt(et_TLy.getText().toString());
        int TRx = Integer.parseInt(et_TRx.getText().toString());
        int TRy = Integer.parseInt(et_TRy.getText().toString());
        int BLx = Integer.parseInt(et_BLx.getText().toString());
        int BLy = Integer.parseInt(et_BLy.getText().toString());
        int BRx = Integer.parseInt(et_BRx.getText().toString());
        int BRy = Integer.parseInt(et_BRy.getText().toString());

        float pTLx = convertDpToPixel(TLx, this);
        float pTLy = convertDpToPixel(TLy, this);
        float pTRx = convertDpToPixel(TRx, this);
        float pTRy = convertDpToPixel(TRy, this);
        float pBLx = convertDpToPixel(BLx, this);
        float pBLy = convertDpToPixel(BLy, this);
        float pBRx = convertDpToPixel(BRx, this);
        float pBRy = convertDpToPixel(BRy, this);

        mCanvas.drawCircle(pTLx, pTLy, 15, mPaintRed);
        mCanvas.drawCircle(pTRx, pTRy, 15, mPaintRed);
        mCanvas.drawCircle(pBLx, pBLy, 15, mPaintRed);
        mCanvas.drawCircle(pBRx, pBRy, 15, mPaintRed);

        mPaintRed.setStrokeWidth(3);
        mCanvas.drawLine(pTLx, pTLy, pTRx, pTRy, mPaintRed);
        mCanvas.drawLine(pTRx, pTRy, pBRx, pBRy, mPaintRed);
        mCanvas.drawLine(pBRx, pBRy, pBLx, pBLy, mPaintRed);
        mCanvas.drawLine(pBLx, pBLy, pTLx, pTLy, mPaintRed);

        imageView.setImageBitmap(editBitmap);
    }

    public void handleSettingsReceived(String settings) {
        String[] val = settings.split(" ");

        if (val.length == 10) {
            et_BLx.setText(val[1]);
            et_BLy.setText(val[2]);
            et_BRx.setText(val[3]);
            et_BRy.setText(val[4]);
            et_TRx.setText(val[5]);
            et_TRy.setText(val[6]);
            et_TLx.setText(val[7]);
            et_TLy.setText(val[8]);

            int threshVal;
            try {
                threshVal = Integer.parseInt(val[9]);
                seekThresh.setProgress(threshVal);
            } catch (NumberFormatException e) {
                seekThresh.setProgress(0);
            }

//            et_RGB_R.setText(val[9]);
//            et_HLS_L.setText(val[10]);
//            et_LAB_L.setText(val[11]);
        }
    }



    public static float convertDpToPixel(float dp, Context context){
        return dp;// * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
