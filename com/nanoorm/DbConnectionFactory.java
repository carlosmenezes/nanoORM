package com.nanoorm;

import com.nanoorm.android.AndroidConfiguration;
import com.nanoorm.android.AndroidDbConnection;

/**
 * Classe responsável por instanciar objetos de classes
 * que implementam a interface DbConnection.
 * @author Carlos Eduardo Pacheco Menezes
 */
public class DbConnectionFactory {
	
	private DbConnectionFactory() { }

	/**
	 * Fabrica instâncias de DbConnection.
	 * @param configuration Configuração com as informações necessárias à fabricação da conexão.
	 * @throws Exception Quando é solicitada uma conexão de um banco de dados não suportado pelo framework.
	 */	
	public static DbConnection getConnection(Configuration configuration) throws Exception {
		DbConnection connection = null;
		switch (configuration.getDataSourceType()) {		
		
		case ANDROID:
			if (configuration instanceof AndroidConfiguration)
				connection = new AndroidDbConnection(configuration.getUrl(), ((AndroidConfiguration)configuration).getContext());
			else
				connection = new AndroidDbConnection(configuration.getUrl(), null);
			break;
			
		case JDBC:
			
			break;
		default:
			throw new Exception("Banco de dados não suportado.");
		}
		
		return connection;
	}
		
}
