package de.hska.iwii.picturecommunity.backend.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.transaction.support.TransactionTemplate;

import de.hska.iwii.picturecommunity.backend.database.Initializer;

/**
 * Abstrakte Basisklasse aller DAO.
 */
public class AbstractDAO extends NamedParameterJdbcDaoSupport {

	@Autowired
	private Initializer initializer;
	
	protected TransactionTemplate txTemplate;

	/**
	 * Per DI injizieren.
	 */
	public void setTransactionTemplate(TransactionTemplate txTemplate) {
		this.txTemplate = txTemplate;
	}
}
