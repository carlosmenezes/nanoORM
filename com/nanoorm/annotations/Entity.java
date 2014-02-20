package com.nanoorm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Informa que uma classe é mapeada para uma tabela, tableName é opcional e seu valor padrão é "".
 * @author Carlos Eduardo Pacheco Menezes
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Entity {
	/**
	 * Nome da tabela para a qual se deseja mapear a classe.
	 */
	String tableName() default "";
}
