package terminKalender;


import hauptFenster.Reha;
import hauptFenster.ReverseSocket;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import javax.swing.SwingWorker;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXHeader;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTitledPanel;
//import org.jdesktop.swingx.decorator.SortOrder;





import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import rechteTools.Rechte;
import rehaContainer.RehaTP;
import CommonTools.JRtaTextField;
import systemEinstellungen.SystemConfig;
import systemTools.RezeptFahnder;
import systemTools.Verschluesseln;
import dialoge.PinPanel;
import dialoge.RehaSmartDialog;
import events.RehaTPEvent;
import events.RehaTPEventClass;
import events.RehaTPEventListener;




public class SchnellSuche extends RehaSmartDialog implements ActionListener, KeyListener, RehaTPEventListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3482074172384055074L;
	private int setOben;

	private RehaTPEventClass rtp = null;
	private JXPanel jp1 = null;
	private static ArrayList<String[]> termine = new ArrayList<String[]>();
	private static JXPanel jtp = null;
	private static String dieserName = "";
	private static JXTable pliste = null;
	private static JXTitledPanel  jp;
	private JRtaTextField tfSuche = null;
	private JXButton heute = null;
	private JXButton heute4 = null;
	private JTextArea tae = null;
	private JXLabel lblsuche = null;
	private JXLabel lbldatum = null;
	private JXTable ttbl = null;
	private String aktDatum = "";
	private Vector vTdata = new Vector();
	public static SchnellSuche thisClass = null;
	public String startdatum = DatFunk.sHeute();
	SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
	Date newStart = null;
	TerminFenster eltern;
	public JXDatePicker datePicker = null;
	SchnellSucheListSelectionHandler ssucheselect = new SchnellSucheListSelectionHandler();
	
	public SchnellSuche(JXFrame owner,TerminFenster eltern){
		//super(frame, titlePanel());
		super(owner,"SchnellSuche");
		dieserName = "SchnellSuche";
		setName(dieserName);
		getSmartTitledPanel().setName(dieserName);
		
		this.eltern = eltern;
		
		this.setModal(true);
		this.setUndecorated(true);
		this.setContentPanel(titlePanel() );
		this.jtp.setLayout(new BorderLayout());
		
		JXPanel jp1 = new JXPanel();
		jp1.setBorder(null);
		jp1.setBackground(Color.WHITE);
        jp1.setLayout(new BorderLayout());
        //jp1.setLayout(new VerticalLayout(1));
        String ss = "icons/header-image.png";
        JXHeader header = new JXHeader("Mit dieser Schnellsuche....",
                "....können Sie auf einfache Weise einen Namen oder eine Rezeptnummer suchen ('Roogle' ist zwar besser, aber diese Funktion ist schneller).\n" +
                "Mit dem Button >>Startdatum<< durchsuchen sie lediglich einen Tag (schneller).\n" +
                "Mit dem Button >>Startdatum + 4 Tage<< dursuchen Sie den Kalender über einen Zeitraum von insgesamt 5 Tagen\n\n"+
                "Sie schließen dieses Fenster über den roten Punkt rechts oben, oder mit der Taste >>ESC<<.",
                new ImageIcon(ss));
        jp1.add(header,BorderLayout.NORTH);
        jp1.add(eingabePanel(),BorderLayout.SOUTH);

		this.jtp.add(jp1,BorderLayout.NORTH);
		JScrollPane jscr = new JScrollPane();
		/*
		tae = new JTextArea();
		*/
		/*
		tae.setFont(new Font("Courier New",Font.PLAIN,12));
		tae.addKeyListener(this);
		jscr.setViewportView(tae);
		*/
		ttbl = new JXTable();
		//tae.setFont(new Font("Courier New",Font.PLAIN,12));
		ttbl.addKeyListener(this);
		SchnellSucheTableModel myTable = new SchnellSucheTableModel();
		String[] column = {"Tag","Datum","Uhrzeit","Name","Rez.Nummer","Therapeut",""};
		myTable.columnNames = column;
		myTable.data = vTdata;
		ttbl.setModel(myTable);
		ttbl.getColumn(0).setMaxWidth(60);
		ttbl.getColumn(1).setMaxWidth(65);
		ttbl.getColumn(2).setMaxWidth(60);
		ttbl.getColumn(6).setMinWidth(0);
		ttbl.getColumn(6).setMaxWidth(0); //Datenvector				
		ttbl.getSelectionModel().addListSelectionListener(ssucheselect);
		jscr.setViewportView(ttbl);
		
		
		this.jtp.add(jscr,BorderLayout.CENTER);
		JXPanel dummy = new JXPanel();
		dummy.setBorder(null);
		dummy.setPreferredSize(new Dimension(0,10));
		this.jtp.add(dummy,BorderLayout.SOUTH);
		getSmartTitledPanel().setTitle("Schnellsuche ab heute + 4 Tage");
		PinPanel pinPanel = new PinPanel();
		pinPanel.getGruen().setVisible(false);
		pinPanel.setName(dieserName);
		pinPanel.setzeName(dieserName);
		setPinPanel(pinPanel);
		rtp = new RehaTPEventClass();
		rtp.addRehaTPEventListener((RehaTPEventListener) this);
		thisClass = this;
		SwingUtilities.invokeLater(new Runnable(){
		 	   public  void run()
		 	   {
		 		  suchenFocus();
		 	   }
		}); 	   
	}		    
