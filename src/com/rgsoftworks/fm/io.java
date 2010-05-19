package com.rgsoftworks.fm;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface.OnClickListener;
import android.graphics.*;
import com.rgsoftworks.fm.drv_linux;

public class io {
	/************************************************************************/
	/*																		*/
	/*		Name:		IO.C												*/
	/*		Project:	Football Manager Remake								*/
	/*		Purpose:	IO Functions, Driver Interface						*/
	/*		Author:		Paul Robson											*/
	/*		Created:	7th December 2001									*/
	/*		Updated:	17th December 2001									*/
	/*																		*/
	/************************************************************************/

	/************************************************************************/
	/*																		*/
	/*	Functions with a straight functionality equivalent in the driver	*/
	/*																		*/
	/************************************************************************/

	/************************************************************************/
	/*																		*/
	/*						Hardware Initialisation							*/
	/*																		*/
	/************************************************************************/

	static final int COPYFROMBB=1;
	static final int COPYTOBB=0;

	static Canvas backupCanvas=null;
	static Bitmap backupBitmap=null;
		
	static void IOCopy(int CopyType) {
		if(backupCanvas==null) {
			backupCanvas = new Canvas();
			backupBitmap = Bitmap.createBitmap(drv_linux.mCanvas.getWidth(), drv_linux.mCanvas.getHeight(), Bitmap.Config.RGB_565);
			backupCanvas.setBitmap(backupBitmap);
		}
		if(CopyType==COPYFROMBB) {
			drv_linux.mCanvas.drawBitmap(backupBitmap, 0, 0, null);
		} else {
			backupCanvas.drawBitmap(drv_linux.mBitmap, 0, 0, null);			
		}
	}
	
	static void IOInitialise()
	{
	    drv_linux.HWInitialise();
	}

	static int IOInkey() {
		return drv_linux.HWInkey();
	}
	
	static void IODisplayGoal() {
		drv_linux.HWDisplayGoal();
	}

	static int IOJoy() {
		return drv_linux.getKeyPress();
	}
	
	static void IOTerminate() {
		drv_linux.HWTerminate();
	}

	static void OKDialog(String TitleText, String MessageText, String Button1Text,
			OnClickListener OKCode) {
		
        Builder OKMessage = new Builder(drv_linux.fm);

        String titleText = TitleText;
        OKMessage.setTitle(titleText);

        String message = MessageText;
        OKMessage.setMessage(message);
        
        OKMessage.setPositiveButton(Button1Text,OKCode);

        OKMessage.setCancelable(false);
        OKMessage.show();
	}
	
    static void YesNo(String TitleText, String MessageText, String Button1Text, String Button2Text, 
    		OnClickListener YesCode, OnClickListener NoCode) {
        Builder YesNoMessage = new Builder(drv_linux.fm);

        String titleText = TitleText;
        YesNoMessage.setTitle(titleText);

        String message = MessageText;
        YesNoMessage.setMessage(message);
        
        YesNoMessage.setPositiveButton(Button1Text,YesCode);
        YesNoMessage.setNegativeButton(Button2Text,NoCode);

        YesNoMessage.setCancelable(false);
        YesNoMessage.show();
      }

	static long IOClock() {
		return drv_linux.HWClock();
	}

	static void IOUpdate() {
		drv_linux.HWUpdate();
	}
	
	static void IOSound(int a,int b) {
		
	}
	
	/************************************************************************/
	/*																		*/
	/*					Put with validated parameters						*/
	/*																		*/
	/************************************************************************/

	static void IOPut(int x,int y,int Colour)
	{
	    if (x < 0 || x >= 256) { return; }
	    if (y < 0 || y >= 192) { return; }
	    drv_linux.HWPut(x,y,Colour);
	}

	/************************************************************************/
	/*																		*/
	/*					Errors, Assert rejections come here					*/
	/*																		*/
	/************************************************************************/

	static void IOError(int Line,String File)
	{
	    drv_linux.HWTerminate();                                /* Crash out */
	}


	/************************************************************************/
	/*																		*/
	/*							Character drawer							*/
	/*																		*/
	/************************************************************************/

