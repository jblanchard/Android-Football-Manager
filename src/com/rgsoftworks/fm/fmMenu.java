package com.rgsoftworks.fm;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.SystemClock;
import android.view.KeyEvent;

import com.rgsoftworks.fm.io;
import com.rgsoftworks.fm.GameView.GameThread;

public class fmMenu {
	/************************************************************************/
	/*																		*/
	/*								Main menu								*/
	/*																		*/
	/************************************************************************/
	private Game ThisGame;
	
	private int mCurrentChoice=0;
	private int mMaxChoice=0;
	private int mMinChoice=0;
	private int mMenuType=0;
	private ArrayList<MenuArray> MenuItems;
	
	private final int MENU_MAIN=0;
	private final int MENU_SELECTSQUAD=1;
	//private final int MENU_SELLPLAYERS=2;
	//private final int MENU_BUYPLAYERS=3;

	public void setMaxChoice(int MaxChoice) { mMaxChoice=MaxChoice; }
	public int getMaxChoice() { return mMaxChoice; }
	public void setMenuType(int MenuType) { mMenuType = MenuType; }
	public void clearChoice() { mCurrentChoice=0; }
	
	public ArrayList<MenuArray> getMenuList() { return MenuItems; }
	public void setMenuList(ArrayList<MenuArray> NewList) { MenuItems=NewList; }
	
	public boolean setChoice(Game g,int choice) { 
		int oldChoice=mCurrentChoice;
		boolean returnVal=true;
		
		mCurrentChoice=choice;
		if(mCurrentChoice>mMaxChoice) {
			mCurrentChoice = mMaxChoice;
			return false;
		}
		if(mCurrentChoice<mMinChoice) {
			mCurrentChoice = mMinChoice;
			return false;
		}

		if(mMenuType==MENU_MAIN) {
			_MENURefresh(g,oldChoice,false);
			_MENURefresh(g,mCurrentChoice,true);
			return false;
		} else if(mMenuType==MENU_SELECTSQUAD) {
			if(oldChoice!=mCurrentChoice) MENUPlayer(g,MenuItems.get(oldChoice).index,8+(oldChoice*8));
			MENUPlayer(g,MenuItems.get(mCurrentChoice).index,8+(mCurrentChoice*8));
		}

		return returnVal;
	}
	public int getChoice() { return mCurrentChoice; }
	
	public boolean MENUMain(Game g)
	{
	    int i;

	    mMaxChoice=10;
        io.IOClear(Constants.COL_BLACK);
        io.IOText(-1,8,Constants.COL_YELLOW,Constants.COL_RED," Main Menu ");
        io.IOText(-1,158,Constants.COL_RED,Constants.COL_BLACK,"Football Manager by Paul Robson");
        io.IOText(-1,168,Constants.COL_RED,Constants.COL_BLACK,"Original version by Kevin Toms");
        io.IOText(-1,178,Constants.COL_RED,Constants.COL_BLACK,"Android port by Jonn Blanchard");
        for (i = 0;i < 256;i++)
        {
            io.IOPut(i,0,Constants.COL_YELLOW);io.IOPut(i,191,Constants.COL_YELLOW);
            if (i < 192) {
                { io.IOPut(0,i,Constants.COL_YELLOW);io.IOPut(255,i,Constants.COL_YELLOW); }
            }
        }

        for (i = 0;i < 11;i++) 
            if(mCurrentChoice==i) {
                _MENURefresh(g,i,true);
            } else {
                _MENURefresh(g,i,false);
            }
	    return false; //(i == 7);
	}

	/************************************************************************/
	/*																		*/
	/*				Refresh a menu item, optional highlight					*/
	/*																		*/
	/************************************************************************/

	public void _MENURefresh(Game g,int n,boolean Highlight)
	{
	    String Msg,_Temp;
	    switch(n)
	    {
	        case 0:
	            Msg = "View Fixture List";break;
	        case 1:
	            Msg = "Display Division";break;
	        case 2:
	            Msg = "Sell or List Players";break;
	        case 3:
	            Msg = "Print Score";break;
	        case 4:
	            Msg = "Obtain a Loan";break;
	        case 5:
	            Msg = "Pay off a Loan";break;
	        case 6:
	            Msg = "Change Skill Level";break;
	        case 7:
	            Msg = "Save Game";break;
	        case 8:
	            Msg = "Toggle Sound (now on) ";
	            if (!g.Sound) {
	                Msg = "Toggle Sound (now off)";
	            }
	            break;
	        case 9:
	            Msg = "Continue Game";break;
	        case 10:
	        	Msg = "Restart Game";break;
	        default:
	            return;
	    }
	    _Temp=Integer.toString(n) + " - " + Msg;
	    io.IOText(40,n*12+26,Highlight ? Constants.COL_WHITE:Constants.COL_GREEN,Constants.COL_BLACK,_Temp);
	}

