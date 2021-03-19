# Debug con quarkus y k8s

######  Instalar las dependecias

```
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-kubernetes</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-container-image-docker</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-config-yaml</artifactId>
</dependency>
```
Moficicar la extension del archivo application.properties a application.yaml

######  Pegar el siguiente contenido
```
quarkus:
    package:
        type: mutable-jar
    live-reload:
        password: abc123 #password para la conexion remote-dev
    container-image:
        build: true # contruye la imagen
        push: true # push de la imagen a dockerhub
        group: wlopezob #nombre del usuario de dockerhub, nuestras imagenes tendran el prefijo wlopezob/miimagen
    kubernetes:
        namespace: default
        replicas: 1
        #node-port: 30001
        ports:
            https:
                container-port: 8443
            debug:
                container-port: 5005 #puerto para debug
        deployment-target: kubernetes
        env:
            vars:
                quarkus-launch-devmode: true #variable de entorno para habilitar la conexion con remote-dev 
                java-enable-debug: true #variable degugger 
                #custom.message: k8s-local-variable
            configmaps: config-ms-servicios-juntos
        service-type: node-port #el servicio de tipo NodePort para poder conectarnos con remote-dev 
    kubernetes-client:
        trust-certs: true
      

mconfiguracion:
    mensaje: ${MCONFIGURATION.MENSAJE:"hola"}
mconfiguracion:
    mensaje02: mensaje02
```

######  Creamos el ConfigMap (opcional, pruebas de quarkus con configmap)
```
apiVersion: v1
kind: ConfigMap
metadata:
  name: config-ms-servicios-juntos
  labels:
    tier: dev
data:
  mconfiguracion.mensaje: k8s-config
```
###### Compilamos y creamos nuestro POD y Service en kubernetes
```
mvn clean install -DskipTests -Dquarkus.kubernetes.deploy=true
```
## Profile dev activated. Live Coding activated
###### Podemos listar el POD creado, tambien revisar el log donde se pueda verificar que el **Profile dev activated. Live Coding activated** este activado
```
kubectl get po
kubectl logs ms-servicios-juntos-6d5b5fb857-2h445

2021-03-18 20:53:21,315 WARN  [io.qua.kub.dep.KubernetesProcessor] (build-14) No project was detected, skipping generation of kubernetes manifests!
2021-03-18 20:53:21,953 INFO  [io.quarkus] (Quarkus Main Thread) ms-servicios-juntos 1.0.0-SNAPSHOT on JVM (powered by Quarkus 1.12.2.Final) started in 0.962s. Listening on: http://0.0.0.0:8080
2021-03-18 20:53:21,953 INFO  [io.quarkus] (Quarkus Main Thread) Profile dev activated. Live Coding activated.
2021-03-18 20:53:21,954 INFO  [io.quarkus] (Quarkus Main Thread) Installed features: [cdi, config-yaml, kubernetes, resteasy]
2021-03-18 20:53:21,954 INFO  [io.qua.dep.dev.RuntimeUpdatesProcessor] (vert.x-worker-thread-0) Hot replace total time: 2.590s
```
###### Revisamos el puerto del servicio
kubectl get svc
```
NAME                      TYPE           CLUSTER-IP       EXTERNAL-IP              PORT(S)                                                     AGE      
kubernetes                ClusterIP      10.96.0.1        <none>                   443/TCP                                                     31d       
ms-servicios-juntos       NodePort       10.104.99.59     <none>                   8080:31574/TCP,8443:32623/TCP,5005:30379/TCP                8h       
```
###### Establecemos la conexion quarkus:remote-dev
```
mvn quarkus:remote-dev -Ddebug=false -Dquarkus.package.type=mutable-jar -Dquarkus.live-reload.url=http://localhost:31574 -Dquarkus.live-reload.password=abc123
```

###### attach la conexion y task remota al hostname: localhost con el puerto NodePort 30379 
```
launch.json
{
    // Use IntelliSense to learn about possible attributes.
    // Hover to view descriptions of existing attributes.
    // For more information, visit: https://go.microsoft.com/fwlink/?linkid=830387
    "version": "0.2.0",
    "configurations": [

        {
            "type": "java",
            "name": "Attach to Remote Program",
            "request": "attach",
            "hostName": "localhost",
            "port": 30379
        },
        {
            "preLaunchTask": "quarkus:remote-dev",
            "type": "java",
            "request": "attach",
            "hostName": "localhost",
            "name": "Remote Debug Quarkus application",
            "port": 30379
        }
    ]
}

tasks.json
{
    // See https://go.microsoft.com/fwlink/?LinkId=733558
    // for the documentation about the tasks.json format
    "version": "2.0.0",
    "tasks": [
        {
			"label": "quarkus:remote-dev",
			"type": "shell",
			"command": "./mvnw quarkus:remote-dev -Ddebug=false -Dquarkus.package.type=mutable-jar -Dquarkus.live-reload.url=http://localhost:31574  -Dquarkus.live-reload.password=abc123",
			"windows": {
				"command": ".\\mvnw.cmd quarkus:remote-dev -Ddebug=false -Dquarkus.package.type=mutable-jar -Dquarkus.live-reload.url=http://localhost:31574  -Dquarkus.live-reload.password=abc123"
			},
			"isBackground": true,
			"problemMatcher": [
				{
					"pattern": [
						{
							"regexp": "\\b\\B",
							"file": 1,
							"location": 2,
							"message": 3
						}
					],
					"background": {
						"activeOnStart": true,
						"beginsPattern": "^.*Scanning for projects...*",
						"endsPattern": "^.*Quarkus augmentation completed in *"
					}
				}
			]
		}
    ]
}
```
###### Para Dquarkus.live-reload.url se podria crear un proxy-reverse como Traefik para que siempre sea la misma URL, no encontre la configuracion del node-port para el puerto del debug en la configuracion de Service del application.yaml mediante el uso de quarkus-kubernetes, es por ello que tendremos obtenerlo manualemente del NodePort del servicio en k8s hasta encontrar una solucion 