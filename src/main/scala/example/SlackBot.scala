package example
import com.flyberrycapital.slack.SlackClient

object SlackBot {
  def main(args: Array[String]): Unit = {

    val s = new SlackClient("token")
    val response = s.chat.postMessage("#channel_name", "Init Message")
    s.chat.update(response, "This is my update.")
  }
}