	/************************************************************************/
	/*																		*/
	/*					Sell/List your players section						*/
	/*																		*/
	/************************************************************************/

	public void _MENUSellList(Game g) {
		ThisGame=g;
		Thread mt = new Thread(null,MENUSellListThread,"Background");
		mt.start();
	}

	private Runnable MENUSellListThread = new Runnable() {
		public void run() {
			MENUSellList(ThisGame);
		}
	};

	private void MENUSellList(Game g)
	{
	    int d,i = 0,y=0,Team;
	    long Bid;
	    String _Msg;
	    ArrayList<MenuArray> Players = new ArrayList<MenuArray>();
	    int choice=0;
	    boolean finish=false;
	    int playcount=0;
	    Player p;
	    
	    drv_linux.mKeyPress=0;
	    do
	    {
	        io.IOClear(Constants.COL_BLACK);
	        io.IOText(-1,4,Constants.COL_RED,Constants.COL_YELLOW," Sell or List Players ");
	        finish=false;
	        y = 24; y= MENUPlayer(g,-1,y);
	        playcount=0;
	        for (i = 0; i < g.PlayerCount;i++)
	            if (g.Player[i].InOurTeam) {
	                MenuArray ma = new MenuArray();
	                ma.Y=y;
	                y=MENUPlayer(g,i,y,choice==playcount);
	                
	                ma.index=i;
	                Players.add(ma);
	                playcount++;
	            }
	        
            io.IOText(0,170,Constants.COL_CYAN,Constants.COL_BLACK,"Center to Select Back to Cancel");
	        int oldchoice=0;
	        while(!finish) {
	            //long lasttime=io.IOClock();
	            //while(io.IOClock()-lasttime<500) {}
	            int butt=io.IOJoy();
	            if(butt==Constants.VK_UP) choice--;
	            if(butt==Constants.VK_DOWN) choice++;
	            if(choice<0) choice=0;
	            if(choice>playcount-1) choice=playcount-1;
	            if(butt==KeyEvent.KEYCODE_BACK) {
	                i=-1;
	                finish=true;
	            }
	            if(butt==Constants.VK_FX) {
	            	i=Players.get(choice).index;
	                finish=true;
	            }
	            if(choice!=oldchoice) {
	            	MENUPlayer(g,Players.get(oldchoice).index,Players.get(oldchoice).Y,false);
	            	MENUPlayer(g,Players.get(choice).index,Players.get(choice).Y,true);
	            	oldchoice=choice;
	            }
	        }
	        
	        if (i >= 0 && i < g.PlayerCount) {
	            if (g.Player[i].InOurTeam)
	        {
	            io.IOClear(Constants.COL_BLACK);
	            p = (g.Player[i]);
	            if (p.Status == Constants.INJURED)
	            {
	                io.IOText(-1,16,Constants.COL_MAGENTA,Constants.COL_BLACK,"He is injured-nobody wants him");
	                Init.INITPressEnter();
	            }
	            else
	            {
	                Bid = ((long)(Math.floor(Math.random()*5)+8))*(p.Value)/10;
	                d = (int)Math.floor(Math.random()*(g.DivCount));
	                do
	                Team = (int)Math.floor(Math.random()*(g.Division[d].NoTeams));
	                while (Team == g.Division[d].Team);
	                _Msg=g.Division[d].Teams[Team].Name+" have bid " + (char)(96)+ Double.toString(Bid) + " for";
	                io.IOText(-1,24,Constants.COL_YELLOW,Constants.COL_BLACK,_Msg);
	                io.IOText(-1,36,Constants.COL_YELLOW,Constants.COL_BLACK,g.Player[i].Name);
	                _Msg="He is worth " + (char)96 + Double.toString(p.Value);
	                io.IOText(-1,64,Constants.COL_CYAN,Constants.COL_BLACK,_Msg);
	                io.IOText(-1,80,Constants.COL_MAGENTA,Constants.COL_BLACK,"Do you accept ?");
	                if (MENUYesNo())
	                {
	                    g.Player[i].InOurTeam = false;
	                    g.Cash += Bid;
	                    _Msg="Player " + g.Player[i].Name +"has been sold";
	                    io.IOText(-1,112,Constants.COL_YELLOW,Constants.COL_BLACK,_Msg);
	                    Init.INITPressEnter();
	                }
	                else
	                {
	                    if (Math.floor(Math.random()*3) == 0) {
	                        g.Player[i].Status = Constants.INJURED;
	                    }
	                }
	            }
	        }
	        }
	    } while (i >= 0);
	    drv_linux.fm.getGameThread().setScreenState(GameThread.MODE_MENU);
	    mMenuType=MENU_MAIN;
	    MENUMain(g);
	}

