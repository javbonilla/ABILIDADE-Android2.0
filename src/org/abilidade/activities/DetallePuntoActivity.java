package org.abilidade.activities;

import org.abilidade.R;
import org.abilidade.application.AbilidadeApplication;
import org.abilidade.db.DatabaseCommons;
import org.abilidade.db.PuntoProvider;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import greendroid.app.GDActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;

public class DetallePuntoActivity extends GDActivity {
	
	// Definicion de los componentes de pantalla
	private TextView textViewTitulo;
	private TextView textViewDireccion;
	private TextView textViewDescripcion;
	private TextView textViewNoImagenesAux;
	private ImageView imageViewImagenPrincipal;
	private ImageView imageViewImagenAux1;
	private ImageView imageViewImagenAux2;
	private Button botonVolver;
	
	// Definicion de atributos de la clase
	private long id = -1;
	private String titulo = null;
	private String direccion = null;
	private String descripcion = null;
	private Bitmap imagenPrincipal = null;
	private Bitmap imagenAux1 = null;
	private Bitmap imagenAux2 = null;
	private String imagenPrincipalCod = null;
	private String imagenAux1Cod = null;
	private String imagenAux2Cod = null;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarContentView(R.layout.detalle_punto);
		
		// Elementos en la barra de accion --> Boton para localizar al usuario y boton de ayuda
		addActionBarItem(Type.Locate, R.id.action_bar_locate);
		
		// Localizacion de los componentes de pantalla
		textViewTitulo = (TextView) findViewById(R.id.detallePuntoTitulo);
		textViewDireccion = (TextView) findViewById(R.id.detallePuntoDireccion);
		textViewDescripcion = (TextView) findViewById(R.id.detallePuntoDescripcion);
		textViewNoImagenesAux = (TextView) findViewById(R.id.detallePuntoSinImagAux);
		imageViewImagenPrincipal = (ImageView) findViewById(R.id.detallePuntoImagenPrincipal);
		imageViewImagenAux1 = (ImageView) findViewById(R.id.detallePuntoImagenAux1);
		imageViewImagenAux2 = (ImageView) findViewById(R.id.detallePuntoImagenAux2);
		botonVolver = (Button) findViewById(R.id.detallePuntoBotonVolver);
		
		// Recibimos el intent y sus parametros
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		id = extras.getLong(AbilidadeApplication.puntoId);
		
		// Asignamos los Listeners correspondientes a todos los elementos de pantalla
		botonVolver.setOnClickListener(onClickButtonListener);
		imageViewImagenPrincipal.setOnClickListener(onClickImageListener);
		
		// Obtenemos la informacion del punto
		obtenerInfoPunto();
	}
	
	private void obtenerInfoPunto() {
		// Accedemos a la base de datos con el ID recibido por parametro y obtenemos toda la informacion del punto
		final String[] columnas = new String[] { DatabaseCommons.Punto._ID, DatabaseCommons.Punto.TITULO, DatabaseCommons.Punto.DIRECCION, 
				DatabaseCommons.Punto.DESCRIPCION, DatabaseCommons.Punto.IMAGEN_PRINCIPAL, DatabaseCommons.Punto.IMAGEN_AUX1, DatabaseCommons.Punto.IMAGEN_AUX2};
		Uri uri = PuntoProvider.CONTENT_URI;
		String selection = DatabaseCommons.Punto._ID + " = ?";
		String[] projection = new String[] { ""+id };
		Cursor cursor = managedQuery(uri, columnas, selection, projection, DatabaseCommons.Punto.DEFAULT_SORT_ORDER);
		
		// Comprobamos si ha habido cambios para recargar el cursor
    	cursor.setNotificationUri(getContentResolver(), uri);
    	
    	// La actividad se encargara de manejar el cursor segun su ciclo de vida
    	startManagingCursor(cursor);
    	
    	// Cada fila recuperada del cursor se vuelca al mapa. Si no hay ningun punto se informara al usuario, al igual que si ha habido error
    	if (cursor != null) {
    		if (cursor.moveToFirst()) {
    			// Recuperamos los campos y los almacenamos en un OverlayItemPunto, objeto que se cargara al mapa
    			titulo = cursor.getString(1);
    			direccion = cursor.getString(2);
        		descripcion = cursor.getString(3);
        		
        		imagenPrincipalCod = cursor.getString(4);
        		imagenAux1Cod = cursor.getString(5);
        		imagenAux2Cod = cursor.getString(6);
        		
        		imagenPrincipal = AbilidadeApplication.descodificarImagen(imagenPrincipalCod);
        		imagenAux1 = AbilidadeApplication.descodificarImagen(imagenAux1Cod);
        		imagenAux2 = AbilidadeApplication.descodificarImagen(imagenAux2Cod);
        		
    		} else {
    			Toast.makeText(DetallePuntoActivity.this, getString(R.string.errorDetallePunto), Toast.LENGTH_LONG).show();
    			finish();
    		}
    	} 
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		textViewTitulo.setText(titulo);
		textViewDireccion.setText(direccion);
		if (descripcion == null || descripcion.length() == 0) {
			descripcion = getString(R.string.detallePuntoDescripcion) + getString(R.string.sinDescripcion);
		} 
		textViewDescripcion.setText(descripcion);
		imageViewImagenPrincipal.setImageBitmap(imagenPrincipal); 
		if (imagenAux1 == null && imagenAux2 == null) {
			textViewNoImagenesAux.setVisibility(View.VISIBLE);
			imageViewImagenAux1.setVisibility(View.INVISIBLE);
			imageViewImagenAux2.setVisibility(View.INVISIBLE);
		} else {
			imageViewImagenAux1.setImageBitmap(imagenAux1);
			imageViewImagenAux2.setImageBitmap(imagenAux2);	
		}
	}
	
	private OnClickListener onClickButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
		}
	};
	
	private OnClickListener onClickImageListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			/*if(v.getId() == R.id.detallePuntoImagenPrincipal) {
				AbilidadeApplication.verFoto(DetallePuntoActivity.this, null, imagenPrincipalCod);
			}
			if(v.getId() == R.id.detallePuntoImagenAux1) {
				AbilidadeApplication.verFoto(DetallePuntoActivity.this, null, imagenAux1Cod);
			}
			if(v.getId() == R.id.detallePuntoImagenAux2) {
				AbilidadeApplication.verFoto(DetallePuntoActivity.this, null, imagenAux2Cod);
			}
			ARREGLAR
			*/
		}
	};
		
	/**
     * onHandleActionBarItemClick: listener para manejar el boton del ActionBar sobre el que hace clic el usuario
     * @param item: item sobre el que hizo clic el usuario
     * @param pos: posicion de item
     */
    @Override
    public boolean onHandleActionBarItemClick(ActionBarItem item, int pos) {
        if (item.getItemId() == R.id.action_bar_locate) {
        	finish();
        }
        return super.onHandleActionBarItemClick(item, pos);
    }
    
}
