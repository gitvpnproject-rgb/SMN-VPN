# SMN VPN 🔐

**Decentralized Mesh Network VPN for Android**

![Status](https://img.shields.io/badge/Status-Alpha-yellow)
![Language](https://img.shields.io/badge/Language-Kotlin-blue)
![License](https://img.shields.io/badge/License-MIT-green)
![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)

---

## 🌐 Как работает?

```
Твой телефон
    ↓
Подключение к 3 серверам одновременно
    ↓
Сервер 1 упал? → Сервер 2 уже готов (мгновенное переключение)
    ↓
Multi-hop маршрутизация (трафик прыгает через узлы)
    ↓
Конечный сервер не видит твой IP
```

---

## 🔒 Два профиля

### 🟢 Обычный (стандарт)
- ✅ Kill Switch (интернет блокируется если VPN упал)
- ✅ DNS защита
- ✅ IPv6 защита
- ✅ Авто-переподключение
- ✅ Умный выбор сервера

### 🔴 Параноик (максимум защиты)
- ✅ Multi-hop (2-3 сервера в цепочке)
- ✅ Авто-ротация ключей (каждые 10 минут)
- ✅ Обфускация DPI (маскировка трафика)
- ✅ Высокая анонимность
- ⚠️ Медленнее, но безопаснее

---

## 🔐 Шифрование

| Режим | Алгоритм | Скорость | Защита |
|-------|----------|----------|--------|
| 🔴 Maximum | AES-256 | Средняя | ⭐⭐⭐⭐⭐ |
| 🟡 Balanced | AES-128 | Быстро | ⭐⭐⭐⭐ |
| 🟢 Fast | ChaCha20 | Самая быстрая | ⭐⭐⭐⭐ |

*ChaCha20 — как в WireGuard, безопасен и быстр на мобилках*

---

## 🏗️ Архитектура

### Два типа узлов:

**🖥️ Server-Coordinator**
- Хранит маршруты
- Знает где находятся узлы
- **НЕ видит твой трафик**
- Может держать 100+ юзеров

**📱 Node-Relay**
- Пропускает трафик
- Шифрует/пересылает пакеты
- **Не знает весь маршрут**
- Может быть обычное устройство

---

## ✅ Что реализовано

- [x] Структура Android проекта
- [x] MainActivity с UI
- [x] AndroidManifest с разрешениями
- [x] GitHub Actions для сборки APK
- [ ] VpnService (главный сервис)
- [ ] CryptoManager (шифрование)
- [ ] KillSwitch (блокировка)
- [ ] ServerConnection (3 сервера)
- [ ] SettingsFragment (профили)
- [ ] AddNodeFragment (добавление узлов)
- [ ] StatsFragment (статистика)

---

## 🚀 Установка

1. Клонируй репо:
```bash
git clone https://github.com/gitvpnproject-rgb/SMN-VPN.git
cd SMN-VPN
```

2. Собери APK через GitHub Actions (в вкладке Actions выбери последний run)

3. Установи на телефон

---

## 📁 Структура проекта

```
SMN-VPN/
├── app/src/main/
│   ├── java/com/smn/vpn/
│   │   ├── MainActivity.kt
│   │   ├── service/
│   │   │   ├── VpnService.kt
│   │   │   ├── ServerConnection.kt
│   │   │   └── KillSwitch.kt
│   │   ├── crypto/
│   │   │   └── CryptoManager.kt
│   │   ├── ui/
│   │   │   ├── SettingsFragment.kt
│   │   │   ├── StatsFragment.kt
│   │   │   └── AddNodeFragment.kt
│   ├── res/
│   │   └── layout/
│   │       └── activity_main.xml
│   └── AndroidManifest.xml
├── build.gradle
├── settings.gradle
└── README.md
```

---

## ⚠️ Честный дисклеймер

- **НЕ 100% анонимность** — не существует
- **Безопасность зависит от узлов** — выбирай надёжные серверы
- **Проект в разработке** — используй на свой риск
- **Открытый исходный код** — любой может проверить код

---

## 📜 Лицензия

MIT — используй свободно, но в своём приложении упомяни авторство

---

## 🤝 Помощь проекту

Ошибки? Идеи? Pull Request приветствуются!

---

**Сделано с ❤️ для приватности**
