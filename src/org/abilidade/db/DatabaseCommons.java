package org.abilidade.db;

import android.provider.BaseColumns;

public class DatabaseCommons {
	
	/** 
	 * Esta clase solo contiene una abstraccion de las tablas de la base de datos y los nombres de los campos,
	 * para evitar asi tener que trabajar constantemente con hardcodes.
	 */
	
	/**
	 * Name and version of the Database
	 */
	public static final String DB_NAME = "abilidade.db";
	public static final int DB_VERSION = 1;
	
	/**
	 * Definicion de la tabla "punto"
	 */
	public static final class Punto implements BaseColumns {
		
		/* La clase no puede ser instanciada */
		private Punto() {}
		
		/* Abstraccion de los nombres de campos */
		public static final String TABLE_NAME = "punto";
		
		public static final String _ID = "_id";
		public static final String TITULO = "titulo";
		public static final String DIRECCION = "direccion";
		public static final String LOCALIDAD = "localidad";
		public static final String PROVINCIA = "provincia";
		public static final String ESTADO = "estado";
		public static final String DESCRIPCION = "descripcion";
		public static final String CORREOE = "correoe";
		public static final String LATITUD = "latitud";
		public static final String LONGITUD = "longitud";
		public static final String IMAGEN_PRINCIPAL = "imagen_principal";
		public static final String IMAGEN_AUX1 = "imagen_aux1";
		public static final String IMAGEN_AUX2 = "imagen_aux2";
		public static final String SINCRONIZADO =  "sincronizado";
		
		public static final String _COUNT = "12"; // El campo _id no se cuenta
		
		/* Orden por defecto para recuperar las filas de la tabla */
		public static final String DEFAULT_SORT_ORDER = _ID + " DESC";
	}
}
