package com.rgsoftworks.fm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.rgsoftworks.fm.GameView.GameThread;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class footballmanager extends Activity {
    private static final int MENU_PAUSE = 4;
    private static final int MENU_RESUME = 5;
    private static final int MENU_START = 6;
    private static final int MENU_STOP = 7;
    private static final int MENU_LOAD = 8;
	GameView gview;
	private GameThread mGameThread;
	private fmMenu GameMenu = new fmMenu();
	
    public GameThread getGameThread() { return mGameThread; }
    public fmMenu getGameMenu() { return GameMenu; }
    public GameView getGameView() { return gview; }
    public Menu myMenu=null;
    
    public void enablesaveload() {
    	if(myMenu!=null) {
    		myMenu.setGroupEnabled(1, true);
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, MENU_START, 0, "START");
        menu.add(1, MENU_PAUSE, 0, "PAUSE");
        menu.add(2, MENU_RESUME, 0, "RESUME");
        menu.add(0, MENU_LOAD, 0, "LOAD");

        menu.setGroupVisible(2, false);
        
        myMenu=menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_START:
            	ImageView im=(ImageView)findViewById(R.id.attractImage);
            	
            	im.setBackgroundDrawable(null);
                mGameThread.doStart();
                return true;
            case MENU_STOP:
                mGameThread.setThreadState(GameThread.STATE_PAUSE,
                        "STOPPED");
                return true;
            case MENU_PAUSE:
                myMenu.setGroupVisible(2, true);
                myMenu.setGroupVisible(1, false);
                mGameThread.pause();
                return true;
            case MENU_RESUME:
                myMenu.setGroupVisible(1, true);
                myMenu.setGroupVisible(2, false);
                mGameThread.unpause();
                return true;
            case MENU_LOAD:
            {
            	Game g=null;
            	boolean success=false;

            	success=LoadGame(g,false);
                return success;
            }
        }

        return false;
    }

    public boolean LoadGame(Game g, boolean Auto) {
    	boolean success=false;
    	
    	try {
    		g=mGameThread.InitGameObject(g);
			g=LoadBinaryFile("savegame.fm",g);
			if(g!=null) { success=true; }
		} catch (IOException e) {
			e.printStackTrace();
			success=false;
		}

    	if(!success) {
    		if(!Auto) {
	            Toast.makeText(footballmanager.this, "Load failed.",
	                    Toast.LENGTH_SHORT).show();
    		}
            return true;
    	} else {
    		if(!Auto) {
	            Toast.makeText(footballmanager.this, "Game Loaded.",
	                    Toast.LENGTH_SHORT).show();            		
    		}
    	}
    	mGameThread.setGame(g);
    	
    	return success;
    }
    
    public void SaveGame(Game g) {
        try {
			SaveBinaryFile("savegame.fm",g);
		} catch (IOException e) {
			e.printStackTrace();
		} 
    }
    
    /** Called when the activity is first created. */
    @Override    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // turn off the window's title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        drv_linux.fm=this;
        //gview = new GameView(this, null);
        setContentView(R.layout.main);

        gview = (GameView) findViewById(R.id.game);

    	ImageView im=(ImageView)findViewById(R.id.attractImage);    	
    	im.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.fmcredit));
        
        // give the LunarView a handle to the TextView used for messages
        gview.setTextView((TextView) findViewById(R.id.text));
        mGameThread = gview.getThread();
        if (savedInstanceState != null) {
            //mGameThread.restoreState(savedInstanceState);
        	Game g=null;
        	boolean success=false;

        	success=LoadGame(g,true);
        	if(success) {
	        	mGameThread.setGame(g);
        	}
	        mGameThread.setThreadState(GameThread.STATE_PAUSE);
        	myMenu.setGroupVisible(1, false);
        	myMenu.setGroupVisible(2, true);
        } else {        	
        }

        //mGameThread.doStart();
        //setContentView(gview);
   }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gview.stopDrawing();        
    }

    @Override
    protected void onResume() {
    	super.onResume();
    	gview.onResume();
    }
    
    /**
     * Invoked when the Activity loses user focus.
     */
    @Override
    protected void onPause() {
    	super.onPause();
        gview.pause(); // pause game when Activity pauses
    }

   public FileInputStream OpenFile(String Filename) {
	   try {
		return openFileInput(Filename);
	} catch (FileNotFoundException e) {
		e.printStackTrace();
		return null;
	}
   }

   public void SaveBinaryFile(String Filename, Game g) throws IOException {
	   try {
		OutputStream os=openFileOutput(Filename,MODE_WORLD_READABLE);
		g.writeToOutputStream(os);
		os.close();
        Toast.makeText(footballmanager.this, "Game saved.",
                Toast.LENGTH_SHORT).show();
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	}
   }

   public Game LoadBinaryFile(String Filename,Game g) throws IOException {
	   boolean success=false;
	   
	   try {
		InputStream os=openFileInput(Filename);
		if(g==null) {
			g=new Game();
			g=mGameThread.InitGame(g);
		}
		g.readFromInputStream(os);
		os.close();
        Toast.makeText(footballmanager.this, "Game Loaded.",
                Toast.LENGTH_SHORT).show();
        success=true;
	} catch (FileNotFoundException e) {
		e.printStackTrace();
		success=false;
	}
	
	if(success) {
		return g;
	} else {
		return null;
	}
   }

   public byte[] OpenAsset(String Filename) {
	   try {
		InputStream is=getAssets().open(Filename);
        int size = is.available();
		byte b[] = new byte[size];

		is.read(b);
		
		return b;
	} catch (IOException e) {
		e.printStackTrace();
		return null;
	}
   }
   
   public void SaveLocalFile(String Filename, byte data[]) {
	   OutputStream fs;
	   
	   try {
		fs = openFileOutput(Filename,MODE_WORLD_READABLE);
		fs.write(data);
        Toast.makeText(footballmanager.this, "Game Saved",
                Toast.LENGTH_SHORT).show();
	} catch (FileNotFoundException e) {
        Toast.makeText(footballmanager.this, "Save failed.",
                Toast.LENGTH_SHORT).show();
		e.printStackTrace();
	} catch (IOException e) {
        Toast.makeText(footballmanager.this, "Save failed.",
                Toast.LENGTH_SHORT).show();
		e.printStackTrace();
	}
   }

   @Override
   protected void onSaveInstanceState(Bundle outState) {
       // just have the View's thread save its state into our Bundle
       super.onSaveInstanceState(outState);
       mGameThread.saveState(outState);
       Log.w(this.getClass().getName(), "SIS called");
   }
 }
