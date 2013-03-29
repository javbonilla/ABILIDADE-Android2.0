package org.abilidade.activities;

import org.abilidade.R;
import org.abilidade.application.AbilidadeApplication;
import org.abilidade.mapa.MapaActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import greendroid.app.GDActivity;

public class RutasAccesiblesActivity extends GDActivity {

	// Definición de componentes de pantalla
	private ListView listView;
	
	// Definición del Array con las rutas
	private String[] listado = new String[] {"01. Calzada Romana", "02. Casa Museo Guayasamin", "03. Complejo Cultural San Francisco", "04. Juderías Vieja y Nueva", "05. Museo de Historia y Cultura Casa de Pedrilla", 
			"06. Museo Vostell Malpartida", "07. Parque del Príncipe", "08. Parque del Rodeo", "09. Paseo Cánovas - Paseo Calvo Sotelo", "10. Santuario Virgen de la Montaña"};
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarContentView(R.layout.rutas_accesibles_list);
		
		// Creamos un Adapter para acceder a los datos de nuestro listado. 
		// Cada item se mostrará en un view definido por Android (simple_list_item_1)
		ListAdapter adapter = new ArrayAdapter<String> (this, android.R.layout.simple_list_item_1, listado);
		
		// Enlazamos nuestro Adapter con nuestra vista
		listView = (ListView) findViewById(android.R.id.list);
		listView.setAdapter(adapter);
		
		// Mostramos un mensaje cuando el usuario pulse un item
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent();
	        	
	        	intent.setClass(RutasAccesiblesActivity.this, webViewActivity.class);
	        	intent.putExtra(AbilidadeApplication.RutaAccesibleParametro, position);
	        	
	        	startActivity(intent);
			}
		});
	}
}
