package com.example.lanekeepassist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class SettingsActivity extends AppCompatActivity {

    ImageView imageView;
    EditText et_TLx, et_TLy, et_TRx, et_TRy, et_BLx, et_BLy, et_BRx, et_BRy;
    Button b_update;

    private Canvas mCanvas;
    private Paint mPaintRed = new Paint();
    private Bitmap orgBitmap, editBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeViews();

        mPaintRed.setColor(Color.RED);

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inMutable = true;
        orgBitmap = BitmapFactory.decodeResource(getResources(), R.raw.snap1, opt);

        b_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateImagePoints();
            }
        });
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
        b_update = findViewById(R.id.b_update);
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



    public static float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
