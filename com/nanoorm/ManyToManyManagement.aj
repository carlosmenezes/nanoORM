package com.nanoorm;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.annotation.SuppressAjWarnings;

import com.nanoorm.annotations.Entity;
import com.nanoorm.annotations.ManyToMany;
import com.nanoorm.mappings.ClassMap;

/**
 * Aspecto responsável por gerenciar as tabelas associativas dos relacionamentos @ManyToMany.
 */
public aspect ManyToManyManagement {

	private enum ManyToManyInstruction { INSERT, UPDATE, DELETE }

	private Map<Class<?>, Map<Field, EnumMap<ManyToManyInstruction, String>>> manyToManyMappings =
		new HashMap<Class<?>, Map<Field, EnumMap<ManyToManyInstruction, String>>>();		
	
	/**
	 * Ponto de atuação responsável por interceptar a inicialização pela JVM
	 * das classes gerenciadas pelo framework.
	 */
	pointcut entityInit() : staticinitialization(@Entity *);
	/**
	 * Ponto de atuação responsável por interceptar as operações
	 * de persistência e atualização de objetos.
	 */
	pointcut saveManagement() : execution(public void Manager.save(..)) || execution(public void Manager.update(..)) && within(!CascadeManagement);
	/**
	 * Ponto de atuação responsável por interceptar as operações de
	 * remoção de objetos.
	 */
	pointcut deleteManagement() : execution(public void Manager.delete(..)) && within(!CascadeManagement);

	/**
	 * Adendo para o ponto de atuação entityInit(), obtém todas
	 * as associações @ManyToMany da classe para posterior utilização do aspecto.
	 */
	@SuppressAjWarnings
	after() : entityInit() {
		Class<?> entityClass = thisJoinPointStaticPart.getSignature().getDeclaringType();
		for (Field field : entityClass.getDeclaredFields()) {
			if (field.isAnnotationPresent(ManyToMany.class)){
				if (!manyToManyMappings.containsKey(entityClass))
					manyToManyMappings.put(entityClass, new HashMap<Field, EnumMap<ManyToManyInstruction, String>>());

				Map<Field, EnumMap<ManyToManyInstruction, String>> fieldsMap = manyToManyMappings.get(entityClass);
				fieldsMap.put(field, new EnumMap<ManyToManyInstruction, String>(ManyToManyInstruction.class));

				String insert = initInsert(entityClass, field);
				String delete = initDelete(entityClass, field);

				Map<ManyToManyInstruction, String> instructionsMap = fieldsMap.get(field);
				instructionsMap.put(ManyToManyInstruction.INSERT, insert);
				instructionsMap.put(ManyToManyInstruction.DELETE, delete);
			}
		}
	}	
	
	/**
	 * Adendo para o ponto de atuação saveManagement(), realiza a persistência na tabela associativa.
	 */
	after() returning() : saveManagement() {
		Object[] args = thisJoinPoint.getArgs();

		if (args.length > 0) {
			Object arg = args[0];
			
			if (manyToManyMappings.containsKey(arg.getClass())) {
				Manager manager = ManagerFactory.getManagerForClass(arg.getClass());
				ClassMap<?> classMapA = manager.getDataSource().getMappingFor(arg.getClass());
				
				Map<Field, EnumMap<ManyToManyInstruction, String>> manyToManyFields = manyToManyMappings.get(arg.getClass());
				
				for (Field field : manyToManyFields.keySet()) {
					field.setAccessible(true);
					ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
					boolean isOwner = manyToMany.mappedBy() == "";
					
					Map<ManyToManyInstruction, String> fieldInstructions = manyToManyFields.get(field);
					StringBuilder deleteBuilder = new StringBuilder(fieldInstructions.get(ManyToManyInstruction.DELETE));
					
					Field idProperty = classMapA.getIdProperty();
					idProperty.setAccessible(true);
					int replaceStart = deleteBuilder.indexOf(":columnValue");
					int replaceEnd = replaceStart + ":columnValue".length();
					
					DbConnection connection = manager.getDataSource().getConnection();
					try {
						deleteBuilder.replace(replaceStart, replaceEnd, idProperty.get(arg).toString());
						connection.open();
						connection.executeSql(deleteBuilder.toString());
						connection.close();
					} catch (Exception ex) {
						throw new RuntimeException("Falha ao inserir o objeto.", ex);
					}
					
					// obtem o parametro generico da colecao
					ParameterizedType type = (ParameterizedType)field.getGenericType();	
					Class<?> classBType = (Class<?>)type.getActualTypeArguments()[0];
					manager = ManagerFactory.getManagerForClass(classBType);
					ClassMap<?> classMapB = manager.getDataSource().getMappingFor(classBType);
					
					try {
						List<?> objects = (List<?>) field.get(arg);
						
						StringBuilder insertBuilder = null;
						for (Object object : objects) {
							insertBuilder = new StringBuilder(fieldInstructions.get(ManyToManyInstruction.INSERT));
							replaceStart = insertBuilder.indexOf(":joinColumnValue");
							replaceEnd = replaceStart + ":joinColumnValue".length();
							int inverseReplaceStart = insertBuilder.indexOf(":inverseJoinColumnValue");
							int inverseReplaceEnd = inverseReplaceStart + ":inverseJoinColumnValue".length();
							
							if (isOwner) {
								idProperty = classMapB.getIdProperty();
								idProperty.setAccessible(true);
								insertBuilder.replace(inverseReplaceStart, inverseReplaceEnd, idProperty.get(object).toString());
								idProperty = classMapA.getIdProperty();
								idProperty.setAccessible(true);
								insertBuilder.replace(replaceStart, replaceEnd, idProperty.get(arg).toString());								
							} else {
								idProperty = classMapA.getIdProperty();
								idProperty.setAccessible(true);
								insertBuilder.replace(inverseReplaceStart, inverseReplaceEnd, idProperty.get(arg).toString());
								idProperty = classMapB.getIdProperty();
								idProperty.setAccessible(true);
								insertBuilder.replace(replaceStart, replaceEnd, idProperty.get(object).toString());
							}
							
							connection = manager.getDataSource().getConnection();
							connection.open();
							connection.executeSql(insertBuilder.toString());
							connection.close();
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}								
				}
			}
		}		 
	}
	
	/**
	 * Adendo para o ponto de atuação deleteManagement(), realiza a remoção de linhas da tabela associativa.
	 */
	after() returning() : deleteManagement() {
		Object[] args = thisJoinPoint.getArgs();	
		
		if (args.length > 0) {
			Object arg = args[0];			

			if (manyToManyMappings.containsKey(arg.getClass())) {
				Manager manager = ManagerFactory.getManagerForClass(arg.getClass());
				ClassMap<?> classMap = manager.getDataSource().getMappingFor(arg.getClass());
				
				Map<Field, EnumMap<ManyToManyInstruction, String>> manyToManyFields = manyToManyMappings.get(arg.getClass());
				List<String> instructions = new ArrayList<String>();
				for (Field field : manyToManyFields.keySet()) {
					Map<ManyToManyInstruction, String> fieldInstructions = manyToManyFields.get(field);
					StringBuilder deleteBuilder = new StringBuilder(fieldInstructions.get(ManyToManyInstruction.DELETE));
					
					Field idProperty = classMap.getIdProperty();
					int replaceStart = deleteBuilder.indexOf(":columnValue");
					int replaceEnd = replaceStart + ":columnValue".length();
					
					try {
						deleteBuilder.replace(replaceStart, replaceEnd, idProperty.get(arg).toString());
						instructions.add(deleteBuilder.toString());
					} catch (Exception ex) {
						throw new RuntimeException("Falha ao deletar o objeto.", ex);
					}
				}
				
				DbConnection connection = manager.getDataSource().getConnection();
				connection.open();
				for (String instruction : instructions)			
					connection.executeSql(instruction);

				connection.close();
			}
		}
	}

	private String initDelete(Class<?> entityClass, Field field) {		
		String baseDelete = "DELETE FROM :joinTable WHERE :joinColumn = :columnValue";
		ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
		boolean isOwner = manyToMany.mappedBy() == "";	

		ClassMap<?> classMapA = ManagerFactory.getManagerForClass(entityClass).getDataSource().getMappingFor(entityClass);

		ParameterizedType type = (ParameterizedType)field.getGenericType();	
		Class<?> classBType = (Class<?>)type.getActualTypeArguments()[0];
		ClassMap<?> classMapB = ManagerFactory.getManagerForClass(entityClass).getDataSource().getMappingFor(classBType);											

		String joinTable = "";
		String joinColumn = "";
		try {
			if (isOwner) {
				joinTable = manyToMany.joinTable() != "" ? manyToMany.joinTable() : classMapA.getTableName() + "_" + classMapB.getTableName();
				joinColumn = manyToMany.joinColumn().name() != "" ? manyToMany.joinColumn().name() : classMapA.getTableName() + "_id";
			} else {
				Field ownerField = classBType.getDeclaredField(manyToMany.mappedBy());
				ManyToMany otherManyToMany = ownerField.getAnnotation(ManyToMany.class);
				joinTable = otherManyToMany.joinTable() != "" ? otherManyToMany.joinTable() : classMapB.getTableName() + "_" + classMapA.getTableName();
				joinColumn = otherManyToMany.inverseJoinColumn().name() != "" ? otherManyToMany.inverseJoinColumn().name() : classMapB.getTableName() + "_id";
			}
		} catch (Exception e) {
			throw new RuntimeException("Mapeamento @ManyToMany inválido o atributo " +
					manyToMany.mappedBy() + " não existe na classe " + classBType.getName(), e);
		}

		StringBuilder sqlBuilder = new StringBuilder(baseDelete);
		int replaceStart = sqlBuilder.indexOf(":joinTable");
		int replaceEnd = replaceStart + ":joinTable".length();
		sqlBuilder.replace(replaceStart, replaceEnd, joinTable);

		replaceStart = sqlBuilder.indexOf(":joinColumn");
		replaceEnd = replaceStart + ":joinColumn".length();
		sqlBuilder.replace(replaceStart, replaceEnd, joinColumn);

		return sqlBuilder.toString();
	}

	private String initInsert(Class<?> entityClass, Field field) {
		String baseInsert = "INSERT INTO :joinTable (:joinColumn, :inverseJoinColumn) VALUES (:joinColumnValue, :inverseJoinColumnValue)";		
		ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
		boolean isOwner = manyToMany.mappedBy() == "";	

		ClassMap<?> classMapA = ManagerFactory.getManagerForClass(entityClass).getDataSource().getMappingFor(entityClass);

		ParameterizedType type = (ParameterizedType)field.getGenericType();	
		Class<?> classBType = (Class<?>)type.getActualTypeArguments()[0];
		ClassMap<?> classMapB = ManagerFactory.getManagerForClass(entityClass).getDataSource().getMappingFor(classBType);											

		String joinTable = "";
		String joinColumn = "";
		String inverseJoinColumn = "";
		try {
			if (isOwner) {
				joinTable = manyToMany.joinTable() != "" ? manyToMany.joinTable() : classMapA.getTableName() + "_" + classMapB.getTableName();
				joinColumn = manyToMany.joinColumn().name() != "" ? manyToMany.joinColumn().name() : classMapA.getTableName() + "_id";
				inverseJoinColumn = manyToMany.inverseJoinColumn().name() != "" ? manyToMany.inverseJoinColumn().name() : classMapB.getTableName() + "_id";
			} else {
				Field ownerField = classBType.getDeclaredField(manyToMany.mappedBy());
				ManyToMany otherManyToMany = ownerField.getAnnotation(ManyToMany.class);
				joinTable = otherManyToMany.joinTable() != "" ? otherManyToMany.joinTable() : classMapB.getTableName() + "_" + classMapA.getTableName();
				joinColumn = otherManyToMany.joinColumn().name() != "" ? otherManyToMany.joinColumn().name() : classMapA.getTableName() + "_id";
				inverseJoinColumn = otherManyToMany.inverseJoinColumn().name() != "" ? otherManyToMany.inverseJoinColumn().name() : classMapB.getTableName() + "_id";
			}
		} catch (Exception e) {
			throw new RuntimeException("Mapeamento @ManyToMany inválido o atributo " +
					manyToMany.mappedBy() + " não existe na classe " + classBType.getName(), e);
		}

		StringBuilder sqlBuilder = new StringBuilder(baseInsert);
		int replaceStart = sqlBuilder.indexOf(":joinTable");
		int replaceEnd = replaceStart + ":joinTable".length();
		sqlBuilder.replace(replaceStart, replaceEnd, joinTable);

		replaceStart = sqlBuilder.indexOf(":joinColumn");
		replaceEnd = replaceStart + ":joinColumn".length();
		sqlBuilder.replace(replaceStart, replaceEnd, joinColumn);

		replaceStart = sqlBuilder.indexOf(":inverseJoinColumn");
		replaceEnd = replaceStart + ":inverseJoinColumn".length();
		sqlBuilder.replace(replaceStart, replaceEnd, inverseJoinColumn);

		return sqlBuilder.toString();
	}
}
