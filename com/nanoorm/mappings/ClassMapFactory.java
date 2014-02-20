package com.nanoorm.mappings;

import com.nanoorm.DataSource;
import com.nanoorm.android.AndroidClassMap;

/**
 * Classe responsável por fabricar instâncias de ClassMap.
 */
public class ClassMapFactory {

	/**
	 * Constrói uma instância de ClassMap da classe informada no parâmetro
	 * entityClass para a Implementação  da DataSource informada no parâmetro dataSource.
	 * @return       com.nanoorm.mappings.ClassMap
	 * @param        entityClass Classe para qual se deseja obter a instância de
	 * ClassMap.
	 * @param        dataSource Fonte de dados para qual se deseja obter a
	 * instância da ClassMap.
	 * @throws IllegalArgumentException Lançada quando o parâmetro dataSource possui
	 * um DataSourceType não suportado.
	 */
	public static <T> ClassMap<T> getClassMap (Class<T> entityClass, DataSource dataSource) throws IllegalArgumentException {
		ClassMap<T> classMap = null;
		switch (dataSource.getDataSourceType()) {
		case ANDROID:
			classMap = new AndroidClassMap<T>(entityClass, dataSource);	break;
		case JDBC:
			throw new UnsupportedOperationException("Fontes de dados JDBC ainda não são suportadas.");
		default:
			throw new IllegalArgumentException("Tipo de fonte de dados não suportado: " + dataSource.getDataSourceType() + ".");
		}
		return classMap;
	}

	/**
	 * @return       com.nanoorm.mappings.ClassMapFactory
	 */
	private ClassMapFactory () { }
		
}
