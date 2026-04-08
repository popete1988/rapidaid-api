# RapidAid Disease API

Backend en Spring Boot (Java 17) que proporciona información médica estructurada
(descripción, síntomas, tratamiento) en español para la app Android RapidAid.

## Cómo desplegarlo en Railway (paso a paso)

### 1. Subir a GitHub
1. Crea un repositorio nuevo en github.com (ej: `rapidaid-api`)
2. Abre una terminal en la carpeta `RapidAidAPI` y ejecuta:
```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/TU_USUARIO/rapidaid-api.git
git push -u origin main
```

### 2. Desplegar en Railway
1. Ve a railway.app e inicia sesión con tu cuenta GitHub
2. Haz clic en **"New Project"**
3. Selecciona **"Deploy from GitHub repo"**
4. Elige el repositorio `rapidaid-api`
5. Railway detectará automáticamente que es un proyecto Maven/Java
6. Haz clic en **"Deploy"** — tardará 2-3 minutos
7. Ve a **Settings → Networking → Generate Domain**
8. Copia la URL que te da (ej: `https://rapidaid-api-production.up.railway.app`)

### 3. Probar que funciona
Abre en el navegador:
```
https://TU_URL.up.railway.app/api/v1/health
https://TU_URL.up.railway.app/api/v1/disease?name=meningitis
```

### 4. Actualizar la app Android
En el archivo `NetworkModule.kt` de la app Android, cambia la URL base a la tuya de Railway.

## Endpoints

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/api/v1/disease?name=ENFERMEDAD` | Busca una enfermedad |
| GET | `/api/v1/health` | Comprueba que el servidor está activo |

## Ejemplo de respuesta
```json
[
  {
    "name": "Meningitis",
    "description": "La meningitis es la inflamación de las membranas...",
    "symptoms": "Fiebre repentina, dolor de cabeza intenso, rigidez de nuca...",
    "treatment": "La meningitis bacteriana se trata con antibióticos...",
    "source": "MedlinePlus NLM"
  }
]
```
