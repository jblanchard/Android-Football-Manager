package com.rgsoftworks.fm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class TeamInfo                          /* Team basic information */
{
    public String Name;                                /* Teams name */
    public int  Won,Drawn,Lost;                          /* Table information */
    public int  GoalsFor,GoalsAgainst;                   /* Goals scored etc. */
    public int  Points;                                  /* Points total */
    public int  Colour;                                  /* Teams shirt colour */
    public int  Score;                                   /* Teams score this match */
    public long SortSc;                                  /* Sorting score */
    public boolean  HomeTeam;                                /* Home team this match ? */
    public int  LeaguePos;                               /* League Position */
    public String toXML(String tabout) {
    	String xmlstring="";
    	
    	xmlstring =tabout+"<Team>\n";
    	xmlstring+=tabout+"    <Name>"+Name+"</Name>\n";
    	xmlstring+=tabout+"    <Drawn>"+Integer.toString(Drawn)+"</Drawn>\n";
    	xmlstring+=tabout+"    <Lost>"+Integer.toString(Lost)+"</Lost>\n";
    	xmlstring+=tabout+"    <GoalsFor>"+Integer.toString(GoalsFor)+"</GoalsFor>\n";
    	xmlstring+=tabout+"    <GoalsAgainst>"+Integer.toString(GoalsAgainst)+"</GoalsAgainst>\n";
    	xmlstring+=tabout+"    <Points>"+Integer.toString(Points)+"</Points>\n";
    	xmlstring+=tabout+"    <Colour>"+Integer.toString(Colour)+"</Colour>\n";
    	xmlstring+=tabout+"    <Score>"+Integer.toString(Score)+"</Score>\n";
    	xmlstring+=tabout+"    <SortSc>"+Long.toString(SortSc)+"</SortSc>\n";
    	xmlstring+=tabout+"    <HomeTeam>"+Boolean.toString(HomeTeam)+"</HomeTeam>\n";
    	xmlstring+=tabout+"    <Won>"+Integer.toString(Won)+"</Won>\n";
    	xmlstring+=tabout+"    <LeaguePos>"+Integer.toString(LeaguePos)+"</LeaguePos>\n";
    	xmlstring+=tabout+"</Team>\n";
    	return xmlstring;
    }
    public void writeToOutputStream(DataOutputStream os) throws IOException {
    	os.writeInt(Name.length());
    	os.writeChars(Name);
    	os.writeInt(Won);                                   /* Our current division */
    	os.writeInt(Drawn);                           /* Number of players */
    	os.writeInt(Lost);                              /* Number of divisions */
    	os.writeInt(GoalsFor);
    	os.writeInt(GoalsAgainst);                                  /* Money we have */
    	os.writeInt(Points);                                  /* Money we have */
    	os.writeInt(Colour);                                  /* Money we have */
    	os.writeInt(Score);                                  /* Money we have */
    	os.writeLong(SortSc);                                  /* Money we have */
    	os.writeInt(LeaguePos);                                  /* Money we have */
    	os.writeBoolean(HomeTeam);                                 /* Current loans */
    }
    public void readFromInputStream(DataInputStream os) throws IOException {
    	int NameLength=os.readInt();
    	Name="";
    	for(int i=0;i<NameLength;i++) {
    		Name+=os.readChar();
    	}
    	Won=os.readInt();                                   /* Our current division */
    	Drawn=os.readInt();                           /* Number of players */
    	Lost=os.readInt();                              /* Number of divisions */
    	GoalsFor=os.readInt();
    	GoalsAgainst=os.readInt();                                  /* Money we have */
    	Points=os.readInt();                                  /* Money we have */
    	Colour=os.readInt();                                  /* Money we have */
    	Score=os.readInt();                                  /* Money we have */
    	SortSc=os.readLong();                                  /* Money we have */
    	LeaguePos=os.readInt();                                  /* Money we have */
    	HomeTeam=os.readBoolean();                                 /* Current loans */
    }
}
