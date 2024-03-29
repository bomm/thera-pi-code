package opRgaf;




import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jdesktop.swingworker.SwingWorker;

import CommonTools.INIFile;
import CommonTools.SqlInfo;
import CommonTools.StartOOApplication;
import CommonTools.Verschluesseln;
import RehaIO.RehaIOMessages;
import RehaIO.RehaReverseServer;
import RehaIO.SocketClient;
import ag.ion.bion.officelayer.application.IOfficeApplication;
import ag.ion.bion.officelayer.application.OfficeApplicationException;




public class OpRgaf implements WindowListener{

	/**
	 * @param args
	 */
	public final Cursor wartenCursor = new Cursor(Cursor.WAIT_CURSOR);
	public final Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
	public final Cursor kreuzCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
	public final Cursor cmove = new Cursor(Cursor.MOVE_CURSOR);  //  @jve:decl-index=0:
	public final Cursor cnsize = new Cursor(Cursor.N_RESIZE_CURSOR);  //  @jve:decl-index=0:
	public final Cursor cnwsize = new Cursor(Cursor.NW_RESIZE_CURSOR);  //  @jve:decl-index=0:
	public final Cursor cnesize = new Cursor(Cursor.NE_RESIZE_CURSOR);  //  @jve:decl-index=0:
	public final Cursor cswsize = new Cursor(Cursor.SW_RESIZE_CURSOR);  //  @jve:decl-index=0:
	public final Cursor cwsize = new Cursor(Cursor.W_RESIZE_CURSOR);  //  @jve:decl-index=0:
	public final Cursor csesize = new Cursor(Cursor.SE_RESIZE_CURSOR);  //  @jve:decl-index=0:
	public final Cursor cssize = new Cursor(Cursor.S_RESIZE_CURSOR);  //  @jve:decl-index=0:
	public final Cursor cesize = new Cursor(Cursor.E_RESIZE_CURSOR);  //  @jve:decl-index=0:	
	public final Cursor cdefault = new Cursor(Cursor.DEFAULT_CURSOR);  //  @jve:decl-index=0:	

	public static boolean DbOk;
	JFrame jFrame;
	public static JFrame thisFrame = null;
	public Connection conn;
	public static OpRgaf thisClass;
	
	public static IOfficeApplication officeapplication;
	
	public String dieseMaschine = null;
	/*
	public static String dbIpAndName = null;
	public static String dbUser = null;
	public static String dbPassword = null;
	
	
*/

	public static String dbIpAndName = "jdbc:mysql://192.168.2.2:3306/rtadaten";
	public static String dbUser = "rtauser";
	public static String dbPassword = "rtacurie";
	public static String officeProgrammPfad = "C:/Program Files (x86)/LibreOffice 3";
	public static String officeNativePfad = "C:/RehaVerwaltung/Libraries/lib/openofficeorg/";
	public static String progHome = "C:/RehaVerwaltung/";
	public static String aktIK = "510841109";

	public static HashMap<String,Object> mahnParameter = new HashMap<String,Object>();
	
	public static HashMap<String,String> hmAbrechnung = new HashMap<String,String>();
	public static HashMap<String,String> hmFirmenDaten  = null;
	public static HashMap<String,String> hmAdrPDaten = new HashMap<String,String>();
	/*
	public static String hmRechnungPrivat = "C:/RehaVerwaltung/vorlagen/HMRechnungPrivat.ott.Kopie.ott";
	public static String hmRechnungKasse = "C:/RehaVerwaltung/vorlagen/HMRechnungPrivat.ott.Kopie.ott";
	public static String rhRechnungPrivat = "C:/RehaVerwaltung/vorlagen/HMRechnungPrivat.ott.Kopie.ott";
	public static String rhRechnungKasse = "C:/RehaVerwaltung/vorlagen/HMRechnungPrivat.ott.Kopie.ott";
	*/
	
	public static boolean testcase = false;
	public OpRgafTab otab = null;

	public static int xport = -1;
	public static boolean xportOk = false;
	public RehaReverseServer rehaReverseServer = null;
	public static int rehaReversePort = -1;
	public SqlInfo sqlInfo;
	
