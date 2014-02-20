package com.nanoorm;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.nanoorm.annotations.Column;
import com.nanoorm.annotations.Entity;
import com.nanoorm.annotations.Id;

/**
 * Class Configuration
 * Responsável por obter as configurações necessárias para o funcionamento do
 * framework.
 * @author Carlos Eduardo Pacheco Menezes
 */
public class Configuration {

	private DataSourceType dataSourceType;
	private String url;
	private String userName;
	private String password;
	private List<Class<?>> classes = new ArrayList<Class<?>>();

	/**
	 * Get the value of dataSourceType
	 * @return the value of dataSourceType
	 */
	public DataSourceType getDataSourceType () {
		return dataSourceType;
	}

	/**
	 * Get the value of url
	 * @return the value of url
	 */
	public String getUrl () {
		return url;
	}

	/**
	 * Get the value of userName
	 * @return the value of userName
	 */
	public String getUserName () {
		return userName;
	}

	/**
	 * Get the value of password
	 * @return the value of password
	 */
	public String getPassword () {
		return password;
	}

	/**
	 * Get the value of classes
	 * @return the value of classes
	 */
	public List<Class<?>> getClasses() {
		return classes;
	}

	/**
	 * Adiciona os parâmetros necessários para a conexão com o banco de dados.
	 * @param        dataSourceType Tipo de base de dados desejado para a conexão.
	 * @param        url Caminho para o banco de dados.
	 * @param        userName Nome de usuário a ser utilizado no login com o banco.
	 * @param        password Senha a ser utilizada no login com o banco de dados.
	 */
	public Configuration addDataSourceConfig(DataSourceType dataSourceType, String url, String userName, String password) {
		this.dataSourceType = dataSourceType;
		this.url = url;
		this.userName = userName;
		this.password = password;
		return this;
	}

	/**
	 * Adiciona uma classe a configuração do framework. Para a classe ser adicionada com sucesso
	 * a configuração a mesma deve ser anotada com a anotação @Entity e ter no mínimo um atributo
	 * anotado com a anotação @Id.
	 * @param        entityClass Classe a ser adicionada a configuração do framework.
	 * @throws Exception Lançada quando a classe não atende aos requisitos descritos acima.
	 */
	public Configuration addClass(Class<?> entityClass) throws Exception	{
		int idCount = 0;	
		Entity entity = entityClass.getAnnotation(Entity.class);
		
		if (entity == null)
			throw new Exception("A classe " + entityClass + " não é uma entidade válida, pois não possui a anotação @Entity");
		
		Field[] fields = entityClass.getDeclaredFields();
		for(Field field : fields) {
			Annotation[] annotations = field.getAnnotations();
			for (Annotation annotation : annotations) {
				if (annotation.annotationType() == Id.class) 
					idCount++;
				
				if (annotation.annotationType() == Column.class) {
					if("".equals(((Column)annotation).name()))
						throw new Exception("O parâmetro name da anotação Column não pode ser vazio.");
				}
			}
		}
		
		if (idCount > 1)
			throw new Exception("A classe " + entityClass + " não é uma entidade válida, pois possui mais de um atributo com a anotação @Id");
		
		if (idCount == 0)
			throw new Exception("A classe " + entityClass + " não é uma entidade válida, pois não possui um atributo com a anotação @Id");

		// Se tudo deu certo...
		classes.add(entityClass);
		return this;
	}

	/**
	 * Adiciona um conjunto de classes a configuração do framework. Para cada classe ser adicionada com sucesso
	 * a configuração a mesma deve ser anotada com a anotação @Entity e ter no mínimo um atributo
	 * anotado com a anotação @Id.
	 * @param classes Classes a serem adicionadas a configuração do framework. 
	 * @throws Exception Lançada quando uma das classes não atende aos requisitos descritos acima.
	 */
	public Configuration addClasses(Class<?>... classes) throws Exception {
		for (Class<?> clazz : classes)
			addClass(clazz);
		return this;
	}
}
