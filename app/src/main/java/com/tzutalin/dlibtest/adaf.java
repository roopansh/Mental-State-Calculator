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
                    Log.e("shubz", "\tSTRESS : " + String.valueOf(stressScore) + "\tDISGUST : " + String.valueOf(disgustScore) + "\tHAPPINESS : " + String.valueOf(happinessScore) + "\tFEAR : " + String.valueOf(fearScore) + "\tFOCUS : " + String.valueOf(focusScore));
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
    float openMouth = (Math.abs(landmarks.get(50).y - landmarks.get(58).y) + Math.abs(landmarks.get(51).y - landmarks.get(57).y) + Math.abs(landmarks.get(52).y - landmarks.get(56).y))/3.0f;

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
