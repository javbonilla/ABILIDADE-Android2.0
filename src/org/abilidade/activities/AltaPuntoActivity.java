package org.abilidade.activities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;


import org.abilidade.application.AbilidadeApplication;
import org.abilidade.application.DireccionCompleta;
import org.abilidade.R;
import org.apache.http.client.ClientProtocolException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import greendroid.app.GDActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;

public class AltaPuntoActivity extends GDActivity implements LocationListener {
	
	// Definicion de constantes
	private static final int VER_FOTO = 0;
	private static final int TOMAR_FOTO = 1;
	private static final int ELEGIR_GALERIA = 2;
	
	private static final int TOMAR_FOTO_PRINCIPAL = 3;
	private static final int TOMAR_FOTO_AUX1 = 4;
	private static final int TOMAR_FOTO_AUX2 = 5;
	
	private static final int ELEGIR_GALERIA_PRINCIPAL = 6;
	private static final int ELEGIR_GALERIA_AUX1 = 7;
	private static final int ELEGIR_GALERIA_AUX2 = 8;
	
	
	// Definicion de los componentes de pantalla
	private EditText editTextTitulo;
	private EditText editTextDireccion;
	private EditText editTextDescripcion;
	private ImageView imageViewImagenPrincipal;
	private ImageView imageViewImagenAux1;
	private ImageView imageViewImagenAux2;
	private Button buttonAgregarPunto;
	
	// Atributos utiles para geolocalizar al usuario
	private boolean ubicarUsuario = false;
	private LocationManager lm;
	private int sensor;
	private String provider = "";
	
	// Definicion de los atributos relativos al punto inaccesible
	private String sTitulo = "";
	private String sDireccion = "";
	private String sLocalidad = "";
	private String sProvincia = "";
	private String sDescripcion = "";
	private String sCorreoE = "";
	private double dLatitud = 0;
	private double dLongitud = 0;

	private String sImagenPrincipalPath = "";
	private String sImagenAux1Path = "";
	private String sImagenAux2Path = "";
	
	private Bitmap bmImagenPrincipal = null;
	private Bitmap bmImagenAux1 = null; 
	private Bitmap bmImagenAux2 = null;
	
	private Uri mImageUri;
	
	private DireccionCompleta direccionCompleta;
	
	// ProgressDialog para mientras se registra el punto
	private ProgressDialog pDialog;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarContentView(R.layout.alta_punto);
		
		// Elementos en la barra de accion --> Boton para localizar al usuario y boton de ayuda
		addActionBarItem(Type.Locate, R.id.action_bar_refresh);
		addActionBarItem(Type.Help, R.id.action_bar_help);
		
		// Localizacion de los componentes de pantalla
		editTextTitulo = (EditText) findViewById(R.id.altaPuntoTitulo);
		editTextDireccion = (EditText) findViewById(R.id.altaPuntoDireccion);
		editTextDescripcion = (EditText) findViewById(R.id.altaPuntoDescripcion);
		imageViewImagenPrincipal = (ImageView) findViewById(R.id.altaPuntoImagenPrincipal);
		imageViewImagenAux1 = (ImageView) findViewById(R.id.altaPuntoImagenAux1);
		imageViewImagenAux2 = (ImageView) findViewById(R.id.altaPuntoImagenAux2);
		buttonAgregarPunto = (Button) findViewById(R.id.altaPuntoBotonAgregar);
		
