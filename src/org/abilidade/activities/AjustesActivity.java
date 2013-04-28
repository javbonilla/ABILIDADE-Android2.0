package org.abilidade.activities;


import greendroid.app.GDActivity;

import org.abilidade.R;
import org.abilidade.application.AbilidadeApplication;

import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class AjustesActivity extends GDActivity {
	
	// ************************** DEFINICION DE ATRIBUTOS DE LA CLASE  ************************** //
	
	// Elementos de pantalla de la Activity
	
	private Button buttonCerrarUsuario;
	
	// Atributos de la clase
	
	// ************************** DEFINICION DE METODOS DE LA CLASE  ************************** //

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarContentView(R.layout.ajustes);
		
		// Localizacion de los elementos de pantalla
		buttonCerrarUsuario         = (Button) findViewById(R.id.ajustesBotonCerrarUsuario);
		
		// Accion en caso de pulsar el boton de login
		buttonCerrarUsuario.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				// 1. Se limpian los datos de usuario en la SharedPreferences
				SharedPreferences pref = getApplicationContext().getSharedPreferences(AbilidadeApplication.SHARED_PREFERENCES, 0); // 0 - for private mode
				Editor editor = pref.edit();
				
				editor.putBoolean(AbilidadeApplication.SHPF_LOGIN, false);
				editor.putString(AbilidadeApplication.SHPF_USUARIO, "");
				
				editor.commit();
				
				// 2. Se muestra un mensaje al usuario
				Toast.makeText(getApplicationContext(), getString(R.string.usuarioDesconectado), Toast.LENGTH_LONG).show();
				
				// 3. Se conecta con la pantalla de login de usuario
				Intent i = new Intent();
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Para cerrar todas las demas Activities
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Para comenzar la nueva Activity
                i.setClass(AjustesActivity.this, AccederActivity.class);
                startActivity(i);
                
                // 4. Se finaliza esta Activity
                Intent returnIntent = new Intent();
        		setResult(RESULT_OK,returnIntent);
                finish();
			}
		});
	}
	
	@Override
	protected void onStop() {
		
		// Se retorna como resultado RESULT_CANCEL, para que MapaActivity reciba este resultado y no termine cuando el usuario
		// ha tocado en "volver"
		Intent returnIntent = new Intent();
		setResult(RESULT_CANCELED,returnIntent);
		
		super.onStop();
	}

}