/*******************************************************/			
/*********************************************************/
    class SchnellSucheListSelectionHandler implements ListSelectionListener {
    	
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel lsm = (ListSelectionModel)e.getSource();
            boolean isAdjusting = e.getValueIsAdjusting();
            if(isAdjusting){
            	return;
            }
            if (lsm.isSelectionEmpty()) {

            } else {
                int minIndex = lsm.getMinSelectionIndex();
                int maxIndex = lsm.getMaxSelectionIndex();
                for (int i = minIndex; i <= maxIndex; i++) {
                    if (lsm.isSelectedIndex(i)) {
                		int row = ttbl.getSelectedRow();
                		if(row < 0){return;}
                		final String reznr = (String)ttbl.getValueAt(row, 4);
                		if(reznr.trim().length()>=2){
                			if("KGMAERLORHPO".indexOf(reznr.substring(0,2)) >= 0){
                				new SwingWorker<Void,Void>(){
									@Override
									protected Void doInBackground()
											throws Exception {
										//todo = "Farbsignale mit \XY  vor der Suche abtrennen
										int index = reznr.indexOf("\\");
										if(index >= 0){
											new RezeptFahnder(false).doFahndung(reznr.substring(0,index));	
										}else{
											new RezeptFahnder(false).doFahndung(reznr);
										}
		                				//in Spalte 1 des TK den Behandler aufrufen 
		                				//und den markierten Termin auf den angewählten Termin setzen.
										return null;
									}
                				}.execute();

                			}
                		}
        				if(Reha.thisClass.terminpanel != null){
        					try{
               					Reha.thisClass.terminpanel.setzeTerminAktuell(ttbl.getValueAt(ttbl.getSelectedRow(), 1).toString(),
            							ttbl.getValueAt(ttbl.getSelectedRow(), 2).toString(),
            							ttbl.getValueAt(ttbl.getSelectedRow(), 5).toString());
               					/*
            					System.out.println(eltern.getAktiveSpalte(0));
            					System.out.println(eltern.getAktiveSpalte(1));
            					System.out.println(eltern.getAktiveSpalte(2));
            					*/
            					//eltern.terminBestaetigen(eltern.getAktiveSpalte(2),false);    						
        					}catch(Exception ex){
        						
        					}
 
        				}
                        break;
                    }
                }
            }

        }
    }	
	
public void FensterSchliessen(String welches){
	////System.out.println("Eltern-->"+this.getParent().getParent().getParent().getParent().getParent());
	//webBrowser.dispose();
	this.dispose();
}

