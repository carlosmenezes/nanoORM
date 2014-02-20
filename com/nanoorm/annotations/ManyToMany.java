package com.nanoorm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Esta anotação define um atributo como representante
 * de uma associação de muitos-para-muitos.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ManyToMany {
	/**
	 * Define o nome do atributo que mapeia o relacionamento da classe corrente
	 * com a classe dona do relacionamento, o atributo deve existir na outra
	 * classe que compõe o relacionamento.
	 */
	String mappedBy() default "";
	/**
	 * Define o nome da tabela associativa a ser criada
	 * no banco de dados, seu uso é opcional.
	 */
	String joinTable() default "";
	/**
	 * Nome da coluna que representa o atributo chave da classe que é dona do relacionamento.
	 */
	Column joinColumn() default @Column(name="");
	/**
	 * Nome da coluna que representa o atributo chave da outra classe do relacionamento.
	 */
	Column inverseJoinColumn() default @Column(name="");
}
