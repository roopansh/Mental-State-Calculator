/*
Name of the module : MainActivity.java

Date on which the module was created: 04/4/2017

Author’s name:  Roopansh Bansal

Modification history :  By Shubham Singhal 07/04/2017
                        By Shivam Gupta 10/04/2017

Synopsis of the module : Main File which is executed when the app is started.

Different functions supported, along with their input/output parameters.

Global variables accessed/modified by the module.
*/


package com.tzutalin.dlibtest;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Handler;
import android.os.Trace;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.tzutalin.dlib.Constants;
import com.tzutalin.dlib.FaceDet;
import com.tzutalin.dlib.VisionDetRet;

import junit.framework.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*
 *
 * Class that takes in preview frames and
 * converts the image to Bitmaps to process with dlib lib.
 *
 */
public class OnGetImageListener implements OnImageAvailableListener {
    private static final boolean SAVE_PREVIEW_BITMAP = false;

    private static final int INPUT_SIZE = 224;
    private static final String TAG = "OnGetImageListener";

    private int mScreenRotation = 90;

    private int mPreviewWdith = 0;
    private int mPreviewHeight = 0;
    private byte[][] mYUVBytes;
    private int[] mRGBBytes = null;
    private Bitmap mRGBframeBitmap = null;
    private Bitmap mCroppedBitmap = null;

    private boolean mIsComputing = false;
    private Handler mInferenceHandler;

    private Context mContext;
    private FaceDet mFaceDet;
    private TrasparentTitleView mTransparentTitleView1;
    private TrasparentTitleView mTransparentTitleView2;
    private FloatingCameraWindow mWindow;
    private Paint mFaceLandmardkPaint;


    private static final String timeTAG = "TIME";
    //TIme Handler to control blink rate and update it to zero.
    private final Handler timeHandler = new Handler();
    //variable to store current activity which is used to extract current rotation of screen
    private Activity mcurractivity;

    /*
    *
    * Various score levels of the various emotions will be stored in these variables.
    *
    */

    private float stressScore= 0;
    private float disgustScore= 0;
    private float happinessScore= 0;
    private float fearScore= 0;
    private float focusScore= 0;

    /*
    *
    * The following variables are used to calculate the eyes blink rate.
    * @blinkRateNext store the current count of blinks for 20 seconds.
    * 
    */

    private int blinkRate= 3;
    private int blinkRatenext= 0;

    /*
    * Eyebrow shape will be :-
    *  0 => 'raised and arched' when a person is surprised
    *  1 => 'lowered and knit' together when a person is disgust
    *  2 => 'inner corners' drawn up if stressed
    */
    private int eyeBrow= 0;
    /*
    * Mouth shape will be :-
    *  0 => 'open mouth' for fear
    *  1 => 'corners raised' for happiness
    *  2 => 'corners drawn down' for sadness
    */
    private int mouth= 0;

    /*
    * Eyes shape will be :-
    *  0 => 'wide open' for surprise
    *  1 => 'intensely staring' (disgust)
    *  2 => 'crow's feet crinkles' (happy)
    */
    private int eyeSize= 0;

    private float pupilSize= 0;


