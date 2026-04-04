# External Config

This directory is for local or server-side secret config files that must not be committed.

Create these files from the examples:

- `config/alipay.yml`
- `config/cos.yml`

Profiles:

- `local`: default for local development
- `prod`: production-safe defaults

Environment variables:

- `SPRING_PROFILES_ACTIVE=local|prod`
- `APP_CONFIG_DIR=./config` or your server config directory

Examples:

```powershell
Copy-Item config/alipay.example.yml config/alipay.yml
Copy-Item config/cos.example.yml config/cos.yml
```

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:APP_CONFIG_DIR="C:\deploy\book-mall\config"
java -jar .\target\book-mall-backend-0.0.1-SNAPSHOT.jar
```
