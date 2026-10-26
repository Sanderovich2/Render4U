# Render4U

Простая и быстрая рендер-утилита для Fabric (Minecraft 26.3+) с поддержкой **Vulkan** и **OpenGL**.

База для кастомного рендера интерфейсов без лишнего оверхеда и просадок FPS.

## Что внутри

- **Блюр (`Blur`)** — быстрый 2-pass Gaussian блюр по умолчанию (плюс поддержка Kawase при необходимости). Весь пайплайн собран в одном компактном классе без лишних аллокаций буферов.
- **Шейдерные примитивы**:
  - `RoundedRect` — скруглённые прямоугольники и градиенты.
  - `Shadow` — мягкие тени со спредом и блюром.
  - `Outline` — сглаженные обводки с регулировкой толщины.
  - `Line` — SDF-линии с толщиной и градиентом вдоль линии.
  - `Circle` — диски, кольца и дуги (прогресс-индикаторы).
  - `Image` — текстуры и иконки по Identifier, кастомные картинки (файлы, `BufferedImage`) с тонировкой.
- **Батчинг (`DisplayBatcher`)** — собирает отрисовку, отсекает элементы за экраном и эффективно переиспользует командные буферы под Vulkan и OpenGL.
- **Оптимизированный блюр** — down/up-проходы рендерятся только в области запрошенных панелей (scissor-кроп с запасом на ядро фильтра), а не по всему экрану.
- **Пайплайны (`Pipelines`)** — все шейдерные пайплайны модуля в одном классе.
- **Self-test (`RenderSelfTestModule`)** — демо-оверлей, каждый кадр прогоняющий весь рендер-стек и пишущий результат в лог (см. ниже).
- **3D Рендер (`Render3D` & `EventRender3D`)** — проекция координат из мира на экран (World-to-Screen) с кэшем матрицы на кадр, 3D боксы, 2D Box ESP, Corner ESP, трейсеры, неймтеги, хелперы сущностей с интерполяцией по partialTick (`drawEntityBox2D`, `drawEntityNametag` и др.).
- **Шрифты (Font)** — быстрый рендерер TrueType-шрифтов на основе динамического атласа с полной поддержкой кириллицы (в комплекте Roboto).
- **Утилита цвета (`ColorUtil`)** — удобный парсер HEX (`#RGB`, `#RRGGBB`, `#AARRGGBB`, `0x...`), RGB/RGBA и генератор плавных градиентов / радуги.

---

## Как добавить Render4U в свой клиент

### Вариант 1: Подключение готовым JAR (рекомендуется)

1. Соберите jar-файл:
   ```bash
   ./gradlew build
   ```
2. Поместите полученный `build/libs/render4u-1.0.0.jar` в папку `libs/` вашего клиента.
3. В `build.gradle` вашего проекта добавьте:
   ```groovy
   repositories {
       flatDir {
           dirs 'libs'
       }
   }

   dependencies {
       modImplementation ":render4u-1.0.0"
       include ":render4u-1.0.0" // Для упаковки утилиты прямо в ваш клиент
   }
   ```
4. Готово! Fabric автоматически запустит `Render4U`, зарегистрирует шейдеры и миксины.

---

### Вариант 2: Перенос исходников в свой клиент

Если вы хотите интегрировать код напрямую в дерево исходников вашего мода/клиента:

1. Скопируйте папку пакета `src/main/java/dev/render4u` в исходники вашего проекта.
2. Скопируйте папку ресурсов `src/main/resources/assets/render4u` к себе в `resources/assets/`.
3. Подключите миксины:
   - Либо добавьте `"render4u.mixins.json"` в массив `"mixins"` вашего `fabric.mod.json`:
     ```json
     "mixins": [
       "yourclient.mixins.json",
       "render4u.mixins.json"
     ]
     ```
   - Либо перенесите 3 миксина в свой миксин-конфиг:
     - `GameRendererMixin` (хук извлечения кадра рендера)
     - `GuiRendererMixin` (обработка проходов блюра)
     - `ShaderManagerMixin` (сброс пайплайнов для совместимости с Vulkan)
