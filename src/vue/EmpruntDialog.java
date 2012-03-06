package vue;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import modèle.Media;
import modèle.Membre;

import controleur.Bibliotheque;

public class EmpruntDialog extends JDialog implements ActionListener {

	private String[] labels = {"ISBN média", "Identifiant membre"};
	private int retStatus = -1;
	private TextForm form;
	private JButton btnValider;
	private JButton btnAnnuler;
	
	public EmpruntDialog(JFrame parent) {
		super(parent, "Effecter un nouvel emprunt", true);
		
		buildInterface();
		buildEvents();
	}
	
	private void buildInterface() {
		setLayout(new BorderLayout());
		
		setLocationRelativeTo(null);
		setResizable(false);
		
		this.form = new TextForm(this.labels);
		
		JPanel pnlButtons = new JPanel();
		this.btnValider = new JButton("Emprunter");
		this.btnAnnuler = new JButton("Annuler");
		
		pnlButtons.add(btnValider);
		pnlButtons.add(btnAnnuler);
		
		getContentPane().add(form, BorderLayout.NORTH);
		getContentPane().add(pnlButtons, BorderLayout.SOUTH);
		
		pack();
	}
	
	private void buildEvents() {
		btnValider.addActionListener(this);
		btnAnnuler.addActionListener(this);
	}
	
	public int getReturnStatus() {
		return this.retStatus;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == btnAnnuler) {
			this.retStatus = BiblioDialog.RET_CANCEL;
			setVisible(false);
			dispose();
		}
		else if(e.getSource() == btnValider) {
			// On récupère les champs
			String isbn = form.getFieldText(0);
			int id = -1;
			
			try {
				id = Integer.parseInt(form.getFieldText(1));
				
				// On test si le média et le membre existent bien
				if(!Bibliotheque.mediaExists(isbn) || !Bibliotheque.membreExists(id)) {
					JOptionPane.showConfirmDialog(this, "Le membre ou le média n'existe pas", "Erreur", JOptionPane.CLOSED_OPTION, JOptionPane.ERROR_MESSAGE);
				}
				else {
					// On vérifie que le média n'est pas en cours d'emprunt ou d'écoute/lecture
					if(!Bibliotheque.isEmpruntable(isbn)) {
						JOptionPane.showConfirmDialog(this, "Le média est déjà en cours d'emprunt ou en cours d'écoute", "Erreur", JOptionPane.CLOSED_OPTION, JOptionPane.ERROR_MESSAGE);
					}
					else if(Bibliotheque.nouvelEmprunt(isbn, id)) { // Tout est OK en principe !
						this.retStatus = BiblioDialog.RET_OK; 	// On informe la frame parente que tout est OK
						this.dispose();
					}
					else {	
						System.out.println("Emprunt impossible... WHY ???");
					}
				}
			}
			catch(NumberFormatException nfe) {
				JOptionPane.showConfirmDialog(this, "L'identifiant du membre doit être un nombre", "Erreur", JOptionPane.CLOSED_OPTION, JOptionPane.ERROR_MESSAGE);
			}				
		}
	}
}