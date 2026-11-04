# LLib

**LLib** — общая библиотека-плагин для Minecraft-серверов (Paper / Folia) от LastStudio.
Она сама ничего «не делает» в игре: на неё опираются другие плагины (LastMines и т.д.),
чтобы не писать каждый раз одно и то же — команды, цветные сообщения, предметы, частицы, действия и планировщик.

- **Minecraft:** 1.21 – 26.3 (Paper и Folia)
- **Java:** 21+
- **Версия:** 0.2
- **Сайт:** <https://laststudio.space> · **Репозиторий:** <https://github.com/LastDev1337/LLib>

---

## Содержание

1. [Для владельцев серверов](#для-владельцев-серверов) — что это и как установить
2. [Для разработчиков](#для-разработчиков) — как подключить и пользоваться
   - [Подключение: Maven / Gradle / Gradle Kotlin](#подключение-к-проекту)
   - [plugin.yml](#pluginyml-вашего-плагина)
   - [Получение API](#получение-api)
   - [Планировщик (Paper и Folia)](#планировщик-paper-и-folia)
   - [Действия (actions)](#действия-actions)
   - [Команды](#команды)
   - [Сообщения и цвета](#сообщения-и-цвета)
   - [Локализация](#локализация-translationjson)
   - [Предметы](#предметы)
   - [Частицы и версия сервера](#частицы-и-версия-сервера)
3. [Сборка из исходников](#сборка-из-исходников)
4. [Частые вопросы](#частые-вопросы)

---

# Для владельцев серверов

## Что это и зачем

Если другой плагин пишет в консоли `LLib not found` или в его описании указано `depend: LLib` —
значит, ему нужна эта библиотека. Установите её один раз, и она будет работать для всех плагинов LastStudio.

## Установка

1. Скачайте `LLib` последней версии (раздел *Releases* на GitHub).
2. Положите файл в папку `plugins/` вашего сервера.
3. Перезапустите сервер (команда `/reload` не подходит — библиотека загружается при старте).
4. В консоли должно появиться: `[LLib] enabling successfully`.

**Первый запуск требует интернета.** LLib сама скачивает несколько небольших библиотек
(`BCmd`, `byaml`, `BPlaceholder`, `render` — движок команд, конфигов и частиц) в папку `plugins/LLib/.libraries/`.
Дальше скачивать ничего не нужно.

Версии сервера: **1.21 – 26.3**. На других версиях LLib отключится сама и напишет об этом в консоль.
Поддерживаются и обычный **Paper**, и **Folia**.

### Необязательные плагины

| Плагин | Для чего                                           |
|---|----------------------------------------------------|
| **Vault** | действия `[vault]` (деньги)                        |
| **PlayerPoints** | действия `[playerpoints]` (очки)                   |
| **BLib** | не обязателен, но нужен для его "не конфликтности" |

Нет этих плагинов — всё остальное работает, нужные действия просто отключаются с одним предупреждением в консоли.

## Что появится в папке `plugins/LLib/`

| Файл / папка | Что это |
|---|---|
| `config.yml` | основные настройки (цвета, предметы) |
| `translation.json` | тексты сообщений библиотеки (русский и английский) |
| `items/` | ваши именованные предметы (шаблоны) — см. `items/example.yml` |
| `.libraries/` | скачанные служебные библиотеки (не трогайте) |

### `config.yml`

```yaml
color:
  type: ALL        # какой формат цвета понимать в сообщениях и названиях предметов
items:
  enable: true     # загружать предметы из папки items/
```

Варианты `color.type`:

| Значение | Пример                                              |
|---|-----------------------------------------------------|
| `LEGACY` | `&c&lТекст &9синий`                                 |
| `HEX` | `&#FF0000Красный &#99FF99зелёный`                   |
| `MINI_MESSAGE` | `<bold><red>Текст</red></bold>`                     |
| `GSON` | JSON-формат Minecraft                               |
| `ALL` | LEGACY + HEX + MINI_MESSAGE без GSON (по умолчанию) |

### Язык сообщений

В `translation.json` задаётся язык: `"use-locale": "auto"` — язык берётся из клиента игрока
(для консоли — `default-locale`, по умолчанию `ru_RU`). Хотите всем один язык — впишите, например, `"en_US"`.
Тексты можно править прямо в этом файле.

## Команды и права

| Команда | Права | Что делает |
|---|---|---|
| `/llib` (`/lastlib`, `/lib`) | `llib.reload` (по умолчанию — оператор) | перезагружает `config.yml`, `translation.json` и предметы из `items/` |

## Свои предметы (`items/*.yml`)

Любой `*.yml` в папке `items/` — набор именованных предметов. Один раз описали — дальше ссылаетесь по имени
(`llib:fire_sword`) из других плагинов, команд и действий (`[give] llib:fire_sword`).
Готовый образец со ВСЕМИ секциями (название, описание, зачарования, атрибуты, цвет, зелья, отделка брони,
контейнеры, `item_model` и др.) лежит в `items/example.yml`.

```yaml
space: llib
items:
  fire_sword:
    material: netherite_sword
    name: '&cОгненный клинок'
    lore:
      - '&7Награда за 5 уровень'
    enchantments:
      sharpness: 5
    unbreakable: true
```

После правки выполните `/llib`.

## Действия, которые понимает LLib (для настроек других плагинов)

Если плагин просит «список действий» (actions), вы можете использовать такие строки:

| Строка | Что делает                                 |
|---|--------------------------------------------|
| `[message] &aПривет!` | сообщение в чат (`[chat]` — то же)         |
| `[broadcast] &6Всем привет` | сообщение всем                             |
| `[actionbar] &fТекст` | текст над хотбаром                         |
| `[title] "&6Заголовок\n&7Подзаголовок" 10;70;20` | титр (появление; показ; затухание в тиках) |
| `[sound] entity.player.levelup 1 1` | звук (громкость, тон)                      |
| `[console] give %player% diamond 1` | команда от имени консоли                   |
| `[player] spawn` | команда от имени игрока                    |
| `[give] diamond 3` / `[give] llib:fire_sword` | выдать предмет                             |
| `[take] diamond 1` | забрать предмет                            |
| `[particle] FLAME` | частицы                                    |
| `[vault] give 100` · `take` · `set` | деньги Vault                               |
| `[playerpoints] give 10` · `take` · `set` | деньги PlayerPoints                        |

К любому действию можно добавить шанс: `[message] [chance:30] &aПовезло!` — сработает в 30% случаев.
У большинства есть вариант на всех игроков: `[title_all]`, `[give_all]`, `[sound_all]`, `[actionbar_all]`.

---

# Для разработчиков

LLib даёт готовое API: подключите её как зависимость, получите `LLibAPI.get()` и пользуйтесь сервисами.
LLib **не вшивается** в ваш jar — это отдельный плагин на сервере, а у вас она `compileOnly`/`provided`.

## Подключение к проекту

Репозиторий: `https://repo.laststudio.space/releases`
Пакет: `dev.laststudio.lib:llib:0.2`

### Maven

```xml
<repositories>
    <repository>
        <id>laststudio</id>
        <url>https://repo.laststudio.space/releases</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>dev.laststudio.lib</groupId>
        <artifactId>llib</artifactId>
        <version>0.2</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### Gradle Groovy (`build.gradle`)

```groovy
repositories {
    mavenCentral()
    maven { url 'https://repo.laststudio.space/releases' }
}

dependencies {
    compileOnly 'dev.laststudio.lib:llib:0.2'
}
```

### Gradle Kotlin DSL (`build.gradle.kts`)

```kotlin
repositories {
    mavenCentral()
    maven("https://repo.laststudio.space/releases")
}

dependencies {
    compileOnly("dev.laststudio.lib:llib:0.2")
}
```

### Локальная разработка рядом с LLib (composite build)

Если исходники LLib лежат рядом с вашим проектом, можно собирать против них, не публикуя jar.
В `settings.gradle.kts` вашего проекта:

```kotlin
includeBuild("../LLib") {
    dependencySubstitution {
        substitute(module("dev.laststudio.lib:llib")).using(project(":"))
    }
}
```

### Типы из библиотек by1337

API использует типы `dev.by1337.cmd.Command`, `dev.by1337.yaml.YamlMap` и т.п. Чтобы они были видны при компиляции,
добавьте их тоже как `compileOnly` (на сервере их загрузит LLib сама):

```kotlin
compileOnly("dev.by1337.cmd:BCmd:1.4")
compileOnly("dev.by1337.yaml:byaml-bukkit:1.6")
compileOnly("dev.by1337.plc:BPlaceholder:1.3")
compileOnly("dev.by1337.particle:render-bukkit:1.8")   // репозиторий: https://repo.by1337.space/repository/maven-releases/
```

## `plugin.yml` вашего плагина

```yaml
name: MyPlugin
main: com.example.MyPlugin
api-version: '1.21'
depend: [ LLib ]        # LLib загрузится раньше вашего плагина
folia-supported: true   # только если ваш код действительно безопасен для Folia
```

## Получение API

```java
import dev.laststudio.lib.api.LLibAPI;
import dev.laststudio.lib.api.LLibProvider;

LLibProvider lib = LLibAPI.get();   // бросит IllegalStateException, если LLib не включена
```

Сервисы провайдера:

| Метод | Что даёт |
|---|---|
| `lib.scheduler()` | планировщик, одинаково работающий на Paper и Folia |
| `lib.actions()` | разбор и выполнение действий (`[message]`, `[console]`, `[vault]`…) |
| `lib.commands()` | построитель команд (BCmd) с правами и подсказками |
| `lib.messages()` | цветные сообщения (LEGACY / HEX / MiniMessage) |
| `lib.locale()` | тексты из `translation.json` на языке игрока |
| `lib.items()` | чтение и запись предметов (`ItemSnapshot`) |
| `lib.itemRegistries()` | именованные предметы (`llib:fire_sword`) |
| `lib.particles()` | частицы |
| `lib.version()` | версия сервера и диапазон поддержки |

## Планировщик (Paper и Folia)

Один API на оба ядра. Правило: **блоки — `region`, игрок/сущность — `entity`, консоль и общие данные — `global`,
база данных и сеть — `async`.** Время в тиках (минимум 1).

```java
SchedulerService s = LLibAPI.get().scheduler();

s.global(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "say hi"));
s.region(plugin, location, () -> location.getBlock().setType(Material.STONE));
s.entity(plugin, player, () -> player.sendMessage("Привет"));
s.async(plugin, () -> saveToDatabase());

s.globalLater(plugin, () -> {}, 40L);           // через 2 секунды
ScheduledTask t = s.globalTimer(plugin, () -> {}, 20L, 20L);   // каждую секунду
t.cancel();

s.teleport(player, location);                   // CompletableFuture<Boolean>
s.cancelAll(plugin);                            // в onDisable
```

На обычном Paper всё выполняется в главном потоке, на Folia — в потоке нужного региона.

## Действия (actions)

Действие — это строка из конфига вида `[тип] значение`. Сервис разбирает её **один раз** при загрузке,
а выполняет сколько угодно раз.

```java
ActionService actions = LLibAPI.get().actions();

// при загрузке конфига
List<ParsedAction> parsed = actions.parse(config.getStringList("on-join"));

// при срабатывании: одному игроку
actions.execute(player, parsed);

// нескольким целям, с шансом, в потоке каждой цели и с подстановкой своих плейсхолдеров
actions.execute(plugin, players, parsed,
        (target, text) -> text.replace("%player%", target.getName()));
```

Что делает `execute(owner, targets, actions, placeholders)`:

1. бросает шанс (`[chance:N]`) один раз на действие;
2. отдаёт действие каждой цели в её потоке (игрок — поток игрока, остальные — глобальный);
3. ошибка в одной строке пишется в лог и не мешает остальным.

### Свои действия

```java
LLibAPI.get().actions().register("heal", (target, args) -> {
    if (target instanceof Player p) {
        double amount = args.tokens().length > 0 ? Double.parseDouble(args.tokens()[0]) : 20;
        p.setHealth(Math.min(p.getMaxHealth(), p.getHealth() + amount));
    }
});
// в конфиге:  - '[heal] 10'
```

`ActionArgs` умеет: `raw()` (вся строка), `tokens()` (слова), и для формы с ключами — `getString/getInt/getFloat/getBoolean`.
Вторая форма записи:

```yaml
- vault:
    mode: give
    amount: 100
    chance: 50
```

### Встроенные действия

`message`/`chat`, `broadcast`, `actionbar`, `title`, `sound` (+ варианты `*_all`), `console`, `player`,
`particle`, `give`, `give_all`, `take`, `vault`, `playerpoints`.
Таблица с примерами — в разделе для владельцев серверов выше.

## Команды

Построитель команд на BCmd. Права проверяются без спама сообщениями, подсказки (Tab) — через аргументы.

```java
CommandService cs = LLibAPI.get().commands();

Command<CommandSender> root = cs.command("hello")
        .aliases("hi")
        .requires(cs.permission("myplugin.hello"))
        .argument(cs.playerArg("target"))
        .executor((sender, args) -> {
            Player target = (Player) args.get("target");
            sender.sendMessage("Привет, " + target.getName());
        });

cs.register(plugin, root);        // в onEnable
// cs.unregister(root);           // в onDisable
```

Готовые аргументы: `intArg`, `doubleArg`, `playerArg`. Подкоманды: `root.sub(...)`.

## Сообщения и цвета

Формат берётся из `color.type` в `config.yml` LLib.

```java
MessageService msg = LLibAPI.get().messages();

msg.send(player, "&aПривет, &#FF8800мир!");
msg.sendList(player, List.of("<gold>Строка 1", "<red>Строка 2"));
msg.broadcast("&6Всем привет");

Component c = msg.render("&bТекст");        // Adventure Component
String legacy = msg.renderLegacy("&bТекст"); // старый §-формат
String plain = msg.plain("&bТекст");         // без цветов
String safe = msg.escape(userInput);         // обезвредить теги в тексте игрока
```

> Всегда пропускайте через `escape(...)` то, что ввёл игрок, прежде чем подставлять в текст.

## Локализация (`translation.json`)

Своих ключей в `translation.json` LLib вы добавить не можете (файл принадлежит LLib), но можете читать её тексты
и использовать тот же приём в своём плагине.

```java
LocaleService locale = LLibAPI.get().locale();
String text = locale.get(player, "reload", 12);   // {} заменяется аргументами
```

Формат файла:

```json
{
  "use-locale": "auto",
  "default-locale": "ru_RU",
  "messages": {
    "reload": { "ru_RU": "Плагин перезагружен! ({}ms)", "en_US": "Plugin reloaded! ({}ms)" }
  }
}
```

## Предметы

### Именованные предметы

```java
ItemRegistryService reg = LLibAPI.get().itemRegistries();

ItemStack sword = reg.getItemStack("llib:fire_sword");   // готовый ItemStack
ItemModel model = reg.getModel("llib", "fire_sword");     // шаблон
reg.register("myplugin", "gem", model);                   // зарегистрировать свой
```

Описание секций предмета — в `src/main/resources/items/example.yml` (все секции расписаны в комментариях):
`material`, `name`, `lore`, `amount`, `damage`, `enchantments`, `stored_enchantments`, `unbreakable`, `item_flags`,
`attributes`, `color`, `potion`, `potion_contents`, `model_data`, `textures`, `container`, `bundle_contents`, `trim`,
`hide_tooltip`, `max_stack_size`, `enchantment_glint_override`, а для 1.21.3+ ещё `glider`, `item_model`, `tooltip_style`.

### Чтение и запись произвольного предмета

```java
ItemDataService items = LLibAPI.get().items();

ItemSnapshot snapshot = items.read(player.getInventory().getItemInMainHand());
String json = items.toJson(snapshot);          // сохранить в базу / файл
ItemStack back = items.write(items.fromJson(json));
```

## Частицы и версия сервера

```java
LLibAPI.get().particles().spawn(player, ParticleType.FLAME, player.getLocation());

VersionService v = LLibAPI.get().version();
v.current();        // например "1.21.11" или "26.2"
v.isSupported();    // true в диапазоне 1.21 – 26.3
```

---

# Сборка из исходников

```bash
git clone https://github.com/LastDev1337/LLib
cd LLib
gradle clean build        # jar появится в build/libs/LLib-0.2.jar
```

Нужна Java 21+. После сборки остановите Gradle-демон, чтобы он не занимал память: `gradle --stop`.

# Частые вопросы

**LLib написала `Server version … is not supported` и выключилась.**
Ваша версия вне диапазона 1.21 – 26.3. Обновите (или понизьте) ядро.

**Плагин пишет `LLib not found`.** Положите `LLib-0.2.jar` в `plugins/` и перезапустите сервер.

**При первом запуске ошибка загрузки `runtime library`.** Нужен доступ в интернет к `repo.by1337.space`.
Скачайте ошибочный файл вручную в `plugins/LLib/.libraries/…` либо поставьте плагин BLib, в котором эти библиотеки уже есть.

**Работает ли на Folia?** Да. Для своих плагинов используйте `lib.scheduler()` вместо `Bukkit.getScheduler()`.

**Действие `[vault]` ничего не делает.** Установите Vault и плагин экономики; в консоли будет одно предупреждение, если их нет.

**Как перезагрузить настройки?** Команда `/llib` (право `llib.reload`).