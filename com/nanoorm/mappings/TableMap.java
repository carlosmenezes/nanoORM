package com.nanoorm.mappings;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.nanoorm.annotations.Column;
import com.nanoorm.annotations.Entity;
import com.nanoorm.annotations.Id;
import com.nanoorm.annotations.ManyToMany;
import com.nanoorm.annotations.ManyToOne;
import com.nanoorm.annotations.OneToOne;

/**
 * TableMap é responsável por obter informações de uma classe e
 * partir delas gerar sua representação na forma de tabela em um banco de dados.
 */
public abstract class TableMap<T> {

	protected Class<T> entityClassType;
	protected String tableName;
	protected HashMap<Field, String> tableColumns = new HashMap<Field, String>();
	protected Field idProperty;
	protected String primaryKey;
	protected HashMap<String, HashMap<String, String>> foreignKeyMap = new HashMap<String, HashMap<String,String>>();
	protected HashMap<String, HashMap<String, String>> properties;
	protected String createTableInstruction;
	protected String primaryKeyInstruction;
	protected List<String> constraintInstructions;

	/**
	 * Obtém informações de uma tabela informada no parâmetro tableName.
	 */
	protected abstract void getTableInfo();
	
	/**
	 * Obtém informações da classe passada como parâmetro.
	 */
	protected void getClassInfo() {
		Entity entity = entityClassType.getAnnotation(Entity.class);
		tableName = "".equals(entity.tableName()) ? entityClassType.getSimpleName() : entity.tableName();
		
		for (Field field : entityClassType.getDeclaredFields()) {
			String column = field.isAnnotationPresent(Column.class) ? field.getAnnotation(Column.class).name() : field.getName(); 
			
			if (field.isAnnotationPresent(Id.class)) {
				primaryKey = column;
				idProperty = field;
			}			
			tableColumns.put(field, column);
			
			if  (field.isAnnotationPresent(ManyToOne.class)	||
				(field.isAnnotationPresent(OneToOne.class) && !"".equals(field.getAnnotation(OneToOne.class).mappedBy())))  {									
					ParameterizedType type = (ParameterizedType) field.getGenericType();
		            Type[] types = type.getActualTypeArguments();           		            
		            String foreignColumn = "";
		            String foreignTable = "";
		            
		            Entity foreignEntity = types[0].getClass().getAnnotation(Entity.class);       
		            foreignTable =  "".equals(foreignEntity.tableName()) ? types[0].getClass().getSimpleName() : foreignEntity.tableName();	
		            
		            for(Field foreignField : (types[0].getClass()).getDeclaredFields()){
		            	String fColumn = foreignField.isAnnotationPresent(Column.class) ? foreignField.getAnnotation(Column.class).name() : foreignField.getName();
		            	if (foreignField.isAnnotationPresent(Id.class)) 
		            		foreignColumn = fColumn;		    			
		            }		            
		            HashMap<String, String> foreignData = new HashMap<String, String>();
		            foreignData.put(foreignTable, foreignColumn);
		            foreignKeyMap.put(column,foreignData);									
			}
			if (field.isAnnotationPresent(ManyToMany.class)){
				
				
				
			}
						
		}		
	}
	
	protected abstract String getSqlType(Class<?> propertyType);
	
	/**
	 * Constrói a instrução SQL Create Table.
	 */
	protected abstract String buildCreateTable();
	
	/**
	 * Cria a chave primária da tabela.
	 */
	protected abstract String buildPrimaryKey();
	
	/**
	 * Constrói uma chave estrangeira para entidade corrente.
	 * @param foreignKeyColumn Representa a coluna chave estrangeira da tabela.
	 * @param primaryKeyColumn Representa a chave primária que será referenciada pela foreignKey.
	 * @param foreignKeyTable Representa a tabela que terá o campo referenciado pela foreignKey.
	 */
	protected abstract String buildForeignKey(String foreignKeyColumn,
			String primaryKeyColumn, String foreignKeyTable);
	
	/**
	 * Atualiza o tipo da coluna informada na tabela.
	 * @param column Representa a coluna que será alterada no banco de dados.
	 * @param newType Representa o tipo que a coluna passará a ter.
	 */
	protected abstract String buildAlterColumn(String column, String newType);
	
	/**
	 * Constrói uma instância de TableMap para a classe informada no parâmetro entityClassType.
	 * @param entityClassType Classe que se deseja mapear para o banco de dados.
	 */
	public TableMap(Class<T> entityClassType) {
		this.entityClassType = entityClassType;
		getClassInfo();
		getTableInfo();		
	}
	
	/**
	 * Cria a instrução necessária para a criação da tabela no banco.
	 * @return A instrução para criação da tabela.
	 */
	public String createTable() {		
		return buildCreateTable();		
	}
	
	/**
	 * Cria uma lista com as restrições de chaves e campos na tabela.
	 * @return Um List&lt;String&gt; com todas as instruções necessárias
	 * para a criação das restrições da tabela.
	 */
	public List<String> createConstraints() {
		return new ArrayList<String>();		
	}

	/**
	 * Cria a instrução necessária para a atualização da tabela no banco.
	 * @return A instrução para atualização da tabela.
	 */
	public String updateTable() {
		return null;
	}
	
	/**
	 * Cria uma lista com as restrições de chaves e campos na tabela.
	 * @return Um List&lt;String&gt; com todas as instruções necessárias
	 * para a atualização das restrições da tabela.
	 */
	public List<String> updateConstraints() {
		return null;
	}

}
