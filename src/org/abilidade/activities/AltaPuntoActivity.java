package org.abilidade.activities;

import java.io.File;
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
import org.abilidade.db.DatabaseCommons;
import org.abilidade.db.PuntoProvider;
import org.abilidade.R;
import org.apache.http.client.ClientProtocolException;

import android.os.Handler;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
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
	private EditText editTextCorreoE;
	private ImageView imageViewImagenPrincipal;
	private ImageView imageViewImagenAux1;
	private ImageView imageViewImagenAux2;
	private Button buttonAgregarPunto;
	
	private ProgressDialog dialogPuntoInsertado;
	
	// Atributos utiles para geolocalizar al usuario
	private boolean ubicarUsuario = false;
	private LocationManager lm;
	private int sensor;
	private String provider = "";
	
	// Definicion de los atributos relativos al punto inaccesible
	private String titulo = "";
	private String direccion = "";
	private String localidad = "";
	private String provincia = "";
	private String descripcion = "";
	private String correoE = "";
	private double latitud = 0;
	private double longitud = 0;

	private String imagenPrincipalPath = "";
	private String imagenAux1Path = "";
	private String imagenAux2Path = "";
	
	private Bitmap imagenPrincipal = null;
	private Bitmap imagenAux1 = null; 
	private Bitmap imagenAux2 = null;
	
	private Uri mImageUri;
	
	private DireccionCompleta direccionCompleta;
	
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
		editTextCorreoE = (EditText) findViewById(R.id.altaPuntoCorreoE);
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
			latitud = extras.getDouble(AbilidadeApplication.altaPuntoParametroLatitud, 0);
			longitud = extras.getDouble(AbilidadeApplication.altaPuntoParametroLongitud, 0);
			
			// Con la latitud y longitud tomadas por parametro obtenemos la direccion aproximada donde se encuentra el usuario
			direccionCompleta = AbilidadeApplication.obtenerDireccion(AltaPuntoActivity.this, latitud, longitud);
			
			direccion = direccionCompleta.getDireccion();
			localidad = direccionCompleta.getLocalidad();
			provincia = direccionCompleta.getProvincia();
		}
		
		// Asignamos los Listeners correspondientes a todos los elementos de pantalla
		editTextTitulo.setOnKeyListener(keyListener);
		editTextDireccion.setOnKeyListener(keyListener);
		editTextDescripcion.setOnKeyListener(keyListener);
		editTextCorreoE.setOnKeyListener(keyListener);
		
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
				registrarPunto();

				// Enviamos el punto a la web
				HttpClient httpclient = new DefaultHttpClient();     
				HttpPost httppost = new HttpPost("http://www.abilidade.eu/r/subircc.php");      
			 
				try {         
					// Imagenes
					 MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
					 entity.addPart("titulo",new StringBody(titulo));
					 entity.addPart("direccion",new StringBody(direccion));
					 entity.addPart("descripcion",new StringBody(descripcion));
					 entity.addPart("correoe",new StringBody(correoE));
					 entity.addPart("latitud",new StringBody(""+latitud));
					 entity.addPart("longitud",new StringBody(""+longitud));
					 
					 File file= new File(imagenPrincipalPath);
					 entity.addPart("image1",new FileBody(file));
					 
					 if (imagenAux1 != null) {
						 File file2= new File(imagenAux1Path);
						 entity.addPart("image2",new FileBody(file2));
					 }
					 
					 if (imagenAux2 != null) {
						 File file3= new File(imagenAux2Path);
						 entity.addPart("image3",new FileBody(file3));
					 }
					 
					 httppost.setEntity(entity);

				     HttpResponse response = httpclient.execute(httppost);
					
				} 
				catch (ClientProtocolException e) {       
				} 
				catch (IOException e) {         
				} 
				
				Toast.makeText(getApplicationContext(), getString(R.string.altaPuntoRegistroExito), Toast.LENGTH_LONG).show();
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
							AbilidadeApplication.verFoto(AltaPuntoActivity.this, imagenPrincipalPath, null);
						} else if (v.getId() == R.id.altaPuntoImagenAux1) {
							AbilidadeApplication.verFoto(AltaPuntoActivity.this, imagenAux1Path, null);
						} else if (v.getId() == R.id.altaPuntoImagenAux2) {
							AbilidadeApplication.verFoto(AltaPuntoActivity.this, imagenAux2Path, null);
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
				titulo = editTextTitulo.getText().toString();
			} else if (v.getId() == R.id.altaPuntoDireccion) {
				direccion = editTextDireccion.getText().toString();
			} else if (v.getId() == R.id.altaPuntoDescripcion) {
				descripcion = editTextDescripcion.getText().toString();
			} else if (v.getId() == R.id.altaPuntoCorreoE) {
				correoE = editTextCorreoE.getText().toString();
			} else {
				return false;
			}
			return false;
		}
	};
	
	@Override
    public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
        if (item.getItemId() == R.id.action_bar_help) {
        	Intent intent = new Intent();
        	intent.setClass(AltaPuntoActivity.this, AyudaAltaPunto.class);
        	startActivity(intent);
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
		editTextTitulo.setText(titulo);
		editTextDireccion.setText(direccion);
		editTextDescripcion.setText(descripcion);
		editTextCorreoE.setText(correoE);
		if (imagenPrincipal != null) {
			imageViewImagenPrincipal.setImageBitmap(imagenPrincipal);
		}
		if (imagenAux1 != null) {
			imageViewImagenAux1.setImageBitmap(imagenAux1);
		}
		if (imagenAux2 != null) {
			imageViewImagenAux2.setImageBitmap(imagenAux2);
		}
	}
	
	@Override
	protected void onPause() {
		// Guardamos los valores de todos los EditTexts para poder mostrarlos cuando la Activity vuelva a tener el foco
		// Las imagenes se han ido guardando segun se han ido tomando, por eso no es necesario guardarlas aqui
		titulo = editTextTitulo.getText().toString();
		direccion = editTextDireccion.getText().toString();
		descripcion = editTextDescripcion.getText().toString();
		correoE = editTextCorreoE.getText().toString();
		
		super.onPause();
	}
	
	/**
	 * obtenerUbicacionUsuario: metodo para obtener la ubicacion del usuario usando para ello los sensores activos adecuados 
	 */
	private void obtenerUbicacionUsuario() {
		// Comprobamos los sensores que hay activos. Si no hay ninguno activo, mostraremos la pantalla de ajustes
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
	   	
	   	lm.requestLocationUpdates(provider, 0, 0, AltaPuntoActivity.this);
	}
	
	@Override
	public void onLocationChanged(Location location) {
		
		// Con la latitud y longitud obtendremos el resto de campos gracias a la API Geocoder
		latitud = location.getLatitude();
		longitud = location.getLongitude();
		
		// A partir de la latitud y longitud, obtenemos la direccion aproximada donde se encuentra el usuario
		direccionCompleta = AbilidadeApplication.obtenerDireccion(AltaPuntoActivity.this, latitud, longitud);
		
		direccion = direccionCompleta.getDireccion();
		localidad = direccionCompleta.getLocalidad();
		provincia = direccionCompleta.getProvincia();
		
		// Mostramos la direccion al usuario
		editTextDireccion.setText(direccion);
		
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
		titulo = editTextTitulo.getText().toString();
		direccion = editTextDireccion.getText().toString();
		
		if (titulo.length() == 0 || titulo == null) {
			Toast.makeText(getApplicationContext(), getString(R.string.altaPuntoValidacionTitulo), Toast.LENGTH_LONG).show();
			return false;
		} 
		if (direccion.length() == 0 || direccion == null) {
			Toast.makeText(getApplicationContext(), getString(R.string.altaPuntoValidacionDireccion), Toast.LENGTH_LONG).show();
			return false;
		}
		if (imagenPrincipal == null) {
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
	    			imagenPrincipalPath = mImageUri.getPath();
	    	    	imagenPrincipal = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);
	    	    	imagenPrincipal = AbilidadeApplication.rotarImagen(imagenPrincipalPath, imagenPrincipal);
	    	    	imageViewImagenPrincipal.setImageBitmap(imagenPrincipal);
	    			break;
	    		case TOMAR_FOTO_AUX1:
	    			imagenAux1Path = mImageUri.getPath();
	    	    	imagenAux1 = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);
	    	    	imagenAux1 = AbilidadeApplication.rotarImagen(imagenAux1Path, imagenAux1);
	    	        imageViewImagenAux1.setImageBitmap(imagenAux1);
	    			break;
	    		case TOMAR_FOTO_AUX2:
	    			imagenAux2Path = mImageUri.getPath();
	    	    	imagenAux2 = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);
	    	    	imagenAux2 = AbilidadeApplication.rotarImagen(imagenAux2Path, imagenAux2);
	    	        imageViewImagenAux2.setImageBitmap(imagenAux2);
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
        		imagenPrincipal = BitmapFactory.decodeFile(filePath);
        		imagenPrincipal = AbilidadeApplication.rotarImagen(filePath, imagenPrincipal);
                imagenPrincipalPath = filePath;
                imageViewImagenPrincipal.setImageBitmap(imagenPrincipal);
        		break;
        	case ELEGIR_GALERIA_AUX1:
        		imagenAux1 = BitmapFactory.decodeFile(filePath);
        		imagenAux1 = AbilidadeApplication.rotarImagen(filePath, imagenAux1);
                imagenAux1Path = filePath;
                imageViewImagenAux1.setImageBitmap(imagenAux1);
        		break;
        	case ELEGIR_GALERIA_AUX2:
        		imagenAux2 = BitmapFactory.decodeFile(filePath);
        		imagenAux2 = AbilidadeApplication.rotarImagen(filePath, imagenAux2);
                imagenAux2Path = filePath;
                imageViewImagenAux2.setImageBitmap(imagenAux2);
        		break;
        }
        
        /** ESTO ES PARA OBTENER EL THUMBNAIL DE LA IMAGEN **/
		/* //imageUri será la uri de la imagen que vamos a obtener el thumb
		// String uriThumb = "";
		//Obtenemos el último segmento de la uri que es el que nos indica el ID
		long uriThumbId = Long.parseLong(selectedImage.getLastPathSegment());
		 
		//Más info sobre esta función en
		// http://developer.android.com/reference/android/provider/MediaStore.Images.Thumbnails.html
		Cursor cursor = android.provider.MediaStore.Images.Thumbnails.queryMiniThumbnail(getContentResolver(), uriThumbId, android.provider.MediaStore.Images.Thumbnails.MINI_KIND, null);
		
		 
		//Comprobamos que no tengamos un cursor nulo y que hayamos obtenido almenos un resultado
		if (cursor != null && cursor.getCount() > 0) {
    		cursor.moveToFirst(); //Como en este caso sólo nos interesa una imagen vamos al primer registro
    		 
    		//cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA)
    		//Obtiene el índice de la columna que queremos obtener el valor, en nuestro caso obtenemos el campo data
    		//cursor.getString(indiceColumna) 
    		//Obtenemos el valor de la columna seleccionada
    		 
    		uriThumb = cursor.getString(cursor.getColumnIndex(android.provider.MediaStore.Images.Thumbnails.DATA));
    		cursor.close();
    		
    		Bitmap yourSelectedImage = BitmapFactory.decodeFile(uriThumb);
            imagenCentral.setImageBitmap(yourSelectedImage);
		} */
	}
	
	/**
	 * registrarPunto: registra el punto con la informacion ofrecida por el usuario en la BD local de su terminal
	 */
	private void registrarPunto() {
		
		// Mostramos un ProgressDialog indicando al usuario que se esta llevando a cabo la operacion
		dialogPuntoInsertado = ProgressDialog.show(AltaPuntoActivity.this, "", getString(R.string.altaPuntoRegistrandoPunto), true, false);
		
		// Lanzamos un nuevo thread en el que se llevara a cabo el registro de punto inaccesible
		Thread thr = new Thread() {
			public void run() {
				String imagenPrincipalTexto = "";
				String imagenAux1Texto = "";
				String imagenAux2Texto = "";
				
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
				values.put(DatabaseCommons.Punto.LOCALIDAD, localidad);
				values.put(DatabaseCommons.Punto.PROVINCIA, provincia);
				values.put(DatabaseCommons.Punto.DESCRIPCION, descripcion);
				values.put(DatabaseCommons.Punto.ESTADO, AbilidadeApplication.ESTADO_ABIERTO);
				values.put(DatabaseCommons.Punto.CORREOE, correoE);
				values.put(DatabaseCommons.Punto.LATITUD, latitud);
				values.put(DatabaseCommons.Punto.LONGITUD, longitud);
				values.put(DatabaseCommons.Punto.SINCRONIZADO, AbilidadeApplication.PUNTO_NO_SINCRONIZADO); // Se registra el punto como no sincronizado
				values.put(DatabaseCommons.Punto.IMAGEN_PRINCIPAL, imagenPrincipalTexto);
				values.put(DatabaseCommons.Punto.IMAGEN_AUX1, imagenAux1Texto);
				values.put(DatabaseCommons.Punto.IMAGEN_AUX2, imagenAux2Texto);
				
				// Por ultimo, guardamos el punto en la BD local del terminal
				getContentResolver().insert(PuntoProvider.CONTENT_URI, values);
				
				// Enviamos un mensaje diciendo que todo se ha realizado correctamente
				uiCallback.sendEmptyMessage(0);
			}
		};
		thr.start();
	}
	
	private Handler uiCallback = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			dialogPuntoInsertado.dismiss();
			
			// Notificamos al usuario que la insercion ha sido correcta
			Toast.makeText(getApplicationContext(), getString(R.string.altaPuntoRegistroExito), Toast.LENGTH_LONG).show();
			
			// Como el punto ha sido dado de alta, cerramos la Activity
			finish();
		}
	};
	
	@Override
	protected void onStop() {
		if (lm != null) {
			lm.removeUpdates(this);
		}
		
		super.onStop();
	}
}