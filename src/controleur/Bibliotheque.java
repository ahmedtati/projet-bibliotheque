package controleur;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import modele.Abonne;
import modele.AudioLivre;
import modele.Cd;
import modele.CoffretDvd;
import modele.Database;
import modele.Dvd;
import modele.Emprunt;
import modele.Livre;
import modele.Magazine;
import modele.Media;
import modele.Membre;
import modele.Personnel;
import modele.Piste;

/**
 * 
 * Classe contrôleur
 *
 */
public class Bibliotheque {

	private static Database db;
	
	public static void initBibliotheque() {
		db = new Database();
		db.openDatabase();
	}

	public static void closeBibliotheque() {
		System.out.print("Fermeture du programme en cours...");
		db.closeDatabase();
		System.out.println(" Ok !"); 
		System.exit(0);
	}
	
	public static void updateMedia(Media m) {
		db.updateObject(m);
	}
	
	public static void addMembre(Membre m) {
		db.storeObject(m);
	}
	
	public static List<Membre> rechercheMembre(String recherche) {
		return db.rechercheMembre(recherche);
	}
	
	public static Membre getMembreById(int id) {
		List<Membre> listMembres = (List<Membre>) db.getList(Membre.class);
		
		for(Membre m : listMembres) {
			if(m.getIdentifiant() == id) {
				return m;
			}
		}
		return null;
	}
	
	public static void delMembre(int id) {
		db.removeObject(db.getMembreById(id));
	}
	
	public static void addMedia(Media m) {
		db.storeObject(m);
	}
	
	public static void delMedia(String isbn) {
		db.removeObject(db.getMediaByIsbn(isbn));
	}
	
	public static Media getMediaByIsbn(String isbn) {
		return db.getMediaByIsbn(isbn);
	}
	
	public static boolean nouvelEmprunt(String isbn, int memberId) {
		Membre mb = getMembreById(memberId);
		Media med = getMediaByIsbn(isbn);
		
		if(mb == null || med == null) 
			return false;
		
		Emprunt e = new Emprunt(med,mb);
		mb.addEmprunt(e);
		
		db.updateObject(mb); // On met à jour le Membre avec son nouvel emprunt
		db.storeObject(e);
		
		return true;
	}
	
	public static void terminerEmprunt(String isbn, int id) {
		Emprunt emp = Bibliotheque.getEmpruntEnCours(isbn, id);
		
		emp.setDateRetour(new Date()); // On définit une date de retour --> isEnCours = false
		
		System.out.println(emp.getDateRetour());
		db.storeObject(emp); // On met à jour l'emprunt dans la base

	}

	public static boolean mediaExists(String isbn) {
		return (db.getMediaByIsbn(isbn) != null);
	}
	
	public static boolean membreExists(int id) {
		return (db.getMembreById(id) != null);
	}

	public static Emprunt getEmprunt(String isbn, int id) {	
		return db.getEmprunt(isbn, id);	
	}
	
	public static Emprunt getEmpruntEnCours(String isbn, int id) {
		
		for(Emprunt e : db.getEmprunts(true)) {
			if(e.getMedia().getIsbn().equals(isbn) && e.getMembre().getIdentifiant() == id)
				return e;
		}
		
		return null;
	}
	
	/**
	 * Le média ne doit ni être en cours d'emprunts ou en cours de lecture/écoute
	 * @param isbn
	 * @return
	 */
	public static boolean isEmpruntable(String isbn) {
		// Test si en cours d'emprunt
		if(Bibliotheque.isEnCoursDemprunt(isbn))
			return false;
		
		// Test si en cours de lecture s'il s'agit d'un livre ou d'un CD
		if(Bibliotheque.getMediaByIsbn(isbn) instanceof Livre) {
			Livre livre = (Livre) Bibliotheque.getMediaByIsbn(isbn);
			if(livre.enLecture())
				return false;
		}
		else if(Bibliotheque.getMediaByIsbn(isbn) instanceof Cd) {
			Cd cd = (Cd) Bibliotheque.getMediaByIsbn(isbn);
			if(cd.enLecture())
				return false;
		}

		return true;
	}
	
	public static boolean isEnCoursDemprunt(String isbn) {
		for(Emprunt e : Bibliotheque.getListEmpruntsEnCours())
			if(e.getMedia().getIsbn().equals(isbn))
				return true;
		
		return false;
	}
	
	public static int getNouvelIdMembre() {
		int newID = 1;
		List<Membre> membres = (List<Membre>) db.getList(Membre.class);
		
		if(membres != null && !membres.isEmpty())
			newID = membres.get(membres.size()-1).getIdentifiant() + 1;
		
		return newID;
	}
	
	public static List<Media> getListMediasEnLecture() {
		List<Media> mediasEnLecture = new ArrayList<Media>();
		
		for(Livre l : Bibliotheque.getListLivre())
			if(l.enLecture())
				mediasEnLecture.add(l);
		
		for(Cd c : Bibliotheque.getListCd())
			if(c.enLecture())
				mediasEnLecture.add(c);
		
		return mediasEnLecture;
	}
	
