package org.abilidadev2.map_components;

import org.abilidadev2.map_components.OverlayItemPunto;
import org.abilidadev2.R;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A view representing a MapView marker information balloon.
 * <p>
 * This class has a number of Android resource dependencies:
 * <ul>
 * <li>drawable/balloon_overlay_bg_selector.xml</li>
 * <li>drawable/balloon_overlay_close.png</li>
 * <li>drawable/balloon_overlay_focused.9.png</li>
 * <li>drawable/balloon_overlay_unfocused.9.png</li>
 * <li>layout/balloon_map_overlay.xml</li>
 * </ul>
 * </p>
 * 
 * @author Jeff Gilfelt
 *
 */
public class BalloonOverlayView<Item extends OverlayItemPunto> extends FrameLayout {

	// Definicion de atributos
	private LinearLayout layout;
	private ImageView imageViewImagenPunto;
	private TextView textViewTituloPunto;
	private TextView textViewDireccionPunto;
	
	/**
	 * Create a new BalloonOverlayView.
	 * 
	 * @param context - The activity context.
	 * @param balloonBottomOffset - The bottom padding (in pixels) to be applied
	 * when rendering this view.
	 */
	public BalloonOverlayView(Context context, int balloonBottomOffset) {

		super(context);

		setPadding(10, 0, 10, balloonBottomOffset);
		layout = new LinearLayout(context);
		layout.setVisibility(VISIBLE);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.balloon_overlay, layout);
		textViewTituloPunto = (TextView) v.findViewById(R.id.balloon_titulo_punto);
		textViewDireccionPunto = (TextView) v.findViewById(R.id.ballon_direccion_punto);
		imageViewImagenPunto = (ImageView) v.findViewById(R.id.ballon_imagen_punto);
		
		/*ImageView close = (ImageView) v.findViewById(R.id.balloon_cerrar);
		close.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				layout.setVisibility(GONE);
			}
		});*/

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.NO_GRAVITY;

		addView(layout, params);

	}
	
	/**
	 * Sets the view data from a given overlay item.
	 * 
	 * @param item - The overlay item containing the relevant view data 
	 * (title and snippet). 
	 */
	public void setData(Item item) {
		
		layout.setVisibility(VISIBLE);
		textViewTituloPunto.setText(item.getsTitulo());	
		textViewDireccionPunto.setText(item.getsDireccion());
		imageViewImagenPunto.setImageBitmap(BitmapFactory.decodeFile(item.getsImagenPrincipalThumb()));
	}
}
