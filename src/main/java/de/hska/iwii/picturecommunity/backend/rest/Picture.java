package de.hska.iwii.picturecommunity.backend.rest;

import java.io.Serializable;

/**
 * Ein REST-Bild nimmt eine Teilmenge des Bild-Objektes aus der Datenbank auf.
 */
public class Picture implements Serializable {

	// Eindeutige Datenbank-ID des Bildes
	private int id;

	// Mime-Typ des Bildes.
	private String mimeType;
	
	// Textuelle Beschreibung des Bildes.
	private String description;

	// Name des Bildes.
	private String name;

	/**
	 * Ein neues REST-Bild auf Basis eines vollstaendigen
	 * Datenbank-Bildes erstellen.
	 * @param dbPicture Vollstaendiges Bild-Objekt aus der Datenbank.
	 */
	public Picture(de.hska.iwii.picturecommunity.backend.entities.Picture dbPicture) {
		this.id = dbPicture.getId();
		this.mimeType = dbPicture.getMimeType();
		this.description = dbPicture.getDescription();
		this.name = dbPicture.getName();
	}
	
	/**
	 * Eindeutige Datenbank-ID des Bildes auslesen.
	 * @return Eindeutige Datenbank-ID des Bildes.
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Mime-Typ des Bildes auslesen.
	 * @return Mime-Typ des Bildes.
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Textuelle Beschreibung des Bildes auslesen.
	 * @return Textuelle Beschreibung des Bildes.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Namen des Bildes auslesen.
	 * @return Name des Bildes.
	 */
	public String getName() {
		return name;
	}
}
