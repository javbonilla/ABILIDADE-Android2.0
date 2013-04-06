package org.abilidade.activities;


import org.abilidade.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class AjustesActivity extends Activity {
	
	// ************************** DEFINICION DE ATRIBUTOS DE LA CLASE  ************************** //
	
	// Elementos de pantalla de la Activity
	
	private Button buttonCerrarUsuario;
	
	// Atributos de la clase
	
	// ************************** DEFINICION DE METODOS DE LA CLASE  ************************** //

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ajustes);
		
		// Localizacion de los elementos de pantalla
		buttonCerrarUsuario         = (Button) findViewById(R.id.ajustesBotonCerrarUsuario);
		
		// Accion en caso de pulsar el boton de login
		buttonCerrarUsuario.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				// 1. Se limpian los datos de usuario en la SharedPreferences
				//TODO Aqui es donde tengo que hacer la gestión de SharedPreferences
				
				// 2. Se muestra un mensaje al usuario
				Toast.makeText(getApplicationContext(), getString(R.string.usuarioDesconectado), Toast.LENGTH_LONG).show();
				
				// 3. Se conecta con la pantalla de login de usuario
				Intent i = new Intent();
                i.setClass(AjustesActivity.this, AccederActivity.class);
                startActivity(i);
                
                // 4. Se finaliza esta Activity
                finish();
			}
		});
	}

}