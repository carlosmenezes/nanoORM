package com.nanoorm;


/**
 * Interface DbConnection
 * Encapsula o comportamento comum as conexões utilizadas por diversos bancos de
 * dados. (vai encapsular a classe SQLiteDatabase do Android).
 */
public interface DbConnection {

	/**
	 * Inicia uma transação com o banco de dados.
	 */
	public void beginTransaction ();

	/**
	 * Finaliza a transação com o banco de dados com sucesso.
	 */
	public void commitTransaction ();

	/**
	 * Finaliza a transação com o banco de dados desfazendo todas as operações
	 * realizadas.
	 */
	public void rollbackTransaction ();

	/**
	 * executa uma instrução sql que não retorna resultados.
	 * @param        sql Instrução sql a ser executada.
	 */
	public void executeSql (String sql);

	/**
	 * Executa uma consulta sql que retorna resultados.
	 * @return       com.nanoorm.DbCursor
	 * @param        sqlQuery Consulta a ser executada.
	 */
	public DbCursor executeSqlQuery (String sqlQuery);
	
	/**
	 * Abre uma conexão com banco de dados.
	 */
	public void open();

	/**
	 * Fecha uma conexão  ativa com o banco de dados.
	 */
	public void close();

	/**
	 * Indica se a conexão com o banco de dados está ativa.
	 * @return       boolean
	 */
	public boolean isOpen();
}