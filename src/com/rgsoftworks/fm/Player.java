package com.rgsoftworks.fm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class Player implements java.io.Serializable                           /* Player record */
{
	private static final long serialVersionUID = 1L;
	public String Name;                                /* Players name */
    public int Status;                          /* Status */
    public int Pos;                            /* Position */
    public boolean  InOurTeam;                               /* Is player in our team ? */
    public int  Energy,Skill;                            /* Energy and skill */
    public long Value;                                   /* Current value */
    public Player() {
    	
    }
    public String toXML(String tabout) {
    	String xmlstring="";
    	
    	xmlstring=tabout+"<Player>\n";
    	xmlstring+=tabout+"   <Name>"+Name+"</Name>\n";
    	xmlstring+=tabout+"   <Status>"+Integer.toString(Status)+"</Status>\n";
    	xmlstring+=tabout+"   <Pos>"+Integer.toString(Pos)+"</Pos>\n";
    	xmlstring+=tabout+"   <InOurTeam>"+Boolean.toString(InOurTeam)+"</InOurTeam>\n";
    	xmlstring+=tabout+"   <Energy>"+Integer.toString(Energy)+"</Energy>\n";
    	xmlstring+=tabout+"   <Skill>"+Integer.toString(Skill)+"</Skill>\n";
    	xmlstring+=tabout+"   <Value>"+Long.toString(Value)+"</Value>\n";
    	xmlstring+=tabout+"</Player>\n";
    	return xmlstring;
    }
    public void writeToOutputStream(DataOutputStream os) throws IOException {
    	os.writeInt(Status);                                   /* Our current division */
    	os.writeInt(Pos);                           /* Number of players */
    	os.writeInt(Energy);                              /* Number of divisions */
    	os.writeInt(Skill);
    	os.writeLong(Value);                                  /* Money we have */
    	os.writeBoolean(InOurTeam);                                 /* Current loans */
    }
    public void readFromInputStream(DataInputStream os) throws IOException {
    	Status=os.readInt();                                   /* Our current division */
    	Pos=os.readInt();                           /* Number of players */
    	Energy=os.readInt();                              /* Number of divisions */
    	Skill=os.readInt();
    	Value=os.readLong();                                  /* Money we have */
    	InOurTeam=os.readBoolean();                                 /* Current loans */
    }
}