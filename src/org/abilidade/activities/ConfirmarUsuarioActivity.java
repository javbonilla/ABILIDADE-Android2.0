package org.abilidade.activities;

import org.abilidade.R;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;

public class ConfirmarUsuarioActivity extends Activity {
	
	// ************************** DEFINICION DE ATRIBUTOS DE LA CLASE  ************************** //
	
	// Elementos de pantalla de la Activity
	private Button botonVolver;
	
	// ************************** DEFINICION DE METODOS DE LA CLASE  ************************** //
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.confirmar_usuario);
		
		// Localizar los elementos de pantalla
		botonVolver = (Button) findViewById(R.id.confirmarUsuarioBotonVolver);
		
	    
	    // Accion en caso de pulsar el boton de volver
	    botonVolver.setOnClickListener(new View.OnClickListener() {
					
				@Override
				public void onClick(View v) {
					finish();
				}
	     });
  }
}