	public static List<Membre> getListMembres() {
		return (List<Membre>) db.getList(Membre.class);
	}
	
	public static List<Personnel> getListPersonnels() {
		return (List<Personnel>) db.getList(Personnel.class);
	}
	
	public static List<Abonne> getListAbonnes() {
		return (List<Abonne>) db.getList(Abonne.class);
	}
	
	public static List<Media> getListMedias() {
		return (List<Media>) db.getList(Media.class);
	}
	
	public static List<Emprunt> getListEmpruntsEnCours() {
		return db.getEmprunts(true);
	}
	
	public static List<Emprunt> getListEmpruntsTermines() {
		return db.getEmprunts(false);
	}
	
	public static List<AudioLivre> getListAudioLivre(){
		return (List<AudioLivre>) db.getList(AudioLivre.class);
	}
	
	public static List<Livre> getListLivre(){
		return (List<Livre>) db.getList(Livre.class);
	}

	public static List<Livre> getListMagazine(){
		return (List<Livre>) db.getList(Magazine.class);
	}

	public static List<Cd> getListCd() {
		return (List<Cd>) db.getList(Cd.class);
	}
	
	public static List<Dvd> getListDvd() {
		return (List<Dvd>) db.getList(Dvd.class);
	}
	
	public static List<CoffretDvd> getListCoffretDvd() {
		return (List<CoffretDvd>) db.getList(CoffretDvd.class);
	}
	
	public static Date stringToDate(String date) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		return formatter.parse(date);
	}
	
	/**
	 * 
	 * @param typeObj
	 * @param editable
	 * @return
	 */
	public static String[] getLabelValues(Class<?> typeObj, boolean editable) {
		if(typeObj == Abonne.class) {
			if(!editable) // Pour un simple affichage
				return new String[]{"Identifiant", "Nom", "Prénom", "Date de naissance", "Taux Reduction"};
			else // Lorsqu'on créé ou on modifie l'objet : ex: on ne peut pas renseigner l'identifiant
				return new String[]{"Nom", "Prénom", "Date de naissance"};
		}
		else if(typeObj == Personnel.class) {
			if(!editable)
				return new String[]{"Identifiant", "Nom", "Prénom", "Date de naissance","Poste", "Taux Reduction"};
			else
				return new String[]{"Nom", "Prénom", "Date de naissance", "Poste"};			
		}
		else if(typeObj == AudioLivre.class) {
			if(!editable)
				return new String[]{"ISBN", "Auteur", "Titre", "Date de parution", "Nombre de pages", "Durée", "Prix"};
			else
				return new String[]{"ISBN", "Auteur", "Titre", "Date de parution", "Nombre de pages"};
		}
		else if(typeObj == Cd.class) {
			if(!editable)
				return new String[]{"ISBN", "Auteur", "Titre", "Date de parution", "Prix", "Nombre de pistes", "Durée"};
			else
				return new String[]{"ISBN", "Auteur", "Titre", "Date de parution"};
		}
		else if(typeObj == CoffretDvd.class) {
			if(!editable)
				return new String[]{"ISBN", "Auteur", "Titre", "Date de parution", "Durée", "Prix"};
			else
				return new String[]{"ISBN", "Auteur", "Titre", "Date de parution"};	
		}
		else if(typeObj == Dvd.class) {
			if(!editable)
				return new String[]{"ISBN", "Auteur", "Titre", "Date de parution", "Durée", "Prix"};
			else
				return new String[]{"ISBN", "Auteur", "Titre", "Date de parution", "Durée"};
		}
		else if(typeObj == Livre.class) {
			if(!editable)
				return new String[]{"ISBN", "Auteur", "Titre", "Date de parution", "Nombre de pages","Prix"};
			else
				return new String[]{"ISBN", "Auteur", "Titre", "Date de parution", "Nombre de pages"};	
		}
		else if(typeObj == Magazine.class) {
			if(!editable)
				return new String[]{"ISBN", "Auteur", "Titre", "Date de parution", "Nombre de pages", "Mode de parution","Prix"};
			else
				return new String[]{"ISBN", "Auteur", "Titre", "Date de parution", "Nombre de pages", "Mode de parution"};
		}
		else if(typeObj == Piste.class) {
			if(!editable)
				return new String[]{"Numéro", "Titre", "Durée"};
			else
				return new String[]{"Numéro", "Titre", "Durée"};
		}
		else if(typeObj == Emprunt.class) {
			if(!editable)
				return new String[]{"ISBN", "Titre du média", "Identifiant emprunteur","Nom de l'emprunteur", "Date début emprunt", "Date retour limite", "Date retour"};
			else
				return new String[]{"ISBN du média", "Identifiant emprunteur"};
		}
		else if(typeObj == Media.class) {
			return new String[]{"ISBN", "Auteur", "Titre", "Date de parution", "Prix"};
		}
		else {
			throw new IllegalArgumentException("Classe non répertoriée"+typeObj.toString());
		}
	}

	public static void updateObject(Object o) {
		db.updateObject(o);
	}
}
