package com.nanoorm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação usada para definir um atributo que representa a chave primária da tabela, sendo que apenas
 * atributos do tipo Serializable podem portar esta anotação.
 * O parâmetro autoGenerate é usado para informar que o valor da
 * chave deve ser gerado ou não pelo framework sua declaração é opcional e o valor padrão é true.
 * @author Carlos Eduardo Pacheco Menezes
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Id {
	/**
	 * Define se o valor da chave primária deve ser gerado pelo framework.
	 */
	boolean autoGenerate() default true;
}