    //initialises all the private variavles
    public void initialize(
            final Context context,
            final AssetManager assetManager,
            final TrasparentTitleView scoreView1,
            final TrasparentTitleView scoreView2,
            final Handler handler,
            final Activity curractivity) {
        this.mContext = context;
        this.mTransparentTitleView1 = scoreView1;
        this.mTransparentTitleView2 = scoreView2;
        this.mInferenceHandler = handler;
        this.mcurractivity= curractivity;
        mFaceDet = new FaceDet(Constants.getFaceShapeModelPath());
        mWindow = new FloatingCameraWindow(mContext);

        mFaceLandmardkPaint = new Paint();
        mFaceLandmardkPaint.setColor(Color.GREEN);
        mFaceLandmardkPaint.setStrokeWidth(2);
        mFaceLandmardkPaint.setStyle(Paint.Style.STROKE);

        //time handler to handle the blinkrate which is resetted after every 20 sec
        timeHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.i(timeTAG, "TESTING");
                blinkRate = 3 * blinkRatenext;
                blinkRatenext = 0;
                Log.d("SHubz" , "in sam handles");
                timeHandler.postDelayed(this, 20000);
            }
        });;
    }

    public void deInitialize() {
        synchronized (OnGetImageListener.this) {
            if (mFaceDet != null) {
                mFaceDet.release();
            }

            if (mWindow != null) {
                mWindow.release();
            }
        }
    }

    //draw a floating small bitmap in which facial landmarks are shown.
    private void drawResizedBitmap(final Bitmap src, final Bitmap dst,final Activity mcurractivity) {

        Display getOrient = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int orientation = Configuration.ORIENTATION_UNDEFINED;
        int ors = Surface.ROTATION_0;
        Point point = new Point();
        getOrient.getSize(point);
        int screen_width = point.x;
        int screen_height = point.y;

        int rotation = mcurractivity.getWindowManager().getDefaultDisplay().getRotation();

        //function to handlle the rotation of floating screen
        if (screen_width < screen_height) {
            if(rotation == Surface.ROTATION_0)
                mScreenRotation = 270;
            else if(rotation == Surface.ROTATION_90)
                mScreenRotation = 0;
            else if(rotation == Surface.ROTATION_180)
                mScreenRotation = 90;
            else if(rotation == Surface.ROTATION_270)
                mScreenRotation = 90;

        } else {
//            rotation = Configuration.ORIENTATION_LANDSCAPE;
//            Log.d("shubznew" , "should never come");
            if(rotation == Surface.ROTATION_0)
                mScreenRotation = 270;
            else if(rotation == Surface.ROTATION_90)
                mScreenRotation = 0;
            else if(rotation == Surface.ROTATION_180)
                mScreenRotation = 90;
            else if(rotation == Surface.ROTATION_270)
                mScreenRotation = 180;
//            mScreenRotation = 0;
        }

        Assert.assertEquals(dst.getWidth(), dst.getHeight());
        final float minDim = Math.min(src.getWidth(), src.getHeight());

        final Matrix matrix = new Matrix();

        // We only want the center square out of the original rectangle.
        final float translateX = -Math.max(0, (src.getWidth() - minDim) / 2);
        final float translateY = -Math.max(0, (src.getHeight() - minDim) / 2);
        matrix.preTranslate(translateX, translateY);

        final float scaleFactor = dst.getHeight() / minDim;
        matrix.postScale(scaleFactor, scaleFactor);

        // Rotate around the center if necessary.
        if (mScreenRotation != 0) {
            matrix.postTranslate(-dst.getWidth() / 2.0f, -dst.getHeight() / 2.0f);
            matrix.postRotate(mScreenRotation);
            matrix.postTranslate(dst.getWidth() / 2.0f, dst.getHeight() / 2.0f);
        }

        final Canvas canvas = new Canvas(dst);
        canvas.drawBitmap(src, matrix, null);
    }


