# 🌿 Greenhouse

O Greenhouse é um sistema multiplataforma para gerenciamento de uma floricultura. A aplicação permite administrar a gestão da floricultura em uma única plataforma.

<br>

## Tecnologias

* **Kotlin**
* **Kotlin Multiplatform**
* **Compose Multiplatform**
* **Android**
* **iOS**
* **Web**
* **Desktop**

<br>

## Instalação

Clone o projeto:
**
```bash
git clone <URL_DO_REPOSITÓRIO>
```

Entre no diretório do projeto:

```bash
cd Greenhouse
```

Compile o projeto:

```bash
./gradlew build
```

<br>

## Executando a aplicação

### Android

Para gerar a versão de desenvolvimento:

```bash
./gradlew :androidApp:assembleDebug
```

Para instalar diretamente em um dispositivo ou emulador Android:

```bash
./gradlew :androidApp:installDebug
```

### Desktop

Para executar a aplicação Desktop:

```bash
./gradlew :desktopApp:run
```

Para utilizar hot reload:

```bash
./gradlew :desktopApp:hotRun --auto
```

### Web

Para executar a versão Web utilizando Kotlin/Wasm:

```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

Caso seja necessário utilizar o target JavaScript:

```bash
./gradlew :webApp:jsBrowserDevelopmentRun
```

### iOS

A aplicação iOS possui um projeto próprio localizado em:

```text
/iosApp
```

Abra o projeto no **Xcode** e execute a aplicação através do ambiente de desenvolvimento.

<br>