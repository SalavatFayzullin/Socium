# Docker команды для Socium

## Основные команды

### Запуск приложения
```bash
# Запуск с Docker Compose (рекомендуется)
docker-compose up -d

# Запуск с пересборкой образа
docker-compose up --build -d

# Просмотр логов
docker-compose logs -f socium-app
```

### Остановка и очистка
```bash
# Остановка сервисов
docker-compose down

# Остановка с удалением volumes
docker-compose down -v

# Полная очистка (осторожно!)
docker-compose down -v --rmi all
```

## Разработка

### Сборка образа
```bash
# Сборка образа вручную
docker build -t socium:latest .

# Запуск контейнера из образа
docker run -p 8080:8080 --name socium-container socium:latest
```

### Отладка
```bash
# Подключение к работающему контейнеру
docker exec -it socium /bin/bash

# Просмотр логов приложения
docker logs socium

# Проверка здоровья контейнера
docker ps
```

### Управление данными
```bash
# Просмотр volumes
docker volume ls

# Создание бэкапа данных
docker run --rm -v socium_data:/data -v $(pwd):/backup alpine tar czf /backup/socium-backup.tar.gz -C /data .

# Восстановление данных из бэкапа
docker run --rm -v socium_data:/data -v $(pwd):/backup alpine tar xzf /backup/socium-backup.tar.gz -C /data
```

## Полезные команды

### Мониторинг
```bash
# Статистика использования ресурсов
docker stats socium

# Информация о контейнере
docker inspect socium
```

### Очистка системы
```bash
# Удаление неиспользуемых образов
docker image prune

# Удаление всех неиспользуемых ресурсов
docker system prune -a
``` 