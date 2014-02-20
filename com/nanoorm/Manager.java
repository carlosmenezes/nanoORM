package com.nanoorm;

import java.io.Serializable;
import java.util.List;


/**
 * Interface Manager
 * Classes que implementam esta interface realizam as operações de persitência
 * básicas para uma fonte de dados qualquer.
 * @author Carlos Eduardo Pacheco Menezes
 */
public interface Manager {

	/**
	 * Persiste uma Entidade do tipo T na fonte de dados.
	 * @param <T>
	 * @param        entity Entidade a ser persistida na fonte de dados.
	 */
	public <T> void save (T entity);

	/**
	 * Carrega um objeto da fonte de dados com o identificador informado no parâmetro
	 * id.
	 * @param <T>
	 * @return       T
	 * @param 		 entityClass Classe a qual se deseja carregar o objeto.
	 * @param        id Identificador do objeto a ser carregado da fonte de dados.
	 * @throws Exception 
	 */
	public <T> T load (Class<T> entityClass, Serializable id);

	/**
	 * Remove a entidade informada da fonte de dados.
	 * @param <T>
	 * @param        entity Entidade a ser removida da fonte de dados.
	 */
	public <T> void delete (T entity);

	/**
	 * Atualiza a entidade informada no parâmetro entity.
	 * @param <T>
	 * @param        entity Entidade a ser atualizada.
	 */
	public <T> void update (T entity);

	/**
	 * Cria uma lista com todas as entidades da classe informada no parâmetro
	 * entityClass.
	 * @param <T>
	 * @param        entityClass Classe cujo a lista com todas as entidades será
	 * gerada.
	 */
	public <T> List<T> list (Class<T> entityClass);


	/**
	 * Cria um critério para o tipo informado no parâmetro entityClass.
	 * @param <T>
	 * @param        entityClass Classe da entidade para qual será gerado o critério.
	 */
	public <T> void createCriteria (Class<T> entityClass);
	
	/**
	 * Inicia uma transação com o fonte de dados.
	 */
	public void beginTransaction ();

	/**
	 * Finaliza a transação com o fonte de dados com sucesso.
	 */
	public void commitTransaction ();

	/**
	 * Finaliza a transação com o fonte de dados desfazendo todas as operações
	 * realizadas.
	 */
	public void rollbackTransaction ();
	
	public DataSource getDataSource();

}