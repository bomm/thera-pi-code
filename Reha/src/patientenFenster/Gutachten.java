package patientenFenster;

import hauptFenster.ProgLoader;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTable;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import patientenFenster.Historie.HistorPanel;
import patientenFenster.TherapieBerichte.MyBerichtTableModel;
import sqlTools.SqlInfo;
import systemEinstellungen.SystemConfig;
import systemTools.JCompTools;

public class Gutachten extends JXPanel implements ActionListener, TableModelListener, PropertyChangeListener{

	public static Gutachten gutachten = null;
	JXPanel leerPanel = null;
	JXPanel vollPanel = null;
	JXPanel wechselPanel = null;
	
	
	public String aktPanel = "";
	public JButton[] gutbut = {null,null,null,null};
	public JLabel anzahlGutachten = null;
	public JXTable tabbericht = null;
	public MyGutachtenTableModel dtblm;


	public Gutachten(){
		super();
		gutachten = this;
		setOpaque(false);
		setLayout(new BorderLayout());
		/********zuerst das Leere Panel basteln**************/
		leerPanel = new KeinRezept("noch keine Gutachten angelegt f�r diesen Patient");
		leerPanel.setName("leerpanel");
		leerPanel.setOpaque(false);
		
		/********dann das volle**************/	
		JXPanel allesrein = new JXPanel(new BorderLayout());
		allesrein.setOpaque(false);
		allesrein.setBorder(null);
		
		FormLayout lay = new FormLayout("5dlu,fill:0:grow(1.00),0dlu",
		"0dlu,p,2dlu,p,2dlu,fill:0:grow(1.00),5dlu");
		CellConstraints cc = new CellConstraints();
		allesrein.setLayout(lay);
		
		wechselPanel = new JXPanel(new BorderLayout());
		wechselPanel.setOpaque(false);
		wechselPanel.setBorder(null);
		wechselPanel.add(leerPanel,BorderLayout.CENTER);
		aktPanel = "leerPanel";
		
		allesrein.add(getToolbar(),cc.xy(2, 2));
		//allesrein.add(getTabelle(),cc.xy(2, 4));
		allesrein.add(wechselPanel,cc.xy(2, 6));

		add(JCompTools.getTransparentScrollPane(allesrein),BorderLayout.CENTER);
		validate();
		new Thread(){
			public void run(){
				new SwingWorker<Void,Void>(){

					@Override
					protected Void doInBackground() throws Exception {
				
						// TODO Auto-generated method stub
						vollPanel = new JXPanel();
						FormLayout vplay = new FormLayout("5dlu,fill:0:grow(1.00),5dlu","13dlu,fill:0:grow(1.00),5dlu");
						//FormLayout vplay = new FormLayout("5dlu,fill:0:grow(1.00),5dlu","5dlu,fill:0:grow(1.00),5dlu");
						CellConstraints vpcc = new CellConstraints();
						vollPanel.setLayout(vplay);
						vollPanel.setOpaque(false);
						vollPanel.setBorder(null);
						anzahlGutachten = new JLabel("Anzahl sozialmed. Gutachten: 0");
						vollPanel.add(anzahlGutachten,vpcc.xy(2,1));
						vollPanel.add(getGutachtenTbl(),vpcc.xy(2,2));
			
						return null;

					}
					
				}.execute();
			}
		}.start();
		
		

	}
	private JXPanel getGutachtenTbl(){
		JXPanel dummypan = new JXPanel(new BorderLayout());
		dummypan.setOpaque(false);
		dummypan.setBorder(null);
		dtblm = new MyGutachtenTableModel();
		String[] column = 	{"ID","Titel","Verfasser","erstellt","Empf�nger","letzte �nderung",""};
		dtblm.setColumnIdentifiers(column);
		tabbericht = new JXTable(dtblm);
		tabbericht.setEditable(true);
		tabbericht.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
				if(arg0.getClickCount()==2){
					// hier pr�fen welcher Berichtstyp und dementsprechend das Berichtsfenster �ffnen
					///neuanlageRezept(false,"");
					int wahl = tabbericht.getSelectedRow();
					if(wahl < 0){
						return;
					}
					String verfas = (String)dtblm.getValueAt(wahl, 2); 
					if( verfas.toUpperCase().contains("REHAARZT")){
						doBerichtEdit();						
					}
				}
			}
		});
