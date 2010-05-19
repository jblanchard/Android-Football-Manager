package com.rgsoftworks.fm;

public class Eos {
	public void EOSEndSeason(Game g)
	{
	    Division d = (g.Division[g.Div]);
	    String _Temp,_Name;
	    int i,j,ODiv,OPos;
	    long n;

	    if (d.Played<(d.Fixtures-1)) { return; }            /* Not completed the season */

	    io.IOClear(Constants.COL_BLACK);                           /* End of season message */
	    io.IOText(-1,8,Constants.COL_WHITE,Constants.COL_RED," End of Season ");

	    ODiv = g.Div;                                /* Remember performance */
	    OPos = d.Teams[d.Team].LeaguePos;
	    _Name = d.Teams[d.Team].Name;

	    g.Seasons++;                                 /* One more season */
	    n = (d.NoTeams-OPos+1) +                     /* Calculate new score */
	        g.LastCupRound + 2 * g.Div;
	    g.Score = g.Score + n;                      /* Add to overall score */
	    n = n * g.FinancialScaler * 5000;            /* Make it a cash bonus */
	    g.Cash += n;                                 /* Add it on */
	    _Temp = "Cash Bonus " + (char)96 + Double.toString(n);
	    io.IOText(-1,88,Constants.COL_YELLOW,Constants.COL_BLACK,_Temp);
	    Init.INITPressEnter();
	    io.IOClear(Constants.COL_BLACK);
	    drv_linux.fm.getGameMenu().INITDisplayDivision(g);                       /* Display the Division */

	    for (i = 0;i < g.DivCount;i++)               /* Use the scores as "already sel." */
	        for (j = 0;j < g.Division[i].NoTeams;j++)
	    {
	        g.Division[i].Teams[j].Score = 0;
	        g.Division[i].Teams[j].HomeTeam = false;
	    }

	    d.Teams[d.Team].HomeTeam = true;               /* Mark us */
	    io.IOClear(Constants.COL_BLACK);                           /* Display promoted/relegated */
	    _Temp=" " + d.DivName + " ";
	    io.IOText(-1,8,Constants.COL_YELLOW,Constants.COL_RED,_Temp);
	    for (i = 0;i < d.NoTeams;i++)                /* Work through the teams */
	    {
	        if ((d.Teams[i].LeaguePos <= g.MoveCount && g.Div != 0) ||
	            d.Teams[i].LeaguePos == 1)
	        {
	            _Temp=d.Teams[i].Name;
	            if (d.Teams[i].LeaguePos == 1) {
	                _Temp=_Temp+" are champions";
	            } else {
	                _Temp=_Temp+" are promoted";
	            }
	            io.IOText(-1,32+d.Teams[i].LeaguePos*12,Constants.COL_YELLOW,Constants.COL_BLACK,_Temp);
	            if (g.Div > 0) {                      /* Promote one team */
	                _EOSSwapTeam(g,(d.Teams[i]),g.Div-1);
	            }
	        }
	        if (g.Div < g.DivCount-1 &&
	            d.Teams[i].LeaguePos > d.NoTeams-g.MoveCount)
	        {
	            _Temp=d.Teams[i].Name + " are relegated";
	            io.IOText(-1,102+(d.NoTeams-d.Teams[i].LeaguePos)*12,
	                Constants.COL_RED,Constants.COL_BLACK,_Temp);
	            _EOSSwapTeam(g,                       /* Relegate one team */
	                (d.Teams[i]),g.Div+1);
	        }
	    }
	    Init.INITPressEnter();

	    if (ODiv == 0 && OPos == 1)                   /* Check for league champs */
	    {
	        io.IOClear(Constants.COL_BLACK);
	        _Temp=" *** " + _Name +" are league champions *** ";
	        io.IOText(-1,88,Constants.COL_WHITE,Constants.COL_RED,_Temp);
	        Init.INITPressEnter();
	    }

	    for (i = 0;i < g.DivCount;i++)               /* Find us now */
	        for (j = 0;j < g.Division[i].NoTeams;j++)
	            if (g.Division[i].Teams[j].HomeTeam)
	            {
	                g.Div = i;
	                g.Division[i].Team = j;
	            }
	    Init.INITNewSeason(g);                             /* Start a new season */
	}


	/************************************************************************/
	/*																		*/
	/*			Swap given team with team in other Division					*/
	/*																		*/
	/************************************************************************/

	public void _EOSSwapTeam(Game g,TeamInfo tMove,int NewDiv)
	{
	    Division nd = (g.Division[NewDiv]);
	    TeamInfo t,ts;
	    int n;
	    do                                            /* Find one not already done ! */
	    n = (int)Math.floor(Math.random() * nd.NoTeams);
	    while (nd.Teams[n].Score != 0);
	    t = (nd.Teams[n]);                          /* Get pointer to it */
	    ts = t;nd.Teams[n] = tMove;g.Division[g.Div].Teams[tMove.LeaguePos] = ts;              /* Swap them */
	    t.Score = tMove.Score = 1;                  /* Stop any more swapping */
	}
}
