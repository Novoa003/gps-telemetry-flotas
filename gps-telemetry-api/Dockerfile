# ---------- Etapa 1: build ----------
# Usamos una imagen con Maven + JDK 21 solo para compilar el proyecto.
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copiamos primero el pom.xml y descargamos dependencias por separado.
# Esto aprovecha el cache de capas de Docker: si solo cambia el código
# (no las dependencias), Docker no vuelve a descargar todo de internet
# en cada build, lo cual ahorra tiempo significativo en rebuilds.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Ahora copiamos el código fuente y compilamos el .jar final.
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---------- Etapa 2: runtime ----------
# Imagen final, mucho más liviana: solo el JRE (no el JDK completo ni Maven),
# necesario únicamente para EJECUTAR el .jar ya compilado.
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiamos únicamente el .jar generado en la etapa de build.
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]