package com.rgsoftworks.fm;

import android.view.KeyEvent;

public class Misc {
	/************************************************************************/
	/*																		*/
	/*		Name:		MISC.C												*/
	/*		Project:	Football Manager Remake								*/
	/*		Purpose:	Miscellaneous Routines        						*/
	/*		Author:		Paul Robson											*/
	/*		Created:	12th December 2001									*/
	/*		Updated:	18th December 2001									*/
	/*																		*/
	/************************************************************************/

	/************************************************************************/
	/*																		*/
	/*							Team Selection								*/
	/*																		*/
	/************************************************************************/
	//private int Chosen_DIV=-1;
	//private int Chosen_Team=-1;
	
	//public int getChosen_DIV() { return Chosen_DIV; }
	//public int getChosen_Team() { return Chosen_Team; }
	//public void setChosen_DIV(int Chosen) { Chosen_DIV=Chosen; }
	//public void setChosen_Team(int Chosen) { Chosen_Team=Chosen; }

	private int lastKeyCode=0;
	private boolean mFinished=false;
	
	private Game ThisGame=null;

	public boolean getFinished() { return mFinished; }
	
	public void MISCSelectTeam(Game g)
	{
		ThisGame=g;
		Thread thread = new Thread(null,SelectTeam,"Background");
		thread.start();
	}
	
	private Runnable SelectTeam = new Runnable() {
		public void run() {
			SelectDivision(ThisGame);
		}
	};

	public void setLastKey(int keyCode) {
		lastKeyCode=keyCode;
	}
	
	private void SelectDivision(Game g) {
	    int x,y,n = 0,Div = -1,Found = 0;
	    int choice=0;
	    {
	        //long lasttime=io.IOClock();
	        //while(io.IOClock()-lasttime<500) {}
	    }
	    TeamInfo t1,t2;
	    String _Temp;
	    g.TeamNo=-1;
	    while (Found == 0)
	    {
	        choice=0;
	        Div = Div+1;                              /* Next division */
	        if (Div==g.DivCount) Div=0;              /* Loop round if at the end */
	        io.IOClear(Constants.COL_BLACK);                       /* Display labels */
	        _Temp=" "+g.Division[Div].DivName+" ";
	        io.IOText(-1,8,Constants.COL_YELLOW,Constants.COL_RED,_Temp);
	        io.IOText(-1,148,Constants.COL_GREEN,Constants.COL_BLACK," Please Select a Team ");
	        io.IOText(-1,158,Constants.COL_GREEN,Constants.COL_BLACK,"Press R for more teams");

            boolean finish=false;
            boolean changed=true;
            lastKeyCode = 0;
	        
	        while(!finish) {
	        	if(changed) {
			        for (n = 0;n < g.Division[Div].NoTeams;n++)
			        {
			            x = (n % 2)*128+4;y = (n / 2)*10+24;
			            _Temp=Integer.toString(n+1);io.IOText(x,y,Constants.COL_YELLOW,Constants.COL_BLACK,_Temp);
			            if(choice==n)
			                 io.IOText(x+24,y,Constants.COL_BLACK,Constants.COL_GREEN,g.Division[Div].Teams[n].Name);
			            else
			                 io.IOText(x+24,y,Constants.COL_GREEN,Constants.COL_BLACK,g.Division[Div].Teams[n].Name);
			        }
			        changed=false;
	        	}
	            if(lastKeyCode==KeyEvent.KEYCODE_DPAD_UP) {
	            	choice--;
	            	changed=true;
	            	lastKeyCode=0;
	            }
	            if(lastKeyCode==KeyEvent.KEYCODE_DPAD_DOWN) {
	            	choice++;
	            	changed=true;
	            	lastKeyCode=0;
	            }
	            
	            if(choice<0) choice=0;
	            if(choice>=g.Division[Div].NoTeams) choice=g.Division[Div].NoTeams-1;
	            if(lastKeyCode==KeyEvent.KEYCODE_DPAD_CENTER) {
	                Found=1;
	                n=choice;
	                finish=true;
	            	lastKeyCode=0;
	            }
	            if(lastKeyCode==KeyEvent.KEYCODE_DPAD_RIGHT) finish=true;
	        }
	    }
	/* t1 points to this,t2 to last team*/
	    t1 = (g.Division[Div].Teams[n]);
	    t2 = (g.Division[g.DivCount-1].Teams[0]);
	    //t = t1;t1 = t2;t2 = t;                    /* Relegate to bottom of league */
	    g.Division[Div].Teams[n]=t2;
	    g.Division[g.DivCount-1].Teams[0]=t1;
	    g.TeamNo=n;
	    //this.notifyAll();
	    //drv_linux.fm.getGameThread().setState(GameThread.STATE_SELECTDIFF);
	}
	
