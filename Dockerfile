FROM eclipse-temurin:22.0.2_9-jdk-jammy AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw && \
    ./mvnw -B -ntp dependency:go-offline
COPY src/ src/
RUN ./mvnw -B -ntp -DskipTests package

FROM eclipse-temurin:22.0.2_9-jre-jammy
WORKDIR /app
RUN groupadd --system spring && \
    useradd --system --gid spring spring
COPY --from=build --chown=spring:spring \
    /workspace/target/phone_shop-0.0.1-SNAPSHOT.jar \
    /app/app.jar
USER spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
