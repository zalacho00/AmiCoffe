# ☕ Proyecto AmiCoffe

## 🛠️ Tecnologías Usadas

El sistema *AmiCoffe* fue construido utilizando las siguientes tecnologías:

*   *Página Web*:
    *   *React* (Librería principal para crear la interfaz de usuario).
    *   *Vite* (Herramienta súper rápida de empaquetado para correr la web).
    *   HTML5 y CSS.
*   *Aplicación Móvil*:
    *   *Android Studio* (Entorno de desarrollo oficial).
    *   *Kotlin* (Lenguaje de programación en el que está construida la app móvil).
*   *Backend y Base de Datos*:
    *   *Firebase Firestore* (Base de datos en tiempo real de Google).
    *   *Firebase Cloud Functions* (El código de nuestro servidor escrito en *Node.js* que corre automáticamente en la nube).
    *   *Firebase Hosting & Auth* (Para alojar la web y manejar el inicio de sesión).

¡Bienvenido al código fuente de AmiCoffe! Este proyecto es un sistema completo que incluye una **aplicación móvil** para celulares, una **página web** para administrar todo, y un **cerebro en la nube** que conecta ambas partes. 

Aquí te explicamos de manera sencilla cómo está organizado todo nuestro código.

## 🗂️ Estructura General del Proyecto

El proyecto está dividido en 3 grandes partes principales, cada una en su propia carpeta:

### 1. 📱 `android-app/` (La Aplicación Móvil)
Esta carpeta tiene todo el proyecto diseñado para abrirse en **Android Studio**. Es la app que se instala en los celulares.

*   **`app/src/main/`**: Aquí adentro está el corazón de la aplicación.
    *   **`kotlin/`**: Contiene todo el código lógico de la app. Aquí le decimos a la aplicación qué hacer cuando el usuario toca un botón, cómo pedir los datos, etc.
    *   **`res/`**: Es la carpeta de "Recursos" (Resources). Aquí guardamos todo lo visual: las imágenes, los colores, los íconos y los diseños de las pantallas.
    *   **`AndroidManifest.xml`**: Es como la "cédula de identidad" de la app. Le dice al celular cómo se llama la aplicación, qué permisos necesita (como el internet) y cuáles son sus pantallas principales.
*   **`build.gradle.kts` / `settings.gradle.kts`**: Son archivos de configuración o "instrucciones de construcción". Le dicen a Android Studio cómo armar la aplicación para que esté lista para instalarse.

---

### 2. 💻 `web-app/` (La Página Web)
Aquí vive todo el código de nuestra plataforma en internet. Sirve para que los administradores y el equipo de cocina puedan ver y controlar el sistema desde una computadora.

*   **`src/`**: Significa "Source" (Fuente). Aquí está todo el código que escribimos para que la página funcione y se vea bonita.
    *   **`pages/`**: Cada archivo aquí es una "pantalla" o página completa de la web.
        *   **`admin/`**: Las pantallas que solo ven los administradores (para controlar ventas, menús, etc.).
        *   **`kds/`**: Son las pantallas de la cocina (Kitchen Display System), donde los cocineros/baristas ven los pedidos que van llegando.
    *   **`components/`**: Son "piezas de lego" de la web. Cosas que usamos en muchas partes, como un botón especial o un menú de navegación, se guardan aquí para no repetir código.
    *   **`firebase/`**: Los archivos que conectan la página web con nuestra base de datos.
    *   **`assets/`**: Imágenes, logos o fuentes de texto que usa la web.
    *   **`index.css` / `App.css`**: Los archivos de estilo. Aquí le ponemos los colores y decidimos qué tan grande es cada cosa.
*   **`public/`**: Archivos públicos generales de la web, como el ícono pequeño que sale en la pestaña del navegador (`index.html`).
*   **`package.json`**: Es la lista de "ingredientes" extra que necesita la web para funcionar (librerías externas).

---

### 3. ☁️ `functions/` (El "Cerebro" en la Nube)
A veces necesitamos que el sistema haga tareas automáticas por su cuenta, sin que la app o la web se lo pidan. Para eso sirve esta carpeta.

*   **`index.js`**: Aquí escribimos pequeñas funciones de código que viven en internet (en Firebase). Por ejemplo: enviar una notificación cuando el café está listo, o calcular los totales del día automáticamente.
*   **`package.json`**: Al igual que en la web, son los ingredientes o herramientas que necesitan nuestras funciones en la nube.

---

### ⚙️ Archivos Sueltos (Configuraciones Generales)
En la carpeta principal del proyecto (afuera de las 3 carpetas grandes) verás unos archivos extra. La mayoría son para que el proyecto se comunique con **Firebase** (la plataforma de Google que usamos para guardar datos en internet).

*   **`firebase.json` y `.firebaserc`**: Guardan la información de conexión a nuestra cuenta en la nube.
*   **`firestore.rules`**: Son las reglas de seguridad. Aquí decidimos quién puede ver o cambiar información (por ejemplo, evitar que alguien que no es admin borre productos).
*   **`firestore.indexes.json`**: Ayuda a que las búsquedas de información en la base de datos sean súper rápidas.
*   **`.gitignore`**: Una lista que le dice a GitHub qué archivos **no** debe subir a internet (como contraseñas o archivos muy pesados que se pueden volver a generar).
