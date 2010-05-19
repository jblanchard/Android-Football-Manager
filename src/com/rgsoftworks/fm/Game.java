package com.rgsoftworks.fm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.rgsoftworks.fm.Constants;
import com.rgsoftworks.fm.Player;
import com.rgsoftworks.fm.Division;

public final class Game implements java.io.Serializable
{
	static final long serialVersionUID=1;
	public Player Player[] = new Player[Constants.MAXPLAYERS];                    /* Players */
    public Division Division[] = new Division[Constants.DIVISIONS];                 /* Divisions */
    public int    Div;                                   /* Our current division */
    public int    PlayerCount;                           /* Number of players */
    public int    DivCount;                              /* Number of divisions */
    public long   Cash;                                  /* Money we have */
    public long   Loans;                                 /* Current loans */
    public int    Skill=-1;                                 /* Skill level */
    public boolean    OutOfCup;                              /* Out of cup flag */
    public int    FinancialScaler;                       /* Finance multiplier */
    public int    Morale;                                /* Morale */
    public long   Score,Seasons;                         /* Scoring */
    public long   GroundRent;                            /* Ground Rent */
    public boolean    Sound;                                 /* Sound on ? */
    public long   Receipts;                              /* Receipts */
    public long   CurrentCrowd;                          /* Current Crowd */
    public int    Picked,Injured,Available;              /* Counters for team */
    public int    MoveCount;                             /* Number of up/down movers */
    public int    LastCupRound;                          /* Last cup round played in */
    public int    Sacked;                                /* Sacked Flag */
    public int	  TeamNo=-1;								 /* Just so we know we selected one */
    public String toXML() {
    	String xmlstring="";
    	xmlstring ="<SaveGame>\n";
    	xmlstring+="    <Div>"+Integer.toString(Div)+"</Div>\n";
    	xmlstring+="    <Cash>"+Long.toString(Cash)+"</Cash>\n";
    	xmlstring+="    <Loans>"+Long.toString(Loans)+"</Loans>\n";
    	xmlstring+="    <Skill>"+Integer.toString(Skill)+"</Skill>\n";
    	xmlstring+="    <OutOfCup>"+Boolean.toString(OutOfCup)+"</OutOfCup>\n";
    	xmlstring+="    <FinancialScaler>"+Integer.toString(FinancialScaler)+"</FinancialScaler>\n";
    	xmlstring+="    <Morale>"+Integer.toString(Morale)+"</Morale>\n";
    	xmlstring+="    <Score>"+Long.toString(Score)+"</Score>\n";
    	xmlstring+="    <Seasons>"+Long.toString(Seasons)+"</Seasons>\n";
    	xmlstring+="    <GroundRent>"+Long.toString(GroundRent)+"</GroundRent>\n";
    	xmlstring+="    <Sound>"+Boolean.toString(Sound)+"</Sound>\n";
    	xmlstring+="    <Receipts>"+Long.toString(Receipts)+"</Receipts>\n";
    	xmlstring+="    <CurrentCrowd>"+Long.toString(CurrentCrowd)+"</CurrentCrowd>\n";
    	xmlstring+="    <Picked>"+Integer.toString(Picked)+"</Picked>\n";
    	xmlstring+="    <Injured>"+Integer.toString(Injured)+"</Injured>\n";
    	xmlstring+="    <Available>"+Integer.toString(Available)+"</Available>\n";
    	xmlstring+="    <MoveCount>"+Integer.toString(MoveCount)+"</MoveCount>\n";
    	xmlstring+="    <LastCupRound>"+Integer.toString(LastCupRound)+"</LastCupRound>\n";
    	xmlstring+="    <Sacked>"+Integer.toString(Sacked)+"</Sacked>\n";
    	xmlstring+="    <TeamNo>"+Integer.toString(TeamNo)+"</TeamNo>\n";
    	xmlstring+="    <PlayerCount>"+Integer.toString(PlayerCount)+"</PlayerCount>\n";
    	xmlstring+="    <Players>\n";
    	for(int i=0;i<PlayerCount;i++) {
    		if(Player[i]==null) {
    			i=PlayerCount;
    		} else {
    			xmlstring+=Player[i].toXML("        ");
    		}
    	}
    	xmlstring+="    </Players>\n";
    	xmlstring+="    <DivCount>"+Integer.toString(DivCount)+"</DivCount>\n";
    	xmlstring+="    <Divisions>\n";
    	for(int i=0;i<DivCount;i++) {
    		if(Division[i]==null) {
    			i=DivCount;
    		} else {
    			xmlstring+=Division[i].toXML("        ");
    		}
    	}
    	xmlstring+="    </Divisions>\n";
    	xmlstring+="</SaveGame>";
    	return xmlstring;
    }
    
