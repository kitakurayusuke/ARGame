package com.example.argame;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Vibrator;

/**
 * Render a pair of tumbling cubes.
 */

class GLRenderer implements GLSurfaceView.Renderer {

	private Context mContext;
	private Vibrator vibrator;

	float camThetaXZ;
	float camThetaY;
	final float CAMERA_R = 10;

	private Enemy enemy;

	private int touchX;
	private int touchY;

	private boolean isTouched = false;

	public static final int STATE_START = 0;
	public static final int STATE_PLAY = 1;
	public static final int STATE_GAMEOVER = 2;

	private int state;
	private int score;

	public GLRenderer(Context context) {
		mContext = context;
		// 振動器の用意
		vibrator = (Vibrator) mContext
				.getSystemService(Context.VIBRATOR_SERVICE);
		initialize();
	}

	private void initialize() {
		enemy = new Enemy();
		state = STATE_START;
		score = 0;
	}

	public void onDrawFrame(GL10 gl) {
		switch (state) {
		case STATE_START:
			if (isTouched) {
				isTouched = false;
				state = STATE_PLAY;
			}
			break;
		case STATE_PLAY:
			// 描画処理を行う前にバッファの消去を行う
			// GL_COLOR_BUFFER_BITではカラーバッファを
			// GL10.GL_DEPTH_BUFFER_BITでは陰面消去に使われるデプスバッファを
			// 指定しています
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

			// 演算対象をモデルビューにする
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			// 演算行列を単位行列にする
			gl.glLoadIdentity();

			// カメラの位置と、端末の向きから視点の中心を求める
			float centerY = CAMERA_R * (float) Math.sin(camThetaY);
			float centerX = CAMERA_R * (float) Math.cos(camThetaY)
					* (float) Math.cos(-camThetaXZ);
			float centerZ = CAMERA_R * (float) Math.cos(camThetaY)
					* (float) Math.sin(-camThetaXZ);
			// カメラの位置と、視点の中心を指定する
			GLU.gluLookAt(gl, 0, 0, 0, centerX, centerY, centerZ, 0f, 1f, 0f);

			// 頂点配列を有効化します
			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			// カラー配列を有効化します
			gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

			// オブジェクトを移動します
			enemy.move(gl);
			// オブジェクトを描画します
			enemy.draw(gl);

			if (enemy.isHit()) {
				state = STATE_GAMEOVER;
				vibrator.vibrate(10000);
			}

			
			if (isTouched) {
				isTouched = false;
				slap(gl);
			}


			break;
		case STATE_GAMEOVER:
			if (isTouched) {
				isTouched = false;
				initialize();
			}
			break;
		}
	}

	public void onTouch(int x, int y) {
		isTouched = true;
		touchX = x;
		touchY = y;
	}

	private void slap(GL10 gl) {
		int color[] = new int[1];
		ByteBuffer buf = ByteBuffer.allocateDirect(4);
		buf.order(ByteOrder.nativeOrder());
		gl.glReadPixels(touchX, touchY, 1, 1, GL10.GL_RGBA,
				GL10.GL_UNSIGNED_BYTE, buf);
		buf.asIntBuffer().get(color);
		if (color[0] != 0) {
			vibrator.vibrate(100);
			enemy.initPosition();
			score += 1;
		}
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// 描画領域が変更されたときに呼び出されます

		// 描画を領域行う領域を指定します
		// ここでは画面全体を指定しています
		gl.glViewport(0, 0, width, height);

		float ratio = (float) width / height;
		// 射影行列を選択する状態にします
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		// 射影方法を遠近法を使用する透視射影として描画領域を指定します
		gl.glFrustumf(-ratio, ratio, -1.0f, 1.0f, 1f, 10000f);
		// ディザ処理を無効化し、なめらかな表示を行います
		gl.glDisable(GL10.GL_DITHER);
		// 透視射影の程度を処理速度を重視したものを指定します
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
		// 背景を透明に設定します
		gl.glClearColor(0, 0, 0, 0);
		// ポリゴンの背面を描画しなようにします
		gl.glEnable(GL10.GL_CULL_FACE);
		// 面の描画をなめらかにするようにします
		gl.glShadeModel(GL10.GL_SMOOTH);
		// デプスバッファを有効化します
		gl.glEnable(GL10.GL_DEPTH_TEST);
		// テクスチャを有効化します
		gl.glEnable(GL10.GL_TEXTURE_2D);
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// 描画領域が作成されたときに呼び出されます
	}

	public void setState(float thetaXZ, float thetaY) {
		// カメラの角度を取得します
		camThetaXZ = -thetaXZ;
		camThetaY = (float) (-Math.PI / 2 - thetaY);
	}

	public int getState() {
		return state;
	}

	public int getScore() {
		return score;
	}

}
