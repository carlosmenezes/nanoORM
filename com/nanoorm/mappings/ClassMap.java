package com.nanoorm.mappings;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

import com.nanoorm.DbCursor;

/**
 * Interface ClassMap
 * As classes que implementam esta interface realizam as operações necessárias de
 * mapeamento entre uma classe e a fonte de dados.
 */
public interface ClassMap <T>
{

	/**
	 * Constrói a instrução sql select para um objeto da entidade informada no
	 * parâmetro entity.
	 * @return       String
	 * @param        entityId Id do objeto para qual será gerada a instrução sql.
	 */
	public String buildSelectFor (Serializable entityId);

	/**
	 * Constrói a instrução sql select que seleciona todos os objetos.
	 * @return       String
	 */
	public String buildSelectForAll ();

	/**
	 * Constrói a instrução sql insert para a entidade informada no parâmetro entity.
	 * @return       String
	 * @param        entity Entidade para qual será gerada a instrução sql.
	 */
	public String buildInsertFor (T entity);

	/**
	 * Constrói a instrução sql update para a entidade informada no parâmetro entity.
	 * @return       String
	 * @param        entity Entidade para qual será gerada a instrução sql.
	 */
	public String buildUpdateFor (T entity);

	/**
	 * Constrói a instrução sql delete para a entidade informada no parâmetro entity.
	 * @return       String
	 * @param        entity Entidade para qual será gerada a instrução sql.
	 */
	public String buildDeleteFor (T entity);

	
	/**
	 * Instância um objeto da classe informada no parâmetro entityClass com os valores
	 * obtidos do parâmetro dbCursor.
	 * @return       T
	 * @param        dbCursor DbCursor do qual serão obtidos os valores do objeto a ser
	 * instanciado.
	 */
	public T buildObject(DbCursor dbCursor); 

	/**
	 * Instância uma lista de objetos da classe informada no parâmetro entityClass com
	 * os valores obtidos do parâmetro dbCursor.
	 * @return       List<T>
	 * @param        dbCursor DbCursor do qual serão obtidos os valores dos objetos a
	 * serem instanciados.
	 */
	public List<T> buildList(DbCursor dbCursor);

	/**
	 * Obtém um List&lt;String&gt; com todos os nomes das colunas
	 * que representam a classe mapeada por ClassMap no banco de dados. 
	 */
	public List<String> getColumns();
	
	/**
	 * Obtém o nome da tabela que representa a classe mapeada por ClassMap
	 * no banco de dados. 
	 */
	public String getTableName();
	
	/**
	 * Obtém o atributo que representa o id da classe mapeada.
	 */
	public Field getIdProperty();
	
	/**
	 * Obtém o nome da coluna chave primária da tabela que 
	 * a classe mapeada representa.
	 */		
	public String getIdColumn();
}
