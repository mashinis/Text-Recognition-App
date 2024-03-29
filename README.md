# MLKit Text Recognition App

Это приложение, разработанное с использованием MLKit, обеспечивает распознавание километра по километровому знаку на РЖД, с использованием камеры смартфона, в реальном времени. Проект представляет собой работающее приложения Android, который демонстрирует использование камеры для сканирования километровых знаков и распознавание на них километра в реальном времени. Кроме того, приложение также сохраняет распознанный километр, сопоставляет его с текущей GPS координатой и сохраняет данные в файл JSON после закрытия приложения. На экране смартфона отображается текущая координата и распознанный километр. В текущей редакции, происходит распознавание километров, состоящих только из четырех цифр.
## Функциональность

- Распознавание километра в реальном времени с помощью MLKit.
- Отображение текущих GPS-координат и других данных о местоположении.
- Возможность сохранения данных о распознаных километрах и их координатах.

## Требования

- Android устройство с версией Android 6.0 (API уровень 23) и выше.
- Разрешение на использование камеры и местоположения.

## Установка

1. Клонируйте репозиторий на свое устройство: git clone https://github.com/mashinis/Text-Recognition-App.git
2. Откройте проект в Android Studio.
3. Подключите устройство Android или используйте эмулятор для запуска приложения.

## Использование

1. Запустите приложение на устройстве Android.
2. Направьте камеру устройства на километровый знак, который вы хотите распознать.
3. Приложение автоматически распознает число и отобразит его на экране.
4. Текущие координаты GPS и другие данные о местоположении также будут отображены на экране.