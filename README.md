This project contains a simple and very rough 3D function graph visualizer. It's written in Scala, Java, and using Libgdx for the graphics. The purpose was because I wanted a way to visualize functions in Scala (instead of MATLAB) and to test Libgdx.

To use it you'll want to go to common/src/main/scala/Libgdxtest.scala, modify the "func" value to whatever function you want to graph. Then do "sbt run/desktop" in the root of the multi-project. You can modify the bounding box of the graph by modifying "minX, maxX, minY, maxY, minZ, maxZ". lol

Note that you can only graph explicit form functions "z = f(x, y)". There is no adaptive sampling just simple grid sampling of which you can adjust the resolution. It will not know about discontinuities. You can also modify the shader to remove wireframes. To remove specific axes and axes labels you can comment them out. The are named like so:

    tl    t    tr
       oxxxxxxo 
       x      x
     l x base x r
       x      x
       oxxxxxxo
    bl    b     br
