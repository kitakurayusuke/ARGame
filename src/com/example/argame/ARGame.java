package com.example.argame;

import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class ARGame extends Activity implements SensorEventListener {

	private SensorManager sensorManager;
	private float[] accelerometerValues = new float[3];
	private float[] magneticValues = new float[3];
	private ARView arView;

	private GLSurfaceView mGLSurfaceView;
	private GLRenderer mGLRenderer;

	private List<Sensor> listMag;
	private List<Sensor> listAcc;

	
	private int displayX;
	private int displayY;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		
		getWindow().clearFlags(
				WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// ARViewの取得
		arView = new ARView(this);

		// 各種センサーの用意
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		listMag = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
		listAcc = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);

		// 画面サイズの取得
		Display disp = ((WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		displayX = disp.getWidth();
		displayY = disp.getHeight();

		mGLSurfaceView = new GLSurfaceView(this);
		mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);

		mGLRenderer = new GLRenderer(this);
		mGLSurfaceView.setRenderer(mGLRenderer);
		mGLSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		// Viewの重ね合わせ
		setContentView(mGLSurfaceView);
		addContentView(new CameraView(this), new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		addContentView(arView, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
	}

	@Override
	protected void onResume() {
		super.onResume();
		mGLSurfaceView.onResume();
		sensorManager.registerListener(this, listMag.get(0),
				SensorManager.SENSOR_DELAY_GAME);
		sensorManager.registerListener(this, listAcc.get(0),
				SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	public void onStop() {
		super.onStop();
		sensorManager.unregisterListener(this);
	}

	// add
	@Override
	protected void onPause() {
		super.onPause();
		mGLSurfaceView.onPause();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	// センサー値の反映
	@Override
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			accelerometerValues = event.values.clone();
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			magneticValues = event.values.clone();
			break;
		}

		if (magneticValues != null && accelerometerValues != null) {
			float[] R = new float[16];
			float[] I = new float[16];

			SensorManager.getRotationMatrix(R, I, accelerometerValues,
					magneticValues);

			float[] actual_orientation = new float[3];

			SensorManager.getOrientation(R, actual_orientation);

			mGLRenderer.setState(actual_orientation[0], actual_orientation[2]);

			arView.drawScreen(mGLRenderer.getState(), mGLRenderer.getScore());
		}
	}

	// 画面に触れたときに呼び出される関数
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mGLRenderer.onTouch((int) event.getX(), displayY
					- (int) event.getY());
			break;
		case MotionEvent.ACTION_UP:
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_CANCEL:
			break;
		}
		return super.onTouchEvent(event);
	}

}