	//private void NextDivision(Game g) {
    //    Chosen_Team=0;
    //    Chosen_DIV = Chosen_DIV+1;                              /* Next division */
    //    if (Chosen_DIV==g.DivCount) Chosen_DIV=0;              /* Loop round if at the end */
    //    io.IOClear(Constants.COL_BLACK);                       /* Display labels */
    //    DisplayTeams(g);
	//}
	
	/*public void DisplayTeams(Game g) {
	    int x,y,n = 0,Div = -1;
	    int choice=0;
	    String _Temp;
	    
	    drv_linux.fm.getGameView().setFocusableInTouchMode(true);
	    Div=Chosen_DIV;
	    choice=Chosen_Team;
        if(choice<0) choice=0;
        if(choice>=g.Division[Div].NoTeams) choice=g.Division[Div].NoTeams-1;

        _Temp=" " + g.Division[Div].DivName + " ";
        io.IOText(-1,8,Constants.COL_YELLOW,Constants.COL_RED,_Temp);
        io.IOText(-1,148,Constants.COL_GREEN,Constants.COL_BLACK," Please Select a Team ");
        io.IOText(-1,158,Constants.COL_GREEN,Constants.COL_BLACK,"Press R for more teams");
        //{
	    //    long lasttime=SystemClock.elapsedRealtime();
	    //    while(SystemClock.elapsedRealtime()-lasttime<500) {}
	    //}
	    //while (Found == 0)
	    //{

            //boolean finish=false;
	        
	        //while(!finish) {
	                                                  /* Display the teams */
	    /*    for (n = 0;n < g.Division[Div].NoTeams;n++)
	        {
	            x = (n % 2)*128+4;y = (n / 2)*10+24;
	            _Temp= Integer.toString(n+1);io.IOText(x,y,Constants.COL_YELLOW,Constants.COL_BLACK,_Temp);
	            if(choice==n)
	                 io.IOText(x+24,y,Constants.COL_BLACK,Constants.COL_GREEN,g.Division[Div].Teams[n].Name);
	            else
	                 io.IOText(x+24,y,Constants.COL_GREEN,Constants.COL_BLACK,g.Division[Div].Teams[n].Name);
	        }

	            //int butt=io.IOJoy();
	            //if(butt==Constants.VK_UP) choice--;
	            //if(butt==Constants.VK_DOWN) choice++;
	            
	            //if(butt==Constants.VK_FX) {
	                //Found=1;
	            //    n=choice;
	                //finish=true;
	            //}
	           // if(butt==Constants.VK_FR) finish=true;
	        //}
	   // }
	} */
	
	/*public void InitTeam(Game g) {
	    TeamInfo t,t1,t2;
	 	// t1 points to this,t2 to last team
	    t1 = (g.Division[Chosen_DIV].Teams[Chosen_Team]);
	    t2 = (g.Division[g.DivCount-1].Teams[0]);
	    t = t1;t1 = t2;t2 = t;                    // Relegate to bottom of league
	} */


