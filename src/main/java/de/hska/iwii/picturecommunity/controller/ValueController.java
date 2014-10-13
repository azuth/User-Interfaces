package de.hska.iwii.picturecommunity.controller;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Test-Controller fuer die Beispielanwendung
 */
@Component
@Scope("session")
public class ValueController implements Serializable {

	// gespeicherter Wert
	private String value;

	/**
	 * Nach der Initialisierung eine Meldung in die Log-Datei schreiben
	 * (ist ueberfluessig).
	 */
	@PostConstruct
	public void wakeup() {
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, this.getClass().getName() + " ist wach.");
    }  

	/**
	 * Meldung mit Wert auslesen.
	 * @return Meldung
	 */
	public String getMessage() {
		return "Hallo mit Wert " + value;
	}

	/**
	 * Neuen Wert speichern.
	 */
	public synchronized void setValue(String value) {
		this.value = value;
	}

	public synchronized String getValue() {
		return value;
	}

	/**
	 * Simuliertes Speichern des Textes.
	 */
	public Object update() {
		return null;
	}
}