	static void IOChar(int x,int y,int Ink,int Paper,int Char)
	{
	    int x1,y1,Pixel;

	    if (drv_linux.HWXChar(x,y,Ink,Paper,Char)) {             /* Driver function ? */
	        return;
	    }

	    if(Paper!=-1) {
	    	drv_linux.HWRectangle(x,y,x+8,y+8,Paper);
	    }

	    for (x1 = 0;x1 < 8;x1++)                      /* Work through the font bits */
	        for (y1 = 0;y1 < 8;y1++)
	    {
	        Pixel =                                   /* Get the pixel */
	            SpecFont[(Char & 0x7F)*8+y1] & (0x80 >> x1);
	        if (Paper > 0 || Pixel != 0) {
	            IOPut(x+x1,y+y1,Pixel>0 ? Ink:Paper);
	        }
	    }
	}


	/************************************************************************/
	/*																		*/
	/*								Draw a line								*/
	/*																		*/
	/************************************************************************/

	static void IOLine(int x1,int y1,int x2,int y2,int Colour)
	{
	    int n,d1,d2;
	    if (x1 == x2 && y1 == y2) { return; }             /* Nothing to do */
	    if (drv_linux.HWXLine(x1,y1,x2,y2,Colour)) {             /* Driver can do it ? */
	        return;
	    }
	    d1 = Math.abs(x1-x2);d2 = Math.abs(y1-y2);              /* Calculate differences */
	    if (d2 > d1) { d1 = d2; }                         /* d1 is the biggest difference */
	    for (n = 0;n <= d1;n++) {                      /* Draw dots along that line */
	        IOPut(x1+(x2-x1)*n/d1,                    /* I know Bresenham's better */
	            y1+(y2-y1)*n/d1,Colour);
	    }
	}

	static void IOClear(int Colour) {
		drv_linux.mCanvas.drawColor(Colour);
	}
	
	/************************************************************************/
	/*																		*/
	/*								Display text							*/
	/*																		*/
	/************************************************************************/

	static void IOText(int x1,int y1,int Ink,int Paper,String Text)
	{
		int pos=0;
	    if (x1 < 0) { x1=128-Text.length()*4; }            /* Auto centring */
	    while (pos<Text.length())
	    {
	        IOChar(x1,y1,Ink,Paper,Text.charAt(pos));
	        x1 += 8;
	        pos++;
	    }
	    drv_linux.HWUpdate();
	}

	/************************************************************************/
	/*																		*/
	/*                      Spectrum Font and Graphics                      */
	/*																		*/
	/************************************************************************/

