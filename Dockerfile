FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn package -DskipTests -B

# Stage 2: Runtime con JRE + Tesseract
FROM eclipse-temurin:21-jre

RUN sed -i 's|http://archive.ubuntu.com/ubuntu|https://mirror.rackspace.com/ubuntu|g' /etc/apt/sources.list && \
    apt-get update && apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-spa \
    && rm -rf /var/lib/apt/lists/*
ENV TESSDATA_PATH=/usr/share/tesseract-ocr/5/tessdata

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]