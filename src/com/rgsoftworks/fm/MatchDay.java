package com.rgsoftworks.fm;

import java.util.ArrayList;

import android.util.Log;
import android.view.KeyEvent;

import com.rgsoftworks.fm.io;
import com.rgsoftworks.fm.Init;
import com.rgsoftworks.fm.GameView.GameThread;

public class MatchDay extends Thread {
	/************************************************************************/
	/*																		*/
	/*		Name:		MATCHDAY.C 											*/
	/*		Project:	Football Manager Remake								*/
	/*		Purpose:	Matchday routines									*/
	/*		Author:		Paul Robson											*/
	/*		Created:	12th December 2001									*/
	/*		Updated:	18th December 2001									*/
	/*																		*/
	/************************************************************************/

	public boolean needtowait=false;
	private int DivCup=0,Fixture=0;
	private boolean IsReplay=false;
	private boolean AtHome=false;
	private boolean IsCup=false;
	private TeamInfo OurTeam;
	private TeamInfo Opposition;
	private Division Division;
	private Division ODivision;
	private int OurStat[]=new int[5],OppStat[]=new int[5];
	private int xc,yc;
	private boolean xFlipped;
	private Game ThisGame;

	/************************************************************************/
	/*																		*/
	/*							One complete matchday						*/
	/*																		*/
	/************************************************************************/

	boolean PlayMatch(Game g) {
		ThisGame = g;
		Thread thread = new Thread(null,doPlayGame,"Background");
		thread.start();

    	return true;
	}

	private Runnable doPlayGame = new Runnable() {
		public void run() {
			_MDPlayMatch(ThisGame);
		}
	};

	public int PreMatchStats(Game g) {
		int returnValue=0;

        if (Division.Played != 0) {               /* Energy and Injury adjust */
            _MDInjuryEnergy(g);
        }
            
        _MDWorkOutStatus(g);                      /* Work out oppositions status */
        _MDCalculateIncome(g);                    /* Calculate gate receipts */
        OurTeam.Score = 0;                       /* Reset the score to 0-0 */
        Opposition.Score = 0;
		
		if(_MDDisplayStats(g)) {
			returnValue=1;
		}

		Init.INITPressEnter();
		return returnValue;
	}

	/************************************************************************/
	/*																		*/
	/*								Team Selection							*/
	/*																		*/
	/************************************************************************/

	boolean _MDSelectTeam(Game g)
	{
		drv_linux.fm.gview.requestFocus();
	    String Msg = "";
	    int i,y=0;
	    int oldchoice=0,choice=0;
	    int PlayCount=0;
	    int footerPosition=0;
	    boolean finished=false;
	    ArrayList<MenuArray> Players=new ArrayList<MenuArray>(); //drv_linux.fm.getGameMenu().getMenuList();

	    Players.clear();
	    
	    io.IOClear(Constants.COL_BLACK);                           /* Editing options */
	    y = drv_linux.fm.getGameMenu().MENUPlayer(g,-1,y);
	    for (i = 0; i < g.PlayerCount;i++)
	        if (g.Player[i].InOurTeam) {
	        	MenuArray Player = new MenuArray();
	        		        	
	        	Player.index = i;
	        	Player.Y=y;
	        	y=drv_linux.fm.getGameMenu().MENUPlayer(g,i,y,choice==PlayCount ? true:false);
	        	Players.add(Player);

	        	PlayCount++;
	        }

	    footerPosition=y;
	    y=drv_linux.fm.getGameMenu().MENUPlayer(g,-2,y);
	/* Display prompt */
	    while(!finished) {
		    io.IOText(-1,154,Constants.COL_CYAN,Constants.COL_BLACK,g.Picked > 11 ?
		        "Select the player to drop":"Select the player to pick");
		    //i = (int)drv_linux.fm.getGameMenu().MENUGetInt(120,164,2);               /* Get number */
		    int joy=0;
		    while(joy==0)
		    	joy=io.IOJoy();

		    i=-1;
		    if(joy==Constants.VK_LEFT || joy==KeyEvent.KEYCODE_BACK) { // Exit
		    	i=0; finished=true;
		    } else if(joy==Constants.VK_UP) {
		    	if(choice>0) choice--;
		    } else if(joy==Constants.VK_DOWN) {
		    	if(choice<PlayCount-1) choice++;
		    } else if(joy==Constants.VK_FX) {
		    	i=Players.get(choice).index+1;
		    }

		    if(i>=0) {
			    if (i == 0) { return false; }                         /* Nothing selected */
			    if (i > g.PlayerCount) { return false; }             /* Too high */
			    i--;                                          /* Convert to index */
			    if (g.Picked <= 11)                          /* Adding a player */
			    {
			        if (g.Player[i].Status == Constants.INJURED) {
			            Msg = "The player is injured";
			        } else if (g.Player[i].Status == Constants.PICKED) {
			            Msg = "Player is selected already";
			        } else if (!g.Player[i].InOurTeam) {
			            Msg = "Player is not in our team";
			        } else {
			            g.Player[i].Status = Constants.PICKED;
			            g.Picked++;
			    	    drv_linux.fm.getGameMenu().MENUPlayer(g,-2,footerPosition);
			        }
			    } else                                          /* Chopping a player */
			    {
			        if (!g.Player[i].InOurTeam ||
			            g.Player[i].Status != Constants.PICKED) {
			            Msg = "You cannot drop that player";
			        } else {
			            g.Player[i].Status = Constants.AVAILABLE;
			            g.Picked--;
			    	    drv_linux.fm.getGameMenu().MENUPlayer(g,-2,footerPosition);
			        }
			    }
		
			    if (!Msg.equals(""))                              /* Message ? */
			    {
			        io.IOText(-1,174,Constants.COL_YELLOW,Constants.COL_BLACK,Msg);
			        //Init.INITPressEnter();
			    }
		    }
		    if(oldchoice!=choice || i>0) {
	        	if(oldchoice!=choice) y=drv_linux.fm.getGameMenu().MENUPlayer(g,Players.get(oldchoice).index,Players.get(oldchoice).Y,false);
	        	y=drv_linux.fm.getGameMenu().MENUPlayer(g,Players.get(choice).index,Players.get(choice).Y,true);
	        	oldchoice=choice;
		    }
	    }
	    i = (g.Picked > 11 ? 1:0);                         /* if was 12+ or is now 12 then */
	    _MDCalcStatus(g);                             /* We must edit again */
	    return i==1 || (g.Picked > 11) ;
	}

