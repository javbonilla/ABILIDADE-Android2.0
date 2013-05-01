package org.abilidadev2.application;

public class DireccionCompleta {
	
	// Definicion de atributos
	private String direccion;
	private String localidad;
	private String provincia;
	
	// Constructor de la clase
	public DireccionCompleta(String direccion, String localidad, String provincia) {
		this.direccion = direccion;
		this.localidad = localidad;
		this.provincia = provincia;
	}

	public DireccionCompleta() {
		direccion = "";
		localidad = "";
		provincia = "";
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public String getLocalidad() {
		return localidad;
	}

	public void setLocalidad(String localidad) {
		this.localidad = localidad;
	}

	public String getProvincia() {
		return provincia;
	}

	public void setProvincia(String provincia) {
		this.provincia = provincia;
	}
}