4. В точке входа клиента инициализируйте пайплайны:
   ```java
   Pipelines.init();
   FontManager.init();
   ```

---

## Отрисовка в клиенте

### Способ 1: Через шину событий EventBus
Подпишитесь на `EventDisplay` в любом модуле или слушателе:

```java
import dev.render4u.Render4U;
import dev.render4u.core.event.EventTarget;
import dev.render4u.core.event.impl.EventDisplay;
import dev.render4u.render.display.blur.Blur;
import dev.render4u.render.display.shape.RoundedRect;
import dev.render4u.render.font.FontManager;
import dev.render4u.util.color.ColorUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class MyHud {
    public MyHud() {
        Render4U.getEventBus().register(this);
    }

    @EventTarget
    public void onDisplay(EventDisplay event) {
        GuiGraphicsExtractor graphics = event.getGraphics();

        float x = 20.0F, y = 20.0F, w = 150.0F, h = 40.0F;

        // Размытие под плашкой
        Blur.of(x, y, w, h).radius(6).strength(2).render(graphics);

        // Полупрозрачный фон
        RoundedRect.of(x, y, w, h).radius(6).color(ColorUtil.hex("#9010141C")).render(graphics);

        // Текст
        FontManager.ROBOTO.drawString(graphics, "Render4U Active", x + 10, y + 14, 14.0F, 0xFFFFFFFF);
    }
}
```

### Способ 2: Внутри любого Screen или ванильного GUI
Вы можете вызывать примитивы прямо в методах `extractProcesses` или везде, где доступен `GuiGraphicsExtractor`:

```java
Blur.of(x, y, w, h).radius(8).render(graphics);
RoundedRect.of(x, y, w, h).radius(8).color(ColorUtil.colorRgba(20, 20, 20, 180)).render(graphics);
```

---

## 3D Рендер в мире (ESP, боксы, трейсеры)

Подпишитесь на `EventRender3D` (или используйте внутри `EventDisplay`):

```java
import dev.render4u.Render4U;
import dev.render4u.core.event.EventTarget;
import dev.render4u.core.event.impl.EventRender3D;
import dev.render4u.render.world.Render3D;
import dev.render4u.util.color.ColorUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;

public class EspModule {
    public EspModule() {
        Render4U.getEventBus().register(this);
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        GuiGraphicsExtractor graphics = event.getGraphics();
        int espColor = ColorUtil.hex("#5B86E5");

        // 1. Трейсер от центра экрана к координатам
        Render3D.drawTracer(graphics, targetX, targetY, targetZ, espColor, 1.5F);

        // 2. 2D Box ESP вокруг хитбокса
        Render3D.drawBox2D(graphics, player.getBoundingBox(), espColor, 1.5F);

        // 3. Угловой (Corner) ESP
        Render3D.drawCornerBox2D(graphics, player.getBoundingBox(), espColor, 1.5F, 8.0F);

        // 4. Полноценный 3D Wireframe куб сущности или блока
        Render3D.drawBox3D(graphics, player.getBoundingBox(), espColor, 1.5F);
        Render3D.drawBlockBox3D(graphics, new BlockPos(100, 64, 100), ColorUtil.hex("#FF5555"), 1.5F);

        // 5. Неймтеги и дистанция
        Render3D.drawDistanceTag(graphics, "Target", targetX, targetY + 2.0, ColorUtil.hex("#9010141C"));

        // 6. Хелперы сущностей (интерполяция по partialTick из события)
        Render3D.drawEntityCornerBox2D(graphics, player, event.partialTick(), espColor, 1.5F, 8.0F);
        Render3D.drawEntityNametag(graphics, player, event.partialTick(), 0xFFFFFFFF, 0x9010141C);
        Render3D.drawEntityTracer(graphics, player, event.partialTick(), espColor, 1.5F);
    }
}
```

---

## Примеры элементов интерфейса

