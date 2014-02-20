package com.nanoorm.android;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.nanoorm.DbConnection;
import com.nanoorm.DbCursor;

/**
 * Class AndroidDbConnection
 * @author Carlos Eduardo Pacheco Menezes
 */
public class AndroidDbConnection implements DbConnection {

	private SQLiteDatabase connection;
	private String databasePath;
	private Context context;

	/**
	 * Instância um AndroidDbConnection para o banco de dados informado no parâmetro databasePath.
	 * @param databasePath Caminho para o banco de dados.
	 * @param context Contexto da aplicação a ser utilizado na abertura da conexão com o banco.
	 */
	public AndroidDbConnection(String databasePath, Context context) {
		this.databasePath = databasePath;
		this.context = context;
		open();
	}
	
	/**
	 * Inicia uma transação com o banco de dados.
	 */
	public void beginTransaction ()	{
		if (!isOpen())
			throw new IllegalStateException("Para iniciar uma transação a conexão deve ser aberta antes.");
		connection.beginTransaction();
	}

	/**
	 * Finaliza a transação com o banco de dados com sucesso.
	 */
	public void commitTransaction () {
		if (!isOpen())
			throw new IllegalStateException("Para finalizar uma transação a conexão deve ser aberta antes.");
		connection.setTransactionSuccessful();
		connection.endTransaction();
	}

	/**
	 * Finaliza a transação com o banco de dados desfazendo todas as operações
	 * realizadas.
	 */
	public void rollbackTransaction () {
		if (!isOpen())
			throw new IllegalStateException("Para finalizar uma transação a conexão deve ser aberta antes.");
		connection.endTransaction();
	}

	/**
	 * executa uma instrução sql que não retorna resultados.
	 * @param        sql Instrução sql a ser executada.
	 */
	public void executeSql (String sql)	{
		if (!isOpen())
			throw new IllegalStateException("Para executar uma instrução sql a conexão deve ser aberta antes.");
		connection.execSQL(sql);
	}

	/**
	 * Executa uma consulta sql que retorna resultados.
	 * @return       com.nanoorm.DbCursor
	 * @param        sqlQuery Consulta a ser executada.
	 */
	public DbCursor executeSqlQuery (String sqlQuery) {
		if (!isOpen())
			throw new IllegalStateException("Para executar uma instrução sql a conexão deve ser aberta antes.");
				
		return new AndroidDbCursor(connection.rawQuery(sqlQuery, null));
	}

	@Override
	public void close() {
		connection.close();
	}

	@Override
	public boolean isOpen() {
		return connection != null && connection.isOpen();
	}

	@Override
	public void open() {
		if (connection == null || !isOpen())
			if (context != null)
				connection = context.openOrCreateDatabase(databasePath, SQLiteDatabase.OPEN_READWRITE, null);
			else
				connection = SQLiteDatabase.openOrCreateDatabase(databasePath, null);
	}

}