	public int SelectTeam(Game g) {
		int returnValue=-1;
		
        if(_MDSelectTeam(g))              /* Keep selecting team */
        	returnValue=2;
        
        return returnValue;
	}

	boolean MDRun(Game g) {
		ThisGame = g;
		Thread thread = new Thread(null,MDRunThread,"Background");
		thread.start();

    	return true;
	}

	private Runnable MDRunThread = new Runnable() {
		public void run() {
			_MDRun(ThisGame);
		}
	};

	void _MDRun(Game g)
	{
	    String _Temp,Msg;
	    int Col;
	    Division = (g.Division[g.Div]);            /* Our division */
	/* This is our team */
	    OurTeam = (Division.Teams[Division.Team]);
	/* Get the fixture */
	    Fixture = Division.FixtureList[Division.Played];

	    IsCup = (Fixture < 0);                        /* Set "Is Cup" flag */
	    if (IsCup && g.OutOfCup)                     /* Check knocked out of cup */
	    {
	        io.IOClear(Constants.COL_BLACK);
	        io.IOText(-1,10*8-4,Constants.COL_CYAN,Constants.COL_BLACK,"F.A. Cup Matches this week");
	        io.IOText(-1,10*8+12,Constants.COL_CYAN,Constants.COL_BLACK,"You were knocked out.");
	        Init.INITPressEnter();
	        Division.Played++;
		    drv_linux.fm.getGameThread().setScreenState(GameThread.MODE_ENDMATCH);
	        return;
	    }
	    if (IsCup) {                                /* Record last round played */
	        g.LastCupRound = -Fixture;
	    }
	    Init.INITGenerateSortArray(g);                     /* Calculate league posns */
	    _MDIdentifyGround(g);                         /* Where is it played */
	    _MDSelectOpposition(g);                       /* Who are we playing */
	    IsReplay = false;
	    do
	    {
	        if (Division.Played != 0) {             /* Energy and Injury adjust */
	            _MDInjuryEnergy(g);
	        }
	        _MDWorkOutStatus(g);                      /* Work out oppositions status */
	        _MDCalculateIncome(g);                    /* Calculate gate receipts */
	        OurTeam.Score = 0;                       /* Reset the score to 0-0 */
	        Opposition.Score = 0;	
	        while (_MDDisplayStats(g))                /* Keep going till continue */
		            while (_MDSelectTeam(g))              /* Keep selecting team */
		        {
		        };
		        
				_MDPlayMatch(g);
			//}
		        io.IOClear(Constants.COL_BLACK);                       /* Display final score & receipts */
		        io.IOText(-1,1*8,Constants.COL_YELLOW,Constants.COL_RED," **** FINAL SCORE ****");
		        if (AtHome) {
		            _Temp=OurTeam.Name+" "+Integer.toString(OurTeam.Score)+"   "+Opposition.Name+" "+Integer.toString(Opposition.Score);
		        } else {
		            _Temp=Opposition.Name+" "+Integer.toString(Opposition.Score)+"   "+OurTeam.Name+" "+Integer.toString(OurTeam.Score);
		        }
		        io.IOText(-1,6*8,Constants.COL_GREEN,Constants.COL_BLACK,_Temp);
		        io.IOText(-1,14*8,Constants.COL_WHITE,Constants.COL_BLACK,"Gate Receipts");
		        _Temp=(char)96+Double.toString(g.Receipts);
		        io.IOText(-1,16*8,Constants.COL_YELLOW,Constants.COL_BLACK,_Temp);
		        g.Cash += g. Receipts;                  /* Add receipts to cash */
		        Init.INITPressEnter();

		        if (IsCup &&                              /* Cup replay */
		            OurTeam.Score == Opposition.Score)
		        {
		            IsReplay = true;
		            AtHome = (!AtHome);
		        }
		        else                                      /* Otherwise, the loop ends */
		            IsReplay = false;
		    } while (IsReplay);
		    if (!IsCup)                               /* League match, other results */
		    {
		        _MDAdjustStatistics(OurTeam,Opposition);
		        if (AtHome) {
		            _MDCalculateResults(g,OurTeam,Opposition);
		        } else {
		            _MDCalculateResults(g,Opposition,OurTeam);
		        }
		        if (OurTeam.Score < Opposition.Score)
		        {
		            g.CurrentCrowd = g.CurrentCrowd * 9 / 10;
		            if (g.CurrentCrowd < 1000) { g.CurrentCrowd = 1000; }
		        }
		        if (OurTeam.Score > Opposition.Score) {
		            g.CurrentCrowd += (10000*g.FinancialScaler-g.CurrentCrowd)/10;
		        }
		        drv_linux.fm.getGameMenu()._INITDisplayDivision(g);
		    }
		    else                                          /* Analyse etc. cup results */
		    {
		        if (Opposition.Score >                   /* Set out of cup flag */
		            OurTeam.Score)
		        {
		            g.OutOfCup = true;
		            Col = Constants.COL_MAGENTA;
		            Msg = "You're out of the F.A. Cup";
		        }
		        else
		        {
		            Col = Constants.COL_CYAN;Msg = "You're through to the next round";
		            if (Fixture == -8) {
		                Col = Constants.COL_YELLOW;Msg = "You've won the F.A. Cup !";
		            }
		        }
		        io.IOClear(Constants.COL_BLACK);
		        io.IOText(-1,12,Constants.COL_YELLOW,Constants.COL_RED," F.A. Cup ");
		        io.IOText(-1,84,Col,Constants.COL_BLACK,Msg);
		        Init.INITPressEnter();
		    }
		/* Adjust morale of team */
		    if (Opposition.Score > OurTeam.Score) {
		        g.Morale = g.Morale / 2;
		    }
		    if (OurTeam.Score > Opposition.Score) {
		        g.Morale = g.Morale + (20-g.Morale)/2;
		    }
		    if (g.Morale < 1) g.Morale = 1;             /* Morale forced into range */
		    if (g.Morale > 20) g.Morale = 20;
		    Division.Played++;                           /* Played one more match */
	        
		    Misc misc=new Misc();
		    misc.MISCBills(g);
	        if (g.Sacked == 0)                        /* Sacked on financial grounds */
	        {
	            misc.MISCTransfers(g);                    /* Buy players ? */
	            Eos endofseason=new Eos();
	            endofseason.EOSEndSeason(g);                     /* End of Season Code */
	        }
		    drv_linux.fm.getGameThread().setScreenState(GameThread.MODE_ENDMATCH);
	}

