package org.abilidade.db;

import org.abilidade.R;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;


public class PuntoProvider extends ContentProvider {
	
	/**
	 * Definicion de atributos y constantes importantes para el ContentProvider
	 */
	public static final String PROVIDER_NAME = "org.abilidade.punto";
	public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/puntos");
	
	private static final int PUNTOS = 1;
	private static final int PUNTOS_ID = 2;
	
	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, "puntos", PUNTOS);
		uriMatcher.addURI(PROVIDER_NAME, "puntos/#", PUNTOS_ID);
	}
	
	private SQLiteDatabase abilidadeDB;

	/**
	 * Definicion de los metodos de la clase
	 */
	
	/**
	 * onCreate: Initialize the ContentProvider on startup. Open the database abilidadeDB
	 */
	@Override
	public boolean onCreate() {
		AbilidadeSQLHelper helper = new AbilidadeSQLHelper(getContext());
		abilidadeDB = helper.getWritableDatabase();
		return abilidadeDB == null ? false : true;
	}
	
	
	/**
	 * getType: Handles requests for the MIME type of the data at the given URI
	 * @param uri: the URI to query.
	 */
	@Override
	public String getType(Uri uri) {
		switch(uriMatcher.match(uri)) {
			case PUNTOS: // Conjunto de puntos
				return "vnd.android.cursor.dir/vnd.accesible.punto";
			case PUNTOS_ID: // Un solo punto
				return "vnd.android.cursor.item/vnd.accesible.punto";
			default:
				throw new IllegalArgumentException(getContext().getResources().getString(R.string.providerUriNoSoportada) + " " + uri);
		}
	}
	
	/**
	 * insert: Handles requests to insert a new row
	 * @param uri: The content:// URI of the insertion request.
	 * @param values: A set of column_name/value pairs to add to the table.
	 * @return The URI for the newly inserted item.
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		
		long rowId = abilidadeDB.replace(DatabaseCommons.Punto.TABLE_NAME, null, values);
		
		if (rowId > 0) {		
			// Returns the Uri if insertion is OK
			Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(_uri, null);
			getContext().getContentResolver().notifyChange(CONTENT_URI, null);
			
			return _uri;
		}
		throw new SQLException(getContext().getResources().getString(R.string.providerFalloInsercion) + " " + uri);
	}

	/**
	 * delete Handles requests to delete one or more rows
	 * @param uri The full URI to query, including a row ID (if a specific record is requested).
	 * @param where, whereargs	An optional restriction to apply to rows when deleting.
	 * @return The number of rows affected.
	 */
	@Override
	public int delete(Uri uri, String where, String[] whereargs) {
		int count = 0;
		Context context = getContext();
		
		switch(uriMatcher.match(uri)) {
			case PUNTOS:
				count = abilidadeDB.delete(DatabaseCommons.Punto.TABLE_NAME, where, whereargs);
				break;
			case PUNTOS_ID:
				String id = uri.getPathSegments().get(1);
				String whereClause = DatabaseCommons.Punto._ID + " = " + id + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "");
				count = abilidadeDB.delete(DatabaseCommons.Punto.TABLE_NAME, whereClause, whereargs);
				
				break;
			default:
				throw new SQLException(context.getResources().getString(R.string.providerUriNoSoportada) + " " + uri);
		}
		context.getContentResolver().notifyChange(uri, null);
		return count;
 	}
	
	/**
	 * update Handles requests to update one or more rows
	 * @param uri The URI to query. This can potentially have a record ID if this is an update request for a specific record.
	 * @param values A Bundle mapping from column names to new column values (NULL is a valid value).
	 * @param selection An optional filter to match rows to update.
	 * @return the number of rows affected.
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int count = 0;
		Context context = getContext();
		
		switch(uriMatcher.match(uri)) {
			case PUNTOS:
				count = abilidadeDB.update(DatabaseCommons.Punto.TABLE_NAME, values, selection, selectionArgs);
				break;
			case PUNTOS_ID:
				String id = uri.getPathSegments().get(1);
				String whereClause = DatabaseCommons.Punto._ID + " = " + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
				count = abilidadeDB.update(DatabaseCommons.Punto.TABLE_NAME, values, whereClause, selectionArgs);
				break;
			default:
				throw new SQLException(context.getResources().getString(R.string.providerUriNoSoportada) + uri);
		}
		context.getContentResolver().notifyChange(uri, null);
		return count;
	}
 
	/** query Handles query requests from clients
	 * @param uri The URI to query. This will be the full URI sent by the client
	 * @param projection the list of columns to put into the cursor. If null all columns are included.
	 * @param selection A selection criteria to apply when filtering rows.
	 * @param selectionArgs You may include ?s in selection, which will be replaced by the values from selectionArgs
	 * @param sortOrder How the rows in the cursor should be sorted.
	 * @return a Cursor or null.
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		sqlBuilder.setTables(DatabaseCommons.Punto.TABLE_NAME);
		
		if (uriMatcher.match(uri) == PUNTOS_ID) {
			String id = uri.getPathSegments().get(1);
			String whereClause = DatabaseCommons.Punto._ID + " = " + id;
			sqlBuilder.appendWhere(whereClause);
		}
		
		if (sortOrder == null || sortOrder == "") {
			sortOrder = DatabaseCommons.Punto.DEFAULT_SORT_ORDER;
		}
		
		Cursor c = sqlBuilder.query(abilidadeDB, projection, selection, selectionArgs, null, null, sortOrder);
		// Registry the changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		
		return c;
	}
}
