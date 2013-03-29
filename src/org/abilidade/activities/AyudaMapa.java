package org.abilidade.activities;

import org.abilidade.R;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import greendroid.app.GDActivity;

public class AyudaMapa extends GDActivity {
	
	// Definicion de los componentes de pantalla
	private Button botonVolver;
		
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarContentView(R.layout.ayuda_mapa);
		
		// Localizacion de los componentes de pantalla
		botonVolver = (Button) findViewById(R.id.ayudaMapaBotonVolver);
		
		// Asignacion de listeners a los elementos de pantalla
		botonVolver.setOnClickListener(onClickButtonListener);
	}
	
	private OnClickListener onClickButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
		}
	};
}