### Карточка со скруглением, тенью и обводкой
```java
// Мягкая тень под карточкой
Shadow.of(x, y, w, h).radius(8).blur(10).color(ColorUtil.colorRgba(0, 0, 0, 160)).render(graphics);

// Фон карточки
RoundedRect.of(x, y, w, h).radius(8).color(ColorUtil.hex("#1A2030")).render(graphics);

// Градиентная обводка
Outline.of(x, y, w, h).radius(8).thickness(1.0F)
    .horizontalGradient(ColorUtil.hex("#5B86E5"), ColorUtil.hex("#36D1DC"))
    .render(graphics);
```

### Текст (Font)
```java
FontManager.ROBOTO.drawString(graphics, "Привет, мир!", x, y, 16.0F, 0xFFFFFFFF);
```

### Круги, линии и картинки
```java
Circle.of(centerX, centerY, 8.0F).color(0xFF5B86E5).render(graphics);
Circle.of(centerX, centerY, 8.0F).innerRadius(4.0F).color(0xFF36D1DC).render(graphics);
Circle.of(centerX, centerY, 10.0F).innerRadius(6.0F).angle(0.0F, 270.0F).color(0xFFFFB74D).render(graphics);

Line.of(x1, y1, x2, y2).thickness(2.0F).gradient(0xFF5B86E5, 0xFF36D1DC).render(graphics);

Image.of("textures/block/stone.png").bounds(x, y, 16.0F, 16.0F).render(graphics);
Image.of(Identifier).bounds(x, y, w, h).uv(0.0F, 0.0F, 0.5F, 0.5F).color(0x80FFFFFF).render(graphics);
Image.of(bufferedImage, "name").bounds(x, y, w, h).radius(6.0F).render(graphics);
```

### Кастомные картинки
Кастомная текстура регистрируется по имени, кэшируется и повторные вызовы `of` с тем же именем не перезагружают её. Обновить содержимое уже зарегистрированной картинки — `Image.update`. Все вызовы должны быть на render-потоке (например, из модулей).
```java
Image.of(Path.of("config/logo.png")).bounds(x, y, 64.0F, 64.0F).render(graphics);
Image.of(Path.of("config/logo.png"), "mylogo").bounds(x, y, 64.0F, 64.0F).render(graphics);
Image.of(bufferedImage, "preview").bounds(x, y, 32.0F, 32.0F).render(graphics);
Image.update("preview", newBufferedImage);
```

### Работа с цветом
```java
int hexColor = ColorUtil.parseHex("#5B86E5");
Color color = ColorUtil.hex("#AF10141C");
int rgb = ColorUtil.rgba(255, 128, 0, 200);
int rainbow = ColorUtil.rainbow(4.0F, 0.8F, 1.0F, 0);
```

---

## Проверка, что всё рендерится

### 1. Встроенный self-test (в игре)
Вместе с утилитой регистрируются модули **Render Self Test** (включён по умолчанию) и **Debug HUD** (выключен). Self-test каждый кадр прогоняет весь рендер-стек и рисует демо-панель: тень, блюр, скруглённую панель, градиентную обводку, текст с кириллицей, круг/кольцо/дугу, градиентную линию, текстуру, градиентную полоску, а в мире — угловой бокс, трейсер, неймтег и хелперы сущностей.

Если всё отрисовалось без ошибок, в логе появятся строки:

```
[Render4U/SelfTest] Self-test: 2D OK (shadow, blur, rounded rect, outline, circle, line, image, gradient, text, cyrillic)
[Render4U/SelfTest] Self-test: 3D OK (corner box, tracer, distance tag, entity helpers)
```

**Debug HUD** показывает бэкенд (`VulkanHelper`), FPS, время кадра и статистику батчера. Включить:

```java
import dev.render4u.feature.module.impl.DebugHudModule;

Render4U.getModuleManager().getModule(DebugHudModule.class).setEnabled(true);
```

### 2. Unit-тесты
Логика без GPU (шина событий, цвета, градиенты, упаковка параметров примитивов) покрыта JUnit-тестами:

```bash
./gradlew test
```

---

## Сборка

Требуется **Java 25**.

```bash
./gradlew build
```
Готовый jar-файл появится в папке `build/libs/render4u-1.0.0.jar`.
