package de.hska.iwii.picturecommunity.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.servlet.http.HttpSession;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import de.hska.iwii.picturecommunity.backend.dao.PictureDAO;
import de.hska.iwii.picturecommunity.backend.dao.UserDAO;
import de.hska.iwii.picturecommunity.backend.entities.User;
import de.hska.iwii.picturecommunity.backend.entities.Picture;

/**
 * Test-Controller fuer die Beispielanwendung
 */
@Component
@Scope("session")
@Qualifier("daoController")
public class DAOController  {

	// gespeicherter Wert
	private String value;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private PictureDAO pictureDAO;
	
	@Autowired
	@Qualifier("org.springframework.security.authenticationManager")
    protected AuthenticationManager authenticationManager;

//	private Picture prevPic;
	
	/**
	 * Nach der Initialisierung eine Meldung in die Log-Datei schreiben
	 * (ist ueberfluessig).
	 */
	@PostConstruct
	public void wakeup() {
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Bin da.");
		
		
//        PushContext pushContext = PushContextFactory.getDefault().getPushContext();  
//        pushContext.push("/counter", String.valueOf(42));  

		User user = userDAO.findUserByMailaddress("holger.vogelsang1@web.de");
		if (user == null) {
			user = new User("holger.vogelsang1@web.de", "password1", "name1", User.ROLE_USER);
			userDAO.createUser(user);
			Logger.getLogger(DAOController.class.getName()).log(Level.INFO,  "User hinzugefuegt mit email: " + user.getEmail());
		}
		
		User user1 = userDAO.findUserByMailaddress("holger.vogelsang@web.de");
		if (user1 == null) {
			user1 = new User("holger.vogelsang@web.de", "password", "name", User.ROLE_ADMIN);
			user1.getFriendsOf().add(user);
			userDAO.createUser(user1);
			Logger.getLogger(DAOController.class.getName()).log(Level.INFO,  "Admin hinzugefuegt mit email: " + user1.getEmail());
		}
		if (user.getFriendsOf().size() == 0) {
			user.getFriendsOf().add(user1);
			userDAO.updateUser(user);
		}
		user1.getFriendsOf().add(user);
		userDAO.updateUser(user1);
		
		user1 = userDAO.findUserByMailaddress("holger.vogelsang@web.de");
		user = userDAO.findUserByMailaddress("holger.vogelsang1@web.de");
		
		List<Picture> cont = pictureDAO.getPictures(user, 0, Integer.MAX_VALUE, false);
		for (Picture c: cont) {
			System.out.println("Content: " + c.getName());
		}
		
		List<User> users = userDAO.findUsersByName("na", user);
		for (User u: users) {
			Logger.getLogger(DAOController.class.getName()).log(Level.INFO, u.getUsername() + ", " + u.getPassword());
		}

		List<Picture> contents = pictureDAO.getPictures(user, 0, Integer.MAX_VALUE, false);
		for (Picture c: contents) {
			System.out.println("Content: " + c);
		}
		
		List<User> follow = userDAO.findFriendsOfUser(user);
		for (User u: follow) {
			System.out.println("User " + u.getUsername() + " folgt " + user.getUsername());
		}
		
		List<Map.Entry<User, Long>> mostActive = userDAO.getMostActiveUsers(-1);
		for (Map.Entry<User, Long> entry: mostActive) {
			System.out.println("Aktiv: User " + entry.getKey().getUsername() + ", " + entry.getValue());
		}
		
		long medias = pictureDAO.getPictureCount(user);
		System.out.println(medias);
		
//		Picture content = new Picture();
//		content.setDescription("Ein geheimes Bild");
//		content.setName("Bild 1");
//		content.setMimeType("png");
//		content.setPublicVisible(true);
//		content.setData(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0});
//		contentDAO.createPicture(user, content);
//		
////		userDAO.deleteUser(user);
//		user = userDAO.findUserByMailaddress("holger.vogelsang1@web.de");
//		System.out.println(user);
//		content = new Picture();
//		content.setDescription("Ein geheimes neues Bild");
//		content.setName("Bild 2");
//		content.setMimeType("png");
//		content.setPublicVisible(false);
//		content.setData(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0});
//		contentDAO.createPicture(user, content);
//
//		medias = contentDAO.getPictureCount(user);
//		System.out.println(medias);
	}
	
