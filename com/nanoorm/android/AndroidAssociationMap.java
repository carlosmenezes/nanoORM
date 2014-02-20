package com.nanoorm.android;

import java.lang.reflect.Field;

import com.nanoorm.annotations.ManyToMany;
import com.nanoorm.annotations.ManyToOne;
import com.nanoorm.annotations.OneToMany;
import com.nanoorm.annotations.OneToOne;
import com.nanoorm.mappings.AssociationMap;
import com.nanoorm.mappings.ClassMap;

/**
 * Class AndroidAssociationMap
 */
public class AndroidAssociationMap implements AssociationMap {

	// Ex.: Selecionar todos os produtos de uma venda
	public static final String SELECT_ONE_TO_MANY = "SELECT :fields FROM :entityB WHERE :entityBForeignKey = :entityAKey";
	// Ex.: Selecionar a venda ao qual um produto faz parte
	public static final String SELECT_MANY_TO_ONE = "SELECT :fields FROM :entityB WHERE :entityBKey = :entityAForeignKey";
	// Ex.: Selecionar a placa-mãe utilizada em um PC.
	public static final String SELECT_ONE_TO_ONE = "SELECT :fields FROM :entityB WHERE :entityBKey = :entityAForeignKey";
	// Ex.: Remover todos os produtos que compõem uma venda.
	public static final String DELETE = "DELETE FROM :entityB WHERE :entityBForeignKey = :entityAKey";
	// Ex.: Remover a placa mãe de um deteminado pc.
	public static final String DELETE_ONE_TO_ONE_OWNER = "DELETE FROM :entityB WHERE :entityAForeignKey = :entityBKey";
	
	public static final String SELECT_MANY_TO_MANY = "SELECT b.:fields FROM :entityB b\n" +
	                                                 "INNER JOIN :joinTable j ON(b.:entityBKey = j.:entityBJoinKey) \n" +
	                                                 "WHERE j.:entityAJoinKey = :entityAKey"; 
	
	private String setParameters(ClassMap<?> entityClassA, ClassMap<?> entityClassB, String sql) {
		StringBuilder sqlBuilder = new StringBuilder(sql);
		
		if (sql.equals(SELECT_MANY_TO_MANY)) {
			for (String column : entityClassB.getColumns()) {
				int replaceStart = sqlBuilder.indexOf(":fields");
				int replaceEnd = replaceStart + 7;
				sqlBuilder.replace(replaceStart, replaceEnd, column + ", b.:fields");
			}
			sqlBuilder.replace(sqlBuilder.indexOf(":entityB"), sqlBuilder.indexOf(":entityB") + 8, entityClassB.getTableName());			
			
			sqlBuilder.delete(sqlBuilder.indexOf(", b.:fields"), sqlBuilder.indexOf(", b.:fields") + 11);
		} else {		
			for (String column : entityClassB.getColumns()) {
				int replaceStart = sqlBuilder.indexOf(":fields");
				int replaceEnd = replaceStart + 7;
				sqlBuilder.replace(replaceStart, replaceEnd, column + ", :fields");
			}
			sqlBuilder.replace(sqlBuilder.indexOf(":entityB"), sqlBuilder.indexOf(":entityB") + 8, entityClassB.getTableName());			
			
			sqlBuilder.delete(sqlBuilder.indexOf(", :fields"), sqlBuilder.indexOf(", :fields") + 9);
		}
		return sqlBuilder.toString();
	}
	
	/**
	 * Constrói uma instrução SQL select que obtém 1 ou n objetos da classe informada
	 * no parâmetro entityClassB relacionados a objetos informados no parâmetro
	 * entityClassA.
	 * @return       String
	 * @param        entityClassA Representa a entidade forte do relacionamento.
	 * @param        entityClassB Representa a entidade fraca do relacionamento.
	 * @param        property Propriedade da classe informada no parâmetro entityClassA a ser obtida.
	 */
	public String getSelectMapping (ClassMap<?> entityClassA, ClassMap<?> entityClassB, Field property) {
		String sql = null;
		
		if (property.isAnnotationPresent(OneToMany.class))
			sql = setParameters(entityClassA, entityClassB, SELECT_ONE_TO_MANY);
		else if (property.isAnnotationPresent(ManyToOne.class))
			sql = setParameters(entityClassA, entityClassB, SELECT_MANY_TO_ONE);
		else if (property.isAnnotationPresent(OneToOne.class))
			sql = setParameters(entityClassA, entityClassB, SELECT_ONE_TO_ONE);
		else if (property.isAnnotationPresent(ManyToMany.class))
			sql = setParameters(entityClassA, entityClassB, SELECT_MANY_TO_MANY);
				
		return sql;
	}

	/**
	 * @return       String
	 * @param        entityClassA Representa a entidade forte do relacionamento.
	 * @param        entityClassB Representa a entidade fraca do relacionamento.
	 * @param        property Propriedade que representa o relacionamento.
	 */
	public String getDeleteMapping (ClassMap<?> entityClassA, ClassMap<?> entityClassB, Field property) {
		StringBuilder sql = null;
		int replaceStart = 0;
		int replaceEnd = 0;
		
		if (property.isAnnotationPresent(OneToOne.class) && !"".equals(property.getAnnotation(OneToOne.class).mappedBy())) {
			sql = new StringBuilder(DELETE_ONE_TO_ONE_OWNER);
			replaceStart = sql.indexOf(":entityB");
			replaceEnd = replaceStart + 8;
			sql.replace(replaceStart, replaceEnd, entityClassB.getTableName());
		} else if (property.isAnnotationPresent(ManyToMany.class)) { // tratar depois...
			sql = new StringBuilder(DELETE);
			replaceStart = sql.indexOf(":entityB");
			replaceEnd = replaceEnd + 8;
			String joinTable = "".equals(property.getAnnotation(ManyToMany.class).joinTable()) ?
					entityClassA.getTableName() + "_" + entityClassB.getTableName() : property.getAnnotation(ManyToMany.class).joinTable(); 
			sql.replace(replaceStart, replaceEnd, joinTable);
		} else {
			sql = new StringBuilder(DELETE);
			replaceStart = sql.indexOf(":entityB");
			replaceEnd = replaceStart + 8;
			sql.replace(replaceStart, replaceEnd, entityClassB.getTableName());			
		}
				
		return sql.toString();
	}

}