	/************************************************************************/
	/*																		*/
	/*								Title Page								*/
	/*																		*/
	/************************************************************************/

	public void MISCTitle()
	{
	    io.IOClear(Constants.COL_BLACK);
	    io.IOText(-1,4,Constants.COL_YELLOW,Constants.COL_RED,"                  ");
	    io.IOText(-1,12,Constants.COL_YELLOW,Constants.COL_RED," Football Manager ");
	    io.IOText(-1,20,Constants.COL_YELLOW,Constants.COL_RED,"                  ");
	    io.IOText(-1,40,Constants.COL_CYAN,Constants.COL_BLACK,"A rewrite of the Sinclair");
	    io.IOText(-1,50,Constants.COL_CYAN,Constants.COL_BLACK,"Spectrum Classic.");
	    io.IOText(-1,80,Constants.COL_GREEN,Constants.COL_BLACK,"Written by Paul Robson 2001-2");
	    io.IOText(-1,90,Constants.COL_GREEN,Constants.COL_BLACK,"Linux/SDL Port 2003");
	    io.IOText(-1,140,Constants.COL_GREEN,Constants.COL_BLACK,"Original version by Kevin Toms");
	    io.IOText(-1,150,Constants.COL_GREEN,Constants.COL_BLACK,"Addictive Games, 1982");    
	    io.IOText(-1,160,Constants.COL_GREEN,Constants.COL_BLACK,"Android Port By Jonn Blanchard");
	}


	/************************************************************************/
	/*																		*/
	/*									Pay Bills							*/
	/*																		*/
	/************************************************************************/

	/*public void MISCBills(Game g)
	{
		ThisGame=g;
		mFinished=false;
		Thread mt = new Thread(null,MISCBillsThread,"Background");
		mt.start();
	}
	
	private Runnable MISCBillsThread = new Runnable() {
		public void run() {
			RunMISCBills(ThisGame);
		}
	};*/
	
	public void MISCBills(Game g) {
	    long Interest,Wages = 0;
	    int i,y = 32;
	    String _Temp;
	    io.IOClear(Constants.COL_BLACK);
	    io.IOText(-1,8,Constants.COL_YELLOW,Constants.COL_RED," Weekly Bills ");
	    for (i = 0;i < g.PlayerCount;i++)            /* Calculate the wages */
	        if (g.Player[i].InOurTeam)
	            Wages = Wages + (long)(g.Player[i].Skill) * 10L * g.FinancialScaler;
	    
	    if(g.Loans>0)
	    	Interest = g.Loans/100;                      /* CAlculate interest */
	    else
	    	Interest=0;
	    _Temp="Wage Bill " + (char)96 + Double.toString(Wages);
	    io.IOText(-1,y,Constants.COL_GREEN,Constants.COL_BLACK,_Temp);y += 12;
	    _Temp="Ground Rent "+(char)96+Double.toString(g.GroundRent);
	    io.IOText(-1,y,Constants.COL_GREEN,Constants.COL_BLACK,_Temp);y += 12;
	    _Temp="Loan Interest "+(char)96+Double.toString(Interest);
	    io.IOText(-1,y,Constants.COL_GREEN,Constants.COL_BLACK,_Temp);y += 24;
	    g.Cash = g.Cash - Wages -                   /* Take the money out */
	        Interest - g.GroundRent;
	    _Temp="Weekly Balance "+(char)96+Double.toString(g.Cash);
	    io.IOText(-1,y,(g.Cash < 0) ? Constants.COL_RED:Constants.COL_YELLOW,Constants.COL_BLACK,_Temp);y += 24;

	    if (g.Cash <= 0)                             /* Borrow to make up the difference */
	    {
	        g.Loans += (-g.Cash);                   /* Do so */
	        g.Cash = 0;
	        io.IOText(-1,y,Constants.COL_MAGENTA,Constants.COL_BLACK,"Loan increased to pay the Bills");
	        y+= 24;
	        if (g.Loans > 250000L *                  /* If you borrow too much,Game over */
	            g.FinancialScaler)
	        {
	            io.IOText(-1,y,Constants.COL_YELLOW,Constants.COL_RED," The team is bankrupt ");
	            io.IOText(-1,y+16,Constants.COL_YELLOW,Constants.COL_RED," The board have sacked you ");
	            g.Sacked = 1;
	        }
	    }
	    Init.INITPressEnter();
	    mFinished=true;
	}


