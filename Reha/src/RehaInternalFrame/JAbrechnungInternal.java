package RehaInternalFrame;

import hauptFenster.AktiveFenster;
import hauptFenster.Reha;

import java.awt.event.FocusListener;
import java.beans.PropertyVetoException;

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;

import events.RehaEvent;
import events.RehaEventClass;
import events.RehaEventListener;

public class JAbrechnungInternal extends JRehaInternal implements FocusListener, RehaEventListener{
		/**
	 * 
	 */
	private static final long serialVersionUID = -4989326440978535166L;
		RehaEventClass rEvent = null;
		public JAbrechnungInternal(String titel, ImageIcon img, int desktop) {
			super(titel, img, desktop);
			this.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
			rEvent = new RehaEventClass();
			rEvent.addRehaEventListener((RehaEventListener) this);


			//addInternalFrameListener(this);
			// TODO Auto-generated constructor stub
		}
		@Override
		public void internalFrameClosed(InternalFrameEvent arg0) {
			System.out.println("JInternalFrame-Kassenabrechnung aufr�umen");
			Reha.thisClass.aktiviereNaechsten(this.desktop);
			Reha.thisClass.desktops[this.desktop].remove(this);
			rEvent.removeRehaEventListener(this);
			removeFocusListener(this);
			this.removeAncestorListener(this);
			AktiveFenster.loescheFenster("Abrechnung-1");
			rEvent = null;
			Reha.thisClass.progLoader.loescheAbrechnung1();

		}
		@Override
		public void rehaEventOccurred(RehaEvent evt) {
			if(evt.getRehaEvent().equals("REHAINTERNAL")){
				//System.out.println("es ist ein Reha-Internal-Event");
			}
			if(evt.getDetails()[0].equals(this.getName())){
				if(evt.getDetails()[1].equals("#ICONIFIED")){
					try {
						this.setIcon(true);
					} catch (PropertyVetoException e) {
						e.printStackTrace();
					}
					this.setActive(false);
				}
			}
		}
		

}