public JXPanel eingabePanel(){
	JXPanel eingabep = new JXPanel();
	eingabep.setBorder(null);
	eingabep.setPreferredSize(new Dimension(0,31));
	//                                 1     2    3    4     5     6    7    8     9     10     11   12
	FormLayout flay = new FormLayout("10dlu,40dlu,4dlu,80dlu,4dlu,80dlu,4dlu,80dlu,15dlu,80dlu,4dlu,40dlu:g",
	"3dlu,15dlu,80dlu,15dlu,15dlu,15dlu,30dlu,15dlu");
	eingabep.setLayout(flay);
	CellConstraints cc = new CellConstraints();

	lblsuche = new JXLabel("Suche nach:");
	eingabep.add(lblsuche,cc.xy(2,2));

	tfSuche = new JRtaTextField("GROSS",false);
	tfSuche.addKeyListener(this);
	tfSuche.setName("SucheFeld");
	eingabep.add(tfSuche,cc.xy(4,2));
	
	heute = new JXButton(startdatum);
	heute.setActionCommand("start");
	heute.addActionListener(this);
	heute.addKeyListener(this);	
	eingabep.add(heute,cc.xy(6,2));

	heute4 = new JXButton(startdatum+" + 4 Tage");
	heute4.setActionCommand("startplusvier");
	heute4.addActionListener(this);
	heute4.addKeyListener(this);
	eingabep.add(heute4,cc.xy(8,2));
	
	lbldatum = new JXLabel("            ");
	eingabep.add(lbldatum,cc.xy(10,2));
	

	
	datePicker = new JXDatePicker(); 
	datePicker.setDate(new Date());
	datePicker.setName("datePicker");
	//datePicker.addActionListener(this);
	
	datePicker.addActionListener(new ActionListener(){

		@Override
		public void actionPerformed(ActionEvent e) {
			newStart = datePicker.getDate();
			startdatum = sdf.format(newStart);
			//System.out.println("Neues Startdatum = "+startdatum);
			heute.setText(startdatum);
			heute4.setText(startdatum+" + 4 Tage");
		}
		
	});
	
	eingabep.add(datePicker,cc.xy(12,2));
	
	
	eingabep.setVisible(true);
	
	return eingabep;
}

public void setLabelDatum(String sdatum){
	lbldatum.setText(sdatum);
}
public void setTextAreaText(String stext){
	String aktText = tae.getText();
	aktText = aktText+stext+"\n";
	tae.setText(aktText);
}

public void setTerminTable(Vector threadVect){
	vTdata = threadVect;
	SchnellSucheTableModel myTable = new SchnellSucheTableModel();
	String[] column = {"Tag","Datum","Uhrzeit","Name","Rez.Nummer","Therapeut",""};
	myTable.columnNames = column;
	myTable.data = vTdata;
	ttbl.setModel(myTable);
	ttbl.getColumn(0).setMaxWidth(60);
	ttbl.getColumn(1).setMaxWidth(65);
	ttbl.getColumn(2).setMaxWidth(60);
	ttbl.getColumn(6).setMinWidth(0);
	ttbl.getColumn(6).setMaxWidth(0); //Datenvector				
	ttbl.setEditable(false);
	ttbl.setSortable(true);
	//SortOrder setSort = SortOrder.ASCENDING;
	//ttbl.setSortOrder(6,(SortOrder) setSort);
	ttbl.setSelectionMode(0);	

	
}
private static JXPanel titlePanel(){
	jp = new RehaTP(0);
	jp.setName(dieserName);
	jtp = (JXPanel) jp.getContentContainer();
	jtp.setSize(new Dimension(200,200));
	jtp.setVisible(true);
	return jtp;
}


public String dieserName(){
	return this.getName();
}

public void rehaTPEventOccurred(RehaTPEvent evt) {
	// TODO Auto-generated method stub
	//System.out.println("****************das darf doch nicht wahr sein in DruckFenster**************");
	String ss =  this.getName();
	//System.out.println("SchnellSucheFenster "+this.getName()+" Eltern "+ss);
	try{
		//if (evt.getDetails()[0].equals(ss) && evt.getDetails()[1]=="ROT"){
			FensterSchliessen(evt.getDetails()[0]);
			rtp.removeRehaTPEventListener((RehaTPEventListener) this);
		//}	
	}catch(NullPointerException ne){
		//System.out.println("In DruckFenster" +evt);
	}


}

public void actionPerformed(ActionEvent arg0) {
	String cmd = arg0.getActionCommand();
		if(cmd.equals("start")){
			vTdata.clear();
			tageSuchen(0);
			tfSuche.requestFocus();
			return;
		}else if(cmd.equals("startplusvier")){
			vTdata.clear();
			tageSuchen(4);
			tfSuche.requestFocus();
			return;
		}
}

