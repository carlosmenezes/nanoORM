package com.nanoorm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Vincula o atributo anotado a uma coluna da tabela, o parâmetro name é obrigatório
 * caso queira que a coluna tenha o mesmo nome do atributo
 * basta não usar esta anotação.
 * @author Carlos Eduardo Pacheco Menezes
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
	/**
	 * Nome da coluna a qual o atributo anotado será vinculado.
	 */
	String name();
}