    public byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }

    public int byteArrayToInt(byte [] b) {
        return (b[0] << 24)
                + ((b[1] & 0xFF) << 16)
                + ((b[2] & 0xFF) << 8)
                + (b[3] & 0xFF);
    }

    public void writeToOutputStream(OutputStream os) throws IOException {
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();  
    	DataOutputStream dos = new DataOutputStream(bos);  

    	dos.writeInt(Available);
    	dos.writeInt(Div);                                   // Our current division 
    	dos.writeInt(PlayerCount);                           // Number of players 
    	dos.writeInt(DivCount);                              // Number of divisions 
    	dos.writeLong(Cash);                                  // Money we have 
    	dos.writeLong(Loans);                                 // Current loans 
    	dos.writeInt(Skill);                                 // Skill level 
    	dos.writeBoolean(OutOfCup);                              // Out of cup flag 
    	dos.writeInt(FinancialScaler);                       // Finance multiplier 
    	dos.writeInt(Morale);                                // Morale 
    	dos.writeLong(Score);
    	dos.writeLong(Seasons);                         // Scoring 
    	dos.writeLong(GroundRent);                            // Ground Rent 
    	dos.writeBoolean(Sound);                                 // Sound on ? 
    	dos.writeLong(Receipts);                              // Receipts 
    	dos.writeLong(CurrentCrowd);                          // Current Crowd 
    	dos.writeInt(Picked);
    	dos.writeInt(Injured);
    	dos.writeInt(MoveCount);                             // Number of up/down movers 
    	dos.writeInt(LastCupRound);                          // Last cup round played in 
    	dos.writeInt(Sacked);                                // Sacked Flag 
    	dos.writeInt(TeamNo);								 // Just so we know we selected one 

    	int playercount=0;
    	
    	for(int i=0;i<PlayerCount;i++) {
    		if(Player[i]!=null) {
    			playercount+=1;
    		} else {
    			break;
    		}
    	}
    	int divcount=0;
    	
    	for(int i=0;i<DivCount;i++) {
    		if(Division[i]!=null) {
    			divcount+=1;
    		} else {
    			break;
    		}
    	}
    	
    	dos.writeInt(playercount);
    	for(int i=0;i<PlayerCount;i++) {
    		if(Player[i]!=null) {
    			Player[i].writeToOutputStream(dos);
    		}
    	}

    	dos.writeInt(divcount);
    	for(int i=0;i<DivCount;i++) {
    		Division[i].writeToOutputStream(dos);
    	}

    	dos.flush();  
    	byte[] data = bos.toByteArray(); 

    	int dlen=data.length;
    	
    	os.write(intToByteArray(dlen));
    	os.write(data);
    }
    
    public void readFromInputStream(InputStream os) throws IOException {
    	byte[] byteint=new byte[4];
    	
    	os.read(byteint);
    	int arrayLength=byteArrayToInt(byteint);
    	
    	byte[] data=new byte[arrayLength];
    	int playercount=0;
    	int divcount=0;
    	
    	os.read(data);
    	
    	ByteArrayInputStream bos = new ByteArrayInputStream(data);  
    	DataInputStream dos = new DataInputStream(bos);  

    	Available=dos.readInt();
    	Div=dos.readInt();                                   // Our current division 
    	PlayerCount=dos.readInt();                           // Number of players 
    	DivCount=dos.readInt();                              // Number of divisions 
    	Cash=dos.readLong();                                  // Money we have 
    	Loans=dos.readLong();                                 // Current loans 
    	Skill=dos.readInt();                                 // Skill level 
    	OutOfCup=dos.readBoolean();                              // Out of cup flag 
    	FinancialScaler=dos.readInt();                       // Finance multiplier 
    	Morale=dos.readInt();                                // Morale 
    	Score=dos.readLong();
    	Seasons=dos.readLong();                         // Scoring 
    	GroundRent=dos.readLong();                            // Ground Rent 
    	Sound=dos.readBoolean();                                 // Sound on ? 
    	Receipts=dos.readLong();                              // Receipts 
    	CurrentCrowd=dos.readLong();                          // Current Crowd 
    	Picked=dos.readInt();
    	Injured=dos.readInt();
    	MoveCount=dos.readInt();                             // Number of up/down movers 
    	LastCupRound=dos.readInt();                          // Last cup round played in 
    	Sacked=dos.readInt();                                // Sacked Flag 
    	TeamNo=dos.readInt();								 // Just so we know we selected one 

    	playercount=dos.readInt();
    	for(int i=0;i<playercount;i++) {
    		if(Player[i]==null) {
    			Player[i]=new Player();
    		}
			Player[i].readFromInputStream(dos);
    	}
    	
    	divcount=dos.readInt();
    	for(int i=0;i<divcount;i++) {
    		if(Division[i]==null) {
    			Division[i]=new Division();
    		}
			Division[i].readFromInputStream(dos);
    	}
    }
}
