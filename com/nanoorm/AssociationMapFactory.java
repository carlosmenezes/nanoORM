package com.nanoorm;

import com.nanoorm.android.AndroidAssociationMap;
import com.nanoorm.mappings.AssociationMap;

/**
 * Fábrica de instâncias de AssociationMap.
 */
public class AssociationMapFactory {

	private AssociationMapFactory() { }
	
	/**
	 * Constrói uma instância de AssociationMap para o tipo de fonte de dados
	 * informada no parâmetro dataSourceType. 	
	 * @param dataSourceType Tipo da fonte de dados para a qual será construida a instância de AssociationMap.
	 * @throws Exception Quando é solicitada a construção de uma instância de AssociationMap
	 * para uma fonte de dados não suportada pelo framework. 
	 */
	public static AssociationMap buildAssociationMap(DataSourceType dataSourceType) throws Exception {
		switch (dataSourceType) {
		case ANDROID:
			return new AndroidAssociationMap();

		default:
			throw new Exception("Banco de dados não suportado.");
		}
	}
	
}
