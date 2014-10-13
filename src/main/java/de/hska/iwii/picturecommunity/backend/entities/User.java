package de.hska.iwii.picturecommunity.backend.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class User implements UserDetails, Serializable {
	private static final long serialVersionUID = 2992954192934331599L;
	public static final String ROLE_USER = "user";
	public static final String ROLE_ADMIN = "admin";

	// Eindeutige Datenbank-ID des Anwenders
	private Integer id;

	// Rolle, der der Anwender zugeordnet ist.
	private String role;

	// Name des Anwenders, muss eindeutig sein.
	private String name;
	
	// Passwort des Anwenders
	private String password;

	// Mailadresse des Anwenders, muss eindeutig sein
	private String email;

	// Andere Anwender, die mich als Freund deklariert haben (von 
	// diesen Anwendern darf ich die privaten Bilder sehen).
	private Set<User> friendsOf = new HashSet<User>();

	/**
	 * Einen neuen Anwender anlegen.
	 */
	public User() {
	}

	/**
	 * Einen neuen Anwender anlegen. Bei allen Parametern werden fuehrende
	 * und abschliessende Leerzeichen sowie Tabulatoren entfernt.
	 * @param email EMail-Adressse des Anwenders.
	 * @param password Passwort des Anwenders.
	 * @param name Name des Anwenders.
	 * @param role Name der Rolle, der der Anwender zugeordnet ist.
	 */
	public User(String email, String password, String name, String role) {
		this.email = email.trim();
		this.password = password.trim();
		this.name = name.trim();
		this.role = role.trim();
	}

	/**
	 * Eindeutige Datenbank-ID des Anwenders eintragen.
	 * @param id Eindeutige Datenbank-ID des Anwenders.
	 */
	public void setId(Integer id) {
		this.id = id;
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
	 * Dem Anwender eine EMail-Adresse zuordnen.
	 * @param email EMail-Adresse des Anwenders.
	 * 		Es werden fuehrende und abschliessende Leerzeichen
	 * 		sowie Tabulatoren entfernt.
	 */
	public void setEmail(String email) {
		this.email = email.trim();
	}

	/**
	 * Rollenname des Anwenders auslesen.
	 * @return Rollenname des Anwenders.
	 */
	public String getRole() {
		return role;
	}

	/**
	 * Dem Anwender eine Rolle zuordnen.
	 * @param role Name der Rolle des Anwenders.
	 * 		Es werden fuehrende und abschliessende Leerzeichen
	 * 		sowie Tabulatoren entfernt.
	 */
	public void setRole(String role) {
		this.role = role.trim();
	}

	/**
	 * Passwort des Anwenders auslesen.
	 * @return Passwort des Anwenders.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Dem Anwender ein Passwort zuordnen.
	 * @param password Passwort des Anwenders.
	 * 		Es werden fuehrende und abschliessende Leerzeichen
	 * 		sowie Tabulatoren entfernt.
	 */
	public void setPassword(String password) {
		this.password = password.trim();
	}

	/**
	 * Andere Anwender auslesen, die mich als Freund eingeladen haben.
	 * @return Andere Anwender, die mich als Freund eingeladen haben.
	 */
	public Set<User> getFriendsOf() {
		return friendsOf;
	}

	/**
	 * Namen des Anwenders auslesen.
	 * @return Name des Anwenders.
	 */
	@Override
	public String getUsername() {
		return name;
	}
	public String getName() {
		return getUsername();
	}

	/**
	 * Dem Anwender einen Namen zuordnen.
	 * @param name Name des Anwenders.
	 * 		Es werden fuehrende und abschliessende Leerzeichen
	 * 		sowie Tabulatoren entfernt.
	 */
	public void setName(String name) {
		this.name = name.trim();
	}

	/**
	 * Zwei Anwender sind dann gleich, wenn sie dieselbe ID
	 * besitzen.
	 * @param other Zu vergleichender Anwender.
	 * @return <code>true</code>, wenn der Anwender gleich dem aktuellen
	 * 			Anwender-Objekt ist.
	 */
	@Override
	public boolean equals(Object other) {
		if (other != null && other.getClass() == getClass()) {
			User user = (User) other;
			if (getId() == null && user.getId() == null) {
				return true;
			}
			if (getId() == null && user.getId() != null ||
			    getId() != null && user.getId() == null) {
				return false;
			}
			return getId().equals(user.getId());
		}
		return false;
	}
	
	/**
	 * Als Hash-Code dient hier nur die ID, die sich bei allen Objekten
	 * unterscheidet.
	 * @return Hash-Code eines Anwenders.
	 */
	@Override
	public int hashCode() {
		return getId();
	}

	/**
	 * Fuer eine lesbarerer textuelle des Anwenders.
	 * @return Textuelle Darstellung des Anwenders.
	 */
	@Override
	public String toString() {
		return "[User: id=" + id + " email=" + email + " role=" + role + " follows.size()="
				+ getFriendsOf().size() + "]";
	}

	/**
	 * Fuer Spring-Security: Welche Rechte besitzt der Anwender?
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		ArrayList<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority(ROLE_USER));
		if (getRole().equals(ROLE_ADMIN)) {
			authorities.add(new SimpleGrantedAuthority(ROLE_ADMIN));
		}
		return authorities;
	}

	/**
	 * Fuer Spring-Security: Ist das Konto nicht abgelaufen?
	 * @return Immer <code>true</code>, weil das Konto nie verfaellt.
	 */
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	/**
	 * Fuer Spring-Security: Ist das Konto nicht gesperrt?
	 * @return Immer <code>true</code>, weil das Konto nie gesperrt wird.
	 */
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	/**
	 * Fuer Spring-Security: Sind die Berechtigungen nicht ungueltig?
	 * @return Immer <code>true</code>, weil die Berechtigungen immer gueltig bleiben.
	 */
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	/**
	 * Fuer Spring-Security: Ist das Anwenderkonto aktiv?
	 * @return Immer <code>true</code>, weil das Konto immer aktiv bleibt.
	 */
	@Override
	public boolean isEnabled() {
		return true;
	}
}
