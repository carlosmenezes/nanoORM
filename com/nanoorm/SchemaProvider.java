package com.nanoorm;

import java.util.ArrayList;
import java.util.List;

import com.nanoorm.mappings.TableMap;
import com.nanoorm.mappings.TableMapFactory;


/**
 * Class SchemaProvider
 */
@SuppressWarnings("unchecked")
public class SchemaProvider {

	private DbConnection connection;
	private List<String> tables = new ArrayList<String>();
	private List<String> constraints = new ArrayList<String>();
	private List<TableMap<?>> tableMaps = new ArrayList();
	
	private void executeCommands() {
		try {
			connection.open();
			connection.beginTransaction();
			
			for(String table : tables)				
				connection.executeSql(table);
			
			for(String constraint : constraints)
				connection.executeSql(constraint);
			
			connection.commitTransaction();
		} catch (Exception e) {
			throw new RuntimeException("Erro durante a criação da tabela no banco de dados.", e);
		} finally {
			connection.rollbackTransaction();
			connection.close();
		}
	}
	
	/**
	 * Instancia um SchemaProvider com a configuração necessária informada no parâmetro configuration.
	 * @param configuration Configuração necessária para a criação do esquema de banco de dados.
	 */
	
	public SchemaProvider(Configuration configuration) {
		try {
			//this.connection = DbConnectionFactory.getConnection(configuration.getDataSourceType(), configuration.getUrl(), configuration.getUserName(), configuration.getPassword());
		} catch (Exception e) {
			throw new RuntimeException("Erro durante a obtenção da conexão com o banco de dados.", e);
		}
		
		for (Class<?> entityClass : configuration.getClasses()){
			TableMap<?> tableMap = TableMapFactory.getTableMap(entityClass, configuration.getDataSourceType());
			tableMaps.add(tableMap);			
		}			
	}
	
	/**
	 * Cria o esquema de banco de dados com base no modelo de classes configurado no
	 * arquivo nanoorm.xml.
	 * @param        commitOnFinish Usado para informar se o esquema de tabelas gerado
	 * deve ser gravado no banco de dados.
	 */
	public void create (boolean commitOnFinish) {	
		for(TableMap<?> tableMap : tableMaps) {
			String sql = tableMap.createTable();
			System.out.println(sql);
			tables.add(sql);
		}
		
		for(TableMap<?> tableMap : tableMaps)
			constraints.addAll(tableMap.createConstraints());			
		
		if (commitOnFinish)
			executeCommands();
	}

	/**
	 * Atualiza o esquema de banco de dados com base no modelo de classes configurado
	 * no arquivo nanoorm.xml.
	 * @param        commitOnFinish Usado para informar se o esquema de tabelas gerado
	 * deve ser gravado no banco de dados.
	 */
	public void update (boolean commitOnFinish) {
		for(TableMap<?> tableMap : tableMaps)
			tables.add(tableMap.updateTable());
				
		
		for(TableMap<?> tableMap : tableMaps)
			constraints.addAll(tableMap.updateConstraints());			
		
		if (commitOnFinish) 
			executeCommands();
	}
}
