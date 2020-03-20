package config

import play.api.ApplicationLoader.Context
import play.api._

class MacwiredApplicationLoader extends ApplicationLoader {
  def load(context: Context): Application = {
    LoggerConfigurator(context.environment.classLoader).foreach { _.configure(context.environment) }
    (new BuiltInComponentsFromContext(context) with Components).application
  }
}

import play.api.i18n._
import play.api.routing.Router
import router.Routes
import controllers.Assets
import play.api._

trait Components extends BuiltInComponents with AppComponents with I18nComponents {

  import com.softwaremill.macwire._

  lazy val assets: Assets = wire[Assets]
  lazy val prefix: String = "/"
  lazy val router: Router = wire[Routes]

  def langs: Langs
}
