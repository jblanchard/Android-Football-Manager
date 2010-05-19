package com.rgsoftworks.fm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.rgsoftworks.fm.Constants;
import com.rgsoftworks.fm.TeamInfo;

public final class Division implements java.io.Serializable                         /* Division, not sorted by pts */
{
	private static final long serialVersionUID = 1L;
    int  Played;                                  /* Matches played in this division */
    int  NoTeams;                                 /* Teams in this division */
    int  Team;                                    /* Index of our team */
    String DivName;                             /* Name of Division */
    TeamInfo[] Teams = new TeamInfo[Constants.MAXTEAMS];                     /* Teams, up to 24 in division */
    int  SortIndex[] = new int[Constants.MAXTEAMS];                     /* Sorting array */
    int  Fixtures;                                /* Number of fixtures for our team */
    int  FixtureList[] = new int[Constants.MAXTEAMS+12];                /* Fixtures */
    boolean  FirstMatchHome;                          /* Next match at home ? */

    public String toXML(String tabout) {
    	String xmlstring="";
    	
    	xmlstring=tabout+"<Division>\n";
    	xmlstring+=tabout+"    <Played>"+Integer.toString(Played)+"</Played>\n";
    	xmlstring+=tabout+"    <NoTeams>"+Integer.toString(NoTeams)+"</NoTeams>\n";
    	xmlstring+=tabout+"    <Team>"+Integer.toString(Team)+"</Team>\n";
    	xmlstring+=tabout+"    <DivName>"+DivName+"</DivName>\n";
    	xmlstring+=tabout+"    <Fixtures>"+Integer.toString(Fixtures)+"</Fixtures>\n";
    	xmlstring+=tabout+"    <FirstMatchHome>"+Boolean.toString(FirstMatchHome)+"</FirstMatchHome>\n";
    	for(int i=0;i<Constants.MAXTEAMS;i++) {
    		if(Teams[i]==null) {
    			i=Constants.MAXTEAMS;
    		} else {
    			xmlstring+=Teams[i].toXML(tabout+"    ");
    		}
    	}
    	xmlstring+=tabout+"    <SortIndexes>\n";
    	for(int i=0;i<Constants.MAXTEAMS;i++) {
    		xmlstring+=tabout+"        <SortIndex>"+Integer.toString(SortIndex[i])+"</SortIndex>\n";
    	}
    	xmlstring+=tabout+"    </SortIndexes>\n";
    	xmlstring+=tabout+"    <FixtureList>\n";
    	for(int i=0;i<Constants.MAXTEAMS+12;i++) {
    		xmlstring+=tabout+"        <Fixture>"+Integer.toString(FixtureList[i])+"</Fixture>\n";
    	}
    	xmlstring+=tabout+"    </FixtureList>\n";
    	xmlstring+=tabout+"</Division>\n";
    	return xmlstring;
    }
    public void writeToOutputStream(DataOutputStream dos) throws IOException {
    	dos.writeInt(Played);
    	dos.writeInt(NoTeams);                                   /* Our current division */
    	dos.writeInt(Team);                           /* Number of players */
    	dos.writeInt(Fixtures);                                  /* Money we have */
    	dos.writeBoolean(FirstMatchHome);                                 /* Current loans */

    	for(int i=0;i<Constants.MAXTEAMS;i++) {
    		dos.writeInt(SortIndex[i]);
    	}
    	for(int i=0;i<Constants.MAXTEAMS+12;i++) {
    		dos.writeInt(FixtureList[i]);
    	}

    	for(int i=0;i<Constants.MAXTEAMS;i++) {
    		if(Teams[i]!=null) {
    			Teams[i].writeToOutputStream(dos);
    		}
    	}
    }
    public void readFromInputStream(DataInputStream os) throws IOException {
    	Played=os.readInt();    	
    	NoTeams=os.readInt();                                   /* Our current division */
    	Team=os.readInt();
    	if(Team==255) { Team=-1; }
    	Fixtures=os.readInt();
    	FirstMatchHome=os.readBoolean();

    	for(int i=0;i<Constants.MAXTEAMS;i++) {
    		SortIndex[i]=os.readInt();
    	}

    	for(int i=0;i<Constants.MAXTEAMS+12;i++) {
    		FixtureList[i]=os.readInt();
    	}

    	for(int i=0;i<Constants.MAXTEAMS;i++) {
    		if(Teams[i]!=null) {
    			Teams[i].readFromInputStream(os);
    		}
    	}
    }
}