		// Recibimos el intent y sus parametros, y los almacenamos en los atributos correspondientes
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras == null) {
			ubicarUsuario = true; // Es necesario que la Activity se encargue de ubicar al usuario
		} else {
			dLatitud = extras.getDouble(AbilidadeApplication.altaPuntoParametroLatitud, 0);
			dLongitud = extras.getDouble(AbilidadeApplication.altaPuntoParametroLongitud, 0);
			
			Log.d("AltaPunto", "Latitud recibida:  " + dLatitud);
			Log.d("AltaPunto", "Longitud recibida: " + dLongitud);
			
			if (dLatitud == 0 && dLongitud == 0) {
				ubicarUsuario = true;
			} else {
				// Con la latitud y longitud tomadas por parametro obtenemos la direccion aproximada donde se encuentra el usuario
				direccionCompleta = AbilidadeApplication.obtenerDireccion(AltaPuntoActivity.this, dLatitud, dLongitud);
				
				sDireccion = direccionCompleta.getDireccion();
				sLocalidad = direccionCompleta.getLocalidad();
				sProvincia = direccionCompleta.getProvincia();
				
				editTextDireccion.setEnabled(true);
			}
			
		}
		
		// El correo-e con el que se registrara un punto es el correo-e del usuario logueado en el sistema. Se lee del ShPf
		SharedPreferences pref = getApplicationContext().getSharedPreferences(AbilidadeApplication.SHARED_PREFERENCES, 0); // 0 - for private mode
		sCorreoE = pref.getString(AbilidadeApplication.SHPF_USUARIO, "");
		
		Log.d("AltaPunto","correo-e con el que se va a registrar:"+sCorreoE);
		
		// Asignamos los Listeners correspondientes a todos los elementos de pantalla
		editTextTitulo.setOnKeyListener(keyListener);
		editTextDireccion.setOnKeyListener(keyListener);
		editTextDescripcion.setOnKeyListener(keyListener);
		
		imageViewImagenPrincipal.setOnClickListener(onClickImageListener);
		imageViewImagenAux1.setOnClickListener(onClickImageListener);
		imageViewImagenAux2.setOnClickListener(onClickImageListener);
		
		buttonAgregarPunto.setOnClickListener(onClickButtonListener);
		
		// Si es necesario, comenzamos con el proceso de ubicacion del usuario
		if (ubicarUsuario) {
			obtenerUbicacionUsuario();
		}
	}
	
	private OnClickListener onClickButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (validaciones()) {
				
				// Se usa una tarea asincrona para poder mostrar mientras tanto un ProgessDialog
				new asynclogin().execute();
			}
		}
	};
	
	private OnClickListener onClickImageListener = new OnClickListener() {
		@Override
		public void onClick(final View v) {
			
			String[] opcionesFoto = new String[] { getString(R.string.altaPuntoVerFoto), getString(R.string.altaPuntoTomarFoto), getString(R.string.altaPuntoElegirGaleria) };
			
			// Con los valores de la vista que se hizo clic, se muestra un Dialog y se ejecuta la accion correspondiente
			// en funcion del ID de la vista sobre la que se hizo clic
			AlertDialog.Builder builder = new AlertDialog.Builder(AltaPuntoActivity.this);
			builder.setTitle(R.string.altaPuntoSeleccioneOpcion);
			builder.setItems(opcionesFoto, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
			        if (item == VER_FOTO) {
						if (v.getId() == R.id.altaPuntoImagenPrincipal) {
							AbilidadeApplication.verFoto(AltaPuntoActivity.this, sImagenPrincipalPath, null);
						} else if (v.getId() == R.id.altaPuntoImagenAux1) {
							AbilidadeApplication.verFoto(AltaPuntoActivity.this, sImagenAux1Path, null);
						} else if (v.getId() == R.id.altaPuntoImagenAux2) {
							AbilidadeApplication.verFoto(AltaPuntoActivity.this, sImagenAux2Path, null);
						}
					} else if (item == TOMAR_FOTO) {
						if (v.getId() == R.id.altaPuntoImagenPrincipal) {
							tomarFoto(TOMAR_FOTO_PRINCIPAL);
						} else if (v.getId() == R.id.altaPuntoImagenAux1) {
							tomarFoto(TOMAR_FOTO_AUX1);
						} else if (v.getId() == R.id.altaPuntoImagenAux2) {
							tomarFoto(TOMAR_FOTO_AUX2);
						}
					} else if (item == ELEGIR_GALERIA) {
						if (v.getId() == R.id.altaPuntoImagenPrincipal) {
							elegirFoto(ELEGIR_GALERIA_PRINCIPAL);
						} else if (v.getId() == R.id.altaPuntoImagenAux1) {
							elegirFoto(ELEGIR_GALERIA_AUX1);
						} else if (v.getId() == R.id.altaPuntoImagenAux2) {
							elegirFoto(ELEGIR_GALERIA_AUX2);
						}
					}
			    }
			});
			AlertDialog alert = builder.create();
			alert.show();
		}
	};
	
	private OnKeyListener keyListener = new OnKeyListener() {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (v.getId() == R.id.altaPuntoTitulo) {
				sTitulo = editTextTitulo.getText().toString();
			} else if (v.getId() == R.id.altaPuntoDireccion) {
				sDireccion = editTextDireccion.getText().toString();
			} else if (v.getId() == R.id.altaPuntoDescripcion) {
				sDescripcion = editTextDescripcion.getText().toString();
			} else {
				return false;
			}
			return false;
		}
	};
	
	@Override
    public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
        if (item.getItemId() == R.id.action_bar_help) {
        	// Lo que se hace es llamar al navegador con la pagina para la ayuda
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse("http://abilidade.eu/r/androidhelp/registrar_punto.htm"));
			startActivity(i);
		} else if (item.getItemId() == R.id.action_bar_refresh) {
			editTextDireccion.setText("");
			editTextDireccion.setHint(R.string.obteniendoDireccion);
			obtenerUbicacionUsuario();
		} else {
			return super.onHandleActionBarItemClick(item, position);
		}
        return true;
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Cargamos todos los valores almacenados del punto en los componentes de pantalla
		editTextTitulo.setText(sTitulo);
		editTextDireccion.setText(sDireccion);
		editTextDescripcion.setText(sDescripcion);
		if (bmImagenPrincipal != null) {
			imageViewImagenPrincipal.setImageBitmap(bmImagenPrincipal);
		}
		if (bmImagenAux1 != null) {
			imageViewImagenAux1.setImageBitmap(bmImagenAux1);
		}
		if (bmImagenAux2 != null) {
			imageViewImagenAux2.setImageBitmap(bmImagenAux2);
		}
	}
	
	@Override
	protected void onPause() {
		// Guardamos los valores de todos los EditTexts para poder mostrarlos cuando la Activity vuelva a tener el foco
		// Las imagenes se han ido guardando segun se han ido tomando, por eso no es necesario guardarlas aqui
		sTitulo = editTextTitulo.getText().toString();
		sDireccion = editTextDireccion.getText().toString();
		sDescripcion = editTextDescripcion.getText().toString();
		
		super.onPause();
	}
	
	/**
	 * obtenerUbicacionUsuario: metodo para obtener la ubicacion del usuario usando para ello los sensores activos adecuados 
	 */
	private void obtenerUbicacionUsuario() {
		// Comprobamos los sensores que hay activos. Si no hay ninguno activo, mostraremos la pantalla de ajustes
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
	   	
	   	lm.requestLocationUpdates(provider, 0, 0, AltaPuntoActivity.this);
	}
	
	@Override
	public void onLocationChanged(Location location) {
		
		// Con la latitud y longitud obtendremos el resto de campos gracias a la API Geocoder
		dLatitud = location.getLatitude();
		dLongitud = location.getLongitude();
		
		// A partir de la latitud y longitud, obtenemos la direccion aproximada donde se encuentra el usuario
		direccionCompleta = AbilidadeApplication.obtenerDireccion(AltaPuntoActivity.this, dLatitud, dLongitud);
		
		sDireccion = direccionCompleta.getDireccion();
		sLocalidad = direccionCompleta.getLocalidad();
		sProvincia = direccionCompleta.getProvincia();
		
		// Mostramos la direccion al usuario
		editTextDireccion.setText(sDireccion);
		
		// Activamos el EditText para que el usuario pueda modificar la posicion
		editTextDireccion.setEnabled(true);
		
		// Como el usuario ya ha sido geolocalizado, se para el manager
		lm.removeUpdates(this);
	}
	
	@Override
	public void onProviderDisabled(String provider) {
		// NO hacer nada de momento
	}

	@Override
	public void onProviderEnabled(String provider) {
		// NO hacer nada de momento
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// NO hacer nada de momento
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data); 
    	
    	if (requestCode == TOMAR_FOTO_PRINCIPAL && resultCode == RESULT_OK) {
    		cargarImagenDesdeCamara(TOMAR_FOTO_PRINCIPAL);
    	}
    	
    	if (requestCode == TOMAR_FOTO_AUX1 && resultCode == RESULT_OK) {
    		cargarImagenDesdeCamara(TOMAR_FOTO_AUX1);
    	}
    	
    	if (requestCode == TOMAR_FOTO_AUX2 && resultCode == RESULT_OK) {
    		cargarImagenDesdeCamara(TOMAR_FOTO_AUX2);
    	}
    	
    	if (requestCode == ELEGIR_GALERIA_PRINCIPAL && resultCode == RESULT_OK) {
    		cargarImagenDesdeGaleria(data, ELEGIR_GALERIA_PRINCIPAL);
    	}
    	
    	if (requestCode == ELEGIR_GALERIA_AUX1 && resultCode == RESULT_OK) {
    		cargarImagenDesdeGaleria(data, ELEGIR_GALERIA_AUX1);
    	}
    	
    	if (requestCode == ELEGIR_GALERIA_AUX2 && resultCode == RESULT_OK) {
    		cargarImagenDesdeGaleria(data, ELEGIR_GALERIA_AUX2);
    	}
	}
	
	/**
	 * validaciones: Este metodo valida si el usuario ha introducido los datos necesarios para registrar el punto
	 *              Los datos obligatorios son: titulo del punto y foto principal
	 */
	private boolean validaciones() {
		
		// Lo primero que vamos a hacer es recoger los campos obligatorios, para comprobar si el usuario los ha rellenado
		sTitulo = editTextTitulo.getText().toString();
		sDireccion = editTextDireccion.getText().toString();
		
		if (sTitulo.length() == 0 || sTitulo == null) {
			Toast.makeText(getApplicationContext(), getString(R.string.altaPuntoValidacionTitulo), Toast.LENGTH_LONG).show();
			return false;
		} 
		if (sDireccion.length() == 0 || sDireccion == null) {
			Toast.makeText(getApplicationContext(), getString(R.string.altaPuntoValidacionDireccion), Toast.LENGTH_LONG).show();
			return false;
		}
		if (bmImagenPrincipal == null) {
			Toast.makeText(getApplicationContext(), getString(R.string.altaPuntoValidacionImagen), Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}
	
	
	/**
	 * tomarFoto: Metodo encargado de llamar a la app Camera de Android para tomar una foto de un punto inaccesible
	 * @param destinoFoto: destino de la foto tomada (foto principal, auxiliar 1 o auxiliar 2)
	 */
	private void tomarFoto(int destinoFoto) {
		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
	    File photo;
	    try
	    {
	        // Lugar donde se va a almacenar temporalmente la foto hecha por la camara
	        File tempDir= Environment.getExternalStorageDirectory();
	        tempDir = new File(tempDir.getAbsolutePath() + "/.temp/");
	        if(!tempDir.exists())
	        {
	            tempDir.mkdir();
	        }
	        photo = File.createTempFile("picture", ".jpg", tempDir);
	        photo.delete();
	        
	        mImageUri = Uri.fromFile(photo);
		    intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
		    //start camera intent
		    startActivityForResult(intent, destinoFoto);
	    }
	    catch(Exception e)
	    {
	        Log.d("AltaPuntoActivity", getString(R.string.altaPuntoFalloTomarFoto));
	        Toast.makeText(getApplicationContext(), getString(R.string.altaPuntoFalloTomarFoto), Toast.LENGTH_LONG).show();
	    }
	}
	
	/**
	 * elegirFoto: Metodo encargado de llamar a la app Galeria de Android para elegir una foto de un punto inaccesible
	 * @param destinoFoto: destino de la foto elegida (foto principal, auxiliar 1 o auxiliar 2)
	 */
	private void elegirFoto(int destinoFoto) {
		Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
	    startActivityForResult(intent, destinoFoto);
	}
	
	/**
	 * cargarImagenDesdeCamara: Metodo encargado de recuperar la imagen de camara y cargarla en el destino indicado
	 * @param destinoFoto: destino de la foto elegida (foto principal, auxiliar 1 o auxiliar 2)
	 */
	private void cargarImagenDesdeCamara(int destinoFoto) {
		getContentResolver().notifyChange(mImageUri, null);
	    try
	    {
	    	switch(destinoFoto) {
	    		case TOMAR_FOTO_PRINCIPAL:
	    			sImagenPrincipalPath = mImageUri.getPath();
	    	    	
	    			// Se redimensiona primero la imagen y luego se carga al imageView correspondiente
	    			bmImagenPrincipal = decodeSampledBitmapFromResource(sImagenPrincipalPath, AbilidadeApplication.defaultImageWidth, AbilidadeApplication.defaultImageHeight);
	    			imageViewImagenPrincipal.setImageBitmap(bmImagenPrincipal);
	    	    	
	    	    	break;
	    		case TOMAR_FOTO_AUX1:
	    			sImagenAux1Path = mImageUri.getPath();
	    			
	    			// Se redimensiona primero la imagen y luego se carga al imageView correspondiente
	    			bmImagenAux1 = decodeSampledBitmapFromResource(sImagenAux1Path, AbilidadeApplication.defaultImageWidth, AbilidadeApplication.defaultImageHeight);
	    			imageViewImagenAux1.setImageBitmap(bmImagenAux1);
	    	    	
	    	    	break;
	    		case TOMAR_FOTO_AUX2:
	    			sImagenAux2Path = mImageUri.getPath();

	    			// Se redimensiona primero la imagen y luego se carga al imageView correspondiente
	    			bmImagenAux2 = decodeSampledBitmapFromResource(sImagenAux2Path, AbilidadeApplication.defaultImageWidth, AbilidadeApplication.defaultImageHeight);
	    			imageViewImagenAux2.setImageBitmap(bmImagenAux2);
	    	    	
	    	    	break;
	    	}
	    }
	    catch (Exception e)
	    {
	        Toast.makeText(this, getString(R.string.altaPuntoFalloFotoTomada), Toast.LENGTH_SHORT).show();
	        Log.d("AltaPuntoActivity", getString(R.string.altaPuntoFalloFotoTomada), e);
	    }
	}
	
	/**
	 * cargarImagenDesdeGaleria: Este metodo se encarga de recuperar la imagen de galeria y cargarla en el destino indicado
	 * @param data: Intent devuelto por la galeria, el cual contiene la foto a cargar
	 * @param destinoFoto: destino de la foto elegida (foto principal, auxiliar 1 o auxiliar 2)
	 */
	private void cargarImagenDesdeGaleria(Intent data, int destinoFoto) {
		Uri selectedImage = data.getData();
        String[] filePathColumn = {android.provider.MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close(); /** ESTO ES PARA OBTENER LA IMAGEN EN RESOLUCION COMPLETA **/
        
        switch(destinoFoto) {
        	case ELEGIR_GALERIA_PRINCIPAL:
        		
        		// Se redimensiona primero la imagen y luego se carga al imageView correspondiente
        		bmImagenPrincipal = decodeSampledBitmapFromResource(filePath, AbilidadeApplication.defaultImageWidth, AbilidadeApplication.defaultImageHeight);
        		
        		
        		sImagenPrincipalPath = filePath;
                
                imageViewImagenPrincipal.setImageBitmap(bmImagenPrincipal);
        		break;
        	case ELEGIR_GALERIA_AUX1:
        		
        		// Se redimensiona primero la imagen y luego se carga al imageView correspondiente
        		bmImagenAux1 = decodeSampledBitmapFromResource(filePath, AbilidadeApplication.defaultImageWidth, AbilidadeApplication.defaultImageHeight);
        		sImagenAux1Path = filePath;
                
        		imageViewImagenAux1.setImageBitmap(bmImagenAux1);
        		break;
        	case ELEGIR_GALERIA_AUX2:
        		// Se redimensiona primero la imagen y luego se carga al imageView correspondiente
        		bmImagenAux2 = decodeSampledBitmapFromResource(filePath, AbilidadeApplication.defaultImageWidth, AbilidadeApplication.defaultImageHeight);
        		sImagenAux2Path = filePath;
                
        		imageViewImagenAux2.setImageBitmap(bmImagenAux2);
        		break;
        }
	}
	
	public Bitmap decodeSampledBitmapFromResource(String filePath, int iDesiredWidth, int iDesiredHeight) {
		 
		// 1. Primero se descodifica la imagen con inJustDecodeBounds=true para comprobar las propiedades de la imagen
		final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(filePath, options);
	    
	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, iDesiredWidth, iDesiredHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeFile(filePath, options);
	}
	
	public int calculateInSampleSize(Options options, int iDesiredWidth, int iDesiredHeight) {
		
		// Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;

	    if (height > iDesiredWidth || width > iDesiredHeight) {

	        // Calculate ratios of height and width to requested height and width
	        final int heightRatio = Math.round((float) height / (float) iDesiredHeight);
	        final int widthRatio = Math.round((float) width / (float) iDesiredWidth);

	        // Choose the smallest ratio as inSampleSize value, this will guarantee
	        // a final image with both dimensions larger than or equal to the
	        // requested height and width.
	        inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
	    }

	    return inSampleSize;
	}
	
	@Override
	protected void onStop() {
		if (lm != null) {
			lm.removeUpdates(this);
		}
		
		pDialog = null;
		
		super.onStop();
	}
	
	/*		CLASE ASYNCTASK
	 * Se usa esta clase para poder mostrar el dialogo de progreso mientras se envian y obtienen los datos.     
	 */

	class asynclogin extends AsyncTask< String, String, String > {
		 
		protected void onPreExecute() {
	    	//para el progress dialog
	        pDialog = new ProgressDialog(AltaPuntoActivity.this);
	        pDialog.setMessage(getString(R.string.altaPuntoRegistrandoPunto));
	        pDialog.setIndeterminate(false);
	        pDialog.setCancelable(false);
	        pDialog.show();
	    }

		protected String doInBackground(String... params) {
			// Enviamos el punto a la web
			HttpClient httpclient = new DefaultHttpClient();     
			HttpPost httppost = new HttpPost("http://www.abilidade.eu/r/subirccv2.php");      
		 
			try {         
				 // Imagenes
				 MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
				 entity.addPart("titulo",new StringBody(sTitulo));
				 entity.addPart("direccion",new StringBody(sDireccion));
				 entity.addPart("descripcion",new StringBody(sDescripcion));
				 entity.addPart("correoe",new StringBody(sCorreoE));
				 entity.addPart("latitud",new StringBody(""+dLatitud));
				 entity.addPart("longitud",new StringBody(""+dLongitud));
				 
				 File file= new File(sImagenPrincipalPath);
				 
				 entity.addPart("image1",new FileBody(file));
				 
				 if (bmImagenAux1 != null) {
					 File file2= new File(sImagenAux1Path);
					 entity.addPart("image2",new FileBody(file2));
				 }
				 
				 if (bmImagenAux2 != null) {
					 File file3= new File(sImagenAux2Path);
					 entity.addPart("image3",new FileBody(file3));
				 }
				 
				 httppost.setEntity(entity);

			     HttpResponse response = httpclient.execute(httppost);
			     Log.d("AltaPunto","response: "+response.toString());
				
			} 
			catch (ClientProtocolException e) {       
			} 
			catch (IOException e) {         
			} 
			
			return AbilidadeApplication.RETORNO_OK;
	    	
		}
	   
		protected void onPostExecute(String result) {

		    if (pDialog != null) {
		    	pDialog.dismiss();//ocultamos progess dialog.
		    }
			
	       Log.d("AltaPuntoOnPostExecute=",""+result);
	       
			// Si el registro del punto ha finalizado con exito, se finaliza la actividad
			finish();
	     }
	}
}