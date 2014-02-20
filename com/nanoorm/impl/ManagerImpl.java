package com.nanoorm.impl;

import java.io.Serializable;
import java.util.List;

import com.nanoorm.DataSource;
import com.nanoorm.Manager;

/**
 * Class ManagerImpl
 * @author Carlos Eduardo Pacheco Menezes
 */
public class ManagerImpl implements Manager {

	private DataSource dataSource;

	/**
	 * @param        connection Conexão a ser utilizada com a fonte de dados.
	 * @param        dataSource Fonte de dados a ser utilizada para as operações de
	 * persistência.
	 */
	public ManagerImpl (DataSource dataSource)	{
		this.dataSource = dataSource;
	}

	/**
	 * Persiste uma Entidade do tipo T na fonte de dados.
	 * @param <T>
	 * @param        entity Entidade a ser persistida na fonte de dados.
	 */
	public <T> void save (T entity) {
		dataSource.open();
		dataSource.save(entity);
		dataSource.close();
	}

	/**
	 * Carrega um objeto da fonte de dados com o identificador informado no parâmetro
	 * id.
	 * @return       T
	 * @param 		 entityClass Classe a qual se deseja carregar o objeto.
	 * @param        id Identificador do objeto a ser carregado da fonte de dados.
	 * @throws Exception 
	 */
	public <T> T load (Class<T> entityClass, Serializable id) {
		dataSource.open();
		T entity = dataSource.load(entityClass, id);
		dataSource.close();
		return entity;
	}

	/**
	 * Remove a entidade informada da fonte de dados.
	 * @param <T>
	 * @param        entity Entidade a ser removida da fonte de dados.
	 */
	public <T> void delete (T entity) {
		dataSource.open();
		dataSource.delete(entity);
		dataSource.close();
	}

	/**
	 * Atualiza a entidade informada no parâmetro entity.
	 * @param <T>
	 * @param        entity Entidade a ser atualizada.
	 */
	public <T> void update (T entity) {
		dataSource.open();
		dataSource.update(entity);
		dataSource.close();
	}

	/**
	 * Cria uma lista com todas as entidades da classe informada no parâmetro
	 * entityClass.
	 * @param        entityClass Classe cujo a lista com todas as entidades será
	 * gerada.
	 */
	public <T> List<T> list (Class<T> entityClass)	{
		dataSource.open();
		List<T> entities = dataSource.list(entityClass);
		dataSource.close();
		return entities;
	}

	/**
	 * Cria um critério para o tipo informado no parâmetro entityClass.
	 * @param        entityClass Classe da entidade para qual será gerado o critério.
	 */
	public <T> void createCriteria (Class<T> entityClass) {
		throw new UnsupportedOperationException("Método não implementado.");		
	}

	@Override
	public void beginTransaction() {
		dataSource.beginTransaction();
	}

	@Override
	public void commitTransaction() {
		dataSource.commitTransaction();	
	}

	@Override
	public void rollbackTransaction() {
		dataSource.rollbackTransaction();
	}

	@Override
	public DataSource getDataSource() {
		return dataSource;
	}
	
}