	public static void main(String[] args) {
		OpRgaf application = new OpRgaf();
		application.getInstance();
		application.getInstance().sqlInfo = new SqlInfo();
		if(args.length > 0 || testcase){
			if(!testcase){
				System.out.println("hole daten aus INI-Datei "+args[0]);
				INIFile inif = new INIFile(args[0]+"ini/"+args[1]+"/rehajava.ini");
				dbIpAndName = inif.getStringProperty("DatenBank","DBKontakt1");
				dbUser = inif.getStringProperty("DatenBank","DBBenutzer1");
				String pw = inif.getStringProperty("DatenBank","DBPasswort1");
				String decrypted = null;
				if(pw != null){
					Verschluesseln man = Verschluesseln.getInstance();
					man.init(Verschluesseln.getPassword().toCharArray(), man.getSalt(), man.getIterations());
					decrypted = man.decrypt (pw);
				}else{
					decrypted = new String("");
				}
				dbPassword = decrypted.toString();
				inif = new INIFile(args[0]+"ini/"+args[1]+"/rehajava.ini");
				officeProgrammPfad = inif.getStringProperty("OpenOffice.org","OfficePfad");
				officeNativePfad = inif.getStringProperty("OpenOffice.org","OfficeNativePfad");
				progHome = args[0];
				aktIK = args[1];
				
				inif = new INIFile(args[0]+"ini/"+args[1]+"/oprgaf.ini");
				mahnParameter.put("frist1", (Integer) inif.getIntegerProperty("General","TageBisMahnung1") );
				mahnParameter.put("frist2", (Integer) inif.getIntegerProperty("General","TageBisMahnung2") );
				mahnParameter.put("frist3", (Integer) inif.getIntegerProperty("General","TageBisMahnung3") );
				mahnParameter.put("einzelmahnung", (Boolean) (inif.getIntegerProperty("General","EinzelMahnung").equals("1") ? Boolean.TRUE : Boolean.FALSE) );
				mahnParameter.put("drucker", (String) inif.getStringProperty("General","MahnungDrucker") );
				mahnParameter.put("exemplare", (Integer) inif.getIntegerProperty("General","MahnungExemplare") );
				mahnParameter.put("inofficestarten", (Boolean) (inif.getIntegerProperty("General","InOfficeStarten").equals("1") ? Boolean.TRUE : Boolean.FALSE) );
				mahnParameter.put("erstsuchenab", (String) inif.getStringProperty("General","AuswahlErstAb") );
				/***/
				String forms = inif.getStringProperty("General","FormularMahnung1") ;
				if(forms.indexOf("/") > 0){
					forms = forms.substring(forms.lastIndexOf("/")+1);
				}
				mahnParameter.put("formular1", (String) progHome+"vorlagen/"+aktIK+"/"+forms );
				/***/
				forms = inif.getStringProperty("General","FormularMahnung2") ;
				if(forms.indexOf("/") > 0){
					forms = forms.substring(forms.lastIndexOf("/")+1);
				}
				mahnParameter.put("formular2", (String) progHome+"vorlagen/"+aktIK+"/"+forms  );
				/***/
				forms = inif.getStringProperty("General","FormularMahnung3") ;
				if(forms.indexOf("/") > 0){
					forms = forms.substring(forms.lastIndexOf("/")+1);
				}
				mahnParameter.put("formular3", (String) progHome+"vorlagen/"+aktIK+"/"+forms  );
				/***/
				forms = inif.getStringProperty("General","FormularMahnung4") ;
				if(forms.indexOf("/") > 0){
					forms = forms.substring(forms.lastIndexOf("/")+1);
				}
				mahnParameter.put("formular4", (String) progHome+"vorlagen/"+aktIK+"/"+forms   );
				/***/
				//System.out.println(mahnParameter.get("formular1"));
				//System.out.println(mahnParameter.get("formular2"));
				//System.out.println(mahnParameter.get("formular3"));
				//System.out.println(mahnParameter.get("formular4"));
				mahnParameter.put("diralterechnungen", (String) inif.getStringProperty("General","DirAlteRechnungen")  );
				mahnParameter.put("inkasse", (String) inif.getStringProperty("General","WohinBuchen")  );
				AbrechnungParameter(progHome);
				FirmenDaten(progHome);
				if(args.length >= 3){
					rehaReversePort = Integer.parseInt(args[2]);
				}
			}else{
				INIFile inif = new INIFile(progHome+"ini/"+aktIK+"/oprgaf.ini");
				mahnParameter.put("frist1", (Integer) inif.getIntegerProperty("General","TageBisMahnung1") );
				mahnParameter.put("frist2", (Integer) inif.getIntegerProperty("General","TageBisMahnung2") );
				mahnParameter.put("frist3", (Integer) inif.getIntegerProperty("General","TageBisMahnung3") );
				mahnParameter.put("einzelmahnung", (Boolean) (inif.getIntegerProperty("General","EinzelMahnung").equals("1") ? Boolean.TRUE : Boolean.FALSE) );
				mahnParameter.put("drucker", (String) inif.getStringProperty("General","MahnungDrucker") );
				mahnParameter.put("exemplare", (Integer) inif.getIntegerProperty("General","MahnungExemplare") );
				mahnParameter.put("inofficestarten", (Boolean) (inif.getIntegerProperty("General","InOfficeStarten").equals("1") ? Boolean.TRUE : Boolean.FALSE) );
				mahnParameter.put("erstsuchenab", (String) inif.getStringProperty("General","AuswahlErstAb") );
				/***/
				String forms = inif.getStringProperty("General","FormularMahnung1") ;
				if(forms.indexOf("/") > 0){
					forms = forms.substring(forms.lastIndexOf("/")+1);
				}
				mahnParameter.put("formular1", (String) progHome+"vorlagen/"+aktIK+"/"+forms );
				/***/
				forms = inif.getStringProperty("General","FormularMahnung2") ;
				if(forms.indexOf("/") > 0){
					forms = forms.substring(forms.lastIndexOf("/")+1);
				}
				mahnParameter.put("formular2", (String) progHome+"vorlagen/"+aktIK+"/"+forms  );
				/***/
				forms = inif.getStringProperty("General","FormularMahnung3") ;
				if(forms.indexOf("/") > 0){
					forms = forms.substring(forms.lastIndexOf("/")+1);
				}
				mahnParameter.put("formular3", (String) progHome+"vorlagen/"+aktIK+"/"+forms  );
				/***/
				forms = inif.getStringProperty("General","FormularMahnung4") ;
				if(forms.indexOf("/") > 0){
					forms = forms.substring(forms.lastIndexOf("/")+1);
				}
				mahnParameter.put("formular4", (String) progHome+"vorlagen/"+aktIK+"/"+forms   );
				/***/
				//System.out.println(mahnParameter.get("formular1"));
				//System.out.println(mahnParameter.get("formular2"));
				//System.out.println(mahnParameter.get("formular3"));
				//System.out.println(mahnParameter.get("formular4"));
				mahnParameter.put("diralterechnungen", (String) inif.getStringProperty("General","DirAlteRechnungen")  );
				mahnParameter.put("inkasse", (String) inif.getStringProperty("General","InKasseBuchen")  );
				AbrechnungParameter(progHome);
				FirmenDaten(progHome);
			}
			if(testcase){
				System.out.println(mahnParameter);
				System.out.println("TestCase = "+testcase);
				AbrechnungParameter(progHome);
				FirmenDaten(progHome);

			}
			final OpRgaf xOpRgaf = application;
			new SwingWorker<Void,Void>(){
				@Override
				
				protected Void doInBackground() throws java.lang.Exception {
					xOpRgaf.starteDB();
					long zeit = System.currentTimeMillis();
					while(! DbOk){
						try {
							Thread.sleep(20);
							if(System.currentTimeMillis()-zeit > 10000){
								System.exit(0);
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					if(!DbOk){
						JOptionPane.showMessageDialog(null, "Datenbank konnte nicht geöffnet werden!\nReha-Statistik kann nicht gestartet werden");
						System.exit(0);
					}
					OpRgaf.starteOfficeApplication();
					return null;
				}
				
			}.execute();
			application.getJFrame();
		}else{
			JOptionPane.showMessageDialog(null, "Keine Datenbankparameter übergeben!\nReha-Statistik kann nicht gestartet werden");
			System.exit(0);
		}
		
	}
	/********************/
	
	public JFrame getJFrame(){
		try {
			UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		thisClass = this;
		jFrame = new JFrame(){
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void setVisible(final boolean visible) {
			
			if(getState()!=JFrame.NORMAL) { setState(JFrame.NORMAL); }

			  if (visible) {
			      //setDisposed(false);
			  }
			  if (!visible || !isVisible()) { 
			      super.setVisible(visible);
			  }

			  if (visible) {
			      int state = super.getExtendedState();
			      state &= ~JFrame.ICONIFIED;
			      super.setExtendedState(state);
			      super.setAlwaysOnTop(true);
			      super.toFront();
			      super.requestFocus();
			      super.setAlwaysOnTop(false);
			  }
		}

		@Override
		public void toFront() {
			  super.setVisible(true);
			  int state = super.getExtendedState();
			  state &= ~JFrame.ICONIFIED;
			  super.setExtendedState(state);
			  super.setAlwaysOnTop(true);
			  super.toFront();
			  super.requestFocus();
			  super.setAlwaysOnTop(false);
		}	
		};	
		
		new InitHashMaps();

		try{
			rehaReverseServer = new RehaReverseServer(7000);
		}catch(Exception ex){
			rehaReverseServer = null;
		}
		sqlInfo.setFrame(jFrame);
		jFrame.addWindowListener(this);
		jFrame.setSize(1000,675);
		jFrame.setTitle("Thera-Pi  Rezeptgebührrechnung/Ausfallrechnung/Mahnwesen  [IK: "+aktIK+"] "+"[Server-IP: "+dbIpAndName+"]");
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setLocationRelativeTo(null);
		otab = new OpRgafTab();
		otab.setHeader(0);
		otab.setFirstFocus();
		
		jFrame.getContentPane().add (otab);
		//jFrame.setIconImage( Toolkit.getDefaultToolkit().getImage( OpRgaf.progHome+"icons/Guldiner_I.png" ) );
		jFrame.setIconImage( Toolkit.getDefaultToolkit().getImage( System.getProperty("user.dir")+File.separator+"icons"+File.separator+"Guldiner_I.png" ) );
		jFrame.setVisible(true);
		thisFrame = jFrame;
		try{
			new SocketClient().setzeRehaNachricht(OpRgaf.rehaReversePort,"AppName#OpRgaf#"+Integer.toString(OpRgaf.xport));
			new SocketClient().setzeRehaNachricht(OpRgaf.rehaReversePort,"OpRgaf#"+RehaIOMessages.IS_STARTET);
		}catch(Exception ex){
			JOptionPane.showMessageDialog(null, "Fehler in der Socketkommunikation");
		}
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				otab.opRgafPanel.setzeFocus();		
			}
		});
		return jFrame;
	}
	
	
	/********************/
	
	public OpRgaf getInstance(){
		thisClass = this;
		return this;
	}
	
	/*******************/
	
	public void starteDB(){
		DatenbankStarten dbstart = new DatenbankStarten();
		dbstart.run(); 			
	}
	
	/*******************/
	
	public static void stoppeDB(){
		try {
			OpRgaf.thisClass.conn.close();
			OpRgaf.thisClass.conn = null;
		} catch (SQLException e) {
			e.printStackTrace();
		} 			
	}
	
	/**********************************************************
	 * 
	 */
	final class DatenbankStarten implements Runnable{
		private void StarteDB(){
			final OpRgaf obj = OpRgaf.thisClass;

			final String sDB = "SQL";
			if (obj.conn != null){
				try{
				obj.conn.close();}
				catch(final SQLException e){}
			}
			try{
				Class.forName("com.mysql.jdbc.Driver").newInstance();
	        } catch (InstantiationException e) {
				e.printStackTrace();
        		System.out.println(sDB+"Treiberfehler: " + e.getMessage());
        		OpRgaf.DbOk = false;
	    		return ;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
        		System.out.println(sDB+"Treiberfehler: " + e.getMessage());
        		OpRgaf.DbOk = false;
	    		return ;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
        		System.out.println(sDB+"Treiberfehler: " + e.getMessage());
        		OpRgaf.DbOk = false;
	    		return ;
			}	
        	try {
        		
   				obj.conn = (Connection) DriverManager.getConnection(dbIpAndName+"?jdbcCompliantTruncation=false"
   						,dbUser,dbPassword);
   				OpRgaf.thisClass.sqlInfo.setConnection(obj.conn);
				OpRgaf.DbOk = true;
    			System.out.println("Datenbankkontakt hergestellt");
        	} 
        	catch (final SQLException ex) {
        		System.out.println("SQLException: " + ex.getMessage());
        		System.out.println("SQLState: " + ex.getSQLState());
        		System.out.println("VendorError: " + ex.getErrorCode());
        		OpRgaf.DbOk = false;
        
        	}
	        return;
		}
		public void run() {
			StarteDB();
		}
	
	
	}
	/*****************************************************************
	 * 
	 */

	@Override
	public void windowActivated(WindowEvent arg0) {
	}
	@Override
	public void windowClosed(WindowEvent arg0) {
		if(OpRgaf.thisClass.conn != null){
			try {
				OpRgaf.thisClass.conn.close();
				System.out.println("Datenbankverbindung wurde geschlossen");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.exit(0);
	}
	@Override
	public void windowClosing(WindowEvent arg0) {
		if(OpRgaf.thisClass.conn != null){
			try {
				OpRgaf.thisClass.conn.close();
				System.out.println("Datenbankverbindung wurde geschlossen");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if(OpRgaf.thisClass.rehaReverseServer != null){
			try{
				new SocketClient().setzeRehaNachricht(OpRgaf.rehaReversePort,"OpRgaf#"+RehaIOMessages.IS_FINISHED);
				rehaReverseServer.serv.close();
				System.out.println("ReverseServer geschlossen");
			}catch(Exception ex){
				ex.printStackTrace();
			}
			
		}
		System.exit(0);
	}
	@Override
	public void windowDeactivated(WindowEvent arg0) {
	}
	@Override
	public void windowDeiconified(WindowEvent arg0) {
	}
	@Override
	public void windowIconified(WindowEvent arg0) {
	}
	@Override
	public void windowOpened(WindowEvent arg0) {
	}
	
	public static void AbrechnungParameter(String proghome){
		hmAbrechnung.clear();
		/********Heilmittelabrechnung********/
		INIFile inif = new INIFile(proghome+"ini/"+aktIK+"/abrechnung.ini");
		hmAbrechnung.put("hmgkvformular", inif.getStringProperty("HMGKVRechnung", "Rformular"));
		hmAbrechnung.put("hmgkvrechnungdrucker", inif.getStringProperty("HMGKVRechnung", "Rdrucker"));
		hmAbrechnung.put("hmgkvtaxierdrucker", inif.getStringProperty("HMGKVRechnung", "Tdrucker"));
		hmAbrechnung.put("hmgkvbegleitzettel", inif.getStringProperty("HMGKVRechnung", "Begleitzettel"));
		hmAbrechnung.put("hmgkvrauchdrucken", inif.getStringProperty("HMGKVRechnung", "Rauchdrucken"));
		hmAbrechnung.put("hmgkvrexemplare", inif.getStringProperty("HMGKVRechnung", "Rexemplare"));

		hmAbrechnung.put("hmpriformular", inif.getStringProperty("HMPRIRechnung", "Pformular"));
		hmAbrechnung.put("hmpridrucker", inif.getStringProperty("HMPRIRechnung", "Pdrucker"));
		hmAbrechnung.put("hmpriexemplare", inif.getStringProperty("HMPRIRechnung", "Pexemplare"));
		
		hmAbrechnung.put("hmbgeformular", inif.getStringProperty("HMBGERechnung", "Bformular"));
		hmAbrechnung.put("hmbgedrucker", inif.getStringProperty("HMBGERechnung", "Bdrucker"));
		hmAbrechnung.put("hmbgeexemplare", inif.getStringProperty("HMBGERechnung", "Bexemplare"));
		/********Rehaabrechnung********/
		hmAbrechnung.put("rehagkvformular", inif.getStringProperty("RehaGKVRechnung", "RehaGKVformular"));
		hmAbrechnung.put("rehagkvdrucker", inif.getStringProperty("RehaGKVRechnung", "RehaGKVdrucker"));
		hmAbrechnung.put("rehagkvexemplare", inif.getStringProperty("RehaGKVRechnung", "RehaGKVexemplare"));
		hmAbrechnung.put("rehagkvik", inif.getStringProperty("RehaGKVRechnung", "RehaGKVik"));
		
		hmAbrechnung.put("rehadrvformular", inif.getStringProperty("RehaDRVRechnung", "RehaDRVformular"));
		hmAbrechnung.put("rehadrvdrucker", inif.getStringProperty("RehaDRVRechnung", "RehaDRVdrucker"));
		hmAbrechnung.put("rehadrvexemplare", inif.getStringProperty("RehaDRVRechnung", "RehaDRVexemplare"));
		hmAbrechnung.put("rehadrvik", inif.getStringProperty("RehaDRVRechnung", "RehaDRVik"));
		
		hmAbrechnung.put("rehapriformular", inif.getStringProperty("RehaPRIRechnung", "RehaPRIformular"));
		hmAbrechnung.put("rehapridrucker", inif.getStringProperty("RehaPRIRechnung", "RehaPRIdrucker"));
		hmAbrechnung.put("rehapriexemplare", inif.getStringProperty("RehaPRIRechnung", "RehaPRIexemplare"));
		hmAbrechnung.put("rehapriik", inif.getStringProperty("RehaPRIRechnung", "RehaPRIik"));
		
		hmAbrechnung.put("hmallinoffice", inif.getStringProperty("GemeinsameParameter", "InOfficeStarten"));
		/*
		String INI_FILE = "";
		if(System.getProperty("os.name").contains("Windows")){
			INI_FILE = proghome+ "nebraska_windows.conf";
		}else if(System.getProperty("os.name").contains("Linux")){
			INI_FILE = proghome+ "nebraska_linux.conf";			
		}else if(System.getProperty("os.name").contains("String f�r MaxOSX????")){
			INI_FILE = proghome+"nebraska_mac.conf";
		}
		*/
		/*
		org.thera_pi.nebraska.gui.utils.Verschluesseln man = org.thera_pi.nebraska.gui.utils.Verschluesseln.getInstance();
		man.init(org.thera_pi.nebraska.gui.utils.Verschluesseln.getPassword().toCharArray(), man.getSalt(), man.getIterations());
		try{
			inif = new INIFile(INI_FILE);
			String pw = null;
			String decrypted = null;
			hmAbrechnung.put("hmkeystorepw", "");
			int anzahl = inif.getIntegerProperty("KeyStores", "KeyStoreAnzahl");
			for(int i = 0; i < anzahl;i++){
				if(inif.getStringProperty("KeyStores", "KeyStoreAlias"+Integer.toString(i+1)).trim().equals("IK"+Reha.aktIK)){
					pw = inif.getStringProperty("KeyStores", "KeyStorePw"+Integer.toString(i+1));
					decrypted = man.decrypt(pw);
					hmAbrechnung.put("hmkeystorepw", decrypted);
					break;
				}
			}

		}catch(Exception ex){
			JOptionPane.showMessageDialog(null,"Zertifikatsdatenbank nicht vorhanden oder fehlerhaft.\nAbrechnung nach � 302 kann nicht durchgef�hrt werden.");
		}
		*/
	}
	
	/***************************/
	public static void FirmenDaten(String proghome){
		String[] stitel = {"Ik","Ikbezeichnung","Firma1","Firma2","Anrede","Nachname","Vorname",
				"Strasse","Plz","Ort","Telefon","Telefax","Email","Internet","Bank","Blz","Kto",
				"Steuernummer","Hrb","Logodatei","Zusatz1","Zusatz2","Zusatz3","Zusatz4","Bundesland"};
		hmFirmenDaten = new HashMap<String,String>();
		INIFile inif = new INIFile(proghome+"ini/"+OpRgaf.aktIK+"/firmen.ini");
		for(int i = 0; i < stitel.length;i++){
			hmFirmenDaten.put(stitel[i],inif.getStringProperty("Firma",stitel[i] ) );
		}
	}
	
	
	/***************************/
	
    public static void starteOfficeApplication(){ 
    	try {
			officeapplication = (IOfficeApplication)new StartOOApplication(OpRgaf.officeProgrammPfad,OpRgaf.officeNativePfad).start(false);
			 System.out.println("OpenOffice ist gestartet und Active ="+officeapplication.isActive());
		} catch (OfficeApplicationException e1) {
			e1.printStackTrace();
		}
    }
	

}
