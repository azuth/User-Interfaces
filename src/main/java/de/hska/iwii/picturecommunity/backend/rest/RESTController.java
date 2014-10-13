package de.hska.iwii.picturecommunity.backend.rest;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import de.hska.iwii.picturecommunity.backend.dao.PictureDAO;
import de.hska.iwii.picturecommunity.backend.dao.UserDAO;
import de.hska.iwii.picturecommunity.backend.utils.ImageUtils;

@RestController
@RequestMapping("/")
public class RESTController {

	@Autowired
	private UserDAO memberDAO;
	
	@Autowired
	private PictureDAO pictureDAO;
	
	/**
	 * Ermittelt das User-Objekt zu den uebergebenen Anmeldedaten. 
	 * @param principal Anmeldedaten des Benutzers.
	 * @return User-Objekt oder <code>null</code>, wenn die Anmeldedaten nicht stimmen.
	 */
	private de.hska.iwii.picturecommunity.backend.entities.User getUser(Principal principal) {
		de.hska.iwii.picturecommunity.backend.entities.User user = null;
		if (principal != null) {
			user = memberDAO.findUserByName(principal.getName());
		}
		return user;
	}

	/**
	 * Sollen nur oeffentliche Bilder beruecksichtigt werden?
	 * @param caller Anwender, der ein Bild lesen moechte.
	 * @param pictureOwner Besitzer des Bilder.
	 * @return <code>true</code>, wenn nur oeffentliche Bilder beruecksichtigt werden koennen,
	 * 		ansonsten <code>false</code>.
	 */
	private boolean isOnlyPublicPictures(de.hska.iwii.picturecommunity.backend.entities.User caller,
										de.hska.iwii.picturecommunity.backend.entities.User pictureOwner) {
		boolean onlyPublic = true;
		if (caller != null) {
			if (pictureOwner.getId().equals(caller.getId())) {
				onlyPublic = false;
			}
			else {
				for (de.hska.iwii.picturecommunity.backend.entities.User friendUser: caller.getFriendsOf()) {
					if (friendUser.getId().equals(pictureOwner.getId())) {
						onlyPublic = false;
						break;
					}
				}
			}
		}
		return onlyPublic;
	}

	/**
	 * Registriert einen neuen Anwender.
	 * @param userId Login-Name des Anwenders.
	 * @param password Passwort des Anwenders.
	 * @param mailaddress E-Mail-Adresse des Anwenders.
	 */
	@RequestMapping(value="/register", method=RequestMethod.POST)
	public ResponseEntity<Void> register(@RequestParam String username, @RequestParam String password, @RequestParam String mailaddress) {
		
		boolean created = false;
		StringBuilder logMessage = new StringBuilder();
		logMessage.append("REST: /register/" + " [Username: " + username + ", Password: " + password + ", Mailaddress: " + mailaddress + "]\n");

		de.hska.iwii.picturecommunity.backend.entities.User user = memberDAO.findUserByName(username);
		
		if (user == null) {
			user = new de.hska.iwii.picturecommunity.backend.entities.User(mailaddress, password, username,
																		de.hska.iwii.picturecommunity.backend.entities.User.ROLE_USER);
			memberDAO.createUser(user);
			created = true;
		}
		logMessage.append(created ? "    User created." : "    User already exists.");
		
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, logMessage.toString());

