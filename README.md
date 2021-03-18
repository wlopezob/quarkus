# Debug con quarkus y k8s

### Instalar las dependecias

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

### Pegar el siguiente contenido
```
quarkus:
    package:
        type: mutable-jar
    live-reload:
        password: abc123 #password para la conexion remote-dev
    container-image:
        build: true 
        push: true
        group: wlopezob
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
                quarkus-launch-devmode: true
                java-enable-debug: true
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