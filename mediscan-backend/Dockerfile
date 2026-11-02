
# Stage 1: Build the Spring Boot application
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -Dmaven.test.skip=true

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Install Tesseract OCR and English language data
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    tesseract-ocr \
    tesseract-ocr-eng && \
    rm -rf /var/lib/apt/lists/*

# Verify installation
RUN tesseract --version && \
    dpkg -L tesseract-ocr-eng | grep eng.traineddata && \
    tesseract --list-langs

# Tesseract configuration
ENV TESSERACT_PATH=/usr/share/tesseract-ocr/4.00/tessdata
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/4.00/tessdata
ENV TESSERACT_LANGUAGE=eng


RUN echo "Checking Tesseract installation..." && \
    tesseract --version && \
    dpkg -L tesseract-ocr-eng | grep eng.traineddata && \
    tesseract --list-langs

# Copy the generated Spring Boot JAR
COPY --from=build /app/target/*.jar app.jar

# Start Spring Boot using Render's assigned port
CMD ["sh", "-c", "java -Dserver.port=${PORT:-10000} -jar app.jar"]
