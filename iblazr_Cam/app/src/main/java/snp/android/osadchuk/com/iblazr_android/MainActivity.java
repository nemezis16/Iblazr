package snp.android.osadchuk.com.iblazr_android;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import snp.android.osadchuk.com.iblazr_android.Animation.LeftToMiddle;
import snp.android.osadchuk.com.iblazr_android.Animation.LeftToRight;
import snp.android.osadchuk.com.iblazr_android.Animation.MiddleToLeft;
import snp.android.osadchuk.com.iblazr_android.Animation.MiddleToRight;
import snp.android.osadchuk.com.iblazr_android.Animation.RightToLeft;
import snp.android.osadchuk.com.iblazr_android.Animation.RightToMiddle;
import snp.android.osadchuk.com.iblazr_android.AudioJackReceiver.AudioJackOut;
import snp.android.osadchuk.com.iblazr_android.Iblazr_Library.constantlight.GeneratorConstThread;
import snp.android.osadchuk.com.iblazr_android.Iblazr_Library.shot.GeneratorPulseThread;

import static android.hardware.Camera.PictureCallback;

public class MainActivity extends Activity {

    private boolean isFinished=true;

    private int mIndexView1=0;
    private int mIndexView2=1;
    private int mIndexView3=2;

    private int mCurrentIndex1;
    private int mCurrentIndex2;
    private int mCurrentIndex3;

    SurfaceView sv;
    SurfaceHolder holder;
    HolderCallback holderCallback;
    Camera camera;
    private static final String FIRST_LOG = "first_log";
     int CAMERA_ID;
    final boolean FULL_SCREEN = true;
    OrientationEventListener myOrientationEventListener;
    Animation animClockwise;
    Animation animCounterclockwise;
    //BackgroundTask mBackgroundTask;
    LinearLayout natflash;
    LinearLayout galleryIco;
    LinearLayout frontIco;
    String orientationPortrait="portrait";
    String orientationLandscape="landscape";
    String orientationPortrait2="portrait 2";
    String orientationLandscape2="landscape 2";
    String orientationBefore;
    ImageView iblazerico;
    public static final int RESULT_GALLERY = 0;
    private String selectedImagePath;
    private String fileManagerString;
    private static  final int FOCUS_AREA_SIZE= 300;
    Camera.Face[] detectedFaces;
    DrawingView drawingView;
    private ScheduledExecutorService myScheduledExecutorService;
    Matrix matrix;
    RectF rectF;
    AudioJackOut mAudioJackOut;
    GeneratorConstThread generator;
    // minimum value of frequency
    public static final int fmin = 1300;
    // maximum value of frequency
    public static final int fmax = 9400;
    private SeekBar slbar;
    private static final String KEY_INDEX1="index1";
    private static final String KEY_INDEX2="index2";
    private static final String KEY_INDEX3="index3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.cam);
        mAudioJackOut = new AudioJackOut();

        galleryIco=(LinearLayout)findViewById(R.id.galleryIco);
        natflash=(LinearLayout)findViewById(R.id.nativeIco);
        frontIco=(LinearLayout)findViewById(R.id.frontIco);

        if (savedInstanceState!=null) {
            mIndexView1 = savedInstanceState.getInt(KEY_INDEX1);
            mIndexView1 = savedInstanceState.getInt(KEY_INDEX2);
            mIndexView1 = savedInstanceState.getInt(KEY_INDEX3);
        }