	/************************************************************************/
	/*																		*/
	/*						Identify Home or Away							*/
	/*																		*/
	/************************************************************************/

	private void _MDIdentifyGround(Game g)
	{
	    AtHome = (int)Math.floor(Math.random()*2)!=0;                            /* At home random in cup */
	    if (!IsCup)                               /* Identify home or away if league */
	    {
	        AtHome = Division.FirstMatchHome;
	        for (int i = 0;i < Division.Played;i++) {
	            if (Division.FixtureList[i] >= 0) AtHome = (!AtHome);
	        }
	        Opposition = (Division.Teams[Fixture]);
	        ODivision = Division;
	    }
	}


	/************************************************************************/
	/*																		*/
	/*			Get information about opposition in the cup 				*/
	/*																		*/
	/************************************************************************/

	private void _MDSelectOpposition(Game g)
	{
	    int i;
	    if (IsCup)                               /* If a cup match.... */
	    {
	        do                                        /* Select a Division */
	        {
	            i = (int)Math.floor(Math.random()*g.DivCount);
	            if (-Fixture <= 2) { i++; }
	            if (-Fixture == 3) { i++; }
	            if (-Fixture >= 7) { i--; }
	            if (-Fixture == 8) { i=0; }
	        } while (i < 0 || i >= g.DivCount);

	        ODivision = (g.Division[i]);            /* Select a team in the Division */
	        DivCup = i;
	        do
	        {
	            i = (int)Math.floor(Math.random()*ODivision.NoTeams);
	        } while (ODivision == Division            /* Not us */
	            && i == Division.Team);
	        Opposition = (ODivision.Teams[i]);
	    }
	}


	/************************************************************************/
	/*																		*/
	/*					Work out oppositions status levels					*/
	/*																		*/
	/************************************************************************/

	private void _MDWorkOutStatus(Game g)
	{
	    int i;
	    for (i = 0;i < 5;i++)                         /* Work out statuses */
	    {
	        if (IsCup)                           /* Cup team statuses */
	        {
	            OppStat[i] = (int)Math.floor(Math.random() * 16 + g.Skill);
	            OppStat[i] = OppStat[i] - (DivCup - g.Div);
	        }
	        else                                      /* League team statuses */
	        {
	            OppStat[i] = (int)Math.floor(Math.random() *  14 + g.Skill);
	            OppStat[i] += Opposition.Points / (Division.Played+1);
	        }
	        if (OppStat[i] < 1) {                      /* Force into range */
	            OppStat[i] = 1;
	        }
	        if (OppStat[i] > 20) {
	            OppStat[i] = 20;
	        }
	    }
	}


	/************************************************************************/
	/*																		*/
	/*						Calculate Game Receipts							*/
	/*																		*/
	/************************************************************************/

	private void _MDCalculateIncome(Game g)
	{
	    if (IsCup)                                    /* Cup receipts */
	    {
	        if (AtHome) {                              /* Home Game */
	            g.Receipts = g.CurrentCrowd;
	        } else {                                      /* Away Game */
	            g.Receipts = (g.DivCount - g.Div)*1000;
	        }
	/* End Games */
	        if (Fixture == -7) { g.Receipts = 50000L; }
	        if (Fixture == -8) { g.Receipts = 100000L; }
	    }
	    else                                          /* League Receipts */
	    {
	        if (AtHome) {
	            g.Receipts = g.CurrentCrowd;
	        } else {
	            g.Receipts = (Division.NoTeams-Opposition.LeaguePos) *
	                g.FinancialScaler * 500;
	        }
	    }
	}


	/************************************************************************/
	/*																		*/
	/*						Display league Game/cup Game					*/
	/*																		*/
	/************************************************************************/

	private String Label[] = { "Energy","Morale","Defence","Midfield","Attack" };