	public void SellPlayer(Game g,int i) {
		if (i >= 0 && i < g.PlayerCount) {
            if (g.Player[i].InOurTeam)
            {
	            io.IOClear(Constants.COL_BLACK);
	            Player p = (g.Player[i]);
	            if (p.Status == Constants.INJURED)
	            {
	            	io.OKDialog("Sell Player", "He is injured - nobody wants him", "OK", new OnClickListener() {
			          public void onClick(DialogInterface dialog, int which) {
			          }});
	            }
	            else
	            {
	            	long Bid;
	            	int d,Team;
	            	String _Msg;
	            	
	                Bid = ((long)(Math.floor(Math.random()*5)+8))*(p.Value)/10;
	                d = (int)Math.floor(Math.random()*g.DivCount);
	                do
	                Team = (int)Math.floor(Math.random()*g.Division[d].NoTeams);
	                while (Team == g.Division[d].Team);
	                _Msg=g.Division[d].Teams[Team].Name + " have bid " + (char)96 + Double.toString(Bid) +" for";
	                io.IOText(-1,24,Constants.COL_YELLOW,Constants.COL_BLACK,_Msg);
	                io.IOText(-1,36,Constants.COL_YELLOW,Constants.COL_BLACK,g.Player[i].Name);
	                _Msg="He is worth " + (char)96 + Double.toString(p.Value);
	                io.IOText(-1,64,Constants.COL_CYAN,Constants.COL_BLACK,_Msg);
	                io.IOText(-1,80,Constants.COL_MAGENTA,Constants.COL_BLACK,"Do you accept ?");
	                if (MENUYesNo())
	                {
	                    g.Player[i].InOurTeam = false;
	                    g.Cash += Bid;
	                    _Msg="Player " + g.Player[i].Name +" has been sold";
	                    io.IOText(-1,112,Constants.COL_YELLOW,Constants.COL_BLACK,_Msg);
	                    io.OKDialog("Sell Player", _Msg, "OK", new OnClickListener() {
				          public void onClick(DialogInterface dialog, int which) {
				          }});
	                    Init.INITPressEnter();
	                }
	                else
	                {
	                    if (Math.floor(Math.random()*3) == 0) {
	                        g.Player[i].Status = Constants.INJURED;
	                    }
	                }
	            }
            }
		}
	}

	/************************************************************************/
	/*																		*/
	/*							Draw player entry, or header				*/
	/*																		*/
	/************************************************************************/

	public int MENUPlayer(Game g,int n,int y,boolean hl)
	{
	    Player p;
	    int Col,Ch;
	    String _Temp;
	    if (n == -1)
	    {
	        io.IOText(0,y,Constants.COL_BLACK,Constants.COL_YELLOW,"  Name     No Skl  Eng  Value   ");
	        y += 8;return y;
	    }
	    if (n == -2)
	    {
	        _Temp="Avail: " + Integer.toString(g.Available) + " Picked " + Integer.toString(g.Picked) + " Injured " + g.Injured;
	        io.IOText(0,y,Constants.COL_GREEN,Constants.COL_BLACK,_Temp);
	        y += 8;return y;
	    }

	    p = (g.Player[n]);
	    Col = Constants.COL_WHITE;Ch = 'D';
	    if (p.Pos == Constants.MIDFIELD) { Col = Constants.COL_BLUE;Ch = 'M'; };
	    if (p.Pos == Constants.ATTACK) { Col = Constants.COL_RED;Ch = 'A';};
	    
	    int BackCol = Constants.COL_BLACK;
	    if(hl) {
	    	Col = Constants.COL_YELLOW;
	    	BackCol = Constants.COL_DKGREEN;
	    }
	    io.IOChar(0,y,Col,BackCol,Ch);
	    io.IOText(16,y,Col,BackCol,p.Name);
	    //_Temp= Integer.toString(n+1) + Integer.toString(p.Skill) + Integer.toString(p.Energy) + (char)96 + Long.toString(p.Value);
	    _Temp= FormatNumber(n+1,"00  ") + FormatNumber(p.Skill,"00   ") + FormatNumber(p.Energy,"00  ") + (char)96 + Long.toString(p.Value);
	    io.IOText(88,y,Col,BackCol,_Temp);
	    if (p.InOurTeam)
	    {
	        switch(p.Status)
	        {
	            case Constants.AVAILABLE: io.IOChar(248,y,Constants.COL_BLACK,Constants.COL_YELLOW,' ');break;
	            case Constants.PICKED:    io.IOChar(248,y,Constants.COL_BLACK,Constants.COL_MAGENTA,'p');break;
	            case Constants.INJURED:   io.IOChar(248,y,Constants.COL_BLACK,Constants.COL_GREEN,'i');break;
	        }
	    }
	    y += 8;
	    return y;
	}

