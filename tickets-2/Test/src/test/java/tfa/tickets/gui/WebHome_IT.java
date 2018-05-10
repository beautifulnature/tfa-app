package tfa.tickets.gui;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;

import net.sourceforge.jwebunit.htmlunit.HtmlUnitTestingEngineImpl;
import net.sourceforge.jwebunit.junit.JWebUnit;

import static net.sourceforge.jwebunit.junit.JWebUnit.*;

// Example of integration test using JWebUnit
public class WebHome_IT
{
    static final String ROOT_URL = "http://localhost:8080/tickets";

    private static WebClient wc = null;

    // For JS management with JWebUnit
    @SuppressWarnings("unused")
    private void waitJs()
    {
        // For some JS frameworks, wait max 3s for async javascript
        wc.waitForBackgroundJavaScript(3000);
    }

    @BeforeClass
    public static void setUpBefore() throws InterruptedException
    {
        // Precond : the application must be deployed and launched
        JWebUnit.setBaseUrl(ROOT_URL);
        setScriptingEnabled(true); // javascript on !

        HtmlUnitTestingEngineImpl engine = (HtmlUnitTestingEngineImpl) getTestingEngine();
        engine.setDefaultBrowserVersion(BrowserVersion.FIREFOX_38);

        // Open the welcome page ( try during 4*(2+2) = 16 seconds )
        for (int nb = 0;;)
        {
            try
            {
                // Try to connect ( timeout 2 s)
                beginAt("/");
                break;
            }
            catch (Exception e)
            {
                if (!e.getMessage().contains("Connection refused") || nb++ >= 4)
                    throw e;

                // retry after 2 s
                Thread.sleep(2000);
            }
        }

        // Client connection : first call
        wc = engine.getWebClient();
        wc.setCssErrorHandler(new SilentCssErrorHandler());
        wc.waitForBackgroundJavaScript(1000);
    }

    @AfterClass
    public static void tearDownAfter() throws Exception
    {
        // Close
        wc = null;
        closeBrowser();
    }

    @Test
    public void testFirstPage()
    {
        // Check welcome page
        gotoPage("");
        assertTextPresent("Hello");

        // Check link to rest call
        JWebUnit.clickLinkWithText("Click");
        assertTextPresent("visits");

        // assertEquals( "xxx", getElementTextByXPath("//table[@id='2']/tbody/tr[3]/td[4]") );
    }

}
