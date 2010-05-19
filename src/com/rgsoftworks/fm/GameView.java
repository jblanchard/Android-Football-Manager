package com.rgsoftworks.fm;

import java.io.IOException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

class GameView extends SurfaceView implements SurfaceHolder.Callback {
	class GameThread extends Thread {
        private boolean mPaused;
        private boolean mHasFocus;
        private boolean mHasSurface;
        private boolean mContextLost;

        /** Message handler used by thread to interact with TextView */
        private Handler mHandler;
        private SurfaceHolder mSurfaceHolder;
        private boolean mRun=false;
        private int mScreenMode=0;
        private int mThreadMode=STATE_STOPPED;
        private Misc misc = new Misc();
        @SuppressWarnings("unused")
		private long mLastTime=0;
        @SuppressWarnings("unused")
		private Context mContext=null;

        float mCanvasWidth=0,mCanvasHeight=0;

        public static final int STATE_STOPPED = 0;
        public static final int STATE_PAUSE = 1;
        public static final int STATE_RUNNING = 2;
        
        public static final int MODE_MENU = 2;
        public static final int MODE_CREDITS = 3;
        public static final int MODE_SELECTTEAM = 5;
        public static final int MODE_SELECTDIFF = 6;
        public static final int MODE_RESULTS = 7;
        public static final int MODE_FIXTURES = 8;
        public static final int MODE_TRANSFER = 9;
        public static final int MODE_DISPLAYDIVISIONS = 10;
        public static final int MODE_DIVISIONMP = 11;
        public static final int MODE_WAITFORENTER = 12;
        public static final int MODE_RUNMATCH = 13;
        public static final int MODE_ENDMATCH = 14;
        public static final int MODE_WAITINGFORMATCH = 15;
        public static final int MODE_PREMATCHSTATS = 16;
        public static final int MODE_CHOOSEPLAYERS = 17;
        public static final int MODE_WAITINGFORSQUAD = 18;
        public static final int MODE_SELLPLAYERS = 19;

        private int mNextMode=MODE_MENU;
        
        private fmMenu GameMenu = drv_linux.fm.getGameMenu();
        private MatchDay matchday;
        private Thread MatchThread;
        
        public GameThread(SurfaceHolder surfaceHolder, Context context,
                Handler handler) {

        	mSurfaceHolder = surfaceHolder;
            mHandler = handler;
            mContext = context;
        	setName("fmMainThread");
        }

        public void setNextState(int State) { mNextMode = State; }
        public int getNextState() { return mNextMode; }
        public int getThreadState() { return mThreadMode; }

        /**
         * Pauses the physics update & animation.
         */
        public void onPause() {
            synchronized (mSurfaceHolder) {
            	mPaused=true;
                if (mThreadMode == STATE_RUNNING) setThreadState(STATE_PAUSE);
            }
        }

        public void onResume() {
            synchronized (mSurfaceHolder) {
            	mPaused=false;
                setThreadState(STATE_RUNNING);
            }
        }

        public void surfaceCreated() {
            synchronized(this) {
                mHasSurface = true;
                mContextLost = false;
                notify();
            }
        }

        public void surfaceDestroyed() {
            synchronized(this) {
                mHasSurface = false;
                notify();
            }
        }

        public void onWindowFocusChanged(boolean hasFocus) {
            synchronized (this) {
                mHasFocus = hasFocus;
                if (mHasFocus == true) {
                    notify();
                }
            }
        }

        private boolean needToWait() {
            return (mPaused || (! mHasFocus) || (! mHasSurface) || mContextLost)
                && (mRun);
        }