	public int MENUPlayer(Game g,int n,int y)
	{
	    Player p;
	    int Col,Ch,BCol;
	    String _Temp;
	    mMenuType=MENU_SELECTSQUAD;
	    if (n == -1)
	    {
	        io.IOText(0,y,Constants.COL_BLACK,Constants.COL_YELLOW,"  Name     No Skl  Eng  Value   ");
	        y += 8;return y;
	    } else if (n == -2)
	    {
	        _Temp="Available " + Integer.toString(g.Available) + " Picked " + Integer.toString(g.Picked) + " Injured " + Integer.toString(g.Injured);
	        io.IOText(0,y,Constants.COL_GREEN,Constants.COL_BLACK,_Temp);
	        y += 8;return y;
	    } else if (n == -3)
	    {
	    	
	    }
	    
	    p = (g.Player[n]);
	    BCol = Constants.COL_BLACK;
	    Col = Constants.COL_WHITE;Ch = 'D';
	    if (p.Pos == Constants.MIDFIELD) { Col = Constants.COL_BLUE;Ch = 'M';};
	    if (p.Pos == Constants.ATTACK) { Col = Constants.COL_RED;Ch = 'A';};
	    if(mCurrentChoice==n) {
	    	Col = Constants.COL_YELLOW;
	    	BCol = Constants.COL_DKGREEN;
	    }
	    io.IOChar(0,y,BCol,Col,Ch);
	    io.IOText(16,y,Col,BCol,p.Name);
	    _Temp= FormatNumber(n+1,"00  ") + FormatNumber(p.Skill,"00   ") + FormatNumber(p.Energy,"00  ") + (char)96 + Long.toString(p.Value);
	    io.IOText(88,y,Col,BCol,_Temp);
	    if (p.InOurTeam)
	    {
	        switch(p.Status)
	        {
	            case Constants.AVAILABLE: io.IOChar(248,y,BCol,Constants.COL_YELLOW,' ');break;
	            case Constants.PICKED:    io.IOChar(248,y,BCol,Constants.COL_MAGENTA,'p');break;
	            case Constants.INJURED:   io.IOChar(248,y,BCol,Constants.COL_GREEN,'i');break;
	        }
	    }
	    y += 8;
	    return y;
	}

	private String FormatNumber(int Number, String format) {
		DecimalFormat formatter = new DecimalFormat(format);
		
		return formatter.format(Number);
	}
	
	/************************************************************************/
	/*																		*/
	/*							 Input an integer							*/
	/*																		*/
	/************************************************************************/