// when image is available, call the function with current imagereader
    @Override
    public void onImageAvailable(final ImageReader reader) {
        Image image = null;
        try {
            image = reader.acquireLatestImage();

            if (image == null) {
                return;
            }

            // No mutex needed as this method is not reentrant.
            if (mIsComputing) {
                image.close();
                return;
            }
            mIsComputing = true;

            Trace.beginSection("imageAvailable");

            final Plane[] planes = image.getPlanes();

            // Initialize the storage bitmaps once when the resolution is known.
            if (mPreviewWdith != image.getWidth() || mPreviewHeight != image.getHeight()) {
                mPreviewWdith = image.getWidth();
                mPreviewHeight = image.getHeight();

                Log.d(TAG, String.format("Initializing at size %dx%d", mPreviewWdith, mPreviewHeight));
                mRGBBytes = new int[mPreviewWdith * mPreviewHeight];
                mRGBframeBitmap = Bitmap.createBitmap(mPreviewWdith, mPreviewHeight, Config.ARGB_8888);
                mCroppedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Config.ARGB_8888);

                mYUVBytes = new byte[planes.length][];
                for (int i = 0; i < planes.length; ++i) {
                    mYUVBytes[i] = new byte[planes[i].getBuffer().capacity()];
                }
            }

            for (int i = 0; i < planes.length; ++i) {
                planes[i].getBuffer().get(mYUVBytes[i]);
            }

            final int yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();
            ImageUtils.convertYUV420ToARGB8888(
                    mYUVBytes[0],
                    mYUVBytes[1],
                    mYUVBytes[2],
                    mRGBBytes,
                    mPreviewWdith,
                    mPreviewHeight,
                    yRowStride,
                    uvRowStride,
                    uvPixelStride,
                    false);

            image.close();
        } catch (final Exception e) {
            if (image != null) {
                image.close();
            }
            Log.e(TAG, "Exception!", e);
            Trace.endSection();
            return;
        }

        mRGBframeBitmap.setPixels(mRGBBytes, 0, mPreviewWdith, 0, 0, mPreviewWdith, mPreviewHeight);
        drawResizedBitmap(mRGBframeBitmap, mCroppedBitmap,mcurractivity);

        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(mCroppedBitmap);
        }

        //make a runnable for each of image obtained
        mInferenceHandler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        if (!new File(Constants.getFaceShapeModelPath()).exists()) {
                            mTransparentTitleView1.setText("Copying landmark model to " + Constants.getFaceShapeModelPath());
                            FileUtils.copyFileFromRawToOthers(mContext, R.raw.shape_predictor_68_face_landmarks, Constants.getFaceShapeModelPath());
                        }

                        long startTime = System.currentTimeMillis();
                        //detect all landmarks
                        List<VisionDetRet> results;
                        synchronized (OnGetImageListener.this) {
                            results = mFaceDet.detect(mCroppedBitmap);
                        }
                        long endTime = System.currentTimeMillis();
