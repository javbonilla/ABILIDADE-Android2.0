Este repositorio se utiliza para sincronizar la app cliente Android del Proyecto ABILIDADE.

24 10 2011: Añadida funcionalidad para elegir una imagen para el punto. La imagen podra ser de la camara o de la galeria

25 10 2011: Trabajo en pantalla de alta de punto inaccesible. Pantalla diseñada con greendroid, añadidos los EditText las ImageViews para previsualizar las imagenes de punto. Además, se dota de funcionalidad para que el usuario elija entre ver las imágenes a pantalla completa, tomar una foto del punto o seleccionarla de la galería

26 10 2011: Funcionalidad de pedir informacion completa a la hora de dar de alta un punto. Falta por guardar la información en la Base de Datos cuando el usuario haga clic en "Registrar punto"

27 10 2011: Realizado el siguiente trabajo:
	- Limpieza y estructuración de código (mensajes en @string, métodos comunes, comentarios)
	- Funcionalidad para redimensionar las imágenes a 800x600 y, cuando es necesario, rotarlas para verlas correctamente en pantalla
	- Creación de la BD (AbilidadeSQLHelper y DatabaseCommons)
	- Creación del ContentProvider para manejar la base de datos (PuntoProvider). Pruebas del ContentProvider

28 10 2011: Se ha añadido la funcionalidad para guardar las imágenes como texto en la Base de Datos