        public void requestExitAndWait() {
            synchronized(this) {
            	if(matchday!=null) matchday.interrupt();
                mRun = false;
                notify();
            }
            try {
                join();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            Log.d("requestExitAndWait","Exited Join");
        }

        /**
         * Starts the game, setting parameters for the current difficulty.
         */
        public void doStart() {
            synchronized (mSurfaceHolder) {
                mLastTime = System.currentTimeMillis() + 100;
                setThreadState(STATE_RUNNING);
            	RunFootballManager(false);
            }
        }

        /**
         * Pauses the physics update & animation.
         */
        public void pause() {
            synchronized (mSurfaceHolder) {
            	if(MatchThread!=null) {
            		MatchThread.interrupt();
            	}
            	if (mThreadMode==STATE_RUNNING) setThreadState(STATE_PAUSE);
            }
        }

        /**
         * Restores game state from the indicated Bundle. Typically called when
         * the Activity is being restored after having been previously
         * destroyed.
         * 
         * @param savedState Bundle containing the game state
         */
        public synchronized void restoreState(Bundle savedState) {
            synchronized (mSurfaceHolder) {
            	drv_linux.fm.LoadGame(g,true);
            }
        }

        @Override
        public void run() {
            while (mRun) {
            	try {
            		synchronized(this) {
                        if(needToWait()) {
                        	if(matchday!=null) {
                        		matchday.needtowait=true;
                        	}
                            while (needToWait()) {
                            	if(!mRun) return;
                                try {
                                    wait();
                                } catch (InterruptedException e) {
                                    
                                } catch (Exception e) {
                                	Log.d("Run",e.toString());
                                }
                            }
                        } else {
                        	if(matchday!=null) {
                        		matchday.needtowait=false;
                        	}
                        }
            		}
            	} catch (Exception e) {
            		
            	}
            	if(mScreenMode==MODE_CHOOSEPLAYERS) {
            		mScreenMode = MODE_WAITINGFORSQUAD;
            		matchday.SelectTeam(g);
            	} else if(mScreenMode==MODE_ENDMATCH) {
            		matchday.stop();
            		matchday=null;
        			mScreenMode=MODE_MENU;
        			GameMenu.setMenuType(0);
        			GameMenu.MENUMain(g);
            	} else if(mScreenMode==MODE_SELECTTEAM && g.TeamNo!=-1) {
            		ChooseDifficulty();
            	}

            	Canvas c = null;
                try {
                    c = mSurfaceHolder.lockCanvas(null);
                    if(c!=null) {
	                    synchronized (mSurfaceHolder) {
	                        doDraw(c);
	                    }
                    }
                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }

        /**
         * Dump game state to the provided Bundle. Typically called when the
         * Activity is being suspended.
         * 
         * @return Bundle with this view's state
         */
        public Bundle saveState(Bundle map) {
            synchronized (mSurfaceHolder) {
            	drv_linux.fm.SaveGame(g);
            }
            return map;
        }

        /**
         * Sets the current difficulty.
         * 
         * @param difficulty
         */
        public void setDifficulty(int difficulty) {
            synchronized (mSurfaceHolder) {
            }
        }

        /**
         * Used to signal the thread whether it should be running or not.
         * Passing true allows the thread to run; passing false will shut it
         * down if it's already running. Calling start() after this was most
         * recently called with false will result in an immediate shutdown.
         * 
         * @param b true to run, false to shut down
         */
        public void setRunning(boolean b) {
            mRun = b;
        }

        /**
         * Sets the game mode. That is, whether we are running, paused, in the
         * failure state, in the victory state, etc.
         * 
         * @see #setState(int, CharSequence)
         * @param mode one of the STATE_* constants
         */
        public void setThreadState(int mode) {
            synchronized (mSurfaceHolder) {
                setThreadState(mode, null);
            }
        }

        public void setGame(Game newG) {
            synchronized (mSurfaceHolder) {
                mLastTime = System.currentTimeMillis() + 100;
	        	if(newG!=null) { 
	        		InitGame();
	        		g = newG;
	        	}
                setThreadState(STATE_RUNNING);
	        	mScreenMode = MODE_MENU;
	        	GameMenu.MENUMain(g);
            }
        }
                
        public void setScreenState(int mode) {
        	mScreenMode = mode;
        }
        
        public void Initialise() {
        	RunFootballManager(true);
        }
        
        /**
         * Sets the game mode. That is, whether we are running, paused, in the
         * failure state, in the victory state, etc.
         * 
         * @param mode one of the STATE_* constants
         * @param message string to add to screen or null
         */
        public void setThreadState(int mode, CharSequence message) {
            synchronized (mSurfaceHolder) {
                mThreadMode = mode;

                if(drv_linux.fm!=null) {
                	if(drv_linux.fm.myMenu!=null) {
		                if(mode == STATE_PAUSE) {
		        			drv_linux.fm.myMenu.setGroupVisible(1, false);
		        			drv_linux.fm.myMenu.setGroupVisible(2, true);
		
		                } else if(mode == STATE_RUNNING) {
		        			drv_linux.fm.myMenu.setGroupVisible(1, true);
		        			drv_linux.fm.myMenu.setGroupVisible(2, false);                	
		                }
                	}
                }
                
                if (mThreadMode==STATE_RUNNING) {
                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("text", "");
                    b.putInt("viz", View.INVISIBLE);
                    msg.setData(b);
                    mHandler.sendMessage(msg);
                } else {
                    CharSequence str = "";
                    if (message != null) {
                        str = message + "\n" + str;
                    }

                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("text", str.toString());
                    b.putInt("viz", View.VISIBLE);
                    msg.setData(b);
                    mHandler.sendMessage(msg);
                }
            }
        }

        /* Callback invoked when the surface dimensions change. */
        public void setSurfaceSize(int width, int height) {
            // synchronized to make sure these all change atomically
            synchronized (mSurfaceHolder) {
            	boolean firstrun=false;
            	
            	if(drv_linux.mCanvas!=null)
            		firstrun=false;
            	else
            		firstrun=true;
            	
                mCanvasWidth = width;
                mCanvasHeight = height;

                drv_linux.Glo.Depth=16;
                drv_linux.Glo.xSize=width;
                drv_linux.Glo.ySize=height;

                if(firstrun) {
                    CreateBackCanvas((int)mCanvasWidth,(int)mCanvasHeight);
                	drv_linux.fm.LoadGame(new Game(), true);
                } else {
                	// Need to resize the background bitmap and redisplay
                	
	                //Bitmap newBitmap=Bitmap.createScaledBitmap(drv_linux.mBitmap,(int)mCanvasWidth,(int)mCanvasHeight,false);
	                
	                //drv_linux.mBitmap=newBitmap;
	                //drv_linux.mCanvas.setBitmap(newBitmap);
                }
            }
        }

        /**
         * Resumes from a pause.
         */
        public void unpause() {
            // Move the real time clock up to now
            synchronized (mSurfaceHolder) {
                mLastTime = System.currentTimeMillis() + 100;
            }
            setThreadState(STATE_RUNNING);
            if(MatchThread!=null) MatchThread.resume();
        }

        /**
         * Handles a key-down event.
         * 
         * @param keyCode the key that was pressed
         * @param msg the original event object
         * @return true
         */
        boolean doKeyDown(int keyCode, KeyEvent msg) {
        	if(!mRun) { return false; }
        	boolean consume=false;
        	try {
            synchronized (mSurfaceHolder) {
            	if(keyCode==KeyEvent.KEYCODE_BACK) {
            		if(mScreenMode == MODE_MENU || mScreenMode==MODE_CREDITS) {
            			return false;
            		}
            		consume=true;
            	}
            	
                //if (mMode == STATE_CREDITS && keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                //    ChooseTeam();
                //    return true;
            	drv_linux.setKeyPress(keyCode);
                if(mScreenMode == MODE_WAITFORENTER) {
                	if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_BACK) {
                		RunNextMode(mNextMode);
                	}
                } else if(mScreenMode == MODE_SELECTTEAM) {
                	misc.setLastKey(keyCode);
                } else if (mScreenMode == MODE_SELECTDIFF) {
                	if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                		g.Skill--;
                		if(g.Skill<0)
                			g.Skill=0;
                		else
                			GameMenu.MENUSkillLevel(g);
                        return true;
                	} else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                		g.Skill++;
                		if(g.Skill>6)
                			g.Skill=6;
                		else
                			GameMenu.MENUSkillLevel(g);
                        return true;
                	} else if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                		//g.Skill++;
                		if(mNextMode==MODE_FIXTURES) {
                			RunNextMode(mNextMode);
                		} else {
                			drv_linux.fm.enablesaveload();
                			StartNewGame();
                		}
                		return true;
                	} else if(keyCode>KeyEvent.KEYCODE_0 && keyCode<KeyEvent.KEYCODE_7) {
                		g.Skill = keyCode-KeyEvent.KEYCODE_1;
                		GameMenu.MENUSkillLevel(g);
                        return true;
                	}
                } else if(mScreenMode==MODE_MENU) {
                	if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
                		GameMenu.setChoice(g,GameMenu.getChoice()+1);
                	else if(keyCode == KeyEvent.KEYCODE_DPAD_UP)
                		GameMenu.setChoice(g,GameMenu.getChoice()-1);
                	else if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                		DoMenuCommand(GameMenu.getChoice());
                	}
                } else if(mScreenMode==MODE_DISPLAYDIVISIONS) {
                	if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
                		mScreenMode = MODE_MENU;
                } else if(mScreenMode==MODE_DIVISIONMP) {
                } else if(mScreenMode==MODE_FIXTURES) {
                	if(keyCode==KeyEvent.KEYCODE_DPAD_CENTER) {
                		mScreenMode=MODE_MENU;
                	}
                }
                return consume;
            }
        	} catch(Exception e) {
        		System.out.print(e.toString());
        		return consume;
        	}
        }

        void RunNextMode(int Mode) {
        	if(!mRun) { return; }
        	switch(Mode) {
        		case MODE_CREDITS:
        			ChooseTeam(); break;
        		case MODE_FIXTURES: {
        			mScreenMode=MODE_MENU;
        			GameMenu.MENUMain(g);
        			break;
        		}
        		default: mScreenMode=Mode;break;
        	}
        }
        
        void DoMenuCommand(int Command) {
        	if(!mRun) { return; }
        	switch(Command) {
		        case 0: {
		        	mScreenMode=MODE_FIXTURES;
		        	Init.INITFixtureList(g);
		        	mScreenMode=MODE_WAITFORENTER;
		        	mNextMode=MODE_FIXTURES;
		        	break;
		        }
		        case 1: {
		        	mScreenMode=MODE_DISPLAYDIVISIONS;
		        	drv_linux.fm.getGameMenu().INITDisplayDivision(g);
		        	break;
		        }
		        case 2: {
				    GameMenu._MENUSellList(g);				    
		        	mScreenMode = MODE_SELLPLAYERS;
		        	break;
		        }
		        case 3: {
		        	GameMenu._MENUScore(g);
		        	mScreenMode = MODE_WAITFORENTER;
		        	mNextMode = MODE_FIXTURES;
		        	break;
		        }
		        case 4: GameMenu._MENUObtainLoan(g);break;
		        case 5: GameMenu._MENURepayLoan(g);break;
		        case 6: {
		        	mNextMode = MODE_FIXTURES;
		        	mScreenMode = MODE_SELECTDIFF;
		        	GameMenu.MENUSkillLevel(g);
		        	break;
		        }
		        case 7: {
		        	drv_linux.fm.SaveGame(g);
		        	break;
		        }
		        case 8: g.Sound = !g.Sound;break;
		        case 9: {
        			mScreenMode=MODE_WAITINGFORMATCH;
        	        matchday = new MatchDay();
        	        MatchThread = new Thread(matchday);
        	        
        			GameMenu.clearChoice();
        	        MatchThread.start();
    				mScreenMode=MODE_RUNMATCH;
                    matchday.MDRun(g);                                // Play one game 
                    break;
		        }
		        case 10: {
	                AlertDialog.Builder alert1 = new AlertDialog.Builder(drv_linux.fm)
	                .setTitle("Restart Game")
	                .setMessage("Are you sure you want to restart?")
	                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	                        public void onClick(DialogInterface dialog, int whichButton) {
	                        	RunFootballManager(false);
	                        }
	                })
	                .setNegativeButton("No", new DialogInterface.OnClickListener() {
	                        public void onClick(DialogInterface dialog, int whichButton) {
	                                
	                        }
	                });	
	                
	                alert1.show();
	                break;
		        }
        	}
        }
        /**
         * Handles a key-up event.
         * 
         * @param keyCode the key that was pressed
         * @param msg the original event object
         * @return true if the key was handled and consumed, or else false
         */
        boolean doKeyUp(int keyCode, KeyEvent msg) {
            boolean handled = false;

            synchronized (mSurfaceHolder) {
            }

            return handled;
        }
        
        boolean TouchEvent(MotionEvent event) {
            //float x = event.getX();
            //float y = event.getY();
        	if(!mRun) { return false; }
            
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    //touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    //	doKeyDown(KeyEvent.KEYCODE_DPAD_CENTER,null);
                    if(mScreenMode==MODE_WAITFORENTER) {
                    	doKeyDown(KeyEvent.KEYCODE_DPAD_CENTER,null);
                    } else {
                    	drv_linux.touchX = event.getX();
                    	drv_linux.touchY = event.getY();
                    	drv_linux.setKeyPress(-1);
                    }
                    invalidate();
                    break;
            }

            return true;
        }

        private void doDraw(Canvas canvas) {
        	canvas.save();
        	if(drv_linux.mCanvas!=null) {
        		canvas.drawBitmap(drv_linux.mBitmap, 0, 0, null);
        	}
        	canvas.restore();
        }

        private void CreateBackCanvas(int width, int height) {
    		drv_linux.mBitmap = Bitmap.createBitmap(width,height, Bitmap.Config.RGB_565);
    		drv_linux.mCanvas = new Canvas();
    		drv_linux.mCanvas.setBitmap(drv_linux.mBitmap);
        }
        
        Game g;                                           /* The entire game is here ! */

        // static void FixFixtureList(GAME *g);

        /************************************************************************/
        /*																		*/
        /*								Main Program							*/
        /*																		*/
        /************************************************************************/

        int RunFootballManager(boolean initialising)
        {
            //FILE *f;
            g = new Game();
            io.IOInitialise();									/* Initialise driver */
            try {
    			Init.INITNewGame(g);
    		} catch (RuntimeException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}                              						/* Load game.dat etc. */
    		if(!initialising) {
	    		mScreenMode=MODE_CREDITS;
	            //misc.MISCTitle();									/* Display title page */
	            mScreenMode=MODE_SELECTTEAM;
	            //mNextMode=MODE_CREDITS;
	            ChooseTeam();
    		}
            return 0;
        }
        
        public Game InitGameObject(Game newG) {
        	try {
        		if(newG==null) { newG=new Game(); }
				newG=Init.INITNewGame(newG);
			} catch (RuntimeException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return newG;
        }

        public Game SaveGame() {
        	return g;
        }
        
        public void InitGame() {
        	RunFootballManager(true);
        }
        
        public Game InitGame(Game newG) {
        	g = newG;
			try {
				Init.INITNewGame(g);
			} catch (RuntimeException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return g;
        }
        
        public void LoadGame(Bundle map) {
        	restoreState(map);
        }
        
        void ChooseTeam() {
        	g.TeamNo=-1;
        	mScreenMode = MODE_SELECTTEAM;
        	//misc.setChosen_Team(0);
        	misc.MISCSelectTeam(g);
        }

        void ChooseDifficulty() {
        	g.Skill=0;
        	mScreenMode = MODE_SELECTDIFF;
        	GameMenu.MENUSkillLevel(g);
        }
        
        void StartNewGame() {
        	Init.INITNewSeason(g);
        	mScreenMode = MODE_MENU;
        	GameMenu.MENUMain(g);
        }        
}

    /** Handle to the application context, used to e.g. fetch Drawables. */
    //private Context mContext;

    /** Pointer to the text view to display "Paused.." etc. */
    //private TextView mStatusText;

    /** The thread that actually draws the animation */
    private GameThread gthread;
    private TextView mStatusText;
    private boolean hasSurface=false;
    private Context mContext;
    private SurfaceHolder holder;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // register our interest in hearing about changes to our surface
        holder = getHolder();
        holder.addCallback(this);
        mContext=context;

        createthread(holder);

        setFocusable(true); // make sure we get key events
	    setFocusableInTouchMode(true);
    }

    private void createthread(SurfaceHolder holder) {
    // create thread only; it's started in surfaceCreated()
	    gthread = new GameThread(holder, mContext, new Handler() {
	        @Override
	        public void handleMessage(Message m) {
	            mStatusText.setVisibility(m.getData().getInt("viz"));
	            mStatusText.setText(m.getData().getString("text"));
	        }
	    });
    }

    /**
     * Fetches the animation thread corresponding to this LunarView.
     * 
     * @return the animation thread
     */
    public GameThread getThread() {
        return gthread;
    }

    /**
     * Standard override to get key-press events.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {
        return gthread.doKeyDown(keyCode, msg);
    }

    /**
     * Standard override for key-up. We actually care about these, so we can
     * turn off the engine or stop rotating.
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent msg) {
        return gthread.doKeyUp(keyCode, msg);
    }

    /**
     * Standard window-focus override. Notice focus lost so we can pause on
     * focus lost. e.g. user switches to take a call.
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
    	if(gthread!=null) {
    		gthread.onWindowFocusChanged(hasWindowFocus);
    	}
    }

    public void onResume() {
		if(gthread==null) {
			createthread(holder);
			//gthread.setThreadState(GameThread.STATE_STOPPED);
			if(hasSurface) gthread.start();
			drv_linux.fm.myMenu.setGroupVisible(1, false);
			drv_linux.fm.myMenu.setGroupVisible(2, true);
			boolean success=false;
			Game g=new Game();
			
			success=drv_linux.fm.LoadGame(g, true);
			if(success) {
				gthread.setGame(g);
			}
		}
		if(gthread!=null) gthread.onResume();
    }

    protected void stopDrawing() {
        gthread.requestExitAndWait();
        gthread=null;
    }

    /**
     * Installs a pointer to the text view used for messages.
     */
    public void setTextView(TextView textView) {
        mStatusText = textView;
    }

    /* Callback invoked when the surface dimensions change. */
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        if(gthread!=null) gthread.setSurfaceSize(width, height);
    }

    /*
     * Callback invoked when the Surface has been created and is ready to be
     * used.
     */
    public void surfaceCreated(SurfaceHolder holder) {
    	if(gthread!=null) {
    		gthread.surfaceCreated();
	    	hasSurface=true;
	    	gthread.setRunning(true);
	    	try {
	    		gthread.start();
	    	} catch (Exception e) {
	    	}
    	}
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gthread.TouchEvent(event);
    }
    /*
     * Callback invoked when the Surface has been destroyed and must no longer
     * be touched. WARNING: after this method returns, the Surface/Canvas must
     * never be touched again!
     */
    public void pause() {
    	gthread.onPause();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
    	if(gthread!=null) gthread.surfaceDestroyed();
    	hasSurface=false;
    	pause();
    }
}
