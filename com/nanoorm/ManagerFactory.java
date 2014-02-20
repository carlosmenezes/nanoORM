package com.nanoorm;

import java.util.HashMap;
import java.util.Map;

import com.nanoorm.impl.DataSourceImpl;
import com.nanoorm.impl.ManagerImpl;

/**
 * Class ManagerFactory
 * Esta classe é reponsável por obter as configurações necessárias ao funcionamento
 * do framework e instanciar objetos que implementem a interface Manager.
 * @author Carlos Eduardo Pacheco Menezes
 */
public class ManagerFactory {

	private static Map<Configuration, Manager> registeredManagers = new HashMap<Configuration, Manager>();
	
	private ManagerFactory() { }

	/**
	 * Constrói uma instância de Manager a partir das configurações obtidas
	 * no parâmetro configuration.
	 * @param configuration Configuração a ser utilizada na contrução da instância de Manager.
	 * @return       com.nanoorm.Manager
	 * @throws Exception Caso seja informado um banco de dados sem suporte pelo framework.
	 */
	public static Manager buildManager (Configuration configuration) throws Exception {
		DbConnection connection = DbConnectionFactory.getConnection(configuration);
		
		DataSource dataSource = new DataSourceImpl(connection, configuration.getDataSourceType());
		Manager manager = new ManagerImpl(dataSource); 
		
		registeredManagers.put(configuration, manager);
		
		return manager;
	}
	
	/**
	 * Obtém uma instância de Manager resposável por gerenciar os objetos
	 * da classe informada no parâmetro entityClass.
	 * @param registeredClass Classe registrada no framework que se deseja obter o Manager.
	 * @return
	 */
	protected static Manager getManagerForClass(Class<?> registeredClass) {		
		for (Configuration configuration : registeredManagers.keySet())
			if (configuration.getClasses().contains(registeredClass))
				return registeredManagers.get(configuration);
		
		throw new RuntimeException("A classe " + registeredClass.getName() + " não é uma classe registrada no framework.");
	}	
	
}
