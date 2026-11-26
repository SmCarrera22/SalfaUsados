# Despliegue Salfa360

Los microservicios se empaquetan mediante Gradle BootJar
y se ejecutan como servicios systemd en Amazon EC2.

## Administrador Kafka

- Java 25.
- Puerto administrativo 8084.
- Binding exclusivo a 127.0.0.1.
- Acceso administrativo mediante tunel SSH.
- Sin base de datos propia.
- Conexion mediante Apache Kafka AdminClient.

Variables de entorno requeridas:

| Variable | Descripcion |
|---|---|
| KAFKA_BOOTSTRAP_SERVERS | Direcciones privadas de los tres brokers |
| SERVER_ADDRESS | 127.0.0.1 |
| SERVER_PORT | 8084 |

El archivo de variables se almacena fuera del repositorio:

/etc/salfa360/ms-kafka-admin.env

La unidad systemd se encuentra en el directorio systemd.

No se deben publicar credenciales, tokens, claves SSH
ni archivos de configuracion privados en GitHub.
