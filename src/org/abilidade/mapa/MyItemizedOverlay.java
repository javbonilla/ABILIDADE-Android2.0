/***
 * Copyright (c) 2010 readyState Software Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.abilidade.mapa;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import org.abilidade.activities.DetallePuntoActivity;
import org.abilidade.application.AbilidadeApplication;
import org.abilidade.map_components.BalloonItemizedOverlay;
import org.abilidade.map_components.OverlayItemPunto;
import com.google.android.maps.MapView;


public class MyItemizedOverlay extends BalloonItemizedOverlay<OverlayItemPunto> {

	private ArrayList<OverlayItemPunto> m_overlays = new ArrayList<OverlayItemPunto>();
	private Context c;
	
	public MyItemizedOverlay(Drawable defaultMarker, MapView mapView) {
		super(boundCenter(defaultMarker), mapView);
		c = mapView.getContext();
	}

	public void addOverlay(OverlayItemPunto overlay) {
	    m_overlays.add(overlay);
	    populate();
	}

	@Override
	protected OverlayItemPunto createItem(int i) {
		return m_overlays.get(i);
	}

	@Override
	public int size() {
		return m_overlays.size();
	}

	@Override
	protected boolean onBalloonTap(int index, OverlayItemPunto item) {
		Intent intent = new Intent();
		intent.setClass(c, DetallePuntoActivity.class);
		intent.putExtra(AbilidadeApplication.PUNTO_TITULO, item.getsTitulo());
		intent.putExtra(AbilidadeApplication.PUNTO_DESCRIPCION, item.getsDescripcion());
		intent.putExtra(AbilidadeApplication.PUNTO_DIRECCION, item.getsDireccion());
		intent.putExtra(AbilidadeApplication.PUNTO_IMAGEN_PRINCIPAL, item.getsImagenPrincipal());
		intent.putExtra(AbilidadeApplication.PUNTO_IMAGEN_AUX1, item.getsImagenAux1());
		intent.putExtra(AbilidadeApplication.PUNTO_IMAGEN_AUX2, item.getsImagenAux2());
		intent.putExtra(AbilidadeApplication.PUNTO_IMAGEN_PRINCIPAL_THUMB,item.getsImagenPrincipalThumb());
		
		// Y se arranca la pantalla de Detalle del punto
		c.startActivity(intent);
		return true;
	}
}
