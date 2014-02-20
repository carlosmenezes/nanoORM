package com.nanoorm.mappings;

import java.lang.reflect.Field;


/**
 * Interface AssociationMap
 * A interface AssociationMap possui os métodos necessários para o mapeamento das
 * associações entre objetos de duas classes diferentes e suas respectivas
 * instruções SQL.
 * 
 */
public interface AssociationMap {

	/**
	 * Constrói uma instrução SQL select que obtém 1 ou n objetos da classe informada
	 * no parâmetro entityClassB relacionados a objetos informados no parâmetro
	 * entityClassA.
	 * @return       String
	 * @param        entityClassA Representa a entidade forte do relacionamento.
	 * @param        entityClassB Representa a entidade fraca do relacionamento.
	 */
	public String getSelectMapping (ClassMap<?> entityClassA, ClassMap<?> entityClassB, Field property);

	/**
	 * @return       String
	 * @param        entityClassA Representa a entidade forte do relacionamento.
	 * @param        entityClassB Representa a entidade fraca do relacionamento.
	 * @param        property Propriedade que representa.
	 */
	public String getDeleteMapping (ClassMap<?> entityClassA, ClassMap<?> entityClassB, Field property);

}
