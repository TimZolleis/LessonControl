package de.waldorfaugsburg.lessoncontrol.client.service.bbb;

import de.waldorfaugsburg.lessoncontrol.client.LessonControlClientApplication;
import de.waldorfaugsburg.lessoncontrol.client.service.AbstractService;
import de.waldorfaugsburg.lessoncontrol.client.util.DialogUtil;
import de.waldorfaugsburg.lessoncontrol.common.service.BBBServiceConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public final class BBBService extends AbstractService<BBBServiceConfiguration> {

    private static final ExpectedCondition<Boolean> WAIT_CONDITION =
            driver -> ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");

    private static final String URL = "https://app.bildungsplattform.org/";
    private static final Function<String, String> SELECT_PERSON_URL_FUNCTION = person -> URL + "user/select_person?_switch_user=" + person;
    private static final Function<String, String> COURSE_URL_FUNCTION = courseId -> URL + "courses/" + courseId;
    private static final Function<String, String> BBB_STOP_FUNCTION = courseId -> COURSE_URL_FUNCTION.apply(courseId) + "/bbb?status=stopped";

    private WebDriver webDriver;
    private long lastAction;

    private BBBServiceConfiguration.BBBSession currentSession;

    public BBBService(final LessonControlClientApplication application, final BBBServiceConfiguration configuration) {
        super(application, configuration);
    }

    @Override
    public void enable() throws Exception {
        System.setProperty("webdriver.chrome.driver", getConfiguration().getDriverPath());

        final ChromeOptions options = new ChromeOptions();
        final Map<String, Object> preferences = new HashMap<>();
        preferences.put("profile.default_content_setting_values.media_stream_mic", 1);
        preferences.put("profile.default_content_setting_values.media_stream_camera", 1);
        options.setExperimentalOption("prefs", preferences);
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--start-minimized");

        webDriver = new ChromeDriver(options);
        webDriver.manage().window().minimize();
        login();

        getApplication().getEventDistributor().call(BBBListener.class, listener -> listener.onSessionsReceived(getConfiguration().getSessions()));
    }

    @Override
    public void disable(boolean shutdown) throws Exception {
        if (shutdown) {
            stopCurrentSession();
            webDriver.quit();
        }
    }

    public void startSession(final BBBServiceConfiguration.BBBSession session) {
        // Ensure login
        login();

        if (currentSession != null) {
            stopCurrentSession();

            // Deselect previous person
            webDriver.get(SELECT_PERSON_URL_FUNCTION.apply("_exit"));
            waitUntilPageReady();
        }

        // Select new person
        webDriver.get(SELECT_PERSON_URL_FUNCTION.apply(session.getTeacherEmail()));
        waitUntilPageReady();

        // Open page of desired course
        webDriver.get(COURSE_URL_FUNCTION.apply(session.getCourseId()));
        waitUntilPageReady();

        final By joinButton = By.xpath("//a[contains(text(),'teilnehmen')]");
        final List<WebElement> elements = webDriver.findElements(joinButton);
        boolean sendInformation = true;
        if (!elements.isEmpty()) {
            final boolean reuseSession = DialogUtil.openYesNoQuestionDialog("BBB", "Ihre Sitzung im gewünschten Kurs läuft bereits! Möchten Sie diese laufende Sitzung verwenden?");
            if (reuseSession) {
                elements.get(0).click();
                sendInformation = false;
            } else {
                // Calling stop URL
                webDriver.get(BBB_STOP_FUNCTION.apply(session.getCourseId()));
                waitUntilPageReady();
            }
        }

        if (sendInformation) {
            // Enter course information
            clearAndSendKeys("big_blue_button_meeting_maxUsers", Integer.toString(session.getParticipantCount()));
            clearAndSendKeys("big_blue_button_meeting_duration", Integer.toString(session.getDuration()), Keys.ENTER);

            // Click new join button
            createWait().until(ExpectedConditions.elementToBeClickable(joinButton)).click();
        }

        // Switch tab to BBB session
        final String currentHandle = webDriver.getWindowHandle();
        final Set<String> windowHandles = webDriver.getWindowHandles();
        for (final String windowHandle : windowHandles) {
            if (!windowHandle.equals(currentHandle)) {
                webDriver.switchTo().window(windowHandle);
                webDriver.manage().window().maximize();
                break;
            }
        }
        waitUntilPageReady();

        // Select microphone mode
        createWait().until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@aria-label,\"Mit Mikrofon\")]"))).click();

        // Accept echo test
        createWait().until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@aria-label,\"Echo ist hörbar\")]"))).click();

        // Wait for echo test popup to vanish
        try {
            Thread.sleep(1500);
        } catch (final InterruptedException ignored) {
        }

        // Start video broadcast
        createWait().until(ExpectedConditions.elementToBeClickable(By.id("tippy-79"))).click();

        // Select OBS Virtual Camera as camera
        new Select(createWait().until(ExpectedConditions.elementToBeClickable(By.id("setCam")))).selectByVisibleText("OBS Virtual Camera");

        // Select High Definition as quality
        new Select(createWait().until(ExpectedConditions.elementToBeClickable(By.id("setQuality")))).selectByVisibleText("High Definition");

        // Wait for settings to be processed
        try {
            Thread.sleep(2000);
        } catch (final InterruptedException ignored) {
        }

        // Confirm video broadcast
        createWait().until(ExpectedConditions.elementToBeClickable(By.xpath("//span[contains(text(),'Freigabe starten')]"))).click();

        try {
            Thread.sleep(2000);
        } catch (final InterruptedException ignored) {
        }

        // Hide presentation
        createWait().until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@aria-label,\"Präsentation verbergen\")]"))).click();

        currentSession = session;
        getApplication().getEventDistributor().call(BBBListener.class, listener -> listener.onSessionStart(session));
    }

    public void stopCurrentSession() {
        if (currentSession == null) return;

        webDriver.close();
        for (final String windowHandle : webDriver.getWindowHandles()) {
            webDriver.switchTo().window(windowHandle);
            break;
        }

        webDriver.manage().window().minimize();

        webDriver.get(BBB_STOP_FUNCTION.apply(currentSession.getCourseId()));
        waitUntilPageReady();

        webDriver.get(SELECT_PERSON_URL_FUNCTION.apply("_exit"));
        waitUntilPageReady();

        currentSession = null;
        getApplication().getEventDistributor().call(BBBListener.class, BBBListener::onSessionStop);
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

        log.info("Trying to log-in as '{}' for BBB", getConfiguration().getEmail());

        clearAndSendKeys("inputEmail", getConfiguration().getEmail());
        clearAndSendKeys("inputPassword", getConfiguration().getPassword(), Keys.ENTER);
        waitUntilPageReady();

        if (!webDriver.getCurrentUrl().contains("select_person")) {
            log.error("Invalid credentials given for BBB! Please check. (URL was {})", webDriver.getCurrentUrl());
            DialogUtil.openErrorDialog("BBB", "Ungültige Zugangsdaten!");
            return;
        }

        webDriver.findElement(By.id("form_person_0")).click();
        webDriver.findElement(By.tagName("button")).click();
        waitUntilPageReady();

        if (!webDriver.getCurrentUrl().equals(URL)) {
            log.error("Person selection failed! Please check. (URL was {})", webDriver.getCurrentUrl());
            DialogUtil.openErrorDialog("BBB", "Personenauswahl fehlgeschlagen!");
            return;
        }

        lastAction = System.currentTimeMillis();
        log.info("Successfully logged-in as '{}' for BBB", getConfiguration().getEmail());
    }

    private void clearAndSendKeys(final String elementId, final CharSequence... keysToSend) {
        final Supplier<WebElement> elementSupplier = () -> webDriver.findElement(By.id(elementId));
        elementSupplier.get().clear();
        for (final CharSequence sequence : keysToSend) {
            elementSupplier.get().sendKeys(sequence);
        }
    }

    private void waitUntilPageReady() {
        createWait().until(WAIT_CONDITION);
    }

    private WebDriverWait createWait() {
        return new WebDriverWait(webDriver, Duration.ofSeconds(10));
    }
}
