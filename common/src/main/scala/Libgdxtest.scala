package libgdxtest
import scala.math._
import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.VertexAttributes.Usage
import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.graphics.Mesh
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader
import com.badlogic.gdx.graphics.g3d.Renderable
import com.badlogic.gdx.graphics.g3d.utils.RenderContext
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder
import Libgdxtest._

object Libgdxtest {
  implicit def doubleToFloat(double: Double): Float = double.toFloat
}
class Libgdxtest extends ApplicationListener {
  case class TickLabel(position: Vector3, string: String)
  case class Ticks(model: Model, labels: List[TickLabel])
  case class Axis(model: Model, ticks: Ticks)
  case class Axes(top: Axis, left: Axis, right: Axis, bottom: Axis, topLeft: Axis, topRight: Axis, bottomLeft: Axis, bottomRight: Axis)
  case class Grid(horizontal: Model, vertical: Model)
  case class Grids(base: Grid, top: Grid, left: Grid, right: Grid, bottom: Grid)
  lazy val camera: PerspectiveCamera = new PerspectiveCamera(30, Gdx.graphics.getWidth, Gdx.graphics.getHeight)
  lazy val modelBatch = new ModelBatch
  lazy val cameraInputController = new MyCameraInputController(camera)
  lazy val boxAxes = createAxes()
  lazy val grids = createGrids()
  lazy val models = List[Model](
    boxAxes.top.model,
    boxAxes.left.model,
    boxAxes.right.model,
    boxAxes.bottom.model,
    boxAxes.topLeft.model,
    boxAxes.topRight.model,
    boxAxes.bottomLeft.model,
    boxAxes.bottomRight.model,
    boxAxes.top.ticks.model,
    boxAxes.left.ticks.model,
    boxAxes.right.ticks.model,
    boxAxes.bottom.ticks.model,
    boxAxes.topLeft.ticks.model,
    boxAxes.topRight.ticks.model,
    boxAxes.bottomLeft.ticks.model,
    boxAxes.bottomRight.ticks.model,
    grids.base.horizontal,
    grids.base.vertical
    //    grids.top.horizontal,
    //    grids.top.vertical,
    //    grids.left.horizontal,
    //    grids.left.vertical,
    //    grids.right.horizontal,
    //    grids.right.vertical,
    //    grids.bottom.horizontal,
    //    grids.bottom.vertical,
//    referenceAxes
  )
  lazy val modelInstances = models.map(new ModelInstance(_))
  lazy val spriteBatch = new SpriteBatch
  lazy val font = new BitmapFont
  val minX: Double = -2
  val maxX: Double = 2
  val minY: Double = -2
  val maxY: Double = 2
  val minZ: Double = -2
  val maxZ: Double = 2
  val intervals: Int = 8
  val yIntervalLength = (maxY - minY) / intervals
  val xIntervalLength = (maxX - minX) / intervals
  val zIntervalLength = (maxZ - minZ) / intervals
  val tickLength: Double = xIntervalLength
  val labelOffset: Double = xIntervalLength

