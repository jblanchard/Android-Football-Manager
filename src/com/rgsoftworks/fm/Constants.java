package com.rgsoftworks.fm;

import android.graphics.Color;
import android.view.KeyEvent;

interface Constants {
	public static final int MAXTEAMS = 24;                          /* Max teams per division */
	public static final int MAXPLAYERS = 35;                          /* Max players available */
	public static final int DIVISIONS = 6;                           /* Number of divisions */
	public static final int AVAILABLE=0;
	public static final int PICKED=1;
	public static final int INJURED=2;
	public static final char DEFENCE='d';
	public static final char MIDFIELD='m';
	public static final char ATTACK='a';
	public static final int COL_BLACK = Color.BLACK;                           /* Colour constants */
	public static final int COL_RED = Color.RED;
	public static final int COL_GREEN = Color.GREEN;
	public static final int COL_YELLOW = Color.YELLOW;
	public static final int COL_BLUE = Color.BLUE;
	public static final int COL_MAGENTA = Color.MAGENTA;
	public static final int COL_CYAN = Color.CYAN;
	public static final int COL_WHITE = Color.WHITE;
	public static final int COL_DKGREEN = Color.argb(255, 50, 100, 50);
	public static final int COL_GREY = Color.GRAY;
	public static final int COL_CONVERT[] = { Color.BLACK,Color.RED,Color.GREEN,Color.YELLOW,Color.BLUE,Color.MAGENTA,Color.CYAN,COL_DKGREEN,Color.GRAY };
	public static final int VK_UP = KeyEvent.KEYCODE_DPAD_UP;
	public static final int VK_DOWN = KeyEvent.KEYCODE_DPAD_DOWN;
	public static final int VK_LEFT = KeyEvent.KEYCODE_DPAD_LEFT;
	public static final int VK_RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT;
	public static final int VK_FX = KeyEvent.KEYCODE_DPAD_CENTER;
	public static final int VK_FA = 5;
	public static final int VK_FL = 6;
	public static final int VK_FR = 7;
}
