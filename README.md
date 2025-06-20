﻿# Taller2Paradigmas
README - Plataforma de Evaluación: Taxonomía de Bloom
Descripción General
Este software ofrece una interfaz gráfica en Java que permite realizar y revisar una evaluación compuesta por ítems organizados según la Taxonomía de Bloom. Soporta la importación de preguntas desde un archivo externo, muestra cada ítem al usuario, registra sus respuestas y genera un informe final con un resumen completo, incluyendo análisis y porcentaje de aciertos por categoría y nivel cognitivo.

Funcionalidades Principales
Importación automática de archivo .bloom si se encuentra en la carpeta raíz.
Carga manual de archivo .bloom mediante explorador de archivos.
Presentación ordenada de ítems (opción múltiple y verdadero/falso).
Navegación sencilla entre preguntas (siguiente/anterior).
Almacenamiento de respuestas del usuario.
Resumen en porcentajes de respuestas correctas por:
Tipo de pregunta (MULTIPLE, VERDADERO_FALSO).
Nivel de Bloom (RECORDAR, ENTENDER, etc.).
Revisión de cada ítem con retroalimentación visual.

Instrucciones de Ejecución
Requisitos Previos

SDK: Oracle OpenJDK 23.0.1.
IntelliJ IDEA Community Edition 2024.1.7.
Archivo .bloom con las preguntas de la evaluación, ubicado en el directorio raíz del proyecto (opcional si se carga manualmente).

Ejecución

Compilar y ejecutar Main.java que se encuentra en el paquete principal.
Al iniciar, si existe un archivo .bloom, será importado de forma automática.
Si no se detecta el archivo, se debe utilizar el botón "Cargar archivo de ítems" para seleccionarlo manualmente.

Formato del Archivo .bloom
El archivo debe estar en formato de texto plano con extensión .bloom. Cada ítem se describe con el siguiente esquema:

Enunciado: ¿En qué año comenzó la Segunda Guerra Mundial?
Opciones: a)1939 b)1940 c)11890 d)1889
Respuesta: 0
Tipo: MULTIPLE
Nivel: RECORDAR
Tiempo: 5minutos
Enunciado: Texto de la pregunta.
Respuesta: Número de la opción correcta (comenzando desde 0).
Tipo: MULTIPLE o VERDADERO_FALSO.
Nivel: Uno de RECORDAR, ENTENDER, APLICAR, ANALIZAR, EVALUAR, CREAR.
Tiempo: Duración estimada en segundos.
--- Delimita el final del ítem.
Se pueden añadir varios ítems repitiendo este formato.

Alcances y Supuestos
Solo se aceptan archivos con extensión .bloom. Se verifica la presencia de errores comunes en el formato.
Un ítem puede tener hasta 5 opciones disponibles.
El sistema únicamente acepta ítems de tipo opción múltiple o verdadero/falso.
Se asume que el archivo .bloom tiene datos estructurados correctamente (aunque se incluye validación básica).
El tiempo total estimado se obtiene sumando los tiempos de cada ítem.
