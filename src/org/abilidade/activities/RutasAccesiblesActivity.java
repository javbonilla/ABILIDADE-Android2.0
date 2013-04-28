package org.abilidade.activities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.abilidade.R;
import org.abilidade.application.AbilidadeApplication;
import org.abilidade.map_components.OverlayItemPunto;
import org.abilidade.mapa.MapaActivity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

	// Definicion de componentes de pantalla
	private ListView listView;
	private ProgressDialog pDialog;
	
	// Definiciion de atributos de la clase
	private List<String> sListadoRutas; 
	private int iRuta;
	private String sNombreRuta;
	
	// Definicion del Array con las rutas
	//private String[] listado = new String[] {"01. Calzada Romana", "02. Casa Museo Guayasamin", "03. Complejo Cultural San Francisco", "04. Juderías Vieja y Nueva", "05. Museo de Historia y Cultura Casa de Pedrilla", 
	//		"06. Museo Vostell Malpartida", "07. Parque del Príncipe", "08. Parque del Rodeo", "09. Paseo Cánovas - Paseo Calvo Sotelo", "10. Santuario Virgen de la Montaña"};
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarContentView(R.layout.rutas_accesibles_list);
		
		// Se inicializa la lista de rutas accesibles
		sListadoRutas = new ArrayList<String>();
		
		// Se reciben todas las rutas accesibles desde el servidor, mostrando un ProgressDialog mientras tanto
		descargarRutas();
		
		// Creamos un Adapter para acceder a los datos de nuestro listado. 
		// Cada item se mostrará en un view definido por Android (simple_list_item_1)
		ListAdapter adapter = new ArrayAdapter<String> (RutasAccesiblesActivity.this, android.R.layout.simple_list_item_1, sListadoRutas);
		
		// Enlazamos nuestro Adapter con nuestra vista
		listView = (ListView) findViewById(android.R.id.list);
		listView.setAdapter(adapter);
		
		// Mostramos un mensaje cuando el usuario pulse un item
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent();
	        	
	        	intent.setClass(RutasAccesiblesActivity.this, MapaActivity.class);
	        	intent.putExtra(AbilidadeApplication.RutaAccesibleParametro, iRuta);
	        	
	        	startActivity(intent);
	        	
	        	// Se finaliza esta Activity
	        	finish();
			}
		});
	}
	
	private void descargarRutas() {
		String jsonPuntos = null;
		JSONObject jObject = null;
		JSONArray menuObject = null;
		
		String sRuta;
		
		// 1. Se recibe el JSON para trocearlo y obtener los puntos
    	jsonPuntos = recibirJSON();
    	
    	// 2. Se trocea el JSON recibido
    	try {
			jObject = new JSONObject(jsonPuntos);
			menuObject = jObject.getJSONArray(AbilidadeApplication.GESTION_PUNTOS_DATA);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	
    	// 3. Se va extrayendo la informacion del JSON
    	int totalElementos = menuObject.length();
		for (int i=0 ; i<totalElementos; i++) {
    		try {
				
    			// Se reciben los datos de la ruta
    			iRuta        = menuObject.getJSONArray(i).getInt(0);
				sNombreRuta  = menuObject.getJSONArray(i).getString(1);
				
				Log.d("RutasAccesiblesActivity","sIdRuta:     " + iRuta);
				Log.d("RutasAccesiblesActivity","sNombreRuta: " + sNombreRuta);
				
				// Se concatenan
				sRuta = iRuta + ". " + sNombreRuta;
				
				// Y se guardan en el listado de rutas accesibles
				sListadoRutas.add(sRuta);
				Log.d("RutasAccesiblesActivity", "longitud sListadoRutas: " + sListadoRutas.size());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String recibirJSON() {
    	StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet("http://abilidade.eu/r/rutas.php");
		
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else {
				Log.e("RutasAccesiblesActivity", "Fallo al descargar el JSON de rutas");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
    }
	
	/*		CLASE ASYNCTASK
	 * Se usa esta clase para poder mostrar el dialogo de progreso mientras se envian y obtienen los datos.     
	 */

	class asynclogin extends AsyncTask< String, String, String > {
		 
		protected void onPreExecute() {
	    	//para el progress dialog
	        pDialog = new ProgressDialog(RutasAccesiblesActivity.this);
	        pDialog.setMessage(getString(R.string.descargandoRutas));
	        pDialog.setIndeterminate(false);
	        pDialog.setCancelable(false);
	        pDialog.show();
	    }

		protected String doInBackground(String... params) {
			// Se llama al metodo para descargar los puntos
			descargarRutas();
			
			return AbilidadeApplication.RETORNO_OK;
		}
	   
		protected void onPostExecute(String result) {
			
			// La descarga de puntos ha finalizado, por lo que se oculta el ProgressDialog
			pDialog.dismiss();
	     }
	}
}
