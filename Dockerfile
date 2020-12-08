FROM openjdk:8-jdk-alpine as builder

RUN apk add maven

COPY . /app
RUN cd /app && mvn clean package

# =========================================
FROM openjdk:8-jdk-alpine

WORKDIR /app
RUN adduser -D -h /home/app app
USER app

COPY --from=builder /app/target/encounters-mdi-kepler-test-1.0-SNAPSHOT.jar /app/

ENTRYPOINT ["java", "-jar", "encounters-mdi-kepler-test-1.0-SNAPSHOT.jar"]