//                        mTransparentTitleView.setText("Time cost: " + String.valueOf((endTime - startTime) / 1000f) + " sec");
                        // Draw on bitmap
                        if (results != null) {
                            for (final VisionDetRet ret : results) {
                                float resizeRatio = 1.0f;
                                Rect bounds = new Rect();
                                bounds.left = (int) (ret.getLeft() * resizeRatio);
                                bounds.top = (int) (ret.getTop() * resizeRatio);
                                bounds.right = (int) (ret.getRight() * resizeRatio);
                                bounds.bottom = (int) (ret.getBottom() * resizeRatio);
                                Canvas canvas = new Canvas(mCroppedBitmap);
                                canvas.drawRect(bounds, mFaceLandmardkPaint);

                                // Draw landmark
                                //Landmarks will be having 58 points in total
                                //18-22 and 23-27 : Eyebrow
                                //37-42 and 43-48 : Eyes
                                //49-58 : Upper and lower lips

                                ArrayList<Point> landmarks = ret.getFaceLandmarks();
                                for (int i = 0; i < landmarks.size(); i++) {
                                    Point point = landmarks.get(i);
                                    int a = point.x;
                                    int b = point.y;
                                    Log.e("shivam" ,""+i+": "+Integer.toString(a)+" , "+Integer.toString(b) );
                                    if (i < 27)
                                    {
                                        mFaceLandmardkPaint.setColor(Color.RED);
                                        if(i< 22 && i>=17)
                                            mFaceLandmardkPaint.setColor(Color.rgb(40,40,40));
                                        if(i>=22)
                                            mFaceLandmardkPaint.setColor(Color.CYAN);
                                    }
                                    else if (i < 36)
                                        mFaceLandmardkPaint.setColor(Color.YELLOW);
                                    else if (i < 48) {
                                        mFaceLandmardkPaint.setColor(Color.BLUE);
                                        if(i==36)
                                            mFaceLandmardkPaint.setColor(Color.rgb(40,40,40));
                                        if(i==38)
                                            mFaceLandmardkPaint.setColor(Color.GRAY);
                                        if(i==40)
                                            mFaceLandmardkPaint.setColor(Color.MAGENTA);
                                        if(i==42)
                                            mFaceLandmardkPaint.setColor(Color.rgb(40,40,40));
                                        if(i==44)
                                            mFaceLandmardkPaint.setColor(Color.GRAY);
                                        if(i==46)
                                            mFaceLandmardkPaint.setColor(Color.MAGENTA);
                                    }
                                    else
                                    {
                                        if(i<53)
                                            mFaceLandmardkPaint.setColor(Color.RED);
                                        else if(i<58)
                                            mFaceLandmardkPaint.setColor(Color.YELLOW);
                                        else if(i<63)
                                            mFaceLandmardkPaint.setColor(Color.CYAN);
                                        else if(i<68)
                                            mFaceLandmardkPaint.setColor(Color.GREEN);
                                    }
                                    int pointX = (int) (point.x * resizeRatio);
                                    int pointY = (int) (point.y * resizeRatio);
                                    canvas.drawCircle(pointX, pointY, 1, mFaceLandmardkPaint);
                                }

                                //functions to calculate the features
                                calculateFeatures(landmarks, bounds);

                                //CAlculate the final facial expression
                                String topfacialExpression = getFacialExpression(4);
                                // Set the facial expression scores accordingly
                                String sectopfacialExpression = getFacialExpression(3);

                                //Set the final facial expression in the UI
                                mTransparentTitleView1.setText("   Top I. "+topfacialExpression);
                                mTransparentTitleView2.setText("   Top II. "+sectopfacialExpression);
                                Log.e("shubz", "\tSTRESS : " + String.valueOf(stressScore) + "\tDISGUST : "
                                        + String.valueOf(disgustScore) + "\tHAPPINESS : " +
                                        String.valueOf(happinessScore) + "\tFEAR : " + String.valueOf(fearScore) + "\tFOCUS : " + String.valueOf(focusScore));
                            }
                        }

                        mWindow.setRGBBitmap(mCroppedBitmap);
                        mIsComputing = false;
                    }
                });

        Trace.endSection();
    }


    //function to calculate the features
    private void calculateFeatures(ArrayList<Point> landmarks, Rect bounds) {
        // Eye Brow Shape
        eyeBrow = calculateEyebrowShape(landmarks, bounds);
        // mouth shape
        mouth = calculateMouthShape(landmarks, bounds);
        // Eye size
        eyeSize = calculateEyesShape(landmarks);

        //Calculate scores for all the facial expressions
        stressScore = calculateStressScore();
        disgustScore = calculateDisgustScore();
        happinessScore = calculateHappinessScore();
        fearScore = calculateFearScore();
        focusScore = calculateFocusScore();
    }

    //function to calculate the eye shape
    private int calculateEyesShape(ArrayList<Point> landmarks) {
        /*
        * Eyes shape will be :-
        *  0 => 'wide open' (surprise)
        *  1 => 'intensely staring' (disgust)
        *  2 => 'crow's feet crinkles' (happy)
        */

        int ans = 2;
        // get the size of left eye
        float leftEyeIndex = getEyeSizeAux(landmarks.subList(36,42));
        // get the size of right eye
        float rightEyeIndex = getEyeSizeAux(landmarks.subList(42,48));

        // Normalize both the eyes index by the dimensions of the face size
        leftEyeIndex = leftEyeIndex / (float)(Math.pow((landmarks.get(27).y - landmarks.get(30).y),2));
        rightEyeIndex = rightEyeIndex / (float)(Math.pow((landmarks.get(27).y - landmarks.get(30).y),2));

        // Average the size over both left and right eyes
        double eyesSize = Math.sqrt(leftEyeIndex*rightEyeIndex) * 100;

        /*
        *  ans = 0 => 'wide open' when eyesize > 17
        *  ans = 1 => 'intensely staring' when eyesize < 17 and eyesize > 14
        *  ans = 2 => 'crow's feet crinkles' when eyesize < 14
        */
        if( eyesSize > 17 ) {
            ans = 0;
        } else if ( eyesSize > 14) { 
            ans = 1;
        } else if ( eyesSize > 10){
            ans = 2;
        } else {
            blinkRatenext++;
        }

        // pupil size is equal to the eyes size approximately
        pupilSize = (float) eyesSize;
        return ans;
    }

    // an auxillary function used in calculating the eye size
    private float getEyeSizeAux(List<Point> points) {
        // calculate the lentgh of the length and breadth of the eyes size (rectangle)
        // d1 = length
        // d2 = breadth

        double d1 = Math.pow((points.get(1).x - points.get(4).x), 2) + Math.pow((points.get(1).y - points.get(4).y), 2) ;
        d1 = Math.sqrt(d1);
        double d2 = Math.pow((points.get(2).x - points.get(5).x), 2) + Math.pow((points.get(2).y - points.get(5).y), 2) ;
        d2 = Math.sqrt(d2);
        
        // area = length * breadth
        float ans = (float)(d1*d2);
        return ans;
    }

    //function to calculate the mouth shape
    private int calculateMouthShape(ArrayList<Point> landmarks, Rect bounds) {
        /*
        * Mouth shape will be :-
        *  0 => 'open mouth' for fear
        *  1 => 'corners raised' for happiness
        *  2 => 'corners drawn down' for stress
        */

        // vertical height of the corners of lips above the center of the lips == smile
        // to calculate how much the person is smiling
        float smile = (Math.abs(landmarks.get(50).y - landmarks.get(48).y) + Math.abs(landmarks.get(52).y - landmarks.get(54).y))/2.0f;

        // area of the region enclosed by the points(landmarks) on the lips == openMouth
        // to calculate how much the person has opened his mouth
        float openMouth = (Math.abs(landmarks.get(50).y - landmarks.get(58).y) + Math.abs(landmarks.get(51).y - landmarks.get(57).y)
                + Math.abs(landmarks.get(52).y - landmarks.get(56).y))/3.0f;
        
        // vertical height of the corners of lips below the center of the lips == stress
        // to calculate how much the person is frowning
        float stress = (Math.abs(landmarks.get(58).y - landmarks.get(48).y) + Math.abs(landmarks.get(56).y - landmarks.get(54).y))/2.0f;
        
        // to normalise the area by the size of the face
        openMouth = openMouth/Math.abs(bounds.top - bounds.bottom);


        /*
        *  ans = 0 => 'open mouth' is dominant trait of the mouth
        *  ans = 1 => 'corners raised' is dominant trait of the mouth
        *  ans = 2 => 'corners drawn down' is dominant trait of the mouth
        */


        int ans = 1;
        if (openMouth > 0.2){
            ans = 0;
        } else if (stress > smile) {
            ans = 1;
        } else {
            ans = 2;
        }
        return ans;
    }

    //function to calculate the eyebrow shape
    private int calculateEyebrowShape(ArrayList<Point> landmarks, Rect bounds) {
        /*
        * Eyebrow shape will be :-
        *  0 => 'raised and arched' when a person is surprised
        *  1 => 'lowered and knit' together when a person is disgust
        *  2 => 'inner corners' drawn up if stressed
        */
        int ans = 2;

        // eyebrowup == vertical height of the eyebrows
        // this is to indicate how much the eyebrows are arched
        float eyeBrowUp = (Math.abs(landmarks.get(19).y - landmarks.get(27).y) + Math.abs(landmarks.get(24).y - landmarks.get(27).y))/2.0f;
        // take average of the eyebrows
        eyeBrowUp = (eyeBrowUp*100)/Math.abs(bounds.top - bounds.bottom);
        
        // innercornerup == how much the inner portion of the eyebrows are above it's outer corners
        // this is to indicate how much the eyebrows have inner corners drawn up
        float innerCornerUp = (Math.abs(landmarks.get(21).y - landmarks.get(27).y) + Math.abs(landmarks.get(22).y - landmarks.get(27).y))/2.0f;
        innerCornerUp = (innerCornerUp*100)/Math.abs(bounds.top - bounds.bottom);

        // knittogether == how much close the inner corners of the two eyebrows are close  
        float knitTogether = Math.abs(landmarks.get(21).x - landmarks.get(22).x);
        knitTogether = (knitTogether*100)/Math.abs(bounds.left - bounds.right);


        if ( eyeBrowUp > 13.5f ) {
            ans = 0;
        } else if ( knitTogether < 12f ){
            ans = 1;
        } else if ( innerCornerUp > 9f){
            ans = 2;
        }

        return ans;
    }

    //function to calculate the stress store
    private float calculateStressScore() {
//        STRESS Score =   (Blink Rate Index + Eye Brow Index + Mouth Index )/ 3

        float blinkRateIndex = (blinkRate-19)/7,
                eyeBrowIndex = 0,
                mouthIndex = 0;
        if (eyeBrow == 2) {
            eyeBrowIndex = 1;
        }
        if (mouth == 2){
            mouthIndex = 1;
        }
        if(blinkRateIndex < 0){
            blinkRateIndex = 0;
        }
        else if(blinkRateIndex > 1){
            blinkRateIndex = 1;
        }
        float Score = (blinkRateIndex + eyeBrowIndex + mouthIndex)/3;
        return Score;
    }

    //function to calculate the disgust score
    private float calculateDisgustScore() {
        // DisgustScore = (eyeBrowIndex + eyeSizeIndex)/2;
        float   eyeBrowIndex = 0,
                eyeSizeIndex = 0;
        if (eyeBrow == 1) {
            eyeBrowIndex = 1;
        }
        if (eyeSize == 1){
            eyeSizeIndex = 1;
        }
        float Score = (eyeBrowIndex + eyeSizeIndex)/2;
        return Score;
    }

    //function to calculate the happiness score
    private float calculateHappinessScore() {
        //HapinessScore = (pupilSizeIndex + eyeSizeIndex + mouthIndex)/3;
        float   pupilSizeIndex = 0.5f,
                eyeSizeIndex = 0,
                mouthIndex = 0;
        if (eyeSize == 2) {
            eyeSizeIndex = 1;
        }
        if (mouth == 1){
            mouthIndex = 1;
        }
        float Score = (pupilSizeIndex + eyeSizeIndex + mouthIndex)/3;
        return Score;
    }

    //function to calculate the fear score
    private float calculateFearScore() {
        //FearScore = (pupilSizeIndex + blinkRateIndex + mouthIndex)/3;

        float   pupilSizeIndex = 0.5f,
                blinkRateIndex = 0,
                mouthIndex = 0;
        blinkRateIndex = (blinkRate-3)/4;
        if(blinkRateIndex >= 1)
            blinkRateIndex = 0;
        else if(blinkRateIndex <= 0)
            blinkRateIndex = 1;
        if (mouth == 0){
            mouthIndex = 1;
        }
        float Score = (pupilSizeIndex + blinkRateIndex + mouthIndex)/3;
        return Score;
    }

    //function to calculate the focus score
    private float calculateFocusScore() {
        //FOcusScore = (pupilSizeIndex + blinkRateIndex )/2;

        float   pupilSizeIndex = 0.5f,
                blinkRateIndex = 0;
        blinkRateIndex = (blinkRate-3)/4;
        //note : since he will be less focused if high blink rate
        if(blinkRateIndex >= 1)
            blinkRateIndex = 0;
        else if(blinkRateIndex <= 0)
            blinkRateIndex = 1;
        float Score = (pupilSizeIndex + blinkRateIndex )/2;
        return Score;
    }

    private String mTAG = "FEATURES\t";


    /*
    * THis function sorts the score levels 
    * and then returns the top two score levels and their expressions.
    */
    private String getFacialExpression(int j) {
        List<String> scoretype = Arrays.asList("Stress", "Disgust", "Happiness" , "Fear" , "Focus");
        List<Float> origscore = Arrays.asList(stressScore, disgustScore, happinessScore , fearScore , focusScore);
        List<Float> score = Arrays.asList(stressScore, disgustScore, happinessScore , fearScore , focusScore);
        
        // sort according to the scroe leveles
        Collections.sort(score);
        String topscoretype="";
        String sectopscoretype="";
        
        // set the top two expressions according to the score levels
        Float topscore = score.get(j);
        for (int i = 0; i < score.size(); i++) {
            if(Float.compare(topscore, origscore.get(i)) == 0)
            {
                topscoretype = scoretype.get(i);
            }
        }
        String toreturn = topscoretype;
        return toreturn;
    }
}
