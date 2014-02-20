package com.nanoorm.mappings;

import com.nanoorm.DataSourceType;
import com.nanoorm.android.AndroidTableMap;

/**
 * Class TableMapFactory
 */
public class TableMapFactory {

	/**
	 * Obtém o TableMap para a classe informada.
	 * @param <T>
	 * @param        entityClass Classe que se deseja obter o TableMap
	 * @param        dataSourceType Tipo da fonte de dados que se deseja obter o
	 * TableMap.
	 */
	public static <T> TableMap<T> getTableMap (Class<T> entityClass, DataSourceType dataSourceType) {
		
		TableMap<T> tableMap = null;
		switch (dataSourceType) {
		case ANDROID:
			tableMap = new AndroidTableMap<T>(entityClass);	break;
		case JDBC:
			throw new UnsupportedOperationException("Fontes de dados JDBC ainda não são suportadas.");
		default:
			throw new IllegalArgumentException("Tipo de fonte de dados não suportado: " + dataSourceType + ".");
		}
		return tableMap;
	}

	private TableMapFactory () {
		
		
	}

}
