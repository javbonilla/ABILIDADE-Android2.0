package org.abilidade.mapa;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import org.abilidade.R;
import org.abilidade.activities.AjustesActivity;
import org.abilidade.activities.AltaPuntoActivity;
import org.abilidade.activities.AyudaMapa;
import org.abilidade.activities.RutasAccesiblesActivity;
import org.abilidade.application.AbilidadeApplication;
import org.abilidade.map_components.OverlayItemPunto;
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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml.Encoding;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import greendroid.app.GDMapActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;

public class MapaActivity extends GDMapActivity implements LocationListener {
	
	// Atributos relativos al mapa y su gestion
	private MapView mapView = null;
	private MapController mapController = null;
	private List<Overlay> mapOverlays;
	private MyItemizedOverlay itemizedOverlayPuntosAccesibles;
	private MyItemizedOverlay itemizedOverlayPuntosInaccesibles;
	private MyItemizedOverlayUsuario itemizedOverlayUsuario;
	
	// Atributos relacionados con la localizacion del usuario
	private LocationManager lm;
	private int sensor;
	private String provider;
	private double latitud;
	private double longitud;
	
	// Atributos relacionados con la descarga de puntos desde el servidor
	private String jsonPuntos;
	private JSONObject jObject;
	private JSONArray menuObject;
	
	// Definicion de los campos de un punto
	private String sPuntoTitulo = "";
	private String sPuntoDireccion = "";
	private String sPuntoLocalidad = "";
	private String sPuntoProvincia = "";
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
	
	private Bitmap bmImagenPrincipalThumb = null;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarContentView(R.layout.mapa);
        
        // Elementos en la barra de accion --> Solamente un boton de ayuda
     	addActionBarItem(Type.Add, R.id.action_bar_add);
     	addActionBarItem(Type.Eye, R.id.action_bar_eye);
        
        // Localizacion de los elementos en pantalla
        mapView = (MapView) findViewById(R.id.activityMapaMapView);
        
        // Cargamos el mapa y sus controles
        mapView.displayZoomControls(true);
        mapView.setBuiltInZoomControls(true);
        
        mapController = mapView.getController();
        mapController.setZoom(17);
        
        mapOverlays = mapView.getOverlays();
        
        // Esto lo mejor seria preguntarlo como parametro, o dejarlo en el menu de opciones
        mapView.setSatellite(false);
        
        // Iniciamos el objeto itemizedOverlayPuntosAccesibles y el marker que se usara para señalar los puntos
        itemizedOverlayPuntosAccesibles = new MyItemizedOverlay(getResources().getDrawable(R.drawable.marker_green), mapView);
        
        // Iniciamos el objeto itemizedOverlayPuntosInaccesibles y el marker que se usara para señalar los puntos
        itemizedOverlayPuntosInaccesibles = new MyItemizedOverlay(getResources().getDrawable(R.drawable.marker_red), mapView);
        
        // Iniciamos el objeto itemizedOverlayUsuario y el marker que se usara para su ubicacion
        itemizedOverlayUsuario = new MyItemizedOverlayUsuario(MapaActivity.this, getResources().getDrawable(R.drawable.usermarker));
    }
    
    /**
     * onHandleActionBarItemClick: listener para manejar el boton del ActionBar sobre el que hace clic el usuario
     * @param item: item sobre el que hizo clic el usuario
     * @param pos: posicion de item
     */
    @Override
    public boolean onHandleActionBarItemClick(ActionBarItem item, int pos) {
        if (item.getItemId() == R.id.action_bar_add) {
        	// Conectamos con la Activity de alta de punto inaccesible
        	Intent intent = new Intent();
        	
        	intent.setClass(MapaActivity.this, AltaPuntoActivity.class);
        	intent.putExtra(AbilidadeApplication.altaPuntoParametroLatitud, latitud);
        	intent.putExtra(AbilidadeApplication.altaPuntoParametroLongitud, longitud);
        	
        	startActivity(intent);
		} else if (item.getItemId() == R.id.action_bar_eye) {
			// Se descargan y muestran los puntos
			Log.d("MapaActivity","Comienza la descarga de puntos");
			descargarPuntos();
		} 
        return super.onHandleActionBarItemClick(item, pos);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	// Geolocalizamos al usuario
    	comprobarSensores();
    	lm.requestLocationUpdates(provider, 0, 0, MapaActivity.this);
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
				startActivity(intent);
				
				// Se finaliza la Activity del mapa, para que no se vuelva a ella
				finish();
				
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
				
    			return true;
    		case R.id.menuMapaAyuda:
    			// Se muestra la ayuda de la aplicacion
    			//TODO Aqui habra que incluir la ayuda COMPLETA de la aplicacion
    			intent.setClass(MapaActivity.this, AyudaMapa.class);
    			startActivity(intent);
				
    			return true;
    		default:
    			return false;
    	}
    }
    
    /* ***************** ************************************************************ ***************** */
    
    
    private void comprobarSensores() {
    	lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	   	if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
	   		Toast.makeText(getApplicationContext(), getString(R.string.ubicacionGPS), Toast.LENGTH_SHORT).show();
	   		sensor = AbilidadeApplication.SENSOR_GPS;
	   	} 
	   	else if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
	   		Toast.makeText(getApplicationContext(), getString(R.string.ubicacionRed), Toast.LENGTH_SHORT).show();
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
    
    private void descargarPuntos() {
    	
    	GeoPoint point = null;
    	int iLatitud;
    	int iLongitud; 
    	OverlayItemPunto overlayItem;
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
				iLatitud = (int)(dPuntoLatitud * 1E6);
				iLongitud = (int)(dPuntoLongitud * 1E6);
    			
    			point = new GeoPoint(iLatitud, iLongitud);
				overlayItem = new OverlayItemPunto(point, sPuntoTitulo, "", sPuntoDireccion, sPuntoDescripcion, sPuntoImagenPrincipalPath, sPuntoImagenAux1Path, sPuntoImagenAux2Path, sThumbPathAndroid);
				
				// Si el estado del punto es 0, el punto es inaccesible. Si es 1, el punto es accesible
				if (iPuntoEstado == 0) {
					itemizedOverlayPuntosInaccesibles.addOverlay(overlayItem);
					mapOverlays.add(itemizedOverlayPuntosInaccesibles);
				} else {
					itemizedOverlayPuntosAccesibles.addOverlay(overlayItem);
					mapOverlays.add(itemizedOverlayPuntosAccesibles);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
    }
    
    private void recibirImagenPrincipalThumbPunto() {
    	File img = new File("/sdcard/app/tmp/abilidade/" + sPuntoImagenPrincipalThumbPath);

        // Create directories
        new File("/sdcard/app/tmp/abilidade").mkdirs();

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
	   		} catch (IOException e) {
	   			img = null;
	   		}
        }
        
        // Se obtiene la imagen principal y se escala a un tamaño adecuado
        sThumbPathAndroid = img.getAbsolutePath();
        bmImagenPrincipalThumb = BitmapFactory.decodeFile(sThumbPathAndroid);
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
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLocationChanged(Location location) {
		
		// Actualizamos las coordenadas atributo
		latitud = location.getLatitude();
		longitud = location.getLongitude();
		
		// Actualizamos el overlay que muestra la ubicacion del usuario
		itemizedOverlayUsuario.addOverlay(latitud, longitud);
		mapOverlays.add(itemizedOverlayUsuario);
		
		// Y centramos el mapa en la posicion del usuario
		GeoPoint point = new GeoPoint((int)(latitud*1E6), (int)(longitud*1E6));
		mapController.animateTo(point);
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
}