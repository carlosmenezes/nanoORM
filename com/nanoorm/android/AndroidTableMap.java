package com.nanoorm.android;

import java.lang.reflect.Field;

import com.nanoorm.mappings.TableMap;

public class AndroidTableMap<T> extends TableMap<T> {

	public AndroidTableMap(Class<T> entityClassType) {
		super(entityClassType);		
		createTableInstruction = "CREATE TABLE :table(:columns)";
	}

	@Override
	protected String buildAlterColumn(String column, String newType) {
		return null;
	}

	@Override
	protected String buildCreateTable() {
		StringBuilder sql = new StringBuilder(createTableInstruction);
		int replaceStart = sql.indexOf(":table");
		int replaceEnd = replaceStart + 6;
		
		sql.replace(replaceStart, replaceEnd, tableName);
		
		//Adiciona o campo da chave primária
		String sqlType = "INTEGER PRIMARY KEY";
		String column = primaryKey;
		replaceStart = sql.indexOf(":columns");
		replaceEnd = replaceStart + 8;
		sql.replace(replaceStart, replaceEnd, String.format("%s %s, :columns", column, sqlType));		
		
		for (Field property : tableColumns.keySet())
			if (property != idProperty) {			
				sqlType = getSqlType(property.getType());
				column = tableColumns.get(property);
				replaceStart = sql.indexOf(":columns");
				replaceEnd = replaceStart + 8;
				sql.replace(replaceStart, replaceEnd, String.format("%s %s, :columns", column, sqlType));
			}		
		sql.delete(sql.indexOf(", :columns"), sql.indexOf(", :columns") + 10);		
		return sql.toString();
	}

	@Override
	protected String buildForeignKey(String foreignKeyColumn,
			String primaryKeyColumn, String foreignKeyTable) {
		return null;
	}

	@Override
	protected String buildPrimaryKey() {
		return null;
	}

	@Override
	protected void getTableInfo() {
		
	}

	@Override
	protected String getSqlType(Class<?> propertyType) {
		String value = "";
		if ("String".equalsIgnoreCase(propertyType.getSimpleName()))
			value = "TEXT";
		else if ("Double".equalsIgnoreCase(propertyType.getSimpleName()) || "Float".equalsIgnoreCase(propertyType.getSimpleName()))
			value = "REAL";
		else if ("Integer".equals(propertyType.getSimpleName()) || "int".equals(propertyType.getSimpleName()) ||
				"Long".equalsIgnoreCase(propertyType.getSimpleName()) || "Short".equalsIgnoreCase(propertyType.getSimpleName()) ||
				"java.util.Date".equals(propertyType.getName()) || "java.sql.Date".equals(propertyType.getName()) ||
				"Calendar".equals(propertyType.getSimpleName()))
			value = "INTEGER";
		else if ("Byte[]".equalsIgnoreCase(propertyType.getSimpleName()))
			value = "BLOB";	
		else
			throw new IllegalArgumentException("O tipo " + propertyType + " não é suportado por este banco de dados.");
		return value;
	}
	
}