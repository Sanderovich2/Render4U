# Render4U

Быстрая и простая база рендера для Fabric (Minecraft 26.3+) на Vulkan и OpenGL.  
Создана для кастомных интерфейсов и читов без просадок фпс и лишнего мусора.

---

## Что умеет

- **Две системы шрифтов (MSDF & TTF)**:
  - **MSDF (`fonts/msdf/`)** — четкий векторный Apple SF Pro (`SF_BOLD`, `SF_SEMIBOLD`, `SF_MEDIUM`). Выглядит идеально при любом масштабе интерфейса, поддерживает аппаратное неоновое свечение (Glow), обводку (Outline) и тени в шейдере.
  - **TTF (`fonts/ttf/`)** — классические TrueType-шрифты (в комплекте Roboto). Удобная загрузка любых `.ttf` файлов, быстрый массив глифов и нулевая нагрузка на память.
- **Головы игроков** — рисует скины с любым скруглением (вплоть до круглых аватарок), скины кэшируются и не грузят игру.
- **Гифки и картинки** — воспроизведение анимированных гифок на лету, поддержка картинок, скругление любых углов и отзеркаливание.
- **Градиенты и темы** — готовые палитры (Neon, Cyberpunk, Sunset, Astolfo, Aurora, Rainbow) и смешивание цветов.
- **Анимации и пружины** — куча кривых (Easing), пружинная физика и плавное сглаживание значений независимо от герцовки монитора.
- **Удобный класс Draw** — всё рисуется в одну строчку: прямоугольники, размытие, линии, текст, головы, гифки.
- **Быстрый батчинг** — невидимые элементы за экраном сразу отсекаются, отрисовка объединяется в пачки, сборщик мусора не спамит и фпс не падает.

---

## Подключение

Собери мод командой:

```bash
./gradlew build
```

Закинь полученный `build/libs/render4u-1.2.0.jar` в папку `libs/` своего проекта и добавь в `build.gradle`:

```groovy
repositories {
    flatDir { dirs 'libs' }
}

dependencies {
    modImplementation ":render4u-1.2.0"
    include ":render4u-1.2.0"
}
```

---

## Примеры использования

Весь рендер доступен через статические методы `Draw.*`:

### Размытие и панельки
```java
// Блюр заднего плана
Draw.blur(graphics, x, y, w, h, 14.0F, 3, 0x220F172A);

// Скругленный прямоугольник и обводка
Draw.rect(graphics, x, y, w, h, 14.0F, 0xAA0B0F19);
Draw.border(graphics, x, y, w, h, 14.0F, 1.0F, 0x33FFFFFF);

// Линия с градиентом
Draw.lineGrad(graphics, x, y, x + w, y, 2.0F, Gradient.NEON.sample(0.0F), Gradient.NEON.sample(1.0F));
```

### Голова игрока
```java
// Круглая голова себя
Draw.headSelf(graphics, x, y, 36.0F, 18.0F);

// Голова любого игрока со скруглением
Draw.head(graphics, player.getGameProfile(), x, y, 36.0F, 8.0F);
```

### Текст и эффекты
```java
// Текст со свечением
Draw.textWithGlow(graphics, MsdfFonts.SF_BOLD, "Render4U", x, y, 12.0F, 0xFFFFFFFF, 0x8838BDF8, 0.25F);

// Текст с обводкой
Draw.textWithOutline(graphics, MsdfFonts.SF_MEDIUM, "Outline", x, y + 16.0F, 9.0F, 0xFFFFFFFF, 0xFF000000, 0.15F);

// Градиентный текст
Draw.textGradH(graphics, MsdfFonts.SF_SEMIBOLD, "Gradient", x, y + 30.0F, 9.0F, 0xFF38BDF8, 0xFF818CF8);

// TTF шрифт из TTFFontManager
Draw.ttfWithShadow(graphics, TTFFontManager.ROBOTO, "Roboto text", x, y + 44.0F, 12.0F, 0xFFFFFFFF);

// Или регистрируй свой шрифт и используй в любом месте
TTFFont myFont = TTFFontManager.register("myfont", "/assets/myclient/fonts/ttf/myfont.ttf");
Draw.ttfWithShadow(graphics, myFont, "Custom font", x, y + 58.0F, 12.0F, 0xFFFFFFFF);
```

### Анимированные гифки и картинки
```java
// Гифка со скругленными углами
Draw.gif(graphics, Identifier.of("myclient", "textures/cat.gif"), x, y, 100.0F, 100.0F, 10.0F, 0xFFFFFFFF);

// Картинка с сохранением пропорций
Draw.imageFit(graphics, Identifier.of("myclient", "textures/logo.png"), x, y, 64.0F, 64.0F, 8.0F, 0xFFFFFFFF);
```

### Переливающаяся рамка
```java
Draw.flowBorderH(graphics, x, y, 120.0F, 28.0F, 8.0F, 1.0F, 0xFF38BDF8, 0xFF818CF8);
```

---

## Анимации

```java
// Плавная анимация
Animation anim = new Animation(0.0F, 250L, Easing.EASE_OUT_CUBIC);
anim.run(1.0F);
float progress = anim.get();

// Пружинка
SpringAnimation spring = new SpringAnimation(0.0F, 180.0F, 14.0F);
spring.setTarget(100.0F);
spring.update(deltaSeconds);
float val = spring.getValue();

// Плавное следование без привязки к фпс
float smooth = Damp.damp(current, target, 10.0F, deltaSeconds);
```

---

## 3D Рендер в мире

Рендер боксов, трейсеров и неймтегов через событие `EventRender3D`:

```java
@EventTarget
public void onRender3D(EventRender3D event) {
    GuiGraphicsExtractor g = event.getGraphics();
    int color = 0xFF38BDF8;

    // 2D бокс на сущности
    Render3D.drawEntityBox2D(g, player, event.partialTick(), color, 1.5F);

    // Угловой бокс
    Render3D.drawEntityCornerBox2D(g, player, event.partialTick(), color, 1.5F, 6.0F);

    // Трейсер от центра экрана
    Render3D.drawEntityTracer(g, player, event.partialTick(), color, 1.0F);

    // Неймтег
    Render3D.drawEntityNametag(g, player, event.partialTick(), 0xFFFFFFFF, 0x880F172A);
}
```

---

## Сборка

- Java 25
- Fabric Loom 26.3+

```bash
./gradlew build
```
Готовый jar будет лежать в `build/libs/render4u-1.2.0.jar`.