private void tageSuchen(int abheute){
	String suchkrit = tfSuche.getText().trim();
	if(! suchkrit.equals("") && abheute==0){
		SchnellSucheTableModel myTable = new SchnellSucheTableModel();
		String[] column = {"Tag","Datum","Uhrzeit","Name","Rez.Nummer","Therapeut",""};
		myTable.columnNames = column;
		myTable.data = vTdata;
		ttbl.setModel(myTable);
		ttbl.getColumn(0).setMaxWidth(60);
		ttbl.getColumn(1).setMaxWidth(65);
		ttbl.getColumn(2).setMaxWidth(60);
		ttbl.getColumn(6).setMinWidth(0);
		ttbl.getColumn(6).setMaxWidth(0); //Datenvector				
		ttbl.setEditable(false);
		ttbl.setSortable(true);
		//SortOrder setSort = SortOrder.ASCENDING;
		//ttbl.setSortOrder(6,(SortOrder) setSort);
		ttbl.setSelectionMode(0);	

		String[] stmts = {null};
		String sdatum = "";
		aktDatum = DatFunk.sDatPlusTage(startdatum, abheute);
		sdatum = DatFunk.sDatInSQL(aktDatum);
		String stmt = "select * from flexkc where datum = '"+sdatum+"'";
		stmts[0] = stmt;
		setLabelDatum(aktDatum);
		SuchenInTagen sIt = new SuchenInTagen();
		sIt.setzeStatement(stmts, suchkrit);
	}
	if(! suchkrit.equals("") && abheute > 0){
		SchnellSucheTableModel myTable = new SchnellSucheTableModel();
		String[] column = {"Tag","Datum","Uhrzeit","Name","Rez.Nummer","Therapeut",""};
		myTable.columnNames = column;
		myTable.data = vTdata;
		ttbl.setModel(myTable);
		ttbl.getColumn(0).setMaxWidth(60);
		ttbl.getColumn(1).setMaxWidth(65);
		ttbl.getColumn(2).setMaxWidth(60);
		ttbl.getColumn(6).setMinWidth(0);
		ttbl.getColumn(6).setMaxWidth(0); //Datenvector				
		ttbl.setEditable(false);
		ttbl.setSortable(true);
		//SortOrder setSort = SortOrder.ASCENDING;
		//ttbl.setSortOrder(6,(SortOrder) setSort);
		ttbl.setSelectionMode(0);	
		String[] stmts = {null,null,null,null,null};
		String sdatum = "";
		aktDatum = startdatum; //DatFunk.sDatPlusTage(DatFunk.sHeute(), 0);
		sdatum = DatFunk.sDatInSQL(aktDatum);
		String stmt = "select * from flexkc where datum = '"+sdatum+"'";
		setLabelDatum(aktDatum);
		stmts[0] = stmt;
		aktDatum = DatFunk.sDatPlusTage(aktDatum, 1);
		stmt = "select * from flexkc where datum = '"+DatFunk.sDatInSQL(aktDatum)+"'";
		stmts[1] = stmt;
		aktDatum = DatFunk.sDatPlusTage(aktDatum, 1);
		stmt = "select * from flexkc where datum = '"+DatFunk.sDatInSQL(aktDatum)+"'";
		stmts[2] = stmt;
		aktDatum = DatFunk.sDatPlusTage(aktDatum, 1);
		stmt = "select * from flexkc where datum = '"+DatFunk.sDatInSQL(aktDatum)+"'";
		stmts[3] = stmt;
		aktDatum = DatFunk.sDatPlusTage(aktDatum, 1);
		stmt = "select * from flexkc where datum = '"+DatFunk.sDatInSQL(aktDatum)+"'";
		stmts[4] = stmt;
		/*
		for(int i=0;i<5;i++){
			//System.out.println("Befehl = "+stmts[i]);
		}
		*/	
		SuchenInTagen sIt = new SuchenInTagen();
		sIt.setzeStatement(stmts.clone(), suchkrit);
	}

}

public static JXTable getTable(){
	return pliste;
}
public static ArrayList<String[]> getTermine(){
	return termine;
}
public void suchenFocus(){
	SwingUtilities.invokeLater(new Runnable(){
	 	   public  void run()
	 	   {
	 		   tfSuche.requestFocus();
	 	   }
	}); 
	/*
    Verschluesseln man = Verschluesseln.getInstance();
    man.init(Verschluesseln.getPassword().toCharArray(), man.getSalt(), man.getIterations());
    final String encrypted = man.encrypt("s1b2rta");
    //System.out.println ("Verschl�sselt :"+encrypted);
    final String decrypted = man.decrypt (encrypted);
    //System.out.println("Entschl�sselt :"+decrypted);
    */
  }
