package org.abilidadev2.mapa;

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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.abilidadev2.R;
import org.abilidadev2.activities.AjustesActivity;
import org.abilidadev2.activities.AltaPuntoActivity;
import org.abilidadev2.activities.DetallePuntoActivity;
import org.abilidadev2.activities.RutasAccesiblesActivity;
import org.abilidadev2.application.AbilidadeApplication;
import org.abilidadev2.map_components.OverlayItemPunto;
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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapaActivity extends android.support.v4.app.FragmentActivity implements LocationListener {
	
	// Atributos relativos al mapa y su gestion
	private boolean bVistaSatelite; // false = mapa; true = satelite
	private boolean bDescargarPuntos;
	private boolean bCentrarVistaRuta;
	
	private Marker markerUsuario;
	
	private Hashtable<String,OverlayItemPunto> htPuntos;
	
	// Atributos relacionados con la localizacion del usuario
	private LocationManager lm;
	private int sensor;
	private String provider;
	private double dLatitud;
	private double dLongitud;
	
	// Atributos relacionados con la descarga de puntos desde el servidor
	private String jsonPuntos;
	private JSONObject jObject;
	private JSONArray menuObject;
	
	// Atributos relacionados con la descarga de rutas desde el servidor
	private String jsonRutas;
	private JSONObject jObjectRuta;
	private JSONArray menuObjectRuta;
	
	private String jsonPuntosRuta;
	private JSONObject jObjectPuntosRuta;
	private JSONArray menuObjectPuntosRuta; 
	
	private ProgressDialog pDialogPuntos;
	private ProgressDialog pDialogRuta;
	
	// Atributos relacionados con la gestion de rutas accesibles 
	private int iRuta;
	private double dLatitudRuta;
	private double dLongitudRuta;
	
	// Definicion de los campos de un punto
	private String sPuntoTitulo = "";
	private String sPuntoDireccion = "";
	private String sPuntoDescripcion = "";
	private String sPuntoCorreoE = "";
	private double dPuntoLatitud = 0;
	private double dPuntoLongitud = 0;
	private int iPuntoEstado = 0;
	
	private String sPuntoImagenPrincipalPath = "";
	private String sPuntoImagenPrincipalThumbPath = "";
	private String sPuntoImagenAux1Path = "";
	private String sPuntoImagenAux2Path = "";
	private String sThumbPathAndroid = "";
	
	// MAPAS V2
	private GoogleMap mMap = null;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapa);
        
        markerUsuario = null;
        bCentrarVistaRuta = false;
        
        htPuntos = new Hashtable<String,OverlayItemPunto>();
        
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.activityMapaMapView)).getMap();
        
		// Por defecto se muestra la vista como mapa, aunque se puede cambiar a vista satelite desde el menu
        bVistaSatelite = false;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        
        // Otras opciones para el mapa
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        
        // Si se recibe un Intent es que se ha llegado desde RutasAccesiblesActivity, y por lo tanto se ha de mostrar una ruta
     	Intent intent = getIntent();
   		Bundle extras = intent.getExtras();
   		if (extras == null) {
   			Log.d("MapaActivity","No he recibido ningun Intent");
   		} else {
   			iRuta = extras.getInt(AbilidadeApplication.RutaAccesibleParametro, 0);
   			Log.d("MapaActivity","He recibido un Intent. La ruta es: " + iRuta);
   			
   			// Se usa una tarea asincrona para mostrar un ProgressDialog mientras se descargan la ruta y sus puntos
   			descargarRuta();
   			descargarPuntosRuta();
   			
   			bCentrarVistaRuta = true;
   		}
   		
   		//Listener para cuando se haga clic en un marker
   		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker marker) {
				
				OverlayItemPunto itemPunto;
				
				itemPunto = (OverlayItemPunto) htPuntos.get(marker.getId());
				
				// Se comprueba asi que el marker pulsado no sea el marker de usuario
				if (itemPunto != null) {
					// Se llama a la activity de Detalle del punto
					Intent intent = new Intent();
					intent.setClass(MapaActivity.this, DetallePuntoActivity.class);
					intent.putExtra(AbilidadeApplication.PUNTO_TITULO, itemPunto.getsTitulo());
					intent.putExtra(AbilidadeApplication.PUNTO_DESCRIPCION, itemPunto.getsDescripcion());
					intent.putExtra(AbilidadeApplication.PUNTO_DIRECCION, itemPunto.getsDireccion());
					intent.putExtra(AbilidadeApplication.PUNTO_IMAGEN_PRINCIPAL, itemPunto.getsImagenPrincipal());
					intent.putExtra(AbilidadeApplication.PUNTO_IMAGEN_AUX1, itemPunto.getsImagenAux1());
					intent.putExtra(AbilidadeApplication.PUNTO_IMAGEN_AUX2, itemPunto.getsImagenAux2());
					intent.putExtra(AbilidadeApplication.PUNTO_IMAGEN_PRINCIPAL_THUMB,itemPunto.getsImagenPrincipalThumb());
					
					// Y se arranca la pantalla de Detalle del punto
					startActivity(intent);
				}
				
			}
   			
   		});
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	// Geolocalizamos al usuario
    	comprobarSensores();
    	lm.requestLocationUpdates(provider, 0, 0, MapaActivity.this);
    	
    	// Se descargar los puntos solamente la primera vez que se acceda a la aplicacion
    	if (!bDescargarPuntos) {   
    		descargarPuntos();
    	}
    }
    
	@Override
	protected void onStop() {
		pDialogPuntos = null;
		
		super.onStop();
	}
	
    /* ***************** AQUI COMIENZA LA GESTION DEL MENU PRINCIPAL DE LA APLICACION ***************** */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu_mapa, menu);
    	
    	return true;
    }
    
    @Override 
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	Intent intent = new Intent();
    	
    	switch (item.getItemId()) {
    		case R.id.menuMapaAjustes:
    			// Se arranca la Activity de Ajustes
    			intent.setClass(MapaActivity.this, AjustesActivity.class);
				startActivityForResult(intent, 1);
				
				return true;
    		case R.id.menuMapaRegistrarPunto:
    			// Se muestra la pantalla de alta de punto
    			intent.setClass(MapaActivity.this, AltaPuntoActivity.class);
				startActivity(intent);
    			
    			return true;
    		case R.id.menuMapaRutasAccesibles:
    			// Se muestra la lista de rutas accesibles
				intent.setClass(MapaActivity.this, RutasAccesiblesActivity.class);
				startActivity(intent);
				
				// Se finaliza el Mapa, puesto que mas tarde se volver a el
				finish();
    			
    			return true;
    		case R.id.menuMapaAyuda:
    			// Lo que se hace es llamar al navegador con la pagina para la ayuda
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse("http://abilidade.eu/r/androidhelp/help_menu_principal.htm"));
				startActivity(i);
				
    			return true;
    			
    		case R.id.menuMapaVista:
    			if (bVistaSatelite) {
    				// Se vuelve al modo mapa
    				mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    				bVistaSatelite = false;
    			} else {
    				// Se pasa al modo satelite
    				mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    				bVistaSatelite = true;
    			}
    		default:
    			return false;
    	}
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        
        // Se sobreescribe este metodo para trabajar con el texto de la vista: ponerlo a "Vista Mapa" o "Vista Satélite"
        MenuItem menuItemVista = menu.findItem(R.id.menuMapaVista);
        
        if (bVistaSatelite) {
        	menuItemVista.setTitle(getString(R.string.vistaMapa));
        } else {
        	menuItemVista.setTitle(getString(R.string.vistaSatelite));
        }
        
        return super.onPrepareOptionsMenu(menu); 
    }
    
    /* ***************** ************************************************************ ***************** */
    
    
    private void comprobarSensores() {
    	lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	   	if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
	   		sensor = AbilidadeApplication.SENSOR_GPS;
	   	} 
	   	else if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
	   		sensor = AbilidadeApplication.SENSOR_NETWORK;
	   	} else {
	   		Toast.makeText(getApplicationContext(), getString(R.string.recomiendaUbicacion), Toast.LENGTH_LONG).show();
	   		startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	   	}
	   	
	   	// Una vez que tenemos algun sensor activado, localizamos al usuario
	   	if (sensor == AbilidadeApplication.SENSOR_GPS) {
	   		provider = LocationManager.GPS_PROVIDER;
	   	} else {
	   		provider = LocationManager.NETWORK_PROVIDER;
	   	}
    }
    
    private void descargarRuta() {
    	
    	// 0. Creacion de componentes
    	PolylineOptions lineas = new PolylineOptions();
    	lineas.width(6);
    	lineas.color(Color.BLUE);
    	
    	// 1. Se recibe el JSON para trocearlo y obtener las rutas
    	jsonRutas = recibirJSONRuta();
    	
    	// 2. Se trocea el JSON recibido
    	try {
			jObjectRuta = new JSONObject(jsonRutas);
			menuObjectRuta = jObjectRuta.getJSONArray(AbilidadeApplication.GESTION_PUNTOS_DATA);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	
    	// 3. Se va extrayendo la informacion del JSON
    	int totalElementos = menuObjectRuta.length();
		for (int i=0 ; i<totalElementos; i++) {
    		try {
				
    			dLatitudRuta   = menuObjectRuta.getJSONArray(i).getDouble(0);
				dLongitudRuta  = menuObjectRuta.getJSONArray(i).getDouble(1);
				
				Log.d("MapaActivity","latidud ruta  " + i + ": " + dLatitudRuta);
				Log.d("MapaActivity","longitud ruta " + i + ": " + dLongitudRuta);
				
				// El punto descargado se añade a la ruta que se va a mostrar
				lineas.add(new LatLng(dLatitudRuta, dLongitudRuta)); 
    			
    			// Centramos la vista en el comienzo de la ruta
    			if (i==0) {
    				mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(dLatitudRuta, dLongitudRuta)));
    				mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
    			}
    		} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		// 4. Por ultimo, se muestra la ruta en el mapa
		mMap.addPolyline(lineas);
    }
    
    private void descargarPuntos() {
    	
    	int iLongitudCadena;
    	
    	// 1. Se recibe el JSON para trocearlo y obtener los puntos
    	jsonPuntos = recibirJSON();
    	Log.d("MapaActivity",jsonPuntos.toString());
    	
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
				sPuntoTitulo      = menuObject.getJSONArray(i).getString(0);
				sPuntoDireccion   = menuObject.getJSONArray(i).getString(1);
				sPuntoDescripcion = menuObject.getJSONArray(i).getString(2);
				
				sPuntoCorreoE     = menuObject.getJSONArray(i).getString(3);
				dPuntoLatitud     = menuObject.getJSONArray(i).getDouble(4);
				dPuntoLongitud    = menuObject.getJSONArray(i).getDouble(5);
				iPuntoEstado      = menuObject.getJSONArray(i).getInt(6);
				
				iLongitudCadena = menuObject.getJSONArray(i).length();
				Log.d("MapaActivity","Total: " + iLongitudCadena);
				
				sPuntoImagenPrincipalThumbPath = menuObject.getJSONArray(i).getString(7);
				sPuntoImagenPrincipalPath      = menuObject.getJSONArray(i).getString(8);
				
				// Si la longitud es al menos 10, es que se recibe imagen Aux1 del punto
				if (iLongitudCadena >= 10) {
				
					sPuntoImagenAux1Path           = menuObject.getJSONArray(i).getString(9);
					
					// Si la longitud es 11, es que también se recibe imagen Aux2 del punto
					if (iLongitudCadena == 11) {
						sPuntoImagenAux2Path           = menuObject.getJSONArray(i).getString(10);	
					}
				} 
					
				Log.d("MapaActivity","Titulo:                "+sPuntoTitulo);
				Log.d("MapaActivity","Direccion:             "+sPuntoDireccion);
				Log.d("MapaActivity","Descripcion:           "+sPuntoDescripcion);
				Log.d("MapaActivity","Correo e:              "+sPuntoCorreoE);
				Log.d("MapaActivity","Latitud:               "+dPuntoLatitud);
				Log.d("MapaActivity","Longitud:              "+dPuntoLongitud);
				Log.d("MapaActivity","Estado:                "+iPuntoEstado);
				
				Log.d("MapaActivity","Path imagen principal:       "+sPuntoImagenPrincipalPath);
				Log.d("MapaActivity","Path imagen principal thumb: "+sPuntoImagenPrincipalThumbPath);
				Log.d("MapaActivity","Path imagen aux1:            "+sPuntoImagenAux1Path);
				Log.d("MapaActivity","Path imagen aux2:            "+sPuntoImagenAux2Path);
				
				// 4. Se recibe el thumbnail de la imagen principal del punto desde el servidor para mostrarla por pantalla
				recibirImagenPrincipalThumbPunto();
				
				// 5. El punto descargado se añade a la lista de puntos que se muestran en el mapa
				Marker marker;
				OverlayItemPunto itemPunto;
				
				// Si el estado del punto es 0, el punto es inaccesible. Si es 1, el punto es accesible
				if (iPuntoEstado == 0) {
					marker = mMap.addMarker(new MarkerOptions()
		                    .position(new LatLng(dPuntoLatitud, dPuntoLongitud))
		                    .title(sPuntoTitulo)
		                    .snippet(sPuntoDireccion)
		                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_red)));
				} else {
					marker = mMap.addMarker(new MarkerOptions()
		                    .position(new LatLng(dPuntoLatitud, dPuntoLongitud))
		                    .title(sPuntoTitulo)
		                    .snippet(sPuntoDireccion)
		                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_green)));
				}
				
				itemPunto = new OverlayItemPunto(sPuntoTitulo, sPuntoDireccion, sPuntoDescripcion, sPuntoImagenPrincipalPath, sPuntoImagenAux1Path, sPuntoImagenAux2Path, sThumbPathAndroid);
				htPuntos.put(marker.getId(), itemPunto);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
    }
    
    private void descargarPuntosRuta() {
    	
    	int iLongitudCadena;
    	
    	// 1. Se recibe el JSON para trocearlo y obtener los puntos
    	jsonPuntosRuta = recibirJSONPuntosRuta();
    	
    	// 2. Se trocea el JSON recibido
    	try {
			jObjectPuntosRuta = new JSONObject(jsonPuntosRuta);
			menuObjectPuntosRuta = jObjectPuntosRuta.getJSONArray(AbilidadeApplication.GESTION_PUNTOS_DATA);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	
    	// 3. Se va extrayendo la informacion del JSON
    	int totalElementos = menuObjectPuntosRuta.length();
		for (int i=0 ; i<totalElementos; i++) {
    		try {
				sPuntoTitulo      = menuObjectPuntosRuta.getJSONArray(i).getString(0);
				sPuntoDireccion   = menuObjectPuntosRuta.getJSONArray(i).getString(1);
				sPuntoDescripcion = menuObjectPuntosRuta.getJSONArray(i).getString(2);
				
				sPuntoCorreoE     = menuObjectPuntosRuta.getJSONArray(i).getString(3);
				dPuntoLatitud     = menuObjectPuntosRuta.getJSONArray(i).getDouble(4);
				dPuntoLongitud    = menuObjectPuntosRuta.getJSONArray(i).getDouble(5);
				iPuntoEstado      = menuObjectPuntosRuta.getJSONArray(i).getInt(6);
				
				iLongitudCadena = menuObjectPuntosRuta.getJSONArray(i).length();
				Log.d("MapaActivity","Total: " + iLongitudCadena);
				
				sPuntoImagenPrincipalThumbPath = menuObjectPuntosRuta.getJSONArray(i).getString(7);
				sPuntoImagenPrincipalPath      = menuObjectPuntosRuta.getJSONArray(i).getString(8);
				
				// Si la longitud es al menos 10, es que se recibe imagen Aux1 del punto
				if (iLongitudCadena >= 10) {
					sPuntoImagenAux1Path           = menuObjectPuntosRuta.getJSONArray(i).getString(9);
					// Si la longitud es 11, es que también se recibe imagen Aux2 del punto
					if (iLongitudCadena == 11) {
						sPuntoImagenAux2Path       = menuObjectPuntosRuta.getJSONArray(i).getString(10);	
					}
				} 
					
				Log.d("MapaActivity","Titulo:                "+sPuntoTitulo);
				Log.d("MapaActivity","Direccion:             "+sPuntoDireccion);
				Log.d("MapaActivity","Descripcion:           "+sPuntoDescripcion);
				Log.d("MapaActivity","Correo e:              "+sPuntoCorreoE);
				Log.d("MapaActivity","Latitud:               "+dPuntoLatitud);
				Log.d("MapaActivity","Longitud:              "+dPuntoLongitud);
				Log.d("MapaActivity","Estado:                "+iPuntoEstado);
				
				Log.d("MapaActivity","Path imagen principal:       "+sPuntoImagenPrincipalPath);
				Log.d("MapaActivity","Path imagen principal thumb: "+sPuntoImagenPrincipalThumbPath);
				Log.d("MapaActivity","Path imagen aux1:            "+sPuntoImagenAux1Path);
				Log.d("MapaActivity","Path imagen aux2:            "+sPuntoImagenAux2Path);
				
				// 4. Se recibe el thumbnail de la imagen principal del punto desde el servidor para mostrarla por pantalla
				recibirImagenPrincipalThumbPunto();
				
				// 5. El punto descargado se añade a la lista de puntos que se muestran en el mapa
				Marker marker = mMap.addMarker(new MarkerOptions()
				                .position(new LatLng(dPuntoLatitud, dPuntoLongitud))
				                .title(sPuntoTitulo)
				                .snippet(sPuntoDireccion)
				                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_blue)));
				
				OverlayItemPunto itemPunto = new OverlayItemPunto(sPuntoTitulo, sPuntoDireccion, sPuntoDescripcion, sPuntoImagenPrincipalPath, sPuntoImagenAux1Path, sPuntoImagenAux2Path, sThumbPathAndroid);
				htPuntos.put(marker.getId(), itemPunto);
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
    }
    
    private void recibirImagenPrincipalThumbPunto() {
    	
    	String sState = Environment.getExternalStorageState();
    	File img;
    	String sRuta;
    	
    	// Se comprueba si esta lista la tarjeta SD externa o no. 
    	if (sState.equals(Environment.MEDIA_MOUNTED)) {
    		sRuta = Environment.getExternalStorageDirectory().getPath() + "/app/tmp/abilidade/" + sPuntoImagenPrincipalThumbPath;
    		img = new File(sRuta);
    		//img.mkdirs();
    	} else {
    		File dirImages = getApplicationContext().getDir("abilidade", Context.MODE_PRIVATE);
    	    img = new File(dirImages, sPuntoImagenPrincipalThumbPath);
    	}
    	
    	// only download new images
        if (!img.exists()) {
	    	try {
	    		String ruta = AbilidadeApplication.RUTA_IMAGEN+sPuntoImagenPrincipalThumbPath;
	    		
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
	   			Log.w("MapaActivity","MalformedURLException");
	   		} catch (IOException e) {
	   			img = null;
	   			Log.w("MapaActivity","IOException");
	   		}
        }
        
        sThumbPathAndroid = img.getAbsolutePath();
        
        Log.d("MapaActivity","He pasado correctamente la recepcion de la imagen");
        
        
    }
    
    private String recibirJSONRuta() {
    	StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet("http://abilidade.eu/r/ruta" + iRuta + ".php");
		
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
				Log.e("MapaActivity", "Fallo al descargar el JSON de puntos");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
    }
    
    private String recibirJSONPuntosRuta() {
    	StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet("http://abilidade.eu/r/puntosruta" + iRuta + ".php");
		
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
				Log.e("MapaActivity", "Fallo al descargar el JSON de puntos");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
    }
    
    private String recibirJSON() {
    	StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet("http://abilidade.eu/r/puntosv2.php");
		
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
				Log.e("MapaActivity", "Fallo al descargar el JSON de puntos");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
    }

	@Override
	public void onLocationChanged(Location location) {
		
		// Actualizamos las coordenadas atributo
		dLatitud = location.getLatitude();
		dLongitud = location.getLongitude();
		
		// Borramos el marker previo del usuario
		if (markerUsuario == null) {
			
			// el marketUsuario no estaba configurado, por lo que es la primera vez que se localiza al usuario.
			// Situamos el mapa en la posicion del usuario
			if (!bCentrarVistaRuta) {
				mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(dLatitud, dLongitud)));
				mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
			}
			
		} else {
			markerUsuario.remove();
		}
		
		// Y mostramos en el mapa un nuevo marker con la localizacion del usuario
		markerUsuario = mMap.addMarker(new MarkerOptions()     
					        .position(new LatLng(dLatitud, dLongitud))
					        .title("Usuario"));
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void onPause() {
		lm.removeUpdates(this);
		super.onPause();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 1) {

		     if(resultCode == RESULT_CANCELED){      
		    	 Log.d("MapaActivity","He vuelto de AjustesActivity porque el usuario le dio a atras");
		     }
		     else {
		    	 Log.d("MapaActivity","He vuelto de AjustesActivity porque el usuario le dio a cerrar sesion");
		    	 finish();
		     }
		  }
	}
	
	/*		CLASE ASYNCTASK
	 * Se usa esta clase para poder mostrar el dialogo de progreso mientras se envian y obtienen los datos.     
	 */

	class asynclogin extends AsyncTask< String, String, String > {
		 
		protected void onPreExecute() {
	    	//para el progress dialog
	        pDialogPuntos = new ProgressDialog(MapaActivity.this);
	        pDialogPuntos.setMessage(getString(R.string.descargandoPuntos));
	        pDialogPuntos.setIndeterminate(false);
	        pDialogPuntos.setCancelable(false);
	        pDialogPuntos.show();
	    }

		protected String doInBackground(String... params) {
			// Se llama al metodo para descargar los puntos
			descargarPuntos();
			
			return AbilidadeApplication.RETORNO_OK;
		}
	   
		protected void onPostExecute(String result) {
			
			// La descarga de puntos ha finalizado, por lo que se oculta el ProgressDialog
			if (pDialogPuntos != null) {
				pDialogPuntos.dismiss();
			}
	     }
	}
	
	class asyncloginRuta extends AsyncTask< String, String, String > {
		 
		protected void onPreExecute() {
	    	//para el progress dialog
	        pDialogRuta = new ProgressDialog(MapaActivity.this);
	        pDialogRuta.setMessage(getString(R.string.descargandoRutas));
	        pDialogRuta.setIndeterminate(false);
	        pDialogRuta.setCancelable(false);
	        pDialogRuta.show();
	    }

		protected String doInBackground(String... params) {
			// Se llama al metodo para descargar la ruta
			descargarRuta();
			
			// Y ahora se descargan los puntos de la ruta
			descargarPuntosRuta();
			
			return AbilidadeApplication.RETORNO_OK;
		}
	   
		protected void onPostExecute(String result) {
			
			// La descarga de la ruta ha finalizado, por lo que se oculta el ProgressDialog
			pDialogRuta.dismiss();
	     }
	}
}