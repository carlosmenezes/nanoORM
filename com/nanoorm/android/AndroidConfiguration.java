package com.nanoorm.android;

import android.content.Context;

import com.nanoorm.Configuration;
import com.nanoorm.DataSourceType;

/**
 * Especialização da classe Configuration criada para facilitar o processo de 
 * configuração do framework na plataforma android. 
 * @author Carlos Eduardo Pacheco Menezes
 */
public class AndroidConfiguration extends Configuration {

	private Context context;

	public AndroidConfiguration() { super(); }
	
	/**
	 * Constrói um objeto AndroidConfiguration com o contexto informado no parâmetro context.
	 * @param context Contexto da aplicação a ser utilizado pelo objeto AndroidConfiguration.
	 */
	public AndroidConfiguration(Context context) {
		this();
		this.context = context;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}
	
	/**
	 * Adiciona os parâmetros necessários para a conexão com o banco de dados.
	 * @param        url Caminho para o banco de dados.
	 */
	public Configuration addDataSourceConfig(String url) {
		return super.addDataSourceConfig(DataSourceType.ANDROID, url, "", "");
	}
	
}