	public long MENUGetInt(int x,int y,int Size)
	{
	    boolean finish=false;
	    char num[]= new char[Size];
	    int choice=Size-1;
	    int i,n = 0;
	    
         long lasttime=SystemClock.elapsedRealtime();
         {
               while(SystemClock.elapsedRealtime()-lasttime<500) {}
         }
    
        for(i=0;i<Size;i++) num[i]='0';

        lasttime=0;
        int thischanged=-2;
        int lastchanged=-1;
        while(!finish) {
        	 if(thischanged==-2) {
	             for(i=0;i<Size;i++) {
	                 if(choice==i)
	                      io.IOChar(x+(i*8),y,Constants.COL_BLACK,Constants.COL_YELLOW,num[i]);
	                 else
	                      io.IOChar(x+(i*8),y,Constants.COL_YELLOW,Constants.COL_BLACK,num[i]);
	             }
	             thischanged=-1;
	             lastchanged=-1;
        	 } else if(thischanged!=-1){
                 io.IOChar(x+(thischanged*8),y,Constants.COL_BLACK,Constants.COL_YELLOW,num[thischanged]);
                 if(lastchanged!=-1) io.IOChar(x+(lastchanged*8),y,Constants.COL_YELLOW,Constants.COL_BLACK,num[lastchanged]);
                 thischanged=-1;
                 lastchanged=-1;
        	 }
        	 
             if(SystemClock.elapsedRealtime()-lasttime>50) {
                  int butt=io.IOJoy();
                  if(butt==Constants.VK_LEFT) {
                	  lastchanged=choice;
                	  choice--;
                	  thischanged=choice;
                  }
                  if(butt==Constants.VK_RIGHT) {
                	  lastchanged=choice;
                	  choice++;
                	  thischanged=choice;
                  }
                  if(choice<0) choice=0;
                  if(choice>=Size) choice=Size-1;
                  if(butt==Constants.VK_UP) {
                	  num[choice]++;
                	  thischanged=choice;
                  }
                  if(butt==Constants.VK_DOWN) {
                	  num[choice]--;
                	  thischanged=choice;
                  }
                  if(num[choice]<'0') num[choice]='9';
                  if(num[choice]>'9') num[choice]='0';
                  if(butt==Constants.VK_FX) {
                       n=Integer.parseInt(new String(num));
                       finish=true;
                  }
                  if(butt==Constants.VK_FA) {
                        n=0;
                        finish=true;
                  }
                  lasttime=SystemClock.elapsedRealtime();
             }
        }
	    return n;
	}

	/************************************************************************/
	/*																		*/
	/*						Display the league table						*/
	/*																		*/
	/**
	 * @return **********************************************************************/
	public void INITDisplayDivision(Game g) {
		ThisGame=g;
		Thread thread = new Thread(null,INITDisplayDivisionThread,"Background");
		thread.start();
	}

	private Runnable INITDisplayDivisionThread = new Runnable() {
		public void run() {
			_INITDisplayDivision(ThisGame);
			drv_linux.fm.getGameThread().setScreenState(GameThread.MODE_MENU);
			MENUMain(ThisGame);
		}
	};

	public void _INITDisplayDivision(Game g)
	{
	    int i,n,y = 0;
		boolean TwoPage;
	    String _Temp;
	    Division d = (g.Division[g.Div]);
	    TeamInfo t;
	    TwoPage = (d.NoTeams > 17);
	    Init.INITGenerateSortArray(g);
	    for (i = 0;i < d.NoTeams;i++)
	    {
	        if (i == 0 || (TwoPage && i == d.NoTeams/2))
	        {
	            y = 192/2-4*(TwoPage ? d.NoTeams/2:d.NoTeams)-8;
	            if (i != 0) { Init.INITPressEnter(); }
	            io.IOClear(Constants.COL_BLACK);
	            _Temp=" " + g.Division[g.Div].DivName + " ";
	            io.IOText(-1,4,Constants.COL_YELLOW,Constants.COL_RED,_Temp);
	            io.IOText(84,y,Constants.COL_WHITE,Constants.COL_BLACK,"P  W  D  L  F  A");
	            io.IOText(232,y,Constants.COL_WHITE,Constants.COL_BLACK,"Pts");
	            y+=8;
	        }
	        n = d.SortIndex[i];
	        t = (d.Teams[n]);
	        io.IOText(0,y,(n == d.Team) ? Constants.COL_YELLOW:Constants.COL_RED,Constants.COL_BLACK,d.Teams[n].Name);
	        String Tot=Integer.toString(t.Won+t.Drawn+t.Lost),Won=Integer.toString(t.Won);
	        String Drawn=Integer.toString(t.Drawn),Lost=Integer.toString(t.Lost);
	        String GF=Integer.toString(t.GoalsFor), GA=Integer.toString(t.GoalsAgainst);

	        if(Tot.length()<2) Tot+=" ";
	        if(Won.length()<2) Won+=" ";
	        if(Drawn.length()<2) Drawn+=" ";
	        if(Lost.length()<2) Lost+=" ";
	        if(GF.length()<2) GF+=" ";
	        if(GA.length()<2) GA+=" ";
	        
	        _Temp=Tot+" "+Won+" "+Drawn+" "+Lost+" "+GF+" "+GA;
	        //_Temp=+" "++" "++" "++" "+Integer.toString(t.GoalsFor)+" "+Integer.toString(t.GoalsAgainst);
	        io.IOText(84,y,Constants.COL_BLUE,Constants.COL_BLACK,_Temp);
	        _Temp=Integer.toString(t.Points);
	        io.IOText(232,y,(n == d.Team) ? Constants.COL_YELLOW:Constants.COL_RED,Constants.COL_BLACK,_Temp);
	        y+=8;
	        if ((i == g.MoveCount-1 && g.Div > 0) ||
	            (i == d.NoTeams-g.MoveCount-1 && g.Div < g.DivCount-1))
	        {
	            io.IOLine(0,y+1,127,y+1,Constants.COL_CYAN);
	            io.IOLine(128,y+1,255,y+1,Constants.COL_CYAN);y += 2;
	        }
	    }
	    Init.INITPressEnter();
	}


