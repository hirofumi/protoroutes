package protoroutes.examples.play26.ajax

import java.text.MessageFormat
import java.util.ResourceBundle
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.scalatestplus.play.BrowserFactory
import org.scalatestplus.play.BrowserFactory.UnavailableDriver

trait HeadlessChromeFactory extends BrowserFactory {

  def createWebDriver(): WebDriver =
    try {
      val options = new ChromeOptions()
      // Windows environment requires `--disable-gpu` for now.
      // See https://developers.google.com/web/updates/2017/04/headless-chrome?hl=en#cli
      options.addArguments("--headless", "--disable-gpu")
      new ChromeDriver(options)
    } catch {
      case e: Throwable => HeadlessChromeFactory.unavailableDriver(e)
    }

}

object HeadlessChromeFactory extends HeadlessChromeFactory {

  private[this] lazy val bundle: ResourceBundle =
    ResourceBundle.getBundle("org.scalatestplus.play.ScalaTestPlusPlayBundle")

  private def unavailableDriver(e: Throwable): UnavailableDriver =
    UnavailableDriver(
      Some(e),
      MessageFormat.format(bundle.getString("cantCreateChromeDriver"), e.getMessage)
    )

}
