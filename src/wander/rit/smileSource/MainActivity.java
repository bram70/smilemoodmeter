package wander.rit.smileSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

// to load the cascade classifier files.
import wander.rit.smilemoodmeter.R;




import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements CvCameraViewListener, OnTouchListener{

	// This is the object to show the camera
	private CameraBridgeViewBase openCvCameraView;
	// This is the openCV cascade Classifier for face detection 
	private CascadeClassifier cascadeClassifier;
	// This is the openCV cascade Classifier for smile detection
	private CascadeClassifier cascadeClassifierSmile;
	
	//this is the Mat object to transform the image to gray-scale for analysis purpose.
	private Mat grayScaleImage;
	// The size estimated for the face 20% of the height of the screen
	private int absoluteFaceSize;
	// This is the flag value to switch the camera front/back camera
	private int CAMERA_SWITCH = 1;
	//
	//private Bitmap image_showed;
	// This is the value of the mood.
	static float mood = 0;
	
	
	static boolean hideMessage = false;
	static boolean noVibration = false;
	
	boolean firstTouch = false;
	boolean secondTouch = false;
	boolean thirdTouch = false;
	static int max_neighbors=-1;
	static int min_neighbors=-1;

	
	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		public void onManagerConnected(int status){
			switch(status){
			case LoaderCallbackInterface.SUCCESS:
				initializeOpenCVDependencies();
				openCvCameraView.setOnTouchListener(MainActivity.this);
				
				break;
			default:
				super.onManagerConnected(status);
				break;				
			}
		}
		
	};

	private void initializeOpenCVDependencies(){
		try{
			
			InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
			
			// This section is to load the face cascade classifier
			
			File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
			File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
			FileOutputStream os = new FileOutputStream(mCascadeFile);
			
			byte[] buffer = new byte[4096];
			int bytesRead;
			while((bytesRead = is.read(buffer)) != -1){
				os.write(buffer, 0 , bytesRead);
			}
			is.close();
			os.close();
			
			cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
			InputStream isSmile = getResources().openRawResource(R.raw.haarcascade_smile);
			
			// This section is to load the Smile cascade classifier
			
			File cascadeDirSmile = getDir("cascadeSmile", Context.MODE_PRIVATE);
			File mCascadeFileSmile = new File(cascadeDirSmile, "lbpcascade_smile.xml");
			FileOutputStream osSmile = new FileOutputStream(mCascadeFileSmile);
			
			byte[] bufferSmile = new byte[4096];
			int bytesReadSmile;
			while((bytesReadSmile = isSmile.read(bufferSmile)) != -1){
				osSmile.write(bufferSmile, 0 , bytesReadSmile);

			}
			isSmile.close();
			osSmile.close();
			cascadeClassifierSmile = new CascadeClassifier(mCascadeFileSmile.getAbsolutePath());
		
		}
		catch(Exception e ){

		}
		
		openCvCameraView.enableView();
	}
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		 OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
		
		openCvCameraView = new JavaCameraView(this,1);//-1 for back camera, 1 for front camera
		
		setContentView(openCvCameraView);

		openCvCameraView.setCvCameraViewListener(this);
	}
	
	
	private class DistanceCloseNotification extends AsyncTask<String, Void, String> {
        
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

        		Toast.makeText(getBaseContext(), "Too close!", Toast.LENGTH_SHORT).show();            
       }
		@Override
		protected String doInBackground(String... urls) {
			// TODO Auto-generated method stub
			SystemClock.sleep(500);
				return "";
		}
		
    }
	private class DistanceFarNotification extends AsyncTask<String, Void, String> {
        
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
        		Toast.makeText(getBaseContext(), "Too far!", Toast.LENGTH_SHORT).show();        	            
       }
		@Override
		protected String doInBackground(String... urls) {
			// TODO Auto-generated method stub
			SystemClock.sleep(500);
				return "";
		}		
    }
	
	private class TouchNotification extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String result) {
        		Toast.makeText(getBaseContext(), "Touch!", Toast.LENGTH_SHORT).show();        	            
       }
		@Override
		protected String doInBackground(String... urls) {
			// TODO Auto-generated method stub
			SystemClock.sleep(500);
				return "";
		}		
    }

	private class CameraSwitch extends AsyncTask<String, Void, String> {        
        @Override
        protected void onPostExecute(String result) {
        	//-1 for back camera, 1 for front camera
        	if(CAMERA_SWITCH == -1)
        		Toast.makeText(getBaseContext(), "back camera set", Toast.LENGTH_SHORT).show();
    		else			
    			Toast.makeText(getBaseContext(), "front camera set", Toast.LENGTH_SHORT).show();						
        		        	            
       }
		@Override
		protected String doInBackground(String... urls) {
			// TODO Auto-generated method stub
			SystemClock.sleep(500);
				return "";
		}		
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()){
        case R.id.action_settings:
        	//-1 for back camera, 1 for front camera
        	if(CAMERA_SWITCH == 1)
        	{        		
        		CAMERA_SWITCH = -1;
        		new CameraSwitch().execute("");   
        		item.setTitle("Camera Front");
        	}
    		else
			{
    			CAMERA_SWITCH = 1;			
    			new CameraSwitch().execute("");
    			item.setTitle("Camera Back");
			}
    		openCvCameraView.disableView();
    		openCvCameraView.setCameraIndex(CAMERA_SWITCH);
    		openCvCameraView.enableView();    		
        	break;
        case R.id.vibration:
        	if(!noVibration)
        	{
        		noVibration = true;
        		item.setTitle("Vibration ON");
        	}        	
        	else
        	{
        		noVibration = false;
        		item.setTitle("Vibration OFF");
        	}
        	break;
        case R.id.exit:
        	this.finish();
        	break;        	
		}			
		return true;		
	}
	
	@Override
	public void onCameraViewStarted(int width, int height) {
		// TODO Auto-generated method stub
		grayScaleImage = new Mat(height, width, CvType.CV_8UC4);	 
        // The faces will be a 20% of the height of the screen
        absoluteFaceSize = (int) (height * 0.2);
	}

	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Mat onCameraFrame(Mat inputFrame) {
		if(!thirdTouch)
		{
			if(!firstTouch)
			{
				runOnUiThread(new Runnable() {
		            public void run() {
		            	openCvCameraView.setBackground(getResources().getDrawable(R.drawable.one));
		            }
		        });
				return inputFrame;
			}
			else if(!secondTouch)
			{
				runOnUiThread(new Runnable() {
		            public void run() {
		            	openCvCameraView.setBackground(getResources().getDrawable(R.drawable.two));
		            }
		        });
				return inputFrame;
			}			
			runOnUiThread(new Runnable() {
	            public void run() {
	            	openCvCameraView.setBackground(getResources().getDrawable(R.drawable.three));
	            }
	        });
			return inputFrame;			
		}
		else
		{
	        Imgproc.cvtColor(inputFrame, grayScaleImage, Imgproc.COLOR_RGBA2GRAY);
	 
        	MatOfRect faces = new MatOfRect();
	        
	        
	        //Mat smallImg = new Mat(inputFrame.size(), CvType.CV_8UC1);
	        Mat smallImg = new Mat(inputFrame.size(), CvType.CV_8U);
	        Mat smallImg2 = new Mat(inputFrame.size(), CvType.CV_8U);
	        
	        Imgproc.resize(grayScaleImage, smallImg2, smallImg.size(),0,0, Imgproc.INTER_LINEAR);
	        Imgproc.equalizeHist(smallImg2, smallImg);
	        
	        
	        
	        // Use the classifier to detect faces
	        if (cascadeClassifier != null) {
	            cascadeClassifier.detectMultiScale(smallImg, faces, 1.1, 2, 0 | 2,
	                    new Size(absoluteFaceSize, absoluteFaceSize), new Size());
	        } 
	        // If there are any faces found, draw a rectangle around it
	        Rect[] facesArray = faces.toArray();
	        for (int i = 0; i <facesArray.length; i++)
	        {

	        	if( facesArray[i].height > 490)        	        		
	            {
	        		/*
	        		 * This is the function to validate when the camera is too close
	        		 * */
	        		float intensityDistance = (float)(facesArray[i].height - 490) / 490;
	        		// the maximum value is 0.4        		
	        		intensityDistance = Math.abs(intensityDistance);
	        		intensityDistance = (float)intensityDistance/0.4f;
	        		if(!noVibration)
	        		{
		        		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		        		double timea = intensityDistance*500;        		
		        		long time =  (long)timea;	        		
		        		v.vibrate(time);  
	        		}
	                if(!hideMessage)
	                {
	                	new DistanceCloseNotification().execute("");
	                	hideMessage = true;
	                }                
	                runOnUiThread(new Runnable() {
	    	            public void run() {
	    	            	openCvCameraView.setBackground(getResources().getDrawable(R.drawable.closeh));
	    	            }
	    	        });
	                return inputFrame;
	            }
	        	else if( facesArray[i].height < 390 )
	        	{
	        		/*
	        		 * This is the function to validate when the camera is too close
	        		 * */
	        		float intensityDistance = (float)(facesArray[i].height - 490) / 490;
	        		// the maximum value is 0.4        		
	        		intensityDistance = Math.abs(intensityDistance);
	        		intensityDistance = (float)intensityDistance/0.4f;        		
	        		if(!noVibration)
	        		{
		        		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);        		
		        		double timea = intensityDistance*500;        		
		        		long time =  (long)timea;
		        		
		        		v.vibrate(time);
	        		}
	                if(!hideMessage)
	                {
	                	new DistanceFarNotification().execute("");
	                	hideMessage = true;
	                }
	                
	                runOnUiThread(new Runnable() {
	    	            public void run() {
	    	            	openCvCameraView.setBackground(getResources().getDrawable(R.drawable.farh));
	    	            }
	    	        });
	                return inputFrame;
	        	}
	        	Mat grayScaleImageROI= null;
	        	MatOfRect nestedObjects = new MatOfRect();
	        	
	        	hideMessage = false;
	
	        	new DistanceCloseNotification().cancel(true);
	        	new DistanceFarNotification().cancel(true);
	        	new TouchNotification().cancel(true);
	        	new CameraSwitch().cancel(true);  
	        	
	        	final int half_height = (int)Math.round((float)facesArray[i].height * 0.5);
	        	facesArray[i].y = facesArray[i].y + half_height;
	        	facesArray[i].height = half_height;
	        	
	        	grayScaleImageROI = smallImg.submat(facesArray[i]);
	    		
	        	
	        	
        		if (cascadeClassifierSmile != null) {
        			cascadeClassifierSmile.detectMultiScale(grayScaleImageROI, nestedObjects, 1.1, 0, 2, new Size(absoluteFaceSize, absoluteFaceSize), new Size());
                }
        		
        		Rect[] nestedArray = nestedObjects.toArray();
        		
        		int smile_neighbors = (int) nestedArray.length; // check the value in C++

        		if(min_neighbors ==-1)
        			min_neighbors = smile_neighbors;
    		
        		max_neighbors = Math.max(max_neighbors, smile_neighbors);	        		  
        		float intensityZeroOne = ((float)smile_neighbors - min_neighbors) / (max_neighbors - min_neighbors + 1);
        		float threshold= 0.1f;
        		
        		if(intensityZeroOne>threshold){

        			runOnUiThread(new Runnable() {
        	            public void run() {
        	            	openCvCameraView.setBackground(getResources().getDrawable(R.drawable.smileh));
        	            }
        	        });	        		
        			return inputFrame;        			
        		}
        		else{
        			runOnUiThread(new Runnable() {
        	            public void run() {
        	            	openCvCameraView.setBackground(getResources().getDrawable(R.drawable.sadh));
        	            }
        	        });
        	        return inputFrame;
        		}        		        		
	        }	               
	        runOnUiThread(new Runnable() {
	            public void run() {
	            	openCvCameraView.setBackground(getResources().getDrawable(R.drawable.noface));
	            }
	        });
	        return inputFrame;
		}
	}
	public void onPause()
    {
        super.onPause();
        if (openCvCameraView != null)
        	openCvCameraView.disableView();
        
        new DistanceFarNotification().cancel(true);
        new DistanceCloseNotification().cancel(true);
        new TouchNotification().cancel(true);
        new CameraSwitch().cancel(true);
    }
	public void onResume(){
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this, mLoaderCallback);
		//here validate
	}
	public void onDestroy() {
        super.onDestroy();
        if (openCvCameraView != null)
        	openCvCameraView.disableView();
        
        new DistanceFarNotification().cancel(true);
        new DistanceCloseNotification().cancel(true);
        new TouchNotification().cancel(true);
        new CameraSwitch().cancel(true);
    }

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(!thirdTouch)
		{
			if(!firstTouch)
			{
				firstTouch = true;
				return false;
			}
			else if(!secondTouch)
			{
				secondTouch= true;
				return false;
			}			
			thirdTouch= true;
			return false;			
		}
		return false;		
	}
}
