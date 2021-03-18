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
###### Podemos verificar los POD creados, revisar el log donde se pueda verificar que el profile dev este activado
```
kubectl get po
kubectl logs ms-servicios-juntos-6d5b5fb857-2h445

2021-03-18 20:53:21,315 WARN  [io.qua.kub.dep.KubernetesProcessor] (build-14) No project was detected, skipping generation of kubernetes manifests!
2021-03-18 20:53:21,953 INFO  [io.quarkus] (Quarkus Main Thread) ms-servicios-juntos 1.0.0-SNAPSHOT on JVM (powered by Quarkus 1.12.2.Final) started in 0.962s. Listening on: http://0.0.0.0:8080
2021-03-18 20:53:21,953 INFO  [io.quarkus] (Quarkus Main Thread) **Profile dev activated. Live Coding activated.**
2021-03-18 20:53:21,954 INFO  [io.quarkus] (Quarkus Main Thread) Installed features: [cdi, config-yaml, kubernetes, resteasy]
2021-03-18 20:53:21,954 INFO  [io.qua.dep.dev.RuntimeUpdatesProcessor] (vert.x-worker-thread-0) Hot replace total time: 2.590s
```