package com.rgsoftworks.fm;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.SystemClock;

public class drv_linux {

	public static Global Glo = new Global();
	public static int xOffset=0;
	public static int yOffset=0;
	static int Scale=0;
	boolean IsWhiteNoise;
	public static final int AudioFound=0;
	public static Canvas mCanvas=null;
	public static Bitmap mBitmap=null;
	public static footballmanager fm=null;
	public static int mKeyPress=0;
	public static float touchX=-1;
	public static float touchY=-1;

	public static int getKeyPress() {
		int thisPress=mKeyPress;
		mKeyPress=0;
		return thisPress;
	}
	public static void setKeyPress(int newPress) {
		mKeyPress=newPress;
	}
	/************************************************************************/
	/*																		*/
	/*			Tables converting to and from the IBM Colours				*/
	/*																		*/
	/************************************************************************/

	static int ToHW[] = { 0,12,10,14,9,13,11,15,2,7,7,7,7,7,7,7 };
	static int FromHW[] = new int[16];
	
	/************************************************************************/
	/*																		*/
	/*						Initialise the driver							*/
	/*																		*/
	/************************************************************************/

	static void HWInitialise()
	{
	    int xs,ys;

	    xs = Glo.xSize/256;ys = Glo.ySize/192;        /* Calculate working scale */
	    Scale = (xs > ys) ? ys : xs;
	    xOffset = (Glo.xSize-256*Scale)/2;            /* And frame */
	    yOffset = (Glo.ySize-192*Scale)/2;
	}

	/************************************************************************/
	/*																		*/
	/*							Clear the screen							*/
	/*																		*/
	/************************************************************************/

	void HWClear(int Col)
	{
		mCanvas.drawColor(Col);
	}


	/************************************************************************/
	/*																		*/
	/*						Terminate the driver							*/
	/*																		*/
	/************************************************************************/

	public static boolean HWRectangle(int x,int y,int r, int b, int Paper) {
	    Rect rc=new Rect();
	    //if (Scale == 1) return false;                     /* IO Layer does it if scale is 1 */
	    rc.left = x * Scale + xOffset;
	    rc.top = y * Scale + yOffset;
	    rc.right = (8*Scale)+rc.left;
	    rc.bottom = (8 * Scale)+rc.top;
	    if (Paper != 0) { SDL_FillRect(mCanvas,rc,_HWSpecToSDL(Paper)); }

	    return true;
	}
	
	static void HWTerminate()
	{
	}

	static float getTouchX() {
		return touchX/Scale-xOffset;
	}
	static float getTouchY() {
		return touchY/Scale-yOffset;
	}
	/************************************************************************/
	/*																		*/
	/*								Put a pixel								*/
	/*																		*/
	/************************************************************************/

	static void HWPut(int x,int y,int Colour)
	{
	    Rect rc=new Rect();
	    rc.left = x*Scale+xOffset;rc.top = y*Scale+yOffset;rc.right = Scale+rc.left; rc.bottom = Scale+rc.top;
	    //SDL_FillRect(Screen,&rc,_HWSpecToSDL(Colour));
	    Paint p = new Paint();
	    
	    p.setColor(Colour);
	    mBitmap.setPixel(rc.left, rc.top, _HWSpecToSDL(Colour));
	    //mCanvas.drawPoint(rc.left, rc.top, p);
	    //SDL_FillRect(mCanvas,rc,Colour);
	}

	/************************************************************************/
	/*                                                                      */
	/*              Convert Speccy Colour to SDL Colour format              */
	/*                                                                      */
	/************************************************************************/

	static int _HWSpecToSDL(int Colour)
	{
		return Colour;
	    //int r,g,b,l;
	    //Colour = ToHW[Colour & 15];                   /* Convert colour to hw colour */
	    //l = (Colour & 0x8)>0 ? 255:128;
	    //r = (Colour & 4)>0 ? l:0;
	    //g = (Colour & 2)>0 ? l:0;
	    //b = (Colour & 1)>0 ? l:0;
	    //return Color.argb(255, r, g, b);
	}

	static int convertSpecColour(int Colour) {
		if(Colour>0 && Colour<8) { return Constants.COL_CONVERT[Colour]; }
		return Colour;
	}
	/************************************************************************/
	/*                                                                      */
	/*                          Update the display                          */
	/*                                                                      */
	/************************************************************************/

	static void HWUpdate()
	{
	    //SDL_UpdateRect(Screen,0,0,Screen->w,Screen->h);
	}

	/************************************************************************/
	/*																		*/
	/*					Read the time in milliseconds						*/
	/*																		*/
	/************************************************************************/

	static long HWClock()
	{
	    return SystemClock.elapsedRealtime();
	}

