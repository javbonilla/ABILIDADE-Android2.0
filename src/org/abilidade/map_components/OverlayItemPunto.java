package org.abilidade.map_components;

import android.graphics.Bitmap;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class OverlayItemPunto extends OverlayItem {

	// Definicion de atributos de la clase
	private long id = 0;
	private String direccion = null;
	private Bitmap imagen = null;
	
	// Definicion de metodos
	public OverlayItemPunto(GeoPoint point, String title, String snippet, long id, String direccion, Bitmap imagen) {
		super(point, title, snippet);
		this.direccion = direccion;
		this.imagen = imagen;
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public Bitmap getImagen() {
		return imagen;
	}

	public void setImagen(Bitmap imagen) {
		this.imagen = imagen;
	}
}
