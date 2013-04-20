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
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
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
	
	private TextView textViewImagenPrincipal;
	private TextView textViewImagenAux1;
	private TextView textViewImagenAux2;
	
	private Button botonVolver;
	
	// Definicion de atributos de la clase
	private String sTitulo = null;
	private String sDireccion = null;
	private String sDescripcion = null;
	
	private String sImagenPrincipal = null;
	private String sImagenAux1      = null;
	private String sImagenAux2      = null;
	
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
		
		textViewImagenPrincipal = (TextView) findViewById(R.id.detallePuntoImagenPrincipal);
		textViewImagenAux1      = (TextView) findViewById(R.id.detallePuntoImagenAux1);
		textViewImagenAux2      = (TextView) findViewById(R.id.detallePuntoImagenAux2);
		
		botonVolver = (Button) findViewById(R.id.detallePuntoBotonVolver);
		
		// Formato para el texto de las imagenes
		SpannableString content = new SpannableString(textViewImagenPrincipal.getText().toString()); 
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0); 
		textViewImagenPrincipal.setText(content);
		
		// Recibimos el intent y sus parametros
		Intent intent    = getIntent();
		Bundle extras    = intent.getExtras();
		sTitulo          = extras.getString(AbilidadeApplication.PUNTO_TITULO);
		sDireccion       = extras.getString(AbilidadeApplication.PUNTO_DIRECCION);
		sDescripcion     = extras.getString(AbilidadeApplication.PUNTO_DESCRIPCION);
		sImagenPrincipal = extras.getString(AbilidadeApplication.PUNTO_IMAGEN_PRINCIPAL);
		sImagenAux1      = extras.getString(AbilidadeApplication.PUNTO_IMAGEN_AUX1);
		sImagenAux2      = extras.getString(AbilidadeApplication.PUNTO_IMAGEN_AUX2);
		
		if (sDescripcion.length() == 0 || sDescripcion.equals(null)) {
			sDescripcion = getString(R.string.sinDescripcion);
		}
		
		// Si no se han recibido enlaces para la imagen aux 1 o la imagen 2 aux, no se muestra el enlace
		if (sImagenAux1.length() == 0 || sImagenAux1.equals(null)) {
			textViewImagenAux1.setVisibility(View.INVISIBLE);
		}
		
		if (sImagenAux2.length() == 0 || sImagenAux2.equals(null)) {
			textViewImagenAux2.setVisibility(View.INVISIBLE);
		}
		
		// Asignamos los Listeners correspondientes a todos los elementos de pantalla
		botonVolver.setOnClickListener(onClickButtonListener);
		
		// Accion en caso de pulsar el texto de Olvidaste tu password
		textViewImagenPrincipal.setOnClickListener(new View.OnClickListener() {
			
		@Override
		public void onClick(View v) {
			// Lo que se hace es llamar al navegador con la pagina para recordar la password
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(AbilidadeApplication.RUTA_IMAGEN+sImagenPrincipal));
			startActivity(i);
		}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		textViewTitulo.setText(sTitulo);
		textViewDireccion.setText(sDireccion);
		textViewDescripcion.setText(sDescripcion);
	}
	
	private OnClickListener onClickButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
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