	/************************************************************************/
	/*																		*/
	/*							Input a Yes/No value						*/
	/*																		*/
	/************************************************************************/

	boolean MENUYesNo()
	{
	    char c = 0;
	    int choice=0;
	    boolean finish=false;

	        {
	              long lasttime=io.IOClock();
	              while(io.IOClock()-lasttime<500) {}
	        }
	    
	    while(!finish) {
	        if(choice==0) {
	              io.IOText(-1,120,Constants.COL_BLACK,Constants.COL_GREEN,"Yes");
	              io.IOText(-1,130,Constants.COL_WHITE,Constants.COL_BLACK,"No");
	        } else {
	              io.IOText(-1,120,Constants.COL_WHITE,Constants.COL_BLACK,"Yes");
	              io.IOText(-1,130,Constants.COL_BLACK,Constants.COL_GREEN,"No");
	        }

	        int butt=io.IOJoy();
	        //if(SDL_GetTicks()-lastime>25) {
	              if(butt==Constants.VK_UP) choice=0;
	              if(butt==Constants.VK_DOWN) choice=1;
	              if(butt==Constants.VK_FX) {
	                    if(choice==0)
	                          c='Y';
	                    else
	                          c='N';
	                    finish=true;
	              }
	        //}
	    }
	    return c == 'Y';
	}


	/************************************************************************/
	/*																		*/
	/*							Display your score							*/
	/*																		*/
	/************************************************************************/
	public void _MENUScore(Game g) {
		ThisGame=g;
		Thread thread = new Thread(null,MENUScoreThread,"Background");
		thread.start();
	}

	private Runnable MENUScoreThread = new Runnable() {
		public void run() {
			MENUScore(ThisGame);
			drv_linux.fm.getGameThread().setScreenState(GameThread.MODE_MENU);
			MENUMain(ThisGame);
		}
	};

	public void MENUScore(Game g)
	{
	    String Msg;
	    long s1;
	    io.IOClear(Constants.COL_BLACK);

	    if (g.Seasons != 0)
	    {
	        s1 = g.Score*2/g.Seasons;
	        if (s1 > 100) { s1 = 100; }
	        Msg="Managerial Rating (Max 100) = " + Double.toString(s1);
	        io.IOText(-1,32,Constants.COL_CYAN,Constants.COL_BLACK,Msg);
	        Msg="Number of Seasons = " + Double.toString(g.Seasons);
	        io.IOText(-1,48,Constants.COL_YELLOW,Constants.COL_BLACK,Msg);
	    }
	    else
	    {
	        io.IOText(-1,32,Constants.COL_YELLOW,Constants.COL_BLACK,"You have not completed a season");
	    }
	    Msg="Skill Level = " + Double.toString(g.Skill);
	    io.IOText(-1,64,Constants.COL_YELLOW,Constants.COL_BLACK,Msg);
	    Msg="Morale = " + Double.toString(g.Morale);
	    io.IOText(-1,80,Constants.COL_YELLOW,Constants.COL_BLACK,Msg);
	    MENUFinances(g,96);
	    Init.INITPressEnter();
	}
	

	/************************************************************************/
	/*																		*/
	/*						Display Financial Information					*/
	/*																		*/
	/************************************************************************/

