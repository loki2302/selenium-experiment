package me.loki2302;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;
import ru.yandex.qatools.ashot.cropper.indent.IndentCropper;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.yandex.qatools.ashot.cropper.indent.IndentFilerFactory.blur;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AppTest {
    @Configuration
    @Import(WebDriverConfiguration.class)
    public static class TestApp extends App {
        @Override
        public MessageProvider messageProvider() {
            return mock(MessageProvider.class);
        }
    }

    @Autowired
    private MessageProvider messageProvider;

    @Autowired
    private WebDriver webDriver;

    @Autowired
    private WebDriverUtils webDriverUtils;

    @Test
    public void angularJsPageTitleShouldSayHello() throws MalformedURLException {
        webDriver.get("http://localhost:8080/angularjs-app.html");
        assertEquals("Hello", webDriver.getTitle());
    }

    @Test
    public void angularJsButtonShouldRevealTheMessage_CRUTCH() {
        final String TEST_MESSAGE = "hello test";

        when(messageProvider.getMessage()).thenReturn(TEST_MESSAGE);

        webDriver.get("http://localhost:8080/angularjs-app.html");
        webDriver.findElement(By.tagName("button")).click();

        new WebDriverWait(webDriver, 15).until(elementTextIsNotEmpty(By.tagName("h1")));
        assertEquals(TEST_MESSAGE, webDriver.findElement(By.tagName("h1")).getText());
    }

    @Test
    public void angular2ButtonShouldRevealTheMessage() {
        final String TEST_MESSAGE = "hello test";

        when(messageProvider.getMessage()).thenReturn(TEST_MESSAGE);

        webDriver.get("http://localhost:8080/angular2-app.html");

        webDriver.findElement(By.tagName("button")).click();
        // see the nicer solution below
        webDriverUtils.synchronizeAngular2();
        webDriverUtils.synchronizeAngular2();
        webDriverUtils.synchronizeAngular2();
        webDriverUtils.synchronizeAngular2();
        webDriverUtils.synchronizeAngular2();
        webDriverUtils.synchronizeAngular2();
        webDriverUtils.dumpLogs();

        assertEquals(String.format("message is %s", TEST_MESSAGE), webDriver.findElement(By.tagName("h1")).getText());
    }

    @Test
    public void angular2ButtonShouldRevealTheMessageSmart() {
        final String TEST_MESSAGE = "hello test";

        when(messageProvider.getMessage()).thenReturn(TEST_MESSAGE);

        webDriver.get("http://localhost:8080/angular2-app.html");

        webDriver.findElement(By.tagName("button")).click();
        webDriverUtils.synchronizeAngular2Smart();
        webDriverUtils.dumpLogs();

        assertEquals(String.format("message is %s", TEST_MESSAGE), webDriver.findElement(By.tagName("h1")).getText());
    }

    @Test
    public void angular2InvokeExposedApi() {
        final String TEST_MESSAGE = "hello test";

        when(messageProvider.getMessage()).thenReturn(TEST_MESSAGE);

        webDriver.get("http://localhost:8080/angular2-app.html");

        webDriver.findElement(By.tagName("button")).click();
        webDriverUtils.synchronizeAngular2();

        Object result = ((JavascriptExecutor)webDriver).executeScript("return window.addNumbers(2, 3);");
        assertEquals(5L, (long)(Long)result);
        webDriverUtils.dumpLogs();
    }

    /**
     * As of Chrome 56.0.2924.87 and ChromeDriver 2.27,
     * native screenshots do not work - they are always black images.
     * Probably here's this issue: https://bugs.chromium.org/p/chromedriver/issues/detail?id=1625
     * The only reliable workaround is to use AWT screenshots {@link AppTest#angularJsButtonShouldRevealTheMessageAwtScreenshots}
     */
    @Test
    public void angularJsButtonShouldRevealTheMessage() throws IOException {
        final String TEST_MESSAGE = "hello test";

        when(messageProvider.getMessage()).thenReturn(TEST_MESSAGE);

        webDriver.get("http://localhost:8080/angularjs-app.html");
        webDriverUtils.makeScreenshot(new File("1.png"));

        WebElement buttonWebElement = webDriver.findElement(By.tagName("button"));
        webDriverUtils.highlight(buttonWebElement);
        webDriverUtils.makeScreenshot(new File("2.png"));
        webDriverUtils.unhighlight();
        buttonWebElement.click();
        webDriverUtils.synchronizeAngularJs();

        assertEquals(TEST_MESSAGE, webDriver.findElement(By.tagName("h1")).getText());
        webDriverUtils.makeScreenshot(new File("3.png"));

        webDriverUtils.dumpLogs();
    }

    @Test
    public void angularJsButtonShouldRevealTheMessageAwtScreenshots() throws IOException {
        final String TEST_MESSAGE = "hello test";

        when(messageProvider.getMessage()).thenReturn(TEST_MESSAGE);

        webDriver.get("http://localhost:8080/angularjs-app.html");
        webDriverUtils.makeScreenshotAwt(new File("awt-1.png"));

        WebElement buttonWebElement = webDriver.findElement(By.tagName("button"));
        webDriverUtils.highlight(buttonWebElement);
        webDriverUtils.makeScreenshotAwt(new File("awt-2.png"));
        webDriverUtils.unhighlight();
        buttonWebElement.click();
        webDriverUtils.synchronizeAngularJs();

        assertEquals(TEST_MESSAGE, webDriver.findElement(By.tagName("h1")).getText());
        webDriverUtils.makeScreenshotAwt(new File("awt-3.png"));

        webDriverUtils.dumpLogs();
    }

    @Test
    public void canTakeScreenshotOfJustOneElement() throws IOException {
        webDriver.get("http://localhost:8080/angular2-app.html");

        WebElement buttonElement = webDriver.findElement(By.tagName("button"));
        Screenshot screenshot = new AShot()
                .coordsProvider(new WebDriverCoordsProvider())
                .takeScreenshot(webDriver, buttonElement);
        ImageIO.write(screenshot.getImage(), "png", new File("button.png"));
    }

    @Test
    public void canTakeScreenshotOfAnElementWithBlurryOtherElements() throws IOException {
        final String TEST_MESSAGE = "hello test";

        when(messageProvider.getMessage()).thenReturn(TEST_MESSAGE);

        webDriver.get("http://localhost:8080/angular2-app.html");

        WebElement buttonElement = webDriver.findElement(By.tagName("button"));
        buttonElement.click();
        webDriverUtils.synchronizeAngular2Smart();

        // "radius" around target element to capture
        final int INDENT = 1000;
        Screenshot screenshot = new AShot()
                .coordsProvider(new WebDriverCoordsProvider())
                .imageCropper(new IndentCropper(INDENT).addIndentFilter(blur()))
                .takeScreenshot(webDriver, buttonElement);
        ImageIO.write(screenshot.getImage(), "png", new File("button-blur.png"));
    }

    private static ExpectedCondition<Boolean> elementTextIsNotEmpty(By by) {
        return input -> !input.findElement(by).getText().isEmpty();
    }
}
