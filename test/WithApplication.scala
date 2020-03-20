package config

import play.api._
import play.api.test._
import play.api.{ Application, ApplicationLoader, BuiltInComponentsFromContext }
import play.api.ApplicationLoader.Context

class WithRealApplication extends WithApplicationLoader(new MacwiredApplicationLoader)
class WithTestApplication extends WithApplicationLoader(new TestApplicationLoader)

class TestApplicationLoader extends ApplicationLoader {
  def load(context: Context): Application = {
    LoggerConfigurator(context.environment.classLoader)
    (new BuiltInComponentsFromContext(context) with Components with TestAppComponents).application
  }
}