	private byte[] getFileContents(InputStream in) throws IOException {
	    byte[] bytes = null;
        // write the inputStream to a FileOutputStream            
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int read = 0;
         bytes = new byte[1024];

        while ((read = in.read(bytes)) != -1) {
            bos.write(bytes, 0, read);
        }
        bytes = bos.toByteArray();
        in.close();
        in = null;
        bos.flush();
        bos.close();
        bos = null;
	    return bytes;
	}
	
    public void handleFileUpload(FileUploadEvent event) throws IOException {  
		Logger.getLogger(DAOController.class.getName()).log(Level.INFO,  "Start Upload of file " + event.getFile().getFileName());
        FacesMessage msg = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");  
        FacesContext.getCurrentInstance().addMessage(null, msg);  
		User user1 = userDAO.findUserByMailaddress("holger.vogelsang1@web.de");
		Logger.getLogger(DAOController.class.getName()).log(Level.INFO,  "User: " + user1.getUsername());
		Picture pic = new Picture();
		pic.setData(getFileContents(event.getFile().getInputstream()));
		pic.setMimeType(event.getFile().getContentType());
		pic.setName(event.getFile().getFileName());
		pic.setDescription("Tooles Bild: " + event.getFile().getFileName());
		pictureDAO.createPicture(user1, pic);
		System.out.println("Bild angelegt: " + pic.getId() + ", " + pic.getData());
    }  
    
    public List<Picture> getImages() {  
		User user = userDAO.findUserByMailaddress("holger.vogelsang1@web.de");
		return pictureDAO.getPictures(user, 0, Integer.MAX_VALUE, false);
    }  
    
    public StreamedContent getImage() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            // 1. Phase: Rendern der HTML-Seite
            return new DefaultStreamedContent();
        }
        else {
            // Jetzt wird erst im zweiten Aufruf das Bild angefordert.
            String picId = context.getExternalContext().getRequestParameterMap().get("id");
            System.out.println("Suche nach Bild-ID: " + picId);
            Picture pic = pictureDAO.getPicture(Integer.parseInt(picId));
            System.out.println("  found pic: " + pic);
            return new DefaultStreamedContent(new ByteArrayInputStream(pic.getData()));
        }
	}


	/**
	 * Meldung mit Wert auslesen.
	 * @return Meldung
	 */
	public String getMessage() {
		// User manuell im Kontext ablegen (simuliert Registrierung)
		User user = userDAO.findUserByMailaddress("holger.vogelsang1@web.de");
		if (user == null) {
			user = new User("holger.vogelsang1@web.de", "password1", "name1", User.ROLE_USER);
			userDAO.createUser(user);
			Logger.getLogger(DAOController.class.getName()).log(Level.INFO,  "User hinzugefuegt mit email: " + user.getEmail());
		}
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user, "password1");
		Authentication authUser = authenticationManager.authenticate(token);
		
		if (authUser.isAuthenticated()) {
			SecurityContextHolder.getContext().setAuthentication(authUser);
			
			// Session anlegen und Security-Kontext darin speichern
			((HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true)).setAttribute(HttpSessionSecurityContextRepository.
					SPRING_SECURITY_CONTEXT_KEY,
										SecurityContextHolder.getContext());
		}
		
		// User manuell aus dem Kontext auslesen
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof User) {
	//		System.out.println(user.getUsername() + ", " + user.getRole());
			System.out.println(user);
		}
		
		return "Hallo mit Wert " + value;
	}

	/**
	 * Neuen Wert speichern.
	 */
	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	/**
	 * Simuliertes Speichern des Textes.
	 */
	public Object update() {
		return null;
	}
}
