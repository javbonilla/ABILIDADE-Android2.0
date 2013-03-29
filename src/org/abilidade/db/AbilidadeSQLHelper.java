package org.abilidade.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AbilidadeSQLHelper extends SQLiteOpenHelper {

	/** 
	 * Clase que se encarga de gestionar la creacion y actualizacion de la base de datos ABILIDADE
	 */
	
	/** 
	 * Constructor
	 * @param context
	 */
	public AbilidadeSQLHelper(Context context) {
		super(context, DatabaseCommons.DB_NAME, null, DatabaseCommons.DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		if(db.isReadOnly()) {
			db = getWritableDatabase();
		}
		db.execSQL("CREATE TABLE " +
				DatabaseCommons.Punto.TABLE_NAME + " (" +
				DatabaseCommons.Punto._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + 
				DatabaseCommons.Punto.TITULO + " TEXT NOT NULL," +
				DatabaseCommons.Punto.DIRECCION + " TEXT NOT NULL," +
				DatabaseCommons.Punto.LOCALIDAD + " TEXT," + 
				DatabaseCommons.Punto.PROVINCIA + " TEXT," + 
				DatabaseCommons.Punto.ESTADO + " INTEGER NOT NULL," +
				DatabaseCommons.Punto.DESCRIPCION + " TEXT," +
				DatabaseCommons.Punto.CORREOE + " TEXT," +
				DatabaseCommons.Punto.LATITUD + " INTEGER NOT NULL," +
				DatabaseCommons.Punto.LONGITUD + " INTEGER NOT NULL," +
				DatabaseCommons.Punto.IMAGEN_PRINCIPAL + " TEXT NOT NULL," +
				DatabaseCommons.Punto.IMAGEN_AUX1 + " TEXT," +
				DatabaseCommons.Punto.IMAGEN_AUX2 + " TEXT," +
				DatabaseCommons.Punto.SINCRONIZADO + " INTEGER" + ")"
		);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		// TODO Do nothing at the moment
	}
}