        //closing intro by touch
        final LinearLayout intro = (LinearLayout) findViewById(R.id.introducingLayout);
        intro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intro.setVisibility(View.INVISIBLE);
            }
        });

       /* final LinearLayout intro = (LinearLayout) findViewById(R.id.introducingLayout);
        if(handleHeadphonesState(getApplicationContext())){
            intro.setVisibility(View.INVISIBLE);
        }else{
            intro.setVisibility(View.VISIBLE);
        }*/

        galleryIco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                String path= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
                Uri picturePath=android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                i.setDataAndType(picturePath, "image/picture*//*");
                startActivity(i);*/

                Intent i = new Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 0);

                /*Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivity(i);*/
            }
        });

        final ImageView flash1 = (ImageView) findViewById(R.id.flash1);
        final ImageView flash2 = (ImageView) findViewById(R.id.flash2);
        final ImageView flash3 = (ImageView) findViewById(R.id.flash3);
        iblazerico=(ImageView)findViewById(R.id.iblazerico);

        //FrameLayout focusDetect = (FrameLayout) findViewById(R.id.focusDetect);
        sv = (SurfaceView) findViewById(R.id.surfaceView);
        holder = sv.getHolder();
        // holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holderCallback = new HolderCallback();
        holder.addCallback(holderCallback);

        //adding rectangles for focusing
        drawingView = new DrawingView(this);
        ViewGroup.LayoutParams layoutParamsDrawing
                = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT);
        this.addContentView(drawingView, layoutParamsDrawing);

        final View view[] = {flash1, flash2, flash3};

        // initial position of bottom toolbar
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(view[mIndexView1].getLeft(), view[mIndexView1].getTop() + 100, view[mIndexView1].getRight(), view[mIndexView1].getBottom());
        view[mIndexView1].setLayoutParams(params);

        FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params2.setMargins(view[mIndexView2].getLeft() + 100, view[mIndexView2].getTop(), view[mIndexView2].getRight(), view[mIndexView2].getBottom());
        view[mIndexView2].setLayoutParams(params2);

        FrameLayout.LayoutParams params3 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params3.setMargins(view[mIndexView3].getLeft() + 200, view[mIndexView3].getTop() + 100, view[mIndexView3].getRight(), view[mIndexView3].getBottom());
        view[mIndexView3].setLayoutParams(params3);

        /*requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        // no need anymore because it did in manifest in android:theme


        slbar =(SeekBar)findViewById(R.id.seekBar2);
        slbar.setMax(fmax-fmin);
        slbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar view, int arg1, boolean fromUser) {
                if (view==slbar){
                    if (null!=generator) generator.setFrequency(slbar.getProgress()+fmin);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar view) {
                if (view == slbar){
                    if (null!=generator) generator.setFrequency(slbar.getProgress()+fmin);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar view) {
                if (view==slbar){
                    if (null!=generator) generator.setFrequency(slbar.getProgress()+fmin);
                }
            }
        });

        //setting animation for top elements by getting orientation
        try {
            myOrientationEventListener = new OrientationEventListener(MainActivity.this, SensorManager.SENSOR_DELAY_NORMAL) {
                @Override
                public void onOrientationChanged(int arg0) {
                    if (arg0 <= 45 || arg0 >= 315) {

                        if(orientationBefore==orientationLandscape){
                            orientationBefore=orientationPortrait;
                            animCounterClockwise();

                        }else if (orientationBefore == orientationLandscape2) {
                            orientationBefore=orientationPortrait;
                            animClockwise();
                        }else{
                            orientationBefore=orientationPortrait;
                        }
                        // mOrientation.setText("Portrait" + String.valueOf(arg0));

                    } else if (arg0 > 45 && arg0 <= 135) {

                        if(orientationBefore==orientationPortrait2){
                            orientationBefore=orientationLandscape2;

                            animClockwise();

                        }else if (orientationBefore == orientationPortrait) {
                            orientationBefore=orientationLandscape2;
                            animCounterClockwise();
                        }else{
                            orientationBefore=orientationLandscape2;
                        }

                    } else if (arg0 > 135 && arg0 < 225) {

                        if(orientationBefore==orientationLandscape){
                            orientationBefore=orientationPortrait2;

                            animClockwise();

                        }else if (orientationBefore == orientationLandscape2) {
                            orientationBefore=orientationPortrait2;

                            animCounterClockwise();

                        }else{
                            orientationBefore=orientationPortrait2;
                        }

                    } else if (arg0 >= 225 && arg0 < 315) {

                        if(orientationBefore==orientationPortrait){
                            orientationBefore=orientationLandscape;

                            animClockwise();

                        }else if (orientationBefore == orientationPortrait2) {
                            orientationBefore=orientationLandscape;

                            animCounterClockwise();


                        }else{
                            orientationBefore=orientationLandscape;
                        }

                    }
                    // textView.setText("Orientation: " + String.valueOf(arg0));
                }
            };

            // required thing for detecting orientation
            if (myOrientationEventListener.canDetectOrientation()){
                //Toast.makeText(this, "Can DetectOrientation", Toast.LENGTH_LONG).show();
                myOrientationEventListener.enable();
            }
            /*else{
                //Toast.makeText(this, "Can't DetectOrientation", Toast.LENGTH_LONG).show();
                finish();
            }*/
        } catch (Exception e) {
        }

        //setting ID for front or back camera (which has been chosen)
            if(Camera.getNumberOfCameras()>1)
            frontIco.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    camera.stopPreview();
                    if (camera != null)
                        camera.release();
                        camera = null;

                        if (CAMERA_ID == Camera.CameraInfo.CAMERA_FACING_BACK) {
                            CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_FRONT;
                        } else {
                            CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_BACK;
                        }
                        //camera = Camera.open(CAMERA_ID);
                        //this step is critical or preview on new camera will no know where to render to
                    camera=camera.open(CAMERA_ID);
                    setCameraDisplayOrientation(CAMERA_ID);
                    Camera.Parameters parameters = camera.getParameters();
                    getBestPictureSize(parameters);
                    initPreview(parameters.getPreviewSize().width,parameters.getPreviewSize().height);
                  //  holderCallback.surfaceChanged(holder, ImageFormat.JPEG, parameters.getPictureSize().width,parameters.getPictureSize().height);
                    try {
                        camera.setPreviewDisplay(holder);
                        //setPreviewSize(FULL_SCREEN);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    camera.startPreview();
                   /* //Code snippet for this method from somewhere on android developers, i forget where
                   // setCameraDisplayOrientation(MainActivity.this, currentCameraId, camera);
                    setCameraDisplayOrientation(MainActivity.this, CAMERA_ID, camera);
                    try {
                        //this step is critical or preview on new camera will no know where to render to
                        camera.setPreviewDisplay(holder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/

                }
            });

        // capture button----
        iblazerico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    camera.takePicture(null, null, new PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                           /* Bitmap originalImage = BitmapFactory.decodeByteArray(data, 0, data.length);
                            Bitmap rotatedImage = rotate(originalImage, getRotationAngle());
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            rotatedImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] byteArray = stream.toByteArray();*/

                           /* new BackgroundTask().execute(data);
                            camera.startPreview();*/
                            try {
                                Log.d("BACKGROUND", "BACKGROUND");
                                camera.startPreview();
                                camera.startFaceDetection();
                                Bitmap originalImage = BitmapFactory.decodeByteArray(data, 0, data.length);
                                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                                if (CAMERA_ID == CameraInfo.CAMERA_FACING_FRONT) {
                                    int angleToRotate = getRotationAngleFront();
                                    Bitmap  bitmapImage = rotate(originalImage, angleToRotate);
                                    MediaStore.Images.Media.insertImage(getContentResolver(), bitmapImage, "iblazr_" + timeStamp, "yourDescription");
                                } else {
                                    int angleToRotate = getRotationAngleBack();
                                    Bitmap  bitmapImage = rotate(originalImage, angleToRotate);
                                    MediaStore.Images.Media.insertImage(getContentResolver(), bitmapImage, "iblazr_" + timeStamp, "yourDescription");
                                }
                            } catch (Exception e) {
                            }

                            /*pictures= Environment.getExternalStoragePublicDirectory(
                                           Environment.DIRECTORY_PICTURES);
                                    final File mediaFile =new File( pictures.getPath() + File.separator + timeStamp + ".jpg");
                                    FileOutputStream fos = new FileOutputStream(mediaFile);
                                    bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                    fos.write(data);
                                    fos.flush();
                                    fos.close();*//*
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });*/
                        }
                    });
                }catch (Exception e){
                }
            }
        });

        /*
        SharedPreferences sp = getSharedPreferences(FIRST_LOG,
                Context.MODE_PRIVATE);
        // проверяем, первый ли раз открывается программа
        boolean hasVisited = sp.getBoolean("hasVisited", false);

        if (!hasVisited) {
            // выводим нужную активность

            title.setImageResource(R.drawable.tittlescreen);

            SharedPreferences.Editor e = sp.edit();
            e.putBoolean("hasVisited", true);
            e.commit(); // не забудьте подтвердить изменения
        }*/


        /*final AlphaAnimation alphaAnimation = new AlphaAnimation(1.00f,0.00f);
        alphaAnimation.setDuration(850);
        alphaAnimation.setFillEnabled(true);*/

        final Camera.AutoFocusCallback mAutoFocusTakePictureCallback=new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                /*if (camera.getParameters().getFocusMode() != Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) {
                    Camera.Parameters parameters = camera.getParameters();
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    parameters.setFocusAreas(null);
                    camera.setParameters(parameters);
                    camera.startPreview();
                }*/
                if (success) {
                    camera.cancelAutoFocus();
                }
            }
        };

