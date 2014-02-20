package com.nanoorm.android;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nanoorm.DataSource;
import com.nanoorm.DbCursor;
import com.nanoorm.annotations.Column;
import com.nanoorm.annotations.Entity;
import com.nanoorm.annotations.Id;
import com.nanoorm.annotations.ManyToMany;
import com.nanoorm.annotations.ManyToOne;
import com.nanoorm.annotations.OneToMany;
import com.nanoorm.annotations.OneToOne;
import com.nanoorm.annotations.Transient;
import com.nanoorm.mappings.ClassMap;

/**
 * Class AndroidClassMap
 * Classe reponsável por obter os mapementos entre uma classe e a fonte de dados na
 * plataforma Android.
 * @author Carlos Eduardo Pacheco Menezes
 */
public class AndroidClassMap <T> implements ClassMap<T> {
	
	private String selectInstruction;
	private String selectAllInstruction;
	private String insertInstruction;
	private String updateInstruction;
	private String deleteInstruction;
	private Class<T> entityClassType;
	private Map<Field, String> columnMappings;
	private Field idProperty;
	private String idColumn;
	private String tableName;
	private DataSource dataSource;
	private boolean generateId;

	private void init() {
		Entity entity = entityClassType.getAnnotation(Entity.class);
		tableName = "".equals(entity.tableName()) ? entityClassType.getSimpleName() : entity.tableName();
		
		for (Field field : entityClassType.getDeclaredFields()) {			
			String column = "";
			
			if (!field.isAnnotationPresent(Transient.class)) {
				if ((field.isAnnotationPresent(ManyToOne.class)) || // ManyToOne
					(field.isAnnotationPresent(OneToOne.class) && "".equals(field.getAnnotation(OneToOne.class).mappedBy())) && // OneToOne
					!"org.aspectj.lang.JoinPoint$StaticPart".equals(field.getType().getName())) { 
					Class<?> referencedClass = field.getType();
					String referencedTable = "".equals(referencedClass.getAnnotation(Entity.class).tableName()) ? referencedClass.getSimpleName() : referencedClass.getAnnotation(Entity.class).tableName(); 
											
					column = field.isAnnotationPresent(Column.class) ? field.getAnnotation(Column.class).name() : referencedTable.toLowerCase() + "_id";
					columnMappings.put(field, column);
				} else if(!field.isAnnotationPresent(OneToOne.class) &&
						  !"org.aspectj.lang.JoinPoint$StaticPart".equals(field.getType().getName()) &&
						  !field.isAnnotationPresent(OneToMany.class) &&
						  !field.isAnnotationPresent(ManyToMany.class)) {
					column = field.isAnnotationPresent(Column.class) ? field.getAnnotation(Column.class).name() : field.getName();
					
					if (field.isAnnotationPresent(Id.class)) {
						idProperty = field;
						generateId = field.getAnnotation(Id.class).autoGenerate();
						idColumn = column;				
					}
					columnMappings.put(field, column);
				}
			}			
		}
		
		initSelect();
		initInsert();
		initUpdate();
		initDelete();
	}
	
	private void initSelect() {
		StringBuilder sql = new StringBuilder("SELECT ");
		
		for (Field field : columnMappings.keySet()) 
			sql.append(columnMappings.get(field)).append(", ");
		
		sql.delete(sql.lastIndexOf(", "), sql.lastIndexOf(", ") + 1);
		sql.append("FROM ").append(tableName);
		
		selectAllInstruction = sql.toString();
		
		sql.append(" WHERE ");
		sql.append(idColumn).append(" = :").append(idColumn);
		selectInstruction = sql.toString();
	}
	
	private void initInsert() {
		StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName);
		sql.append("(@) VALUES (@)");
		
		for (Field field : columnMappings.keySet()) {
			sql.replace(sql.indexOf("@"), sql.indexOf("@") + 1, columnMappings.get(field) + ", @");
			sql.replace(sql.lastIndexOf("@"), sql.lastIndexOf("@") + 1, ":" + columnMappings.get(field) + ", @");
		}
		