	private void _MDDisplayTeam(Game g)
	{
	    String _Msg;
	    io.IOClear(Constants.COL_BLACK);
	    if (IsCup)                                    /* Work out cup round name */
	    {
	        _Msg=" F.A. Cup Round "+Integer.toString(-Fixture);
	        if (Fixture == -7) { _Msg="F.A. Cup Semi-Final"; }
	        if (Fixture == -8) { _Msg="F.A. Cup Final"; }
	        if (IsReplay) { _Msg+=" Replay"; }
	        _Msg+=" ";
	    }
	    else                                          /* League match */
	    {
	        _Msg=" League Match - "+Division.DivName+" ";
	    }
	    io.IOText(-1,8,Constants.COL_YELLOW,Constants.COL_RED,_Msg);
	    io.IOText(-1,32,Constants.COL_GREEN,Constants.COL_BLACK,"V");
	/* Display team info */
	    _MDDisplayInfo((AtHome ? 1:17),Division,OurTeam,OurStat);
	    _MDDisplayInfo((AtHome ? 17:1),ODivision,Opposition,OppStat);
	    for (int i = 0;i < 5;i++)                         /* Status labels */
	    {
	        io.IOText(-1,56+i*14,Constants.COL_WHITE,Constants.COL_BLACK,Label[i]);
	    }
	}


	/************************************************************************/
	/*																		*/
	/*					  Display one teams information						*/
	/*																		*/
	/************************************************************************/

	private void _MDDisplayInfo(int x,Division Div,TeamInfo Team,int Stat[])
	{
	    String _Msg;
	    int i;
	    x = x * 8;                                    /* Name, and maybe Division */
	    _Msg=Team.Name;
	    io.IOText(x+60-(_Msg.length())*4,32,Constants.COL_CYAN,Constants.COL_BLACK,_Msg);
	    _Msg="Pos. " + Integer.toString(Team.LeaguePos);
	    if (IsCup) {
	        _Msg=Div.DivName;
	    }
	    io.IOText(x+60-(_Msg.length())*4,40,Constants.COL_GREEN,Constants.COL_BLACK,_Msg);
	    for (i = 0;i < 5;i++)                         /* Statuses */
	    {
	        _Msg=Integer.toString(Stat[i]);
	        io.IOText(x+60-(_Msg.length())*4,56+i*14,Constants.COL_MAGENTA,Constants.COL_BLACK,_Msg);
	    }
	}


	/************************************************************************/
	/*																		*/
	/*					Calculate our status and no of players				*/
	/*																		*/
	/************************************************************************/

	private void _MDCalcStatus(Game g)
	{
	    int i;
	    for (i = 0;i < 5;i++) OurStat[i]=0;           /* Erase our status */
	    g.Picked = g.Injured = g.Available = 0;
	    OurStat[1] = g.Morale;                       /* Copy morale */
	    for (i = 0;i < g.PlayerCount;i++)            /* Calculate statuses */
	        if (g.Player[i].InOurTeam)               /* Count the team up */
	    {
	        g.Available++;
	        switch(g.Player[i].Status)
	        {
	            case Constants.AVAILABLE:                       /* Available for selection */
	                break;
	            case Constants.INJURED:                         /* Injured */
	                g.Injured++;break;
	            case Constants.PICKED:                          /* In the team */
	                g.Picked++;
	                OurStat[0] +=                     /* Calculating mean energy */
	                    g.Player[i].Energy;
	                switch(g.Player[i].Pos)          /* And area strengths */
	                {
	                    case Constants.DEFENCE:   OurStat[2] += g.Player[i].Skill;break;
	                    case Constants.MIDFIELD:  OurStat[3] += g.Player[i].Skill;break;
	                    case Constants.ATTACK:    OurStat[4] += g.Player[i].Skill;break;
	                }
	                break;
	        }
	    }
	    OurStat[0] /= g.Picked;                      /* Average energy */
	}


	/************************************************************************/
	/*																		*/
	/*				Display Status Info, select Change or Play				*/
	/*																		*/
	/************************************************************************/

	private boolean _MDDisplayStats(Game g)
	{
		drv_linux.fm.gview.requestFocus();
		_MDCalcStatus(g);                             /* Calc statuses and show them */
		_MDDisplayTeam(g);
		if (g.Picked > 11)                           /* Too many players, must change */
		{
		    Init.INITPressEnter(); return true;
		}
		 boolean finish=false;
		 {
		      long lasttime=io.IOClock();
		      while(io.IOClock()-lasttime<500) {}
		 }

		drv_linux.setKeyPress(0);
		int choice=0,oldchoice=-1;
		while(!finish) {
			if(oldchoice!=choice) {
				io.IOText(30, 170, Constants.COL_WHITE, choice==0 ? Constants.COL_RED:Constants.COL_BLACK, "Play Game");
				io.IOText(160, 170, Constants.COL_WHITE, choice==1 ? Constants.COL_RED:Constants.COL_BLACK, "Change Team");
				oldchoice=choice;
			}
			int butt=io.IOJoy();

			if(butt==-1) {
				float tx=drv_linux.getTouchX();
				float ty=drv_linux.getTouchY();
								
				if(tx>=30 && tx<=30+(9*8) && ty>=162 && ty<=192) {
					choice=0;
				} else if(tx>=160 && tx<=160+(11*8) && ty>=162 && ty<=192) {
					choice=1;
				}
			}
			if(butt==Constants.VK_LEFT) {
				choice=0;
			}
			if(butt==Constants.VK_RIGHT) {
				choice=1;
			}
			if(butt==Constants.VK_FX) {
				return (choice!=0);
			}
        }         

	    return true;
	}
	
