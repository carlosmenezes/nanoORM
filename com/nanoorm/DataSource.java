package com.nanoorm;

import java.io.Serializable;
import java.util.List;

import com.nanoorm.mappings.ClassMap;

/**
 * Interface DataSource
 * A interface DataSource possui os métodos necessários para a implementação básica
 * do acesso a uma fonte de dados qualquer.
 */
public interface DataSource {

	/**
	 * Abre uma conexão com a fonte de dados desejada.
	 */
	public void open();

	/**
	 * Fecha uma conexão  ativa com a fonte de dados.
	 */
	public void close();

	/**
	 * Indica se a conexão com a fonte de dados está ativa.
	 * @return       boolean
	 */
	public boolean isOpen();

	/**
	 * Persiste uma Entidade do tipo T na fonte de dados.
	 * @param <T>
	 * @param        entity Entidade a ser persistida na fonte de dados.
	 */
	public <T> void save(T entity);

	/**
	 * Carrega um objeto da fonte de dados com o identificador informado no parâmetro
	 * id.
	 * @return       T
	 * @param 		 entityClass Classe a qual se deseja carregar o objeto.
	 * @param        id Identificador do objeto a ser carregado da fonte de dados.
	 * @throws Exception 
	 */
	public <T> T load(Class<T> entityClass, Serializable id);

	/**
	 * Remove a entidade informada da fonte de dados.
	 * @param        entity Entidade a ser removida da fonte de dados.
	 */
	public <T> void delete(T entity);

	/**
	 * Atualiza a entidade informada no parâmetro entity.
	 * @param        entity Entidade a ser atualizada.
	 */
	public <T> void update(T entity);

	/**
	 * Cria uma lista com todas as entidades da classe informada no parâmetro
	 * entityClass.
	 * @param        entityClass Classe cujo a lista com todas as entidades será
	 * gerada.
	 */
	public <T> List<T> list(Class<T> entityClass);

	/**
	 * Obtém uma instância de ClassMapping para a entidade informada no parâmetro
	 * entityClass.
	 * @param        entityClass Classe da entidade que se deseja obter o mapeamento.
	 */
	public <T> ClassMap<T> getMappingFor(Class<T> entityClass);

	/**
	 * Obtém o tipo da fonte de dados.
	 * @return O tipo da fonte de dados.
	 */
	public DataSourceType getDataSourceType();
	
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

	/**
	 * Obtém a conexão deste DataSource.
	 */
	public DbConnection getConnection();

}
