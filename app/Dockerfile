# =====================================================
# 家計簿アプリ 実技試験 - アプリケーション Dockerfile
# =====================================================
# 【使い方】
#   1. このファイルを Spring Boot プロジェクトのルートに置く
#   2. docker compose up --build で起動
# =====================================================

FROM eclipse-temurin:17-jdk AS build

WORKDIR /workspace

# Maven Wrapper を使用する場合
COPY mvnw mvnw
COPY .mvn .mvn
COPY pom.xml pom.xml

# 依存関係を先にキャッシュ（ソース変更時の再ビルドを高速化）
RUN ./mvnw dependency:go-offline -B

# ソースをコピーしてビルド
COPY src src
RUN ./mvnw package -DskipTests -B

# ===== 実行ステージ =====
FROM eclipse-temurin:17-jre

WORKDIR /app

# ビルド成果物をコピー
COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
