package org.abilidade.mapa;

import java.util.List;

import org.abilidade.R;
import org.abilidade.activities.AjustesActivity;
import org.abilidade.activities.AltaPuntoActivity;
import org.abilidade.activities.AyudaMapa;
import org.abilidade.activities.RutasAccesiblesActivity;
import org.abilidade.application.AbilidadeApplication;
import org.abilidade.db.DatabaseCommons;
import org.abilidade.db.PuntoProvider;
import org.abilidade.map_components.OverlayItemPunto;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
	private MyItemizedOverlay itemizedOverlayPuntos;
	private MyItemizedOverlayUsuario itemizedOverlayUsuario;
	
	// Atributos relacionados con la localizacion del usuario
	private LocationManager lm;
	private int sensor;
	private String provider;
	private double latitud;
	private double longitud;
	
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
        
        // Iniciamos el objeto itemizedOverlayPuntos y el marker que se usara para señalar los puntos
        itemizedOverlayPuntos = new MyItemizedOverlay(getResources().getDrawable(R.drawable.marker), mapView);
        
        // Iniciamos el objeto itemizedOverlayUsuario y el marker que se usara para su ubicacion
        itemizedOverlayUsuario = new MyItemizedOverlayUsuario(MapaActivity.this, getResources().getDrawable(R.drawable.usermarker));
		
        // Cargamos todos los puntos inaccesibles desde la Base de Datos al mapa
        cargarPuntos();
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
			// Se muestran los puntos
			Toast.makeText(getApplicationContext(), "Tengo que mostrar los puntos", Toast.LENGTH_LONG).show();
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
    
    private void cargarPuntos() {
    	GeoPoint point = null;
    	Bitmap imagen = null;
    	int latitud;
    	int longitud; 
    	OverlayItemPunto overlayItem;
    	
    	// Obtenemos los puntos desde la BD
    	final String[] columnas = new String[] { DatabaseCommons.Punto._ID, DatabaseCommons.Punto.TITULO, DatabaseCommons.Punto.DIRECCION,
    			DatabaseCommons.Punto.LATITUD, DatabaseCommons.Punto.LONGITUD, DatabaseCommons.Punto.IMAGEN_PRINCIPAL };
    	Uri uri = PuntoProvider.CONTENT_URI;
    	String selection = DatabaseCommons.Punto.ESTADO + " > ?";
    	String[] projection = new String[] { ""+AbilidadeApplication.ESTADO_CERRADO_RESUELTO };
    	Cursor cursor = managedQuery(uri, columnas, selection, projection, DatabaseCommons.Punto.DEFAULT_SORT_ORDER);
    	
    	// Comprobamos si ha habido cambios para recargar el cursor
    	cursor.setNotificationUri(getContentResolver(), uri);
    	
    	// La actividad se encargara de manejar el cursor segun su ciclo de vida
    	startManagingCursor(cursor);
    	
    	// Cada fila recuperada del cursor se vuelca al mapa. Si no hay ningun punto se informara al usuario, al igual que si ha habido error
    	if (cursor != null) {
    		if (cursor.moveToFirst()) {
    			do {
    				// Recuperamos los campos y los almacenamos en un OverlayItemPunto, objeto que se cargara al mapa
    				latitud = (int)(cursor.getDouble(3) * 1E6);
    				Log.w("MapaActivity",""+latitud);
        			longitud = (int)(cursor.getDouble(4) * 1E6);
        			Log.w("MapaActivity",""+longitud);
        			point = new GeoPoint(latitud, longitud);
        			imagen = AbilidadeApplication.descodificarImagen(cursor.getString(5));
        			overlayItem = new OverlayItemPunto(point, cursor.getString(1), "", cursor.getLong(0), cursor.getString(2), imagen);
        			
        			itemizedOverlayPuntos.addOverlay(overlayItem);
    			} while(cursor.moveToNext());
    			
    			// Por ultimo, añadimos el itemizedOverlayPuntos al mapOverlay
    	    	mapOverlays.add(itemizedOverlayPuntos);
    		} else {
    			Toast.makeText(MapaActivity.this, getString(R.string.mapaActivityCursorVacio), Toast.LENGTH_LONG).show();
    		}
    	} 
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