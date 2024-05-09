FROM openjdk:8-alpine

COPY target/uberjar/strapub.jar /strapub/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/strapub/app.jar"]
