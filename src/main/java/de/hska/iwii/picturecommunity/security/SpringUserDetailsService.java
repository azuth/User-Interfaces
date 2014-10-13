package de.hska.iwii.picturecommunity.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import de.hska.iwii.picturecommunity.backend.dao.UserDAO;

public class SpringUserDetailsService implements UserDetailsService {

	@Autowired
	private UserDAO userDAO;
	
	/**
	 * Wird von Spring-Security verwendet, um beim Anmelden die
	 * Anwender-Daten anhand seines Namens zu finden. Das in
	 * UserDetails vorhandene Passwort wird spaeter mit dem
	 * Anmeldepasswort vergleichen.
	 * @param userName Name des Anwenders beim Anmelden.
	 * @return Informationen ueber den Anwender wie Passwort und 
	 * 			Rollen, die ihm zugeordnet sind.
	 */
	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
		return userDAO.findUserByName(userName);
	}
}
