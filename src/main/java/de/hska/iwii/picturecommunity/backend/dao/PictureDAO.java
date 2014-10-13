package de.hska.iwii.picturecommunity.backend.dao;

import java.lang.ref.WeakReference;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import de.hska.iwii.picturecommunity.backend.entities.User;
import de.hska.iwii.picturecommunity.backend.entities.Picture;

/**
 * Verwaltet alle Zugriffe auf die Bilder in der Datenbank.
 * @author Holger Vogelsang
 */
public class PictureDAO extends AbstractDAO {
	
	@Autowired
	private UserDAO userDAO;
	
	// Cache aller geladenen Bilder.
	private Map<Picture, WeakReference<Picture>> loadedPictures = Collections.synchronizedMap(new WeakHashMap<Picture, WeakReference<Picture>>());
	
	// Bildet einen Datenbankeintrag eines Bilders auf das Bildobjekt ab.
	class PictureRowMapper implements RowMapper<Picture> {
		@Override
		public Picture mapRow(ResultSet resultSet, int rowNum) throws SQLException {
			Picture picture = new Picture();
			picture.setId(resultSet.getInt("id"));
			
			// Schon im Cache?
			WeakReference<Picture> picRef = loadedPictures.get(picture);
			if (picRef != null) {
				Picture loadedPicture = loadedPictures.get(picture).get();
				if (loadedPicture != null) {
					return loadedPicture;
				}
			}

			picture.setMimeType(resultSet.getString("mimeType"));
			picture.setData(resultSet.getBytes("data"));
			picture.setDescription(resultSet.getString("description"));
			picture.setCreationDate(resultSet.getDate("creationDate"));
			picture.setName(resultSet.getString("name"));
			picture.setPublicVisible(resultSet.getBoolean("publicVisible"));
			picture.setCreator(userDAO.findUserByID(resultSet.getInt("owner_id")));
			loadedPictures.put(picture, new WeakReference<Picture>(picture));
			return picture;
		}
	}	
	
	/**
	 * Liest alle abgelegten Bilder eines Anwenders aus.
	 * @param user Anwender, dessen Bilder ausgelesen werden sollen. Wenn der
	 * 			Parameter <code>null</code> ist, werden alle oeffentlichen Bilder
	 * 			ermittelt und der Parameter <code>onlyPublicVisible</code>
	 * 			ignoriert.
	 * @param firstResult Index des ersten Treffers, der zurueckgegeben werden soll.
	 * 			Ist der Parameter 0, dann wird ab dem ersten Treffer zurueckgegeben.
	 * @param maxResults Anzahl an Bildern, die zurueckgegeben werden sollen.
	 * 			Ist der Parameter Integer.MAX_VALUE, dann werden alle Treffer
	 * 			zurueckgegeben.
	 * @param onlyPublicVisible Sollen nur die Bilder eingelesen werden, die
	 * 			ohne Anmeldung sichtbar angezeigt werden koennen? Dann ist der Parameter <code>true</code>.
	 * 			Ist er <code>false</code>, dann werden alle Bilder zurueckgegeben. 
	 * @return Alle Bilder eines Anwenders.
	 */
	public List<Picture> getPictures(User user, int firstResult, int maxResults, boolean onlyPublicVisible) {

		MapSqlParameterSource params = new MapSqlParameterSource();

		String select = "SELECT * FROM Picture ";
		
		String userCond = null;
		String publicCond = null;
		
		if (user != null) {
			userCond = "owner_id = :owner_id" ;
			params.addValue("owner_id", user.getId());
		}
		
		if (onlyPublicVisible || user == null) {
			publicCond = "publicVisible = true";
		}
		
		if (userCond != null && publicCond != null) {
			select += "WHERE " + userCond + " AND " + publicCond;
		}
		else {
			if (userCond != null) {
				select += "WHERE " + userCond;
			}
			else {
				select += "WHERE " + publicCond;
			}
		}
		select += " ORDER BY creationDate ASC ";

		if (maxResults > 0 && maxResults < Integer.MAX_VALUE) {
			select += " LIMIT " + maxResults;
		}
		else {
			select += " LIMIT " + Integer.MAX_VALUE;
		}
		select += " OFFSET " + firstResult;
		
		return getNamedParameterJdbcTemplate().query(select, params, new PictureRowMapper());	
	}

