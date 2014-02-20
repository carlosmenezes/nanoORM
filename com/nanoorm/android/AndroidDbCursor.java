package com.nanoorm.android;

import java.util.Calendar;

import android.database.Cursor;

import com.nanoorm.DbCursor;


/**
 * Class AndroidDbCursor
 */
public class AndroidDbCursor implements DbCursor {

	private Cursor cursor;

	/**
	 * Instância um AndroidDbCursor com o cursor de dados informado no parâmetro cursor.
	 * @param cursor Cursor a ser encapsulado.
	 */
	public AndroidDbCursor(Cursor cursor) {
		this.cursor = cursor;
		first();
	}

	/**
	 * Move o cursor para a próxima linha.
	 * @return       boolean
	 */
	public boolean next () {
		return cursor.moveToNext();
	}

	/**
	 * Move o cursor para a linha anterior.
	 * @return       boolean
	 */
	public boolean previous () {
		return cursor.moveToPrevious();
	}

	/**
	 * Move o cursor para a primeira linha.
	 */
	public void first () {
		cursor.moveToFirst();
	}

	/**
	 * Move o cursor para a ultima linha.
	 */
	public void last ()	{
		cursor.moveToLast();
	}

	/**
	 * Retorna um objeto da coluna informada no parâmetro columnIndex.
	 * @return       Object
	 * @param        columnIndex Índice da coluna que se deseja obter o objeto.
	 */
	public Object getObject (int columnIndex) {	
		throw new UnsupportedOperationException("Método não implementado.");
	}

	/**
	 * Retorna um objeto da coluna informada no parâmetro columnName.
	 * @return       Object
	 * @param        columnName Nome da coluna que se deseja obter o objeto.
	 */
	public Object getObject (String columnName)	{
		return getObject(cursor.getColumnIndex(columnName));
	}
	
	/**
	 * Retorna um objeto da coluna informada no parâmetro columnIndex.
	 * @return       T
	 * @param        columnName Índice da coluna que se deseja obter o objeto.
	 * @param        typeOfValue Tipo do valor a ser obtido da coluna.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getValue(int columnIndex, Class<T> typeOfValue) {
		Object value = null;
		if ("String".equalsIgnoreCase(typeOfValue.getSimpleName()))
			value = cursor.getString(columnIndex);
		else if ("Double".equalsIgnoreCase(typeOfValue.getSimpleName()))
			value = cursor.getDouble(columnIndex);
		else if ("Integer".equals(typeOfValue.getSimpleName()) || "int".equals(typeOfValue.getSimpleName()))
			value = cursor.getInt(columnIndex);
		else if ("Long".equalsIgnoreCase(typeOfValue.getSimpleName())) 
			value = cursor.getLong(columnIndex);
		else if ("Short".equalsIgnoreCase(typeOfValue.getSimpleName()))
			value = cursor.getShort(columnIndex);
		else if ("Float".equalsIgnoreCase(typeOfValue.getSimpleName()))
			value = cursor.getFloat(columnIndex);
		else if ("Byte[]".equalsIgnoreCase(typeOfValue.getSimpleName()))
			value = cursor.getBlob(columnIndex);
		else if ("java.util.Date".equals(typeOfValue.getName()))
			value = new java.util.Date(cursor.getLong(columnIndex));
		else if ("java.sql.Date".equals(typeOfValue.getName()))
			value = new java.sql.Date(cursor.getLong(columnIndex));
		else if ("Calendar".equals(typeOfValue.getSimpleName())) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(cursor.getLong(columnIndex));
			value = calendar;
		} else
			throw new IllegalArgumentException("O tipo " + typeOfValue + " não é suportado por este banco de dados.");
		
		return (T) value;
	}
	
	/**
	 * Retorna um objeto da coluna informada no parâmetro columnName.
	 * @return       T
	 * @param        columnName Nome da coluna que se deseja obter o objeto.
	 * @param        typeOfValue Tipo do valor a ser obtido da coluna.
	 */
	public <T> T getValue(String columnName, Class<T> typeOfValue) {		
		return getValue(cursor.getColumnIndex(columnName), typeOfValue);		
	}

	@Override
	public void close() {
		cursor.close();
	}

	@Override
	public int getRowCount() {
		return cursor.getCount();
	}
	
}