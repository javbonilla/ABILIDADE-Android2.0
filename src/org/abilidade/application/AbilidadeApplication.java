package org.abilidade.application;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.abilidade.R;
import org.abilidade.activities.AltaPuntoActivity;
import org.abilidade.activities.ZoomImagenActivity;
import org.abilidade.base64.Base64Coder;
import org.abilidade.mapa.MapaActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.widget.Toast;

import greendroid.app.GDApplication;

@SuppressLint("NewApi")
public class AbilidadeApplication extends GDApplication {
	
	// Definiciones de constantes de uso comun en el resto de clases 
	public static final String ZoomImagenActivityImagenPath = "imagenPath";
	public static final String ZoomImagenActivityImagenCod = "imagenCod";
 
	public static final float defaultImageWidth = 800;
	public static final float defaultImageHeight = 600;
	public static final int calidadImagen = 90; // Con una calidad de 90 podemos bajar de 300Kb a 90Kb el peso de la imagen

	public static final String altaPuntoParametroLatitud = "latitud";
	public static final String altaPuntoParametroLongitud = "longitud";
	
	public static final String RutaAccesibleParametro = "rutaAccesible";
	
	// Estados por los que puede pasar un punto
	public static final int ESTADO_CERRADO_CANCELADO = 0;
	public static final int ESTADO_CERRADO_RESUELTO = 1;
	public static final int ESTADO_CERRADO_NO_RESUELTO = 2;
	public static final int ESTADO_ABIERTO = 3;
	public static final int ESTADO_EN_ESTUDIO = 4;
	public static final int ESTADO_EN_RESOLUCION = 5;
	
	// Sincronizacion de los puntos en la BD local del terminal
	public static final int PUNTO_NO_SINCRONIZADO = 0;
	public static final int PUNTO_SINCRONIZADO = 1;
	
	// Constantes utiles para la geolocalizacion del usuario
	public static final int SENSOR_GPS = 0;
	public static final int SENSOR_NETWORK = 1;
	
	// Constantes utiles para ProgressDialog
	public static final String RETORNO_OK = "ok";
	public static final String RETORNO_KO = "ko";
	
	public static final String RETORNO_KO_USUARIO = "ko_usuario";
	public static final String RETORNO_KO_PASSWORD = "ko_password";
	public static final String RETORNO_KO_USUACTIVO = "ko_usuactivo";
	public static final String RETORNO_KO_ERROR = "ko_error";
	
	// Longitud de la password
	public static final int LONGITUD_PASSWORD = 4;
	
	// SharedPreferences de la aplicacion
	public static final String SHARED_PREFERENCES = "AbilidadeShPf";
	public static final String SHPF_PRIMERA_VEZ   = "AbilidadeShPfPrimeraVez";
	public static final String SHPF_USUARIO       = "AbilidadeShPfUsuario";
	public static final String SHPF_LOGIN         = "AbilidadeShPfLogin";
	
	// Retornos de la pantalla de bienvenida
	public static final String RETORNO_BIENVENIDA_ACCEDER = "RetornoBienvenidaAcceder";
	public static final String RETORNO_BIENVENIDA_MAIN = "RetornoBienvenidaMain";
	
	// Gestion de puntos con el servidor
	public static final String GESTION_PUNTOS_DATA = "data";
	
	// Informacion a pasar sobre un punto
	public static final String PUNTO_TITULO      = "titulo";
	public static final String PUNTO_DIRECCION   = "direccion";
	public static final String PUNTO_DESCRIPCION = "descripcion";
	public static final String PUNTO_IMAGEN_PRINCIPAL = "ImagenPrincipal";
	public static final String PUNTO_IMAGEN_AUX1 = "ImagenAux1";
	public static final String PUNTO_IMAGEN_AUX2 = "ImagenAux2";
	
	// Ruta de una imagen
	public static final String RUTA_IMAGEN = "http://abilidade.eu/r/imgpoint/";
	
	@Override
	public Class<?> getHomeActivityClass() {
		return MapaActivity.class;
	}
	
	/**
	 * comprobarOrientacionImagen: Metodo para comprobar la orientacion de la imagen.
	 * @param filename: Ruta de la imagen
	 * @return int: orientacion de la imagen
	 */
	public static int comprobarOrientacionImagen(String filename) {
		// Comprobamos la orientacion de la imagen
		ExifInterface exif;
		try {
			exif = new ExifInterface(filename);
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
			
			return orientation;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * rotarImagen: Si la orientacion de la imagen no coincide con la de pantalla, se rotara para que se muestre correctamente
	 * @param filename: ruta de la imagen
	 * @param imagen: imagen 
	 * @return Bitmap: imagen rotada si es necesario, o la original en caso contrario
	 */
	public static Bitmap rotarImagen(String filename, Bitmap imagen) {
		int orientacion = comprobarOrientacionImagen(filename);
			
		switch (orientacion) {
			case 6: // Hay que rotar la imagen 90 grados
				Matrix matrix = new Matrix();
				matrix.postRotate(90);
				Bitmap rotatedBitmap = Bitmap.createBitmap(imagen, 0, 0, imagen.getWidth(), imagen.getHeight(), matrix, true);
				return rotatedBitmap;
			default:
				return imagen;
		}
	}	
	
	/**
	 * convertirImagen: Este metodo se encarga de transformar una imagen de punto seleccionada por el usuario
	 *                  de Bitmap a char[] 
	 * @param imagenPrincipal2
	 * @return String: la imagen convertida a texto gracias al encoder Base64
	 */
	public static String convertirImagen(Bitmap imagen) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		imagen.compress(Bitmap.CompressFormat.JPEG, AbilidadeApplication.calidadImagen, baos); 
		byte[] imagenBytes = baos.toByteArray();
		
		char[] encodedImagen = Base64Coder.encode(imagenBytes);
		return(new String(encodedImagen));	
	}
	
	/**
	 * descodificarImagen: Este metodo transforma una imagen codificada en String (gracias a Base64) en un objeto Bitmap
	 * @param encodedImagen: imagen codificada en texto
	 * @return Bitmap: objeto Bitmap con la imagen
	 */
	public static Bitmap descodificarImagen(String encodedImagen) {
		byte[] decodedString = Base64Coder.decode(encodedImagen);
		Bitmap imagen = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length); 
		return (imagen);
	}
	