	public void MENUFinances(Game g,int y)
	{
	    String _Msg;
	    _Msg="You have " + (char)96 + Double.toString(g.Cash) + " cash.";
	    io.IOText(-1,y,Constants.COL_GREEN,Constants.COL_BLACK,_Msg);
	    _Msg="You owe " + (char)96 + Double.toString(g.Loans) +" in loans.";
	    io.IOText(-1,y+16,Constants.COL_GREEN,Constants.COL_BLACK,_Msg);
	}


	/************************************************************************/
	/*																		*/
	/*								Obtain a loan							*/
	/*																		*/
	/************************************************************************/

	public void _MENUObtainLoan(Game g)
	{
	    int y = 40;
	    long n;
	    io.IOClear(Constants.COL_BLACK);
	    io.IOText(-1,8,Constants.COL_YELLOW,Constants.COL_RED," Obtain a loan ");
	    MENUFinances(g,y);
	    io.IOText(-1,96,Constants.COL_GREEN,Constants.COL_BLACK,"How much do you want to borrow ?");
	    n = MENUGetInt(112,112,6);
	    if (n <= 0) { return; }
	    if (n + g.Loans > 250000L * g.FinancialScaler)
	    {
	        io.IOText(-1,144,Constants.COL_CYAN,Constants.COL_BLACK,"Exceeds your credit limit");
	        Init.INITPressEnter();
	        return;
	    }
	    g.Cash += n;
	    g.Loans += n;
	    io.IOClear(Constants.COL_BLACK);
	    y = 24;MENUFinances(g,y); 
	    Init.INITPressEnter();
	}


	/************************************************************************/
	/*																		*/
	/*								Repay a loan							*/
	/*																		*/
	/************************************************************************/

	public void _MENURepayLoan(Game g)
	{
	    int y = 40;
	    long n;
	    io.IOClear(Constants.COL_BLACK);
	    io.IOText(-1,8,Constants.COL_YELLOW,Constants.COL_RED," Repay a loan ");
	    MENUFinances(g,y);
	    io.IOText(-1,96,Constants.COL_GREEN,Constants.COL_BLACK,"How much do you want to repay ?");
	    n = MENUGetInt(112,112,6);
	    if (n <= 0) { return; }
	    if (n > g.Loans)
	    {
	        io.IOText(-1,144,Constants.COL_CYAN,Constants.COL_BLACK,"You do not owe that much money");
	        Init.INITPressEnter();
	        return;
	    }
	    if (n > g.Cash)
	    {
	        io.IOText(-1,144,Constants.COL_CYAN,Constants.COL_BLACK,"You do not have that much money");
	        Init.INITPressEnter();
	        return;
	    }
	    g.Cash -= n;
	    g.Loans -= n;
	    io.IOClear(Constants.COL_BLACK);
	    y = 24;MENUFinances(g,y);
	    Init.INITPressEnter();
	}


	/************************************************************************/
	/*																		*/
	/*							Select Skill Level							*/
	/*																		*/
	/************************************************************************/

	private String Skills[] =
	{ "Beginner","Novice","Average","Good","Expert","Super Expert","Genius" };

	public void MENUSkillLevel(Game g)
	{
	    int y,i;
	    int choice=g.Skill;
	    io.IOClear(Constants.COL_BLACK);
	    io.IOText(-1,16,Constants.COL_YELLOW,Constants.COL_RED," Please select a skill level ");

	    for (i = 0;i < 7;i++)
	    {
	        y = 48 + i * 14;
	        io.IOChar(80,y,Constants.COL_YELLOW,Constants.COL_BLACK,i+49);
	        if(choice==i)
	             io.IOText(96,y,Constants.COL_BLACK,Constants.COL_CYAN,Skills[i]);
	        else
	             io.IOText(96,y,Constants.COL_CYAN,Constants.COL_BLACK,Skills[i]);        
	    }
	           //int butt=io.IOJoy();
	           //if(butt==Constants.VK_UP) choice--;
	           //if(butt==Constants.VK_DOWN) choice++;
	           //if(choice<0) choice=0;
	           //if(choice>6) choice=6;
	           //if(butt==Constants.VK_FX) {
	           //    g.Skill=choice+1;
	           //    finish=true;
	          // }
	    //}
	    //while (g.Skill < 0)
	    //{
	    //	i = io.IOInkey();
	    //    while (i==0) i = io.IOInkey();
	    //    if (i >= '1' && i <= '7') g.Skill = i-'0';
	    //}
	}
}