	public boolean SelectPlayer(Game g, int i) {
		boolean returnVal=false;
		String Msg="";
		
	    //i = (int)drv_linux.fm.GameMenu.MENUGetInt(120,164,2);               /* Get number */
	    if (i < 0) { return false; }                         /* Nothing selected */
	    if (i > g.PlayerCount) { return false; }             /* Too high */
	    //i--;                                          /* Convert to index */
	    if (g.Picked <= 11)                          /* Adding a player */
	    {
	        if (g.Player[i].Status == Constants.INJURED) {
	            Msg = "The player is injured";
	        }
	        if (g.Player[i].Status == Constants.PICKED) {
	            Msg = "Player is selected already";
	        }
	        if (!g.Player[i].InOurTeam) {
	            Msg = "Player is not in our team";
	        }
	        if (Msg == "") {
	            g.Player[i].Status = Constants.PICKED;
	            returnVal=true;
	        }
	    }
	    else                                          /* Chopping a player */
	    {
	        if (!g.Player[i].InOurTeam ||
	            g.Player[i].Status != Constants.PICKED) {
	            Msg = "You cannot drop that player";
	        } else {
	            g.Player[i].Status = Constants.AVAILABLE;
	            returnVal=true;
	        }
	    }

	    if(Msg.equals("")) {
	    	Msg="                                                                                                                                                                                                                                                                ";
	    }
	    if (!Msg.equals(""))                              /* Message ? */
	    {
	        io.IOText(-1,174,Constants.COL_YELLOW,Constants.COL_BLACK,Msg);
	    }
	    i = (g.Picked > 11 ? 1:0);                         /* if was 12+ or is now 12 then */
	    _MDCalcStatus(g);                             /* We must edit again */
	    return returnVal;
	}


	/************************************************************************/
	/*																		*/
	/*						Energy/Injury Calculations						*/
	/*																		*/
	/************************************************************************/

	static void _MDInjuryEnergy(Game g)
	{
	    int i;
	    Player p;
	    for (i = 0;i < g.PlayerCount;i++)            /* Work through all players */
	    {
	        p = (g.Player[i]);                      /* Temporary pointer */
	        switch(p.Status)                         /* Adjust energy */
	        {
	            case Constants.PICKED:    p.Energy--;break;
	            case Constants.INJURED:   p.Energy++;break;
	            case Constants.AVAILABLE: p.Energy += 10;break;
	        }
	        if (p.Energy < 1) { p.Energy = 1; }          /* Force into limits */
	        if (p.Energy > 20) { p.Energy = 20; }
	        if (p.Status==Constants.INJURED) {                   /* Recover from injuries */
	            p.Status = Constants.AVAILABLE;
	        }
	        if (p.InOurTeam &&                       /* 1 in 20 chance of injury */
	            (int)Math.floor(Math.random() * 20) == 0) {
	            p.Status = Constants.INJURED;
	        }
	    }
	}


	/************************************************************************/
	/*																		*/
	/*						Update information for team						*/
	/*																		*/
	/************************************************************************/

	static void _MDAdjustStatistics(TeamInfo t1,TeamInfo t2)
	{
	    t1.GoalsFor += t1.Score;                    /* Update score */
	    t2.GoalsFor += t2.Score;
	    t1.GoalsAgainst += t2.Score;
	    t2.GoalsAgainst += t1.Score;
	    if (t1.Score == t2.Score) {                  /* Adjust points */
	        t1.Drawn++;t2.Drawn++;
	    }
	    if (t1.Score > t2.Score) {
	        t1.Won++;t2.Lost++;
	    }
	    if (t2.Score > t1.Score) {
	        t2.Won++;t1.Lost++;
	    }
	}


	/************************************************************************/
	/*																		*/
	/*				Calculate other results, display all results			*/
	/*																		*/
	/************************************************************************/

	static void _MDCalculateResults(Game g,TeamInfo t1,TeamInfo t2)
	{
	    int i,j;
	    String _Temp;
	    TeamInfo f,Fix[]=new TeamInfo[Constants.MAXTEAMS];
	    Division d = (g.Division[g.Div]);
	    if(g == null) return;
	    for (i = 0;i < d.NoTeams;i++)                /* Blank all entries */
	        Fix[i] = null;
	    Fix[0] = t1;Fix[1] = t2;
	    for (i = 0;i < d.NoTeams;i++)                /* Fill with the rest */
	        if (t1 != (d.Teams[i]) && t2 != (d.Teams[i]))
	    {
	        do
	        j = (int)Math.floor(Math.random() * d.NoTeams);
	        while (Fix[j] != null);
	        Fix[j] = (d.Teams[i]);
	    }

	    for (i = 2;i < d.NoTeams;i+=2)               /* Play the Games */
	    {
	        Fix[i].Score =
	            Fix[i].Points/(d.Played+1)+(int)Math.floor(Math.random() * 4);
	        Fix[i+1].Score =
	            Fix[i+1].Points/(d.Played+1)+(int)Math.floor(Math.random() * 4);
	        _MDAdjustStatistics(Fix[i],               /* Award points */
	            Fix[i+1]);
	    }

	    do                                            /* Sort results alphabetically */
	    {
	        j = 0;
	        for (i = 0;i < d.NoTeams-2;i+=2)
	            if (Fix[i].Name.equals(Fix[i+2].Name))
	        {
	            j = 1;
	            f = Fix[i];Fix[i] = Fix[i+2];Fix[i+2] = f;
	            f = Fix[i+1];Fix[i+1] = Fix[i+3];Fix[i+3] = f;
	        }
	    }
	    while (j != 0);

	    _Temp=" " + d.DivName + " Results ";                 /* Display the results */
	    io.IOClear(Constants.COL_BLACK);
	    io.IOText(-1,8,Constants.COL_YELLOW,Constants.COL_RED,_Temp);
	    for (i = 0;i < d.NoTeams;i+=2)
	    {
	    	String Team1Name = Fix[i].Name;
	    	String Team2Name = Fix[i+1].Name;
	    	String Team1Score = Integer.toString(Fix[i].Score);
	    	String Team2Score = Integer.toString(Fix[i+1].Score);
	    	
	    	if(Team1Name.length()>10) Team1Name=Team1Name.substring(0,10);
	    	if(Team2Name.length()>10) Team2Name=Team2Name.substring(0,10);
	    	if(Team1Score.length()>2) Team1Score=Team1Score.substring(0,2);
	    	if(Team2Score.length()>2) Team2Score=Team2Score.substring(0,2);

	    	while(Team1Name.length()<10) { Team1Name += " "; }
	    	while(Team2Name.length()<10) { Team2Name += " "; }
	    	while(Team1Score.length()<2) { Team1Score = " "+Team1Score; }
	    	while(Team2Score.length()<2) { Team2Score += " "; }

	        _Temp=Team1Name + " " + Team1Score + " - " + Team2Score + " " + Team2Name;
	        io.IOText(-1,24+i*6,Constants.COL_GREEN,Constants.COL_BLACK,_Temp);
	    }
	    Init.INITPressEnter();
	}