		sql.delete(sql.indexOf(", @"), sql.indexOf(", @") + 3);
		sql.delete(sql.indexOf(", @"), sql.indexOf(", @") + 3);
		insertInstruction = sql.toString();		
	}
	
	private void initUpdate() {
		StringBuilder sql = new StringBuilder("UPDATE ").append(tableName);
		sql.append(" SET ");
		
		for (Field field : columnMappings.keySet())
			sql.append(columnMappings.get(field)).append(" = :").append(columnMappings.get(field)).append(", ");
		
		sql.delete(sql.lastIndexOf(","), sql.lastIndexOf(",") + 1);
		sql.append("WHERE ").append(idColumn).append(" = :").append(idColumn);
		updateInstruction = sql.toString();
	}
	
	private void initDelete() {
		StringBuilder sql = new StringBuilder("DELETE FROM ").append(tableName);
		sql.append(" WHERE ").append(idColumn).append(" = :").append(idColumn);
		deleteInstruction = sql.toString();
	}
	
	private String getSqlValue(Object value) {
		String returnValue = null;
		if ("String".equals(value.getClass().getSimpleName()))
			returnValue = String.format("'%s'", value);
		else if ("Date".equals(value.getClass().getSimpleName()))
			returnValue = Long.toString(((Date)value).getTime());	
		else if ("Calendar".equals(value.getClass().getSuperclass().getSimpleName()))
			returnValue = Long.toString(((Calendar) value).getTimeInMillis());			
		else if ("Number".equals(value.getClass().getSuperclass().getSimpleName()))
			returnValue = value.toString();
		else if ("boolean".equalsIgnoreCase(value.getClass().getSimpleName()))
			returnValue = ((Boolean)value) ? "'1'" : "'0'";
		//else if ("Byte[]".equals(typeOfValue.getSimpleName()))
		//	returnValue = cursor.getBlob(columnIndex);
		else
			throw new IllegalArgumentException("O tipo " + value.getClass() + " não é suportado por este banco de dados.");
		
		return returnValue;
	}
	
	@SuppressWarnings("unchecked")
	private T getObjectValue(Object value, Class<?> typeOfValue) {
		T returnValue;
		
		if (typeOfValue.isAnnotationPresent(Entity.class))
			returnValue = (T)value;		
		else if ("String".equalsIgnoreCase(typeOfValue.getSimpleName()))
			returnValue = (T)value.toString();
		else if ("Double".equalsIgnoreCase(typeOfValue.getSimpleName()))
			returnValue = (T)Double.valueOf(value.toString());
		else if ("Integer".equals(typeOfValue.getSimpleName()) || "int".equals(typeOfValue.getSimpleName()))
			returnValue = (T)Integer.valueOf(value.toString());
		else if ("Long".equalsIgnoreCase(typeOfValue.getSimpleName())) 
			returnValue = (T)Long.valueOf(value.toString());
		else if ("Short".equalsIgnoreCase(typeOfValue.getSimpleName()))
			returnValue = (T)Short.valueOf(value.toString());
		else if ("Float".equalsIgnoreCase(typeOfValue.getSimpleName()))
			returnValue = (T)Float.valueOf(value.toString());
		//else if ("Byte[]".equals(typeOfValue.getSimpleName()))
		//	value = (T)Array
		else if ("java.util.Date".equals(typeOfValue.getName()))
			returnValue = (T)value;
		else if ("java.sql.Date".equals(typeOfValue.getName()))
			returnValue = (T)value;
		else if ("Calendar".equals(value.getClass().getSuperclass().getSimpleName())) {
			Calendar calendar = (Calendar)value;
			returnValue = (T) calendar;
		} else if ("boolean".equalsIgnoreCase(typeOfValue.getSimpleName()))
			returnValue = (T) ("1".equals(value) ? Boolean.TRUE : Boolean.FALSE);
		else
			throw new IllegalArgumentException("O tipo " + typeOfValue.getName()  + "não é suportado por este banco de dados.");
		
		return returnValue;
	}
	
	private Serializable getReferencedId(Object referencedObject) {
		if (referencedObject == null)
			return null;
		
		ClassMap<?> classMap = dataSource.getMappingFor(referencedObject.getClass());
		Serializable id;
		
		try {
			Field referencedProperty = classMap.getIdProperty();
			referencedProperty.setAccessible(true);
			id = (Serializable) referencedProperty.get(referencedObject);
		} catch (Exception ex) {
			throw new RuntimeException("Falha ao obter o id do objeto referenciado", ex);
		}
		
		return id;
	}
	
	private Object setReferencedId(Object referencedObject, Object value) {
		ClassMap<?> classMap = dataSource.getMappingFor(referencedObject.getClass());
		
		Field referencedProperty = classMap.getIdProperty();
		referencedProperty.setAccessible(true);
		try {
			referencedProperty.set(referencedObject, getObjectValue(value, referencedProperty.getType()));
		} catch (Exception e) {
			throw new RuntimeException("Erro ao inicializar o id do objeto referenciado.", e);
		}
		return referencedObject;
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}
	
	/**
	 * Constrói uma instância de AndroidClassMap para a classe informada no
	 * parâmetro entityClass.
	 * @return       com.nanoorm.android.AndroidClassMap
	 * @param        entityClass Classe a qual se deseja obter os mapeamentos.
	 * @param        dataSource Fonte de dados a qual a instância de AndroidClassMap
	 * irá pertencer.
	 */
	public AndroidClassMap (Class<T> entityClass, DataSource dataSource) {
		this.entityClassType = entityClass;
		this.dataSource = dataSource;
		columnMappings = new HashMap<Field, String>();
		init();
	}

	/**
	 * Constrói a instrução sql select para um objeto da entidade informada no
	 * parâmetro entity.
	 * @return       String
	 * @param        entityId Id do objeto para qual será gerada a instrução sql.
	 */
	public String buildSelectFor (Serializable entityId) {
		StringBuilder sql = new StringBuilder(selectInstruction);
		int replaceStart = sql.indexOf(":" + idColumn);
		int replaceEnd = replaceStart + idColumn.length() + 1; 		
		sql.replace(replaceStart, replaceEnd, entityId.toString());
		return sql.toString();
	}

	/**
	 * Constrói a instrução sql select que seleciona todos os objetos.
	 * @return       String
	 */
	public String buildSelectForAll () {
		return selectAllInstruction;
	}

	/**
	 * Constrói a instrução sql insert para a entidade informada no parâmetro entity.
	 * @return       String
	 * @param        entity Entidade para qual será gerada a instrução sql.
	 */
	public String buildInsertFor (T entity)	{
		StringBuilder sql = new StringBuilder(insertInstruction);
		Object value = null;
		
		for (Field field : columnMappings.keySet()) {
			
			String column = columnMappings.get(field);
			int replaceStart = sql.indexOf(":" + column);
			int replaceEnd = replaceStart + column.length() + 1;
			
			try {
				field.setAccessible(true);
				if (field.isAnnotationPresent(ManyToOne.class) ||
				   (field.isAnnotationPresent(OneToOne.class) && "".equals(field.getAnnotation(OneToOne.class).mappedBy()))) {
					value = getReferencedId(field.get(entity));
					sql.replace(replaceStart, replaceEnd, value == null ? "NULL" : getSqlValue(value));
				} else if (field.getName() != idProperty.getName()) {
					value = field.get(entity);
					sql.replace(replaceStart, replaceEnd, value == null ? "NULL" : getSqlValue(value));									
				} else {
					idProperty.setAccessible(true);
					if (generateId)
						idProperty.set(entity, AndroidSequence.generateKey(dataSource.getConnection(), idProperty.getType()));
					sql.replace(replaceStart, replaceEnd, getSqlValue(idProperty.get(entity)));
				}					
			} catch (Exception e) {
				throw new RuntimeException("Erro ao obter as propridades do objeto.", e);
			}
		}

		return sql.toString();
	}

	/**
	 * Constrói a instrução sql update para a entidade informada no parâmetro entity.
	 * @return       String
	 * @param        entity Entidade para qual será gerada a instrução sql.
	 */
	public String buildUpdateFor (T entity)	{
		StringBuilder sql = new StringBuilder(updateInstruction);
		Object value = null;
		
		for (Field field : columnMappings.keySet()) {
			
			String column = columnMappings.get(field);
			int replaceStart = sql.indexOf(":" + column);
			int replaceEnd = replaceStart + column.length() + 1;
			
			try {
				field.setAccessible(true);
				if (field.isAnnotationPresent(ManyToOne.class) ||
				   (field.isAnnotationPresent(OneToOne.class) && "".equals(field.getAnnotation(OneToOne.class).mappedBy()))) {
					value = getReferencedId(field.get(entity));
					sql.replace(replaceStart, replaceEnd, value == null ? "NULL" : getSqlValue(value));
				} else {				
					value = field.get(entity);					
					sql.replace(replaceStart, replaceEnd, value == null ? "NULL" : getSqlValue(value));
					
					if (field.getName().equals(idProperty.getName()))
						sql.replace(sql.indexOf(":" + idColumn), sql.indexOf(":" + idColumn) + idColumn.length() + 1, value == null ? "NULL" : getSqlValue(value));
				}
			} catch (Exception e) {
				throw new RuntimeException("Erro ao obter as propridades do objeto.", e);
			}
		}
		
		return sql.toString();
	}

	/**
	 * Constrói a instrução sql delete para a entidade informada no parâmetro entity.
	 * @return       String
	 * @param        entity Entidade para qual será gerada a instrução sql.
	 */
	public String buildDeleteFor (T entity)	{
		StringBuilder sql = new StringBuilder(deleteInstruction);
		int replaceStart = sql.indexOf(":" + idColumn);
		int replaceEnd = replaceStart + idColumn.length() + 1;
		
		try {
			idProperty.setAccessible(true);
			sql.replace(replaceStart, replaceEnd, getSqlValue(idProperty.get(entity)));
		} catch (Exception e) {
			throw new RuntimeException("Erro ao obter as propridades do objeto.", e);
		}
		return sql.toString();
	}

	@Override
	public List<T> buildList(DbCursor dbCursor) {
		List<T> list = new ArrayList<T>();

		do {
			T object = null;
			try {
				object = entityClassType.newInstance();
			} catch (Exception e) {
				throw new RuntimeException("Erro ao obter uma instância da classe" + entityClassType.getName() + ".", e);
			}
			
			for (Field field : columnMappings.keySet()) {				
				try {
					Object value = null;
					
					if (field.getType().isAnnotationPresent(Entity.class)) {
						ClassMap<?> classMap = dataSource.getMappingFor(field.getType());
						value = dbCursor.getValue(columnMappings.get(field), classMap.getIdProperty().getType());
					} else					
						value = dbCursor.getValue(columnMappings.get(field), field.getType());
					
					if (value != null) {
						field.setAccessible(true);
						
						if (field.isAnnotationPresent(ManyToOne.class) ||
						   (field.isAnnotationPresent(OneToOne.class) && "".equals(field.getAnnotation(OneToOne.class).mappedBy()))) {
							Object referencedObject = field.getType().newInstance();
							value = setReferencedId(referencedObject, value);							
						}
						
						field.set(object, getObjectValue(value, field.getType()));
					}
				} catch (Exception e) {
					throw new RuntimeException("Erro ao inicializar as propriedades do objeto.", e);
				}
			} 			
			list.add(object); 
		} while (dbCursor.next());
		
		dbCursor.close();
		return list;
	}
	
	@Override
	public T buildObject(DbCursor dbCursor) {
		return buildList(dbCursor).get(0);
	}

	@Override
	public List<String> getColumns() {
		return new ArrayList<String>(columnMappings.values());
	}
	
	@Override
	public String getTableName() {
		return tableName;
	}

	@Override
	public Field getIdProperty() {
		return idProperty;
	}

	@Override
	public String getIdColumn() {
		return idColumn;
	}
}
