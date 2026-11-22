# FileFlow

FileFlow — локальное приложение для организации файлов на компьютере.

## Возможности

- Рекурсивное сканирование папок
- Автоматическая категоризация по расширениям
- Предпросмотр перед перемещением
- Поиск дубликатов (SHA-256)
- Поиск больших файлов
- Пользовательские правила с приоритетами
- История операций и отмена
- Тёмная тема, русский/английский интерфейс

## Стек

- Kotlin 2.1
- Compose Multiplatform Desktop 1.7
- Gradle Kotlin DSL
- Coroutines / Flow
- kotlinx.serialization
- Java NIO
- SQLite

## Запуск

```bash
./gradlew :app:run
```

Требуется JDK 21 (рекомендуется JBR).

## Сборка

```bash
./gradlew :app:packageDistributionForCurrentOS
```