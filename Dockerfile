# Multi-stage Docker build for Java 17 + Embedded Tomcat
FROM maven:3.9-eclipse-temurin-17
WORKDIR /app

# Cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and compile
COPY src ./src
RUN mvn clean test-compile -DskipTests

# Default web port (overridden by cloud platforms via $PORT)
ENV PORT=8080
EXPOSE 8080

# Start Embedded Tomcat
CMD ["mvn", "exec:java", "-Dexec.classpathScope=test", "-Dexec.mainClass=com.campus.lostfound.EmbeddedServer"]
