package com.nanoorm;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.annotation.SuppressAjWarnings;

import com.nanoorm.annotations.Column;
import com.nanoorm.annotations.ManyToMany;
import com.nanoorm.annotations.ManyToOne;
import com.nanoorm.annotations.OneToMany;
import com.nanoorm.annotations.OneToOne;
import com.nanoorm.mappings.AssociationMap;
import com.nanoorm.mappings.ClassMap;

/**
 * Aspecto responsável por realizar a carga de objetos do
 * banco de dados sob demanda (Lazy Loading).
 * @author Carlos Eduardo Pacheco Menezes
 */
public aspect LazyLoad {
	
	private Map<String, String> oneToOneInstructions = new HashMap<String, String>();
	private Map<String, String> oneToManyInstructions = new HashMap<String, String>();
	private Map<String, String> manyToOneInstructions = new HashMap<String, String>();
	private Map<String, String> manyToManyInstructions = new HashMap<String, String>();
	
	/**
	 * Ponto de atuação que intercepta os acessos a atributos de classe anotados
	 * com a anotação @OneToOne.
	 */
	pointcut oneToOneAccessFilter() : get(@OneToOne * *.*) && within(!LazyLoad);
	/**
	 * Ponto de atuação que intercepta os acessos a atributos de classe anotados
	 * com a anotação @OneToMany.
	 */
	pointcut oneToManyAccessFilter() : get(@OneToMany * *.*) && within(!LazyLoad);
	/**
	 * Ponto de atuação que intercepta os acessos a atributos de classe anotados
	 * com a anotação @ManyToOne.
	 */
	pointcut manyToOneAccessFilter() : get(@ManyToOne * *.*) && within(!LazyLoad);
	/**
	 * Ponto de atuação que intercepta os acessos a atributos de classe anotados
	 * com a anotação @ManyToMany.
	 */
	pointcut manyToManyAccessFilter() : get(@ManyToMany * *.*) && within(!LazyLoad);	
	
	/**
	 * Adendo que realiza a carga de objetos sob demanda para 
	 * relacionamentos do tipo @OneToOne.
	 */
	@SuppressAjWarnings
	before() : oneToOneAccessFilter() {		
		if (thisJoinPoint.getThis() != null) {
			Class<?> interceptedClass = thisJoinPoint.getThis().getClass();
			Field field = null;
			
			try {
				field = interceptedClass.getDeclaredField(thisJoinPoint.toString().substring(thisJoinPoint.toString().lastIndexOf(".") + 1, thisJoinPoint.toString().lastIndexOf(")")));
				field.setAccessible(true);				
				
				boolean isUnidirectional = field.getAnnotation(OneToOne.class).mappedBy() == "";
				
				Manager manager = ManagerFactory.getManagerForClass(interceptedClass);
				ClassMap<?> classMapA = manager.getDataSource().getMappingFor(interceptedClass);
				manager = ManagerFactory.getManagerForClass(field.getType());
				ClassMap<?> classMapB = manager.getDataSource().getMappingFor(field.getType());
				
				if (!oneToOneInstructions.containsKey(thisJoinPoint.toString())) {					
					AssociationMap associationMap = AssociationMapFactory.buildAssociationMap(manager.getDataSource().getDataSourceType());
					oneToOneInstructions.put(thisJoinPoint.toString(), associationMap.getSelectMapping(classMapA, classMapB, field));					 
				}
				
				String sql = oneToOneInstructions.get(thisJoinPoint.toString());
				StringBuilder sqlBuilder = new StringBuilder(sql);
				int replaceStart = sqlBuilder.indexOf(":entityAForeignKey");
				int replaceEnd = replaceStart + ":entityAForeignKey".length();
				if (isUnidirectional) {
					Field classBId = classMapB.getIdProperty();
					classBId.setAccessible(true);
					if (classBId.get(field.get(thisJoinPoint.getThis())) == null)
						return;					
					sqlBuilder.replace(replaceStart, replaceEnd, classBId.get(field.get(thisJoinPoint.getThis())).toString());
				} else {
					Field classAId = classMapA.getIdProperty();
					classAId.setAccessible(true);
					if (classAId.get(thisJoinPoint.getThis()) == null)
						return;
					sqlBuilder.replace(replaceStart, replaceEnd, classAId.get(thisJoinPoint.getThis()).toString());
				}
				
				replaceStart = sqlBuilder.indexOf(":entityBKey");
				replaceEnd = replaceStart + ":entityBKey".length();
				
				if (isUnidirectional)
					sqlBuilder.replace(replaceStart, replaceEnd, classMapB.getIdColumn());
				else {
					Field fieldB = field.getType().getDeclaredField(field.getAnnotation(OneToOne.class).mappedBy());
					String columnName = fieldB.isAnnotationPresent(Column.class) ? fieldB.getAnnotation(Column.class).name() : classMapA.getTableName() + "_id";
					sqlBuilder.replace(replaceStart, replaceEnd, columnName);
				}
				sql = sqlBuilder.toString();
	
				DbConnection connection = manager.getDataSource().getConnection();
				connection.open();
				DbCursor cursor = connection.executeSqlQuery(sql);
				if (cursor.getRowCount() > 0) {
					Object value = classMapB.buildObject(cursor);
					field.set(thisJoinPoint.getThis(), value);
				}
				cursor.close();
				connection.close();				
			} catch (Exception e) {
				throw new RuntimeException("Falha na obtenção do atributo " + thisJoinPoint.toShortString(), e);
			}
		}
	}
	
	/**
	 * Adendo que realiza a carga de objetos sob demanda para 
	 * relacionamentos do tipo @OneToMany.
	 */
	@SuppressAjWarnings
	before() : oneToManyAccessFilter() {
		if (thisJoinPoint.getThis() != null) {
			Class<?> interceptedClass = thisJoinPoint.getThis().getClass();
			Field field = null;
			
			try {
				field = interceptedClass.getDeclaredField(thisJoinPoint.toString().substring(thisJoinPoint.toString().lastIndexOf(".") + 1, thisJoinPoint.toString().lastIndexOf(")")));
				field.setAccessible(true);				
				
				boolean isUnidirectional = field.getAnnotation(OneToMany.class).mappedBy() == "";
				
				Manager manager = ManagerFactory.getManagerForClass(interceptedClass);
				ClassMap<?> classMapA = manager.getDataSource().getMappingFor(interceptedClass);
				
				Field idProperty = classMapA.getIdProperty();
				idProperty.setAccessible(true);
				
				if (idProperty.get(thisJoinPoint.getThis()) != null) {				
					// obtem o parametro generico da colecao
					ParameterizedType type = (ParameterizedType)field.getGenericType();	
					Class<?> fieldClass = (Class<?>)type.getActualTypeArguments()[0];
					
					manager = ManagerFactory.getManagerForClass(fieldClass);												
					ClassMap<?> classMapB = manager.getDataSource().getMappingFor(fieldClass);
					
					if (!oneToManyInstructions.containsKey(thisJoinPoint.toString())) {
						AssociationMap associationMap = AssociationMapFactory.buildAssociationMap(manager.getDataSource().getDataSourceType());
						oneToManyInstructions.put(thisJoinPoint.toString(), associationMap.getSelectMapping(classMapA, classMapB, field));
					}
					
					String sql = oneToManyInstructions.get(thisJoinPoint.toString());
					StringBuilder sqlBuilder = new StringBuilder(sql);
					
					int replaceStart = sqlBuilder.indexOf(":entityBForeignKey");
					int replaceEnd = replaceStart + ":entityBForeignKey".length();
					String columnName;
					
					if (isUnidirectional) {
						columnName = classMapA.getTableName() + "_id";
					} else {
						Field fieldB = fieldClass.getDeclaredField(field.getAnnotation(OneToMany.class).mappedBy());
						columnName = fieldB.isAnnotationPresent(Column.class) ? fieldB.getAnnotation(Column.class).name() : classMapA.getTableName() + "_id";
					}
					sqlBuilder.replace(replaceStart, replaceEnd, columnName);
					
					replaceStart = sqlBuilder.indexOf(":entityAKey");
					replaceEnd = replaceStart + ":entityAKey".length();				
					sqlBuilder.replace(replaceStart, replaceEnd, idProperty.get(thisJoinPoint.getThis()).toString());
					
					sql = sqlBuilder.toString();
					
					DbConnection connection = manager.getDataSource().getConnection();
					connection.open();
					DbCursor cursor = connection.executeSqlQuery(sql);
					if (cursor.getRowCount() > 0) {
						Object value = classMapB.buildList(cursor);
						field.set(thisJoinPoint.getThis(), value);
					}
					cursor.close();
					connection.close();
				}
			} catch (Exception e) {
				throw new RuntimeException("Falha na obtenção do atributo " + thisJoinPoint.toShortString(), e);
			}
		}
	}

	/**
	 * Adendo que realiza a carga de objetos sob demanda para 
	 * relacionamentos do tipo @ManyToOne.
	 */
	@SuppressAjWarnings
	before() : manyToOneAccessFilter() {
		if (thisJoinPoint.getThis() != null) {
			Class<?> interceptedClass = thisJoinPoint.getThis().getClass();
			Field field = null;
			
			try {
				field = interceptedClass.getDeclaredField(thisJoinPoint.toString().substring(thisJoinPoint.toString().lastIndexOf(".") + 1, thisJoinPoint.toString().lastIndexOf(")")));
				field.setAccessible(true);								
				
				Manager manager = ManagerFactory.getManagerForClass(interceptedClass);
				ClassMap<?> classMapA = manager.getDataSource().getMappingFor(interceptedClass);
				manager = ManagerFactory.getManagerForClass(field.getType());
				ClassMap<?> classMapB = manager.getDataSource().getMappingFor(field.getType());
				Field classBId = classMapB.getIdProperty();
				classBId.setAccessible(true);
				
				if (classBId.get(field.get(thisJoinPoint.getThis())) != null) {				
					if (!manyToOneInstructions.containsKey(thisJoinPoint.toString())) {
						AssociationMap associationMap = AssociationMapFactory.buildAssociationMap(manager.getDataSource().getDataSourceType());
						manyToOneInstructions.put(thisJoinPoint.toString(), associationMap.getSelectMapping(classMapA, classMapB, field));
					}
					
					String sql = manyToOneInstructions.get(thisJoinPoint.toString());
					StringBuilder sqlBuilder = new StringBuilder(sql);
					int replaceStart = sqlBuilder.indexOf(":entityBKey");
					int replaceEnd = replaceStart + ":entityBKey".length();
					sqlBuilder.replace(replaceStart, replaceEnd, classMapB.getIdColumn());
									
					replaceStart = sqlBuilder.indexOf(":entityAForeignKey");
					replaceEnd = replaceStart + ":entityAForeignKey".length();
					sqlBuilder.replace(replaceStart, replaceEnd, classBId.get(field.get(thisJoinPoint.getThis())).toString());														
					
					sql = sqlBuilder.toString();
					
					DbConnection connection = manager.getDataSource().getConnection();
					connection.open();
					DbCursor cursor = connection.executeSqlQuery(sql);
					if (cursor.getRowCount() > 0) {
						Object value = classMapB.buildObject(cursor);
						field.set(thisJoinPoint.getThis(), value);
					}
					cursor.close();
					connection.close();		
				}
			} catch (Exception e) {
				throw new RuntimeException("Falha na obtenção do atributo " + thisJoinPoint.toShortString(), e);
			}
		}
	}
	
	/**
	 * Adendo que realiza a carga de objetos sob demanda para 
	 * relacionamentos do tipo @ManyToMany.
	 */
	@SuppressAjWarnings
	before() : manyToManyAccessFilter() {
		Class<?> interceptedClass = thisJoinPoint.getThis().getClass();
		Field field = null;
		
		Manager manager = ManagerFactory.getManagerForClass(interceptedClass);
		ClassMap<?> classMapA = manager.getDataSource().getMappingFor(interceptedClass);
		Field idProperty = classMapA.getIdProperty();
		idProperty.setAccessible(true);
		
		try {
			if (idProperty.get(thisJoinPoint.getThis()) != null) {		
				field = interceptedClass.getDeclaredField(thisJoinPoint.toString().substring(thisJoinPoint.toString().lastIndexOf(".") + 1, thisJoinPoint.toString().lastIndexOf(")")));
				field.setAccessible(true);						
				
				// obtem o parametro generico da colecao
				ParameterizedType type = (ParameterizedType)field.getGenericType();	
				Class<?> classBType = (Class<?>)type.getActualTypeArguments()[0];
				manager = ManagerFactory.getManagerForClass(classBType);
				ClassMap<?> classMapB = manager.getDataSource().getMappingFor(classBType);
				
				if (!manyToManyInstructions.containsKey(thisJoinPoint.toString())) {
					AssociationMap associationMap = AssociationMapFactory.buildAssociationMap(manager.getDataSource().getDataSourceType());
					manyToManyInstructions.put(thisJoinPoint.toString(), associationMap.getSelectMapping(classMapA, classMapB, field));
				}
				
				String sql = manyToManyInstructions.get(thisJoinPoint.toString());
				StringBuilder sqlBuilder = new StringBuilder(sql);
				
				ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
				boolean isOwner = manyToMany.mappedBy() == "";
				
				String joinTable = "";
				String joinColumn = "";
				String inverseJoinColumn = "";
				if (isOwner) {
					joinTable = manyToMany.joinTable() != "" ? manyToMany.joinTable() : classMapA.getTableName() + "_" + classMapB.getTableName();
					joinColumn = manyToMany.joinColumn().name() != "" ? manyToMany.joinColumn().name() : classMapA.getTableName() + "_id";
					inverseJoinColumn = manyToMany.inverseJoinColumn().name() != "" ? manyToMany.inverseJoinColumn().name() : classMapB.getTableName() + "_id";
				} else {
					Field ownerField = classBType.getDeclaredField(manyToMany.mappedBy());
					ManyToMany otherManyToMany = ownerField.getAnnotation(ManyToMany.class);
					joinTable = otherManyToMany.joinTable() != "" ? otherManyToMany.joinTable() : classMapB.getTableName() + "_" + classMapA.getTableName();
					joinColumn = otherManyToMany.inverseJoinColumn().name() != "" ? otherManyToMany.inverseJoinColumn().name() : classMapB.getTableName() + "_id";
					inverseJoinColumn = otherManyToMany.joinColumn().name() != "" ? otherManyToMany.joinColumn().name() : classMapA.getTableName() + "_id";;
				}
				
				int replaceStart = sqlBuilder.indexOf(":joinTable");
				int replaceEnd = replaceStart + ":joinTable".length();
				sqlBuilder.replace(replaceStart, replaceEnd, joinTable);
				
				replaceStart = sqlBuilder.indexOf(":entityBKey");
				replaceEnd = replaceStart + ":entityBKey".length();
				sqlBuilder.replace(replaceStart, replaceEnd, classMapB.getIdColumn());
								
				replaceStart = sqlBuilder.indexOf(":entityBJoinKey");
				replaceEnd = replaceStart + ":entityBJoinKey".length();
				sqlBuilder.replace(replaceStart, replaceEnd, inverseJoinColumn);
				
				replaceStart = sqlBuilder.indexOf(":entityAJoinKey");
				replaceEnd = replaceStart + ":entityAJoinKey".length();
				sqlBuilder.replace(replaceStart, replaceEnd, joinColumn);
				
				replaceStart = sqlBuilder.indexOf(":entityAKey");
				replaceEnd = replaceStart + ":entityAKey".length();
				Field idPropertyA = classMapA.getIdProperty();
				idPropertyA.setAccessible(true);
				sqlBuilder.replace(replaceStart, replaceEnd, idPropertyA.get(thisJoinPoint.getThis()).toString());
				
				sql = sqlBuilder.toString();
				
				DbConnection connection = manager.getDataSource().getConnection();
				connection.open();
				DbCursor cursor = connection.executeSqlQuery(sql);
				if (cursor.getRowCount() > 0) {
					Object value = classMapB.buildList(cursor);
					field.set(thisJoinPoint.getThis(), value);
				}
				cursor.close();
				connection.close();			
			}
		} catch (Exception e) {
			throw new RuntimeException("Falha na obtenção do atributo " + thisJoinPoint.toShortString(), e);
		}
	}
}
