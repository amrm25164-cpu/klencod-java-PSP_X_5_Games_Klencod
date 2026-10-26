package com.pspx.app;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.MotionEvent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import java.util.Random;

public class MiniGameActivity extends Activity {
    Board board;
    @Override public void onCreate(Bundle b){super.onCreate(b);board=new Board(getIntent().getIntExtra("mode",0));setContentView(board);}
    @Override protected void onPause(){super.onPause();board.paused=true;}
    @Override protected void onResume(){super.onResume();if(board!=null){board.paused=false;board.invalidate();}}
    class Board extends View {
        final int mode; final Paint p=new Paint(3); final Random rand=new Random();
        boolean paused=false,over=false; int score=0,tick=0,dir=1,length=4;
        float player=.5f,ballX=.5f,ballY=.5f,vx=.012f,vy=.015f,targetX=.5f,targetY=.5f,enemyX=.2f,enemyY=0,shotY=-1,shotX=0;
        int[] sx=new int[400],sy=new int[400]; int foodX=9,foodY=8; float startX,startY;
        Board(int m){super(MiniGameActivity.this);mode=m;reset();setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        void reset(){score=0;tick=0;over=false;player=.5f;ballX=.5f;ballY=.5f;vx=.012f;vy=.015f;enemyY=0;shotY=-1;length=4;dir=1;
            for(int i=0;i<length;i++){sx[i]=8-i;sy[i]=10;}foodX=12;foodY=10;targetX=.3f;targetY=.45f;}
        void color(int c){p.setColor(c);p.setStyle(Paint.Style.FILL);}
        void rect(Canvas c,float x,float y,float w,float h){c.drawRect(x,y,x+w,y+h,p);}
        void circle(Canvas c,float x,float y,float r){c.drawCircle(x,y,r,p);}
        void txt(Canvas c,String s,float x,float y,int size){p.setTextSize(size);c.drawText(s,x,y,p);}
        @Override protected void onDraw(Canvas real){super.onDraw(real);int w=getWidth(),h=getHeight();if(w==0||h==0)return;
            Canvas c=real; c.save();c.scale(w/400f,h/700f);
            c.drawColor(Color.rgb(13,19,36));color(Color.WHITE);txt(c,"PSP X  |  Score: "+score,16,42,24);
            if(!paused&&!over){tick++;if(mode==0&&tick%9==0)snake();if(mode==1)pong();if(mode==2)space();if(mode==3)cars();if(mode==4&&tick%180==0)moveTarget();}
            if(mode==0){color(0xff26334b);rect(c,10,90,380,380);color(0xff58dd9b);for(int i=0;i<length;i++)rect(c,10+sx[i]*19,90+sy[i]*19,17,17);color(0xffff6b77);rect(c,10+foodX*19,90+foodY*19,17,17);color(Color.WHITE);txt(c,"Swipe to turn",90,550,23);}
            if(mode==1){color(0xff66c8ff);rect(c,player*400-48,620,96,15);color(0xffffdc74);circle(c,ballX*400,ballY*650+30,12);color(Color.WHITE);txt(c,"Drag the paddle",90,680,22);}
            if(mode==2){color(0xff58dd9b);c.drawOval(player*400-22,590,player*400+22,630,p);color(0xffff7777);circle(c,enemyX*400,enemyY*630+50,20);if(shotY>=0){color(0xffffdc74);rect(c,shotX*400-3,shotY*700,6,18);}color(Color.WHITE);txt(c,"Drag to move, tap to shoot",55,680,20);}
            if(mode==3){color(0xff3b4355);rect(c,45,80,310,560);color(0xff66c8ff);rect(c,player*300+45-18,560,36,65);color(0xffff7777);rect(c,enemyX*300+45-18,enemyY*650,36,65);color(Color.WHITE);txt(c,"Drag to dodge",110,680,22);}
            if(mode==4){color(0xfff3b44d);circle(c,targetX*400,targetY*650+30,34);color(0xffb94c4c);circle(c,targetX*400,targetY*650+30,19);color(Color.WHITE);txt(c,"Tap the target",105,680,23);}
            if(over){color(0xdd000000);rect(c,0,245,400,180);color(Color.WHITE);txt(c,"GAME OVER",95,305,32);txt(c,"Tap to restart",105,365,25);}
            c.restore();if(!paused)postInvalidateDelayed(35);
        }
        void snake(){int nx=sx[0]+(dir==1?1:dir==3?-1:0),ny=sy[0]+(dir==2?1:dir==0?-1:0);if(nx<0||nx>=20||ny<0||ny>=20){over=true;return;}for(int i=0;i<length;i++)if(sx[i]==nx&&sy[i]==ny){over=true;return;}if(nx==foodX&&ny==foodY){score++;length=Math.min(length+1,399);foodX=rand.nextInt(20);foodY=rand.nextInt(20);}for(int i=length-1;i>0;i--){sx[i]=sx[i-1];sy[i]=sy[i-1];}sx[0]=nx;sy[0]=ny;}
        void pong(){ballX+=vx;ballY+=vy;if(ballX<.03||ballX>.97)vx=-vx;if(ballY<0)vy=Math.abs(vy);if(ballY>.89&&ballY<.94&&Math.abs(ballX-player)<.14&&vy>0){vy=-vy;score++;}if(ballY>1.05)over=true;}
        void space(){enemyY+=.009f+score*.0001f;if(shotY>=0)shotY-=.027f;if(shotY<0)shotY=-1;if(shotY>=0&&Math.abs(shotX-enemyX)<.08&&Math.abs(shotY*700-(enemyY*630+50))<30){score++;enemyY=0;enemyX=.08f+rand.nextFloat()*.84f;shotY=-1;}if(enemyY>1){over=true;}}
        void cars(){enemyY+=.008f+score*.00015f;if(enemyY>.98){enemyY=-.15f;enemyX=.12f+rand.nextFloat()*.76f;score++;}if(enemyY>.75&&enemyY<.97&&Math.abs(enemyX-player)<.13)over=true;}
        void moveTarget(){targetX=.12f+rand.nextFloat()*.76f;targetY=.15f+rand.nextFloat()*.67f;}
        @Override public boolean onTouchEvent(MotionEvent e){float x=e.getX()/getWidth(),y=e.getY()/getHeight();if(e.getAction()==MotionEvent.ACTION_DOWN){if(over){reset();invalidate();return true;}startX=x;startY=y;if(mode==4){float dx=x*400-targetX*400,dy=y*700-(targetY*650+30);if(dx*dx+dy*dy<42*42){score++;moveTarget();}}if(mode==2){shotX=player;shotY=.83f;}return true;}
            if(e.getAction()==MotionEvent.ACTION_MOVE){if(mode==1||mode==2||mode==3)player=Math.max(.12f,Math.min(.88f,x));return true;}
            if(e.getAction()==MotionEvent.ACTION_UP){if(mode==0){float dx=x-startX,dy=y-startY;if(Math.abs(dx)>Math.abs(dy)){int nd=dx>0?1:3;if((nd+2)%4!=dir)dir=nd;}else{int nd=dy>0?2:0;if((nd+2)%4!=dir)dir=nd;}}return true;}return true;}
    }
}