  def referenceAxes(): Model = {
    val modelBuilder = new ModelBuilder
    modelBuilder.begin
    val axesBuilder = modelBuilder.part("axes", GL20.GL_LINES, Usage.Position | Usage.Color, new Material)
    axesBuilder.setColor(Color.RED)
    axesBuilder.line(0, 0, 0, 100, 0, 0)
    axesBuilder.setColor(Color.GREEN)
    axesBuilder.line(0, 0, 0, 0, 100, 0)
    axesBuilder.setColor(Color.BLUE)
    axesBuilder.line(0, 0, 0, 0, 0, 100)
    modelBuilder.end
  }
  def createGrids(): Grids = {
    val modelBuilder = new ModelBuilder
    def createGridLines(parametrizedBeginEndPoints: Int => (Vector3, Vector3)): Model = {
      modelBuilder.begin
      val gridBuilder = modelBuilder.part("grid", GL20.GL_LINES, Usage.Position | Usage.Color, new Material)
      gridBuilder.setColor(Color.LIGHT_GRAY)
      for (i <- 1 until intervals) {
        val (begin, end) = parametrizedBeginEndPoints(i)
        gridBuilder.line(begin, end)
      }
      modelBuilder.end
    }
    val xStep = (i: Int) => (minX + i * yIntervalLength): Float
    val yStep = (i: Int) => (minY + i * xIntervalLength): Float
    val zStep = (i: Int) => (minZ + i * zIntervalLength): Float
    val gridBeginEndPoints = List(
      (yStep.andThen((step: Float) => new Vector3(minX, step, minZ) -> new Vector3(maxX, step, minZ)), // base
        xStep.andThen((step: Float) => new Vector3(step, minY, minZ) -> new Vector3(step, maxY, minZ))), // base
      (zStep.andThen((step: Float) => new Vector3(minX, maxY, step) -> new Vector3(maxX, maxY, step)), // top
        zStep.andThen((step: Float) => new Vector3(step, maxY, minZ) -> new Vector3(step, maxY, maxZ))), // top
      (zStep.andThen((step: Float) => new Vector3(minX, minY, step) -> new Vector3(minX, maxY, step)), // left
        yStep.andThen((step: Float) => new Vector3(minX, step, minZ) -> new Vector3(minX, step, maxZ))), // left
      (zStep.andThen((step: Float) => new Vector3(maxX, minY, step) -> new Vector3(maxX, maxY, step)), // right
        yStep.andThen((step: Float) => new Vector3(maxX, step, minZ) -> new Vector3(maxX, step, maxZ))), // right
      (zStep.andThen((step: Float) => new Vector3(minX, minY, step) -> new Vector3(maxX, minY, step)), // bottom
        xStep.andThen((step: Float) => new Vector3(step, minY, minZ) -> new Vector3(step, minY, maxZ))) // bottom
    )
    val grids =
      for ((horizontal, vertical) <- gridBeginEndPoints) yield {
        Grid(createGridLines(horizontal), createGridLines(vertical))
      }
    Grids(grids(0), grids(1), grids(2), grids(3), grids(4))
  }
  def createAxes(): Axes = {
    val modelBuilder = new ModelBuilder
    val axisPointPairs = List(
      (new Vector3(minX, maxY, minZ), new Vector3(maxX, maxY, minZ)), // top
      (new Vector3(minX, minY, minZ), new Vector3(minX, maxY, minZ)), // left
      (new Vector3(maxX, minY, minZ), new Vector3(maxX, maxY, minZ)), // right
      (new Vector3(minX, minY, minZ), new Vector3(maxX, minY, minZ)), // bottom
      (new Vector3(minX, maxY, minZ), new Vector3(minX, maxY, maxZ)), // topleft
      (new Vector3(maxX, maxY, minZ), new Vector3(maxX, maxY, maxZ)), // topright
      (new Vector3(minX, minY, minZ), new Vector3(minX, minY, maxZ)), // bottomleft
      (new Vector3(maxX, minY, minZ), new Vector3(maxX, minY, maxZ)) // bottomright
    )
    def createAxis(begin: Vector3, end: Vector3) = {
      def createAxisModel(begin: Vector3, end: Vector3) = {
        modelBuilder.begin
        val meshBuilder = modelBuilder.part("grid", GL20.GL_LINES, Usage.Position | Usage.Color, new Material)
        meshBuilder.setColor(Color.LIGHT_GRAY)
        meshBuilder.line(begin, end)
        modelBuilder.end
      }
      def createTicks() = {
        val ticks = {
          for (i <- 1 until intervals) yield {
            if (begin.x == end.x && begin.y == end.y && begin.x == minX && begin.y == maxY) { // topleft
              (new Vector3(minX, maxY, minZ + i * zIntervalLength), new Vector3((minX - tickLength / sqrt(2)).toFloat, (maxY + tickLength / sqrt(2)).toFloat, minZ + i * zIntervalLength), new Vector3((minX - (tickLength + labelOffset) / sqrt(2)).toFloat, (maxY + (tickLength + labelOffset) / sqrt(2)).toFloat, minZ + i * zIntervalLength), (minZ + i * zIntervalLength).toString)
            } else if (begin.x == end.x && begin.y == end.y && begin.x == maxX && begin.y == maxY) { // topright
              (new Vector3(maxX, maxY, minZ + i * zIntervalLength), new Vector3((maxX + tickLength / sqrt(2)).toFloat, (maxY + tickLength / sqrt(2)).toFloat, minZ + i * zIntervalLength), new Vector3((maxX + (tickLength + labelOffset) / sqrt(2)).toFloat, (maxY + (tickLength + labelOffset) / sqrt(2)).toFloat, minZ + i * zIntervalLength), (minZ + i * zIntervalLength).toString)
            } else if (begin.x == end.x && begin.y == end.y && begin.x == minX && begin.y == minY) { // bottomleft
              (new Vector3(minX, minY, minZ + i * zIntervalLength), new Vector3((minX - tickLength / sqrt(2)).toFloat, (minY - tickLength / sqrt(2)).toFloat, minZ + i * zIntervalLength), new Vector3((minX - (tickLength + labelOffset) / sqrt(2)).toFloat, (minY - (tickLength + labelOffset) / sqrt(2)).toFloat, minZ + i * zIntervalLength), (minZ + i * zIntervalLength).toString)
            } else if (begin.x == end.x && begin.y == end.y && begin.x == maxX && begin.y == minY) { // bottomRight
              (new Vector3(maxX, minY, minZ + i * zIntervalLength), new Vector3((maxX + tickLength / sqrt(2)).toFloat, (minY - tickLength / sqrt(2)).toFloat, minZ + i * zIntervalLength), new Vector3((maxX + (tickLength + labelOffset) / sqrt(2)).toFloat, (minY - (tickLength + labelOffset) / sqrt(2)).toFloat, minZ + i * zIntervalLength), (minZ + i * zIntervalLength).toString)
            } else if (begin.y == end.y && begin.y == maxY) { // top
              (new Vector3(minX + i * xIntervalLength, maxY, minZ), new Vector3(minX + i * xIntervalLength, maxY + tickLength, minZ), new Vector3(minX + i * xIntervalLength, maxY + tickLength + labelOffset, minZ), (minX + i * xIntervalLength).toString)
            } else if (begin.x == end.x && begin.x == minX) { // left
              (new Vector3(minX, minY + i * yIntervalLength, minZ), new Vector3(minX - tickLength, minY + i * yIntervalLength, minZ), new Vector3(minX - tickLength - labelOffset, minY + i * yIntervalLength, minZ), (minY + i * yIntervalLength).toString)
            } else if (begin.x == end.x && begin.x == maxX) { // right
              (new Vector3(maxX, minY + i * yIntervalLength, minZ), new Vector3(maxX + tickLength, minY + i * yIntervalLength, minZ), new Vector3(maxX + tickLength + labelOffset, minY + i * yIntervalLength, minZ), (minY + i * yIntervalLength).toString)
            } else if (begin.y == end.y && begin.y == minY) { // bottom
              (new Vector3(minX + i * xIntervalLength, minY, minZ), new Vector3(minX + i * xIntervalLength, minY - tickLength, minZ), new Vector3(minX + i * xIntervalLength, minY - tickLength - labelOffset, minZ), (minX + i * xIntervalLength).toString)
            } else {
              throw new Exception("Points don't form an axis")
            }
          }
        }
        modelBuilder.begin
        val meshBuilder = modelBuilder.part("grid", GL20.GL_LINES, Usage.Position | Usage.Color, new Material)
        meshBuilder.setColor(Color.LIGHT_GRAY)
        val tickLabels = (for ((begin, end, labelPosition, string) <- ticks) yield {
          meshBuilder.line(begin, end)
          TickLabel(labelPosition, string)
        }).toList
        val tickMesh = modelBuilder.end
        Ticks(tickMesh, tickLabels)
      }
      Axis(createAxisModel(begin, end), createTicks())
    }
    val axes = for ((begin, end) <- axisPointPairs) yield createAxis(begin, end)
    Axes(axes(0), axes(1), axes(2), axes(3), axes(4), axes(5), axes(6), axes(7))
  }
  val vertexShader =
    """|attribute vec3 a_position;
       |attribute vec3 a_barycentric;
	   |uniform mat4 u_projViewTrans;
       |varying vec3 vPosition;
       |varying vec3 vBarycentric;
	   |void main() {
       |  vPosition = a_position;
       |  vBarycentric = a_barycentric;
	   |  gl_Position = u_projViewTrans * vec4(a_position, 1.0);
	   |}
	   |""".stripMargin
  val fragmentShader =
    """|
       |#extension GL_OES_standard_derivatives : enable
       |#ifdef GL_ES
	   |  precision mediump float;
	   |#endif
       |uniform float minZ;
       |uniform float maxZ;
       |varying vec3 vPosition;
       |varying vec3 vBarycentric;
       |vec3 rainbow(in float position) {
       |  int numColourBars = 4;
       |  float m = float(numColourBars) * position;
       |  int n = int(m);
       |  float f = m - float(n);
       |  switch(n) {
       |    case 0: {
       |      return vec3(1.0, f, 0.0);
       |    };
       |    case 1: {
       |      return vec3(1.0 - f, 1.0, 0.0);
       |    };
       |    case 2: {
       |      return vec3(0.0, 1.0, f);
       |    };
       |    case 3: {
       |      return vec3(0.0, 1.0 - f, 1.0);
       |    };
       |    case 4: {
       |      return vec3(f, 0.0, 1.0);
       |    };
       |    case 5: {
       |      return vec3(1.0, 0.0, 1.0 - f);
       |    };
       |    default: {
       |      return vec3(0.5, 0.5, 0.0);
       |    };
       |  };
       |}
       |float alpha(in float zPosition) {
       |  if (zPosition < minZ || zPosition > maxZ) {
       |    return 0.0;
       |  } else {
       |    return 1.0;
       |  }
       |}
       |float edgeFactor() {
       |  vec3 d = fwidth(vBarycentric);
       |  vec3 a3 = smoothstep(vec3(0.0, 0.0, 0.0), d * 1, vBarycentric);
       |  return min(min(a3.x, a3.y), a3.z);
       |}
	   |void main() {
//       |  gl_FragColor = vec4(rainbow(1.0 - (vPosition.z - minZ)/(maxZ - minZ)), alpha(vPosition.z));
       |  gl_FragColor = vec4(mix(vec3(0.0), rainbow(1.0 - (vPosition.z - minZ)/(maxZ - minZ)), edgeFactor()), alpha(vPosition.z));
	   |}
	   |""".stripMargin

