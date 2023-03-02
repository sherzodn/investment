FROM adoptopenjdk/openjdk11:alpine-jre

# Set the working directory to /app
WORKDIR /app

# Copy the executable jar file
COPY target/investment-0.0.1-SNAPSHOT.jar /app

# Expose port 8080
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "investment-0.0.1-SNAPSHOT.jar"]
