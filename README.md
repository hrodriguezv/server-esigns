# Instrucciones para el setup del ambiente de desarrollo

### Pre-requisitos: Software requerido para el funcionamiento del tablet
- DisplayLink.exe ->[here](https://www.displaylink.com/downloads/windows)</br>
- Wacom-Signature-SDK-x86-4.0.1.msi -> [escoger versión que corresponda a la arq. de la maquina 32 o 64 bits](https://developer-docs.wacom.com/display/DevDocs/Download+the+SDK+and+useful+tools)</br>
- WacomTablet_6.3.35-2.exe -> [here](https://www.wacom.com/es-es/support/product-support/drivers)</br></br>

# 

1) En el archivo pom.xml se deben configurar correctamente las propiedades ${consultec.workspace.path} con el path absoluto de la raiz en donde fueron clonados los proyectos. La propiedad ${icepdf.target.folder} debe indicar el path donde fue clonado el proyecto icepdf.

2) Cambios requeridos para **com.consultec.esigns.listener**. 
Modificar los siguientes parametros dentro del application.properties

- user.default.basehome=/Consultec/workspace `Ruta absoluta hacia el directorio temporal para el uso de archivos a firmar`
- user.default.icepdfjarpath=C:\\Consultec\\distributions\\client `Ruta absoluta hacia el directorio que contiene los binarios del cliente`
- user.default.icepdfdllpath=C:\\Consultec\\distributions\\client\\WacomGSS > `Ruta absoluta hacia el directorio que contiene los binarios del driver Wacom`

3) Cambios requeridos para **com.consultec.esigns.strokes.impl.wacom**. 
Es necesario instalar en el .m2 local, los binarios con el API de Wacom para la generacion de trazos. Generalmente se consiguen en el directorio *C:\Program Files\Common Files\WacomGSS*, una vez instalado el SDK de Wacom. Para instalarlo se deben ejecutar los siguientes comandos maven:

- mvn install:install-file -Dfile=flsx-1.0.jar -DgroupId=com.florentis -DartifactId=fslx -Dversion=1.0 -Dpackaging=jar
- mvn install:install-file -Dfile=wgssLicenceJNI-1.0.jar -DgroupId=com.florentis.licence -DartifactId=wgssLicenceJNI -Dversion=1.0 -Dpackaging=jar

# 

# PUNTO DE DECISIÓN - Caso JCAPI - En el caso de resolver el problema con Pheox, se deben seguir los siguientes pasos para poder utilizarla en el proyecto.

Descargar el instalador con la libreria 
- JCAPI ->[here](pheox.com/download)</br>

Agregar el proveedor de criptografia fuerte al listado de opciones en el archivo java.security ubicado en %JAVA_HOME%/jre/lib/security
</br> Ejemplo:

security.provider.1=sun.security.provider.Sun </br>
security.provider.2=sun.security.rsa.SunRsaSign </br>
security.provider.3=sun.security.ec.SunEC </br>
security.provider.4=com.sun.net.ssl.internal.ssl.Provider </br>
security.provider.5=com.sun.crypto.provider.SunJCE </br>
security.provider.6=sun.security.jgss.SunProvider </br>
security.provider.7=com.sun.security.sasl.Provider </br>
security.provider.8=org.jcp.xml.dsig.internal.dom.XMLDSigRI </br>
security.provider.9=sun.security.smartcardio.SunPCSC </br>
security.provider.10=sun.security.mscapi.SunMSCAPI </br>
**security.provider.11=com.pheox.jcapi.JCAPIProvider** </br>

Para que la JVM consiga el proveedor en el classpath de la maquina, se debe copiar la librería al directorio "%JAVA_HOME%\jre\lib\ext\".
Ejemplo: </br>
copy JCAPI.jar "%JAVA_HOME%\jre\lib\ext\"

Se debe instalar la libreria JCAPI en el .m2 local para que pueda compilar el código fuente
- mvn install:install-file -Dfile=JCAPI-2.2.0.jar -DgroupId=com.pheox.jcapi -DartifactId=jcapi -Dversion=2.2.0 -Dpackaging=jar

>`'En el supuesto de que no se vaya a utilizar este proveedor, se deben remover referencias en los pom.xml y en las clases *com.consultec.esigns.listener.ServerApp* del proyecto Listener, asi como en el pom.xml y la clase *com.consultec.esigns.core.security.KeyStoreAccessMode* del proyecto core.`'