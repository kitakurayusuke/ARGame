package com.example.argame;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

//public class ARView extends SurfaceView implements SurfaceHolder.Callback {
public class ARView extends View {

	private int displayX;
	private int displayY;

	private int mScore;
	private int mState;

	public ARView(Context context) {
		super(context);
		// 画面サイズの取得
		Display disp = ((WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		displayX = disp.getWidth();
		displayY = disp.getHeight();
		mState = GLRenderer.STATE_START;
	}

	// 描画処理
	@Override
	protected void onDraw(Canvas canvas) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);

		// スコアの描画
		paint.setTextSize(64);
		paint.setColor(Color.WHITE);
		canvas.drawText("SCORE:" + Integer.toString(mScore), 100, 100, paint);
		
		
		if(mState == GLRenderer.STATE_START){
			String str = new String("TAP TO START!");
			paint.setTextSize(128);
			paint.setColor(Color.BLUE);
			float textWidth = paint.measureText(str) / 2;
			canvas.drawText(str, displayX/2-textWidth, displayY/2, paint);
		}else if (mState == GLRenderer.STATE_GAMEOVER){
			String str = new String("GAME OVER");
			paint.setTextSize(128);
			paint.setColor(Color.GREEN);
			float textWidth = paint.measureText(str) / 2;
			canvas.drawText(str , displayX/2-textWidth, displayY/2, paint);
			
			paint.setTextSize(64);
			paint.setColor(Color.YELLOW);
			if(mScore >= 5){canvas.drawText("GOOD", 500, 100, paint);}
			else{canvas.drawText("BAD" , 500, 100, paint);}
			
		}
	
	}

	public void drawScreen(int state, int score) {
		// 状態の更新
		mState = state;
		mScore = score;
		// onDrawを呼び出して再描画
		invalidate();
	}

}
