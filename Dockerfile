FROM eclipse-temurin:21-jre

RUN sed -i 's|http://archive.ubuntu.com/ubuntu|https://mirror.rackspace.com/ubuntu|g' /etc/apt/sources.list && \
    apt-get update && apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-spa \
    && rm -rf /var/lib/apt/lists/*

ENV TESSDATA_PATH=/usr/share/tesseract-ocr/5/tessdata

WORKDIR /app

COPY target/build-check-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
