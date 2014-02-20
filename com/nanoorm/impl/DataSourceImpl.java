package com.nanoorm.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nanoorm.DataSource;
import com.nanoorm.DataSourceType;
import com.nanoorm.DbConnection;
import com.nanoorm.DbCursor;
import com.nanoorm.mappings.ClassMap;
import com.nanoorm.mappings.ClassMapFactory;

/**
 * Class DataSourceImpl Implementação padrão da interface DataSource.
 * @author Carlos Eduardo Pacheco Menezes
 */
public class DataSourceImpl implements DataSource {

	private Map<Class<?>, ClassMap<?>> classMappings;
	private DbConnection connection;
	private DataSourceType dataSourceType;

	/**
	 * Constrói uma instância de AndroidDataSource com uma conexão.
	 * 
	 * @return com.nanoorm.DataSource
	 * @param connection
	 *            Conexão que será utilizada pela fonte de dados.
	 * @param dataSourceType
	 *            Representa o tipo de dados desejado para a fonte de dados.
	 */
	public DataSourceImpl(DbConnection connection, DataSourceType dataSourceType) {
		this.connection = connection;
		this.dataSourceType = dataSourceType;
		this.classMappings = new HashMap<Class<?>, ClassMap<?>>();
	}

	/**
	 * Abre uma conexão com a fonte de dados desejada.
	 */
	public void open() {
		connection.open();
	}

	/**
	 * Fecha uma conexão ativa com a fonte de dados.
	 */
	public void close() {
		connection.close();
	}

	/**
	 * Indica se a conexão com a fonte de dados está ativa.
	 * 
	 * @return boolean
	 */
	public boolean isOpen() {
		return connection.isOpen();
	}

	/**
	 * Persiste uma Entidade do tipo T na fonte de dados.
	 * 
	 * @param entity
	 *            Entidade a ser persistida na fonte de dados.
	 */
	@SuppressWarnings("unchecked")
	public <T> void save(T entity) {
		ClassMap<T> classMap = (ClassMap<T>) getMappingFor(entity.getClass());
		String sql = classMap.buildInsertFor(entity);
		if (!isOpen())
			open();
		connection.executeSql(sql);
	}

	/**
	 * Carrega um objeto da fonte de dados com o identificador informado no
	 * parâmetro id.
	 * 
	 * @param <T>
	 * @return T
	 * @param entityClass
	 *            Classe a qual se deseja carregar o objeto.
	 * @param id
	 *            Identificador do objeto a ser carregado da fonte de dados.
	 * @throws InstantiationException
	 * @throws Exception
	 */
	public <T> T load(Class<T> entityClass, Serializable id) {
		ClassMap<T> classMap = getMappingFor(entityClass);
		T entity = null;

		DbCursor cursor = connection.executeSqlQuery(classMap.buildSelectFor(id));		
		entity = classMap.buildObject(cursor);

		return entity;
	}

	/**
	 * Remove a entidade informada da fonte de dados.
	 * 
	 * @param <T>
	 * @param entity
	 *            Entidade a ser removida da fonte de dados.
	 */
	@SuppressWarnings("unchecked")
	public <T> void delete(T entity) {
		ClassMap<T> classMap = (ClassMap<T>) getMappingFor(entity.getClass());
		connection.executeSql(classMap.buildDeleteFor(entity));
	}

	/**
	 * Atualiza a entidade informada no parâmetro entity.
	 * 
	 * @param <T>
	 * @param entity
	 *            Entidade a ser atualizada.
	 */
	@SuppressWarnings("unchecked")
	public <T> void update(T entity) {
		ClassMap<T> classMap = (ClassMap<T>) getMappingFor(entity.getClass());
		connection.executeSql(classMap.buildUpdateFor(entity));
	}

	/**
	 * Cria uma lista com todas as entidades da classe informada no parâmetro
	 * entityClass.
	 * 
	 * @param <T>
	 * @param entityClass
	 *            Classe cujo a lista com todas as entidades será gerada.
	 */
	public <T> List<T> list(Class<T> entityClass) {
		ClassMap<T> classMap = getMappingFor(entityClass);
		List<T> objects = null;

		DbCursor cursor = connection.executeSqlQuery(classMap.buildSelectForAll());		
		objects = classMap.buildList(cursor);

		return objects;
	}

	/**
	 * Obtém uma instância de ClassMap para a entidade informada no
	 * parâmetro entityClass.
	 * 
	 * @param <T>
	 * @param <T>
	 * @param entityClass
	 *            Classe da entidade que se deseja obter o mapeamento.
	 */
	@SuppressWarnings("unchecked")
	public <T> ClassMap<T> getMappingFor(Class<T> entityClass) {
		ClassMap<T> classMap;
		if (classMappings.containsKey(entityClass))
			classMap = (ClassMap<T>) classMappings.get(entityClass);
		else {
			classMap = ClassMapFactory.getClassMap(entityClass, this);
			classMappings.put(entityClass, classMap);
		}

		return classMap;
	}

	@Override
	public DataSourceType getDataSourceType() {
		return dataSourceType;
	}

	@Override
	public void beginTransaction() {
		connection.beginTransaction();
	}

	@Override
	public void commitTransaction() {
		connection.commitTransaction();
	}

	@Override
	public void rollbackTransaction() {
		connection.rollbackTransaction();
	}

	@Override
	public DbConnection getConnection() {
		return connection;
	}
	
}
