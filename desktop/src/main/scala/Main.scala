package libgdxtest

import com.badlogic.gdx.backends.lwjgl._

object Main extends App {
    val cfg = new LwjglApplicationConfiguration
    cfg.title = "LibGDXTest"
    cfg.height = 768
    cfg.width = 1024
    cfg.samples = 4
    cfg.useGL20 = true
    cfg.forceExit = false
    new LwjglApplication(new Libgdxtest, cfg)
}