	/**
	 * Liest ein Bild eines Anwenders aus.
	 * @param id ID des gesuchten Bildes.
	 * @return Das gewuenschte Bild des Anwenders.
	 */
	public Picture getPicture(int id) {
		MapSqlParameterSource params = new MapSqlParameterSource();

		String select = "SELECT * FROM Picture WHERE id = " + id;
		List<Picture> result = getNamedParameterJdbcTemplate().query(select, params, new PictureRowMapper());
		
		return result.size() == 1 ? result.get(0) : null;
	}

	/** 
	 * Ermittelt fuer einen Anwender, wieviele Bilder er bereits gespeichert hat.
	 * @param user Anwender, dessen Anzahl an Bildern ermittelt wird.
	 * @return Anzahl Bilder des Anwenders.
	 */
	public long getPictureCount(User user) {
		MapSqlParameterSource params = new MapSqlParameterSource();

		String select = "SELECT COUNT(owner_id) FROM Picture WHERE owner_id = " + user.getId();
		List<Long> result = getNamedParameterJdbcTemplate().query(select, params, new RowMapper<Long>() {
			@Override
			public Long mapRow(ResultSet resultSet, int rowNum) throws SQLException {
				return resultSet.getLong(1);
			}
		});
		
		return result.size() == 1 ? result.get(0) : 0;
	}

	/**
	 * Legt zu einem Anwender ein neues Bild ab.
	 * @param user Anwender, dem das Bild zugeordnet wird.
	 * @param picture Bild, das dem Anwender zugeordnet wird. Das Bild
	 * 		muss bis auf den Besitzer ("owner") sowie das Datum
	 * 		("creationDate") vollstaendig gefuellt sein.
	 */
	public void createPicture(User user, Picture picture) {
		picture.setCreator(user);
		picture.setCreationDate(new Date());
		
		String stmtUpdate;
		
		if (picture.getId() == null) {
			stmtUpdate = "INSERT INTO picture VALUES (DEFAULT, :creationDate, "
						+ ":data, :description, :mimeType, :name, :publicVisible, :owner_id)";
		}
		else {
			stmtUpdate = "UPDATE picture SET "
							+ "mimeType = :mimeType, data = :data, description = :description, creationDate = :creationDate,"
							+ " name = :name, publicVisible = :publicVisible, owner_id = :owner_id  WHERE id = " + picture.getId();
		}

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("mimeType", picture.getMimeType());
		params.addValue("data", picture.getData());
		params.addValue("description", picture.getDescription());
		params.addValue("creationDate", picture.getCreationDate());
		params.addValue("name", picture.getName());
		params.addValue("publicVisible", picture.isPublicVisible());
		params.addValue("owner_id", picture.getOwner().getId());
		
		// Erzeugte Schluessel wieder auslesen
		KeyHolder keyHolder = new GeneratedKeyHolder();		
		getNamedParameterJdbcTemplate().update(stmtUpdate, params, keyHolder);

		// Datenbank-ID auslesen -> Neuer Eintrag.
		if (picture.getId() == null) {
			// Workaround fuer Inkompatibilitaet zwischen den Datenbanken
			int id;
			if (keyHolder.getKeys().get("id") != null) {
				id = (Integer) keyHolder.getKeys().get("id");
			}
			else {
				id = keyHolder.getKey().intValue();
			}
			picture.setId(id);
			loadedPictures.put(picture, new WeakReference<Picture>(picture));
		}
	}

	/**
	 * Loescht ein Bild eines Anwenders. 
	 * @param picture Zu loeschendes Bild.
	 */
	public void deletePicture(Picture picture) {
		MapSqlParameterSource params = new MapSqlParameterSource();

		String stmtUpdate = "DELETE FROM picture WHERE id = " + picture.getId();
		getNamedParameterJdbcTemplate().update(stmtUpdate, params);
		
		picture.setCreator(null);
		picture.setId(null);
	}
}
