# Recursos tipográficos

Roboto Condensed 3.016, archivos **estáticos** de Android, pesos regular 400 y
medium 500. Se incorporan sin modificar desde `android/static/` del
[paquete oficial Roboto_v3.016.zip](https://github.com/googlefonts/roboto-3-classic/releases/tag/v3.016).
No se usan los archivos variables ni se descargan fuentes en tiempo de ejecución.

Copyright 2011 The Roboto Project Authors. Licencia SIL Open Font License 1.1,
copiada íntegra desde la misma etiqueta `v3.016`:
[OFL oficial](https://github.com/googlefonts/roboto-3-classic/blob/v3.016/OFL.txt).
También se empaqueta en `src/main/assets/licenses/RobotoCondensed-OFL.txt`.

| Archivo local en res/font | SHA-256 |
| --- | --- |
| roboto_condensed_regular.ttf | 8c4c429a54b4af66cd337311569c0eb1d8e6d909c19d393f24a2817a32dc0924 |
| roboto_condensed_medium.ttf | 094b121451c188f0471710828b62312715b01e8d58665d63cb036630f6b453ad |

Verificado por lectura del paquete/licencia y hashes de los archivos descargados;
no acredita renderizado ni ejecución en dispositivo. La variante estática evita
depender del soporte de ejes variables disponible desde SDK 26.
