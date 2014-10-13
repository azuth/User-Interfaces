package de.hska.iwii.picturecommunity.backend.dao;

import java.lang.ref.WeakReference;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import de.hska.iwii.picturecommunity.backend.entities.User;

/**
 * Verwaltet alle Zugriffe auf den User in der Datenbank.
 * @author H. Vogelsang
 */
public class UserDAO extends AbstractDAO {
	
	// Cache aller geladenen Anwender.
	private WeakHashMap<User, WeakReference<User>> loadedUsers = new WeakHashMap<>();

	// Bildet einen Datenbankeintrag eines Anwenders auf das Anwenderobjekt ab.
	class UserRowMapper implements RowMapper<User> {
		
		private List<User> currentyLoadedUsers;

		/**
		 * Beruecksichtigt die in einem Aufruf bereits geladenen Anwender.
		 * @param currentyLoadedUsers Anwender, die beim Ermitteln der Freunde bereits geladen
		 * 		wurden. Diese Liste dient dazu, bei zyklischen Abhaengigkeiten Endlos-Rekursionen
		 * 		zu vermeiden.
		 */
		public UserRowMapper(List<User> currentyLoadedUsers) {
			this.currentyLoadedUsers = currentyLoadedUsers;
		}

		/**
		 * Startet mit einer leeren Liste bereits geladener Anwender.
		 */
		public UserRowMapper() {
			currentyLoadedUsers = new ArrayList<User>();
		}
		
		@Override
		public User mapRow(ResultSet resultSet, int rowNum) throws SQLException {
			User user = new User();
			user.setId(resultSet.getInt("id"));

			// Schon im Cache?
			WeakReference<User> userRef = loadedUsers.get(user);
			if (userRef != null) {
				User loadedUser = userRef.get();
				if (loadedUser != null) {
					return loadedUser;
				}
			}
			
			user.setRole(resultSet.getString("role"));
			user.setName(resultSet.getString("name"));
			user.setPassword(resultSet.getString("password"));
			user.setEmail(resultSet.getString("email"));
			if (!currentyLoadedUsers.contains(user)) {
				currentyLoadedUsers.add(user);
				fillFriendsOf(user, currentyLoadedUsers);
			}
			loadedUsers.put(user, new WeakReference<User>(user));
			return user;
		}
	}	
	
	// Bildet einen Datenbankeintrag eines Anwenders inkl. der Anzahl Bilder, die er
	// abgelegt hat, auf eine Map mit dem Anwenderobjekt als Schluessel und
	// der Anzahl als Wert ab.
	class UserActivityRowMapper implements RowMapper<Map.Entry<User, Long>> {
		private List<User> currentyLoadedUsers = new ArrayList<User>();

		@Override
		public Map.Entry<User, Long> mapRow(ResultSet resultSet, int rowNum) throws SQLException {
			User user = new User();
			user.setId(resultSet.getInt("id"));

			user.setRole(resultSet.getString("role"));
			user.setName(resultSet.getString("name"));
			user.setPassword(resultSet.getString("password"));
			user.setEmail(resultSet.getString("email"));
			long count = resultSet.getLong("cnt");
			
			// Schon im Cache?
			WeakReference<User> userRef = loadedUsers.get(user);
			User loadedUser = null;
			if (userRef != null) {
				loadedUser = loadedUsers.get(user).get();
				if (loadedUser != null) {
					user = loadedUser;
				}
			}
			if (loadedUser == null) {
				currentyLoadedUsers.add(user);
				loadedUsers.put(user, new WeakReference<User>(user));
				fillFriendsOf(user, currentyLoadedUsers);
			}
			return new AbstractMap.SimpleEntry<User, Long>(user, count);
		}
	}	
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	/**
	 * Legt den neuen Anwender in der Datenbank ab.
	 * @param user Neu erzeugter Anwender. Die EMail-Adresse des Anwenders darf
	 * 			noch nicht benutzt worden sein.
	 * @return <code>true</code>, wenn der Anwender angelegt werden konnte oder
	 * 			<code>false</code>, wenn der Anwender schon existiert.
	 */
	public boolean createUser(final User user) {
		TransactionCallback<Boolean> cb = new TransactionCallback<Boolean>() {

			@Override
			public Boolean doInTransaction(TransactionStatus txStatus) {
				return createUserInTransaction(user);
			}
		};
		
		return txTemplate.execute(cb);
	}
	