	public static char SpecFont[] = {
	    24,88,126,26,120,72,206,2,48,48,24,60,28,56,48,40,
	    24,24,30,58,24,42,36,32,24,24,24,28,60,24,28,8,
	    24,24,60,90,90,60,36,102,0,0,0,0,0,0,3,3,
	    24,26,126,88,30,18,115,64,12,12,24,60,56,28,12,20,
	    24,24,120,92,24,84,36,4,24,24,24,56,60,24,56,8,
	    0,0,0,0,0,0,192,192,24,24,61,90,56,28,20,36,
	    24,24,188,90,28,56,40,36,12,12,60,91,152,62,64,192,
	    48,48,60,218,25,124,2,3,255,255,255,255,255,255,255,255,
	    24,88,126,26,120,72,206,2,48,48,24,60,28,56,48,40,
	    24,24,30,58,24,42,36,32,24,24,24,28,60,24,28,8,
	    24,24,60,90,90,60,36,102,0,0,0,0,0,0,3,3,
	    24,26,126,88,30,18,115,64,12,12,24,60,56,28,12,20,
	    24,24,120,92,24,84,36,4,24,24,24,56,60,24,56,8,
	    0,0,0,0,0,0,192,192,24,24,61,90,56,28,20,36,
	    24,24,188,90,28,56,40,36,12,12,60,91,152,62,64,192,
	    48,48,60,218,25,124,2,3,255,255,255,255,255,255,255,255,
	    0,0,0,0,0,0,0,0,0,16,16,16,16,0,16,0,
	    0,36,36,0,0,0,0,0,0,36,126,36,36,126,36,0,
	    0,8,62,40,62,10,62,8,0,98,100,8,16,38,70,0,
	    0,16,40,16,42,68,58,0,0,8,16,0,0,0,0,0,
	    0,4,8,8,8,8,4,0,0,32,16,16,16,16,32,0,
	    0,0,20,8,62,8,20,0,0,0,8,8,62,8,8,0,
	    0,0,0,0,0,8,8,16,0,0,0,0,62,0,0,0,
	    0,0,0,0,0,24,24,0,0,0,2,4,8,16,32,0,
	    0,60,70,74,82,98,60,0,0,24,40,8,8,8,62,0,
	    0,60,66,2,60,64,126,0,0,60,66,12,2,66,60,0,
	    0,8,24,40,72,126,8,0,0,126,64,124,2,66,60,0,
	    0,60,64,124,66,66,60,0,0,126,2,4,8,16,16,0,
	    0,60,66,60,66,66,60,0,0,60,66,66,62,2,60,0,
	    0,0,0,16,0,0,16,0,0,0,16,0,0,16,16,32,
	    0,0,4,8,16,8,4,0,0,0,0,62,0,62,0,0,
	    0,0,16,8,4,8,16,0,0,60,66,4,8,0,8,0,
	    0,60,74,86,94,64,60,0,0,60,66,66,126,66,66,0,
	    0,124,66,124,66,66,124,0,0,60,66,64,64,66,60,0,
	    0,120,68,66,66,68,120,0,0,126,64,124,64,64,126,0,
	    0,126,64,124,64,64,64,0,0,60,66,64,78,66,60,0,
	    0,66,66,126,66,66,66,0,0,62,8,8,8,8,62,0,
	    0,2,2,2,66,66,60,0,0,68,72,112,72,68,66,0,
	    0,64,64,64,64,64,126,0,0,66,102,90,66,66,66,0,
	    0,66,98,82,74,70,66,0,0,60,66,66,66,66,60,0,
	    0,124,66,66,124,64,64,0,0,60,66,66,82,74,60,0,
	    0,124,66,66,124,68,66,0,0,60,64,60,2,66,60,0,
	    0,254,16,16,16,16,16,0,0,66,66,66,66,66,60,0,
	    0,66,66,66,66,36,24,0,0,66,66,66,66,90,36,0,
	    0,66,36,24,24,36,66,0,0,130,68,40,16,16,16,0,
	    0,126,4,8,16,32,126,0,0,14,8,8,8,8,14,0,
	    0,0,64,32,16,8,4,0,0,112,16,16,16,16,112,0,
	    0,16,56,84,16,16,16,0,0,0,0,0,0,0,0,255,
	    0,28,34,120,32,32,126,0,0,0,56,4,60,68,60,0,
	    0,32,32,60,34,34,60,0,0,0,28,32,32,32,28,0,
	    0,4,4,60,68,68,60,0,0,0,56,68,120,64,60,0,
	    0,12,16,24,16,16,16,0,0,0,60,68,68,60,4,56,
	    0,64,64,120,68,68,68,0,0,16,0,48,16,16,56,0,
	    0,4,0,4,4,4,36,24,0,32,40,48,48,40,36,0,
	    0,16,16,16,16,16,12,0,0,0,104,84,84,84,84,0,
	    0,0,120,68,68,68,68,0,0,0,56,68,68,68,56,0,
	    0,0,120,68,68,120,64,64,0,0,60,68,68,60,4,6,
	    0,0,28,32,32,32,32,0,0,0,56,64,56,4,120,0,
	    0,16,56,16,16,16,12,0,0,0,68,68,68,68,56,0,
	    0,0,68,68,40,40,16,0,0,0,68,84,84,84,40,0,
	    0,0,68,40,16,40,68,0,0,0,68,68,68,60,4,56,
	    0,0,124,8,16,32,124,0,0,14,8,48,8,8,14,0,
	    0,8,8,8,8,8,8,0,0,112,16,12,16,16,112,0,
	    0,20,40,0,0,0,0,0,60,66,153,161,161,153,66,60 };
}