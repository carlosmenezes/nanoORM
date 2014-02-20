package com.nanoorm;


/**
 * Interface DbCursor
 * Representa um cursor de dados, ou seja, um conjunto de linhas e colunas
 * resultantes de uma consulta sql.
 */
public interface DbCursor {

	/**
	 * Move o cursor para a próxima linha.
	 * @return       boolean
	 */
	public boolean next ();

	/**
	 * Move o cursor para a linha anterior.
	 * @return       boolean
	 */
	public boolean previous ();

	/**
	 * Move o cursor para a primeira linha.
	 */
	public void first ();

	/**
	 * Move o cursor para a última linha.
	 */
	public void last ();

	/**
	 * Retorna um objeto da coluna informada no parâmetro columnIndex.
	 * @return       Object
	 * @param        columnIndex Índice da coluna que se deseja obter o objeto.
	 */
	public Object getObject (int columnIndex);

	/**
	 * Retorna um objeto da coluna informada no parâmetro columnName.
	 * @return       Object
	 * @param        columnName Nome da coluna que se deseja obter o objeto.
	 */
	public Object getObject (String columnName);
	
	/**
	 * Retorna um objeto da coluna informada no parâmetro columnIndex.
	 * @return       T
	 * @param        columnName Índice da coluna que se deseja obter o objeto.
	 * @param        typeOfValue Tipo do valor a ser obtido da coluna.
	 */
	public <T> T getValue(int columnIndex, Class<T> typeOfValue);
	
	/**
	 * Retorna um objeto da coluna informada no parâmetro columnName.
	 * @return       T
	 * @param        columnName Nome da coluna que se deseja obter o objeto.
	 * @param        typeOfValue Tipo do valor a ser obtido da coluna.
	 */
	public <T> T getValue(String columnName, Class<T> typeOfValue);
	
	/**
	 * Fecha o cursor liberando recursos.
	 */
	void close();
	
	int getRowCount();
}