# 📱 VentaExpress

Sistema de ventas con tematica **medicamentos** desarrollado en **Android Studio con Kotlin**, como parte del **Segundo Desafío Práctico** de la materia *Desarrollo de Software para Móvil* en la **Universidad Don Bosco**.

---

## 🚀 Características principales
- **Autenticación con Firebase Authentication** (correo/contraseña, GitHub y Facebook).  
- **Gestión de productos**: agregar, editar, eliminar y listar con RecyclerView.  
- **Gestión de clientes**: registrar y administrar información básica.  
- **Registro de ventas**: seleccionar productos y clientes, calcular total y guardar historial.  
- **Base de datos en Firebase Realtime Database**, organizada por UID de cada empleado.  
- **Arquitectura MVC** para separar vistas, controladores y modelos.

---

## 🛠️ Tecnologías utilizadas
- **Lenguaje:** Kotlin  
- **IDE:** Android Studio  
- **Base de datos:** Firebase Realtime Database  
- **Autenticación:** Firebase Authentication  
- **UI:** RecyclerView, CardView, Material Design  

---

## 📂 Estructura del proyecto
/app  
├── java/com/example/desafio02_dsm  
│ ├── models/ # Data classes (Productos, Clientes, Ventas)  
│ ├── views/ # Activities (Login, Dashboard, Productos, Clientes, Ventas)  
│ └── controllers/ # Lógica de Firebase y validaciones  
└── res/  
├── layout/ # Vistas XML  
├── values/ # strings.xml, colors.xml, dimens.xml  
└── drawable/ # Recursos gráficos  


---

## 📸 Capturas de pantalla

---

## 🎥 Video demostrativo
👉 

---

## 👨‍💻 Integrantes
- Josué Eduardo García Estrada     | GE240098  
- Jeremy Edenilson Flores Portillo | FP240479  
- Edgar Josué Gómez Meléndez       | GM240279  

---

## 📌 Requisitos de instalación
1. Clonar este repositorio.  
2. Abrir el proyecto en Android Studio.  
3. Configurar Firebase Authentication y Realtime Database en el proyecto.  
4. Ejecutar en un dispositivo físico o emulador con API mínima requerida.
