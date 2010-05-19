package com.rgsoftworks.fm;

import com.rgsoftworks.fm.Game;
import com.rgsoftworks.fm.io;
import java.io.*;

public class Init {
	public static int mStartDivisionList=0;
	
	public static int ExtractInteger(String Line) {
		int returnValue=0;
		String newString="";
		int iLoop =0;
		
		while(Line.charAt(iLoop)>'0' && Line.charAt(iLoop)<'9') {
			newString+=Line.substring(iLoop, 1);
			iLoop++;
		}

		if(newString.length()>0)
			returnValue = Integer.parseInt(newString);
		else
			returnValue = 0;
		
		return returnValue;
	}
	
	public static final Game INITNewGame(Game g) throws RuntimeException, IOException
	{		
		int i,n,Mode = -1;
		
	    FileInputStream f=drv_linux.fm.OpenFile("game.dat");
		if(f==null) {
			byte data[] = drv_linux.fm.OpenAsset("game.dat");
			drv_linux.fm.SaveLocalFile("game.dat", data);

			f=drv_linux.fm.OpenFile("game.dat");
			if(f==null) return null;
		}
		
		DataInputStream in = new DataInputStream(f);
		
	    String Line="";
	    String Comment=";";
	    g.DivCount = g.PlayerCount = 0;             /* These will count the div/players */
	    g.Cash = 100000L;g.Loans = 0L;              /* Cash, Loans erased */
	    g.Skill = 3;                                 /* A default skill level */
	    try {
	    while (in.available()!=0)
	    {
	    	Line = in.readLine();
	    	while(Line.equals("") || Line.equals(" ") || Line.substring(0, 1).equals(";"))
	    		Line = in.readLine();
	        //while (Line != "" && Line.substring(Line.length()-1,1) <= ' ')
	        //    Line[strlen(Line)-1] = '\0';
	        if (Line.trim().equals(Comment)) { Line = ""; }
	        if (!Line.equals(""))
	        {
	            if (Line.substring(0,1).equals(":"))                     /* Mode switch, check players ? */
	            {
	                if (Line.equals(":Players"))
	                {
	                    Mode = -2;
	                }
	                else                              /* Check new division */
	                {
	                    Mode =  (g.DivCount++);
	                    g.Division[Mode] = new Division();
	                    g.Division[Mode].DivName = Line.substring(1);
	                    g.Division[Mode].NoTeams = 0;
	                }
	            }
	            else                                  /* Data, copy it in */
	            {
	                if (Mode == -2)                   /* A player */
	                {
	                	if(g.PlayerCount<Constants.MAXPLAYERS) {
		                    n = g.PlayerCount++;
		                    //Line = in.readLine();
		                    g.Player[n] = new Player();
		                    g.Player[n].Name = Line;
		                    g.Player[n].InOurTeam = false;
	                	}
	                }
	                else                              /* A team */
	                {
	                    n = g.Division[Mode].NoTeams++;
	                    //Line = in.readLine();
	                    g.Division[Mode].Teams[n] = new TeamInfo();
	                    g.Division[Mode].Teams[n].Name = Line.substring(1);
	                    g.Division[Mode].Teams[n].Colour = ExtractInteger(Line);
	                }
	            }
	        }
	    }
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    try {
			f.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    g.Div = g.DivCount - 1;                     /* Move no divisions to our div */

	    //for (n = 0;n < g.DivCount;n++)
	    //    MASSERT(g.Division[n].NoTeams % 2 == 0);

	    g.Division[g.Div].Team =  0;                /* Give us the first team */

	    for (i = 0;i < g.PlayerCount;i++)            /* Select positions */
	    {
	        g.Player[i].Pos = Constants.DEFENCE;
	        if (i >= g.PlayerCount/3) {
	            g.Player[i].Pos = (i >= 2*g.PlayerCount/3) ? Constants.ATTACK:Constants.MIDFIELD;
	        }
	    }
	    for (i = 0;i < 12;i++)                        /* Pick 12 players for us */
	    {
	        do                                        /* Look for nonselected player */
	        	n = (int) (Math.floor(Math.random()*(g.PlayerCount)));
	        while (g.Player[n].InOurTeam);
	        g.Player[n].InOurTeam = true;               /* part of our team now */
	        g.Player[n].Status =                     /* 11 selected, 1 reserve */
	            (i == 11) ? Constants.AVAILABLE:Constants.PICKED;
	    }
	    g.Score = g.Seasons = 0;                    /* No score or seasons */
	    g.Sound = true;g.Sacked = 0;
	    
	    return g;
	}


	/************************************************************************/
	/*																		*/
	/*						Initialise a new season							*/
	/*																		*/
	/************************************************************************/

	static void INITNewSeason(Game g)
	{
	    int n,i,j;
	    Division d = (g.Division[g.Div]);
	    TeamInfo t;
	    for (i = 0;i < g.DivCount;i++)
	    {
	        if (g.Div != i) {                         /* Set team id to -1 in other divs */
	            g.Division[i].Team = -1;
	        }
	        _INITShuffle((g.Division[i]));
	    }
	    d.FirstMatchHome = (Math.floor(Math.random()*2)==1);                 /* Next home ? */
	    d.Fixtures = d.NoTeams - 1;                 /* Number of league matches */
	    n = 0;
	    for (i = 0;i < d.Fixtures;i++)               /* Create the fixture list */
	    {
	        if (n == d.Team) { n++; }
	        d.FixtureList[i] = n++;
	    }
	    d.Played = 0;                                /* Played no fixtures */
	    for (i = 0;i < d.NoTeams;i++)                /* Erase each team */
	    {
	        t = (d.Teams[i]);
	        t.Won = t.Drawn = t.Lost =0;
	        t.GoalsFor = t.GoalsAgainst = 0;
	        t.Points = 0;
	    }

	    n = 9;                                        /* Insert cup matches */
	    while (--n >= 1)
	    {
	        j = (d.NoTeams-1)*n/8;                   /* Post to insert FA Cup Rounds */
	        i = d.Fixtures-1;                        /* Make space for it */
	        while (i >= j)
	        {
	            d.FixtureList[i+1] = d.FixtureList[i];
	            i--;
	        }
	        d.FixtureList[j] = -n;                   /* Insert the new fixture */
	        d.Fixtures++;
	    }
	    g.OutOfCup = false;                              /* Not out of the cup */
	    g.FinancialScaler =                          /* Financial multiplier */
	        g.DivCount-g.Div;
	    g.Morale = 10;                               /* Initial morale */
	    g.CurrentCrowd =                             /* Financial bases */
	        5000*g.FinancialScaler;
	    g.GroundRent = 500*g.FinancialScaler;

	    try {
	    for (i = 0;i < g.PlayerCount;i++)            /* Reset players detail */
	    {
	        g.Player[i].Value = 5000L*g.FinancialScaler*((long)Math.floor(Math.random()*5)+1);
	        g.Player[i].Skill = (int)(g.Player[i].Value/(5000*g.FinancialScaler));
	        g.Player[i].Energy = (int)Math.floor(Math.random()*20)+1;
	    }
	    } catch(Exception e) {
	    	System.err.print(e);
	    }
	    g.MoveCount = 2;                             /* Promoted/Relegated */
	    if (d.NoTeams > 19) { g.MoveCount = 3; }
	    if (d.NoTeams > 24) { g.MoveCount = 4; }
	}


	/************************************************************************/
	/*																		*/
	/*			Shuffle all the teams in the division, except ours			*/
	/*																		*/
	/************************************************************************/

	static void _INITShuffle(Division d)
	{
	    int i,n1,n2;
	    TeamInfo t;
	    for (i = 0;i < 100;i++)                       /* Lotsa shuffles */
	    {                                             /* Pick 2 teams, not ours */
	        do n1 = (int)Math.floor(Math.random()*(d.NoTeams)); while (n1 == d.Team);
	        do n2 = (int)Math.floor(Math.random()*(d.NoTeams)); while (n2 == d.Team);
	        t = d.Teams[n1];                         /* Swap them */
	        d.Teams[n1] = d.Teams[n2];
	        d.Teams[n2] = t;
	    }
	    i++;
	}


	/************************************************************************/
	/*																		*/
	/*						Display the fixture list						*/
	/*																		*/
	/************************************************************************/

	static void INITFixtureList(Game g)
	{
	    int i,c,x,y;
		boolean h;
	    String _Temp;
	    Division d = (g.Division[g.Div]);
	    io.IOClear(Constants.COL_BLACK);
	    io.IOText(-1,12,Constants.COL_YELLOW,Constants.COL_RED," Fixture List ");
	    h = d.FirstMatchHome;
	    try {
		    for (i = 0;i < d.Fixtures;i++)
		    {
		        x = (i < (d.Fixtures+1)/2) ? 1:17;
		        y = (i % ((d.Fixtures+1)/2)) + 4;
		        x = x* 8-4;y *= 8;
		        //sprintf(_Temp,"%d",i+1);
		        _Temp = Integer.toString(i+1);
		        io.IOText(x,y,Constants.COL_YELLOW,Constants.COL_BLACK,_Temp);
		        if (d.FixtureList[i] >= 0)
		        {
		            _Temp=d.Teams[d.FixtureList[i]].Name+" "+(h ? 'H':'A');
		            h = (!h);
		        }
		        else
		        {
		            switch(-d.FixtureList[i])
		            {
		                case 7: _Temp="FA Cup Semis";break;
		                case 8: _Temp="FA Cup Final";break;
		                default: _Temp="FA Cup R"+Integer.toString(d.FixtureList[i]);break;
		            }
		            if (g.OutOfCup) { _Temp="No match"; }
		        }
		        c = (d.Played == i) ? Constants.COL_CYAN:Constants.COL_GREEN;
		        if (i < d.Played) { c = Constants.COL_BLUE; }
		        io.IOText(x+24,y,c,Constants.COL_BLACK,_Temp);
		    }
	    } catch(Exception e) {
	    	System.err.print(e);
	    }
		io.IOText(-1, 180, Constants.COL_YELLOW, 0, "Press DPad Center/Touch Screen");
	}

	/************************************************************************/
	/*																		*/
	/*						Generate the sort array							*/
	/*																		*/
	/************************************************************************/

	static void INITGenerateSortArray(Game g)
	{
	    int n1,n2,Redo,i;
		boolean Swap;
	    Division d = (g.Division[g.Div]);
	    for (i = 0;i < d.NoTeams;i++)                /* Set sort index, calc points */
	    {
	        d.SortIndex[i] = i;
	        d.Teams[i].Points =                      /* Points first, then gd, then goals */
	            d.Teams[i].Won * 3 + d.Teams[i].Drawn;
	        d.Teams[i].SortSc = (long)(d.Teams[i].Points) * 1000 + 500;
	        d.Teams[i].SortSc += d.Teams[i].GoalsFor - d.Teams[i].GoalsAgainst;
	        d.Teams[i].SortSc = (d.Teams[i].SortSc*1000)+d.Teams[i].GoalsFor;
	    }
	    do                                            /* Bubble sort the SortIndex array */
	    {
	        Redo = 0;
	        for (i = 0;i < d.NoTeams-1;i++)
	        {
	            n1 = d.SortIndex[i];n2 = d.SortIndex[i+1];
	            Swap = (d.Teams[n1].SortSc < d.Teams[n2].SortSc);
	            if (d.Teams[n1].SortSc == d.Teams[n2].SortSc) {
	                Swap = d.Teams[n1].Name.equals(d.Teams[n2].Name);
	            }
	            if (Swap)
	            {
	                Redo = d.SortIndex[i];
	                d.SortIndex[i] = d.SortIndex[i+1];
	                d.SortIndex[i+1] = Redo;
	                Redo = 1;
	            }
	        }
	    } while (Redo != 0);
	    for (i = 0;i < d.NoTeams;i++)
	    {
	        d.Teams[d.SortIndex[i]].LeaguePos = i+1;
	    }
	}

	static void INITPressEnter() {
		drv_linux.setKeyPress(0);
		io.IOText(-1, 180, Constants.COL_YELLOW, 0, "Press DPad Center/Touch Screen");
		while(drv_linux.getKeyPress()==0) {
		}
	}
}
