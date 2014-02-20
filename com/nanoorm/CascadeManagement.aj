package com.nanoorm;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.annotation.SuppressAjWarnings;

import com.nanoorm.annotations.Entity;
import com.nanoorm.annotations.ManyToMany;
import com.nanoorm.annotations.OneToMany;
import com.nanoorm.annotations.OneToOne;
import com.nanoorm.mappings.ClassMap;

/**
 * Aspecto responsável por realizar operações de persistência em cascata.
 */
public aspect CascadeManagement {

	// Não incluí um mapa para ManyToOne pois as operações de persistência em cascata
	// devem ser feitas sempre a partir do lado "dono" do relacionamento.
	private Map<Class<?>, List<Field>> oneToOneFields = new HashMap<Class<?>, List<Field>>();
	private Map<Class<?>, List<Field>> oneToManyFields = new HashMap<Class<?>, List<Field>>();
	private Map<Class<?>, List<Field>> manyToManyFields = new HashMap<Class<?>, List<Field>>();
	
	/* 
	 * As operações em cascata devem preceder as operações de gerenciamento
	 * de ManyToMany para evitarmos o salvamento de chaves nulas nas entidade associativas.
	 */
	declare precedence: CascadeManagement;

	/**
	 * Ponto de atuação responsável por interceptar a inicialização pela JVM
	 * das classes gerenciadas pelo framework.
	 */
	pointcut entityInit() : staticinitialization(@Entity *);
	/**
	 * Ponto de atuação responsável por interceptar as operações
	 * de persistência e atualização de objetos.
	 */
	pointcut saveManagement() : execution(public void Manager.save(..)) || execution(public void Manager.update(..));
	/**
	 * Ponto de atuação responsável por interceptar as operações de
	 * remoção de objetos.
	 */
	pointcut deleteManagement() : execution(public void Manager.delete(..));
	
	/**
	 * Adendo para o ponto de atuação entityInit(), obtém todas as associações
	 * da classe para posterior utilização do aspecto.
	 */
	@SuppressAjWarnings
	after() : entityInit() {
		Class<?> entityClass = thisJoinPointStaticPart.getSignature().getDeclaringType();
		
		for (Field field : entityClass.getDeclaredFields()) {
			if (field.isAnnotationPresent(OneToOne.class) && "".equals(field.getAnnotation(OneToOne.class).mappedBy())) {
				if (oneToOneFields.containsKey(entityClass)) {
					List<Field> fields = oneToOneFields.get(entityClass);
					fields.add(field);
				} else { 
					List<Field> fields = new ArrayList<Field>();
					fields.add(field);
					oneToOneFields.put(entityClass, fields);
				}
			} else if (field.isAnnotationPresent(OneToMany.class)) {
				if (oneToManyFields.containsKey(entityClass)) {
					List<Field> fields = oneToManyFields.get(entityClass);
					fields.add(field);
				} else { 
					List<Field> fields = new ArrayList<Field>();
					fields.add(field);
					oneToManyFields.put(entityClass, fields);
				}
			} else if (field.isAnnotationPresent(ManyToMany.class) && "".equals(field.getAnnotation(ManyToMany.class).mappedBy())) {
				if (manyToManyFields.containsKey(entityClass)) {
					List<Field> fields = manyToManyFields.get(entityClass);
					fields.add(field);
				} else { 
					List<Field> fields = new ArrayList<Field>();
					fields.add(field);
					manyToManyFields.put(entityClass, fields);
				}
			}
		}
	}
	
	/**
	 * Adendo para o ponto de atuação saveManagement(), realiza
	 * a persistência e atualização de objetos em cascata.
	 */
	after() returning() : saveManagement() {
		Object arg = thisJoinPoint.getArgs()[0];
		if (arg == null)
			return;

		Manager manager = null;
		Class<?> entityClass = arg.getClass();
		
		if (oneToOneFields.containsKey(entityClass)) {			
			List<Field> fields = oneToOneFields.get(entityClass);
			
			for (Field field : fields) {
				field.setAccessible(true);
				
				manager = ManagerFactory.getManagerForClass(field.getType());
				ClassMap<?> classMap = manager.getDataSource().getMappingFor(field.getType());
				Field idProperty = classMap.getIdProperty();
				idProperty.setAccessible(true);
				
				try {
					Object entity = field.get(arg);
					
					if (Integer.parseInt(idProperty.get(entity).toString()) > 0)
						manager.update(entity);
					else 
						manager.save(entity);
				} catch (Exception ex) {
					throw new RuntimeException("Falha ao salvar o objeto do atributo " + field.getName() + " em cascata.", ex);
				}
			}
		}
		
		if (oneToManyFields.containsKey(entityClass)) {
			List<Field> fields = oneToManyFields.get(entityClass);
			
			for (Field field : fields) {
				field.setAccessible(true);
				
				// obtem o parametro generico da colecao
				ParameterizedType type = (ParameterizedType)field.getGenericType();	
				Class<?> classBType = (Class<?>)type.getActualTypeArguments()[0];
				manager = ManagerFactory.getManagerForClass(classBType);	

				ClassMap<?> classMap = manager.getDataSource().getMappingFor(classBType);
				Field idProperty = classMap.getIdProperty();
				idProperty.setAccessible(true);
				
				try {
					List<?> objects = (List<?>) field.get(arg);			
					
					for (Object object : objects) {
						if (Integer.parseInt(idProperty.get(object).toString()) > 0)
							manager.update(object);
						else 
							manager.save(object);
					}
				} catch (Exception ex) {
					throw new RuntimeException("Falha ao salvar os objetos do atributo " + field.getName() + " em cascata.", ex);
				}
			}
		}
		
		if (manyToManyFields.containsKey(entityClass)) {
			List<Field> fields = manyToManyFields.get(entityClass);
			
			for (Field field : fields) {
				field.setAccessible(true);
				
				// obtem o parametro generico da colecao
				ParameterizedType type = (ParameterizedType)field.getGenericType();	
				Class<?> classBType = (Class<?>)type.getActualTypeArguments()[0];
				manager = ManagerFactory.getManagerForClass(classBType);	

				ClassMap<?> classMap = manager.getDataSource().getMappingFor(classBType);
				Field idProperty = classMap.getIdProperty();
				idProperty.setAccessible(true);
				
				try {
					List<?> objects = (List<?>) field.get(arg);			
					
					for (Object object : objects) {
						if (Integer.parseInt(idProperty.get(object).toString()) > 0)
							manager.update(object);
						else 
							manager.save(object);
					}
				} catch (Exception ex) {
					throw new RuntimeException("Falha ao salvar os objetos do atributo " + field.getName() + " em cascata.", ex);
				}
			}
		}
	}

	/**
	 * Adendo para o ponto de atuação deleteManagement(), realiza
	 * a remoção de objetos em cascata.
	 */
	after() returning() : deleteManagement() {
		Object arg = thisJoinPoint.getArgs()[0];
		if (arg == null)
			return;
		
		Manager manager = null;
		Class<?> entityClass = arg.getClass();
		
		if (oneToOneFields.containsKey(entityClass)) {			
			List<Field> fields = oneToOneFields.get(entityClass);
			
			for (Field field : fields) {
				field.setAccessible(true);
				
				manager = ManagerFactory.getManagerForClass(field.getType());
				
				try {
					Object entity = field.get(arg);					
					manager.delete(entity);					
				} catch (Exception ex) {
					throw new RuntimeException("Falha ao deletar o objeto do atributo " + field.getName() + " em cascata.", ex);
				}
			}
		}
		
		if (oneToManyFields.containsKey(entityClass)) {
			List<Field> fields = oneToManyFields.get(entityClass);
			
			for (Field field : fields) {
				field.setAccessible(true);
				
				// obtem o parametro generico da colecao
				ParameterizedType type = (ParameterizedType)field.getGenericType();	
				Class<?> classBType = (Class<?>)type.getActualTypeArguments()[0];
				manager = ManagerFactory.getManagerForClass(classBType);	
				
				try {
					List<?> objects = (List<?>) field.get(arg);			
					
					for (Object object : objects) 
						manager.delete(object);
				} catch (Exception ex) {
					throw new RuntimeException("Falha ao deletar os objetos do atributo " + field.getName() + " em cascata.", ex);
				}
			}
		}
		
		if (manyToManyFields.containsKey(entityClass)) {
			List<Field> fields = manyToManyFields.get(entityClass);
			
			for (Field field : fields) {
				field.setAccessible(true);
				
				// obtem o parametro generico da colecao
				ParameterizedType type = (ParameterizedType)field.getGenericType();	
				Class<?> classBType = (Class<?>)type.getActualTypeArguments()[0];
				manager = ManagerFactory.getManagerForClass(classBType);			
				
				try {
					List<?> objects = (List<?>) field.get(arg);			
					
					for (Object object : objects)
						manager.delete(object);
				} catch (Exception ex) {
					throw new RuntimeException("Falha ao deletar os objetos do atributo " + field.getName() + " em cascata.", ex);
				}
			}
		}
	}
}
