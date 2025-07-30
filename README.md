# 🌐 Open Hope - Open Banking Integration Core API

## 📝 Description

Open Hope is a Core API designed to streamline integration with multiple open banking providers. Built with Java Spring Boot and backed by MySQL, it provides a flexible foundation for financial data connectivity across services.

## 🏦 Open Banking Providers Supported

1. 💳 [Redsys](https://redsys.es/) – Spanish payment gateway for secure transactions

## 🧰 Tech Stack

- Java 21
- Spring Boot
- Maven
- MySQL
- JPA / Hibernate

## 🔧 Installation

### Prerequisites

- Java 21+
- Maven 3.8+
- MySQL Server
- IDE (IntelliJ IDEA recommended)

### 📥 Clone the repository

```bash
git clone https://github.com/AdrianSoutoDev/openhopebackend.git
cd openhopebackend
```

### 🛠️ Configure `application.properties`

1. Copy the example configuration file:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

2.  Set the providers ids and secrets

        jwt.secret={your_secret}
        redsys.client.id={your_redsys_client_id}

3.  Set the oauth callback uri with your host:

        redsys.oauth.callback.uri={your_api_host}/api/providers/oauth/callback

4.  Set your own challenge y code verifier

        redsys.oauth.challenge={your_challenge}
        redsys.oauth.code.verifier={code verifier}

    [more info](https://market.apis-i.redsys.es/psd2/xs2a/nodos/oauth2-tokengen)

5.  set your redsys private key and certificate paths

        redsys.rsa.privateKey.file.path=keys/redsys/privkey.pem
        redsys.rsa.certificate.file.path=keys/redsys/certificate.pem

    [more info](https://market.apis-i.redsys.es/psd2/xs2a/nodos/altacertificado)

    ⚠️ To convert the private key from PKCS#8 to PKCS#1 format, copy the key provided by Redsys into [this RSA converter](https://decoder.link/rsa_converter) Then, copy everything except the headers of the converted key and replace the body of your original key with it, keeping the original headers unchanged.

### 🗄️ Configure Database

(not needed for tests)

    CREATE DATABASE openhope;

1.  run script:

        scripts/createCategories.sql

2.  modify application.properties:

        spring.datasource.url=jdbc:mysql://localhost:3306/openhope
        spring.datasource.username=your_user
        spring.datasource.password=your_password

## ⚙️ Build and Run the Application

```bash
mvn clean install -DskipTests=true
mvn spring-boot:run
```

## 🧪 Running Tests

```bash
mvn test
```

## ✍️ Authors

- **[Adrián Souto](https://github.com/AdrianSoutoDev)** — Project creator

## 📄 License

This project is licensed under the [MIT License](https://github.com/AdrianSoutoDev/openhopebackend/blob/main/LICENSE).
