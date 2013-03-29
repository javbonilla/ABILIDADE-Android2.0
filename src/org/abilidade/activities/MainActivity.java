package org.abilidade.activities;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import greendroid.app.GDActivity;

import org.abilidade.R;
import org.abilidade.application.AbilidadeApplication;
import org.abilidade.db.DatabaseCommons;
import org.abilidade.db.PuntoProvider;
import org.abilidade.mapa.MapaActivity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

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

public class MainActivity extends GDActivity {
	
	// Definicion de atributos
	private String jsonPuntos;
	private JSONObject jObject;
	private JSONArray menuObject;
	
	// Definicion de elementos de pantalla
	private Button botonAltaPunto;
	private Button botonMapa;
	private Button botonRutasAccesibles;
	private Button botonDescargarPuntos;
	
	// Definicion de los campos de un punto
	private String titulo = "";
	private String direccion = "";
	private String localidad = "";
	private String provincia = "";
	private String descripcion = "";
	private String correoE = "";
	private double latitud = 0;
	private double longitud = 0;
	private int estado = 0;
	
	private String imagenPrincipalPath = "";
	private String imagenAux1Path = "";
	private String imagenAux2Path = "";

	private Bitmap imagenPrincipal = null;
	private Bitmap imagenAux1 = null; 
	private Bitmap imagenAux2 = null;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarContentView(R.layout.main);
        
        // Localizacion de componentes de pantalla
        botonAltaPunto = (Button) findViewById(R.id.botonAltaPunto);
        botonMapa = (Button) findViewById(R.id.botonMapa);
        botonRutasAccesibles = (Button) findViewById(R.id.botonRutasAccesibles);
        botonDescargarPuntos = (Button) findViewById(R.id.botonDescargarPuntos);
        
        botonAltaPunto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, AltaPuntoActivity.class);
				
				startActivity(intent);
			}
		});
		
		botonMapa.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, MapaActivity.class);
				
				startActivity(intent);
			}
		});
		
		botonRutasAccesibles.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, RutasAccesiblesActivity.class);
				
				startActivity(intent);
			}
		});
		
		botonDescargarPuntos.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "Descargando puntos. Espere, por favor.", Toast.LENGTH_LONG).show();
				
				// Recibimos el JSON para trocearlo y obtener los puntos
				jsonPuntos = recibirJSON();
				
				// Trocear el JSON recibido
				try {
					jObject = new JSONObject(jsonPuntos);
					menuObject = jObject.getJSONArray("data");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				// Bucle para ir sacando campos del JSON
				int totalElementos = menuObject.length();
				for (int i=0 ; i<totalElementos; i++) {
					try {
						titulo = menuObject.getJSONArray(i).getString(0);
						direccion = menuObject.getJSONArray(i).getString(1);
						descripcion = menuObject.getJSONArray(i).getString(2);
						correoE = menuObject.getJSONArray(i).getString(3);
						latitud = menuObject.getJSONArray(i).getDouble(4);
						longitud = menuObject.getJSONArray(i).getDouble(5);
						estado = menuObject.getJSONArray(i).getInt(6);
						imagenPrincipalPath = menuObject.getJSONArray(i).getString(7);
						
						registrarPunto();
						
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				
				Toast.makeText(getApplicationContext(), "Finalizada descarga de puntos. Ya puede consultarlos en el mapas", Toast.LENGTH_LONG).show();
			}
		});
    }
    
    private void registrarPunto() {
		
		String imagenPrincipalTexto = "";
		String imagenAux1Texto = "";
		String imagenAux2Texto = "";
		
		// Recibimos las imagenes desde el servidor
		recibirImagen();
		
		// Se escalan y convierten todas las imagenes a un tamaño adecuado
		imagenPrincipal = AbilidadeApplication.escalarImagen(imagenPrincipal);
		imagenPrincipalTexto = AbilidadeApplication.convertirImagen(imagenPrincipal);
		if (imagenAux1 != null) {
			imagenAux1 = AbilidadeApplication.escalarImagen(imagenAux1);
			imagenAux1Texto = AbilidadeApplication.convertirImagen(imagenAux1);
		}
		if (imagenAux2 != null) {
			imagenAux2 = AbilidadeApplication.escalarImagen(imagenAux2);
			imagenAux2Texto = AbilidadeApplication.convertirImagen(imagenAux2);
		}
				
		// Y ahora se informan todos los campos
		ContentValues values = new ContentValues();
		values.put(DatabaseCommons.Punto.TITULO, titulo);
		values.put(DatabaseCommons.Punto.DIRECCION, direccion);
		values.put(DatabaseCommons.Punto.DESCRIPCION, descripcion);
		values.put(DatabaseCommons.Punto.LOCALIDAD, localidad);
		values.put(DatabaseCommons.Punto.PROVINCIA, provincia);
		values.put(DatabaseCommons.Punto.ESTADO, AbilidadeApplication.ESTADO_ABIERTO);
		values.put(DatabaseCommons.Punto.CORREOE, correoE);
		values.put(DatabaseCommons.Punto.LATITUD, latitud);
		values.put(DatabaseCommons.Punto.LONGITUD, longitud);
		values.put(DatabaseCommons.Punto.SINCRONIZADO, estado); // Se registra el punto como no sincronizado
		values.put(DatabaseCommons.Punto.IMAGEN_PRINCIPAL, imagenPrincipalTexto);
		values.put(DatabaseCommons.Punto.IMAGEN_AUX1, imagenAux1Texto);
		values.put(DatabaseCommons.Punto.IMAGEN_AUX2, imagenAux2Texto);
				
		// Por ultimo, guardamos el punto en la BD local del terminal
		getContentResolver().insert(PuntoProvider.CONTENT_URI, values);
	}

    private void recibirImagen() {
    	
    	File img = new File("/sdcard/app/tmp/abilidade/" + imagenPrincipalPath);

        // Create directories
        new File("/sdcard/app/tmp/abilidade").mkdirs();

        // only download new images
        if (!img.exists()) {
	    	try {
	    		String ruta = "http://abilidade.eu/r/imgpoint/"+imagenPrincipalPath;
	    		URL imageUrl = new URL(ruta);
	    		InputStream in = imageUrl.openStream();
	    		OutputStream out = new BufferedOutputStream(new FileOutputStream(img));
	    			
	    		for (int b; (b = in.read()) != -1;) {
	   				out.write(b);
	   			}
	   			out.close();
	   			in.close();
	   		} catch (MalformedURLException e) {
	   			img = null;
	   		} catch (IOException e) {
	   			img = null;
	   		}
        }
        
        imagenPrincipal = BitmapFactory.decodeFile(img.getAbsolutePath());
    }
    
    private String recibirJSON() {
    	StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet("http://abilidade.eu/r/puntos.php");
		
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
				Log.e(MainActivity.class.toString(), "Failed to download file");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
    }
    
}