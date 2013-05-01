package org.abilidadev2.mapa;

import java.util.ArrayList;

import org.abilidadev2.application.AbilidadeApplication;
import org.abilidadev2.application.DireccionCompleta;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class MyItemizedOverlayUsuario extends ItemizedOverlay<OverlayItem> {
	
	// Definicion de atributos
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem> ();
	private Context context;
	private double latitud;
	private double longitud;
	
	
	// Definicion de metodos
	public MyItemizedOverlayUsuario(Context context, Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		this.context = context;
	}
	
	public void addOverlay(double latitud, double longitud) {
		// Creamos dentro el Overlay, borramos el anterior e insertamos el nuevo
		GeoPoint point = new GeoPoint((int)(latitud * 1E6), (int)(longitud * 1E6));
		OverlayItem overlayItem = new OverlayItem(point, "", "");
		
		clear();
		mOverlays.add(overlayItem);
		populate();
		
		// Guardamos las coordenadas pasadas por parametro por si el usuario hace tap sobre el marker
		this.latitud = latitud;
		this.longitud = longitud;
	}
	
	public void clear() {
		mOverlays.clear();
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return(mOverlays.get(i));
	}

	@Override
	public int size() {
		return(mOverlays.size());
	}
	
	@Override
	protected boolean onTap(int index) {
		// Independientemente de index, mostramos la ubicacion del usuario
		DireccionCompleta direccionCompleta = AbilidadeApplication.obtenerDireccion(context, latitud, longitud);
		Toast.makeText(context, direccionCompleta.getDireccion(), Toast.LENGTH_LONG).show();
		
		return(true);
	}

}