	/**
	 * obtenerDireccion: este metodo se encarga de obtener la direccion a partir de las coordenadas (latitud, longitud) pasadas por parametro
	 * @param context: contexto sobre el que debe trabajar el objeto GeoCoder
	 * @param latitud: coordenada latitud
	 * @param longitud: coordenada longitud 
	 * @return DireccionCompleta: objeto que contiene la direccion completa obtenida (direccion, localidad y provincia)
	 */
	public static DireccionCompleta obtenerDireccion(Context context, double latitud, double longitud) {
		DireccionCompleta direccionCompleta = new DireccionCompleta();
		List<Address> addressesResult;
		Geocoder geocoder = new Geocoder(context);
		String direccion;
		
		try {
			addressesResult = geocoder.getFromLocation(latitud, longitud, 5);
			if (addressesResult!=null && addressesResult.size()>0) {
    			direccion = addressesResult.get(0).getAddressLine(0) + ", " + 
						addressesResult.get(0).getAddressLine(1) + ", " + 
						addressesResult.get(0).getAddressLine(2);
    			
    			direccionCompleta.setDireccion(direccion);
    			direccionCompleta.setLocalidad(addressesResult.get(0).getLocality());
    			direccionCompleta.setProvincia(addressesResult.get(0).getSubAdminArea());
    			
    		} 
    		else {
    			Dialog foundNothingDlg = new AlertDialog.Builder(context)
    				.setIcon(0)
    				.setTitle("Fallo al ubicar al usuario")
    				.setPositiveButton("Ok",null)
    				.setMessage("Ubicación no encontrada...")
    				.create();
    			foundNothingDlg.show();
    		}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return direccionCompleta;
	}
	
	/**
	 * verFoto: Este metodo se encarga de llamar a una Activity que mostrara una foto del punto a pantalla completa
	 * @param packageContext: contexto de la clase que llamara a ZoomImagenActivity
	 * @param imagenPath: Path de la imagen a mostrar en la Activity
	 * @param imagenCod: imagen codificada
	 */
	public static void verFoto(Context packageContext, String imagenPath, String imagenCod) {
		if (imagenPath.length() == 0 && imagenCod == null) {
			Toast.makeText(packageContext, packageContext.getString(R.string.altaPuntoSinImagen), Toast.LENGTH_LONG).show();
		} else {
			// Llamos a la Activity que nos permite ver la imagen a tamaño completo
			Intent intent = new Intent();
			intent.setClass(packageContext, ZoomImagenActivity.class);
			intent.putExtra(AbilidadeApplication.ZoomImagenActivityImagenPath, imagenPath);
			intent.putExtra(AbilidadeApplication.ZoomImagenActivityImagenCod, imagenCod);
			packageContext.startActivity(intent);
		}
	}
	
	/**
	 * escalarImagen: Este metodo se encarga de escalar una imagen a las dimensiones marcadas por defecto en AbilidadeApplication
	 * @param imagen: imagen a escalar
	 * @return Bitmap: imagen escalada
	 */
	public static Bitmap escalarImagen(Bitmap imagen) {
		
		// Se obtienen las dimensiones originales de la imagen
		int width = imagen.getWidth();
		int height = imagen.getHeight();
		
		// Si la imagen ya es pequeña, no se escalara
		if (width <= AbilidadeApplication.defaultImageWidth || height <= AbilidadeApplication.defaultImageHeight) {
			return imagen;
		}
		
		// Se comprueba la orientacion para escalar correctamente
		float scaleWidth;
		float scaleHeight;
		
		if (width >= height) {
			scaleWidth = AbilidadeApplication.defaultImageWidth / width;
			scaleHeight = AbilidadeApplication.defaultImageHeight / height;
		} else {
			scaleWidth = AbilidadeApplication.defaultImageHeight / width;
			scaleHeight = AbilidadeApplication.defaultImageWidth / height;
		}
		
		// Se crea una matriz para la manipulacion y se redimensiona el Bitmap
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		
		// Se crea el nuevo Bitmap escalado, y se devuelve como retorno del metodo
		Bitmap escaledImage = Bitmap.createBitmap(imagen, 0, 0, width, height, matrix, false);
		
		return escaledImage;
	}
}