@Override
public void keyPressed(KeyEvent arg0) {
	////System.out.println(arg0.getKeyCode()+" - "+arg0.getSource());
	if(arg0.getKeyCode() == 27){
		rtp.removeRehaTPEventListener((RehaTPEventListener) this);
		FensterSchliessen(null);
	}
	if(arg0.getKeyCode() == 10 && (!arg0.isControlDown())){
		if(arg0.getComponent().getName() != null){
			if(arg0.getComponent().getName().equals("SucheFeld")){
				vTdata.clear();
				tageSuchen(0);
				tfSuche.requestFocus();
				arg0.consume();
			}
		}

	}
	if(arg0.getKeyCode() == 10 && (arg0.isControlDown())){
		int selrow = -1;
		if((selrow=ttbl.getSelectedRow()) < 0){
			return;
		}
		if(! Rechte.hatRecht(Rechte.Kalender_terminconfirminpast, true)){
			return;
		}
		int frage = JOptionPane.showConfirmDialog(null,"Termin für - "+ttbl.getValueAt(selrow, 4).toString()+" - als behandelt bestätigen?","Wichtige Benutzeranfrage",JOptionPane.YES_NO_OPTION);
		if(frage==JOptionPane.YES_OPTION){
			eltern.terminBestaetigen(eltern.getAktiveSpalte(2),false);
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					ttbl.requestFocus();
				}
			});
		}
	}

	
}
@Override
public void keyReleased(KeyEvent arg0) {
	// TODO Auto-generated method stub
	
}
@Override
public void keyTyped(KeyEvent arg0) {
	// TODO Auto-generated method stub
	if(arg0.getKeyCode() == 27){
		rtp.removeRehaTPEventListener((RehaTPEventListener) this);
		FensterSchliessen(null);
	}	
}



/*******************************************/
}
/******************************************/
final class SuchenInTagen extends Thread implements Runnable{
	Statement stmt = null;
	ResultSet rs = null;
	String sergebnis = "";
	boolean gesperrt = false;
	String[] exStatement = null;
	String suchkrit = "";
	ArrayList<String> atermine = new ArrayList<String>(); 
	public void setzeStatement(String[] exStatement,String suchkrit){
		this.exStatement = exStatement;
		this.suchkrit = suchkrit;
		start();
	}
	public void run(){
		Vector treadVect = new Vector();
		try {
			stmt = (Statement) Reha.thisClass.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE );
			for(int i = 0; i<exStatement.length;i++){
				try{
					rs = (ResultSet) stmt.executeQuery(exStatement[i]);
					////System.out.println("Nach for..."+exStatement[i]);
					//SchnellSuche.thisClass.setLabelDatum("nach ExecuteQuery");
					while(rs.next()){
						/*in Spalte 301 steht die Anzahl der belegten Bl�cke*/ 
						int belegt = rs.getInt(301);
						SchnellSuche.thisClass.setLabelDatum(DatFunk.sDatInDeutsch(rs.getString(305)));
						String name = "";
						String nummer = "";
						String termin = "";
						String sdatum = "";
						String sorigdatum = "";
						String uhrzeit = "";
						String skollege = "";
						int ikollege = 0;
						for(int ii = 0;ii < belegt;ii++){
							name = rs.getString("T"+(ii+1)); 
							nummer = rs.getString("N"+(ii+1));
							skollege = rs.getString(303).substring(0,2);
							if( skollege.substring(0,1).equals("0") ){
								ikollege = Integer.parseInt(skollege.substring(1,2));
							}else{
								ikollege = Integer.parseInt(skollege);								
							}
							if(name.contains(suchkrit) || nummer.contains(suchkrit) ){
								uhrzeit = rs.getString("TS"+(ii+1));
								sorigdatum = rs.getString(305); 
								sdatum = DatFunk.sDatInDeutsch(sorigdatum);
								skollege = (String) ParameterLaden.getKollegenUeberReihe(ikollege);
								//skollege = (String) ParameterLaden.vKollegen.get(ikollege).get(0);
								
								termin = DatFunk.WochenTag(sdatum)+" - "+sdatum+" - "+uhrzeit+
								"  -  "+name +" - "+nummer+" - "+skollege;
								//SchnellSuche.thisClass.setTextAreaText(termin);
								atermine.add(DatFunk.WochenTag(sdatum));
								atermine.add(sdatum);
								atermine.add(uhrzeit.substring(0,5));
								atermine.add(name);
								atermine.add(nummer);								
								atermine.add(skollege);								
								atermine.add(sorigdatum+uhrzeit.substring(0,5));								
								treadVect.addElement(atermine.clone());
								//SchnellSuche.thisClass.setTerminTable((ArrayList) atermine.clone());
								atermine.clear();
							}else if(suchkrit.equals("!®") && (!SystemConfig.isAndi)){
								try{
									String extraktnummer = (nummer.indexOf("\\") >= 0 ? nummer.substring(0,nummer.indexOf("\\")) : nummer);
									if(extraktnummer.length() > 2 && (!name.startsWith("®")) && (!nummer.equals("@FREI"))){
										uhrzeit = rs.getString("TS"+(ii+1));
										sorigdatum = rs.getString(305); 
										sdatum = DatFunk.sDatInDeutsch(sorigdatum);
										skollege = (String) ParameterLaden.getKollegenUeberReihe(ikollege);
										//skollege = (String) ParameterLaden.vKollegen.get(ikollege).get(0);
										
										termin = DatFunk.WochenTag(sdatum)+" - "+sdatum+" - "+uhrzeit+
										"  -  "+name +" - "+nummer+" - "+skollege;
										//SchnellSuche.thisClass.setTextAreaText(termin);
										atermine.add(DatFunk.WochenTag(sdatum));
										atermine.add(sdatum);
										atermine.add(uhrzeit.substring(0,5));
										atermine.add(name);
										atermine.add(nummer);								
										atermine.add(skollege);								
										atermine.add(sorigdatum+uhrzeit.substring(0,5));								
										treadVect.addElement(atermine.clone());
										atermine.clear();
									}									
								}catch(Exception ex){
									
								}

							}
						}
					}
					
				}catch(SQLException ev){
					//System.out.println("SQLException: " + ev.getMessage());
					//System.out.println("SQLState: " + ev.getSQLState());
					//System.out.println("VendorError: " + ev.getErrorCode());
				}	
			}
			SchnellSuche.thisClass.setTerminTable((Vector) treadVect.clone());
		}catch(SQLException ex) {
			//System.out.println("von stmt -SQLState: " + ex.getSQLState());
		}