  val func = (x: Double, y: Double) => sin(x*x) + cos(y*y)
  lazy val mesh = createFunctionPlot(func)

  def createFunctionPlot(f: (Double, Double) => Double): Mesh = {
    val resolution = 50
    val xStep = (maxX - minX) / resolution
    val yStep = (maxY - minY) / resolution
    val xValues = Range.Double.inclusive(minX, maxX, xStep)
    val yValues = Range.Double.inclusive(minY, maxY, yStep)
    val red = Array[Float](1, 0, 0)
    val blue = Array[Float](0, 1, 0)
    val green = Array[Float](0, 0, 1)
    val colours = Vector(red, green, blue)
    val vertices = (for ((x, i) <- xValues.zipWithIndex; (y, j) <- yValues.zipWithIndex) yield {
      Array[Float](x, y, f(x, y).toFloat) ++ colours((j + i * 2) % colours.size)
    }).toArray.flatten
    val mesh = new Mesh(true, xValues.size * yValues.size, (xValues.size - 1) * (yValues.size - 1) * 3 * 2,
      new VertexAttribute(Usage.Position, 3, "a_position"),
      new VertexAttribute(Usage.Generic, 3, "a_barycentric")
    )
    mesh.setVertices(vertices)
    val arrayIndex = (x: Int, y: Int) => (x * yValues.size + y).toShort
    val triangleIndices = for {
      y <- 0 until yValues.size - 1
      x <- 0 until xValues.size - 1
    } yield {
      val topLeft = arrayIndex(x, y + 1)
      val topRight = arrayIndex(x + 1, y + 1)
      val bottomLeft = arrayIndex(x, y)
      val bottomRight = arrayIndex(x + 1, y)
      Array(topLeft, bottomLeft, bottomRight, topLeft, bottomRight, topRight)
    }
    mesh.setIndices(triangleIndices.toArray.flatten)
    mesh
  }