//getting position of touch on screen
        sv.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
            @Override
            public void onSingleTouch(MotionEvent event) {

                if (camera != null) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        float x = event.getX();
                        float y = event.getY();


                        Rect touchRect = new Rect(
                                (int) (x - 100),
                                (int) (y - 100),
                                (int) (x + 100),
                                (int) (y + 100));

                        final Rect targetFocusRect = new Rect(
                                touchRect.left * 2000 / sv.getWidth() - 1000,
                                touchRect.top * 2000 / sv.getHeight() - 1000,
                                touchRect.right * 2000 / sv.getWidth() - 1000,
                                touchRect.bottom * 2000 / sv.getHeight() - 1000);

                        try {
                            List<Camera.Area> focusList = new ArrayList<Camera.Area>();
                            Camera.Area focusArea = new Camera.Area(targetFocusRect, 1000);
                            focusList.add(focusArea);

                            Camera.Parameters param = camera.getParameters();
                            param.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
                            param.setFocusAreas(focusList);
                            param.setMeteringAreas(focusList);
                            camera.setParameters(param);

                            camera.autoFocus(new Camera.AutoFocusCallback() {
                                @Override
                                public void onAutoFocus(boolean success, Camera camera) {
                                    if (success) {
                                        camera.cancelAutoFocus();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.i("TAG", "Unable to autofocus");
                        }
                        drawingView.setVisibility(View.VISIBLE);
                        drawingView.setHaveTouch(true, touchRect);
                        drawingView.invalidate();
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                drawingView.setVisibility(View.INVISIBLE);
                            }
                        }, 700);




                        // So-so working auto- focus
                   /* camera.cancelAutoFocus();
                    Camera.Parameters parameters = camera.getParameters();
                    if (parameters.getMaxNumMeteringAreas() > 0){
                        Log.i("TAG","fancy !");
                        Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f);
                        Rect meteringRect = calculateTapArea(event.getX(), event.getY(), 1.5f);

                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
                        List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
                        meteringAreas.add(new Camera.Area(focusRect, 1000));
                        meteringAreas.add(new Camera.Area(meteringRect, 1000));
                        parameters.setFocusAreas(meteringAreas);
                        parameters.setMeteringAreas(meteringAreas);

                        camera.setParameters(parameters);
                        camera.autoFocus(mAutoFocusTakePictureCallback);
                    }else {
                        camera.autoFocus(mAutoFocusTakePictureCallback);
                    }*/
                    }
                }
            }

            //swiping
            public void onSwipeRight() {
                //Toast.makeText(MainActivity.this, "right", Toast.LENGTH_SHORT).show();
                if (isFinished == true) {
                    mIndexView1 = --mIndexView1;
                    mIndexView2 = --mIndexView2;
                    mIndexView3 = --mIndexView3;
                    if (mIndexView1 < 0) mIndexView1 = view.length - 1;
                    if (mIndexView2 < 0) mIndexView2 = view.length - 1;
                    if (mIndexView3 < 0) mIndexView3 = view.length - 1;
                }

                MiddleToRight middleToRight = new MiddleToRight(view[mIndexView3], 100);
                middleToRight.setDuration(300);
                middleToRight.setFillEnabled(true);

                LeftToMiddle leftToMiddle = new LeftToMiddle(view[mIndexView2], 100);
                leftToMiddle.setDuration(300);
                leftToMiddle.setFillEnabled(true);

                LeftToRight leftToRight = new LeftToRight(view[mIndexView1], 100);
                leftToRight.setDuration(300);
                leftToRight.setFillEnabled(true);

                middleToRight.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        isFinished = false;
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (isFinished == false) {
                            toLeft(view[mIndexView1]);
                            toMiddle(view[mIndexView2]);
                            toRight(view[mIndexView3]);
                        }
                        isFinished = true;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

                if (isFinished == true) {
                    view[mIndexView3].startAnimation(middleToRight);
                    view[mIndexView2].startAnimation(leftToMiddle);
                    view[mIndexView1].startAnimation(leftToRight);
                }
            }

            public void onSwipeLeft() {
                //Toast.makeText(MainActivity.this, "left", Toast.LENGTH_SHORT).show();

                if (isFinished == true) {
                    mIndexView1 = (mIndexView1 + 1) % view.length;
                    mIndexView2 = (mIndexView2 + 1) % view.length;
                    mIndexView3 = (mIndexView3 + 1) % view.length;
                }

                MiddleToLeft middleToLeft = new MiddleToLeft(view[mIndexView1], 100);
                middleToLeft.setDuration(300);
                middleToLeft.setFillEnabled(true);

                RightToMiddle rightToMiddle = new RightToMiddle(view[mIndexView2], 100);
                rightToMiddle.setDuration(300);
                rightToMiddle.setFillEnabled(true);

                RightToLeft rightToLeft = new RightToLeft(view[mIndexView3], 100);
                rightToLeft.setDuration(300);
                rightToLeft.setFillEnabled(true);

                middleToLeft.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        isFinished = false;
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (isFinished == false) {
                            toLeft(view[mIndexView1]);
                            toMiddle(view[mIndexView2]);
                            toRight(view[mIndexView3]);
                        }
                        isFinished = true;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

                if (isFinished == true) {
                    view[mIndexView1].startAnimation(middleToLeft);
                    view[mIndexView2].startAnimation(rightToMiddle);
                    view[mIndexView3].startAnimation(rightToLeft);
                }
            }
        });

        animClockwise = AnimationUtils.loadAnimation(this, R.anim.rotateanim);
        animClockwise.setDuration(300);
        animClockwise.setFillBefore(true);
        animClockwise.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                if(orientationBefore==orientationLandscape){
                    natflash.setRotation(90.0f);
                    galleryIco.setRotation(90.0f);
                    frontIco.setRotation(90.0f);
                }if(orientationBefore==orientationPortrait){
                    natflash.setRotation(0.0f);
                    galleryIco.setRotation(0.0f);
                    frontIco.setRotation(0.0f);
                }if(orientationBefore == orientationLandscape2){
                    natflash.setRotation(-90.0f);
                    galleryIco.setRotation(-90.0f);
                    frontIco.setRotation(-90.0f);
                }if (orientationBefore == orientationPortrait2) {
                    natflash.setRotation(180.0f);
                    galleryIco.setRotation(180.0f);
                    frontIco.setRotation(180.0f);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        animCounterclockwise = AnimationUtils.loadAnimation(this, R.anim.rotateanimback);
        animCounterclockwise.setDuration(300);
        animCounterclockwise.setFillBefore(true);
        animCounterclockwise.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (orientationBefore == orientationLandscape) {
                    natflash.setRotation(90.0f);
                    galleryIco.setRotation(90.0f);
                    frontIco.setRotation(90.0f);
                }
                if (orientationBefore == orientationPortrait) {
                    natflash.setRotation(0.0f);
                    galleryIco.setRotation(0.0f);
                    frontIco.setRotation(0.0f);
                }
                if (orientationBefore == orientationLandscape2) {
                    natflash.setRotation(-90.0f);
                    galleryIco.setRotation(-90.0f);
                    frontIco.setRotation(-90.0f);
                }
                if (orientationBefore == orientationPortrait2) {
                    natflash.setRotation(180.0f);
                    galleryIco.setRotation(180.0f);
                    frontIco.setRotation(180.0f);
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_GALLERY) {
                Uri selectedImageUri = data.getData();

                //OI FILE Manager
                fileManagerString = selectedImageUri.getPath();

                //MEDIA GALLERY
                selectedImagePath = getPath(selectedImageUri);


            }
        }
    }

    public String getPath(Uri uri) {

        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // this is our fallback here
        else return null;//uri.getPath();
    }*/

    private Rect calculateTapArea(float x, float y, float coefficient) {
        int areaSize = Float.valueOf(FOCUS_AREA_SIZE * coefficient).intValue();

        int left = clamp((int) x - areaSize / 2, 0, sv.getWidth() - areaSize);
        int top = clamp((int) y - areaSize / 2, 0, sv.getHeight() - areaSize);

        rectF = new RectF(left, top, left + areaSize, top + areaSize);
        matrix.mapRect(rectF);
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));

        /*int centerX = (int) (x / camera.getParameters().getPreviewSize().width * 2000 - 1000);
        int centerY = (int) (y / camera.getParameters().getPreviewSize().height * 2000 - 1000);

        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int right = clamp(left + areaSize, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);
        int bottom = clamp(top + areaSize, -1000, 1000);
        return new Rect(left, top, right, bottom);*/
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }


    public void touchFocus(final Rect tfocusRect){

        camera.cancelAutoFocus();

        iblazerico.setEnabled(false);

  //      camera.stopFaceDetection();

        //Convert from View's width and height to +/- 1000
        final Rect targetFocusRect = new Rect(
               // tfocusRect.left * 2000/drawingView.getWidth() - 1000,
                tfocusRect.left * 2000/sv.getLayoutParams().width,
                tfocusRect.top * 2000/sv.getLayoutParams().height,
                tfocusRect.right * 2000/sv.getLayoutParams().width,
                tfocusRect.bottom * 2000/sv.getLayoutParams().height);

        final List<Camera.Area> focusList = new ArrayList<Camera.Area>();
        Camera.Area focusArea = new Camera.Area(targetFocusRect, 1000);
        focusList.add(focusArea);

        Camera.Parameters para = camera.getParameters();
        para.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        para.setFocusAreas(focusList);
        //para.setMeteringAreas(focusList);
        camera.setParameters(para);

        camera.autoFocus(myAutoFocusCallback);
        drawingView.setVisibility(View.VISIBLE);
        drawingView.setHaveTouch(true, tfocusRect);
        drawingView.invalidate();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                drawingView.setVisibility(View.INVISIBLE);
            }
        }, 700);
    }

    Camera.AutoFocusCallback myAutoFocusCallback = new Camera.AutoFocusCallback(){
        @Override
        public void onAutoFocus(boolean arg0, Camera arg1) {
            // TODO Auto-generated method stub
            if (arg0){
                iblazerico.setEnabled(true);
                camera.cancelAutoFocus();
            }
            float focusDistances[] = new float[3];
            arg1.getParameters().getFocusDistances(focusDistances);
            Log.d("TAG", String.format("Auto focus success=%s. Focus mode: '%s'. Focused on: %s. Focus distance '%s'",
                    arg0,
                    camera.getParameters().getFocusMode(),
                    camera.getParameters().getFocusAreas().get(0).rect.toString(),
                    focusDistances[Camera.Parameters.FOCUS_DISTANCE_OPTIMAL_INDEX]));
        }};

    /*// Calculating Rectangle ------------------------>
    private Rect calculateFocusArea(float x, float y) {
        int left = clamp(Float.valueOf((x / sv.getWidth()) * 2000 -1200).intValue(), FOCUS_AREA_SIZE);
        int top = clamp(Float.valueOf((y / sv.getHeight()) * 2000 -1200).intValue(), FOCUS_AREA_SIZE);

        return new Rect(left, top, left + FOCUS_AREA_SIZE, top + FOCUS_AREA_SIZE);
    }

    private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper)+focusAreaSize/2>1000){
            if (touchCoordinateInCameraReper>0){
                result = 1000 - focusAreaSize/2;
            } else {
                result = -1000 + focusAreaSize/2;
            }
        } else{
            result = touchCoordinateInCameraReper - focusAreaSize/2;
        }
        return result;
    }

    public boolean handleHeadphonesState(Context context){
        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        if(am.isWiredHeadsetOn()) {
            // handle headphones plugged in
            return true;

        } else{
            // handle headphones unplugged
            return false;
        }
    }*/

    void animClockwise() {
        natflash.startAnimation(animClockwise);
        galleryIco.startAnimation(animClockwise);
        frontIco.startAnimation(animClockwise);
    }

    void animCounterClockwise() {
        natflash.startAnimation(animCounterclockwise);
        galleryIco.startAnimation(animCounterclockwise);
        frontIco.startAnimation(animCounterclockwise);
    }

    public  int getRotationAngleBack() {
        if(orientationBefore==orientationLandscape){
            return 0;
        }if(orientationBefore==orientationPortrait){
            return 90;
        }if(orientationBefore == orientationLandscape2){
            return 180;
        }else if (orientationBefore == orientationPortrait2) {
            return -90;
        }return 0;
    }
    public  int getRotationAngleFront() {
        if(orientationBefore==orientationLandscape){
            return 0;
        }if(orientationBefore==orientationPortrait){
            return -90;
        }if(orientationBefore == orientationLandscape2){
            return 180;
        }else if (orientationBefore == orientationPortrait2) {
            return 90;
        }return 0;
    }

    public  Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }


 /*   public class BackgroundTask extends AsyncTask<byte[], String, String> {


        @Override
        protected String doInBackground(byte[]... data) {
            //int angleToRotate = getRotationAngle();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File photo=new File(Environment.getExternalStorageDirectory(),timeStamp+".jpg");

            try {
                //Bitmap originalImage = BitmapFactory.decodeByteArray(data[0], 0, data.length);
                FileOutputStream fos=new FileOutputStream(photo.getPath());
                fos.write(data[0]);
                fos.flush();
                fos.close();
            }
            catch (java.io.IOException e) {
                Log.e("PictureDemo", "Exception in photoCallback", e);
            }

           *//* try {
                Bitmap originalImage = BitmapFactory.decodeByteArray(data[0], 0, data.length);
               // Bitmap bitmapImage = rotate(originalImage, 90);
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                MediaStore.Images.Media.insertImage(getContentResolver(), originalImage, "iblazr_" + timeStamp, "yourDescription");
            } catch (Exception e) {
                Log.e("PictureDemo", "Exception in photoCallback", e);
            }*//*
            return null;
        }
    }*/


    public void toLeft(View view) {
        view.layout(0,100,view.getMeasuredWidth(),100+view.getMeasuredHeight());
    }

    public void toMiddle(View view) {
        view.layout(100,0,100+view.getMeasuredWidth(),view.getMeasuredHeight());
    }

    public void toRight(View view) {
        view.layout(200,100,200+view.getMeasuredWidth(),100+view.getMeasuredHeight());
    }

    @Override
    public void onBackPressed() {
        this.finish();
        moveTaskToBack(true);

        generator.finish();

        /*try {
            frequencyThread.interrupt();
            if (Thread.interrupted()) throw new InterruptedException();
        } catch (InterruptedException e) {
        }*/

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_INDEX1,mCurrentIndex1);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {

       IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
       registerReceiver(mAudioJackOut, filter);

        camera = Camera.open(CAMERA_ID);
        setPreviewSize(FULL_SCREEN);
        sv.setVisibility(View.VISIBLE);
        super.onResume();

        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume < 9) {
            Toast.makeText(this,"Please volume up to MAX",Toast.LENGTH_SHORT).show();
        }
        generator=new GeneratorConstThread(this,3000);
    }

    @Override
    protected void onPause() {
        generator.finish();
        unregisterReceiver(mAudioJackOut);
        if (camera != null)
            camera.release();
        camera = null;
        sv.setVisibility(View.INVISIBLE);
        super.onPause();

    }
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        myOrientationEventListener.disable();
    }

   /* @Override
    public void onSaveInstanceState(Bundle savedInstanceState){

        savedInstanceState.putInt(KEY_INDEX1, mIndexView1);
        savedInstanceState.putInt(KEY_INDEX2, mIndexView2);
        savedInstanceState.putInt(KEY_INDEX3,mIndexView3);

        super.onSaveInstanceState(savedInstanceState);

    }*/

    private Camera.Size getBestPreviewSize(int width,int height, Camera.Parameters parameters) {
        Camera.Size result=null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result=size;
                }
                else {
                    int resultArea=result.width * result.height;
                    int newArea=size.width * size.height;


                    if (newArea > resultArea) {
                        result=size;
                    }
                }
            }
        }
        return(result);
    }


    private Camera.Size getBestPictureSize(Camera.Parameters parameters) {
        Camera.Size result=camera.getParameters().getPictureSize();

        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
            /*if (result == null) {
                result=size;
            }
            else {*/
                int resultArea=result.width * result.height;
                int newArea=size.width * size.height;

                if (newArea > resultArea) {
                    result=size;
                }
           // }
        }
        return(result);
    }

    private void initPreview(int width, int height) {
            try {
                camera.setPreviewDisplay(holder);
            } catch (Throwable t) {
                Log.e("PreviewDemo-surfaceCallback",
                        "Exception in setPreviewDisplay()", t);
                Toast.makeText(MainActivity.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }


                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = getBestPreviewSize(width, height, parameters);
                Camera.Size pictureSize = getBestPictureSize(parameters);

                    parameters.setPreviewSize(size.width, size.height);
                    parameters.setPictureSize(pictureSize.width, pictureSize.height);
                    parameters.setPictureFormat(ImageFormat.JPEG);
                    //parameters.setAutoWhiteBalanceLock(true);
                    //parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
        camera.setParameters(parameters);
    }

    class HolderCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
                //camera.setFaceDetectionListener(faceDetectionListener);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {

                camera.cancelAutoFocus();
                camera.stopPreview();
//                camera.stopFaceDetection();
            setCameraDisplayOrientation(CAMERA_ID);
            try {
                initPreview(width, height);
                camera.setPreviewDisplay(holder);
                camera.startPreview();
                //camera.startFaceDetection();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {/*
            camera.stopFaceDetection();
            camera.stopPreview();
            camera.release();
            camera = null;*/
        }

    }

    void setPreviewSize(boolean fullScreen) {

        // получаем размеры экрана
        Display display = getWindowManager().getDefaultDisplay();
        boolean widthIsMax = display.getWidth() > display.getHeight();

        // определяем размеры превью камеры
        Size size = camera.getParameters().getPreviewSize();

        RectF rectDisplay = new RectF();
        RectF rectPreview = new RectF();

        // RectF экрана, соотвествует размерам экрана
        rectDisplay.set(0, 0, display.getWidth(), display.getHeight());

        // RectF первью
        if (widthIsMax) {
            // превью в горизонтальной ориентации
            rectPreview.set(0, 0, size.width, size.height);
        } else {
            // превью в вертикальной ориентации
            rectPreview.set(0, 0, size.height, size.width);
        }

        matrix = new Matrix();
        // подготовка матрицы преобразования
        if (!fullScreen) {
            // если превью будет "втиснут" в экран (второй вариант из урока)
            matrix.setRectToRect(rectPreview, rectDisplay,
                    Matrix.ScaleToFit.START);
        } else {
            // если экран будет "втиснут" в превью (третий вариант из урока)
            matrix.setRectToRect(rectDisplay, rectPreview,
                    Matrix.ScaleToFit.START);
            matrix.invert(matrix);
        }
        // преобразование
        matrix.mapRect(rectPreview);

        // установка размеров surface из получившегося преобразования
        sv.getLayoutParams().height = (int) (rectPreview.bottom);
        sv.getLayoutParams().width = (int) (rectPreview.right);
    }

    void setCameraDisplayOrientation(int cameraId) {
        // определяем насколько повернут экран от нормального положения
        int rotation = getWindowManager().getDefaultDisplay().getOrientation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result = 0;

        // получаем инфо по камере cameraId
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        // задняя камера
        if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
            result = ((360 - degrees) + info.orientation);
        } else
            // передняя камера
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                result = ((360 - degrees) - info.orientation);
                result += 360;
            }
        result = result % 360;
        camera.setDisplayOrientation(result);
    }

   /* Camera.FaceDetectionListener faceDetectionListener
            = new Camera.FaceDetectionListener(){

        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera tcamera) {

            if (faces.length == 0){
                //prompt.setText(" No Face Detected! ");
                drawingView.setHaveFace(false);
            }else{
                //prompt.setText(String.valueOf(faces.length) + " Face Detected :) ");
                drawingView.setHaveFace(true);
                detectedFaces = faces;

                //Set the FocusAreas using the first detected face
                List<Camera.Area> focusList = new ArrayList<Camera.Area>();
                Camera.Area firstFace = new Camera.Area(faces[0].rect, 1000);
                focusList.add(firstFace);

                Camera.Parameters para = camera.getParameters();

                if(para.getMaxNumFocusAreas()>0){
                    para.setFocusAreas(focusList);
                }

                if(para.getMaxNumMeteringAreas()>0){
                    para.setMeteringAreas(focusList);
                }

                camera.setParameters(para);

                iblazerico.setEnabled(false);

                //Stop further Face Detection
                camera.stopFaceDetection();

                iblazerico.setEnabled(false);

				*//*
				 * Allways throw java.lang.RuntimeException: autoFocus failed
				 * if I call autoFocus(myAutoFocusCallback) here!
				 *
					camera.autoFocus(myAutoFocusCallback);
				*//*

                //Delay call autoFocus(myAutoFocusCallback)
                myScheduledExecutorService = Executors.newScheduledThreadPool(1);
                myScheduledExecutorService.schedule(new Runnable(){
                    public void run() {
                        camera.autoFocus(myAutoFocusCallback);
                    }
                }, 500, TimeUnit.MILLISECONDS);

            }

            drawingView.invalidate();

        }};*/

    private class DrawingView extends View{

        boolean haveFace;
        Paint drawingPaint;

        boolean haveTouch;
        Rect touchArea;

        public DrawingView(Context context) {
            super(context);
            haveFace = false;
            drawingPaint = new Paint();
            drawingPaint.setColor(Color.GREEN);
            drawingPaint.setStyle(Paint.Style.STROKE);
            drawingPaint.setStrokeWidth(2);

            haveTouch = false;
        }

        public void setHaveFace(boolean h){
            haveFace = h;
        }

        public void setHaveTouch(boolean t, Rect tArea){
            haveTouch = t;
            touchArea = tArea;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            if(haveFace){

                // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
                // UI coordinates range from (0, 0) to (width, height).

                int vWidth = sv.getLayoutParams().width ;
                int vHeight = sv.getLayoutParams().height;

                for(int i=0; i<detectedFaces.length; i++){

                    if(i == 0){
                        drawingPaint.setColor(Color.GREEN);
                    }else{
                        drawingPaint.setColor(Color.RED);
                    }

                    int l = detectedFaces[i].rect.left;
                    int t = detectedFaces[i].rect.top;
                    int r = detectedFaces[i].rect.right;
                    int b = detectedFaces[i].rect.bottom;
                    int left	= (l+1000) * vWidth/2000;
                    int top		= (t+1000) * vHeight/2000;
                    int right	= (r+1000) * vWidth/2000;
                    int bottom	= (b+1000) * vHeight/2000;
                    canvas.drawRect(
                            left, top, right, bottom,
                            drawingPaint);
                }
            }else{
                canvas.drawColor(Color.TRANSPARENT);
            }

            if(haveTouch){
                drawingPaint.setColor(Color.WHITE);
                canvas.drawRect(
                        touchArea.left-75, touchArea.top, touchArea.right-75, touchArea.bottom,
                        drawingPaint);
            }
        }

    }
}