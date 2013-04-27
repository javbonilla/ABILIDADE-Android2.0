package org.abilidade.map_components;

import android.graphics.Bitmap;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class OverlayItemPunto extends OverlayItem {

	// Definicion de atributos de la clase
	private String sTitulo = null;
	private String sDireccion = null;
	private String sDescripcion = null;
	
	private String sImagenPrincipal = null;
	private String sImagenAux1 = null;
	private String sImagenAux2 = null;
	
	private String  sImagenPrincipalThumb = null;
	
	// Definicion de metodos
	public OverlayItemPunto(GeoPoint point, String title, String snippet, String direccion, String descripcion, String imagenPrincipal, String imagenAux1, String imagenAux2, String imagenThumb) {
		super(point, title, snippet);
		this.sTitulo      = title;
		this.sDireccion   = direccion;
		this.sDescripcion = descripcion;
		this.sImagenPrincipal = imagenPrincipal;
		this.sImagenAux1 = imagenAux1;
		this.sImagenAux2 = imagenAux2;
		this.sImagenPrincipalThumb = imagenThumb;
	}

	public String getsTitulo() {
		return sTitulo;
	}

	public void setsTitulo(String sTitulo) {
		this.sTitulo = sTitulo;
	}

	public String getsDireccion() {
		return sDireccion;
	}

	public void setsDireccion(String sDireccion) {
		this.sDireccion = sDireccion;
	}

	public String getsDescripcion() {
		return sDescripcion;
	}

	public void setsDescripcion(String sDescripcion) {
		this.sDescripcion = sDescripcion;
	}

	public String getsImagenPrincipal() {
		return sImagenPrincipal;
	}

	public void setsImagenPrincipal(String sImagenPrincipal) {
		this.sImagenPrincipal = sImagenPrincipal;
	}

	public String getsImagenAux1() {
		return sImagenAux1;
	}

	public void setsImagenAux1(String sImagenAux1) {
		this.sImagenAux1 = sImagenAux1;
	}

	public String getsImagenAux2() {
		return sImagenAux2;
	}

	public void setsImagenAux2(String sImagenAux2) {
		this.sImagenAux2 = sImagenAux2;
	}

	public String getsImagenPrincipalThumb() {
		return sImagenPrincipalThumb;
	}

	public void setsImagenPrincipalThumb(String sImagenPrincipalThumb) {
		this.sImagenPrincipalThumb = sImagenPrincipalThumb;
	}
}
