# Phase 1 — MVP: USB Camera → Preview → YouTube Stream

**Статус:** 🟡 In Progress
**Milestone:** `0.2` — стрим на YouTube с USB камеры работает
**Источник:** [interview_002](../interviews/interview_002_phase1_usb_preview_youtube.md)

---

## Цель

Пользователь подключает USB-камеру (Emeet Piko+ или любую UVC-совместимую),
открывает KrinikCam — видит превью в fullscreen — нажимает кнопку — идёт стрим на YouTube.

---

## Новые Gradle-модули

| Модуль | Назначение |
|--------|-----------|
| `:core:logging` | Файловый debug-логгер; пользователь может шарить лог-файл |
| `:feature:usb` | USB-детекция, UVC-драйвер (AndroidUSBCamera), hot-plug, превью |
| `:feature:capture` | DeviceManager — реестр источников видео/аудио; фундамент |
| `:feature:codec` | MediaCodecList сканер — фундамент, нужен для выбора битрейта |
| `:feature:streaming` | RTMP-клиент (RootEncoder), управление платформами |
| `:data:profiles` | Room DB + DataStore: StreamProfile, DeviceProfile |

---

## Зависимости (новые)

| Библиотека | Координата | Назначение |
|------------|-----------|-----------|
| AndroidUSBCamera | `com.github.jiangdongguo.AndroidUSBCamera:libausbc:3.3.0` | UVC драйвер |
| RootEncoder | `com.github.pedroSG94.RootEncoder:library:2.4.7` | RTMP / MediaCodec |

JitPack уже подключён в `settings.gradle.kts`.

---

## Архитектура Phase 1

```
[ USB Camera (UVC) ]         [ Phone Mic ]
        │                          │
        ▼                          ▼
┌──────────────────┐    ┌──────────────────────┐
│  :feature:usb    │    │  :feature:capture    │
│  UvcCameraManager│    │  DeviceManager       │
│  hot-plug, perm  │    │  (phone mic source)  │
└────────┬─────────┘    └──────────┬───────────┘
         │ Surface / frames        │ PCM audio
         ▼                         ▼
┌─────────────────────────────────────────────┐
│              :feature:streaming             │
│  RtmpStreamer (RootEncoder)                 │
│  StreamPlatformsManager                     │
└──────────────────────┬──────────────────────┘
                       │ RTMP/RTMPS
                       ▼
              [ YouTube Live ]
```

---

## UI / UX

### MainScreen (fullscreen viewfinder)

Источник превью — приоритет:
1. Подключённая USB-камера (UVC)
2. Основная задняя камера телефона
3. Фронтальная камера
4. Любая другая камера
5. Чёрный экран + текст "Нет активных источников" + кнопка к инструкции

### Floating Overlay / Radial Menu

Phase 1 реализует **упрощённый прототип** радиального меню (стиль Sims 3):
- Плавающая кнопка (FAB) в углу
- Нажатие: раскрывает круговое меню с опциями
  - 🔴 Начать стрим / ⬛ Остановить стрим
  - 📡 Платформы (открывает StreamPlatformsOverlay)
  - ⚙️ Настройки
- Анимация: scale + alpha в/из центра

### StreamPlatformsOverlay (модальный диалог)

- Список платформ как сущностей (по умолчанию: YouTube)
- Каждая платформа: включить/выключить, редактировать ключ, удалить
- Изменения мгновенно синхронизируются с SettingsScreen

### "Please Stand By" заглушка

При отключении USB-камеры во время стрима:
- Статический фрейм с логотипом KrinikCam
- Надпись "Please stand by" на 5 языках (EN, RU, ES, DE, ZH)
- Продолжает поступать в RTMP-поток пока камера не вернётся

---

## Модуль `:core:logging`

Обязательный модуль по требованию Криника для отладки.

```
FileLogger:
  - записывает в /sdcard/Android/data/.../files/logs/kcam_YYYY-MM-DD.log
  - ротация: последние 7 дней
  - формат: [HH:mm:ss.SSS] [LEVEL] [TAG] message
  - ShareLogAction: Intent.ACTION_SEND для отправки лог-файла
```

---

## Порядок реализации

| Шаг | Что делаем | Приоритет |
|-----|-----------|-----------|
| 1 | Обновить `settings.gradle.kts`, `libs.versions.toml`, `app/build.gradle.kts` | 🔴 Блокирует всё |
| 2 | Создать `build.gradle.kts` для всех 6 новых модулей | 🔴 Блокирует всё |
| 3 | `:core:logging` — KLog, FileLogger, Hilt-модуль | 🔴 Нужен везде |
| 4 | `:data:profiles` — Room entities, DataStore, репозиторий | 🟠 Нужен для настроек |
| 5 | `:feature:usb` — USBMonitor, UvcCameraManager, превью | 🔴 Ядро Phase 1 |
| 6 | `:feature:capture` — DeviceManager, phone camera source | 🟠 Fallback для превью |
| 7 | `:feature:codec` — CodecScanner (базовый) | 🟡 Нужен для стриминга |
| 8 | `:feature:streaming` — RtmpStreamer, StreamPlatformsManager | 🔴 Ядро Phase 1 |
| 9 | UI: MainScreen, FloatingRadialMenu, StreamPlatformsOverlay | 🔴 Нужен пользователю |
| 10 | UI: SettingsScreen (базовый) | 🟠 Синхронизация с overlay |
| 11 | Navigation: NavGraph wiring | 🔴 Соединяет экраны |
| 12 | Permissions: USB + RECORD_AUDIO runtime flow | 🔴 Без этого не работает |
| 13 | "Please Stand By" заглушка | 🟡 При отключении камеры |
| 14 | Сборка + тест на Headwolf Titan1 + Emeet Piko+ | 🔴 Milestone |

---

## Что НЕ входит в Phase 1

- Мультистриминг (несколько платформ одновременно) → Phase 2
- Полный codec scanner с UI → Phase 2
- GPU-фильтры, кроп → Phase 4
- Foreground Service (фоновый стрим) → Phase 5
- Полная локализация → Phase 7

---

## Файловая структура (после Phase 1)

```
core/
  common/         # уже есть
  ui/             # уже есть
  logging/        # NEW — KLog, FileLogger
feature/
  usb/            # NEW — UVC detection + preview
  capture/        # NEW — DeviceManager
  codec/          # NEW — CodecScanner
  streaming/      # NEW — RtmpStreamer + StreamPlatformsManager
data/
  profiles/       # NEW — Room + DataStore
app/
  ui/
    MainScreen.kt         # fullscreen viewfinder
    NavGraph.kt           # navigation
    screens/
      SettingsScreen.kt
  overlay/
    FloatingRadialMenu.kt
    StreamPlatformsOverlay.kt
    StandbyPlaceholder.kt
```