  override def create() {
    camera.up.x = 0
    camera.up.y = 0
    camera.up.z = 1
    camera.direction.x = 0
    camera.direction.y = 1
    camera.direction.z = 0
    camera.position.set(5*maxX, 5*minY, 5*maxZ);
    camera.lookAt((maxX + minX)/2, (maxY + minY)/2, (maxZ + minZ)/2);
    camera.near = 0.1f;
    camera.far = 1000f;
    camera.update();
    Gdx.input.setInputProcessor(cameraInputController)
    font.setColor(Color.RED)
    Gdx.graphics.setContinuousRendering(false)
  }

  override def render() = {
    Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight())
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT)
    cameraInputController.update
    val shader = new ShaderProgram(vertexShader, fragmentShader)
//    println(shader.isCompiled())
//    println(shader.getLog())
    spriteBatch.begin()
    def drawGridLabels(labels: List[TickLabel]) = {
    	labels.foreach {
    	case TickLabel(position, string) => {
    		val positionCopy = position.cpy
    				camera.project(positionCopy)
    				val bounds = font.getBounds(string)
    				font.draw(spriteBatch, string, positionCopy.x - bounds.width / 2, positionCopy.y + bounds.height / 2)
    	}
    	}
    }
    drawGridLabels(boxAxes.top.ticks.labels)
    drawGridLabels(boxAxes.left.ticks.labels)
    drawGridLabels(boxAxes.right.ticks.labels)
    drawGridLabels(boxAxes.bottom.ticks.labels)
    drawGridLabels(boxAxes.topLeft.ticks.labels)
    drawGridLabels(boxAxes.topRight.ticks.labels)
    drawGridLabels(boxAxes.bottomLeft.ticks.labels)
    drawGridLabels(boxAxes.bottomRight.ticks.labels)
    spriteBatch.end()
    modelBatch.begin(camera)
    modelInstances.foreach(modelBatch.render(_))
    modelBatch.end()
    shader.begin()
    shader.setUniformf("minZ", minZ)
    shader.setUniformf("maxZ", maxZ)
    shader.setUniformMatrix("u_projViewTrans", camera.combined)
//    Gdx.gl.glDepthMask(true)
//    Gdx.gl.glEnable(GL20.GL_BLEND)
//    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
    Gdx.gl.glEnable(GL20.GL_DEPTH_TEST)
    mesh.render(shader, GL20.GL_TRIANGLES)
    Gdx.gl.glDisable(GL20.GL_DEPTH_TEST)
//    Gdx.gl.glDisable(GL20.GL_BLEND)
//    Gdx.gl.glDepthMask(false)
    shader.end

  }

  override def dispose() = {
    modelBatch.dispose
    models.foreach(_.dispose)
  }
  override def resume() = {}
  override def resize(width: Int, height: Int) = {
    camera.viewportWidth = width;
    camera.viewportHeight = height;
    camera.update();
  }
  override def pause() = {}

}
