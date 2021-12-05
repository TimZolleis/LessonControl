package de.waldorfaugsburg.lessoncontrol.client.service.bbb;

import de.waldorfaugsburg.lessoncontrol.client.LessonControlClientApplication;
import de.waldorfaugsburg.lessoncontrol.client.service.AbstractService;
import de.waldorfaugsburg.lessoncontrol.common.service.BBBServiceConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
public final class BBBService extends AbstractService<BBBServiceConfiguration> {

    private static final ExpectedCondition<Boolean> WAIT_CONDITION =
            driver -> ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");

    private static final String URL = "https://app.bildungsplattform.org/";
    private static final String SELECT_PERSON_URL = URL + "user/select_person?_switch_user=";
    private static final String COURSE_URL = URL + "courses/";

    private WebDriver webDriver;
    private long lastAction;

    public BBBService(final LessonControlClientApplication application, final BBBServiceConfiguration configuration) {
        super(application, configuration);
    }

    @Override
    public void enable() throws Exception {
        System.setProperty("webdriver.chrome.driver", getConfiguration().getDriverPath());

        webDriver = new ChromeDriver();
        login();
        startBBB("uwe.henken@waldorf-augsburg.de", "0e466256-0e8c-4607-a10c-bbee44e0fa2c", 1, 10);
    }

    @Override
    public void disable(boolean shutdown) throws Exception {

    }

    public void startBBB(final String teacherEmail, final String courseId, final int participantCount, final int duration) {
        selectPerson("_exit");
        waitUntilPageReady();
        selectPerson(teacherEmail);
        waitUntilPageReady();

        webDriver.get(COURSE_URL + courseId);
        waitUntilPageReady();

        clearAndSendKeys("big_blue_button_meeting_maxUsers", Integer.toString(participantCount));
        clearAndSendKeys("big_blue_button_meeting_duration", Integer.toString(duration), Keys.ENTER);

        new WebDriverWait(webDriver, 5)
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'teilnehmen')]")))
                .click();
        waitUntilPageReady();

        new WebDriverWait(webDriver, 10)
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector("[aria-label=Mit Mikrofon]")))
                .click();

        new WebDriverWait(webDriver, 10)
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector("[aria-label=Echo ist hörbar]")))
                .click();

        new WebDriverWait(webDriver, 10)
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector("[aria-label=Webcam freigeben]")))
                .click();

        ((Select) new WebDriverWait(webDriver, 10)
                .until(ExpectedConditions.elementToBeClickable(By.id("setCam")))).deselectByVisibleText("OBS Virtual Camera");

        ((Select) new WebDriverWait(webDriver, 10)
                .until(ExpectedConditions.elementToBeClickable(By.id("setQuality")))).deselectByVisibleText("High Definition");

        new WebDriverWait(webDriver, 10)
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//span[contains(text(),'Freigabe starten')]")))
                .click();
    }

    public void stopBBB() {

    }

    private void login() {
        final long lastActionPerformed = lastAction;

        // If there's been recent action; session is still valid and hasn't to be checked
        if (System.currentTimeMillis() - lastActionPerformed < TimeUnit.HOURS.toMillis(1)) {
            return;
        }

        webDriver.get(URL);
        waitUntilPageReady();

        if (!webDriver.getCurrentUrl().contains("login")) {
            return;
        }

        clearAndSendKeys("inputEmail", getConfiguration().getEmail());
        clearAndSendKeys("inputPassword", getConfiguration().getPassword(), Keys.ENTER);
        waitUntilPageReady();

        if (!webDriver.getCurrentUrl().contains("select_person")) {
            throw new IllegalStateException("credentials invalid");
        }

        webDriver.findElement(By.id("form_person_0")).click();
        webDriver.findElement(By.tagName("button")).click();
        waitUntilPageReady();

        if (!webDriver.getCurrentUrl().equals(URL)) {
            throw new IllegalStateException("person selection failed");
        }

        lastAction = System.currentTimeMillis();
        log.info("Successfully entered Bildungsplattform as {}", getConfiguration().getEmail());
    }

    private void selectPerson(final String email) {
        webDriver.get(SELECT_PERSON_URL + email);
    }

    private void clearAndSendKeys(final String elementId, final CharSequence... keysToSend) {
        final Supplier<WebElement> elementSupplier = () -> webDriver.findElement(By.id(elementId));
        elementSupplier.get().clear();
        for (final CharSequence sequence : keysToSend) {
            elementSupplier.get().sendKeys(sequence);
        }
    }

    private void waitUntilPageReady() {
        final WebDriverWait driverWait = new WebDriverWait(webDriver, Duration.ofSeconds(5));
        driverWait.until(WAIT_CONDITION);
    }
}
