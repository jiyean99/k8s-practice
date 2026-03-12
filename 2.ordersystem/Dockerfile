FROM eclipse-temurin:17-jdk-alpine as stage1
WORKDIR /app
COPY gradle gradle
COPY src src
COPY build.gradle .
COPY gradlew .
COPY settings.gradle .

RUN chmod +x gradlew
RUN ./gradlew bootJar

# 두번째 스테이지: 이미지 경량화를 위해 스테이지 분리 작업
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=stage1 /app/build/libs/*.jar ordersystem.jar
ENTRYPOINT [ "java", "-jar", "ordersystem.jar" ]

EXPOSE 8080

# [참고용] Docker 이미지 빌드 명령어 
# docker build -t jiyeanlee/myordersystem:latest .

# 도커 컨테이너 실행 (*환경변수 주입 주의)
# - SPRING_DATASOURCE_URL(host.docker.internal) -> RDS 주소로 대치 필요
# - SPRING_DATASOURCE_USERNAME(root) -> RDS user name으로 대치 필요
# - SPRING_DATASOURCE_PASSWORD(test1234) -> RDS pw로 대치 필요
# - SPRING_REDIS_HOST(host.docker.internal) -> Elasticache host로 대치 필요

# sudo docker run --name myspring -d -p 8080:8080 -e SPRING_DATASOURCE_URL=jdbc:mariadb://host.docker.internal:3306/order_system?useSSL=true -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=test1234 -e SPRING_REDIS_HOST=host.docker.internal jiyeanlee/myordersystem:latest