	/************************************************************************/
	/*																		*/
	/*							Transfer Market								*/
	/*																		*/
	/************************************************************************/

	/*public void MISCTransfers(Game g)
	{
		ThisGame=g;
		mFinished=false;
		Thread mt = new Thread(null,MISCTransfersThread,"Background");
		mt.start();
	}
	
	private Runnable MISCTransfersThread = new Runnable() {
		public void run() {
			RunMISCTransfers(ThisGame);
		}
	};*/
	
	public void MISCTransfers(Game g) {
	    int pID,n;
	    long Reqd,Bid;
	    String _Temp;
	    Player p;
	    fmMenu Menu=drv_linux.fm.getGameMenu();
	    do                                            /* Find one who doesn't play for us */
	    {
	        pID = (int)Math.floor(Math.random() * g.PlayerCount);
	        p = (g.Player[pID]);
	    }
	    while (p.InOurTeam);
	    do
	    {
	        io.IOClear(Constants.COL_BLACK);
	        io.IOText(-1,8,Constants.COL_YELLOW,Constants.COL_RED," Transfer Market ");
	        if (g.Available > 15)                    /* Max of 15 players */
	        {
	            io.IOText(-1,82,Constants.COL_CYAN,Constants.COL_BLACK,"You cannot buy any more players");
	            io.IOText(-1,92,Constants.COL_CYAN,Constants.COL_BLACK,"16 is the maximum allowed");
	            Init.INITPressEnter();
	            return;
	        }
	        Menu.MENUFinances(g,32);                       /* Display finances */
	        n = 80;Menu.MENUPlayer(g,-1,n);               /* Display player information */
	        n = 90;Menu.MENUPlayer(g,pID,n);
	        io.IOText(-1,140,Constants.COL_GREEN,Constants.COL_BLACK,"Type your bid");
	        Bid = Menu.MENUGetInt(104,150,6);
	        if (Bid > g.Cash)                        /* Not enough cash */
	        {
	            io.IOText(-1,170,Constants.COL_CYAN,Constants.COL_BLACK," You do not have enough money ");
	            Init.INITPressEnter();
	        }
	        else                                      /* Bid feasible */
	        if (Bid > 0)
	        {                                         /* Calculate value required */
	            Reqd = (long)(Math.floor(Math.random()*10)) *
	                Bid / (p.Value);
	            if (Reqd <= 5)                        /* Not enough */
	            {
	                io.IOText(-1,170,Constants.COL_CYAN,Constants.COL_BLACK," Bid Refused ! ");
	                p.Value = p.Value + (p.Value/5);
	                Init.INITPressEnter();
	            }
	            else                                  /* Successful bid */
	            {
	                p.Value =                        /* Reset the player value */
	                    g.FinancialScaler * p.Skill * 5000;
	                p.InOurTeam = true;                 /* In the team and available */
	                p.Status = Constants.AVAILABLE;
	                _Temp=p.Name + " has joined your team";
	                io.IOText(-1,170,Constants.COL_YELLOW,Constants.COL_BLACK,_Temp);
	                g.Cash -= Bid;                   /* Lose the cash */
	                Init.INITPressEnter();
	                Bid = 0;                          /* Drops out the loop */
	                return;
	            }
	        }
	    } while (Bid > 0);                            /* Abandon it */
	    mFinished=true;
	}
}
