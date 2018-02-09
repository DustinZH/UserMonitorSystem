# state decoder server
This decoder server was created by Zhenghao Dong. The geolocation data is provided by Vistar Media.
The key point to solve the puzzle is that how can we convert real-world problem to math problem.
My idea is that how can we check the test point is in a complext polygon. That's what I need solve.
I have designed three algorithm to parse the geolocation data(lontitude,latitude) to state name in USA.
In this semester, I am studying nodejs+express. So I use them as the web server framework.

## Structure of web server nodejs(v6.11.4)
- .idea
- bin
- node_mudules
- public
- routes
  - index.js (router code,which use to deal GET/POST)
- tools
  - [GeoDecoder.js](##Algorithm Detail) 
  - states.json (I revise it as a JsonArray,it's easy to tranverse)
- view
- app.js (bootstrap file)
- package.json
- state-decoder-server.iml

## Test conmand
```sh
1. cd state-decoder-server
2. node app.js &

3. curl  -d "longitude=-77.036133&latitude=40.513799" http://localhost:8080/             (use ray algorithm)
   or
   curl  -d "longitude=-77.036133&latitude=40.513799&version=v2" http://localhost:8080/  (use winding number algorithm)
```

## Alogrithm Theory
### Ray algorithm (geoDecodeV1)
(1)Theory: test how many times a ray, starting from the test point and going in any fixed direction,
intersects the edges of the polygon. if the test point is in the polygon, it must intersects with polygon even times.

(2)Usecase: Only can use in simple polygon,which doesn't have any self-intersection.

### Winding number algorithm(geoDecodev2)
(1)Theory: Count the winding number of polygon to the test point, if winding number != 0, the point is in the state.
First we define the cycle direction of polygon. Then, The definition of Count winding number has two conditions: (a)The test point is in the same area of y-axis of line-segment,which is connected by two neigbor border points.(b)The point is in the left side of x-axis of segment. After test point satisfies the two conditions, when the line-segment is  downWardEdge, winding number minus one. When the line-segment is upWardEdge, winding number plus one.

(2)Usecase: Compared with "Ray algorithm", the winding number not only can use in simple polygon, but aslo can use in the more complex polygon which has self-intersection. This alogorithm can detect on which layer the point is. 

### Area equality algorithm(geoDecodev3) Deprecated !!
(1)Theory: test whether area of polygon equals sum of area of triangles,which are composed by test point with two neighbor border points.

(2)Usecase: only can use in simple polygon without Concave shape or self-intersection point
