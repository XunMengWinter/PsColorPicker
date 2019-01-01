package top.wefor.pscolorpicker;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * reference Adobe PhotoShop color pick
 * Created by ice on 16/1/8.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * show picked color and value
     */
    @BindView(R.id.pickedColor_view) View mPickedColorView;
    @BindView(R.id.rgb_textView) TextView mRgbTextView;
    @BindView(R.id.hex_textView) TextView mHexTextView;

    /**
     * pick color;
     * gb = green and blue;
     * r = red.
     */
    @BindView(R.id.gb_picker_imageView) ImageView mGbPickerImageView;
    @BindView(R.id.r_picker_imageView) ImageView mRPickerImageView;
    @BindView(R.id.gb_view) ImageView mGbView;
    @BindView(R.id.r_view) ImageView mRView;

    /**
     * pick alpha and show alpha value;
     * a = alpha.
     */
    @BindView(R.id.a_seekBar) SeekBar mASeekBar;
    @BindView(R.id.a_textView) TextView mATextView;


    /**
     * init color
     */
    int mRed = 0x80, mGreen = 0x80, mBlue = 0x80;
    double mAlpha = 1;

    /**
     * they should be final, but I need to getResources()
     */
    int HALF_GB_PICKER_SIZE;
    int HALF_R_PICKER_SIZE;
    int RGB_SIZE;

    /**
     * init draw tools
     */
    Bitmap mGbBitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
    Bitmap mRBitmap = Bitmap.createBitmap(1, 256, Bitmap.Config.ARGB_8888);
    Canvas mGbCanvas = new Canvas(mGbBitmap);
    Canvas mRCanvas = new Canvas(mRBitmap);
    Paint mGbPaint = new Paint();
    Paint mRPaint = new Paint();

    /**
     * in order to change color smoothly when pick color
     */
    boolean isGbChanging, isRChanging;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mGbPaint.setStyle(Paint.Style.FILL);
        mRPaint.setStyle(Paint.Style.FILL);

        HALF_GB_PICKER_SIZE = getResources().getDimensionPixelSize(R.dimen.rgb_margin) / 2;
        HALF_R_PICKER_SIZE = getResources().getDimensionPixelSize(R.dimen.red_seek_size) / 2;
        RGB_SIZE = getResources().getDimensionPixelSize(R.dimen.rgb_size);

        drawGbView();
        drawRView();
        changeColor();

        initGbPickListener();
        initRPickListener();
        initASeekListener();
        mATextView.setText(getString(R.string.alpha_value, mAlpha));
    }

    private void changeColor() {
        int color = Color.argb((int) (mAlpha * 255), mRed, mGreen, mBlue);
        mPickedColorView.setBackgroundColor(color);
        mRgbTextView.setText(getString(R.string.rgb, mRed, mGreen, mBlue));
        mHexTextView.setText(getString(R.string.color_hex_value, Integer.toHexString(color)));
    }

    private void drawGbView() {
        if (isGbChanging) return;
        isGbChanging = true;
        Log.i("xyz", "drawGbView start " + System.currentTimeMillis());

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                // draw green and blue from left top to right bottom
                for (int y = 0; y < 256; y++) {
                    for (int x = 0; x < 256; x++) {
                        mGbPaint.setARGB((int) (mAlpha * 255), mRed, 255 - y, x);
                        mGbCanvas.drawPoint(x, y, mGbPaint);
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mGbView.setImageBitmap(mGbBitmap);
                isGbChanging = false;
                Log.i("xyz", "drawGbView end   " + System.currentTimeMillis());
            }
        }.execute();

    }

    private void drawRView() {
        if (isRChanging) return;
        isRChanging = true;

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                // draw red from top to bottom
                for (int y = 0; y < 256; y++) {
                    mRPaint.setARGB((int) (mAlpha * 255), 255 - y, mGreen, mBlue);
                    mRCanvas.drawPoint(0, y, mRPaint);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mRView.setImageBitmap(mRBitmap);
                isRChanging = false;
            }
        }.execute();
    }

    /**
     * pick green and blue color
     */
    private void initGbPickListener() {
        mGbView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        int selfX = (int) event.getX();
                        int selfY = (int) event.getY();
                        if (selfX < 0 || selfX >= RGB_SIZE) return false;
                        if (selfY < 0 || selfY >= RGB_SIZE) return false;

                        mGreen = 255 - (selfY * 256 / RGB_SIZE);
                        mBlue = selfX * 256 / RGB_SIZE;

                        mGbPickerImageView.layout(selfX - HALF_GB_PICKER_SIZE, selfY - HALF_GB_PICKER_SIZE,
                                selfX + HALF_GB_PICKER_SIZE, selfY + HALF_GB_PICKER_SIZE);

                        drawRView();
                        changeColor();
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * pick red color
     */
    private void initRPickListener() {
        mRView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        int selfY = (int) event.getY();
                        if (selfY < 0 || selfY >= RGB_SIZE) return false;

                        mRed = 255 - (selfY * 256 / RGB_SIZE);

                        mRPickerImageView.layout(mRPickerImageView.getLeft(), selfY - HALF_R_PICKER_SIZE,
                                mRPickerImageView.getRight(), selfY + HALF_R_PICKER_SIZE);

                        drawGbView();
                        changeColor();
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * control alpha
     */
    private void initASeekListener() {
        mASeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAlpha = progress * 1.0 / 100;
                mATextView.setText(getString(R.string.alpha_value, mAlpha));
                changeColor();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