/*
		0		"berichtid," +
		1		"bertitel," +
		2		"verfasser," +
		3		"DATE_FORMAT(erstelldat,'%d.%m.%Y') AS derstelldat," +
		4		"empfaenger," +
		5		"DATE_FORMAT(editdat,'%d.%m.%Y') AS deditdat,
		6		"empfid",
*/
		tabbericht.getColumn(0).setMinWidth(50);
		tabbericht.getColumn(0).setMaxWidth(50);
		tabbericht.getColumn(1).setMinWidth(140);
		tabbericht.getColumn(3).setMaxWidth(80);
		tabbericht.getColumn(5).setMaxWidth(80);
		tabbericht.getColumn(6).setMinWidth(0);
		tabbericht.getColumn(6).setMaxWidth(0);

		tabbericht.validate();

		JScrollPane jscr = JCompTools.getTransparentScrollPane(tabbericht);
		jscr.validate();
		dummypan.add(jscr,BorderLayout.CENTER);
		return dummypan;
	}
	
	public void holeGutachten(String patint,String rez){
		/**********/
					final String xpatint = patint;
					final String xrez_nr = rez;

					new SwingWorker<Void,Void>(){
						@Override
						protected Void doInBackground() throws Exception {
							try{
							//String sstmt = "select * from verordn where PAT_INTERN ='"+xpatint+"' ORDER BY REZ_DATUM";
							Vector vec = SqlInfo.holeSaetze("berhist", 
									"berichtid," +
									"berichttyp," +
									"verfasser," +
									"DATE_FORMAT(erstelldat,'%d.%m.%Y') AS derstelldat," +
									"empfaenger," +
									"DATE_FORMAT(editdat,'%d.%m.%Y') AS deditdat," +
									"empfid",
									"pat_intern='"+xpatint+"' ORDER BY erstelldat", Arrays.asList(new String[]{}));
							int anz = vec.size();
							dtblm.setRowCount(0);
							for(int i = 0; i < anz;i++){
								if(i==0){
									//dtblm.setRowCount(0);						
								}
								if(((String)((Vector)vec.get(i)).get(2)).toUpperCase().contains("REHAARZT")){
									dtblm.addRow((Vector)vec.get(i));							
								}
							}
							anz = dtblm.getRowCount();
							PatGrundPanel.thisClass.jtab.setTitleAt(4,macheHtmlTitel(anz,"Gutachten"));
							anzahlGutachten.setText("Anzahl sozialmed. Gutachten: "+anz);
							if(anz > 0){
								setzeRezeptPanelAufNull(false);
								if(xrez_nr.equals("")){
									tabbericht.setRowSelectionInterval(0,0);							
								}else{
									for(int i = 0;i<anz;i++){
										if(((String)dtblm.getValueAt(i,1)).contains(xrez_nr)){
											tabbericht.setRowSelectionInterval(i,i);
											break;
										}
									}
								}

								int anzeigen = -1;
								wechselPanel.revalidate();
								wechselPanel.repaint();					
							}else{
								setzeRezeptPanelAufNull(true);
								gutbut[0].setEnabled(true);
								wechselPanel.revalidate();
								wechselPanel.repaint();
								dtblm.setRowCount(0);
							}
							}catch(Exception ex){
								ex.printStackTrace();
							}
							
							return null;
						}
						
					}.execute();
					
					
			}
	private String macheHtmlTitel(int anz,String titel){
		String ret = titel+" - "+new Integer(anz).toString();
		return ret;
	}
	
	
	public JToolBar getToolbar(){
		JToolBar jtb = new JToolBar();
		jtb.setOpaque(false);
		jtb.setRollover(true);
		jtb.setBorder(null);
		jtb.setOpaque(false);

		gutbut[0] = new JButton();
		gutbut[0].setIcon(SystemConfig.hmSysIcons.get("neu"));
		gutbut[0].setToolTipText("Neues Gutachten erstellen");
		gutbut[0].setActionCommand("gutneu");
		gutbut[0].addActionListener(this);		
		jtb.add(gutbut[0]);

		gutbut[1] = new JButton();
		gutbut[1].setIcon(SystemConfig.hmSysIcons.get("edit"));
		gutbut[1].setToolTipText("Bestehendes Gutachten �ndern/editieren");
		gutbut[1].setActionCommand("gutedit");
		gutbut[1].addActionListener(this);		
		jtb.add(gutbut[1]);

		gutbut[2] = new JButton();
		gutbut[2].setIcon(SystemConfig.hmSysIcons.get("delete"));
		gutbut[2].setToolTipText("Gutachten l�schen");
		gutbut[2].setActionCommand("gutdelete");
		gutbut[2].addActionListener(this);		
		jtb.add(gutbut[2]);
		
		jtb.addSeparator(new Dimension(30,0));
		
		gutbut[3] = new JButton();
		gutbut[3].setIcon(SystemConfig.hmSysIcons.get("tools"));
		gutbut[3].setToolTipText("Werkzeuge f�r Gutachte");
		gutbut[3].setActionCommand("guttools");
		gutbut[3].addActionListener(this);		
		jtb.add(gutbut[3]);
		for(int i = 0; i < 4;i++){
			gutbut[i].setEnabled(false);
		}
		return jtb;
	}
	public void setzeRezeptPanelAufNull(boolean aufnull){
		if(aufnull){
			if(aktPanel.equals("vollPanel")){
				wechselPanel.remove(vollPanel);
				wechselPanel.add(leerPanel);
				aktPanel = "leerPanel";
				for(int i = 1; i < 4;i++){
					gutbut[i].setEnabled(false);
				}
			}
		}else{
			if(aktPanel.equals("leerPanel")){
				wechselPanel.remove(leerPanel);
				wechselPanel.add(vollPanel);
				aktPanel = "vollPanel";
				for(int i = 0; i < 4;i++){
					gutbut[i].setEnabled(true);
				}
			}
		}
	}

	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();
		if(cmd.equals("gutneu")){
			//ProgLoader.InternalGut2();
			ProgLoader.GutachenFenster(1,PatGrundPanel.thisClass.aktPatID ,-1,"LVA-Arztbericht",true );
			return;
		}
		if(cmd.equals("gutedit")){
			doBerichtEdit();
			return;
		}
		if(cmd.equals("gutdelete")){
			return;
		}
		if(cmd.equals("guttools")){
			return;
		}

		
	}
	private void doBerichtEdit(){
		int row = tabbericht.getSelectedRow();
		if(row < 0){
			return;
		}
		String bertyp = (String) tabbericht.getValueAt(row,1);
		int berid = new Integer( (String) tabbericht.getValueAt(row,0) );
		ProgLoader.GutachenFenster(1,PatGrundPanel.thisClass.aktPatID ,berid,bertyp,false );
		//ProgLoader.InternalGut2();
	}

	@Override
	public void tableChanged(TableModelEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	class GutachtenPanel extends JXPanel{
		ImageIcon hgicon;
		int icx,icy;
		AlphaComposite xac1 = null;
		AlphaComposite xac2 = null;		
		GutachtenPanel(){
			super();
			setOpaque(false);
			hgicon = SystemConfig.hmSysIcons.get("historie"); 
			//hgicon = new ImageIcon(Reha.proghome+"icons/ChipKarte.png");
			//hgicon = new ImageIcon(Reha.proghome+"icons/Chip.png");
			icx = hgicon.getIconWidth()/2;
			icy = hgicon.getIconHeight()/2;
			xac1 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.075f); 
			xac2 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f);			
			
		}

		//@Override
		public void paintComponent( Graphics g ) { 
			super.paintComponent( g );
			Graphics2D g2d = (Graphics2D)g;
			
			if(hgicon != null){
				g2d.setComposite(this.xac1);
				//g2d.drawImage(hgicon.getImage(), 0 , 0,null);
				g2d.drawImage(hgicon.getImage(), (getWidth()/3)-(icx+20) , (getHeight()/2)-(icy-40),null);
				g2d.setComposite(this.xac2);
			}
		}
	}
	class MyGutachtenTableModel extends DefaultTableModel{
		   /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Class getColumnClass(int columnIndex) {
			   if(columnIndex==1){
				   //return JLabel.class;}
			   		return String.class;}
			   else{
				   return String.class;
			   }
	        //return (columnIndex == 0) ? Boolean.class : String.class;
	    }

		    public boolean isCellEditable(int row, int col) {
		        //Note that the data/cell address is constant,
		        //no matter where the cell appears onscreen.
		    	return false;
		    	/*
		        if (col == 0){
		        	return true;
		        }else if(col == 3){
		        	return true;
		        }else if(col == 7){
		        	return true;
		        }else if(col == 11){
		        	return true;
		        } else{
		          return false;
		        }
		        */
		      }
		   
	}
	

}