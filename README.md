# Ferrefix Backend

Java 21, Spring Boot 4.1.2 y MySQL 8.4.

1. Completar `.env` con `DB_PASSWORD`, `CATALOGO_DB_PASSWORD`, `ORDENES_DB_PASSWORD`, `ARRIENDOS_DB_PASSWORD`, `MYSQL_ROOT_PASSWORD` y `JWT_SECRET` (Base64 de 32 bytes aleatorios). Para las cuentas iniciales, configurar también las contraseñas `SEED_*`.
2. Generar los JAR de los cinco servicios con Java 21, sin ejecutar tests:

   ```powershell
   .\empaquetar.bat
   ```

3. Si ya existen máquinas o contratos en `ordenes`, seguir primero la migración descrita abajo. En una instalación nueva, iniciar Docker y ejecutar `docker compose up --build --remove-orphans -d`.
4. API: `http://localhost:8080/api/v1`. Swagger: `/swagger-ui/index.html` con `API_DOCS_ENABLED=true`.

Cada Dockerfile utiliza `eclipse-temurin:21-jre` y copia el JAR de su propia carpeta
`target`; Docker no compila Java. Después de cambiar código, vuelve a generar el
JAR antes de reconstruir la imagen. `.dockerignore` permite incluir ese archivo.

`empaquetar.bat` entra con `cd` en auth, catálogo, órdenes, arriendos y gateway,
y ejecuta `call mvnw.cmd clean package -Dmaven.test.skip=true` en cada uno.
Maven `clean` elimina el contenido generado en `target` antes de empaquetar.
El script se detiene ante el primer error y devuelve un código de salida distinto
de cero. Puede invocarse desde otra carpeta y restaura el directorio inicial al terminar.

| Servicio Compose | Contenedor | Puerto local | Base MySQL / puerto local |
| --- | --- | --- | --- |
| `api-gateway` | `ferrefix-gateway` | 8080 | — |
| `ms-auth` | `ferrefix-ms-auth` | 8081 | `ferrefix_auth` / 3306 (`MYSQL_PORT`) |
| `ms-catalogo` | `ferrefix-ms-catalogo` | 8082 | `ferrefix_catalogo` / 3307 |
| `ms-ordenes` | `ferrefix-ms-ordenes` | 8083 | `ferrefix_ordenes` / 3308 |
| `ms-arriendos` | `ferrefix-ms-arriendos` | 8084 | `ferrefix_arriendos` / 3309 |

`ordenes` administra carritos, pedidos con pago simulado y cotizaciones.
`arriendos` administra máquinas, especificaciones, disponibilidad y contratos,
con autenticación, permisos y cierre automático de contratos vencidos. Ambos
servicios se inician con el Compose normal y tienen su propia base de datos.
Liquibase crea el esquema de arriendos y, en instalaciones nuevas, cuatro máquinas
iniciales. Detalles en [órdenes](docs/ordenes.md) y [arriendos](docs/arriendos.md).

El gateway enruta auth, catálogo, órdenes y arriendos mediante `MS_AUTH_URI`,
`MS_CATALOGO_URI`, `MS_ORDENES_URI` y `MS_ARRIENDOS_URI`. Los puertos publicados están ligados a
`127.0.0.1`. Cada MySQL conserva su volumen y usuario propio; las contraseñas se
leen de `.env`. Los volúmenes anteriores de auth, catálogo e imágenes mantienen
sus nombres para conservar los datos al cambiar los nombres de servicios.

APIs implementadas: registro, JWT, perfil, productos, desactivación, carga de imágenes,
maquinaria, contratos de arriendo, carritos, pedidos con pago simulado, cotizaciones
y ofertas del generador. Ver [persistencia comercial](docs/comercio.md).

