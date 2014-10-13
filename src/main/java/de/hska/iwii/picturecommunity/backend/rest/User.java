package de.hska.iwii.picturecommunity.backend.rest;

import java.io.Serializable;

/**
 * Ein REST-User nimmt eine Teilmenge des Anwender-Objektes auf.
 */
public class User implements Serializable {

	// Eindeutige Datenbank-ID des Anwenders
	private int id;

	// Name des Anwenders, muss eindeutig sein.
	private String username;
	
	// Mailadresse des Anwenders, muss eindeutig sein
	private String email;

	/**
	 * Einen neuen REST-User auf Basis eines vollstaendigen
	 * Datenbank-Users erstellen.
	 * @param dbUser Vollstaendiges Anwender-Objekt aus der Datenbank.
	 */
	public User(de.hska.iwii.picturecommunity.backend.entities.User dbUser) {
		this.id = dbUser.getId();
		this.username = dbUser.getUsername();
		this.email = dbUser.getEmail();
	}
	
	/**
	 * Eindeutige Datenbank-ID des Anwenders auslesen.
	 * @return Eindeutige Datenbank-ID des Anwenders.
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Eindeutige EMail-Adresse des Anwenders auslesen.
	 * @return Eindeutige EMail-Adresse des Anwenders.
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Namen des Anwenders auslesen.
	 * @return Name des Anwenders.
	 */
	public String getUsername() {
		return username;
	}
}