		finally {
			if (rs != null) {
			try {
				rs.close();
			} catch (SQLException sqlEx) { // ignore }
				rs = null;
			}
			if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException sqlEx) { // ignore }
				stmt = null;
			}
			}
			}
		}
		 
	}
	
}


class SchnellSucheTableModel extends AbstractTableModel {
    private static final boolean DEBUG = false;

    //public String[] columnNames = null;
    //public Object[][] data = null;    
    
    public String[] columnNames = { "", "",};

    //public Object[][] data = {{"","","","","","",-1,-1}};
    public Vector<ArrayList> data = null;

    public int getColumnCount() {
      return columnNames.length;
    }

    public int getRowCount() {
      return data.size();
    }
    
    public void deleteRow(int row){
    	////System.out.println("Wert = "+getValueAt(row,3)); 
    	printDebugData();
    	data.remove(row);
    	fireTableDataChanged();
    	printDebugData();
    	//fireTableChanged(null);
    }

    public String getColumnName(int col) {
      return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
      return ((ArrayList) ((Vector) data).get(row)).get(col);
    }

    /*
     * JTable uses this method to determine the default renderer/ editor for
     * each cell. If we didn't implement this method, then the last column
     * would contain text ("true"/"false"), rather than a check box.
     */
    public Class getColumnClass(int c) {
		   return String.class;
      //return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's editable.
     */
    public boolean isCellEditable(int row, int col) {
      //Note that the data/cell address is constant,
      //no matter where the cell appears onscreen.
      if (col < 1 ) {
        return false;
      } else {
        return true;
      }
    }

    /*
     * Don't need to implement this method unless your table's data can
     * change.
     */
    public void setValueAt(Object value, int row, int col) {
      //if (DEBUG) {
        //System.out.println("Setting value at " + row + "," + col
        //    + " to " + value + " (an instance of "
        //    + value.getClass() + ")");
      //}

      //data.set(row)[col] = value;
      fireTableCellUpdated(row, col);

      if (DEBUG) {
        //System.out.println("New value of data:");
        printDebugData();
      }
    }

    private void printDebugData() {
      int numRows = getRowCount();
      int numCols = getColumnCount();

      for (int i = 0; i < numRows; i++) {
        //System.out.print("    row " + i + ":");
        for (int j = 0; j < numCols; j++) {
          //System.out.print("  " +  ((ArrayList) ((Vector) data).get(i)).get(j) );
        }
        //System.out.println();
      }
      //System.out.println("--------------------------");
    }
  }