Los 100 productos iniciales se cargan con Liquibase (18 originales y 82 adicionales).
Auth y catálogo usan `src/main/resources/db/changelog/db.changelog-master.sql`;
la estructura y los datos del catálogo están escritos en SQL formateado de Liquibase.
Auth crea un administrador y dos clientes con las variables `SEED_*` de `.env`,
sin modificar cuentas existentes. Detalles en [datos iniciales](docs/datos-iniciales.md).
La autenticación usa correo y contraseña con sesiones JWT.
Se conserva la [guía de Google](docs/google-oauth.md) como referencia histórica;
esa integración fue retirada del código y de la configuración.
Las fotos de `media/productos` y `media/maquinarias` se copian al volumen persistente
`catalogo-imagenes` y se sirven en `http://localhost:8080/media/<carpeta>/<archivo>`.
Vite redirige `/media` al gateway; en producción configurar `VITE_MEDIA_URL`.
Los banners de diseño permanecen en el frontend. Las ofertas del generador guardan
su configuración en MySQL y sus imágenes en el almacenamiento persistente del backend.
Completar también `CATALOGO_DB_PASSWORD` en `.env`. Detalles de endpoints en [catálogo](docs/catalogo.md).

Microservicios: `application.properties`. Gateway: `application.yml`.

## Organización del código

Los cuatro servicios de negocio usan los mismos paquetes bajo `ferrefix.<servicio>`:

| Paquete | Responsabilidad |
| --- | --- |
| `model` | Entidades y datos persistidos del dominio |
| `dto` | Datos y validaciones de entrada, respuestas y contratos entre servicios |
| `exception` | Conversión de errores a respuestas HTTP |
| `repository` | Consultas JPA o SQL y persistencia |
| `service` | Reglas de negocio, transacciones y clientes de otros servicios |
| `controller` | Rutas HTTP, validación y delegación al servicio |
| `config` | Seguridad, filtros, CORS, OpenAPI e inicialización |

La clase `*Application` permanece en el paquete raíz para descubrir los componentes,
entidades y repositorios. Los controladores no ejecutan SQL ni acceden a repositorios.
El gateway solo configura rutas y seguridad en `config`: no tiene dominio ni base de
datos propios, por lo que no necesita entidades o repositorios vacíos.

## Migrar los arriendos existentes

Realizar una sola vez, durante mantenimiento, **antes del primer inicio del nuevo
`ms-arriendos`**. No es necesario en una instalación nueva. El script requiere Node.js
y Docker; conserva las tablas originales de `ordenes` y genera un respaldo local en
`backups/` (excluido de Git). La base `ferrefix_arriendos` debe estar completamente vacía.

Después de completar `ARRIENDOS_DB_PASSWORD` y empaquetar los servicios:

```powershell
docker compose stop api-gateway ms-ordenes ms-arriendos
docker compose up -d --wait db-ordenes db-arriendos
node scripts/migrar-arriendos.mjs
# Continuar únicamente si la migración termina correctamente:
docker compose up --build --remove-orphans -d
```

El script copia el esquema y los datos de `maquinas`, `maquina_especificaciones` y
`arriendos`, y compara las cantidades de registros. No copia pedidos ni usuarios.
Liquibase reconoce las tablas importadas y omite los datos iniciales cuando ya existen
máquinas. No se modifican los cambios históricos de Liquibase de `ordenes`, para
conservar sus checksums. Sus tablas antiguas de arriendos quedan como respaldo y ya
no reciben escrituras de la aplicación nueva.

Si falla la importación, mantener los servicios detenidos: el esquema MySQL puede haber
quedado parcialmente creado. El script rechaza nuevos intentos sobre una base no vacía;
revisar el respaldo y el destino antes de reintentar. No eliminar el volumen de origen.
Para volver a la versión anterior, restaurar sus JAR e imágenes antes de reiniciar sus
servicios. Después de recibir contratos nuevos en `arriendos`, volver a la versión
anterior requiere reconciliar esos datos primero.

El frontend usa `/api/v1/arriendos/maquinas` y `/api/v1/arriendos/contratos`. El gateway
conserva compatibilidad con `/api/v1/ordenes/maquinas` y `/api/v1/ordenes/arriendos`,
reenviándolas al servicio nuevo. Esa compatibilidad existe en el puerto 8080; las rutas
antiguas ya no están disponibles directamente en `ms-ordenes:8083`.