	/**
	 * Legt den neuen Anwender in der Datenbank ab (wird in einer Transaktion aufgerufen).
	 * @param user Neu erzeugter Anwender. Die EMail-Adresse des Anwenders darf
	 * 			noch nicht benutzt worden sein.
	 * @return <code>true</code>, wenn der Anwender angelegt werden konnte oder
	 * 			<code>false</code>, wenn der Anwender schon existiert.
	 */
	private boolean createUserInTransaction(User user) {
		String select = "SELECT * FROM member WHERE email = :email";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", user.getEmail());
		
		List<User> result = getNamedParameterJdbcTemplate().query(select, params, new UserRowMapper());	
		
		if (result.size() != 0) {
			return false;
		}

		String encodedPassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodedPassword);
		updateUserInTransaction(user);
		return true;
	}
	
	/**
	 * Aktualisiert einen Anwender in der Datenbank. Sind die Anwender,
	 * die <code>user</code> als Freund eingeladen haben, in dieser
	 * Beziehung auch veraendert worden, so werden diese Aenderungen
	 * nicht gesichert!
	 * @param user Anwender, dessen Eintrag aktualisiert werden soll.
	 */
	public void updateUser(final User user) {
		TransactionCallback<Void> cb = new TransactionCallback<Void>() {

			@Override
			public Void doInTransaction(TransactionStatus txStatus) {
				updateUserInTransaction(user);
				return null;
			}
		};
		
		txTemplate.execute(cb);
	}
	
	/**
	 * Aktualisiert einen Anwender in der Datenbank (wird in einer Transaktion aufgerufen).
	 * Ist ein Anwender als sein eigener Freund eingetragen, dann wird diese
	 * Freundschaftbeziehung wieder beendet.
	 * @param user Anwender, dessen Eintrag aktualisiert werden soll.
	 */
	private void updateUserInTransaction(User user) {
		String stmtUpdate;
		
		if (user.getId() == null) {
			stmtUpdate = "INSERT INTO member VALUES (DEFAULT, "
						+ ":email, :password, :role, :name)";
		}
		else {
			stmtUpdate = "UPDATE member SET "
							+ "role = :role, name = :name, password = :password, email = :email"
							+ " WHERE id = " + user.getId();
		}

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("role", user.getRole());
		params.addValue("name", user.getUsername());
		params.addValue("password", user.getPassword());
		params.addValue("email", user.getEmail());
		
		// Erzeugte Schluessel wieder auslesen
		KeyHolder keyHolder = new GeneratedKeyHolder();		
		getNamedParameterJdbcTemplate().update(stmtUpdate, params, keyHolder);

		// Datenbank-ID auslesen -> Neuer Eintrag.
		if (user.getId() == null) {
			// Workaround fuer Inkompatibilitaet zwischen den Datenbanken
			int id;
			if (keyHolder.getKeys().get("id") != null) {
				id = (Integer) keyHolder.getKeys().get("id");
			}
			else {
				id = keyHolder.getKey().intValue();
			}
			user.setId(id);
			loadedUsers.put(user, new WeakReference<User>(user));
		}
		// Alter Eintrag, Freunde koennten sich geaendert haben.
		// Die Implementierung ist ineffizient. Der Set der Freunde
		// koennte einen Aenderungsstatus besitzen.
		else {
			// Alte Beziehungen loeschen
			params = new MapSqlParameterSource();
			stmtUpdate = "DELETE FROM member_member WHERE member_id = " + user.getId();
			getNamedParameterJdbcTemplate().update(stmtUpdate, params);
			// Beziehungen mit Werten neu anlegen
			for (Iterator<User> iter = user.getFriendsOf().iterator(); iter.hasNext(); ) {
				User friend = iter.next();
				// Man kann nicht sein eigener Freund sein.
				if (!friend.equals(user)) {
					stmtUpdate = "INSERT INTO member_member VALUES (" + user.getId() + ", " + friend.getId() + ")";
					getNamedParameterJdbcTemplate().update(stmtUpdate, params);
				}
				else {
					iter.remove();
				}
			}
		}
	}
	
	/**
	 * Sucht nach allen Anwendern, deren Name das uebergebene Namensfragment enthaelt.
	 * Beispiel: Namensfragment "gel" findet "Vogelsang", "Igel", usw.
	 * Gross- und Kleinschreibung werden nicht unterschieden.
	 * @param namePart Namensfragment, nach dem gesucht werden soll. Als Sonderfall ist der
	 * 			Name "*" zugelassen. Damit werden alle Anwender ermittelt.
	 * @param currentUser Anwender, der die Suche durchfuehrt. Sollte sein Name
	 * 			das uebergebene Fragment beinhalten, dann wird er nicht in die
	 * 			Liste der Treffer aufgenommen. Hat <code>currentUser</code>
	 * 			den Wert <code>null</code>, dann werden alle Treffer beruecksichtigt.
	 * @return Liste aller Anwender, deren Namen das Namensfragment enthalten.
	 */
	public List<User> findUsersByName(String namePart, User currentUser) {
		String select = "SELECT * FROM member ";
		MapSqlParameterSource params = new MapSqlParameterSource();

		if (currentUser != null) {
			select += " WHERE id != :id";
			params.addValue("id", currentUser.getId());
		}
		if (!namePart.equals("*")) {
			if (currentUser != null) {
				select +=" AND name LIKE :namePart";
			}
			else {
				select +=" WHERE name LIKE :namePart";
			}
			params.addValue("namePart", "%" + namePart + "%");
		}

		return getNamedParameterJdbcTemplate().query(select, params, new UserRowMapper());
	}

	/**
	 * Sucht nach einen Anwender anhand seines Anmeldenamens.
	 * @param userName Name des Anwenders.
	 * @return Gefundenes Anwender-Objekt oder <code>null</code>, falls es diesen Anwender nicht gibt.
	 */
	public User findUserByName(String userName) {
		String select = "SELECT * FROM member WHERE name = :name";
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("name", userName);

		List<User> result = getNamedParameterJdbcTemplate().query(select, params, new UserRowMapper());	

		return result.size() == 1 ? result.get(0) : null;
	}

	/**
	 * Sucht nach einen Anwender anhand seiner ID.
	 * @param id ID des Anwenders.
	 * @return Gefundenes Anwender-Objekt oder <code>null</code>, falls es diesen Anwender nicht gibt.
	 */
	public User findUserByID(int id) {
		String select = "SELECT * FROM member WHERE id = :id";
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("id", id);

		List<User> result = getNamedParameterJdbcTemplate().query(select, params, new UserRowMapper());	

		return result.size() == 1 ? result.get(0) : null;
	}

	/**
	 * Sucht nach einen Anwender anhand seines Anemldenamens. Das uebergebene Passwort muss stimmen.
	 * @param userName Name des Anwenders.
	 * @param password Passwort des Anwenders.
	 * @return Gefundenes Anwender-Objekt oder <code>null</code>, falls es diesen Anwender nicht gibt
	 * 		oder das Password falsch ist.
	 */
	public User findUserByName(String userName, String password) {
		User user = findUserByName(userName);
		if (user != null) {
			if (passwordEncoder.matches(password, user.getPassword())) {
				return user;
			}
		}
		return null;
	}

	/**
	 * Sucht nach einem Anwender anhand seiner EMail-Adresse.
	 * @param mailaddress Mailadresse, nach dem gesucht werden soll.
	 * @return Gefundener Anwender oder <code>null<code>, wenn es diesen
	 * 			Anwender nicht gibt.
	 */
	public User findUserByMailaddress(String mailaddress) {
		String select = "SELECT * FROM member WHERE email = :email";
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", mailaddress);

		List<User> result = getNamedParameterJdbcTemplate().query(select, params, new UserRowMapper());	
		
		return result.size() == 1 ? result.get(0) : null;
	}

	/** 
	 * Alle Anwender ermitteln, die der uebergebenen Anwender als Freunde eingeladen hat.
	 * @param user Anwender, dessen Freunde ermittelt werden sollen.
	 * @return Alle Anwender ermitteln, die vom uebergebenen Anwender als Freunde eingeladen wurden.
	 */
	public List<User> findFriendsOfUser(User user) {
		String select = "SELECT * FROM member m, member_member mm WHERE m.id = mm.member_id and mm.friendsOf_id = " + user.getId();
		
		MapSqlParameterSource params = new MapSqlParameterSource();

		return getNamedParameterJdbcTemplate().query(select, params, new UserRowMapper());	
	}
	
	/** 
	 * Ermittelt die Anwender, die am meisten Bilder angelegt haben.
	 * @param limit Die ersten <code>limit</code> aktivsten Anwender sollen ausgelesen werden.
	 * 			Hat <code>limit</code> einen Wert kleiner oder gleich <code>0</code>, dann werden alle Anwender
	 * 			zurueck gegeben. 
	 * @return List-Objekt mit den aktivsten Anwendern, nach absteigender Aktivitaet sortiert. Die Liste
	 * 			enthaelt Entry-Eintrage, deren Schluessel jeweils den Anwender und deren Wert dessen
	 * 			Anzahl hochgeladener Bilder enthalten.
	 */
	public List<Map.Entry<User, Long>> getMostActiveUsers(int limit) {
		String select = "SELECT m.id, m.email, m.password, m.role, m.name, count(m.id) AS cnt FROM member m, Picture p WHERE m.id = p.owner_id GROUP BY m.id ORDER BY count(m.id) DESC";
		
		if (limit > 0) {
			select += " LIMIT " + limit;
		}
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		return getNamedParameterJdbcTemplate().query(select, params, new UserActivityRowMapper());	
	}
	
	/**
	 * Loescht den uebergebenen Anwender. Sollte er von anderen Anwendern
	 * als Freund eingeladen worden sein, dann wird er auch aus deren Beziehungen 
	 * geloescht.
	 * @param user Zu loeschender Anwender.
	 */
	public void deleteUser(User user) {
		// Ich habe keine Freunde mehr.
		MapSqlParameterSource params = new MapSqlParameterSource();
		String stmtUpdate = "DELETE FROM member_member WHERE member_id = " + user.getId();
		getNamedParameterJdbcTemplate().update(stmtUpdate, params);

		// Mich selbst loeschen.
		stmtUpdate = "DELETE FROM member WHERE id = " + user.getId();
		getNamedParameterJdbcTemplate().update(stmtUpdate, params);
		
		user.getFriendsOf().clear();
		user.setId(null);
	}
	
	/**
	 * Sucht die Anwender, die den uebergebenen als Freund deklariert haben,
	 * und traegt die Beziehung in das uebergebene Anwenderobjekt ein.
	 * @param user Anwender, zu dem alle anderen Anwender gesucht werden sollen,
	 * 		die diesen Anwender als Freund markiert haben.
	 * @param currentyLoadedUsers Anwender, die beim Ermitteln der Freunde bereits geladen
	 * 		wurden. Diese Liste dient dazu, bei zyklischen Abhaengigkeiten Endlos-Rekursionen
	 * 		zu vermeiden.
	 * @return Gefundene Anwender-Objekte.
	 */
	private void fillFriendsOf(User user, List<User> currentyLoadedUsers) {
		String select = "SELECT * FROM member m, member_member mm WHERE m.id = mm.member_id and mm.friendsOf_id = " + user.getId();
		
		MapSqlParameterSource params = new MapSqlParameterSource();

		List<User> friendsOf =  getNamedParameterJdbcTemplate().query(select, params, new UserRowMapper(currentyLoadedUsers));
		user.getFriendsOf().clear();
		user.getFriendsOf().addAll(friendsOf);
	}
}