		return new ResponseEntity<>(created ? HttpStatus.CREATED : HttpStatus.CONFLICT);
	}

	/**
	 * Ermittelt alle Anwender, deren Namen eine bestimmte Zeichenfolge enthalten.
	 * @param userName Zeichenfolge, die der Anwendername enthalten muss. Als Sonderfall ist der
	 * 			userName "*" zugelassen. Damit werden alle Anwender ermittelt.
	 * @return Alle Anwender, deren Namen in das Suchschema passen.
	 */
	@RequestMapping(value="/user/name/{userName}", method=RequestMethod.GET)
	public ResponseEntity<List<User>> getUsersByName(@PathVariable String userName, Principal principal) {
		StringBuilder logMessage = new StringBuilder();
		logMessage.append("REST: /user/name/" + userName + "\n");

		ArrayList<User> restUsers = new ArrayList<>();
		
		List<de.hska.iwii.picturecommunity.backend.entities.User> users = null;
		if (getUser(principal) != null) {
			users = memberDAO.findUsersByName(userName, null);
			logMessage.append("    Found " + (users != null ? users.size() : 0) + " user(s).");
			// In REST-User mappen
			if (users != null) {
				for (de.hska.iwii.picturecommunity.backend.entities.User dbUser : users) {
					restUsers.add(new User(dbUser));
				}
			}
		}
		else {
			logMessage.append("    User not found.");
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, logMessage.toString());
			return new ResponseEntity<List<User>>(HttpStatus.FORBIDDEN);
		}
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, logMessage.toString());
		return new ResponseEntity<>(restUsers, HttpStatus.OK);
	}	

	/**
	 * Sucht einen Anwender anhand dessen Mail-Adresse. 
	 * @param mailAddress Mail-Adresse des Anwenders.
	 * @return Gefundener Anwender.
	 */
	@RequestMapping(value="/user/mailaddress/{mailAddress:.+}", method=RequestMethod.GET)
	public ResponseEntity<User> getUserByMailAddress(@PathVariable String mailAddress, Principal principal) {
		StringBuilder logMessage = new StringBuilder();
		logMessage.append("REST: /user/mailaddress/" + mailAddress + "\n");

		de.hska.iwii.picturecommunity.backend.entities.User user = null;
		if (getUser(principal) != null) {
			user = memberDAO.findUserByMailaddress(mailAddress);
			logMessage.append("    " + (user != null ? user.getUsername() : "Not") + " found.");
		}
		else {
			logMessage.append("    User not found.");
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, logMessage.toString());
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, logMessage.toString());
		return user != null ? new ResponseEntity<>(new User(user), HttpStatus.OK)  : new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}	

	/**
	 * Ermittelt alle Anwender, denen der uebergebene Anwender folgt.
	 * @param userName Name des Anwenders, dessen Follower ermittelt werden sollen.
	 * @return Alle Anwender, denen der uebergebene Anwender folgt.
	 */
	@RequestMapping(value="/user/friends/{userName}", method=RequestMethod.GET)
	public ResponseEntity<List<User>> getFriends(@PathVariable String userName, Principal principal) {
		StringBuilder logMessage = new StringBuilder();
		logMessage.append("REST: /user/friends/" + userName + "\n");

		ArrayList<User> restUsers = new ArrayList<>();
		de.hska.iwii.picturecommunity.backend.entities.User user = null;
		if (getUser(principal) != null) {
			user = memberDAO.findUserByName(userName);
			// In REST-User mappen
			if (user != null) {
				Set<de.hska.iwii.picturecommunity.backend.entities.User> friends = user.getFriendsOf();
				logMessage.append("    User " + user.getUsername() + " follows " + friends.size() + " friend(s).");
				for (de.hska.iwii.picturecommunity.backend.entities.User dbUser : friends) {
					restUsers.add(new User(dbUser));
				}
			}
			else {
				logMessage.append("    User " + userName + " not found.");
				Logger.getLogger(this.getClass().getName()).log(Level.INFO, logMessage.toString());
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		}
		else {
			logMessage.append("    User not found.");
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, logMessage.toString());
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, logMessage.toString());
		return new ResponseEntity<>(restUsers, HttpStatus.OK);
	}

	/**
	 * Ermittelt alle Bilder eines Anwenders.
	 * @param userName Name des Anwenders, dessen Bild-IDs ermittelt werden sollen.
	 * 		Als Sonderfall ist der userName "*" zugelassen. Damit werden alle oeffentlichen Bilder ermittelt.
	 * 		Folgt dieser Anwender nicht dem in <code>userName</code>, dann werden nur die oeffentlichen Bilder
	 * 		ermittelt.
	 * @return Alle Bilder des uebergebenen Anwenders.
	 */
	@RequestMapping(value="/pictures/{userName}", method=RequestMethod.GET)
	public ResponseEntity<List<Picture>> getPictures(@PathVariable String userName, Principal principal) {
		
		StringBuilder logMessage = new StringBuilder();
		ArrayList<Picture> restPictures = null;
		
		logMessage.append("REST: /pictures/" + userName + "\n");
		
		// Aufrufer der Methode
		de.hska.iwii.picturecommunity.backend.entities.User caller = getUser(principal);

		// User, dessen Bilder gelesen werden sollen.
		de.hska.iwii.picturecommunity.backend.entities.User user = userName.equals("*") ? null : memberDAO.findUserByName(userName);
		
		if (user != null || userName.equals("*")) {
			logMessage.append("    Pictures of User " + (user != null ? user.getUsername() : "*") + "\n");
			// Wenn der Caller dem uebergebenen User folgt, dann werden alle
			// Bild ermittelt, ansonsten nur die oeffentlichen. Der Caller
			// kann immer seine eigenen Bilder auslesen.
			boolean onlyPublic = userName.equals("*") || isOnlyPublicPictures(caller, user);
			logMessage.append("    Only public Pictures? " + onlyPublic + "\n");
			List<de.hska.iwii.picturecommunity.backend.entities.Picture> pictures = pictureDAO.getPictures(user, 0,  Integer.MAX_VALUE, onlyPublic);
			logMessage.append("    Found " + pictures.size() + " picture(s)");
			
			restPictures = new ArrayList<>();
			for (de.hska.iwii.picturecommunity.backend.entities.Picture dbPicture : pictures) {
				restPictures.add(new Picture(dbPicture));
			}
		}
		else {
			logMessage.append("    Invalid user, no pictures selected");
		}
		
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, logMessage.toString());
		return restPictures != null ? new ResponseEntity<>(restPictures, HttpStatus.OK) : new ResponseEntity<List<Picture>>(HttpStatus.NOT_FOUND);
	}

	/**
	 * Liest das Bild mit der uebergeben ID aus. Wird das Bild in Breite und Hoehe skaliert,
	 * so werden Breite und Hoehe als Grenzen der Bounding-Box angenommen und das Bild
	 * unter Einhaltung seiner Proportionen in die Box eingepasst.
	 * @param id ID des Bildes.
	 * @param Optionale Breitenangabe, auf die das Bild skaliert werden soll. Die
	 * 		Breite wird im Request als ?width=xxx (xxx = Breite in Pixeln) angehaengt.
	 * @param Optionale Hoehenangabe, auf die das Bild skaliert werden soll. Die
	 * 		Hoehe wird im Request als ?height=xxx (xxx = Hoehe in Pixeln) angehaengt.
	 * 		Ist dieser Anwender nicht Freund des Besitzers des Bildes, dann wird das Bild
	 * 		nur dann zurueckgegeben, wenn es ein oeffentliches ist.
	 * @param resp Servlet-Response-Objekt.
	 */
	@RequestMapping("/picture/{id}") 
	public ResponseEntity<byte[]> getPicture(@PathVariable int id, @RequestParam(required=false) Integer width,
							@RequestParam(required=false) Integer height, Principal principal,
							HttpServletResponse resp, HttpServletRequest req) {

		StringBuilder logMessage = new StringBuilder();
		
		// Log-meldung bauen
		String scaleMessage = "";
				
		if (width != null && height != null) {
			scaleMessage = " fit into width = " + width + " pixel and height = " + height + " pixel";
		}
		else if (width != null) {
			scaleMessage = " scale to width = " + width + " pixel";
		}
		else if (height != null) {
			scaleMessage = " scale to height = " + height + " pixel";
		}
		
		logMessage.append("REST: /picture/" + id + scaleMessage + "\n");

		// Aufrufer der Methode
		de.hska.iwii.picturecommunity.backend.entities.User caller = getUser(principal);

		de.hska.iwii.picturecommunity.backend.entities.Picture dbPicture = pictureDAO.getPicture(id);

		try {
			// Existiert das Bild mit der ID?
			if (dbPicture != null) { 
				// Wenn der Caller dem Inhaber des Bildes folgt, dann werden auch
				// private Bilder beruecksichtigt, ansonsten nur die oeffentlichen.
				boolean onlyPublic = isOnlyPublicPictures(caller, dbPicture.getOwner());
				
				// Zugriff als Follower erlaubt, oder Bild ist oeffentlich, oder eigenes Bild.
				if (!onlyPublic || dbPicture.isPublicVisible()) {  
					// Nur fuer Download
			        //resp.setHeader("Content-Disposition", "attachment; filename=\"" + dbPicture.getName() + "\"");
					logMessage.append("    Picture " + dbPicture.getName() + " found");
					Logger.getLogger(this.getClass().getName()).log(Level.INFO, logMessage.toString());
					
			        resp.setContentType(dbPicture.getMimeType());
				    resp.setContentLength(dbPicture.getData().length);
	
					return new ResponseEntity<>(ImageUtils.scale(dbPicture.getData(), width, height), HttpStatus.OK);
				}
				else {
					logMessage.append("    Access not allowed");
					Logger.getLogger(this.getClass().getName()).log(Level.INFO, logMessage.toString());
					return new ResponseEntity<>(HttpStatus.FORBIDDEN);
				}
			}
			else {
				logMessage.append("    Picture not found");
				Logger.getLogger(this.getClass().getName()).log(Level.INFO, logMessage.toString());
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		}
		catch (IOException ex) {
			logMessage.append("    Internal error: " + ex);
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, logMessage.toString());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}	

	/**
	 * Laedt ein neues Bild hoch.
	 * @param description Textuelle Beschreibung zum Bild.
	 * @param file Hochgeladene Bilddatei
	 * @return ID des Bildes.
	 */
	@RequestMapping(value="/newpicture", method=RequestMethod.POST)
	public ResponseEntity<Integer> upload(Principal principal, @RequestParam String description,  @RequestParam("public") boolean publicVisible,
									@RequestParam MultipartFile file) {

		StringBuilder logMessage = new StringBuilder();
		logMessage.append("REST: /newpicture/" + "\n");

		try {
			// Aufrufer der Methode
			de.hska.iwii.picturecommunity.backend.entities.User caller = getUser(principal);
			if (caller != null) {
				de.hska.iwii.picturecommunity.backend.entities.Picture pic = new de.hska.iwii.picturecommunity.backend.entities.Picture();
				pic.setData(file.getBytes());
				pic.setMimeType(file.getContentType());
				pic.setName(file.getOriginalFilename());
				pic.setDescription(description);
				pic.setPublicVisible(publicVisible);
				pictureDAO.createPicture(caller, pic);
	
				logMessage.append("    Picture " + pic.getName() + " with new id " + pic.getId() + " uploaded.");
				Logger.getLogger(this.getClass().getName()).log(Level.INFO, logMessage.toString());
				return new ResponseEntity<>(pic.getId(), HttpStatus.CREATED);
			}
			logMessage.append("    User not found, no picture uploaded.");
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, logMessage.toString());
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		catch (IOException ex) {
			logMessage.append("    Internal error: " + ex);
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, logMessage.toString());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
