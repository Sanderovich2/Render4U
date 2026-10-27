<h1 align="center"> Render4U </h1>
<p align="center">
  <img alt="fabric" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/fabric_vector.svg">
</p>

**Render4U** — быстрая и плавная база рендера для Fabric (Minecraft 26.3), созданная для кастомных GUI, интерфейсов и клиентских визуалов без просадок FPS.

Шейдерный SDF-рендер, две независимые системы шрифтов (MSDF и TTF), пружинная физика, анимированные GIF и удобный фасад для рисования в одну строчку.

## Возможности

- **Две системы шрифтов** — векторный MSDF (Apple SF Pro) без пикселей и мыла на любом масштабе + быстрый TTF-движок (Roboto и любые кастомные шрифты через `TTFFontManager`).
- **Свечение и эффекты текста** — нативный неоновый glow, аутлайн, мягкие тени и градиенты прямо в шейдере шрифта.
- **Фасад Draw** — всё рисуется буквально в одну строчку: размытие (blur), скругленные прямоугольники, кольца, круги, линии.
- **Головы игроков (PlayerHead)** — рендер скинов игроков со скруглением и поддержкой наружного слоя (hat layer) без лагов за счет кэширования.
- **GIF и кастомные картинки** — воспроизведение анимированных GIF на лету и загрузка любых изображений с подгонкой пропорций.
- **Анимации и физика** — плавные кривые интерполяции (Easing), пружинная физика (Spring) и независимый от тикрейта сглаживатель (Damp).
- **3D World рендер** — боксы сущностей, угловые рамки, трейсеры и неймтеги с плавной интерполяцией по partialTick.
- **Высокая производительность** — батчинг отрисовки, шейдерный scissor-кроп блюра и нулевой мусор для сборщика мусора.

## Установка

1. Установите [Fabric Loader](https://fabricmc.net/use/) для Minecraft 26.3
2. Скачайте [Fabric API](https://modrinth.com/mod/fabric-api)
3. Добавьте Render4U в зависимости вашего проекта или положите jar в папку `mods`

## Подключение в проект

Закиньте `render4u-1.2.0.jar` в папку `libs/` вашего мода:

```groovy
repositories {
    flatDir { dirs 'libs' }
}

dependencies {
    modImplementation ":render4u-1.2.0"
    include ":render4u-1.2.0"
}
```

## Примеры использования

Весь рендер доступен через статические методы класса `Draw`:

### Панели и размытие

```java
// Блюр заднего плана
Draw.blur(graphics, x, y, w, h, 14.0F, 3, 0x220F172A);

// Скругленный прямоугольник и обводка
Draw.rect(graphics, x, y, w, h, 14.0F, 0xAA0B0F19);
Draw.border(graphics, x, y, w, h, 14.0F, 1.0F, 0x33FFFFFF);

// Линия с градиентом
Draw.lineGrad(graphics, x, y, x + w, y, 2.0F, Gradient.NEON.sample(0.0F), Gradient.NEON.sample(1.0F));
```

### Шрифты (MSDF и TTF)

```java
// MSDF с неоновым свечением
Draw.textWithGlow(graphics, MsdfFonts.SF_BOLD, "Render4U", x, y, 12.0F, 0xFFFFFFFF, 0x8838BDF8, 0.25F);

// MSDF с обводкой
Draw.textWithOutline(graphics, MsdfFonts.SF_MEDIUM, "Outline", x, y + 16.0F, 9.0F, 0xFFFFFFFF, 0xFF000000, 0.15F);

// TTF шрифт из менеджера
Draw.ttfWithShadow(graphics, TTFFontManager.ROBOTO, "Roboto text", x, y + 32.0F, 12.0F, 0xFFFFFFFF);

// Регистрация своего TTF шрифта
TTFFont customFont = TTFFontManager.register("myfont", "/assets/myclient/fonts/ttf/myfont.ttf");
Draw.ttfWithShadow(graphics, customFont, "Custom TTF", x, y + 46.0F, 12.0F, 0xFFFFFFFF);
```

### Головы игроков, GIF и картинки

```java
// Голова игрока со скруглением
Draw.head(graphics, player.getGameProfile(), x, y, 36.0F, 8.0F);

// Анимированная гифка со скругленными углами
Draw.gif(graphics, Identifier.of("myclient", "textures/cat.gif"), x, y, 100.0F, 100.0F, 10.0F, 0xFFFFFFFF);

// Картинка с сохранением пропорций
Draw.imageFit(graphics, Identifier.of("myclient", "textures/logo.png"), x, y, 64.0F, 64.0F, 8.0F, 0xFFFFFFFF);
```

### Анимации и пружины

```java
// Плавная анимация по кривой
Animation anim = new Animation(0.0F, 250L, Easing.EASE_OUT_CUBIC);
anim.run(1.0F);

// Пружинная физика
SpringAnimation spring = new SpringAnimation(0.0F, 180.0F, 14.0F);
spring.setTarget(100.0F);
spring.update(deltaSeconds);
float value = spring.getValue();

// Плавный фпс-независимый damp
float smooth = Damp.damp(current, target, 10.0F, deltaSeconds);
```

### 3D рендер в мире

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

## Сборка из исходников

```bash
./gradlew build
```

Готовый jar появится в `build/libs/`.

Требуется **JDK 25**.

## Ссылки

- **Автор:** [sanderswagboy](https://github.com/Sanderovich2)
- **GitHub:** https://github.com/Sanderovich2/Render4U
- **Telegram:** https://t.me/wtfcrunk

## Лицензия

[MIT License](LICENSE)
