package com.nanoorm.android;

import com.nanoorm.DbConnection;
import com.nanoorm.DbCursor;

/**
 * Representa uma sequência geradora de chaves primárias para a plataforma Android.
 */
public class AndroidSequence {

	private static boolean sequenceExists(DbConnection connection) {
		connection.open();
	 	DbCursor cursor = connection.executeSqlQuery("SELECT NAME FROM SQLITE_MASTER WHERE NAME LIKE \'nanoORMSequence\'");
	 	int sequenceLength = cursor.getRowCount();
	 	cursor.close();
	 	connection.close();
	 	return sequenceLength > 0;
	}
	
	private static void buildSequence(DbConnection connection) {
		connection.open();
		connection.executeSql("CREATE TABLE nanoORMSequence (idSequence INTEGER)");
		connection.executeSql("INSERT INTO nanoORMSequence (idsequence) VALUES (0)");
		connection.close();
	}
	
	/**
	 * Gera uma chave primária do tipo informado no parâmetro typeOfKey,
	 * o tipo desejado para a chave primária deve estender Number.
	 * @param connection Conexão a ser utilizada para a criação da chave.
	 * @param typeOfKey Tipo desejado para a chave primária a ser gerada.
	 */
	public static <T> T generateKey(DbConnection connection, Class<T> typeOfKey) {	
		if (!sequenceExists(connection))
			buildSequence(connection);
		
		connection.open();
		DbCursor cursor = connection.executeSqlQuery("SELECT idSequence + 1 FROM nanoORMSequence");
		T key = cursor.getValue(0, typeOfKey);
		connection.executeSql("UPDATE nanoORMSequence SET idSequence = " +
				key + " WHERE idSequence = " + (((Number)key).longValue() - 1));
		cursor.close();
		connection.close();
		
		return key;
	}
	
}