	/************************************************************************/
	/*																		*/
	/*						Play match with highlights						*/
	/*																		*/
	/************************************************************************/

	private void _MDPlayMatch(Game g)
	{
	    int n,Offer,c1,c2,s;
	    s = 150;                                      /* Speed */
	    c1 = OurTeam.Colour;                         /* Deal with shirt clash */
	    c2 = Opposition.Colour;
	    while (OurTeam.Colour == Opposition.Colour || (OurTeam.Colour==7 || Opposition.Colour==7))
	    {
	        n = (int)(Math.floor(Math.random()*7));
	        if (AtHome) {
	            Opposition.Colour = n;
	        } else {
	            OurTeam.Colour = n;
	        }
	    }

	    Opposition.Colour = drv_linux.convertSpecColour(Opposition.Colour);
	    OurTeam.Colour = drv_linux.convertSpecColour(OurTeam.Colour);
	    
	    OurTeam.HomeTeam = AtHome;                   /* Set the home team flags */
	    Opposition.HomeTeam = (!AtHome);
	    for (Offer = 0;Offer < 5;Offer++)
	    {
	        n = (int)(Math.floor(Math.random()*100)) + (OurStat[Offer]-OppStat[Offer])*5;
	        if (n >= 75) {
	            MAOneAttack(OurTeam,Opposition,AtHome,g.Sound,s);
	        }
	        n = (int)(Math.floor(Math.random()*100)) + (OppStat[Offer]-OurStat[Offer])*5;
	        if (n >= 75) {
	            MAOneAttack(Opposition,OurTeam,AtHome,g.Sound,s);
	        }
	    }
	    if (OurTeam.Score+Opposition.Score == 0)
	    {
	        MAOneAttack(OurTeam,Opposition,AtHome,g.Sound,s);
	        MAOneAttack(Opposition,OurTeam,AtHome,g.Sound,s);
	    }
	    OurTeam.Colour = c1;                         /* Restore Colours */
	    Opposition.Colour = c2;
	}

	private void _MAPlot(int x,int y)
	{
	    xc = xFlipped ? (255-x):x;
	    yc = 175-y;
	}

	private void _MADraw(int x,int y)
	{
	    int x1,y1;
	    x1 = xc + ((xFlipped)?-x:x);
	    y1 = yc-y;
	    io.IOLine(xc,yc,x1,y1,Constants.COL_WHITE);
	    xc = x1;yc = y1;
	}


	/************************************************************************/
	/*																		*/
	/*							Draw the goalmouth							*/
	/*																		*/
	/************************************************************************/

	private void _MAPitch()
	{
	    double d;
	    io.IOClear(Constants.COL_DKGREEN);                         /* Draw the pitch */
	    _MAPlot(71,88);_MADraw(-16,-8);_MADraw(-8,-16);_MADraw(24,0);
	    _MAPlot(8,0);_MADraw(175,175);_MADraw(255-183,0);
	    _MAPlot(8,0);_MADraw(127,0);_MADraw(120,0);
	    _MAPlot(72,64);_MADraw(0,24);_MADraw(48,48);_MADraw(0,-24);
	    _MAPlot(64,56);_MADraw(24,0);_MADraw(64,64);_MADraw(-24,0);
	    _MAPlot(32,24);_MADraw(96,0);_MADraw(115,115);_MADraw(-96,0);
	    _MAPlot(119,135);_MADraw(-16,-8);_MADraw(-8,-16);_MADraw(24,0);
	    _MAPlot(102,126);_MADraw(-48,-48);
	    _MAPlot(158,84);_MADraw(2,0);
	    _MAPlot(158,85);_MADraw(2,0);
	    _MAPlot(170,100);             

	    d = -7.0;
	    while (d <= 95)
	    {
	        io.IOPut((int)(xc + (xFlipped?-1:1)*40*Math.cos(2*3.14*d/360)),
	            (int)(yc+40*Math.sin(2*3.14*d/360)),Constants.COL_WHITE);
	        d = d + 1.0;
	    }
	}


	/************************************************************************/
	/*																		*/
	/*								Draw a player							*/
	/*																		*/
	/************************************************************************/

	private void _MADrawPlayer(Coord Pos,int Colour,boolean FaceLeft)
	{
	    int c = (Pos.x+Pos.y)/2%4;
	    int x = xFlipped ? 255-Pos.x:Pos.x;
	    if (x < 0 || Pos.y < 0) { return; }
	    if (x >= 248 || Pos.y >= 184) { return; }
	    if (FaceLeft) { c = c+6; }
	    io.IOChar(x,Pos.y,Colour,-1,c);
	}


