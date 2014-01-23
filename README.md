This project contains a simple and very rough 3D function graph visualizer. It's written in Scala, Java, and using Libgdx for the graphics.

To use it you'll want to go to common/src/main/scala/Libgdxtest.scala, modify the "func" value to whatever function you want to graph. Then do "sbt run/desktop" in the root of the multi-project. You can modify the bounding box of the graph by modifying "minX, maxX, minY, maxY, minZ, maxZ". lol

Note that you can only graph explicit form functions "z = f(x, y)". There is no adaptive sampling just simple grid sampling of which you can adjust the resolution. You can also modify the shader to remove wireframes.
