# INTEGRANTES DEL EQUIPO
20253ds056<br>
20253ds041<br>
20253ds057<br>

# DESCRIPCIÓN DE LA SOLUCIÓN

Mi Recetario es una aplicación móvil desarrollada en Kotlin y Jetpack Compose que permite a los usuarios registrar, consultar, editar y eliminar sus recetas. Cada receta puede incluir título, dificultad, tiempos de preparación, cocción y horneado, ingredientes, instrucciones y fotografías.

La solución utiliza una API REST desarrollada con Ktor, que se ejecuta localmente y se comunica con la aplicación Android mediante Retrofit. La API procesa las solicitudes, valida la información y gestiona la comunicación con SQLite para almacenar los datos. Las fotografías se guardan en el almacenamiento privado de la aplicación.

# EJECUCIÓN BÁSICA DE LOS ENDPOINTS

Al iniciar la aplicación, se levanta el servidor Ktor y queda disponible para recibir solicitudes HTTP. Retrofit funciona como cliente y se encarga de enviar las peticiones a los endpoints correspondientes.

- El endpoint POST /auth/register permite registrar un usuario mediante el envío de su nombre, correo electrónico y contraseña en formato JSON. Si el registro es exitoso, devuelve la información correspondiente y un token de sesión.
- El endpoint POST /auth/login recibe el correo electrónico y la contraseña del usuario. Si las credenciales son correctas, permite iniciar sesión y devuelve un token de autenticación.
- El endpoint POST /auth/logout permite cerrar la sesión actual e invalidar el token utilizado.
- El endpoint GET /auth/me obtiene la información del usuario autenticado.
- El endpoint POST /auth/avatar permite establecer una fotografía de perfil, mientras que DELETE /auth/avatar permite eliminarla.
- El endpoint GET /recipes obtiene las recetas pertenecientes al usuario autenticado.
- El endpoint POST /recipes permite crear una receta enviando sus datos en formato JSON.
- El endpoint GET /recipes/ permite consultar los detalles de una receta específica mediante su identificador.
- El endpoint PUT /recipes/ permite actualizar la información de una receta existente.
- El endpoint DELETE /recipes/ permite eliminar una receta y sus fotografías asociadas.
- El endpoint POST /recipes//images permite agregar una fotografía a una receta. La imagen se envía como información binaria y el servidor se encarga de procesarla y almacenarla.
- El endpoint DELETE /recipes//images/ permite eliminar una fotografía específica de una receta.
- El endpoint GET /recipes//export genera un archivo ZIP que contiene la información de la receta en formato JSON y sus fotografías.
- El endpoint POST /recipes/import recibe un archivo ZIP, valida su contenido y crea una nueva receta independiente con sus respectivas fotografías.

# INSTRUCCIONES GENERALES DE EJECUCIÓN

Las solicitudes que intercambian información estructurada utilizan el formato JSON. Las operaciones protegidas requieren enviar el token de autenticación en la cabecera Authorization utilizando el formato Bearer seguido del token correspondiente.

Para crear o actualizar recetas, se envían los datos necesarios en el cuerpo de la solicitud. Las fotografías se transmiten como información binaria y no como texto JSON. Cuando un endpoint utiliza un identificador, como , debe sustituirse por el identificador real del recurso.

La API verifica que las recetas consultadas o modificadas pertenezcan al usuario autenticado. También utiliza códigos de respuesta HTTP para indicar el resultado de las operaciones, como 201 para una creación exitosa, 400 para solicitudes inválidas, 401 para falta de autenticación y 404 cuando el recurso no se encuentra disponible.