	/************************************************************************/
	/*																		*/
	/*								Draw the ball							*/
	/*																		*/
	/************************************************************************/

	private void _MADrawBall(Coord Pos)
	{
	    int x = xFlipped ? 255-Pos.x:Pos.x;
	    if (x < 0 || Pos.y < 0) { return; }
	    if (x >= 248 || Pos.y >= 184) { return; }
	    io.IOChar(x,Pos.y,Constants.COL_BLACK,-1,xFlipped ? 10 : 5);
	}


	/************************************************************************/
	/*																		*/
	/*						Find player nearest the ball					*/
	/*																		*/
	/************************************************************************/

	private int _MAFindNearestToBall(Coord Ball,Coord Players[],
	int PCount,int Dist)
	{
	    int x,y,d,Nearest = -1,NearDist = 999,Curr = 0;
	    Dist = 999;                                  /* Initialise distance */
	    while (PCount-- > 0)                          /* For all players */
	    {
	        x = Math.abs(Ball.x-Players[Curr].x);              /* Calc distance */
	        y = Math.abs(Ball.y-Players[Curr].y);
	        if (x < 16 && y < 16)                     /* Worth continuing ? */
	        {
	            d = (int)Math.sqrt(x*x+y*y);                    /* Actual distance */
	            if (d < NearDist)                     /* Best so far */
	            {
	                Dist = NearDist = d;
	                Nearest = Curr;
	            }
	        }
	        Curr++;
	    }
	    return Nearest;
	}


	/************************************************************************/
	/*																		*/
	/*					Play one highlighted match							*/
	/*																		*/
	/************************************************************************/

