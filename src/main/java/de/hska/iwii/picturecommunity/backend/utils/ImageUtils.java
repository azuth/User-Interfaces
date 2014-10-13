package de.hska.iwii.picturecommunity.backend.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.coobird.thumbnailator.Thumbnails;

/**
 * Die Klasse bietet Hilfsmethoden zur Bearbeitung von Bildern
 * an. Momentan gibt es nur eine Methode zur Skalierung.
 */
public class ImageUtils {

	/**
	 * Minimale Bildbreite und -hoehe in Pixeln, die beim Skalieren erlaubt sind.
	 */
	public static final int MIN_IMAGE_SIZE = 10;

	/**
	 * Maximale Bildbreite und -hoehe in Pixeln, die beim Skalieren erlaubt sind.
	 */
	public static final int MAX_IMAGE_SIZE = 10000;
	
	/**
	 * Skaliert ein Bild unter Beibehaltung seiner Proprtionen. Sind Breite und
	 * Hoehe nicht <code>null</code>, dann werden diese Angaben als Boundong-Box,
	 * in die das Bild eingepasst werden soll, interpretiert.
	 * @param pictureData Daten des Bildes.
	 * @param newImageWidth Breite, auf die das Bild skaliert werden soll.
	 * 		Ist die Breite <code>null</code>, dann wird nur die Hoehe
	 * 		beruecksichtigt.
	 * @param newImageHeight Hoehe, auf die das Bild skaliert werden soll.
	 * 		Ist die Hoehe <code>null</code>, dann wird nur die Breite
	 * 		beruecksichtigt.
	 * @return Neue Bilddaten des skalierten Bildes oder die Originaldaten, falls
	 * 		die neue Breite oder Hoehe ausserhalb des Gueltigkeitsbereichs zwischen
	 * 		<code>MIN_IMAGE_SIZE</code> und <code>MAX_IMAGE_SIZE</code> liegen.
	 * 		Das Bild wird auch unveraendert zurueckgegeben, wenn Breite und
	 * 		Hoehe beide <code>null</code> sind.
	 * @throws IOException
	 */
	public static byte[] scale(byte[] pictureData, Integer newImageWidth, Integer newImageHeight) throws IOException {
		// Breite ausserhalb der Grenzne?
		if (newImageWidth != null && (newImageWidth < MIN_IMAGE_SIZE || newImageWidth > MAX_IMAGE_SIZE)) {
			newImageWidth = null;
		}
		// Hoehe ausserhalb der Grenzne?
		if (newImageHeight != null && (newImageHeight < MIN_IMAGE_SIZE || newImageHeight > MAX_IMAGE_SIZE)) {
			newImageHeight = null;
		}
		
		// Skalierung?
		if (newImageWidth != null || newImageHeight != null) {
			InputStream input = new ByteArrayInputStream(pictureData);
			Thumbnails.Builder<?> scaledInput = Thumbnails.of(input);
			
			// Breite und Hoehe angegeben
			if (newImageWidth != null && newImageHeight != null) {
				scaledInput.size(newImageWidth, newImageHeight);
			}
			// Nur Breite angegeben
			else if (newImageWidth != null) {
				scaledInput.width(newImageWidth);
			}
			// Nur Hoehe angegeben
			else if (newImageHeight != null) {
				scaledInput.height(newImageHeight);
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			scaledInput.toOutputStream(out);
			return out.toByteArray();
		}
		// Keine Skalierung
		else {
			return pictureData;
		}
	}
}
