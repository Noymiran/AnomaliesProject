package Backend.output

import com.solarmosaic.client.mail.configuration.{ PasswordAuthenticator, SmtpConfiguration }
import com.solarmosaic.client.mail.content.Text
import com.solarmosaic.client.mail.{ Envelope, Mailer }
import com.typesafe.config.ConfigFactory
import javax.mail.internet.InternetAddress
import utils.Logging

import scala.util.{ Failure, Success, Try }

object EmailClient extends Logging {

  lazy val config = ConfigFactory.load

  def sendMail(subject: String, message: String, receivers: Vector[String]): Try[Unit] = {
    val smtpServer = config getString ("anomaly-detector.email.smtp.server")
    val smtpPort = config getInt ("anomaly-detector.email.smtp.port")
    val user = config getString ("anomaly-detector.email.username")
    val password = config getString ("anomaly-detector.email.password")
    val sender = new InternetAddress(config getString ("anomaly-detector.email.from.email"))
    val tls = config getBoolean ("anomaly-detector.email.smtp.tls")
    val auth = PasswordAuthenticator(user, password)
    val smtpConfig = SmtpConfiguration(smtpServer, smtpPort, tls, authenticator = Some(auth))
    val mailer = Mailer(smtpConfig)
    val iAddress = receivers map (r => new InternetAddress(r))

    Try {
      val envelope = Envelope(
        from = sender,
        to = iAddress,
        subject = subject,
        content = Text(message)
      )

      mailer.send(envelope)
    } match {
      case Success(e) =>
        log.info(s"Mail sent to $iAddress")
        Success()
      case Failure(e) =>
        log.error("Failed to send email", e)
        Failure(e)
    }
  }
}