	void MAOneAttack(TeamInfo Attack,TeamInfo Defend,boolean FlipX,boolean SoundOn,int Speed)
	{
	    Coord Att[]=new Coord[4],Def[]=new Coord[4];
	    Coord Ball=new Coord(),BallDir=new Coord();
	    String _Temp,_Temp2;
	    int d1 = 0,d2=0,Moves,i,x,y,j;
		boolean c,OutOfPlay;
		boolean GoalScored;
	    int NoTouch;
	    long t;
	    TeamInfo tm;
	    xFlipped = FlipX;                             /* Save flip flag */
    	io.IOClear(Constants.COL_GREEN);
	    _MAPitch();                                   /* Draw the pitch */
	    io.IOCopy(io.COPYTOBB);   /* Copy into the back buffer */
	    for(int ii=0;ii<4;ii++) Att[ii]=new Coord();
	    for(int ii=0;ii<4;ii++) Def[ii]=new Coord();
	    Att[0].y = 3+(int)Math.floor(Math.random()*18);                       /* Initialise player with ball */
	    Att[0].x = 25+(int)Math.floor(Math.random()*6);
	    Ball.x = Att[0].x;Ball.y=Att[0].y;Att[0].x++;                     /* Initialise ball */
	    BallDir.x = -1;BallDir.y = 0;
	    Def[3].x = 11 + (int)Math.floor(Math.random()*2);                     /* Goalie position */
	    if (Att[0].y < 10) { Def[3].x += 2; }
	    if (Att[0].y > 13) { Def[3].x -= 2; }
	    Def[3].y = 23-Def[3].x;

	    for (i = 1;i < 3;i++)                         /* Other attackers */
	    {
	        do
	        {
	            Att[i].x = 10+(int)Math.floor(Math.random()*20);
	            Att[i].y = (int)Math.floor(Math.random()*21);
	            c = (Att[i].x+Att[i].y < 25);
	            if (Att[i].x > Att[0].x-4) { c = true; }
	            for (j = 0;j < i;j++)
	                if (Att[i].y == Att[j].y) { c = true; }
	        } while (c);
	    }
	    for (i = 0;i < 3;i++)                         /* Defenders */
	    {
	        do
	        {
	            Def[i].x = 10+(int)Math.floor(Math.random()*10);
	            Def[i].y = (int)Math.floor(Math.random()*18);
	            c = (Def[i].x+Def[i].y < 24 || Def[i].x > Att[0].x-4);
	            for (j = 0;j < 3;j++)
	            {
	                if (Def[i].y == Att[j].y) { c = true; }
	                if (i < j && Def[i].y == Def[j].y) { c = true; }
	            }
	        } while (c);
	    }
	    for (i = 0;i < 3;i++)                         /* Convert to pixel positions */
	    {
	        Att[i].x *= 8;Att[i].y *= 8;
	        Def[i].x *= 8;Def[i].y *= 8;
	    }
	    Ball.x *= 8;Ball.y *= 8;_MADrawBall(Ball);
	    Def[3].x *= 8;Def[3].y *= 8;
	    Moves = (int)Math.floor(Math.random()*3+1)*2-1;                     /* Number of character moves */
	    Moves *= 8;                                   /* Pixel moves */
	    t = io.IOClock();
	    while (Moves-- > 0)                           /* Move players forward */
	    {
	        t = t+(32*Speed/100);                     /* Sync */
	        Ball.x--;
	        for (i = 0;i < 3;i++)                     /* Move players */
	        {
	            x = Att[i].x-1;
	            y = Att[i].y;
	            if (i != 0)                           /* Non-ball players head to middle */
	            {
	                if (y < 80) { y++; }
	                if (y > 72) { y--; }
	            }
	            if (x+y > 25*8 || i == 0)             /* If legal, update positions */
	            {
	                Att[i].x = x;Att[i].y = y;
	            }
	            x = Def[i].x-1;
	            y = Def[i].y;
	            if (i != 0)                           /* Non-ball players head to middle */
	            {
	                if (y < 80) { y++; }
	                if (y > 72) { y--; }
	            }
	            if (x+y > 25*8)                       /* If legal, update positions */
	            {
	                Def[i].x = x;Def[i].y = y;
	            }
	        }
	        io.IOCopy(io.COPYFROMBB);                       /* Update Display */
	        _MADrawBall(Ball);
	        for (i = 0;i < 4;i++)
	        {
	            if (i < 3) { _MADrawPlayer(Att[i],Attack.Colour,xFlipped); }
	            _MADrawPlayer(Def[i],(i == 3) ? Constants.COL_GREEN:Defend.Colour,(!xFlipped));
	        }
	        drv_linux.HWUpdate();
	        while (io.IOClock() < t) {
	        	
	        }                   /* Wait for time out */
	    }
	    BallDir.y = 0;
	    BallDir.x = -2;
	    if (Ball.y < 9*8) { BallDir.y++; }
	    if (Ball.y > 13*8) { BallDir.y--; }
	    if (Ball.y >= 9*8 && Ball.y < 12*8) {
	        BallDir.y = ((int)Math.floor(Math.random()*3)) -1;
	    }
	    GoalScored = OutOfPlay = false;
	    if (SoundOn) { io.IOSound(0,0); }
	    NoTouch = 3;
	    //io.IOCopy(io.COPYTOBB);
	    while (!OutOfPlay)
	    {
	        t = t + (14*Speed/100);                   /* Sync */
	        Ball.x += BallDir.x;
	        Ball.y += BallDir.y;
	        if (Ball.x < 0 || Ball.y < 0              /* If out of bounds mark as such */
	            || Ball.x >= 31*8 || Ball.y >= 23*8)
	        {
	            Ball.x -= BallDir.x;
	            Ball.y -= BallDir.y;
	            OutOfPlay = true;
	        }
	        x = (Ball.x+Ball.y)/8;                    /* Check if goalie move */
	        if (BallDir.x < 0 && Ball.y != Def[3].y && Ball.y > 56 && x >= 24 && x < 28)
	        {
	            y = Def[3].y;
	            if (Ball.y < Def[3].y) { Def[3].y--; } else { Def[3].y++; }
	            if (Def[3].y < 64 || Def[3].y > 112) { Def[3].y = y; }
	            Def[3].x = 23*8-Def[3].y;
	        }
	/* Find nearest player */
	        _MAFindNearestToBall(Ball,Att,3,d1);
	        _MAFindNearestToBall(Ball,Def,4,d2);
	        d1 = d1 / 2;                              /* Scaling this affects effectiveness */
	        d2 = d2 * 9 / 4;
	        NoTouch--;
	        if ((d1 < 5 || d2 < 5) &&                 /* Player in range */
	            NoTouch < 0)
	        {
	            BallDir.y = (int)Math.floor(Math.random()*3)-1;               /* Reset ball dir */
	            NoTouch = 3;
	            BallDir.x = (d1 <= d2) ? -2:2;
	            if (SoundOn) { io.IOSound(0,0); }
	            if (d1 <= d2)                         /* If hit an attacker, aim */
	            {
	                BallDir.y = 0;
	                if (Ball.y < 9*8) { BallDir.y++; }
	                if (Ball.y > 13*8) { BallDir.y--; }
	                if ((Ball.y >= 9*8 && Ball.y < 12*8) || (int)Math.floor(Math.random()*6) == 0) {
	                    BallDir.y = (int)Math.floor(Math.random()*3) -1;
	                }
	            }
	        }
	        if (Ball.x+Ball.y < 170)                  /* Past goal line */
	        {
	            OutOfPlay = true;                        /* Out of play, goal scored ? */
	            GoalScored = (Ball.y > 58 && Ball.y < 108);
	        }
	        io.IOCopy(io.COPYFROMBB);                       /* Update Display */
	        _MADrawBall(Ball);
	        for (i = 0;i < 4;i++)
	        {
	            if (i < 3) { _MADrawPlayer(Att[i],Attack.Colour,xFlipped); }
	            _MADrawPlayer(Def[i],(i == 3) ? Constants.COL_GREEN:Defend.Colour,(!xFlipped));
	        }
	        drv_linux.HWUpdate();
	        while(io.IOClock()<t) {
	        }
	    }

	    if (GoalScored)                               /* Goal scored */
	    {
	        Attack.Score++;
	        drv_linux.HWDisplayGoal();
	        tm = (Attack.HomeTeam) ? Attack:Defend;
	        y = tm.Colour;
	        _Temp=" "+tm.Name+" "+tm.Score+" ";
	        tm = (Attack.HomeTeam) ? Defend:Attack;
	        _Temp2=tm.Name+" "+tm.Score+" ";
	        x = 128-(_Temp.length()+_Temp2.length())*4;
	        io.IOText(x,32,y,Constants.COL_GREY,_Temp);
	        x = x+_Temp.length()*8;
	        io.IOText(x,32,tm.Colour,Constants.COL_GREY,_Temp2);
	    }
	    else
	    {
	        _Temp=" No Goal! ";
	        io.IOText(-1,8,Constants.COL_BLUE,Constants.COL_GREY,_Temp);
	    }
	    if (GoalScored && SoundOn) { io.IOSound(10*Speed,1); }
	    drv_linux.HWUpdate();
	    t=io.IOClock();
	    t = t + 1000L * Speed / 100;                  /* Short delay */
	    while(io.IOClock()<t) {
	    }
	}
}