	/************************************************************************/
	/*																		*/
	/*					Get a keystroke	if there is one						*/
	/*																		*/
	/************************************************************************/

	static int  HWInkey()
	{
//	    SDL_Event Event;
	    int ch=0;
	    HWUpdate();
//	    if (SDL_PollEvent(&Event) == 0) return 0;
//	    if (Event.type != SDL_KEYDOWN) return 0;
//	    ch = Event.key.keysym.unicode;
//	    if (ch & 0xFF80) return 0;
//	    ch = ch & 0x7F;ch = toupper(ch);
	    return ch;
	}


	/************************************************************************/
	/*																		*/
	/*							  Kick/Crowd sound							*/
	/*																		*/
	/************************************************************************/

	void HWSound(int Delay,int WhiteNoise)
	{
	}


	/************************************************************************/
	/*																		*/
	/*						Copy to or from the backbuffer					*/
	/*																		*/
	/************************************************************************/

	void HWCopy(int Dir)
	{
		/*
	    SDL_Rect rc,rc2;
	    rc.x = rc.y = 0;rc.w = Screen->w;rc.h = Screen->h;rc2 = rc;
	    switch(Dir)
	    {
	        case COPYTOBB:
	            SDL_BlitSurface(Screen,&rc,ScreenBuffer,&rc2);
	            break;
	        case COPYFROMBB:
	            SDL_BlitSurface(ScreenBuffer,&rc,Screen,&rc2);
	            break;
	    } */
	}

	public static void SDL_FillRect(Canvas Screen, Rect rc, int Colour) {
	    Paint p = new Paint();
	    
	    p.setColor(Colour);
	    Screen.drawRect(rc, p);
	}

	public static void FONTChar(Canvas Screen, Rect rc, int Colour, int Char) {
		Paint p = new Paint();
		char character=(char)Char;
		
		p.setColor(Colour);
		
		if(character=='`') character = '£';

		p.setTextSize(10);
		p.setTypeface(Typeface.SANS_SERIF);
		Screen.drawText(Character.toString(character), rc.left, rc.top, p);
	}

	public static void FONTString(Canvas Screen, Rect rc, int Colour, String string) {
		Paint p = new Paint();
		
		p.setColor(Colour);
		p.setTextSize(24);
		
		rc.top = rc.top+(((rc.bottom-rc.top)-(int)(p.getFontMetrics().bottom+p.getFontMetrics().top))/2);
		rc.left = rc.left+(((rc.right-rc.left)-(int)p.measureText(string))/2);
		Screen.drawText(string, rc.left, rc.top, p);
	}

	/************************************************************************/
	/*																		*/
	/*				Character drawing function (optional)					*/
	/*																		*/
	/************************************************************************/

	static boolean HWXChar(int x,int y,int Ink,int Paper,int Char)
	{
	    Rect rc=new Rect();
	    if (Scale == 1) { return false; }                    /* IO Layer does it if scale is 1 */
	    rc.left = x * Scale + xOffset;
	    rc.top = y * Scale + yOffset;
	    rc.right = (8*Scale)+rc.left;
	    rc.bottom = (8 * Scale)+rc.top;
	    if (Paper != 0) { drv_linux.HWRectangle(x,y-8,x+8,y,Paper); }//SDL_FillRect(mCanvas,rc,_HWSpecToSDL(Paper));
	    FONTChar(mCanvas,rc,_HWSpecToSDL(Ink),Char);
	    return true;
	}

	/************************************************************************/
	/*																		*/
	/*					Line drawing function (optional)					*/
	/*																		*/
	/************************************************************************/

	public static boolean HWXLine(int x1,int y1,int x2,int y2,int Colour)
	{
	    x1++;x2++;y1++;y2++;Colour++;                 /* Warnings */
	    return false;                                     /* IO Layer does it */
	}

	/************************************************************************/
	/*																		*/
	/*                       Replaced the GOAL display                      */
	/*																		*/
	/************************************************************************/


	static void HWDisplayGoal()
	{
	    Rect rc=new Rect();
	    rc.left = xOffset;rc.top = yOffset;rc.right = (32*8*Scale)+rc.left;rc.bottom = (8*4*Scale)+rc.top;
	    SDL_FillRect(mCanvas,rc,Color.argb(255,255,0,0));
	    FONTString(mCanvas,rc,Color.argb(255,255,255,0)," GOAL ");
	    HWUpdate();
	}

	void HWFillAudio(int Data[],int Stream[],int Size)
	{
	    int i;

	    for (i = 0;i < Size;i++)
	        if (IsWhiteNoise) {
	            Stream[i] = (int)Math.floor(Math.random());
	        } else {
	            Stream[i] = ((i & 32)>0) ? 255:0;
	        }
	}
}
